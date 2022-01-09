package org.nh.pharmacy.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.drools.core.base.RuleNameStartsWithAgendaFilter;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.nh.billing.domain.*;
import org.nh.billing.domain.dto.Medication;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.PaymentMode;
import org.nh.billing.domain.enumeration.Type;
import org.nh.billing.repository.search.PlanRuleDocumentSearchRepository;
import org.nh.billing.service.InvoiceService;
import org.nh.billing.service.PatientPlanService;
import org.nh.billing.service.PlanRuleService;
import org.nh.billing.service.PlanService;
import org.nh.billing.util.BillingApiConstants;
import org.nh.billing.util.DocumentUtil;
import org.nh.common.dto.*;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.enumeration.PricingMethod;
import org.nh.common.util.BigDecimalUtil;
import org.nh.common.util.ExportUtilConstant;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PamIntegration;
import org.nh.pharmacy.annotation.PublishChargeRecord;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.aop.producer.StockServiceAspect;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.UnitDiscountException;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.DispenseType;
import org.nh.pharmacy.domain.enumeration.MedicationRequestStatus;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.repository.ReserveStockRepository;
import org.nh.pharmacy.repository.search.DispenseSearchRepository;
import org.nh.pharmacy.repository.search.ItemSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.FreeMarkerUtil;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.errors.ErrorMessage;
import org.nh.pharmacy.web.rest.util.DateUtil;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.nh.print.PdfGenerator;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.common.util.BigDecimalUtil.*;
import static org.nh.pharmacy.config.Channels.DISPENSE_CHARGE_RECORD_ENCOUNTER_UPDATE_OUTPUT;
import static org.nh.pharmacy.util.ElasticSearchUtil.*;


/**
 * Service Implementation for managing Dispense.
 */
@Service
@Transactional
public class DispenseServiceImpl implements DispenseService {

    private final Logger log = LoggerFactory.getLogger(DispenseServiceImpl.class);

    private final DispenseRepository dispenseRepository;

    private final DispenseSearchRepository dispenseSearchRepository;

    private final MessageChannel pamIntegrationChannel;

    private final StockService stockService;

    private final StockSourceService stockSourceService;

    private final InvoiceService invoiceService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final String moduleName;

    private final ApplicationProperties applicationProperties;

    private final KieBase kieBase;

    private final FreemarkerService freemarkerService;

    private final MessageChannel externalPatientChannel;

    private final WorkflowService workflowService;

    private final JavaMailSender javaMailSender;

    private final PlanRuleDocumentSearchRepository planRuleDocumentSearchRepository;

    private final PlanService planService;

    private final PatientPlanService patientPlanService;

    private final PlanRuleService planRuleService;

    private final ReserveStockRepository reserveStockRepository;

    private final ItemSearchRepository itemSearchRepository;

    private final PlanExecutionService planExecutionService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final MessageChannel dispenseChargeRecordEncounterUpdateChannel;

    private final MessageChannel documentAuditChannel;

    private final MedicationRequestService medicationRequestService;

    private final RestHighLevelClient restHighLevelClient;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Value("${server.port}")
    private String portNo;

    @Value("${spring.mail.fromMail}")
    private String fromMail;

    @Value("${spring.mail.fromText}")
    private String fromText;

    public DispenseServiceImpl(DispenseRepository dispenseRepository, DispenseSearchRepository dispenseSearchRepository, StockSourceService stockSourceService, ApplicationProperties applicationProperties,
                               @Qualifier(org.nh.billing.config.Channels.PAM_PH_OUTPUT) MessageChannel pamIntegrationChannel, StockService stockService,
                               ElasticsearchOperations elasticsearchTemplate, JavaMailSender javaMailSender,
                               InvoiceService invoiceService, @Qualifier("moduleName") String moduleName, KieBase kieBase, FreemarkerService freemarkerService,
                               @Qualifier(org.nh.billing.config.Channels.EXTERNAL_PATIENT_OUTPUT) MessageChannel externalPatientChannel, WorkflowService workflowService,
                               PlanRuleDocumentSearchRepository planRuleDocumentSearchRepository, PlanService planService, PatientPlanService patientPlanService,
                               PlanRuleService planRuleService, ReserveStockRepository reserveStockRepository, ItemSearchRepository itemSearchRepository,
                               PlanExecutionService planExecutionService, SequenceGeneratorService sequenceGeneratorService,
                               @Qualifier(DISPENSE_CHARGE_RECORD_ENCOUNTER_UPDATE_OUTPUT) MessageChannel dispenseChargeRecordEncounterUpdateChannel, @Qualifier(Channels.DMS_DOCUMENT_AUDIT) MessageChannel documentAuditChannel, MedicationRequestService medicationRequestService, RestHighLevelClient restHighLevelClient, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.dispenseRepository = dispenseRepository;
        this.dispenseSearchRepository = dispenseSearchRepository;
        this.stockSourceService = stockSourceService;
        this.applicationProperties = applicationProperties;
        this.pamIntegrationChannel = pamIntegrationChannel;
        this.stockService = stockService;
        this.invoiceService = invoiceService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.moduleName = moduleName;
        this.kieBase = kieBase;
        this.freemarkerService = freemarkerService;
        this.externalPatientChannel = externalPatientChannel;
        this.workflowService = workflowService;
        this.javaMailSender = javaMailSender;
        this.planRuleDocumentSearchRepository = planRuleDocumentSearchRepository;
        this.planService = planService;
        this.patientPlanService = patientPlanService;
        this.planRuleService = planRuleService;
        this.reserveStockRepository = reserveStockRepository;
        this.itemSearchRepository = itemSearchRepository;
        this.planExecutionService = planExecutionService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.dispenseChargeRecordEncounterUpdateChannel = dispenseChargeRecordEncounterUpdateChannel;
        this.documentAuditChannel = documentAuditChannel;
        this.medicationRequestService = medicationRequestService;
        this.restHighLevelClient = restHighLevelClient;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    /**
     * Save a dispense.
     *
     * @param dispense the entity to save
     * @return the persisted entity
     */
    @Override
    public Dispense save(Dispense dispense) {
        log.debug("Request to save Dispense : {}", dispense);
        //Generate Sequence Number for Dispense
        if (dispense.getId() == null) {
            dispense.getDocument().setCreatedDate(LocalDateTime.now());
            dispense.setId(dispenseRepository.getId());
            dispense.setVersion(0);
            dispense.getDocument().setDispenseNumber(dispense.getId().toString());
            if (DispenseStatus.DRAFT.equals(dispense.getDocument().getDispenseStatus())) {
                dispense.setDocumentNumber("DRAFT-" + dispense.getId());
            }
        } else {
            dispenseRepository.updateLatest(dispense.getId());
            dispense.setVersion(dispense.getVersion() + 1);
        }

        dispense.setLatest(true);
        if (DispenseStatus.DRAFT.equals(dispense.getDocument().getDispenseStatus()) && null!= dispense.getDocument().getEncounter() && isIpPatient(dispense.getDocument().getEncounter())){
            dispense.getDocument().setDispenseDate(null);
        }else{
            dispense.getDocument().setDispenseDate(LocalDateTime.now());
        }
        dispense.getDocument().setModifiedDate(LocalDateTime.now());
        dispense.getDocument().setDispenseNumber(dispense.getDocumentNumber());
        dispense.getDocument().getDispenseDocumentLines().forEach(dispenseDocumentLine -> {
            if (dispenseDocumentLine.getLineNumber() == null)
                dispenseDocumentLine.setLineNumber(dispenseRepository.getId());
        });
        saveValidation(dispense);
        Dispense result = dispenseRepository.save(dispense);
        dispenseSearchRepository.save(result);

        return result;
    }
    public static Boolean isIpPatient(EncounterDTO encounterDTO) {
        if (nonNull(encounterDTO) && nonNull(encounterDTO.getEncounterClass()) &&
            (BillingApiConstants.INPATIENT.equalsIgnoreCase(encounterDTO.getEncounterClass().getCode())
                || BillingApiConstants.EMERGENCY.equalsIgnoreCase(encounterDTO.getEncounterClass().getCode()) ||
                BillingApiConstants.DAY_CARE.equalsIgnoreCase(encounterDTO.getEncounterClass().getCode()))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public void validateItemStock(Dispense dispense,Map.Entry<Long,Float> entry) {
        Map<Long, Float> keyMap = new HashedMap();
        List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();
        for (DispenseDocumentLine dispenseDocumentLine : lines) {
            float quantity = dispenseDocumentLine.getQuantity();
            if(keyMap.containsKey(dispenseDocumentLine.getMedicationRequestDocLineId())) {
                if (dispenseDocumentLine.getMedicationRequestDocLineId() == entry.getKey())
                    keyMap.put(dispenseDocumentLine.getMedicationRequestDocLineId(), keyMap.get(dispenseDocumentLine.getMedicationRequestDocLineId()) + dispenseDocumentLine.getQuantity() + entry.getValue());
            } else {
                if(dispenseDocumentLine.getMedicationRequestDocLineId() == entry.getKey())
                    keyMap.put(dispenseDocumentLine.getMedicationRequestDocLineId(), quantity + entry.getValue());
            }
            if(keyMap.containsKey(dispenseDocumentLine.getMedicationRequestDocLineId())) {
                if (keyMap.get(dispenseDocumentLine.getMedicationRequestDocLineId()) > dispenseDocumentLine.getOrderItem().getQuantity()) {
                    String message = "Dispense Quantity can't be greater then order Quantity for Item: "+dispenseDocumentLine.getName();
                    throw new CustomParameterizedException(message);

                }
            }
        }

    }

    /**
     * Save or update a dispense.
     *
     * @param dispense the entity to save
     * @return the persisted entity
     */
    @Override
    public Dispense saveOrUpdate(Dispense dispense) {
        log.debug("Request to saveOrUpdate Dispense : {}", dispense);
        dispense.getDocument().setModifiedDate(LocalDateTime.now());
        Dispense result = dispenseRepository.save(dispense);
        dispenseSearchRepository.save(result);
        return result;
    }

    /**
     * Get invoice by dispenseId or dispenseNumber
     *
     * @param dispenseId
     * @param dispenseNumber
     * @return
     * @throws ResourceNotFoundException
     */
    @Override
    public Invoice getInvoiceByDispenseId(Long dispenseId, String dispenseNumber) throws ResourceNotFoundException {
        log.debug("dispenseId: {}, dispenseNumber: {}", dispenseId, dispenseNumber);
        if (dispenseId == null && dispenseNumber == null) {
            log.error("dispenseId or dispenseNumber not found.");
            throw new ResourceNotFoundException("dispenseId or dispenseNumber not found.");
        }

        Dispense dispense = null;
        if (dispenseId != null) {
            dispense = dispenseSearchRepository.findById(dispenseId).get();
        } else if (dispenseNumber != null) {
            dispense = dispenseSearchRepository.findByDocumentNumber(dispenseNumber);
        }
        log.debug("dispense: {}", dispense);
        if (dispense.getDocument().getSource() == null) {
            log.error("Source document not found.");
        }
        Long invoiceId = dispense.getDocument().getSource().getId();
        log.debug("invoiceId: {}", invoiceId);
        List<Invoice> invoices = invoiceService.search("id:" + invoiceId, PageRequest.of(0, 10)).getContent();
        Invoice invoice = null;
        if (!invoices.isEmpty()) {
            invoice = invoices.get(0);
        } else {
            log.error("Invoice not found.");
            throw new ResourceNotFoundException("Invoice not found.");
        }
        log.debug("invoice: {}", invoice);
        return invoice;
    }

    private Dispense getDispense(Long dispenseId, String dispenseNumber) {
        Dispense dispense = null;
        if (dispenseId != null) {
            dispense = dispenseSearchRepository.findById(dispenseId).get();
        } else if (dispenseNumber != null) {
            dispense = dispenseSearchRepository.findByDocumentNumber(dispenseNumber);
        }
        return dispense;
    }

    @Override
    public byte[] getInvoicePdfByDispense(Long dispenseId, String dispenseNumber, String original) throws Exception {
        Dispense dispense = getDispense(dispenseId, dispenseNumber);
        String fileName = dispense.getDocument().getSource().getReferenceNumber();
        String path = DocumentUtil.getPath(applicationProperties.getAthmaBucket().getDocBasePath(), dispense.getDocument().getPatient().getMrn(), "ADMINISTRATIVE", "INVOICE");
        String fileAbsolutePath = path.concat(fileName + ".html");
        File file = new File(fileAbsolutePath);
        byte[] contentInBytes = null;
        PdfGenerator.port = portNo;
        long count = elasticsearchTemplate.count(new NativeSearchQueryBuilder()
            .withQuery(new QueryStringQueryBuilder(
                new StringBuilder("documentNumber:\"")
                    .append(dispense.getDocument().getSource().getReferenceNumber())
                    .append("\" ")
                    .append("documentType:\"").append("DISPENSE_INVOICE")
                    .append("\" ")
                    .append("auditType:").append("PRINT").toString()).defaultOperator(Operator.AND))
            .build(), IndexCoordinates.of(DocumentAuditDTO.DOCUMENT_NAME));
        if (file.exists()) {
            //Update generatedBy and generatedOn in file, If file exist than create pdf
            UserDTO user =  loadLoggedInUser();
            String generatedBy = user.getDisplayName() + ", " + user.getEmployeeNo();
            String generateOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
            contentInBytes = PdfGenerator.createPdf(file, generatedBy, generateOn);
            if (original == null && count > 0 ) {
                contentInBytes = PdfGenerator.addWaterMarkToPdf(contentInBytes, "DUPLICATE");
            }
        } else {
            Map<String, Object> outData = this.getInvoiceHTMLByDispenseId(dispenseId, dispenseNumber);
            String htmlData = outData.get("html").toString();
            contentInBytes = PdfGenerator.createPDF(htmlData);
        }

        if (!DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus())) {
            contentInBytes = PdfGenerator.addWaterMarkToPdf(contentInBytes, "DRAFT");
        }

        UserDTO userDTO = new UserDTO();
        org.nh.security.dto.User currentUser = UserPreferencesUtils.getCurrentUserPreferences().getUser();
        userDTO.setId(currentUser.getId());
        userDTO.setLogin(currentUser.getLogin());
        userDTO.setEmployeeNo(currentUser.getEmployeeNo());
        userDTO.setDisplayName(currentUser.getDisplayName());
        documentAuditChannel.send(MessageBuilder.withPayload(new DocumentAuditDTO("PRINT",
            dispense.getDocument().getSource().getReferenceNumber(), "DISPENSE_INVOICE",
            Instant.now(), userDTO)).build());

        return contentInBytes;
    }

    public void sendMail(String invoiceNo, String unitName, String userName, BigDecimal receiptAmount, String toMail, byte[] pdf) throws MessagingException {
        log.debug("sending invoice: {}, to mail: {}", invoiceNo, toMail);

        String subject = "Pharmacy Bill - " + unitName;
        String bodyHtml = "<table>\n" +
            "<tr><td>Dear " + userName + ",</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr><td>Thank you for visiting " + unitName + "</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr><td>We wish to confirm the receipt of Rs. " + receiptAmount.floatValue() + " towards the Invoice No - " + invoiceNo + ". A copy of the Bill cum Receipt is attached for your reference.</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr><td> Wishing you a speedy recovery!</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr style='font-weight:bold;'><td>Best Regards!</td></tr>\n" +
            "<tr style='font-weight:bold;'><td>Narayana Health</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr><td>&nbsp;</td></tr>\n" +
            "<tr><td style='font-size:13px;color:  gray;'>This email was sent from a notification-only address that cannot accept incoming email. Please do not reply to this message.</td></tr>\n" +
            "</table>";

        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromText + "<" + fromMail + ">"));
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        //helper.setFrom(fromMail);
        helper.setSubject(subject);
        helper.setTo(toMail);
        helper.setText(bodyHtml, true); // true indicates html
        helper.addAttachment(invoiceNo + ".pdf", new ByteArrayResource(pdf), "application/pdf");
        javaMailSender.send(message);
    }

