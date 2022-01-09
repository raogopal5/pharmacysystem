package org.nh.pharmacy.service.impl;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.nh.billing.util.AdmissionDetailsUtil;
import org.nh.common.dto.InPatientDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.dto.DispenseReturnDocument;
import org.nh.pharmacy.domain.dto.DispenseReturnDocumentLine;
import org.nh.pharmacy.repository.DispenseReturnRepository;
import org.nh.pharmacy.repository.search.DispenseReturnSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.FreemarkerService;
import org.nh.pharmacy.service.IpDispenseReturnPrintService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.FreeMarkerUtil;
import org.nh.print.PdfGenerator;
import org.nh.print.barcode.BarCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.nh.common.util.BigDecimalUtil.add;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForList;
import static org.nh.pharmacy.util.ElasticSearchUtil.queryForObject;

/**
 * Service Implementation for managing IpDispenseReturnPrint.
 */
@Service("ipDispenseReturnPrintService")
@Transactional
public class IpDispenseReturnPrintServiceImpl implements IpDispenseReturnPrintService {
    private final Logger log = LoggerFactory.getLogger(DispenseReturnServiceImpl.class);

    private final DispenseReturnRepository dispenseReturnRepository;

    private final DispenseReturnSearchRepository dispenseReturnSearchRepository;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final FreemarkerService freemarkerService;

    private final ApplicationProperties applicationProperties;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

