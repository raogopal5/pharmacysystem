package org.nh.pharmacy.service.impl;

import org.nh.billing.util.AdmissionDetailsUtil;
import org.nh.common.dto.InPatientDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.Organization;
import org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDocument;
import org.nh.pharmacy.repository.IPDispenseReturnRequestRepository;
import org.nh.pharmacy.repository.search.IPDispenseReturnRequestSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.FreemarkerService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.FreeMarkerUtil;
import org.nh.print.PdfGenerator;
import org.nh.print.barcode.BarCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nh.pharmacy.service.IPDispenseReturnRequestPdfService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.nh.pharmacy.util.ElasticSearchUtil.queryForObject;

/**
 * Service Implementation for  IpDispenseReturnRequestPrint.
 */
@Service("iPDispenseReturnRequestPdfService")
@Transactional
public class IPDispenseReturnRequestPdfServiceImpl implements IPDispenseReturnRequestPdfService {
    private final Logger log = LoggerFactory.getLogger(IPDispenseReturnRequestPdfServiceImpl.class);
    private final IPDispenseReturnRequestRepository iPDispenseReturnRequestRepository;
    private final IPDispenseReturnRequestSearchRepository iPDispenseReturnRequestSearchRepository;
    private final ApplicationProperties applicationProperties;
    private final ElasticsearchOperations elasticsearchTemplate;
    private final FreemarkerService freemarkerService;
    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    public IPDispenseReturnRequestPdfServiceImpl(IPDispenseReturnRequestRepository iPDispenseReturnRequestRepository, IPDispenseReturnRequestSearchRepository iPDispenseReturnRequestSearchRepository, ApplicationProperties applicationProperties, ElasticsearchOperations elasticsearchTemplate, FreemarkerService freemarkerService, PharmacyRedisCacheService pharmacyRedisCacheService)
    {
    this.applicationProperties = applicationProperties;
    this.iPDispenseReturnRequestRepository = iPDispenseReturnRequestRepository;
    this.iPDispenseReturnRequestSearchRepository = iPDispenseReturnRequestSearchRepository;
    this.elasticsearchTemplate = elasticsearchTemplate;
    this.freemarkerService = freemarkerService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }
    @Override
    public byte[] getIpDispenseReturnRequestPdf(Long dispenseReturnRequestId, String dispenseReturnRequestNumber) throws Exception {

        IPDispenseReturnRequest ipDispenseReturnRequest= getDispenseReturnRequest(dispenseReturnRequestId, dispenseReturnRequestNumber);
        String fileName = ipDispenseReturnRequest.getDocumentNumber();
        String fileAbsolutePath = applicationProperties.getAthmaBucket().getPrintSaveFile().concat(File.separator).concat(fileName + ".html");
        File file = new File(fileAbsolutePath);
        byte[] contentInBytes = null;
        if (file.exists()) {
            UserDTO user = loadLoggedInUser();
            String generatedBy = user.getDisplayName() + ", " + user.getEmployeeNo();
            String generateOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
            Map<String, Object> outData = this.getIpDispenseReturnPdfHTML(dispenseReturnRequestId, dispenseReturnRequestNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPdf(file, generatedBy, generateOn);
        } else {
            Map<String, Object> outData = this.getIpDispenseReturnPdfHTML(dispenseReturnRequestId, dispenseReturnRequestNumber);
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
        String templateFilePath = "ipDispenseReturnRequest.ftl"; // Fixed template
        IPDispenseReturnRequest dispenseReturnRequest = getDispenseReturnRequest(dispenseReturnId, dispenseReturnNumber);
        String fileName = dispenseReturnRequest.getDocumentNumber();
        printFile.put("fileName", fileName);

        Map<String, Object> returnData = populateIpDispenseReturnRequestData(dispenseReturnRequest);

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
    private Map<String, Object> populateIpDispenseReturnRequestData(IPDispenseReturnRequest dispenseReturnRequest)throws Exception {
        IPDispenseReturnRequestDocument ipDispenseReturnRequestDocument = dispenseReturnRequest.getDocument();
        UserDTO user = loadLoggedInUser();
        Organization org = getOrganizationData(ipDispenseReturnRequestDocument.getSourceHSC().getPartOf().getId());
        org.nh.pharmacy.domain.HealthcareServiceCenter hsc = queryForObject("healthcareservicecenter", new CriteriaQuery(new Criteria("id").is(ipDispenseReturnRequestDocument.getSourceHSC().getId())),elasticsearchTemplate,  org.nh.pharmacy.domain.HealthcareServiceCenter.class);
        Map<String, Object> ipDispenseReturnRequestData = new HashMap<>();
        if (null != ipDispenseReturnRequestDocument.getPatient().getMrn()) {
            ipDispenseReturnRequestData.put("barcodeImage", BarCodeGenerator.generateBarCodeAsBase64Image(ipDispenseReturnRequestDocument.getPatient().getMrn().trim()));
        } else {
            ipDispenseReturnRequestData.put("barcodeImage", null);
        }
        ipDispenseReturnRequestData.put("patientName", ipDispenseReturnRequestDocument.getPatient().getFullName());
        ipDispenseReturnRequestData.put("patientMrn", ipDispenseReturnRequestDocument.getPatient().getMrn() != null ? ipDispenseReturnRequestDocument.getPatient().getMrn() : ipDispenseReturnRequestDocument.getPatient().getTempNumber());
        ipDispenseReturnRequestData.put("patientPhoneNo", ipDispenseReturnRequestDocument.getPatient().getMobileNumber());

        InPatientDTO inpatientData= AdmissionDetailsUtil.getInPatientByEncounterNumber(ipDispenseReturnRequestDocument.getEncounter().getDocumentNumber(),elasticsearchTemplate);
        ipDispenseReturnRequestData.put("ward", null!=inpatientData.getWard()?inpatientData.getWard().getName():"");
        ipDispenseReturnRequestData.put("bed", null!=inpatientData.getBed()?inpatientData.getBed().getCode():"");
        ipDispenseReturnRequestData.put("consultantName", null!=ipDispenseReturnRequestDocument.getEncounter()?null!=inpatientData.getAdmissionDetails()?null!=inpatientData.getAdmissionDetails().getPrimaryConsultant()?inpatientData.getAdmissionDetails().getPrimaryConsultant().getDisplayName():"":"":"");
        ipDispenseReturnRequestData.put("department", null!=ipDispenseReturnRequestDocument.getEncounter()?null!=ipDispenseReturnRequestDocument.getEncounter().getDepartment()?ipDispenseReturnRequestDocument.getEncounter().getDepartment().getName():"":"");
        ipDispenseReturnRequestData.put("returnStore",null!=ipDispenseReturnRequestDocument.getReturnTOHSC()?ipDispenseReturnRequestDocument.getReturnTOHSC().getName():"");
        ipDispenseReturnRequestData.put("returnRequestNumber",dispenseReturnRequest.getDocumentNumber());
        ipDispenseReturnRequestData.put("requestedDate",null!=dispenseReturnRequest.getDocument().getRequestedDate()?dispenseReturnRequest.getDocument().getRequestedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")):"");
        ipDispenseReturnRequestData.put("requestedBy",null!=dispenseReturnRequest.getDocument().getCreatedBy()?dispenseReturnRequest.getDocument().getCreatedBy().getDisplayName():"");
        ipDispenseReturnRequestData.put("dispenseItems", ipDispenseReturnRequestDocument.getDispenseReturnDocumentLines());
        String preparedBy = ipDispenseReturnRequestDocument.getCreatedBy() != null ? ipDispenseReturnRequestDocument.getCreatedBy().getDisplayName() + ", " + ipDispenseReturnRequestDocument.getCreatedBy().getEmployeeNo() : "-";
        ipDispenseReturnRequestData.put("preparedBy", preparedBy);
        ipDispenseReturnRequestData.put("preparedOn", ipDispenseReturnRequestDocument.getCreatedDate() != null ? ipDispenseReturnRequestDocument.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "-");
        ipDispenseReturnRequestData.put("generatedBy", user.getDisplayName());
        ipDispenseReturnRequestData.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        ipDispenseReturnRequestData.put("unitDisplayName",null!=ipDispenseReturnRequestDocument.getUnit()?ipDispenseReturnRequestDocument.getUnit().getName():"");
        ipDispenseReturnRequestData.put("hscDisplayName", null!=ipDispenseReturnRequestDocument.getReturnTOHSC()?ipDispenseReturnRequestDocument.getReturnTOHSC().getName():"");
        ipDispenseReturnRequestData.put("remarks",null!=ipDispenseReturnRequestDocument.getRemarks()?ipDispenseReturnRequestDocument.getRemarks():"");
        ipDispenseReturnRequestData.put("reasonForReturn",null!=ipDispenseReturnRequestDocument.getReturnReason()?ipDispenseReturnRequestDocument.getReturnReason().getDisplay():"");
        ipDispenseReturnRequestData.put("unitAddress", org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
        .equalsIgnoreCase("work")).findAny().orElse(null));
           ipDispenseReturnRequestData.put("dlNo", hsc.getLicenseNumber() != null ? hsc.getLicenseNumber() : "-");
        ipDispenseReturnRequestData.put("convertDecimal", new FreeMarkerUtil());
        return ipDispenseReturnRequestData;
    }
    private IPDispenseReturnRequest getDispenseReturnRequest(Long dispenseReturnId, String dispenseReturnNumber) {
        IPDispenseReturnRequest dispenseReturnRequest = null;
        if (dispenseReturnId != null) {
            dispenseReturnRequest = iPDispenseReturnRequestRepository.findOne(dispenseReturnId);
        } else if (dispenseReturnNumber != null) {
            dispenseReturnRequest = iPDispenseReturnRequestSearchRepository.findByDocumentNumber(dispenseReturnNumber);
        }

        return dispenseReturnRequest;
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