    @Override
    public void sendDocument(Long dispenseId, String dispenseNumber) throws Exception {
        Dispense dispense = getDispense(dispenseId, dispenseNumber);
        String invoiceNo = dispense.getDocument().getSource().getReferenceNumber();
        log.debug("Request to send invoice details for invoice Number: {}", invoiceNo);
        String toEmail = dispense.getDocument().getPatient().getEmail();
        if (toEmail == null || toEmail.isEmpty()) {
            throw new Exception("Patient Email not found.");
        }
        String userName = dispense.getDocument().getPatient().getFullName();
        String path = DocumentUtil.getPath(applicationProperties.getAthmaBucket().getDocBasePath(), dispense.getDocument().getPatient().getMrn(), "ADMINISTRATIVE", "INVOICE");
        File file = new File(path.concat(invoiceNo + ".html"));
        if (file.exists()) {
            BigDecimal receiptAmount = dispense.getDocument().getPatientNetAmount();
            String unitName = dispense.getDocument().getHsc().getPartOf().getName();
            byte[] pdfData = PdfGenerator.createPdf(file);
            sendMail(invoiceNo, unitName, userName, receiptAmount, toEmail, pdfData);
        }
    }

    @Override
    public Map<String, Object> getInvoiceHTMLByDispenseId(Long dispenseId, String dispenseNumber) throws Exception {
        log.debug("dispenseId: {}, dispenseNumber: {}", dispenseId, dispenseNumber);

        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "invoice.ftl"; // Fixed template

        Dispense dispense = getDispense(dispenseId, dispenseNumber);
        String fileName = dispense.getDocument().getSource().getReferenceNumber();
        printFile.put("fileName", fileName);
        Map<String, Object> invoiceData = populateInvoiceData(dispense);
        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, invoiceData);
        printFile.put("html", html);
        byte[] contentInBytes = html.getBytes();
        printFile.put("content", contentInBytes);
        if (DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus())) {
            createHTMLFile(html, fileName, dispense.getDocument().getPatient().getMrn());
        }
        return printFile;
    }

    /**
     * Create html file
     *
     * @param html
     * @param fileName
     */
    private void createHTMLFile(String html, String fileName, String mrn) {
        FileOutputStream fop = null;
        File file = null;
        try {
            String path = DocumentUtil.getPath(applicationProperties.getAthmaBucket().getDocBasePath(), mrn, "ADMINISTRATIVE", "INVOICE");
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(path.concat(fileName + ".html"));
            if (!file.exists()) {
                file.createNewFile();
            }
            fop = new FileOutputStream(file);
            byte[] contentInBytes = html.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            log.error("Error while creating html file", e);
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                log.error("Error while closing created html file", e);
            }
        }
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

    private Map<String, Object> populateInvoiceData(Dispense dispense) throws Exception{
        log.debug("in populateInvoiceData:-" + dispense.getDocumentNumber());
        Invoice invoice = getInvoiceByDispenseId(dispense.getId(), null);
        Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("invoiceId:" + invoice.getId())
            .defaultOperator(Operator.AND)).build();

        List<InvoiceReceipt> invoiceReceipts = queryForList(moduleName+"_invoicereceipt", query, elasticsearchTemplate, InvoiceReceipt.class);
        List<String> receipts = new ArrayList<>();
        Map<String, Object> payments = new HashMap<>();
        for (InvoiceReceipt invoiceReceipt : invoiceReceipts) {
            CriteriaQuery receiptQuery = new CriteriaQuery(new Criteria("id").is(invoiceReceipt.getReceiptId()));
            Receipt receipt = queryForObject(moduleName+"_receipt", receiptQuery, elasticsearchTemplate, Receipt.class);
            receipts.add(receipt.getReceiptNumber());
            if (payments.get(receipt.getPaymentMode().toString()) == null) {
                payments.put(receipt.getPaymentMode().toString(), receipt.getReceiptAmount());
            } else {
                List temp = new ArrayList();
                temp.add(payments.get(receipt.getPaymentMode().toString()));
                temp.add(receipt.getReceiptAmount());
                payments.put(receipt.getPaymentMode().toString(), temp);

            }
        }

        Map<String, List<String>> manufacturerAndSchedule = getManufacturerAndSchedule(invoice.getInvoiceDocument().getInvoiceItems());

        UserDTO user = loadLoggedInUser();
        HealthcareServiceCenter hsc =  getRecord("healthcareservicecenter", "id:"+dispense.getDocument().getHsc().getId(), elasticsearchTemplate, HealthcareServiceCenter.class);
        Organization org = getOrganizationData(dispense.getDocument().getHsc().getPartOf().getId());
        //String dateFormat = ConfigurationUtil.getConfigurationData("athma_date_format", dispense.getDocument().getHsc().getId(), ((Number) dispense.getDocument().getDispenseUnit().getId()).longValue(), null, elasticsearchTemplate);

        List<SponsorInvoice> sponsorInvoices =  getRecords(moduleName+"_sponsorinvoice", "sponsorDocument.source.referenceNumber.raw:"+invoice.getInvoiceDocument().getInvoiceNumber(),
            PageRequest.of(0, 100) ,elasticsearchTemplate, SponsorInvoice.class);
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("patientName", invoice.getInvoiceDocument().getPatient().getFullName());
        invoiceData.put("patientMrn", invoice.getInvoiceDocument().getPatient().getMrn() != null ? invoice.getInvoiceDocument().getPatient().getMrn() : invoice.getInvoiceDocument().getPatient().getTempNumber());
        invoiceData.put("patientPhoneNo", invoice.getInvoiceDocument().getPatient().getMobileNumber());
        invoiceData.put("consultantName", dispense.getDocument().getConsultant().getDisplayName());
        invoiceData.put("billNo", invoice.getInvoiceNumber());
        invoiceData.put("receipts", receipts);
        invoiceData.put("date", invoice.getInvoiceDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        //invoiceData.put("visitDate",);
        invoiceData.put("invoicePlans", invoice.getInvoiceDocument().getInvoicePlans());


        invoice.getInvoiceDocument().getInvoiceItems().forEach(invoiceItem -> {

            if (invoiceItem.getInvoiceItemPlans() != null && !invoiceItem.getInvoiceItemPlans().isEmpty()) {

                if (invoiceItem.getInvoiceItemPlans().get(0).getPlanRuleDetail().getAliasName() != null &&

                    !invoiceItem.getInvoiceItemPlans().get(0).getPlanRuleDetail().getAliasName().isEmpty()) {

                    invoiceItem.getItem().setName(invoiceItem.getInvoiceItemPlans().get(0).getPlanRuleDetail().getAliasName());

                }

            }

        });


        invoiceData.put("invoiceItems", invoice.getInvoiceDocument().getInvoiceItems());

        invoiceData.put("preparedBy", invoice.getInvoiceDocument().getCreatedBy() != null ? invoice.getInvoiceDocument().getCreatedBy().getDisplayName() + ", " + invoice.getInvoiceDocument().getCreatedBy().getEmployeeNo() : "-");
        invoiceData.put("qualifiedPharmacist", hsc.getContacts());
        invoiceData.put("generatedBy", user.getDisplayName());
        invoiceData.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        invoiceData.put("total", invoice.getInvoiceDocument().getSaleAmount());
        invoiceData.put("invoiceTaxes", invoice.getInvoiceDocument().getInvoiceTaxes());
        invoiceData.put("patientDiscount", invoice.getInvoiceDocument().getPatientDiscount());
        invoiceData.put("sponsorDiscount", invoice.getInvoiceDocument().getSponsorDiscount());
        invoiceData.put("taxDiscount", invoice.getInvoiceDocument().getTaxDiscount());
        invoiceData.put("totalSponsorAmount", invoice.getInvoiceDocument().getSponsorNetAmount());
        invoiceData.put("patientPayable", invoice.getInvoiceDocument().getPatientAmount());
        invoiceData.put("netAmount", invoice.getInvoiceDocument().getNetAmount());
        invoiceData.put("totalDiscount", invoice.getInvoiceDocument().getDiscountAmount());
        invoiceData.put("paymentDetails", generatePaymentMode(payments));
        invoiceData.put("unitDisplayName", dispense.getDocument().getHsc().getPartOf().getName());
        invoiceData.put("hscDisplayName", dispense.getDocument().getHsc().getName());
        invoiceData.put("unitAddress", org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
            .equalsIgnoreCase("work")).findAny().orElse(null));
        invoiceData.put("manufacutererAndSchedule", manufacturerAndSchedule);
        //invoiceData.put("signature",);
        invoiceData.put("dlNo", hsc.getLicenseNumber() != null ? hsc.getLicenseNumber() : "-");
        invoiceData.put("roundOff", invoice.getInvoiceDocument().getRoundOff());
        invoiceData.put("convertDecimal", new FreeMarkerUtil());
        invoiceData.put("sponsorInvoices", sponsorInvoices);
        invoiceData.put("visitNumber", invoice.getInvoiceDocument().getEncounter().getVisitNumber());
        Boolean gstinFound = false;
        if (org.getIdentifier() != null && !org.getIdentifier().isEmpty()) {
            Iterator<OrganizationIdentifier> organizationIdentifierIterator = org.getIdentifier().listIterator();
            while (organizationIdentifierIterator.hasNext()) {
                OrganizationIdentifier organizationIdentifier = organizationIdentifierIterator.next();
                if (organizationIdentifier.getType().equals("GSTIN")) {
                    invoiceData.put("gstin", organizationIdentifier.getValue());
                    gstinFound = true;
                }
            }
        }
        if (!gstinFound) {
            invoiceData.put("gstin", "-");
        }
        String qrcode="";
        if (null != invoice.getInvoiceDocument().getUnit() && null!=invoice.getInvoiceDocument().getUnit().getName()) {
            qrcode=qrcode+"Unit Name: " + invoice.getInvoiceDocument().getUnit().getName().trim();
        }
        if (null!=invoice.getInvoiceDocument().getHsc().getId()) {
            String value="";
            if (null!=dispense.getDocument().getPaymentDetails() && !dispense.getDocument().getPaymentDetails().isEmpty() && null!=dispense.getDocument().getPaymentDetails().get(0).getPaymentMode() && PaymentMode.CASH.equals(dispense.getDocument().getPaymentDetails().get(0).getPaymentMode())) {
                value = ConfigurationUtil.getConfigurationData("athma_pharmacy_cash_payment_bank_account",
                    invoice.getInvoiceDocument().getHsc().getId(), invoice.getInvoiceDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            }
            if (null!=dispense.getDocument().getPaymentDetails() && !dispense.getDocument().getPaymentDetails().isEmpty() && null!=dispense.getDocument().getPaymentDetails().get(0).getPaymentMode() && PaymentMode.CARD.equals(dispense.getDocument().getPaymentDetails().get(0).getPaymentMode())) {
                value = ConfigurationUtil.getConfigurationData("athma_pharmacy_credit_card_payment_bank_account",
                    invoice.getInvoiceDocument().getHsc().getId(), invoice.getInvoiceDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            }
            if (null!=dispense.getDocument().getPaymentDetails() && !dispense.getDocument().getPaymentDetails().isEmpty() && null!=dispense.getDocument().getPaymentDetails().get(0).getPaymentMode() && PaymentMode.E_WALLET.equals(dispense.getDocument().getPaymentDetails().get(0).getPaymentMode())){
                value = ConfigurationUtil.getConfigurationData("athma_pharmacy_wallet_payment_bank_account",
                    invoice.getInvoiceDocument().getHsc().getId(), invoice.getInvoiceDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            }

            if (null!=value && !value.equals("")){
                CriteriaQuery queryToFetchBankDetails = new CriteriaQuery(new Criteria("bankCode.raw").is(value));
                BankDTO bankDTO = elasticsearchTemplate.queryForObject(queryToFetchBankDetails, BankDTO.class,IndexCoordinates.of("bank"));
                if (null != bankDTO && null!=bankDTO.getAccountNumber()) {
                    qrcode=qrcode+", Acc. No: " + bankDTO.getAccountNumber();
                }
                if (null != bankDTO && null!=bankDTO.getIfscCode()) {
                    qrcode=qrcode+", IFSC: " + bankDTO.getIfscCode();
                }
            }
        }
        if (null!=invoice.getInvoiceDocument().getPatientAmount()) {
            qrcode=qrcode+", Invoice Amt: " + invoice.getInvoiceDocument().getPatientAmount().toString();
        }
        if (null != invoice.getInvoiceNumber()) {
            qrcode=qrcode+", Invoice No: " + invoice.getInvoiceNumber();
        }
        if (null!=invoice.getInvoiceDocument().getPatient() && null!=invoice.getInvoiceDocument().getPatient().getDisplayName()) {
            qrcode=qrcode+", Patient Name: " + invoice.getInvoiceDocument().getPatient().getDisplayName();
        }
        if (null != qrcode) {
            invoiceData.put("qrcodeImage", generateQRCodeImage(qrcode));
        } else {
            invoiceData.put("qrcodeImage", null);
        }

        return invoiceData;
    }
    private   String generateQRCodeImage(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
            barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 120, 120);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        baos.flush();
        baos.close();
        String base64Image =  "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        return "<img src='" + base64Image + "' alt=\"barcodeText\" >";
    }

    private String generatePaymentMode(Map<String, Object> payments) {
        log.debug("in generatePaymentMode");
        if (payments.size() == 0 || payments.size() == 1 && payments.get("CASH") != null
            && payments.get("CASH") instanceof Float && (Float) payments.get("CASH") == 0f) {
            return "Not Applicable";
        }
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) payments).entrySet()) {
            if (entry.getValue() instanceof ArrayList) {
                ((ArrayList) entry.getValue()).forEach(value -> {
                    str.append(" Paid via ");
                    str.append(entry.getKey());
                    str.append(" Rs ");
                    str.append(FreeMarkerUtil.decimalFormater(value));
                    str.append(",");
                });
            } else {
                str.append(" Paid via ");
                str.append(entry.getKey());
                str.append(" Rs ");
                str.append(FreeMarkerUtil.decimalFormater(entry.getValue()));
                str.append(",");
            }
        }
        return str.deleteCharAt(str.lastIndexOf(",")).toString();
    }

    private Map<String, List<String>> getManufacturerAndSchedule(List<InvoiceItem> invoiceItems) {
        log.debug("In getManufacturerAndSchedule....");
        List<String> codes = new ArrayList<>();
        invoiceItems.stream().forEach(invoiceItem -> codes.add(QueryParser.escape(invoiceItem.getItem().getCode())));
        Map<String, List<String>> itemManufacturerMap = new HashMap<>();
        Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("code.raw:("+ StringUtils.join(codes, " OR ")+")")).withPageable(PageRequest.of(0, 10000)).build();
        List<Medication> invoiceMedications = queryForList("medication", query, elasticsearchTemplate, Medication.class);
        log.debug("medication request result size:{}", invoiceMedications.size());
        List<ItemDTO> itemDTOs = queryForList("item", query, elasticsearchTemplate, ItemDTO.class);

        invoiceItems.forEach(invoiceItem -> {
            List<String> tempList = new ArrayList<>();
            ItemDTO tempItemDTO = itemDTOs.stream().filter(medication -> medication.getCode().equals(invoiceItem.getItem().getCode())).findAny().orElse(null);
            Medication tempMedication = invoiceMedications.stream().filter(medication -> medication.getCode().equals(invoiceItem.getItem().getCode())).findAny().orElse(null);

            if (tempItemDTO != null) {
                tempList.add(tempItemDTO.getManufacturer());
            }
            if (tempMedication != null) {
                tempList.add(tempMedication.getDrugSchedule() != null ? tempMedication.getDrugSchedule().name() : "-");
            }
            itemManufacturerMap.put(invoiceItem.getItem().getCode(), tempList);
        });
        return itemManufacturerMap;
    }

    /**
     * Get all the dispenses.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Dispense> findAll(Pageable pageable) {
        log.debug("Request to get all Dispenses");
        Page<Dispense> result = dispenseRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one dispense by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Dispense findOne(Long id) {
        log.debug("Request to get Dispense : {}", id);
        Dispense dispense = dispenseRepository.findOne(id);
        return dispense;
    }

    /**
     * Get one dispense by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Dispense findDetachedOne(Long id) {
        log.debug("Request to get Dispense : {}", id);
        return dispenseRepository.findOne(id);
    }

    /**
     * Delete the  dispense by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Dispense : {}", id);
        Dispense dispense = dispenseSearchRepository.findById(id).get();
        deleteValidation(dispense);
        dispenseRepository.delete(id);
        dispenseSearchRepository.deleteById(id);
    }

    /**
     * Search for the dispense corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Dispense> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Dispenses for query {}", query);
        Page<Dispense> result = dispenseSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.dispenseDate").field("document.dispenseStatus")
            .field("document.patient.displayName").field("document.patient.mrn").field("document.patient.tempNumber")
            .field("document.hsc.name").field("document.dispenseUser.displayName")
            .field("document.consultant.displayName").field("document.source.referenceNumber")
            .field("document.patientPaidAmount").field("document.encounter.visitNumber").field("document.dispenseType")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Dispense> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of Dispenses for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND)
                .field("documentNumber").field("document.dispenseDate").field("document.dispenseStatus")
                .field("document.patient.displayName").field("document.patient.mrn").field("document.patient.tempNumber")
                .field("document.hsc.name").field("document.dispenseUser.displayName")
                .field("document.consultant.displayName").field("document.source.referenceNumber")
                .field("document.patientPaidAmount").field("document.encounter.visitNumber")
                .field("document.dispenseType").field("document.orderSource.documentNumber.keyword"))
            .withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return dispenseSearchRepository.search(searchQuery);
    }

    /**
     * Export dispenses corresponding to the query.
     *
     * @param query
     * @param pageable
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, String> exportDispenses(String query, Pageable pageable) throws IOException {
        log.debug("Request to export Dispenses in CSV File query {}", query);

        File file = ExportUtil.getCSVExportFile("dispense", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter dispenseFileWriter = new FileWriter(file);
        Map<String, String> dispenseFileDetails = new HashMap<>();
        dispenseFileDetails.put("fileName", file.getName());
        dispenseFileDetails.put("pathReference", "masterExport");
        //Header for dispense csv file
        final String[] dispenseFileHeader = {"Dispense No", "Date and Time", "Patient Name", "MRN", "Store", "Dispenser", "Consultant", "Order No",
            "Invoice No", "Total Payable", "Sponsor Payable", "Paid Amount", "Status"};
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        String dateFormat = null;
        try (CSVPrinter csvFilePrinter = new CSVPrinter(dispenseFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(dispenseFileHeader);
                Iterator<Dispense> dispenseIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();
                while (dispenseIterator.hasNext()) {
                    Dispense dispense = dispenseIterator.next();

                    if (dateFormat == null)
                        dateFormat = ConfigurationUtil.getConfigurationData("athma_date_format", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);

                    List dispenseRow = new ArrayList();
                    dispenseRow.add(dispense.getDocumentNumber());
                    dispenseRow.add(DateUtil.getFormattedDateAsFunctionForCSVExport(dispense.getDocument().getDispenseDate(), dateFormat));
                    dispenseRow.add(dispense.getDocument().getPatient().getFullName());

                    if (dispense.getDocument().getPatient().getMrn() == null) {
                        dispenseRow.add(dispense.getDocument().getPatient().getMrn());
                    } else {
                        dispenseRow.add(new StringBuilder("=\"").append(dispense.getDocument().getPatient().getMrn()).append("\"").toString());
                    }

                    dispenseRow.add(dispense.getDocument().getHsc().getName());
                    dispenseRow.add(dispense.getDocument().getDispenseUser() != null ? dispense.getDocument().getDispenseUser().getDisplayName() : "");
                    dispenseRow.add(dispense.getDocument().getConsultant() != null ? dispense.getDocument().getConsultant().getDisplayName() : "");

                    dispenseRow.add(dispense.getDocument().getOrderSource() != null ? dispense.getDocument().getOrderSource().get("documentNumber") : null);

                    dispenseRow.add(dispense.getDocument().getSource() != null ? dispense.getDocument().getSource().getReferenceNumber() : null);
                    dispenseRow.add(dispense.getDocument().getNetAmount() != null ? dispense.getDocument().getNetAmount() : 0f);
                    dispenseRow.add(dispense.getDocument().getSponsorNetAmount());
                    dispenseRow.add(dispense.getDocument().getPatientPaidAmount() != null ? dispense.getDocument().getPatientPaidAmount() : 0f);
                    dispenseRow.add(dispense.getDocument().getDispenseStatus());
                    csvFilePrinter.printRecord(dispenseRow);
                }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (dispenseFileWriter != null)
                dispenseFileWriter.close();
        }

        return dispenseFileDetails;
    }


    /**
     * Search for the dispense to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */
    @Override
    public Map<String, Long> getStatusCount(String query) {
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.dispenseDate").field("document.dispenseStatus")
                .field("document.patient.displayName").field("document.patient.mrn")
                .field("document.hsc.name").field("document.dispenseUser.displayName")
                .field("document.consultant.displayName").field("document.source.referenceNumber")
                .field("document.patientPaidAmount")
                .defaultOperator(Operator.AND))

            .addAggregation(AggregationBuilders.terms("status_count").field("document.dispenseStatus.raw"))
            .build();

        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "dispense");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    public List<PatientDTO> searchPatient(String query, Pageable pageable) {
        log.debug("searchPatient query: {}", query);
        List<PatientDTO> patients = new ArrayList<>();
        if (query.length() > 0) {
            String[] include = {"document.patient"};
            String[] exclude = {};
            Query searchQuery = new NativeSearchQueryBuilder().
                withQuery(queryStringQuery(query).field("document.patient.mrn").field("document.patient.displayName").field("document.patient.mobileNumber")
                    .defaultOperator(Operator.OR)).
                withSourceFilter(new FetchSourceFilter(include, exclude)).withPageable(pageable).build();
            List<Dispense> dispenses = dispenseSearchRepository.search(searchQuery).getContent();
            if (dispenses != null) {
                dispenses.forEach(dispense -> {
                    patients.add(dispense.getDocument().getPatient());
                });
            }
        }

        return patients;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Dispense");
        dispenseSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on Dispense latest=true");
        List<Dispense> data = dispenseRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            dispenseSearchRepository.saveAll(data);
        }
    }

    /**
     * Reindex dispense elasticsearch for given id
     *
     * @param id
     */
    @Override
    public void reIndex(Long id) {
        log.info("Request to do re-index for dispense : {}", id);
        if (id != null) {
            Dispense dispense = dispenseRepository.findOne(id);
            if (dispense == null) {
                if (dispenseSearchRepository.existsById(id)) {
                    dispenseSearchRepository.deleteById(id);
                }
                //reIndexChargeRecordDTOS(dispense,id);
            } else {
                dispenseSearchRepository.save(dispense);
            }
        }
        log.debug("Request to do reIndex dispense ends : {}", LocalTime.now());
    }

    /**
     * Get all Dispense and publish
     */
    @Override
    public void produce(Dispense dispense) {
        log.debug("Dispense Id {} has been published", dispense.getId());
        Map<String, Object> dispenseMap = new HashedMap();
        dispenseMap.put("Id", dispense.getId());
        dispenseMap.put("DocumentNo", dispense.getDocumentNumber());
        StockServiceAspect.threadLocal.get().put(Channels.STOCK_OUTPUT, dispenseMap);
    }

    /**
     * publish the PamIntegration entity
     *
     * @param pamDocument
     */
    @Override
    public void produce(PamDocument pamDocument) {
        log.debug("Request to publish PamIntegration : {} ", pamDocument);
        pamIntegrationChannel.send(MessageBuilder.withPayload(pamDocument).build());
    }

    /**
     * publish external patient details
     *
     * @param patient
     */
    @Override
    public void publishExternalPatient(PatientDTO patient) {
        log.debug("Request to publish external patient : {} ", patient);
        externalPatientChannel.send(MessageBuilder.withPayload(patient).build());
    }

    /**
     * Dispense Auto close
     */
    @Override
    @Deprecated
    public void autoClose() {
        log.debug("Dispense Auto close");
        LocalDateTime currentDateTime = LocalDateTime.now();
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("document.dispenseStatus.raw:(" + DispenseStatus.DRAFT + " OR " + DispenseStatus.APPROVED + " OR " + DispenseStatus.REJECTED + " OR " + DispenseStatus.PENDING_APPROVAL + ")"))
            .withPageable(PageRequest.of(0, 10000))
            .build();

        //Iterator<Dispense> dispenseIterator = elasticsearchTemplate.queryForList(query, Dispense.class).listIterator();
        Iterator<Dispense> dispenseIterator = queryForList("dispense", query, elasticsearchTemplate, Dispense.class).listIterator();
        while (dispenseIterator.hasNext()) {
            Dispense dispense = dispenseIterator.next();
            try {
                autoCloseDispenseDocument(currentDateTime, dispense);
            } catch (Exception e) {
                log.error("Dispense indexing issue for Dispense number: {}", dispense.getDocumentNumber());
                reIndex(dispense.getId());
            }
        }
    }

    @Override
    public void autoCloseDispenseDocument(LocalDateTime currentDateTime, Dispense dispense) {

        String hoursForAutoClose = null;
        String processId = null;
        try {
            hoursForAutoClose = ConfigurationUtil.getConfigurationData("athma_dispense_autoclose_duration_hours", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_enable_workflow", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
            if (isWorkflowEnabled) {
                processId = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_workflow_definition", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            }
        } catch (Exception e) {
            //Do nothing
        }

        LocalDateTime modifiedDate = dispense.getDocument().getModifiedDate();
        if (modifiedDate != null && hoursForAutoClose != null) {
            LocalDateTime addedAutoCloseDuration = modifiedDate.plusHours(Long.parseLong(hoursForAutoClose));
            if (currentDateTime.isAfter(addedAutoCloseDuration)) {
                stockService.deleteReservedStock(dispense.getId(), TransactionType.Dispense);
                boolean isProcessInstanceCompleted = dispense.getDocument().getDispenseStatus().equals(DispenseStatus.APPROVED) ||  dispense.getDocument().getDispenseStatus().equals(DispenseStatus.REJECTED);
                dispense.getDocument().setDispenseStatus(DispenseStatus.CLOSED);
                dispense = save(dispense);
                index(dispense);
                if (processId != null && !isProcessInstanceCompleted) {
                    workflowService.abortActiveProcessInstance(processId, "document_number", dispense.getDocumentNumber(), "admin");
                }
            }
        }
    }

    @Override
    public void index(Dispense dispense) {
        dispenseSearchRepository.save(dispense);
    }

    /**
     * Populate tax definition for dispense line by item id and unit id
     *
     * @param documentLine
     * @param unitId
     */
    private void populateTaxMappingForLine(DispenseDocumentLine documentLine, Long unitId) {
        log.debug("Request to getTaxMappingForLine");
        float totalTaxInPercent = 0f;
        Query unitTaxMapQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery("taxMapping.applicableTo.raw:SALE unit.id:" + unitId).defaultOperator(Operator.AND)).build();

        List<UnitTaxMappingDTO> unitTaxMappings = queryForList("unittaxmapping", unitTaxMapQuery, elasticsearchTemplate, UnitTaxMappingDTO.class);
        List<DispenseTax> dispenseTaxList = new ArrayList<>();
        for (UnitTaxMappingDTO unitTaxMappingDTO : unitTaxMappings) {
            String queryString = "item.id:" + documentLine.getItemId() + " taxMapping.id:" + unitTaxMappingDTO.getTaxMapping().getId();
            Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryStringQuery(queryString).defaultOperator(Operator.AND)).build();
            List<ItemTaxMapping> itemTaxMappingList = queryForItemTaxMappingList("itemtaxmapping", searchQuery, elasticsearchTemplate, ItemTaxMapping.class,queryString);
            for (ItemTaxMapping itemTaxMapping : itemTaxMappingList) {
                if (itemTaxMapping.getItem().getId().equals(documentLine.getItemId())) {
                    TaxDefinition taxDefinition = itemTaxMapping.getTaxDefinition();
                    DispenseTax dispenseTax = new DispenseTax();
                    dispenseTax.setTaxCode(taxDefinition.getCode());
                    dispenseTax.setDefinition(taxDefinition.getName());
                    dispenseTax.setTaxDefinition(taxDefinition);
                    dispenseTax.setAttributes(new HashMap<String, String>() {{
                        put("hsnCode", itemTaxMapping.getHsnCode());
                    }});
                    dispenseTaxList.add(dispenseTax);
                    totalTaxInPercent += calculateTotalTaxInPercentForLine(taxDefinition);
                }
            }
        }
        documentLine.setDispenseTaxes(dispenseTaxList);
        documentLine.setTotalTaxInPercent(totalTaxInPercent);
    }

    private List<ItemTaxMapping> queryForItemTaxMappingList(String indexName, Query searchQuery, ElasticsearchOperations elasticsearchTemplate, Class<ItemTaxMapping> itemTaxMappingClass, String queryString) {
        if(applicationProperties.getRedisCache().isCacheEnabled() && applicationProperties.getRedisCache().isTaxMappingCacheEnabled())
        {
            return pharmacyRedisCacheService.fetchItemTaxMapping(indexName,searchQuery,elasticsearchTemplate,itemTaxMappingClass,queryString);
        }else
        {
            return queryForList(indexName, searchQuery, elasticsearchTemplate, itemTaxMappingClass);
        }
    }

    private float calculateTotalTaxInPercentForLine(TaxDefinition taxDefinition) {
        TaxCalculation taxCalculation = taxDefinition.getTaxCalculation();
        if (Type.Percentage.equals(taxCalculation.getType())) {
            return taxCalculation.getPercentage();
        }
        return 0f;
    }

    private void validateDispense(Dispense dispense) {
        KieSession kSession = kieBase.newKieSession();
        try {
            kSession.insert(dispense);
            kSession.fireAllRules(new RuleNameStartsWithAgendaFilter("Dispense_Validation"));
        } finally {
            kSession.dispose();
        }
    }

    /**
     * Calculate Dispense details
     *
     * @param dispense
     * @return
     */
    /*@Override
    public Dispense calculateDispenseDetail(Dispense dispense) {
        validateDispense(dispense);
        this.resetDocument(dispense);
        //Populating tax definition for lines
        populateLineTaxDefinition(dispense);
        //Update document and line details
        //updateDispenseDocument(dispense);

        List<DispensePlan> dispensePlans = dispense.getDocument().getDispensePlans();
        if (dispensePlans != null && !dispensePlans.isEmpty()) {
            for (DispensePlan dispensePlan : dispensePlans) {
                executePlan(dispense, dispensePlan);
            }
        }

        dispense.getDocument().getDispenseDocumentLines().stream().filter(line ->
            line.isListNullOrEmpty(line.getDispenseItemPlans())).forEach(documentLine -> {
            dispense.updateLineWithoutPlanRuleDetail(documentLine);
        });

        if (!dispense.getDocument().isDiscountPercentage()) {
            dispense.getDocument().updateGrossAmount();
        }

        if (dispense.getDocument().getDispenseDocumentLines().stream().anyMatch(line -> line.getPlanDiscAmount() > 0)) {
            dispense.getDocument().setUnitDiscountPercentage(0f);
            dispense.getDocument().setUnitDiscountAmount(0f);
        }
        dispense.applyUnitDiscount();

        if (dispensePlans == null || dispensePlans.isEmpty()) {
            calculateDocumentAmountWithoutPlan(dispense);
        } else {
            calculatePlanWiseAmountForDocument(dispense);
        }

        return dispense;
    }*/
    private void executePlan(Dispense dispense, DispensePlan dispensePlan) {
        KieSession kSession = kieBase.newKieSession();
        try {
            kSession.insert(dispense);
            kSession.insert(dispensePlan);
            kSession.insert(dispensePlan.getPlanRule().getPlanRules());
            kSession.fireAllRules(new RuleNameStartsWithAgendaFilter("Dispense_Rules"));
        } finally {
            kSession.dispose();
        }
    }

    private void populateLineTaxDefinition(Dispense dispense) {
        log.debug("Request to populateTaxDefinition for Document Number : {}", dispense.getDocumentNumber());
        Long unitId = dispense.getDocument().getDispenseUnit().getId();
        dispense.getDocument().getDispenseDocumentLines().forEach(documentLine -> {
            //Populate tax definitions for given line
            List<DispenseTax> dispenseTaxList = documentLine.getDispenseTaxes();
            if (dispenseTaxList == null || dispenseTaxList.isEmpty()) {
                populateTaxMappingForLine(documentLine, unitId);
            }
        });
    }

   /* @Deprecated
    private void updateDispenseDocument(Dispense dispense) {
        dispense.getDocument().getDispenseDocumentLines().forEach(line -> {
            line.setSaleRate(line.getMrp());
            line.setSaleAmount(line.getSaleRate() * line.getQuantity());
            BigDecimal totalTaxAmount = CalculateTaxUtil.calculateTax(line.getSaleAmount(), line.getTotalTaxInPercent());
            line.setTaxAmount(totalTaxAmount);
            //split tax
            line.splitTaxAmountBasedOnTaxDefinition();
            line.setGrossAmount(line.getSaleAmount() - totalTaxAmount);
            line.setGrossRate(line.getGrossAmount() / line.getQuantity());
            dispense.getDocument().setGrossAmount(dispense.getDocument().getGrossAmount() + line.getGrossAmount());
            dispense.getDocument().setPatientGrossAmount(dispense.getDocument().getGrossAmount() + line.getGrossAmount());
        });
    }*/

    /**
     * Reset all the calculation
     *
     * @param dispense
     */
    @Override
    public void resetDocument(Dispense dispense) {
        log.debug("In resetDocument...." + dispense.getDocumentNumber());
        DispenseDocument dispenseDocument = dispense.getDocument();
        dispenseDocument.setGrossAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setDiscountAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setNetAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setPatientNetAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setPatientSaleAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setSponsorDiscount(BigDecimalUtil.ZERO);
        dispenseDocument.setPatientDiscount(BigDecimalUtil.ZERO);
        dispenseDocument.setPatientGrossAmount(BigDecimalUtil.ZERO);
        dispenseDocument.setRoundOff(BigDecimalUtil.ZERO);
        dispenseDocument.setTotalDiscountPercentage(0f);
        dispenseDocument.setDispenseTaxes(null);

        if (dispenseDocument.getDispensePlans() != null) {
            dispenseDocument.getDispensePlans().forEach(plan -> {
                plan.setPatientDiscount(BigDecimalUtil.ZERO);
                plan.setPlanTaxList(null);
                plan.setTotalTax(BigDecimalUtil.ZERO);
                plan.setRoundOff(BigDecimalUtil.ZERO);
                plan.setSponsorDiscount(BigDecimalUtil.ZERO);
                plan.setSponsorPayable(BigDecimalUtil.ZERO);
                plan.setSponsorGrossAmount(BigDecimalUtil.ZERO);
            });
        }

        dispenseDocument.getDispenseDocumentLines().forEach(documentLine -> {
            documentLine.resetLine(documentLine);
        });
    }

    /*private void calculateDocumentAmountWithoutPlan(Dispense dispense) {
        List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();
        Map<String, DispenseTax> taxCodeValueMap = new HashMap<>();
        //Document amount calculation
        this.calculateDocumentPaymentSummary(dispense.getDocument(), taxCodeValueMap);
        //Split Document Tax amount
        dispense.getDocument().setDispenseTaxes(new ArrayList<DispenseTax>(taxCodeValueMap.values()));
        //Header Discount Percentage
        calculateHeaderDiscount(dispense.getDocument());
    }*/

    /*private void calculatePlanWiseAmountForDocument(Dispense dispense) {
        Map<String, DispensePlan> planMap = new HashMap<>();
        Map<String, Map<String, DispenseTax>> planTaxMapping = new HashMap<>();
        List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();
        Map<String, DispenseTax> taxCodeValueMap = new HashMap<>();
        //Document amount calculation
        this.calculateDocumentPaymentSummary(dispense.getDocument(), taxCodeValueMap);


        for (DispenseDocumentLine documentLine : lines) {
            //Plan level amount calculation
            List<DispenseItemPlan> itemPlans = documentLine.getDispenseItemPlans();
            if (itemPlans == null || itemPlans.isEmpty()) {
                continue;
            }
            //calculate plan amount using item plan
            for (DispenseItemPlan itemPlan : itemPlans) {
                String planCode = itemPlan.getPlanRef().getCode();
                DispensePlan docPlan = planMap.get(planCode);
                if (docPlan == null) {
                    docPlan = new DispensePlan();
                }
                docPlan.setSponsorGrossAmount(docPlan.getSponsorGrossAmount() + itemPlan.getSponsorGrossAmount());
                docPlan.setSponsorDiscount(docPlan.getSponsorDiscount() + itemPlan.getSponsorDiscAmount());
                docPlan.setSponsorPayable(docPlan.getSponsorPayable() + itemPlan.getSponsorNetAmount());
                docPlan.setPatientDiscount(docPlan.getPatientDiscount() + itemPlan.getPatientDiscAmount());
                planMap.put(planCode, docPlan);
                //Calculate document sponsor tax details by dispense item plan taxes
                calculatePlanTotalTaxForDocument(itemPlan, planTaxMapping);
            }
        }
        //Split Document Tax amount
        dispense.getDocument().setDispenseTaxes(new ArrayList<DispenseTax>(taxCodeValueMap.values()));
        //Document plan summary calculation
        calculateDocumentPlanSummary(dispense.getDocument(), planMap, planTaxMapping);
        //Header Discount Percentage
        calculateHeaderDiscount(dispense.getDocument());
    }*/

    /*private void calculatePlanTotalTaxForDocument(DispenseItemPlan itemPlan, Map<String, Map<String, DispenseTax>> planTaxMapping) {

        String planCode = itemPlan.getPlanRef().getCode();
        List<DispenseTax> itemPlanTaxes = itemPlan.getPlanTaxList();
        if (itemPlanTaxes == null) {
            return;
        }

        itemPlan.getPlanTaxList().forEach(lineTax -> {
            Map<String, DispenseTax> taxMap = planTaxMapping.get(planCode);
            if (taxMap == null) {
                taxMap = new HashMap<>();
            }
            String lineTaxCode = lineTax.getTaxCode();
            DispenseTax dispenseTax = taxMap.get(lineTaxCode);
            if (dispenseTax == null) {
                dispenseTax = new DispenseTax();
                dispenseTax.setTaxCode(lineTaxCode);
                dispenseTax.setTaxDefinition(lineTax.getTaxDefinition());
            }
            dispenseTax.setTaxAmount(dispenseTax.getTaxAmount() + lineTax.getTaxAmount());
            taxMap.put(lineTaxCode, dispenseTax);
            planTaxMapping.put(planCode, taxMap);
        });
    }*/

   /* private void calculateDocumentPlanSummary(DispenseDocument document, Map<String, DispensePlan> planMap,
                                              Map<String, Map<String, DispenseTax>> planTaxMapping) {

        List<DispensePlan> dispensePlans = document.getDispensePlans();
        for (DispensePlan documentPlan : dispensePlans) {
            String planCode = documentPlan.getPlanRef().getCode();
            DispensePlan tempPlan = planMap.get(planCode);
            if (tempPlan == null) {
                continue;
            }
            Map<String, DispenseTax> taxMap = planTaxMapping.get(planCode);
            if (taxMap != null && !taxMap.isEmpty()) {
                List<DispenseTax> dispenseTaxes = new ArrayList();
                documentPlan.setPlanTaxList(dispenseTaxes);
                for (DispenseTax dispenseTax : taxMap.values()) {
                    DispenseTax taxDetail = new DispenseTax();
                    taxDetail.setTaxCode(dispenseTax.getTaxCode());
                    taxDetail.setTaxAmount(dispenseTax.getTaxAmount());
                    taxDetail.setTaxDefinition(dispenseTax.getTaxDefinition());
                    taxDetail.setDefinition(dispenseTax.getDefinition());
                    taxDetail.setPatientTaxAmount(dispenseTax.getPatientTaxAmount());
                    dispenseTaxes.add(taxDetail);
                    documentPlan.setTotalTax(documentPlan.getTotalTax() + taxDetail.getTaxAmount());
                }
                documentPlan.setTotalTax(roundOff(documentPlan.getTotalTax(), 2));
            }
            documentPlan.setSponsorGrossAmount(tempPlan.getSponsorGrossAmount());
            documentPlan.setSponsorDiscount(tempPlan.getSponsorDiscount());
            documentPlan.setSponsorPayable(roundOff(tempPlan.getSponsorPayable(), 0));
            document.setTotalSponsorAmount(document.getTotalSponsorAmount() + documentPlan.getSponsorPayable());
            documentPlan.setRoundOff(documentPlan.getSponsorPayable() - tempPlan.getSponsorPayable());
            documentPlan.setPatientDiscount(tempPlan.getPatientDiscount());
        }
    }*/

    /*private void calculateDocumentPaymentSummary(DispenseDocument document, Map<String, DispenseTax> taxCodeValueMap) {

        BigDecimal grossAmt = 0f, sponsorDiscAmt = 0f, patientTotalDiscAmt = 0f, netAmt = 0f, totalSponsorAmt = 0f, patientSaleAmt = 0f;
        BigDecimal patientNetAmt = 0f, discountAmt = 0f, patientGrossAmt = 0f, unitDiscAmt = 0f, planDiscAmount = 0f, taxDiscountAmount = 0f;
        for (DispenseDocumentLine line : document.getDispenseDocumentLines()) {
            netAmt += line.getNetAmount();
            grossAmt += line.getGrossAmount();
            sponsorDiscAmt += line.getSponsorDiscAmount();
            planDiscAmount += line.getPlanDiscAmount();
            patientTotalDiscAmt += line.getPatientTotalDiscAmount();
            patientNetAmt += line.getPatientNetAmount();
            discountAmt += line.getTotalDiscountAmount();
            unitDiscAmt += line.getUnitDiscount();
            patientGrossAmt += line.getPatientGrossAmount();
            patientSaleAmt += line.getPatientSaleAmount();
            taxDiscountAmount += line.getTaxDiscountAmount();

            //Add all tax amount based on taxCode
            List<DispenseTax> taxList = line.getDispenseTaxes();
            for (DispenseTax dispenseTax : taxList) {
                DispenseTax mapTax = taxCodeValueMap.get(dispenseTax.getTaxCode());
                if (mapTax == null) {
                    mapTax = new DispenseTax();
                }
                mapTax.setTaxCode(dispenseTax.getTaxCode());
                mapTax.setTaxAmount(mapTax.getTaxAmount() + dispenseTax.getTaxAmount());
                mapTax.setPatientTaxAmount(mapTax.getPatientTaxAmount() + dispenseTax.getPatientTaxAmount());
                //mapTax.setTaxDefinition(dispenseTax.getTaxDefinition());
                taxCodeValueMap.put(dispenseTax.getTaxCode(), mapTax);
            }
        }
        int roundedPatientNetAmt = Math.round(patientNetAmt);
        document.setGrossAmount(grossAmt);
        document.setSponsorDiscount(sponsorDiscAmt);
        document.setPatientDiscount(patientTotalDiscAmt);
        document.setTaxDiscount(taxDiscountAmount);
        document.setPlanDiscountAmount(planDiscAmount);
        document.setNetAmount(netAmt);
        document.setTotalSponsorAmount(totalSponsorAmt);
        document.setPatientNetAmount((float) roundedPatientNetAmt);
        document.setDiscountAmount(discountAmt);
        document.setUnitDiscountAmount(unitDiscAmt);
        document.setPatientGrossAmount(patientGrossAmt);
        document.setPatientSaleAmount(patientSaleAmt);
        document.setRoundOff(roundedPatientNetAmt - patientNetAmt);
    }*/

    /*private void calculateHeaderDiscount(DispenseDocument document) {
        //boolean isDiscPercent = document.isDiscountPercentage();
        float totalDiscAmount = roundOff(document.getPatientDiscount()
            - document.getPlanDiscountAmount()
            + document.getSponsorDiscount() - document.getTaxDiscount(), 2);
        float discOnAmount = document.getPatientGrossAmount();
        //if (user + unit) discount is 100% than disc should be given on tax amount
        if (totalDiscAmount > 0) {
            *//*float totalDiscountOnAmount = 0;
            for (DispenseTax dispenseTax:document.getDispenseTaxes()) {
                totalDiscountOnAmount += dispenseTax.getPatientTaxAmount();
            }
            totalDiscountOnAmount = roundOff(totalDiscountOnAmount+discOnAmount,2);*//*
            float totalDiscPercentage = (totalDiscAmount * 100f) / discOnAmount;
            document.setTotalDiscountPercentage(roundOff(totalDiscPercentage, 2));
        } else if (totalDiscAmount == 0) {
            document.setTotalDiscountPercentage(0f);
        }

    }*/

    //Read "athma_pharmacy_dispense_tax_calculation_type" key configuration and Populate tax calculation type for dispense .
    private void populateConfigurationDetails(Dispense dispense) {
        log.debug("In populateConfigurationDetails" + dispense.getDocumentNumber());
        String discountType = null;
        if (dispense.getDocument().getDiscountType() == null || dispense.getDocument().getDiscountType().isEmpty()) {
            discountType = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_discount_type",
                dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            log.debug("Configured value of key athma_pharmacy_dispense_discount_type: {}", discountType);
            dispense.getDocument().setDiscountType(discountType);
        }

        if (!"MARGIN_BASED_DISCOUNT".equals(discountType)) {
            String defaultUnitDiscount = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_default_discount_percentage",
                dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            log.debug("Configured value of key athma_pharmacy_dispense_default_discount_percentage: {}", defaultUnitDiscount);
            if (defaultUnitDiscount == null || defaultUnitDiscount.trim().isEmpty()) {
                dispense.getDocument().setUnitDiscountPercentage(0f);
            } else {
                dispense.getDocument().setUnitDiscountPercentage(Float.valueOf(defaultUnitDiscount));
            }
        }
        populateConfigurationForMBD(dispense.getDocument(), discountType);
        //Tax calculation after/before discount
        if (dispense.getDocument().getTaxCalculationType() == null || dispense.getDocument().getTaxCalculationType().isEmpty()) {
            String taxCalculationType = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_tax_calculation_type",
                dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
            log.debug("Configured value of key athma_pharmacy_dispense_tax_calculation_type: {}", taxCalculationType);
            dispense.getDocument().setTaxCalculationType(taxCalculationType);
        }
    }

    private void populateConfigurationForMBD(DispenseDocument document, String discountType){
        if ("MARGIN_BASED_DISCOUNT".equals(discountType)) {
            if (document.getDiscountFormula() == null || document.getDiscountFormula().isEmpty()) {
                String key =  "athma_pharmacy_dispense_margin_based_discount_formula";
                if(isIpPatient(document.getEncounter())){
                    key =  "athma_pharmacy_dispense_margin_based_discount_formula_for_ip_pharmacy";
                }

                String discountFormula = ConfigurationUtil.getConfigurationData(key,
                    document.getHsc().getId(), document.getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);

                if (discountFormula == null || discountFormula.isEmpty()) {
                    new RuntimeException("Margin based discount formula is not configured for "+key);
                }
                log.debug("Configured value of {}: {}", key, discountFormula);
                document.setDiscountFormula(discountFormula);
            }

            if (document.getDiscountSlab() == null || document.getDiscountSlab().isEmpty()) {
                String discountSlab = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_margin_based_discount_slab",
                    document.getHsc().getId(), document.getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);

                if (discountSlab == null || discountSlab.isEmpty()) {
                    new RuntimeException("Margin based discount slab not configured.");
                }
                log.debug("Configured value of key athma_pharmacy_dispense_margin_based_discount_slab: {}", discountSlab);
                document.setDiscountSlab(discountSlab);
            }
        }
    }

    @Override
    public Dispense calculate(Dispense dispense, String action, Integer lineIndex) throws Exception {
        validateDispense(dispense);
        populateConfigurationDetails(dispense);

        // if ("NEW-LINE".equals(action) || "APPLY-PLAN".equals(action)) {
        //addPlanRules(dispense);
        if (!"APPLY-DISCOUNT".equals(action)) {
            this.planExecutionService.addPlanRules(dispense);
        }
        //}
        DispenseDocument document = dispense.getDocument();

        switch (action) {
            case "APPLY-DISCOUNT":
                document.updateAppliedUserDiscount(lineIndex);
                document.reCalculateTaxes();
                break;
            default:

                document.getDispenseDocumentLines().forEach(line -> {
                    if (null == line.getDispenseTaxes() || line.getDispenseTaxes().isEmpty()) {
                        populateTaxMappingForLine(line, document.getDispenseUnit().getId());
                    }
                    dispense.getDocument().getPatient().setAgeDTO();
                    this.planExecutionService.accumulateAmountAndQty(dispense);
                    calculateLineDetails(dispense, line);
                });
        }
        //validate and update discretionary discount
        dispense.getDocument().validateAndApplyDiscretionaryDiscount(lineIndex, document.getTaxCalculationType());
        dispense.getDocument().summarize();
        dispense.getDocument().updateSponsorNetAmount();
        return dispense;
    }

    public void calculateLineDetails(Dispense dispense, DispenseDocumentLine line) {
        log.debug("In calculateLineDetails..." + dispense.getDocumentNumber());
        //reset details
        line.reset();
        List<DispensePlan> dispensePlans = dispense.getDocument().getDispensePlans();
        if (dispensePlans != null && !dispensePlans.isEmpty()) {
            for (DispensePlan dispensePlan : dispensePlans) {
                executePlan(dispense, dispensePlan);
            }
        }
        //check plan is applicable
        dispense.getDocument().isUnitDiscountApplicable();
        //calculate sale, gross and tax amount
        line.calculateSale();
        line.calculateTotalTax();
        line.calculateGross();
        //co-payment
        line.updateCoPayment(dispense.getDocument().getTaxCalculationType());
        line.updateAliasDetails();
      /*  if(null != line.getPlanRuleDetail()) {
            applyAmountQuantityLimits(dispense,line);
        } */
        //apply unit disc based on discount type
        dispense.getDocument().setUnitLevelDiscountPercentage(line);
        //boolean isUnitDiscountApplicable = isUnitDiscountApplicable(line, dispense.getDocument().getDispenseUnit().getId());
        //dispense.getDocument().calculateUnitLevelDiscount(line, isUnitDiscountApplicable);
        applyUnitDiscount(dispense, line);
        line.validateAndApplyDiscretionaryDiscount(dispense.getDocument().getTaxCalculationType());
        line.calculateTax(dispense.getDocument().getTaxCalculationType());
    }

    private void applyUnitDiscount(Dispense dispense, DispenseDocumentLine line) {
        if(CollectionUtils.isNotEmpty(dispense.getDocument().getDispensePlans())) return;//if plan applied than don't calculate unit and unitDiscountException discount amount
        UnitDiscountException unitDiscountException = getItemDiscount(line, dispense.getDocument().getDispenseUnit().getId());
        if (null != unitDiscountException) {
            log.debug("UnitDiscountExceptiob", unitDiscountException.toString() + "Discount" + unitDiscountException.getDiscount().toString());
            line.applyUnitExceptionDiscount(unitDiscountException.getDiscount(), dispense.getDocument().getTaxCalculationType());
            log.debug("Unit Discount Applied :", line.getUnitDiscount());
            return;
        } else {
            dispense.getDocument().calculateUnitLevelDiscount(line, Boolean.TRUE);
        }
    }


    private void applyAmountQuantityLimits(Dispense dispense, DispenseDocumentLine line) {
        BigDecimal accumulatedAmount = ZERO;
        Float accumulatedQty = 0f;
        for (DispenseDocumentLine dispenseDocumentLine : dispense.getDocument().getDispenseDocumentLines()) {
            if (null == dispenseDocumentLine.getPlanRuleDetail() || !dispenseDocumentLine.getPlanRuleDetail().getId().equals(line.getPlanRuleDetail().getId())) {
                continue;
            }
            if (dispenseDocumentLine.getPlanRuleDetail().getComponent() == null) {
                if (null != dispenseDocumentLine.getPlanRuleDetail() && null != line.getPlanRuleDetail().getItemGroup() && dispenseDocumentLine.getPlanRuleDetail().getItemGroup().getId().equals(line.getItemGroup().getId())) {
                    accumulatedAmount = add(accumulatedAmount, dispenseDocumentLine.getSponsorNetAmount());
                    accumulatedQty = accumulatedQty + dispenseDocumentLine.getQuantity();
                } else if (null != dispenseDocumentLine.getPlanRuleDetail() && null == dispenseDocumentLine.getPlanRuleDetail().getItemGroup()) {
                    accumulatedAmount = add(accumulatedAmount, dispenseDocumentLine.getSponsorNetAmount());
                    accumulatedQty = accumulatedQty + dispenseDocumentLine.getQuantity();
                }
            } else {
                if (null != dispenseDocumentLine.getPlanRuleDetail() && dispenseDocumentLine.getItemId().equals(line.getItemId())) {
                    accumulatedAmount = add(accumulatedAmount, dispenseDocumentLine.getSponsorNetAmount());
                    accumulatedQty = accumulatedQty + dispenseDocumentLine.getQuantity();
                }
            }
        }
        PlanRuleDetail planRuleDetail = line.getPlanRuleDetail();
/*
            if((planRuleDetail.getMaxAmount()!=null
                && lt (getBigDecimal(planRuleDetail.getMaxAmount()),add(line.getPatientNetAmount() , accumulatedAmount)))
                || (planRuleDetail.getMaxQuantity()!=null && planRuleDetail.getMaxQuantity()< line.getQuantity()+ accumulatedQty)
                ){
                line.removePlan(dispense.getDocument().getTaxCalculationType());
            }\
            */
    }

    /**
     * If record count is zero (0) then apply unit discount.
     *
     * @param line
     * @param unitId
     * @return
     */
    private boolean isUnitDiscountApplicable(DispenseDocumentLine line, Long unitId) {
        log.debug("In isUnitDiscountApplicable...");

        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery("item.id:" + line.getItemId() + " unit.id:" + unitId + " active:true")
            .defaultOperator(Operator.AND)).build();
        return elasticsearchTemplate.count(query, IndexCoordinates.of("unitdiscountexception")) == 0;
    }


    private UnitDiscountException getItemDiscount(DispenseDocumentLine line, Long unitId) {
        log.debug("In isUnitDiscountApplicable...");
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery("item.id:" + line.getItemId() + " unit.id:" + unitId + " active:true")
            .defaultOperator(Operator.AND)).build();
        List<UnitDiscountException> unitDiscountExceptions = queryForList("unitdiscountexception",searchQuery, elasticsearchTemplate, UnitDiscountException.class);
        if (null == unitDiscountExceptions || unitDiscountExceptions.isEmpty()) {
            return null;
        } else {
            return unitDiscountExceptions.get(0);
        }
    }

    /*public Dispense calculate_old(Dispense dispense, String action, Integer lineIndex) {
        validateDispense(dispense);
        switch (action) {
            case "NEW-LINE":
                populateTaxMappingForLine(dispense.getDocument().getDispenseDocumentLines().get(lineIndex),
                    dispense.getDocument().getDispenseUnit().getId());
                DispenseDocumentLine newLine = dispense.getDocument().getDispenseDocumentLines().get(lineIndex);
                newLine.calculateGross();
                newLine.setExecutePlan(true);
                applyPlan(dispense);
                newLine.setExecutePlan(false);
                dispense.getDocument().applyDiscountForLine(newLine);
                dispense.getDocument().resetUnitDiscountIfPlanDiscountExists();
                break;
            case "APPLY-PLAN":
                dispense.getDocument().getDispenseDocumentLines().forEach(line -> {
                    line.reset();
                    line.resetForPlan();
                    line.setExecutePlan(true);
                });
                applyPlan(dispense);
                dispense.getDocument().getDispenseDocumentLines().forEach(line -> line.setExecutePlan(false));
                dispense.getDocument().resetUnitDiscountIfPlanDiscountExists();
                break;
            case "APPLY-DISCOUNT":
                dispense.getDocument().applyDiscount();
                break;
            case "APPLY-DISCOUNT-FOR-LINE":
                dispense.getDocument().applyDiscountForLine(dispense.getDocument().getDispenseDocumentLines().get(lineIndex));
                break;
            case "LINE-MODIFIED":
                DispenseDocumentLine modifiedLine = dispense.getDocument().getDispenseDocumentLines().get(lineIndex);
                modifiedLine.reset();
                modifiedLine.calculateGross();
                modifiedLine.setExecutePlan(true);
                applyPlan(dispense);
                modifiedLine.setExecutePlan(false);
                dispense.getDocument().getDispenseDocumentLines().get(lineIndex).summarizeDiscount();
                dispense.getDocument().resetUnitDiscountIfPlanDiscountExists();
            case "ALL":
                executeForAllLines(dispense);
                break;
        } //FOR ALL ACTIONS
        dispense.getDocument().checkAndApplyForFullDiscount();
        dispense.getDocument().summarize();
        dispense.getDocument().updateSponsorNetAmount();

        return dispense;
    }*/

    /*private void executeForAllLines(Dispense dispense) {
        dispense.getDocument().getDispenseDocumentLines()
            .forEach(line -> {
                line.reset();
                line.setExecutePlan(true);
                populateTaxMappingForLine(line, dispense.getDocument().getDispenseUnit().getId());
            });
        applyPlan(dispense);
        dispense.getDocument().getDispenseDocumentLines().forEach(line -> line.setExecutePlan(false));
        dispense.getDocument().applyDiscount();
    }*/

    /*private void applyPlan(Dispense dispense) {
        List<DispensePlan> dispensePlans = dispense.getDocument().getDispensePlans();
        if (dispensePlans != null && !dispensePlans.isEmpty()) {
            for (DispensePlan dispensePlan : dispensePlans) {
                executePlan(dispense, dispensePlan);
            }
        }
        dispense.getDocument().getDispenseDocumentLines().stream()
            .filter(line -> line.getExecutePlan()
                && Optional.ofNullable(line.getDispenseItemPlans()).orElse(Collections.EMPTY_LIST).isEmpty())
            .forEach(line -> {
                line.reset();
                line.calculateGross();
                line.calculatePatientGross(100f);
                line.calculateTax(100f);
            });
        dispense.getDocument().updateGrossAmount();
    }*/

    /***
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> dashboardDiscount(Long unitId, Date fromDate, Date toDate) throws Exception {
        log.debug("Request to get Dashboard Discount data");
        Map<String, Object> result = new HashMap<>();
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sm.format(fromDate);
        String toDateStr = sm.format(toDate);

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest dispenseSearchRequest= new SearchRequest("dispense");
        SearchSourceBuilder dispenseQueryReq = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.dispenseDate:[" + fromDateStr + " TO " + toDateStr + "] document.dispenseStatus:DISPENSED")
                .defaultOperator(Operator.AND))
            .aggregation(AggregationBuilders.sum("unitDiscountDispense").field("document.dispenseDocumentLines.unitDiscount"))
            .aggregation(AggregationBuilders.sum("userDiscountDispense").field("document.dispenseDocumentLines.userDiscount"))
            .aggregation(AggregationBuilders.sum("sponsorDiscAmountDispense").field("document.dispenseDocumentLines.sponsorDiscAmount"))
            .aggregation(AggregationBuilders.sum("planDiscAmountDispense").field("document.dispenseDocumentLines.planDiscAmount"));
        dispenseSearchRequest.source(dispenseQueryReq);

        SearchRequest dispenseReturnSearchRequest= new SearchRequest("dispensereturn");
        SearchSourceBuilder dispenseReturnQueryReq = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.returnDate:[" + fromDateStr + " TO " + toDateStr + "] document.returnStatus:RETURNED")
                .defaultOperator(Operator.AND))
            .aggregation(AggregationBuilders.sum("unitDiscountReturn").field("document.dispenseReturnDocumentLines.unitDiscount"))
            .aggregation(AggregationBuilders.sum("userDiscountReturn").field("document.dispenseReturnDocumentLines.userDiscount"))
            .aggregation(AggregationBuilders.sum("sponsorDiscAmountReturn").field("document.dispenseReturnDocumentLines.sponsorDiscAmount"))
            .aggregation(AggregationBuilders.sum("planDiscAmountReturn").field("document.dispenseReturnDocumentLines.planDiscAmount"));
        dispenseReturnSearchRequest.source(dispenseReturnQueryReq);
        request.add(dispenseSearchRequest);
        request.add(dispenseReturnSearchRequest);

        MultiSearchResponse mSearchCollectionResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item[] items = mSearchCollectionResponse.getResponses();
        Map<String, Object> tempMap = new HashMap<>();

        for (MultiSearchResponse.Item item : items) {
            Aggregations discountAggs = item.getResponse().getAggregations();
            for (Aggregation agg : discountAggs) {
                tempMap.put(agg.getName(), ((ParsedSum) agg).getValue());
            }
        }

        result.put("unitDiscount", Double.parseDouble(tempMap.get("unitDiscountDispense").toString()) - Double.parseDouble(tempMap.get("unitDiscountReturn").toString()));
        result.put("userDiscount", Double.parseDouble(tempMap.get("userDiscountDispense").toString()) - Double.parseDouble(tempMap.get("userDiscountReturn").toString()));
        result.put("sponsorDiscAmount", Double.parseDouble(tempMap.get("sponsorDiscAmountDispense").toString()) - Double.parseDouble(tempMap.get("sponsorDiscAmountReturn").toString()));
        result.put("planDiscAmount", Double.parseDouble(tempMap.get("sponsorDiscAmountDispense").toString()) - Double.parseDouble(tempMap.get("planDiscAmountReturn").toString()));

        return result;
    }

    /****
     *
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> dashboardDispenseProductivity(Long unitId, Date fromDate, Date toDate) throws Exception {
        log.debug("Request to get Dashboard Productivity");
        Map<String, Object> result = new HashMap<>();
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sm.format(fromDate);
        String toDateStr = sm.format(toDate);


        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest dispenseAvgRevQueryReq= new SearchRequest("dispense");
        SearchSourceBuilder dispenseQueryReq = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.dispenseDate:[" + fromDateStr + " TO " + toDateStr + "] document.dispenseStatus:DISPENSED"+" document.encounter.encounterClass.code.keyword:AMB ")//changes made for showing op dispense in dasboard count
                .defaultOperator(Operator.AND))
            .aggregation(AggregationBuilders.avg("avgRevenue").field("document.netAmount"));
        dispenseAvgRevQueryReq.source(dispenseQueryReq);

        SearchRequest dispensereturnQueryReq= new SearchRequest("dispensereturn");
        SearchSourceBuilder dispenseReturnQueryReq = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.returnDate:[" + fromDateStr + " TO " + toDateStr + "] document.returnStatus:RETURNED")
                .defaultOperator(Operator.AND));
        dispensereturnQueryReq.source(dispenseReturnQueryReq);
        request.add(dispenseAvgRevQueryReq);request.add(dispensereturnQueryReq);
        MultiSearchResponse mSearchDispenseResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item[] items = mSearchDispenseResponse.getResponses();
        Long totalDispenses = items[0].getResponse().getHits().getTotalHits().value;
        result.put("totalDispenses", totalDispenses);
        Aggregations discountAggs = items[0].getResponse().getAggregations();
        for (Aggregation agg : discountAggs) {
            result.put(agg.getName(), ((Double)((ParsedAvg) agg).getValue()).toString().equalsIgnoreCase("Infinity")?0:((ParsedAvg) agg).getValue());
        }
        Long totalDispenseReturn = items[1].getResponse().getHits().getTotalHits().value;
        result.put("totalDispenseReturn", totalDispenseReturn);
        return result;
    }

    /**
     * @param unitId
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> dashboardDispenseProductivityTrend(Long unitId, String format, Date fromDate, Date toDate) throws Exception {
        log.debug("Request to get Dashboard Productivity trend");
        Map<String, Object> result = new HashMap<>();
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sm.format(fromDate);
        String toDateStr = sm.format(toDate);

        DateHistogramInterval dateHistogramInterval = null;
        if (format.equalsIgnoreCase("MONTH")) {
            dateHistogramInterval = DateHistogramInterval.MONTH;
        } else if (format.equalsIgnoreCase("WEEK")) {
            dateHistogramInterval = DateHistogramInterval.WEEK;
        } else if (format.equalsIgnoreCase("DAY")) {
            dateHistogramInterval = DateHistogramInterval.DAY;
        }


        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest dispenseQueryReq= new SearchRequest("dispense");
        SearchSourceBuilder dispenseSourceQuery = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.dispenseDate:[" + fromDateStr + " TO " + toDateStr + "] document.dispenseStatus:DISPENSED")
                .defaultOperator(Operator.AND))
            .aggregation(AggregationBuilders.dateHistogram("dispenseCount")
                .field("document.dispenseDate").dateHistogramInterval(dateHistogramInterval).format("yyyy-MM-dd").minDocCount(0).extendedBounds(new ExtendedBounds(fromDateStr, toDateStr)));
        dispenseQueryReq.source(dispenseSourceQuery);

        SearchRequest dispensereturnQueryReq= new SearchRequest("dispensereturn");
        SearchSourceBuilder dispenseReturnSourceQuery = new SearchSourceBuilder().size(0)
            .query(QueryBuilders.queryStringQuery("document.dispenseUnit.id:" + unitId
                + " document.returnDate:[" + fromDateStr + " TO " + toDateStr + "] document.returnStatus:RETURNED")
                .defaultOperator(Operator.AND))
            .aggregation(AggregationBuilders.dateHistogram("dispenseReturnCount")
                .field("document.returnDate").dateHistogramInterval(dateHistogramInterval).format("yyyy-MM-dd").minDocCount(0).extendedBounds(new ExtendedBounds(fromDateStr, toDateStr)));
        dispensereturnQueryReq.source(dispenseReturnSourceQuery);

        request.add(dispenseQueryReq);request.add(dispensereturnQueryReq);

        MultiSearchResponse mSearchDispenseResponse = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item[] items = mSearchDispenseResponse.getResponses();
        Aggregations dispAggregations = items[0].getResponse().getAggregations();
        Histogram dispAggs = dispAggregations.get("dispenseCount");
        ArrayList<Long> dispeneList = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();

        for (Histogram.Bucket bucket : dispAggs.getBuckets()) {
            String dateKey = bucket.getKeyAsString();
            String convertedDateKey = "";
            if (format.equalsIgnoreCase("MONTH")) {
                LocalDate givenDate = LocalDate.parse(dateKey);
                convertedDateKey = givenDate.getMonth().toString().substring(0, 3) + " " + givenDate.getYear();
            } else if (format.equalsIgnoreCase("WEEK")) {
                SimpleDateFormat sfResult = new SimpleDateFormat("dd/MM");
                Date dateTmp = sm.parse(dateKey);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateTmp);
                cal.add(Calendar.DATE, 6);
                convertedDateKey = sfResult.format(dateTmp) + " to " + sfResult.format(cal.getTime());
            } else if (format.equalsIgnoreCase("DAY")) {
                SimpleDateFormat sfDayResult = new SimpleDateFormat("dd-MM-yyyy");
                convertedDateKey = sfDayResult.format(sm.parse(dateKey));
            }

            keys.add(convertedDateKey);
            dispeneList.add(bucket.getDocCount());
        }

        Aggregations dispReturnAggregations = items[1].getResponse().getAggregations();
        Histogram returnAggs = dispReturnAggregations.get("dispenseReturnCount");
        ArrayList<Long> dispeneReturnList = new ArrayList<>();
        for (Histogram.Bucket bucket : returnAggs.getBuckets()) {
            dispeneReturnList.add(bucket.getDocCount());
        }
        result.put("keys", keys);
        result.put("dispenses", dispeneList);
        result.put("dispenseReturns", dispeneReturnList);
        result.put("format", format);

        return result;
    }

    private Dispense addPlanRules(Dispense dispense) {
        log.debug("In addPlanRules..." + dispense.getDocumentNumber());
        if (null == dispense.getDocument().getDispensePlans() || dispense.getDocument().getDispensePlans().isEmpty())
            return dispense;

        dispense.getDocument().getDispensePlans().forEach(dispensePlan -> dispensePlan.getPlanRule().getPlanRules().getPlanRuleDetailsList().clear());


        for (DispensePlan dispensePlan : dispense.getDocument().getDispensePlans()) {

            StringBuilder planQueryBuilder = new StringBuilder();
            PlanRef planRef = dispensePlan.getPlanRef();
            Long planId = planRef.getId();
            Integer planLevel = planRef.getLevel();
            //planLevel=> 0-PlanTemplate, 1-Plan, 2-PatientPlan

            if (null != planLevel && planLevel > 0) {

                if (1 == planLevel) {
                    populateQueryFromPlanTypeToAddPlanRules(planId, planQueryBuilder);
                } else if (2 == planLevel) {

                    Page<PatientPlan> patientPlans = patientPlanService.search("id:" + planId, PageRequest.of(0, 1));

                    if (patientPlans.hasContent()) {

                        PatientPlan patientPlan = patientPlans.iterator().next();
                        Page<PlanRule> planRules = planRuleService.search(" type:patientplan AND typeId:" + patientPlan.getPlan().getId(), PageRequest.of(0, 1, Sort.Direction.DESC, "version"));

                        for (Iterator<PlanRule> planRuleIterator = planRules.iterator(); planRuleIterator.hasNext(); ) {

                            PlanRule planRuleInstance = planRuleIterator.next();
                            planQueryBuilder.append(" (type:patientplan AND typeId:").append(patientPlan.getId()).append(" AND version:").append(planRuleInstance.getVersion()).append(") OR ");
                            populateQueryFromPlanTypeToAddPlanRules(patientPlan.getPlan().getId(), planQueryBuilder);
                        }
                    }
                }
                //if plan does not have rule than planQueryBuilder length will be 0
                if (planQueryBuilder.length() == 0) {
                    return dispense;
                }
                for (DispenseDocumentLine dispenseDocumentLine : dispense.getDocument().getDispenseDocumentLines()) {

                    Long itemId = dispenseDocumentLine.getItemId();
                    Long itemGroupId = dispenseDocumentLine.getItemGroup().getId();

                    Long itemTypeId = dispenseDocumentLine.getItemType().getId();
                    Long itemCategoryId = dispenseDocumentLine.getItemCategory().getId();


                    StringBuilder queryBuilder = new StringBuilder(" planRuleType:Item")
                        .append(" AND component.id:(0 OR ").append(itemId).append(")")
                        .append(" AND itemGroup.id:(0 OR ").append(itemGroupId).append(")")
                        .append(" AND itemType.id:(0 OR ").append(itemTypeId).append(")")
                        .append(" AND itemCategory.id:(0 OR ").append(itemCategoryId).append(") ")
                        .append(" AND (");

                    queryBuilder.append(planQueryBuilder);
                    queryBuilder.append(")");

                    Query planRuleDocumentSearchQuery = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.queryStringQuery(queryBuilder.toString()))
                        .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC)).build();

                    List<org.nh.billing.domain.dto.PlanRuleDocument> planRuleDocumentList = planRuleDocumentSearchRepository.search(planRuleDocumentSearchQuery).getContent();
                    dispensePlan.getPlanRule().getPlanRules().getPlanRuleDetailsList().addAll(setPlanRuleDetailDocumentFromPlanRule(dispensePlan.getPlanRule().getPlanRules().getPlanRuleDetailsList(), planRuleDocumentList));
                }
            }

        }
        return dispense;
    }


    private StringBuilder populateQueryFromPlanTypeToAddPlanRules(Long planId, StringBuilder queryBuilder) {
        log.debug("In populateQueryFromPlanTypeToAddPlanRules...");
        Page<Plan> plans = planService.search("id:" + planId, PageRequest.of(0, 1));
        if (plans.hasContent()) {

            for (Iterator<Plan> planIterator = plans.iterator(); planIterator.hasNext(); ) {

                Plan planInstance = planIterator.next();
                Page<PlanRule> planRules = planRuleService.search(" type:plan AND typeId:" + planId, PageRequest.of(0, 1, Sort.Direction.DESC, "version"));

                for (Iterator<PlanRule> planRuleIterator = planRules.iterator(); planRuleIterator.hasNext(); ) {

                    PlanRule planRuleInstance = planRuleIterator.next();
                    queryBuilder.append("  (type:plan AND typeId:").append(planInstance.getId()).append(" AND version:").append(planRuleInstance.getVersion()).append(") ");

                    if (null != planInstance.getPartOf() && null != planInstance.getPartOf().getId()) {
                        queryBuilder.append(" OR ");
                        populateQueryFromPlanTypeToAddPlanRules(planInstance.getPartOf().getId(), queryBuilder);
                    }
                }
                if (null != planInstance.getPlanTemplate() && null != planInstance.getPlanTemplate().getId()) {

                    Page<PlanRule> planTemplatePlanRules = planRuleService.search(" type:plantemplate AND typeId:" + planInstance.getPlanTemplate().getId(), PageRequest.of(0, 1, Sort.Direction.DESC, "version"));
                    for (Iterator<PlanRule> planRuleIterator = planTemplatePlanRules.iterator(); planRuleIterator.hasNext(); ) {

                        PlanRule planRuleInstance = planRuleIterator.next();
                        queryBuilder.append(" OR ( type:plantemplate AND typeId:").append(planInstance.getPlanTemplate().getId()).append(" AND version:").append(planRuleInstance.getVersion()).append(") ");
                    }

                }


            }

        }

        return queryBuilder;
    }


    private List<PlanRuleDetail> setPlanRuleDetailDocumentFromPlanRule(List<PlanRuleDetail> planRuleDetailsList, List<org.nh.billing.domain.dto.PlanRuleDocument> planRuleDocumentList) {
        log.debug("In setPlanRuleDetailDocumentFromPlanRule...");
        planRuleDocumentList.stream().distinct().collect(Collectors.toList());
        List<PlanRuleDetail> planRuleDetailList = new ArrayList<>();

        for (org.nh.billing.domain.dto.PlanRuleDocument planRuleDocument : planRuleDocumentList) {

            PlanRuleDetail planRuleDetail = new PlanRuleDetail();

            planRuleDetail.setId(planRuleDocument.getId());
            planRuleDetail.setPlanRuleType(planRuleDocument.getPlanRuleType());
            planRuleDetail.setVisitType(planRuleDocument.getVisitType());
            planRuleDetail.setActive(planRuleDocument.getActive());

            planRuleDetail.setAliasCode(planRuleDocument.getAliasCode());
            planRuleDetail.setAliasName(planRuleDocument.getAliasName());

            planRuleDetail.setMinAmount(planRuleDocument.getMinAmount());
            planRuleDetail.setMaxAmount(planRuleDocument.getMaxAmount());

            planRuleDetail.setDays(planRuleDocument.getDays());
            planRuleDetail.setExclusion(planRuleDocument.getExclusion());
            planRuleDetail.setAuthorizationExclusion(planRuleDocument.getAuthorizationExclusion());

            planRuleDetail.setGender(planRuleDocument.getGender());
            planRuleDetail.setIsGeneric(planRuleDocument.getGeneric());
            planRuleDetail.setGroup(planRuleDocument.getGroup());

            planRuleDetail.setPatientCopayment(planRuleDocument.getPatientCopayment());
            planRuleDetail.setAppliedOnBase(planRuleDocument.getAppliedOnBase());

            planRuleDetail.setAppliedOnPatientAmount(planRuleDocument.getAppliedOnPatientAmount());
            planRuleDetail.setAppliedOnSponsorAmount(planRuleDocument.getAppliedOnSponsorAmount());

            planRuleDetail.setMinQuantity(planRuleDocument.getMinQuantity());
            planRuleDetail.setMaxQuantity(planRuleDocument.getMaxQuantity());

            planRuleDetail.setMaxAge(planRuleDocument.getMaxAge());
            planRuleDetail.setMinAge(planRuleDocument.getMinAge());

            planRuleDetail.setSponsorPayment(planRuleDocument.getSponsorPayment());
            planRuleDetail.setSponsorPayTax(planRuleDocument.isSponsorPayTax());

            planRuleDetail.setTariffClass(planRuleDocument.getTarrifClass());
            planRuleDetail.setTariffClassValue(planRuleDocument.getTarrifClassValue());
            planRuleDetail.setParentRuleId(planRuleDocument.getParentRuleId());

            planRuleDetail.setLevel(planRuleDocument.getLevel());
            planRuleDetail.setComponent(planRuleDocument.getComponent().getId() == 0 ? null : planRuleDocument.getComponent());

            planRuleDetail.setItemGroup(planRuleDocument.getItemGroup().getId() == 0 ? null : planRuleDocument.getItemGroup());
            planRuleDetail.setItemCategory(planRuleDocument.getItemCategory().getId() == 0 ? null : planRuleDocument.getItemCategory());
            planRuleDetail.setItemType(planRuleDocument.getItemType().getId() == 0 ? null : planRuleDocument.getItemType());

            planRuleDetailList.add(planRuleDetail);
        }
        return planRuleDetailList;
    }

    public void deleteValidation(Dispense dispense) {
        log.debug("validate before delete Dispense   : {}", dispense);
        if (dispense.getDocument().getDispenseStatus() != DispenseStatus.DRAFT) {
            throw new CustomParameterizedException("10088", "Can not delete document ,Only Draft Status document can be deleted");
        }
    }

    public void saveValidation(Dispense dispenseRef) {
        log.debug("In saveValidation..." + dispenseRef.getDocumentNumber());
        Dispense dispense = dispenseSearchRepository.findByDocumentNumber(dispenseRef.getDocumentNumber());
        if (null == dispense) {
            dispense = dispenseRef;
        }
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(dispense.getDocument().getDispenseUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with selected logged in unit");
        }
    }

    public void validateAndDeleteStockReserve(Long id) {
        log.debug("In validateAndDeleteStockReserve...");
        Dispense dispense = dispenseRepository.findOne(id);
        if (dispense == null || dispense.getDocument().getDispenseStatus().name().equals("DRAFT") || dispense.getDocument().getDispenseStatus().name().equals("DISPENSED")) {
            reserveStockRepository.deleteReservedStock(id, TransactionType.Dispense);
        }
    }

    @Override
    public Dispense updateItem(Long dispenseId, String dispenseNumber) throws Exception {
        Dispense dispense = this.getDispense(dispenseId, dispenseNumber);
        if (dispense == null) {
            log.error("Dispense document does not found.", dispenseNumber);
            throw new Exception("Dispense document does not found.");
        }
        Map<Long, Item> itemMap = this.updateDispense(dispense);
        this.updateInvoice(dispense.getDocument().getSource(), itemMap);
        return dispense;
    }

    /**
     * Method to save dipense for inpatient and publish the dispense lines to charge record
     *
     * @param dispense
     * @return
     */
    @Override
    @PublishStockTransaction
    @PamIntegration
    @PublishChargeRecord
    public Map<String, Object> saveIPDispense(Dispense dispense) {
        validatePatientStatus(dispense);
        Map<String,Object> documentMap = new HashMap<>();
        if((null == dispense.getId() || dispense.getDocumentNumber().startsWith("DRAFT-")) && DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus()))
            if(DispenseType.DIRECT_ISSUE.equals(dispense.getDocument().getDispenseType()))
                dispense.documentNumber(sequenceGeneratorService.generateSequence("ipPharmacyDirectIssue", "NH", dispense));
            else
                dispense.documentNumber(sequenceGeneratorService.generateSequence("ipPharmacyOrder", "NH", dispense));


        Dispense result = save(dispense);
        if(DispenseStatus.DISPENSED.equals(result.getDocument().getDispenseStatus())) {
            reserveStock(result);
            //Stock out for dispense item
            produce(result);
        }
        documentMap.put(PharmacyConstants.IP_DISPENSE, result);
        return documentMap;
    }
   public Dispense setMedicationObjectDetails(Dispense dispense) {
    //in case of direct dispense medication object is coming as null. so setting medication object
    dispense.getDocument().getDispenseDocumentLines().forEach(dispenseDocumentLine ->
    {
        if (null == dispenseDocumentLine.getMedication()) {
            Medication medication = new Medication();
            medication.setCode(dispenseDocumentLine.getCode());
            medication.setName(dispenseDocumentLine.getName());
            try {
                CriteriaQuery query = new CriteriaQuery(new Criteria("code.raw").is(dispenseDocumentLine.getCode()));
                Medication medicationObject = queryForObject("medication", query, elasticsearchTemplate, Medication.class);
                if (nonNull(medicationObject)) {
                    log.debug("Medication object found. Id={}", medicationObject.getManufacturer());
                    medication.setManufacturer(medicationObject.getManufacturer());
                    medication.setDrugStrength(medicationObject.getDrugStrength());
                    medication.setDrugForm(medicationObject.getDrugForm());
                }
                dispenseDocumentLine.setMedication(medication);
            } catch (Exception ex) {
                log.error("Error while fetching manufacturer inforamtion ex={}", ex);
            }
        }
    });
    return dispense;
}

    private void validatePatientStatus(Dispense dispense) {
        log.debug("Validating patient status before ip dispense");
        String query="encounter.documentNumber.raw:"+dispense.getDocument().getEncounter().getDocumentNumber()+" AND patientDetails.mrn.raw:"
            +dispense.getDocument().getPatient().getMrn()+" AND status.raw:(MARKED_FOR_DISCHARGE OR DISCHARGED)";
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery(query)).build();
        List<InPatientDTO> inPatientDTOS = queryForList("inpatient", searchQuery, elasticsearchTemplate, InPatientDTO.class);
        if(inPatientDTOS.size() > 0)
        {
            Map<String,Object> errorMap = new HashMap<>();
            errorMap.put("patientStatus",inPatientDTOS.get(0).getStatus());
            throw new CustomParameterizedException("10174",errorMap);
        }
    }

    private Map<Long, Item> updateDispense(Dispense dispense) {
        log.debug("Request to update dispense no ", dispense.getDocumentNumber());
        Map<Long, Item> itemMap = new HashMap();
        for (DispenseDocumentLine line : dispense.getDocument().getDispenseDocumentLines()) {
            Item item = itemMap.get(line.getItemId());
            if (null == item) {
                item = itemSearchRepository.findById(line.getItemId()).get();
                if (null == item) {
                    log.error("Item: {} does not found.", line.getItemId());
                    continue;
                }
                itemMap.put(line.getItemId(), item);
            }
            line.setTrackUOM(item.getTrackUOM().getUOMDTO());
            line.setUom(item.getTrackUOM().getUOMDTO());
            ItemCategoryDTO itemCategory = new ItemCategoryDTO();
            itemCategory.setId(item.getCategory().getId());
            itemCategory.setCode(item.getCategory().getCode());
            itemCategory.setDescription(item.getCategory().getDescription());
            line.setItemCategory(itemCategory);
        }
        dispense.setIsNew(false);
        this.saveOrUpdate(dispense);
        return itemMap;
    }

    private void updateInvoice(SourceDTO source, Map<Long, Item> itemMap) {
        log.debug("Request to update invoice no: {}, itemMap:{}", source.getReferenceNumber(), itemMap);
        Invoice invoice = invoiceService.findOne(source.getId());
        for (InvoiceItem invoiceItem : invoice.getInvoiceDocument().getInvoiceItems()) {
            Item item = itemMap.get(invoiceItem.getItem().getId());
            if (null != item) {
                invoiceItem.setTrackUOM(item.getTrackUOM().getUOMDTO());
                invoiceItem.getItem().setTrackUOM(invoiceItem.getTrackUOM());
                invoiceItem.getItem().setPurchaseUOM(item.getPurchaseUOM().getUOMDTO());
                invoiceItem.getItem().setSaleUOM(item.getSaleUOM().getUOMDTO());
                ItemCategoryDTO itemCategory = new ItemCategoryDTO();
                itemCategory.setId(item.getCategory().getId());
                itemCategory.setCode(item.getCategory().getCode());
                itemCategory.setDescription(item.getCategory().getDescription());
                invoiceItem.setItemCategory(itemCategory);
            }
        }
        invoiceService.saveOrUpdate(invoice);
    }

    private void roleBackFromElasticSearch(Iterator<Dispense> dispenseIterator) {
        dispenseIterator.forEachRemaining(dispense -> {
            reIndex(dispense.getId());
        });
    }

    /***
     *
     * @param dispenseNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    public Map<String, Object> compareVersion(String dispenseNumber, Integer versionFirst, Integer versionSecond) {
        log.debug("comparing for the dispence:" + dispenseNumber + " Version First:" + versionFirst + " Version Second:" + versionSecond);

        List<Dispense> dispenseList = dispenseRepository.findDispenseByVersions(dispenseNumber, versionFirst, versionSecond);
        Map<String, Object> documents = new HashedMap();
        for (Dispense dispense : dispenseList) {
            if (versionFirst.equals(dispense.getVersion())) {
                Map<String, Object> firstDoc = new HashedMap();
                firstDoc.put("dispenseNumber", dispense.getDocumentNumber());
                firstDoc.put("version", dispense.getVersion());
                firstDoc.put("document", dispense);
                documents.put("firstDocument", firstDoc);
            } else {
                Map<String, Object> secondDoc = new HashedMap();
                secondDoc.put("dispenseNumber", dispense.getDocumentNumber());
                secondDoc.put("version", dispense.getVersion());
                secondDoc.put("document", dispense);
                documents.put("secondDocument", secondDoc);
            }
        }
        return documents;
    }

    /***
     *
     * @param dispenseNumber
     * @return
     */
    public List<Integer> getAllVersion(String dispenseNumber) {
        log.debug("get All Version for the dispence:" + dispenseNumber);
        List<Integer> versionList = dispenseRepository.filndALlVersion(dispenseNumber);
        return versionList;
    }

    /**
     * To reserve stock
     *
     * @param dispense
     * @throws Exception
     */
    private void reserveStock(Dispense dispense) {
        List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();
        stockService.deleteReservedStock(dispense.getId(), TransactionType.Dispense);
        for (DispenseDocumentLine dispenseDocumentLine : lines) {
            if (dispenseDocumentLine.getQuantity() > 0) {
                stockService.deleteAndReserveStock(dispenseDocumentLine.getStockId(), dispenseDocumentLine.getItemId(), dispenseDocumentLine.getBatchNumber(),
                    dispense.getDocument().getHsc().getId(), dispenseDocumentLine.getQuantity(), dispense.getId(),
                    TransactionType.Dispense, dispense.getDocumentNumber(),
                    dispenseDocumentLine.getLineNumber(), dispense.getDocument().getDispenseDate(),dispense.getDocument().getCreatedBy().getId());
            }
        }
    }

    @Override
    public Map<String, String> exportIPDispenses(String query, Pageable pageable) throws IOException {
        log.debug("Request to export IP Dispenses in CSV File query {}", query);

        File file = ExportUtil.getCSVExportFile("ipDispense", applicationProperties.getAthmaBucket().getTempExport());
        FileWriter dispenseFileWriter = new FileWriter(file);
        Map<String, String> dispenseFileDetails = new HashMap<>();
        dispenseFileDetails.put(ExportUtilConstant.FILE_NAME, file.getName());
        dispenseFileDetails.put(ExportUtilConstant.PATH_REFERENCE, ExportUtilConstant.TEMP_EXPORT);


        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);

        String dateFormat = null;

        try (CSVPrinter csvFilePrinter = new CSVPrinter(dispenseFileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord((Object[]) PharmacyConstants.IP_DISPENSE_HEADER);
                Iterator<Dispense> dispenseIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();
                while (dispenseIterator.hasNext()) {
                    Dispense dispense = dispenseIterator.next();

                    if (dateFormat == null)
                        dateFormat = ConfigurationUtil.getConfigurationData("athma_date_format", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);

                    List dispenseRow = new ArrayList();

                    LocalDateTime orderedDate =null;
                    try {
                        Map<String,Object> orderSource = dispense.getDocument().getOrderSource();
                        orderedDate = (null != dispense.getDocument().getOrderSource() && StringUtils.isNotBlank((String) ((Map)orderSource.get("document")).get("createdDate"))) ? LocalDateTime.parse((String) ((Map)orderSource.get("document")).get("createdDate")) : null;
                    }catch (Exception ex)
                    {
                        log.error("Error while fetching the orderdate for dispense number={}",dispense.getDocumentNumber());
                    }
                    dispenseRow.add(null != dispense.getDocument().getOrderSource() ? dispense.getDocument().getOrderSource().get("documentNumber") : null);
                    dispenseRow.add(null != orderedDate ? DateUtil.getFormattedDateAsFunctionForCSVExport(orderedDate, dateFormat): null);
                    dispenseRow.add(dispense.getDocumentNumber());
                    dispenseRow.add(DateUtil.getFormattedDateAsFunctionForCSVExport(dispense.getDocument().getDispenseDate(), dateFormat));
                    dispenseRow.add(null != dispense.getDocument().getPatient()?dispense.getDocument().getPatient().getMrn():"");
                    dispenseRow.add(null != dispense.getDocument().getPatient()?dispense.getDocument().getPatient().getDisplayName():"");
                    dispenseRow.add(null != dispense.getDocument().getEncounter()?dispense.getDocument().getEncounter().getVisitNumber():"");
                    dispenseRow.add(null != dispense.getDocument().getCreatedBy()? dispense.getDocument().getCreatedBy().getDisplayName():"");
                    dispenseRow.add(null != dispense.getDocument().getOrderingHSC()? dispense.getDocument().getOrderingHSC().getName():"");
                    dispenseRow.add(null != dispense.getDocument().getDispenseUser()?dispense.getDocument().getDispenseUser().getDisplayName():"");
                    dispenseRow.add(null != dispense.getDocument().getHsc()? dispense.getDocument().getHsc().getName():"");
                    dispenseRow.add(dispense.getDocument().getDispensePlans().stream().map( dispensePlan -> dispensePlan.getSponsorRef().getName()).collect(Collectors.joining(",")));
                    dispenseRow.add(null != dispense.getDocument().getConsultant() ? dispense.getDocument().getConsultant().getDisplayName() : "");
                    dispenseRow.add(dispense.getDocument().getDispenseDocumentLines().size());

                    csvFilePrinter.printRecord(dispenseRow);
                }
        } catch (Exception ex) {
            log.error("Error while exporting ip dispenses",ex);
        } finally {
            dispenseFileWriter.close();
        }
        return dispenseFileDetails;
    }

    @Override
    public void validateMedicationRequestStatus(Dispense dispense) throws CustomParameterizedException {
        List<Long> dispensedMedicationLineIds = dispense.getDocument().getDispenseDocumentLines().stream().map(DispenseDocumentLine::getMedicationRequestDocLineId).collect(Collectors.toList());
        List<String> cancelledItems = new ArrayList<>();

        //load medication request document
        Optional<SourceDTO> medicationRequestSource = dispense.getDocument().getSourceDTOList().stream().filter(sourceDTO -> DocumentType.AMBULATORY.equals(sourceDTO.getDocumentType())).findFirst();
        MedicationRequest medicationRequest = null;
        if (medicationRequestSource.isPresent()) {
            String documentNumber = medicationRequestSource.get().getReferenceNumber();
            log.debug("Medication request source found with document  number ={}", documentNumber);
            //CriteriaQuery medicationRequestQuery = new CriteriaQuery(new Criteria("documentNumber").is(documentNumber));
            //medicationRequest = elasticsearchTemplate.queryForObject(medicationRequestQuery, MedicationRequest.class);
            //using repository method to get the object in lock mode
            medicationRequest = medicationRequestService.findByDocumentNumber(documentNumber);
            if (null != medicationRequest) {
                if(MedicationRequestStatus.DISPENSED.equals(medicationRequest.getDocument().getMedicationRequestStatus()))
                    throw new CustomParameterizedException("10427","Medication request is already dispensed");
                log.debug("Medication request object found with document number={}", medicationRequest.getDocumentNumber());
                List<MedicationRequestDocumentLine> cancelledLines = medicationRequest.getDocument().getDocumentLines().stream().
                    filter(medicationRequestDocumentLine -> MedicationRequestStatus.CANCELLED.name().equalsIgnoreCase(medicationRequestDocumentLine.getStatus())
                        || MedicationRequestStatus.CLOSED.name().equalsIgnoreCase(medicationRequestDocumentLine.getStatus())
                        || MedicationRequestStatus.PARTIALLY_CLOSED.name().equalsIgnoreCase(medicationRequestDocumentLine.getStatus())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(cancelledLines)) {
                    for (MedicationRequestDocumentLine line : cancelledLines) {
                        if (dispensedMedicationLineIds.contains(line.getDocumentLineId())) {
                            log.debug("Adding cancelled or closed medication document lines");
                            cancelledItems.add(line.getMedication().getCode());
                        }
                    }
                }
            }
        }
        //Map<Long, DispenseRequest> map = new HashMap<>();
        Map<Long,Float> map=new HashedMap();
        Map<Long, Float> keyMap = new HashedMap();
      //  Map<Long,Long> longLongMap=new HashedMap();
        //validate quantity
        for (MedicationRequestDocumentLine medicationRequestDocumentLine : medicationRequest.getDocument().getDocumentLines()) {
            List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();

          //  map.put(medicationRequestDocumentLine.getDocumentLineId(), medicationRequestDocumentLine.getDispenseRequest().getIssuedQuantity());

            if(map.containsKey(medicationRequestDocumentLine.getDocumentLineId()))
            {
                map.put(medicationRequestDocumentLine.getDocumentLineId(),map.get(medicationRequestDocumentLine.getDocumentLineId())+medicationRequestDocumentLine.getDispenseRequest().getIssuedQuantity());
            }
            else {
                map.put(medicationRequestDocumentLine.getDocumentLineId(), medicationRequestDocumentLine.getDispenseRequest().getIssuedQuantity());
            }


        }

        for (Map.Entry<Long,Float> entry:map.entrySet())
        {
            validateItemStock(dispense,entry);
        }

        if (CollectionUtils.isNotEmpty(cancelledItems)) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("items", cancelledItems);
            throw new CustomParameterizedException("10190", errorMap);
        }
    }

    @Override
    public void validateMandatoryFieldsForChargeRecord(Dispense dispense) {
        if(null == dispense.getDocument().getConsultant() || null == dispense.getDocument().getEncounter() || null == dispense.getDocument().getDepartment()
            || null == dispense.getDocument().getPatient() || null == dispense.getDocument().getOrderingHSC() || null == dispense.getDocument().getCreatedBy())
        {
            log.error("mandatory fields required to create charge record are missing for the current dispense.Patient mrn: {} ",dispense.getDocument().getPatient().getMrn());
            throw new CustomParameterizedException("One or more mandatory fields are missing [ consultant,encounter,department,patient,ordering hsc, ordering unit]");
        }
    }

    @Override
    public void costPricingInclusiveDiscountValidation(Dispense dispense) {
        List<String> pricingErrorItems = new ArrayList<>();
        populateConfigurationDetails(dispense);
        for (DispenseDocumentLine line : dispense.getDocument().getDispenseDocumentLines()) {
            addMarginBasedDiscountDetails(dispense.getDocument().getDiscountType(),dispense.getDocument(),line);
            doPricingValidation(dispense.getDocument(), line, pricingErrorItems);
        }
        if (CollectionUtils.isNotEmpty(pricingErrorItems)) {
            log.error("Selling price is less than cost price for items :{}", pricingErrorItems);
            List<ErrorMessage> errorMessages = new ArrayList<>();
            Map<String, Object> source = new HashMap<>();
            source.put("itemNames", String.join(", ", pricingErrorItems));
            errorMessages.add(new ErrorMessage("10226", source));
            throw new FieldValidationException(errorMessages, "Validation exception");
        }
    }

    private void addMarginBasedDiscountDetails(String discType, DispenseDocument document, DispenseDocumentLine line) {
        log.debug("addMarginBasedDiscountDetails discType:{}",discType);
        if ("MARGIN_BASED_DISCOUNT".equals(discType) || "Margin_Discount".equalsIgnoreCase(discType)) {
            populateConfigurationForMBD(document, "MARGIN_BASED_DISCOUNT");
            MarginDiscountDTO marginDiscountDTO = new MarginDiscountDTO();
            marginDiscountDTO.setDiscountFormula(document.getDiscountFormula());
            marginDiscountDTO.setDiscountSlab(document.getDiscountSlab());
            if (null == line.getItemPricingDTO()) {
                line.setItemPricingDTO(new ItemPricingDTO());
            }
            line.getItemPricingDTO().setMarginDiscountDTO(marginDiscountDTO);
        }
    }

    private void doPricingValidation(DispenseDocument document, DispenseDocumentLine line, List<String> pricingErrorItems) {

        if (null == line.getItemPricingDTO().getPricingMethod()) {
            line.getItemPricingDTO().setPricingMethod(PricingMethod.MRP);
        }
        switch (line.getItemPricingDTO().getPricingMethod().name()) {
            case "MRP":
                if (lt(line.getItemPricingDTO().getMrp(), line.getItemPricingDTO().getCostPrice()))
                    pricingErrorItems.add(line.getName());
                break;
            case "Fixed_Sale":
                if (lt(line.getItemPricingDTO().getFixedSellingPrice(), line.getItemPricingDTO().getCostPrice()))
                    pricingErrorItems.add(line.getName());
                break;
            case "Cost_Plus":
            case "Discount":
            case "Margin_Discount":
                updateItemCostFromStockSource(line);
                //Load MBD formula & slab when pricing method is "Margin_Discount" even discType in configuration is Flat_Discount
                addMarginBasedDiscountDetails(line.getItemPricingDTO().getPricingMethod().name(), document,line);
                BigDecimal discount = line.getItemPricingDTO().calculateERPDiscountAmount();
                if (lt(subtract(line.getItemPricingDTO().getMrp(), discount), line.getItemPricingDTO().getCostPrice())) {
                    pricingErrorItems.add(line.getName());
                }
                break;
        }
    }

    private void updateItemCostFromStockSource(DispenseDocumentLine line){
        log.debug("updateItemCostFromStockSource SKU:{}",line.getItemPricingDTO().getSku());
        if(isNull(line.getItemPricingDTO().getSku())) return;
        StockSource stockSource= stockSourceService.getStockSource(line.getItemPricingDTO().getSku());
        if(null != stockSource) {
            log.debug("updating line itemPricing details purchaseTax:{}, recoverableTax:{}, costPrice:{} from stockSource for ItemId:{}",
                stockSource.getTaxPerUnit(), stockSource.getRecoverableTax(), stockSource.getCostWithoutTax(), line.getItemId());
            line.getItemPricingDTO().setPurchaseTax(stockSource.getTaxPerUnit());
            line.getItemPricingDTO().setRecoverable(stockSource.getRecoverableTax());
            line.getItemPricingDTO().setCostPrice(stockSource.getCostWithoutTax());
        }
    }

    @Override
    @ServiceActivator(inputChannel = Channels.DISPENSE_RECORD_ENCOUNTER_UPDATE_INPUT)
    public void dispenseRecordEncounterUpdate(List<Dispense> dispenseList) {
        log.debug("In dispenseRecordEncounterUpdate method. Dispense List: {}", dispenseList);
        dispenseList.forEach(dispense -> {
            Dispense dispenseRecord = dispenseRepository.findReadOnlyOneByDocumentNumber(dispense.getDocumentNumber());
            dispenseRecord.getDocument().setEncounter(dispense.getDocument().getEncounter());
            dispenseRepository.updateLatest(dispenseRecord.getId());
            dispenseRecord.setVersion(dispenseRecord.getVersion() + 1);
            dispenseRecord.setLatest(true);
            dispenseRecord.getDocument().setModifiedDate(LocalDateTime.now());
            Dispense result = dispenseRepository.save(dispenseRecord);
            dispenseSearchRepository.save(result);
        });

        this.publishDispenseRecordsToChargeRecord(dispenseList);
    }

    @Override
    public void publishDispenseRecordsToChargeRecord(List<Dispense> dispenseList) {
        log.debug("In dispenseRecordEncounterUpdate method. Dispense List: {}", dispenseList);
        dispenseChargeRecordEncounterUpdateChannel.send(MessageBuilder.withPayload(dispenseList).build());
    }

    private Organization getOrganizationData(Long unitId) {
        Organization org;
        String cacheKey = "PHR: unit.id:"+unitId;
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            org = pharmacyRedisCacheService.fetchOrganization(elasticsearchTemplate,unitId,cacheKey);
        }else
        {
            org = getRecord("organization", "id:"+unitId, elasticsearchTemplate, Organization.class);
        }
        return org;
    }
}