        public IpDispenseReturnPrintServiceImpl(DispenseReturnRepository dispenseReturnRepository, DispenseReturnSearchRepository dispenseReturnSearchRepository, ElasticsearchOperations elasticsearchTemplate, FreemarkerService freemarkerService, ApplicationProperties applicationProperties, PharmacyRedisCacheService pharmacyRedisCacheService) {
                this.dispenseReturnRepository = dispenseReturnRepository;
                this.dispenseReturnSearchRepository = dispenseReturnSearchRepository;
                this.elasticsearchTemplate = elasticsearchTemplate;
                this.freemarkerService = freemarkerService;
                this.applicationProperties = applicationProperties;
            this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        }
        @Override
        public byte[] getIpDispenseReturnPdf(Long dispenseReturnId, String dispenseReturnNumber) throws Exception {

        DispenseReturn dispenseReturn = getDispenseReturn(dispenseReturnId, dispenseReturnNumber);
        String fileName = dispenseReturn.getDocumentNumber();
        String fileAbsolutePath = applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html");
        File file = new File(fileAbsolutePath);
        byte[] contentInBytes = null;
        if (file.exists()) {
            UserDTO userDTO = loadLoggedInUser();
            String generatedBy = userDTO.getDisplayName() + ", " + userDTO.getEmployeeNo();
            String generateOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
            Map<String, Object> outData = this.getIpDispenseReturnPdfHTML(dispenseReturnId, dispenseReturnNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPdf(file, generatedBy, generateOn);
        } else {
            Map<String, Object> outData = this.getIpDispenseReturnPdfHTML(dispenseReturnId, dispenseReturnNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPDF(htmlData);
        }

        return contentInBytes;
    }
    public Map<String, Object> getIpDispenseReturnPdfHTML(Long dispenseReturnId, String dispenseReturnNumber) throws Exception {
        log.debug("dispenseReturnId: {}, dispenseReturnNumber: {}", dispenseReturnId, dispenseReturnNumber);

        FileOutputStream fop = null;
        File file;
        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "ipDispenseReturn.ftl"; // Fixed template
        DispenseReturn dispenseReturn = getDispenseReturn(dispenseReturnId, dispenseReturnNumber);
        String fileName = dispenseReturn.getDocumentNumber();
        printFile.put("fileName", fileName);

        Map<String, Object> returnData = populateIpDispenseReturnData(dispenseReturn);

        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, returnData);
        printFile.put("html", html);
        try {
            file = new File(applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html"));
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = html.getBytes();
            printFile.put("content", contentInBytes);
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
        return printFile;
    }
    private Map<String, Object> populateIpDispenseReturnData(DispenseReturn dispenseReturn) throws Exception {
        DispenseReturnDocument returnDocument = dispenseReturn.getDocument();
        UserDTO user = loadLoggedInUser();
        Organization org = getOrganizationData(dispenseReturn.getDocument().getReturnhsc().getPartOf().getId());
        Map<String, Object> ipDispenseReturnData = new HashMap<>();
        Map<String, List<String>> manufacturerAndSchedule = getManufacturerAndSchedule(returnDocument.getDispenseReturnDocumentLines());
       if (null != returnDocument.getPatient().getMrn()) {
            ipDispenseReturnData.put("barcodeImage", BarCodeGenerator.generateBarCodeAsBase64Image(returnDocument.getPatient().getMrn().trim()));
        } else {
            ipDispenseReturnData.put("barcodeImage", null);
        }
        if (null != returnDocument.getPatient().getMrn()) {
            ipDispenseReturnData.put("returnBarcodeImage", BarCodeGenerator.generateBarCodeAsBase64Image(dispenseReturn.getDocumentNumber()));
        } else {
            ipDispenseReturnData.put("returnBarcodeImage", null);
        }

        ipDispenseReturnData.put("patientName", returnDocument.getPatient().getFullName());
        ipDispenseReturnData.put("patientMrn", returnDocument.getPatient().getMrn() != null ? returnDocument.getPatient().getMrn() : returnDocument.getPatient().getTempNumber());
        ipDispenseReturnData.put("patientPhoneNo", returnDocument.getPatient().getMobileNumber());
        InPatientDTO inpatientData= AdmissionDetailsUtil.getInPatientByEncounterNumber(returnDocument.getEncounter().getDocumentNumber(),elasticsearchTemplate);
        ipDispenseReturnData.put("ward", null!=inpatientData.getWard()?inpatientData.getWard().getName():"");
        ipDispenseReturnData.put("bed", null!=inpatientData.getBed()?inpatientData.getBed().getCode():"");
        ipDispenseReturnData.put("consultantName", null!=returnDocument.getEncounter()?null!=inpatientData.getAdmissionDetails()?null!=inpatientData.getAdmissionDetails().getPrimaryConsultant()?inpatientData.getAdmissionDetails().getPrimaryConsultant().getDisplayName():"":"":"");
        ipDispenseReturnData.put("department", null!=returnDocument.getEncounter()?null!=returnDocument.getEncounter().getDepartment()?returnDocument.getEncounter().getDepartment().getName():"":"");
        ipDispenseReturnData.put("returnNo", dispenseReturn.getDocumentNumber());
        ipDispenseReturnData.put("returnDate", returnDocument.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));
        ipDispenseReturnData.put("returnBy", null!=returnDocument.getReceivedBy()?returnDocument.getReceivedBy().getDisplayName():"");
        ipDispenseReturnData.put("returnStore",null!=returnDocument.getReturnhsc()?returnDocument.getReturnhsc().getName():"");
        ipDispenseReturnData.put("returnRequestNumber",returnDocument.getReturnRequestNumber());
        ipDispenseReturnData.put("requestedBy",null!=returnDocument.getRequestedBy()?returnDocument.getRequestedBy().getDisplayName():"");
        ipDispenseReturnData.put("dispenseItems", returnDocument.getDispenseReturnDocumentLines());
        String preparedBy = returnDocument.getCreatedBy() != null ? returnDocument.getCreatedBy().getDisplayName() + ", " + returnDocument.getCreatedBy().getEmployeeNo() : "-";
        ipDispenseReturnData.put("preparedBy", preparedBy);
        ipDispenseReturnData.put("preparedOn", returnDocument.getCreatedDate() != null ? returnDocument.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "-");
        ipDispenseReturnData.put("generatedBy", user != null ?user.getDisplayName() + ", " + user.getEmployeeNo() : "-");
        ipDispenseReturnData.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        ipDispenseReturnData.put("netAmount", returnDocument.getNetAmount());
        ipDispenseReturnData.put("roundOff", returnDocument.getRoundOff());
        ipDispenseReturnData.put("remarks", null!=returnDocument.getRemarks()?returnDocument.getRemarks():"");
        ipDispenseReturnData.put("status", returnDocument.getReturnStatus());
        ipDispenseReturnData.put("unitDisplayName", null!=dispenseReturn.getDocument().getReturnhsc()?null!=dispenseReturn.getDocument().getReturnhsc().getPartOf()?dispenseReturn.getDocument().getReturnhsc().getPartOf().getName():"":"");
        ipDispenseReturnData.put("hscDisplayName",null!=returnDocument.getReturnhsc()?returnDocument.getReturnhsc().getName():"");
        ipDispenseReturnData.put("unitAddress", org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
            .equalsIgnoreCase("work")).findAny().orElse(null));
        ipDispenseReturnData.put("manufacutererAndSchedule", manufacturerAndSchedule);
             ipDispenseReturnData.put("convertDecimal", new FreeMarkerUtil());
               return ipDispenseReturnData;
    }
    private DispenseReturn getDispenseReturn(Long dispenseReturnId, String dispenseReturnNumber) {
        DispenseReturn dispenseReturn = null;
        if (dispenseReturnId != null) {
            dispenseReturn = dispenseReturnSearchRepository.findById(dispenseReturnId).get();
        } else if (dispenseReturnNumber != null) {
            dispenseReturn = dispenseReturnSearchRepository.findByDocumentNumber(dispenseReturnNumber);
        }

        return dispenseReturn;
    }
    private Map<String, List<String>> getManufacturerAndSchedule(List<DispenseReturnDocumentLine> returnItems) {
        List<String> codes = new ArrayList<>();
        returnItems.stream().forEach(returnItem -> codes.add(QueryParser.escape(returnItem.getCode())));
        Map<String, List<String>> itemManufacturerMap = new HashMap<>();
        CriteriaQuery query = new CriteriaQuery(new Criteria("code.raw").in(codes), PageRequest.of(0, 9999));
        List<Medication> medications = queryForList("medication", query, elasticsearchTemplate, Medication.class);
        returnItems.forEach(returnItem -> {
            List<String> tempList = new ArrayList<>();
            Medication tempMedication = medications.stream().filter(medication -> medication.getCode().equals(returnItem.getCode())).findAny().orElse(null);
            if (tempMedication != null) {
                tempList.add(tempMedication.getManufacturer());
                tempList.add(tempMedication.getDrugSchedule() != null ? tempMedication.getDrugSchedule().name() : "-");
            }
            itemManufacturerMap.put(returnItem.getCode(), tempList);
        });
        return itemManufacturerMap;
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


