package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.nh.billing.config.Channels;
import org.nh.billing.domain.dto.Medication;
import org.nh.billing.service.InvoiceService;
import org.nh.billing.util.AdmissionDetailsUtil;
import org.nh.common.dto.*;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.repository.search.DispenseSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.FreemarkerService;
import org.nh.pharmacy.service.IpPrintService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.print.PdfGenerator;
import org.nh.print.barcode.BarCodeGenerator;
import org.nh.security.util.UserPreferencesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForList;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForObject;


/**
 * Service Implementation for managing Dispense.
 */
@Service
@Transactional
public class IpPrintServiceImpl implements IpPrintService {

    private final Logger log = LoggerFactory.getLogger(IpPrintServiceImpl.class);

    private final DispenseRepository dispenseRepository;

    private final DispenseSearchRepository dispenseSearchRepository;

    private final InvoiceService invoiceService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ApplicationProperties applicationProperties;


    private final FreemarkerService freemarkerService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    private final MessageChannel documentAuditChannel;


    @Value("${server.port}")
    private String portNo;

    @Value("${spring.mail.fromMail}")
    private String fromMail;

    @Value("${spring.mail.fromText}")
    private String fromText;

    public IpPrintServiceImpl(DispenseRepository dispenseRepository, DispenseSearchRepository dispenseSearchRepository, ApplicationProperties applicationProperties,
                              @Qualifier(Channels.PAM_PH_OUTPUT) MessageChannel pamIntegrationChannel, StockService stockService,
                              ElasticsearchOperations elasticsearchTemplate,
                              InvoiceService invoiceService, FreemarkerService freemarkerService, PharmacyRedisCacheService pharmacyRedisCacheService,
                              @Qualifier(org.nh.pharmacy.config.Channels.DMS_DOCUMENT_AUDIT) MessageChannel documentAuditChannel) {
        this.dispenseRepository = dispenseRepository;
        this.dispenseSearchRepository = dispenseSearchRepository;
        this.applicationProperties = applicationProperties;
        this.invoiceService = invoiceService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.freemarkerService = freemarkerService;

        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.documentAuditChannel = documentAuditChannel;
    }

    private Dispense getDispense(Long dispenseId, String dispenseNumber) {
        Dispense dispense = null;
        if (dispenseId != null) {
            dispense = dispenseSearchRepository.findOne(dispenseId);
        } else if (dispenseNumber != null) {
            dispense = dispenseSearchRepository.findByDocumentNumber(dispenseNumber);
        }
        return dispense;
    }

    @Override
    public byte[] getIPIssueSlipByDispenseId(Long dispenseId, String dispenseNumber, String original) throws Exception {
        Dispense dispense = getDispense(dispenseId, dispenseNumber);
        //  String fileName = dispense.getDocument().getSource().getReferenceNumber();
        String fileName = dispense.getDocument().getDispenseNumber();
        String fileAbsolutePath = applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html");
        File file = new File(fileAbsolutePath);
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;

        Query query =  new NativeSearchQueryBuilder()
            .withQuery(new QueryStringQueryBuilder(
                new StringBuilder("documentNumber:\"")
                    .append(dispense.getDocumentNumber())
                    .append("\" ")
                    .append("documentType:\"").append("IP_DISPENSE")
                    .append("\" ")
                    .append("auditType:").append("PRINT").toString()).defaultOperator(Operator.AND))
            .build();
        long count = elasticsearchTemplate.count(query, IndexCoordinates.of(DocumentAuditDTO.DOCUMENT_NAME));
        if (file.exists()) {
            //Update generatedBy and generatedOn in file, If file exist than create pdf
            UserDTO user = loadLoggedInUser();
            String generatedBy = user.getDisplayName() + ", " + user.getEmployeeNo();
            String generateOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
            Map<String, Object> outData = this.getIPIssueSlipHTMLByDispenseId(dispenseId, dispenseNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPdf(file, generatedBy, generateOn);
            if (StringUtils.isEmpty(original) && count > 0 ) {
                contentInBytes = PdfGenerator.addWaterMarkToPdf(contentInBytes, "DUPLICATE");
            }
        } else {
            Map<String, Object> outData = this.getIPIssueSlipHTMLByDispenseId(dispenseId, dispenseNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPDF(htmlData);
        }

        UserDTO userDTO = new UserDTO();
        org.nh.security.dto.User currentUser = UserPreferencesUtils.getCurrentUserPreferences().getUser();
        userDTO.setId(currentUser.getId());
        userDTO.setLogin(currentUser.getLogin());
        userDTO.setEmployeeNo(currentUser.getEmployeeNo());
        userDTO.setDisplayName(currentUser.getDisplayName());
        documentAuditChannel.send(MessageBuilder.withPayload(new DocumentAuditDTO("PRINT",
            dispense.getDocumentNumber(), "IP_DISPENSE",
            Instant.now(), userDTO)).build());

        return contentInBytes;
    }
    
    @Override
    public Map<String, Object> getIPIssueSlipHTMLByDispenseId(Long dispenseId, String dispenseNumber) throws Exception {
        log.debug("dispenseId: {}, dispenseNumber: {}", dispenseId, dispenseNumber);

        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "ipPatientSlip.ftl"; // Fixed template

        Dispense dispense = getDispense(dispenseId, dispenseNumber);
        //   String fileName = dispense.getDocument().getSource().getReferenceNumber();
        String fileName = dispense.getDocument().getDispenseNumber();
        printFile.put("fileName", fileName);
        Map<String, Object> issueSlipData = populateissueSlipData(dispense);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, issueSlipData);
        printFile.put("html", html);
        byte[] contentInBytes = html.getBytes();
        printFile.put("content", contentInBytes);
        if (DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus())) {
                 createHTMLFile(html, fileName);
        }
        return printFile;
    }

    /**
     * Create html file
     *
     * @param html
     * @param fileName
     */
    private void createHTMLFile(String html, String fileName) {
        FileOutputStream fop = null;
        File file = null;
        try {
            file = new File(applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html"));
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = html.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Map<String, Object> populateissueSlipData(Dispense dispense)throws Exception {
        log.debug("in populateissueSlipData:-" + dispense.getDocumentNumber());

        UserDTO user = loadLoggedInUser();
        Organization org = getOrganizationData(dispense.getDocument().getDispenseUnit().getId());
        Map<String, Object> issueSlipData = new HashMap<>();
        issueSlipData.put("patientName", null != dispense.getDocument().getPatient() ? dispense.getDocument().getPatient().getDisplayName() : "");

        String gender = null;

        if (null != dispense.getDocument().getPatient().getGender() && !dispense.getDocument().getPatient().getGender().isEmpty()) {

            gender = dispense.getDocument().getPatient().getGender();
        }
        if (null != dispense.getDocument().getPatient().getMrn()) {
            issueSlipData.put("barcodeImage", BarCodeGenerator.generateBarCodeAsBase64Image(dispense.getDocument().getPatient().getMrn().trim()));
        } else {
            issueSlipData.put("barcodeImage", null);
        }

        String age = null;
        if (null != dispense.getDocument().getPatient()) {
            if (null != dispense.getDocument().getPatient().getAgeDTO() && !dispense.getDocument().getPatient().getAgeDTO().toString().isEmpty()) {

                AgeDTO agetDTO = dispense.getDocument().getPatient().getAgeDTO();
                if (agetDTO.getYears() >= 1) {
                    age = agetDTO.getYears() + "y ";
                    if (agetDTO.getMonths() >= 1) {
                        age += agetDTO.getMonths() + "m";
                    }
                } else if (agetDTO.getYears() < 1 && agetDTO.getMonths() >= 1) {
                    age = agetDTO.getMonths() + "m";
                } else if (agetDTO.getYears() < 1 && agetDTO.getMonths() < 1) {
                    age = agetDTO.getDays() + "d";
                }
            }
        }

        StringJoiner genderAge = new StringJoiner(", ");
        if (null != gender) {
            genderAge.add(gender);
        }
        if (null != age) {
            genderAge.add(age);
        }
        if (!genderAge.toString().isEmpty()) {
            issueSlipData.put("genderAge", genderAge.toString());
        }
        issueSlipData.put("patientMrn", dispense.getDocument().getPatient().getMrn() != null ? dispense.getDocument().getPatient().getMrn() : dispense.getDocument().getPatient().getTempNumber());
        issueSlipData.put("dept", null!=dispense.getDocument().getDepartment()?null!=dispense.getDocument().getDepartment().getName()?dispense.getDocument().getDepartment().getName():"":"");
        issueSlipData.put("consultantName", dispense.getDocument().getConsultant().getDisplayName());
        issueSlipData.put("visitNo", dispense.getDocument().getEncounter() != null ? (dispense.getDocument().getEncounter().getVisitNumber() != null ? dispense.getDocument().getEncounter().getVisitNumber() : "-") : "-");


        InPatientDTO inpatientData= AdmissionDetailsUtil.getInPatientByEncounterNumber(dispense.getDocument().getEncounter().getDocumentNumber(),elasticsearchTemplate);
        String orderNo="";
        LocalDateTime orderedDate=null;
        String createdBy="";
        if (null!=dispense.getDocument().getSourceDTOList())
        {
            SourceDTO source = dispense.getDocument().getSourceDTOList().stream().
                filter(sourceDTO ->  DocumentType.AMBULATORY.equals(sourceDTO.getDocumentType())).findFirst().get();
            if (null!=source) {
                List<MedicationRequest> orderMedicationDeatils = getOrderMedicationDeatils(source.getId(), source.getReferenceNumber());
                orderNo = orderMedicationDeatils.get(0).getDocumentNumber();
                orderedDate = orderMedicationDeatils.get(0).getDocument().getCreatedDate();
                createdBy = orderMedicationDeatils.get(0).getDocument().getCreatedBy().getDisplayName();
                if (null != orderNo) {
                    issueSlipData.put("orderBarcodeImage", BarCodeGenerator.generateBarCodeAsBase64Image(orderNo));
                } else {
                    issueSlipData.put("orderBarcodeImage", null);
                }
            }
        }
        issueSlipData.put("orderNo", null!=orderNo?orderNo:"-");
        issueSlipData.put("orderedOn", null!=orderedDate?orderedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")):"-");
        issueSlipData.put("requestNo", "-");
        issueSlipData.put("store", null!=dispense.getDocument().getHsc()?dispense.getDocument().getHsc().getName():"");
        issueSlipData.put("orderedBy", null!=createdBy?createdBy:"-");
        issueSlipData.put("issueNo",dispense.getDocumentNumber());
        issueSlipData.put("issueOn", null!=dispense.getDocument().getCreatedDate()?dispense.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")):"");
        issueSlipData.put("issuedBy", null!=dispense.getDocument().getCreatedBy()?dispense.getDocument().getCreatedBy().getDisplayName():"");
        for (int i = 0; i < dispense.getDocument().getDispenseDocumentLines().size(); i++) {
            if (null != dispense.getDocument().getDispenseDocumentLines().get(i).getMedication() && null == dispense.getDocument().getDispenseDocumentLines().get(i).getMedication().getManufacturer()) {
                setmanufacturerifNull(dispense.getDocument().getDispenseDocumentLines().get(i).getMedication());
            }
        }
        issueSlipData.put("documentLines", dispense.getDocument().getDispenseDocumentLines());
        issueSlipData.put("remarks",null!=dispense.getDocument().getRemarks()?dispense.getDocument().getRemarks():"");
        issueSlipData.put("preparedBy", dispense.getDocument().getCreatedBy() != null ? dispense.getDocument().getCreatedBy().getDisplayName() + ", " + dispense.getDocument().getCreatedBy().getEmployeeNo() : "-");
        issueSlipData.put("preparedOn", dispense.getDocument().getCreatedDate() != null ? dispense.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "-");
        issueSlipData.put("generatedBy", user != null ?user.getDisplayName() + ", " + user.getEmployeeNo() : "-");
        issueSlipData.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        issueSlipData.put("unitDisplayName", null!=dispense.getDocument().getDispenseUnit()?dispense.getDocument().getDispenseUnit().getName():"");
        issueSlipData.put("hscDisplayName", null!=dispense.getDocument().getHsc().getName()?dispense.getDocument().getHsc().getName():"");
        issueSlipData.put("unitAddress", org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString().equalsIgnoreCase("work")).findAny().orElse(null));
        String currentWard="";
        String currentBed="";
        if(null!=dispense.getDocument().getEncounter().getDocumentNumber()) {
            currentWard = inpatientData.getWard().getName();
            currentBed = inpatientData.getBed().getCode();
        }
        issueSlipData.put("currentWard", currentWard);
        issueSlipData.put("currentBed", currentBed);
        issueSlipData.put("ward",null);
        if(!dispense.getDocument().getDispenseType().name().equals("DIRECT_ISSUE")) {
            issueSlipData.put("ward", null != dispense.getDocument().getOrderingHSC() ? dispense.getDocument().getOrderingHSC().getName() : "");
        }
        return issueSlipData;
    }

    private void setmanufacturerifNull(Medication medication) {
        ItemDTO item = queryForObject("item", new CriteriaQuery(new Criteria("code.raw").is(medication.getCode())), elasticsearchTemplate, ItemDTO.class);
        medication.setManufacturer(item.getManufacturer());
    }

    private List<MedicationRequest> getOrderMedicationDeatils(Long id,String referenceNumber) {
        Query query = new NativeSearchQueryBuilder()
            .withQuery(boolQuery()
                .must(matchQuery("id", id))
                .must(matchQuery("documentNumber.raw", referenceNumber))
            ).build();
        List<MedicationRequest> orderMedicationDeatils = queryForList("amb_medicationrequest",query,elasticsearchTemplate ,MedicationRequest.class);
        log.debug("SearchQuery" + query);
        return orderMedicationDeatils;
    }

    private Organization getOrganizationData(Long unitId) {
        Organization org;
        String cacheKey = "PHR: unit.id:"+unitId;
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            org = pharmacyRedisCacheService.fetchOrganization(elasticsearchTemplate,unitId,cacheKey);
        }else
        {
            org = queryForObject("organization", new CriteriaQuery(new Criteria("id").is(unitId)), elasticsearchTemplate, org.nh.pharmacy.domain.Organization.class);
        }
        return org;
    }

    private UserDTO loadLoggedInUser() {
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            String cacheKey = Constants.USER_LOGIN+SecurityUtils.getCurrentUserLogin().get();
            return pharmacyRedisCacheService.getUserData(cacheKey,elasticsearchTemplate);
        }else {
            return ElasticSearchUtil.queryForObject("user", new CriteriaQuery(new Criteria("login.raw").is(SecurityUtils.getCurrentUserLogin().get())), elasticsearchTemplate, UserDTO.class);
        }
    }

}

