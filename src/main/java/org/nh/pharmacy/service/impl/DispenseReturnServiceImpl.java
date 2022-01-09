package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.drools.core.base.RuleNameStartsWithAgendaFilter;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.nh.billing.domain.*;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.Type;
import org.nh.billing.repository.search.InvoiceSearchRepository;
import org.nh.billing.service.*;
import org.nh.billing.util.DocumentUtil;
import org.nh.billing.web.rest.errors.ErrorConstants;
import org.nh.common.dto.DocumentAuditDTO;
import org.nh.common.dto.DocumentRecordDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.util.BigDecimalUtil;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PamIntegration;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.PricingMethod;
import org.nh.pharmacy.domain.enumeration.ReturnStatus;
import org.nh.pharmacy.repository.DispenseReturnRepository;
import org.nh.pharmacy.repository.search.DispenseReturnSearchRepository;
import org.nh.pharmacy.repository.search.DispenseSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.service.FreemarkerService;
import org.nh.pharmacy.util.CalculateTaxUtil;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.FreeMarkerUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.mapper.DispenseReturnToDocumentRecordMapper;
import org.nh.pharmacy.web.rest.mapper.DispenseToReturnMapper;
import org.nh.pharmacy.web.rest.mapper.ReturnToInvoiceMapper;
import org.nh.pharmacy.web.rest.mapper.ReturnToRefundMapper;
import org.nh.pharmacy.web.rest.util.DateUtil;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.nh.print.PdfGenerator;
import org.nh.security.dto.Preferences;
import org.nh.security.util.UserPreferencesUtils;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.common.util.BigDecimalUtil.*;
import static org.nh.pharmacy.domain.enumeration.Context.DispenseReturn_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.ReturnStatus.*;
import static org.nh.pharmacy.util.ElasticSearchUtil.*;

/**
 * Service Implementation for managing DispenseReturn.
 */
@Service("dispenseReturnService")
@Transactional
public class DispenseReturnServiceImpl implements DispenseReturnService {

    private final Logger log = LoggerFactory.getLogger(DispenseReturnServiceImpl.class);

    private final DispenseReturnRepository dispenseReturnRepository;

    private final DispenseReturnSearchRepository dispenseReturnSearchRepository;

    private final DispenseToReturnMapper dispenseToReturnMapper;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final ReturnToInvoiceMapper returnToInvoiceMapper;

    private final ReturnToRefundMapper returnToRefundMapper;

    private final RefundService refundService;

    private final InvoiceService invoiceService;

    private final ReceiptService receiptService;

    private final WorkflowService workflowService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ApplicationProperties applicationProperties;

    private final KieBase kieBase;

    private final StockService stockService;

    private final DispenseSearchRepository dispenseSearchRepository;

    private final BillingService billingService;

    private final FreemarkerService freemarkerService;

    private final GroupService groupService;

    private final LocatorService locatorService;

    private final InvoiceSearchRepository invoiceSearchRepository;

    private final InvoiceReceiptService invoiceReceiptService;

    private final SponsorInvoiceService sponsorInvoiceService;

    private final RuleExecutorService ruleExecutorService;

    private final DocumentRecordService documentRecordService;

    private final DispenseReturnToDocumentRecordMapper dispenseReturnToDocumentRecordMapper;

    private final MessageChannel documentAuditChannel;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    private final ItemStoreLocatorMapService itemStoreLocatorMapService;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    private final String moduleName;

    public DispenseReturnServiceImpl(DispenseReturnRepository dispenseReturnRepository, DispenseReturnSearchRepository dispenseReturnSearchRepository,
                                     DispenseToReturnMapper dispenseToReturnMapper, SequenceGeneratorService sequenceGeneratorService, ReturnToRefundMapper returnToRefundMapper,
                                     ReturnToInvoiceMapper returnToInvoiceMapper, InvoiceService invoiceService, ReceiptService receiptService,
                                     RefundService refundService, WorkflowService workflowService, ElasticsearchOperations elasticsearchTemplate, ApplicationProperties applicationProperties, KieBase kieBase,
                                     StockService stockService, DispenseSearchRepository dispenseSearchRepository, BillingService billingService,
                                     FreemarkerService freemarkerService, GroupService groupService, LocatorService locatorService, InvoiceReceiptService invoiceReceiptService, SponsorInvoiceService sponsorInvoiceService, InvoiceSearchRepository invoiceSearchRepository, RuleExecutorService ruleExecutorService,
                                     DocumentRecordService documentRecordService, DispenseReturnToDocumentRecordMapper dispenseReturnToDocumentRecordMapper, @Qualifier(Channels.DMS_DOCUMENT_AUDIT) MessageChannel documentAuditChannel, PharmacyRedisCacheService pharmacyRedisCacheService, ItemStoreLocatorMapService itemStoreLocatorMapService, @Qualifier("moduleName") String moduleName) {

        this.dispenseReturnRepository = dispenseReturnRepository;
        this.dispenseReturnSearchRepository = dispenseReturnSearchRepository;
        this.dispenseToReturnMapper = dispenseToReturnMapper;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.returnToInvoiceMapper = returnToInvoiceMapper;
        this.returnToRefundMapper = returnToRefundMapper;
        this.invoiceService = invoiceService;
        this.receiptService = receiptService;
        this.refundService = refundService;
        this.workflowService = workflowService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.applicationProperties = applicationProperties;
        this.kieBase = kieBase;
        this.stockService = stockService;
        this.dispenseSearchRepository = dispenseSearchRepository;
        this.billingService = billingService;
        this.freemarkerService = freemarkerService;
        this.groupService = groupService;
        this.locatorService = locatorService;
        this.invoiceReceiptService = invoiceReceiptService;
        this.sponsorInvoiceService = sponsorInvoiceService;
        this.invoiceSearchRepository = invoiceSearchRepository;
        this.ruleExecutorService = ruleExecutorService;
        this.documentRecordService = documentRecordService;
        this.dispenseReturnToDocumentRecordMapper = dispenseReturnToDocumentRecordMapper;
        this.documentAuditChannel = documentAuditChannel;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.itemStoreLocatorMapService = itemStoreLocatorMapService;
        this.moduleName = moduleName;
    }

    /**
     * Save a dispenseReturn.
     *
     * @param dispenseReturn the entity to save
     * @return the persisted entity
     */
    @Override
    public DispenseReturn save(DispenseReturn dispenseReturn) {
        log.debug("Request to save DispenseReturn : {}", dispenseReturn);

        //Generate Sequence Number for Dispensereturn
        if (dispenseReturn.getId() == null) {
            dispenseReturn.setId(dispenseReturnRepository.getId());
            dispenseReturn.setVersion(0);
            if (DRAFT.equals(dispenseReturn.getDocument().getReturnStatus())) {
                dispenseReturn.setDocumentNumber("DRAFT-" + dispenseReturn.getId());
            }
        } else {
            dispenseReturnRepository.updateLatest(dispenseReturn.getId());
            dispenseReturn.setVersion(dispenseReturn.getVersion() + 1);
        }
        dispenseReturn.getDocument().setModifiedDate(LocalDateTime.now());
        dispenseReturn.setLatest(true);
        dispenseReturn.getDocument().getDispenseReturnDocumentLines().forEach(dispenseReturnDocumentLine -> {
            if (dispenseReturnDocumentLine.getLineNumber() == null)
                dispenseReturnDocumentLine.setLineNumber(dispenseReturnRepository.getId());
        });
        if(Boolean.FALSE.equals(dispenseReturn.getDocument().isIpDispense()))
            saveValidation(dispenseReturn);
        DispenseReturn result = dispenseReturnRepository.saveAndFlush(dispenseReturn);
        dispenseReturnSearchRepository.save(result);

        return result;
    }

    @PamIntegration
    @Override
    @PublishStockTransaction
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processDispenseReturn(DispenseReturn dispenseReturn, String action) throws Exception {

        log.debug("Request to save dispenseReturn based on Action : {}", action);
        Map<String, Object> documentMap = new HashMap<String, Object>();
        DispenseReturn result;
        if (action == null) {
            action = "DRAFT";
        }
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval dispenseReturn : {}", dispenseReturn);
                dispenseReturn.getDocument().setReturnDate(LocalDateTime.now());
                if (!isGroupExist(dispenseReturn))
                    throw new CustomParameterizedException("10114", "Approval Group doesn't exit  for logged in unit");
                validateDocument(dispenseReturn);
                checkPendingForReturnDocument(dispenseReturn);
                result = sendForApproval(dispenseReturn);
                break;
            case "APPROVED":
                log.debug("Request to approve dispenseReturn : {}", dispenseReturn);
                dispenseReturn.getDocument().setReturnStatus(APPROVED);
                result = save(dispenseReturn);
                break;
            case "REJECTED":
                log.debug("Request to reject dispenseReturn : {}", dispenseReturn);
                dispenseReturn.getDocument().setReturnStatus(REJECTED);
                result = save(dispenseReturn);
                break;
            case "CONFIRMED":
                log.debug("Request to confirm dispenseReturn : {}", dispenseReturn);
                try {
                    documentMap.put("dispenseReturn", dispenseReturn);
                    DispenseReturn exitingReturn = this.findOne(dispenseReturn.getId());
                    if (RETURNED.equals(exitingReturn.getDocument().getReturnStatus())) {
                        throw new CustomParameterizedException("10098", "Return Document (" + dispenseReturn.getDocumentNumber() + ") is already processed");
                    }
                    List<SourceDTO>sourceDtoList=new ArrayList<>();
                    sourceDtoList.add(exitingReturn.getDocument().getInvoiceRef());
                    dispenseReturn.getDocument().setReturnDate(LocalDateTime.now());
                    dispenseReturn.getDocument().setReturnStatus(RETURNED);
                    Invoice invoice = returnToInvoiceMapper.convertDispenseReturnToInvoice(dispenseReturn);
                    InvoiceDocumentDTO partOf = new InvoiceDocumentDTO();
                    partOf.setInvoiceNumber(dispenseReturn.getDocument().getInvoiceRef().getReferenceNumber());
                    org.nh.common.dto.HealthcareServiceCenterDTO invoiceHsr = new org.nh.common.dto.HealthcareServiceCenterDTO();
                    invoiceHsr.setId(dispenseReturn.getDocument().getHsc().getId());
                    invoiceHsr.setName(dispenseReturn.getDocument().getHsc().getName());
                    invoiceHsr.setCode(dispenseReturn.getDocument().getHsc().getCode());
                    partOf.setHsc(invoiceHsr);
                    invoice.getInvoiceDocument().setPartOf(partOf);
                    SourceDTO invoiceSource = new SourceDTO();
                    invoiceSource.setId(dispenseReturn.getId());
                    invoiceSource.setReferenceNumber(dispenseReturn.getDocumentNumber());
                    invoiceSource.setDocumentType(DocumentType.DISPENSE_RETURN);
                    invoice.getInvoiceDocument().setSource(invoiceSource);
                    invoice.getInvoiceDocument().setSourceDTOList(sourceDtoList);
                    invoice = invoiceService.createInvoiceDispense(invoice, "CONFIRMED");
                    SourceDTO cancelInvoiceRef = new SourceDTO();
                    cancelInvoiceRef.setId(invoice.getId());
                    cancelInvoiceRef.setReferenceNumber(invoice.getInvoiceNumber());
                    cancelInvoiceRef.setDocumentType(DocumentType.CANCELLED_INVOICE);
                    dispenseReturn.getDocument().setCancelledInvoiceRef(cancelInvoiceRef);
                    dispenseReturn.getDocument().setEncounter(invoice.getInvoiceDocument().getEncounter());
                    documentMap.put("invoice", invoice);
                    dispenseReturn.getDocument().setSource(cancelInvoiceRef);
                    List<SponsorInvoiceDTO> sponsorInvoices = null;
                    if (gtZero(dispenseReturn.getDocument().getSponsorDiscount()) || gtZero(dispenseReturn.getDocument().getSponsorNetAmount())) {
                        sponsorInvoices = billingService.createSponsorInvoiceForDispenseReturn(dispenseReturn);
                    }
                    documentMap.put("sponsorInvoices", sponsorInvoices);
                    Receipt receipt = new Receipt();
                    org.nh.common.dto.HealthcareServiceCenterDTO hscRecord = new org.nh.common.dto.HealthcareServiceCenterDTO();
                    hscRecord.setId(dispenseReturn.getDocument().getHsc().getId());
                    hscRecord.setName(dispenseReturn.getDocument().getHsc().getName());
                    hscRecord.setCode(dispenseReturn.getDocument().getHsc().getCode());
                    receipt.setHsc(hscRecord);
                    receipt.setReceivedBy(Long.valueOf(String.valueOf(dispenseReturn.getDocument().getReceivedBy().getId())));
                    receipt.setEncounter(dispenseReturn.getDocument().getEncounter().convertLiteDTO());
                    receipt.setReceivedByUser(dispenseReturn.getDocument().getReceivedBy());
                    List<PaymentDetail> paymentDetails = dispenseReturn.getDocument().getPaymentDetails();
                    if (paymentDetails != null && !paymentDetails.isEmpty()) {
                        PaymentDetail paymentDetail = paymentDetails.get(0);
                        receipt.setTransactionCurrency(paymentDetail.getBaseCurrency());
                        receipt.setBaseCurrency(paymentDetail.getBaseCurrency());
                    }
                    boolean refundRequired = dispenseReturn.getDocument().getRefundRequired();
                    if (gtZero(dispenseReturn.getDocument().getPatientNetAmount())) {
                        receipt = receiptService.createReceiptForReturn(invoice, receipt, refundRequired);
                    }
                   // documentMap.put("receipt", receipt);
                    if (refundRequired) {
                        Refund refund = returnToRefundMapper.convertDispenseReturnToRefund(dispenseReturn);
                        if (gtZero(dispenseReturn.getDocument().getPatientNetAmount())) {
                            refund = refundService.processRefund(refund, receipt);
                        }
                        documentMap.put("refund", refund);
                    }
                    boolean isDraft = dispenseReturn.getDocument().getDraft();
                    if (isDraft) {
                        dispenseReturn.documentNumber(sequenceGeneratorService.generateSequence("Dispense_Return", "NH", dispenseReturn));
                        result = save(dispenseReturn);
                    }

                    result = save(dispenseReturn);
                    stockReversal(result);
                    publishDispenseReturnDocument(result, applicationProperties.getAthmaBucket().getDocBasePath());
                    //Save html file to docbasepath for Patient Records in DMS
                    getReturnHTMLByReturnId(result.getId(), result.getDocumentNumber());
                    documentMap.put("dispenseReturn", result);
                } catch (Exception exception) {
                    log.error("Exception occurred, So Re-indexing Return process. Ex= {} ", exception);

                    reIndexBilling(documentMap);
                    throw exception;
                }
                break;
            default:
                if (dispenseReturn.getDocument().getReturnStatus() == null) {
                    dispenseReturn.getDocument().setReturnStatus(DRAFT);
                }
                result = save(dispenseReturn);
        }

        documentMap.put("dispenseReturn", result);
        return documentMap;
    }





    /**
     * Get all the dispenseReturns.d
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DispenseReturn> findAll(Pageable pageable) {
        log.debug("Request to get all DispenseReturns");
        Page<DispenseReturn> result = dispenseReturnRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one dispenseReturn by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public DispenseReturn findOne(Long id) {
        log.debug("Request to get DispenseReturn : {}", id);
        DispenseReturn dispenseReturn = dispenseReturnRepository.findOne(id);
        return dispenseReturn;
    }

    /**
     * Get one dispenseReturn by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public DispenseReturn findDetachedOne(Long id) {
        log.debug("Request to get DispenseReturn : {}", id);
        return dispenseReturnRepository.findOne(id);
    }

    /**
     * Delete the  dispenseReturn by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete DispenseReturn : {}", id);
        DispenseReturn dispenseReturn = dispenseReturnSearchRepository.findById(id).get();
        deleteValidation(dispenseReturn);
        dispenseReturnRepository.delete(id);
        dispenseReturnSearchRepository.deleteById(id);
    }

    /**
     * Search for the dispenseReturn corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DispenseReturn> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of DispenseReturns for query {}", query);
        Page<DispenseReturn> result = dispenseReturnSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.patient.displayName").field("document.patient.mrn").field("document.returnhsc.name")
            .field("document.createdBy.displayName").field("document.receivedBy.displayName").field("document.patientNetAmount")
            .field("document.returnStatus").field("document.cancelledInvoiceRef.referenceNumber").field("document.returnRequestNumber")
            .field("document.patientLocation").field("document.hsc.name")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DispenseReturn> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of DispenseReturns for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.patient.displayName").field("document.patient.mrn").field("document.returnhsc.name")
                .field("document.createdBy.displayName").field("document.receivedBy.displayName")
                .field("document.patientNetAmount").field("document.returnStatus").field("document.cancelledInvoiceRef.referenceNumber")
                .field("document.returnRequestNumber").field("document.patientLocation").field("document.hsc.name")
                .defaultOperator(Operator.AND))
            .withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return dispenseReturnSearchRepository.search(searchQuery);
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

    @Override
    public byte[] getReturnPdfByReturnId(Long dispenseReturnId, String dispenseReturnNumber) throws Exception {

        DispenseReturn dispenseReturn = getDispenseReturn(dispenseReturnId, dispenseReturnNumber);
        String fileName = dispenseReturn.getDocumentNumber();
        String docPath = DocumentUtil.getPath(applicationProperties.getAthmaBucket().getDocBasePath(), dispenseReturn.getDocument().getPatient().getMrn(), "ADMINISTRATIVE", "DISPENSE_RETURN");
        String fileAbsolutePath = docPath.concat(fileName + ".html");
        File file = new File(fileAbsolutePath);
        Query query = new NativeSearchQueryBuilder()
            .withQuery(new QueryStringQueryBuilder(
                new StringBuilder("documentNumber:\"")
                    .append(dispenseReturn.getDocumentNumber())
                    .append("\" ")
                    .append("documentType:\"").append("DISPENSE_RETURN")
                    .append("\" ")
                    .append("auditType:").append("PRINT").toString()).defaultOperator(Operator.AND))
            .build();
        long count = elasticsearchTemplate.count(query, IndexCoordinates.of(DocumentAuditDTO.DOCUMENT_NAME));
        byte[] contentInBytes = null;
        if (file.exists()) {
            UserDTO user = loadLoggedInUser();
            String generatedBy = user.getDisplayName() + ", " + user.getEmployeeNo();
            String generateOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
            byte[] pdfInBytes = PdfGenerator.createPdf(file, generatedBy, generateOn);
            if (count > 0) {
            contentInBytes = PdfGenerator.addWaterMarkToPdf(pdfInBytes, "DUPLICATE");
            } else {
                contentInBytes = pdfInBytes;
            }
        } else {
            Map<String, Object> outData = this.getReturnHTMLByReturnId(dispenseReturnId, dispenseReturnNumber);
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
            dispenseReturn.getDocumentNumber(), "DISPENSE_RETURN",
            Instant.now(), userDTO)).build());
        return contentInBytes;
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

    @Override
    public Map<String, Object> getReturnHTMLByReturnId(Long dispenseReturnId, String dispenseReturnNumber) throws Exception {
        log.debug("dispenseReturnId: {}, dispenseReturnNumber: {}", dispenseReturnId, dispenseReturnNumber);

        FileOutputStream fop = null;
        File file;
        Map<String, Object> printFile = new HashMap<>();
        String templateFilePath = "dispense-return.ftl"; // Fixed template
        DispenseReturn dispenseReturn = getDispenseReturn(dispenseReturnId, dispenseReturnNumber);
        if (dispenseReturn.getDocument().getCancelledInvoiceRef().getReferenceNumber() == null) {
            return null; //For all IP returns there will not be cancelled invoice reference.
        }

        String fileName = dispenseReturn.getDocumentNumber();
        printFile.put("fileName", fileName);

        Map<String, Object> returnData = populateReturnData(dispenseReturn);

        String html = freemarkerService.mergeTemplateIntoString(templateFilePath, returnData);
        printFile.put("html", html);
        try {
            String docPath = DocumentUtil.getPath(applicationProperties.getAthmaBucket().getDocBasePath(), dispenseReturn.getDocument().getPatient().getMrn(), "ADMINISTRATIVE", "DISPENSE_RETURN");
            file = new File(docPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(docPath.concat(fileName + ".html"));
            if (!file.exists()) {
                file.createNewFile();
            }
            fop = new FileOutputStream(file);
            byte[] contentInBytes = html.getBytes();
            printFile.put("content", contentInBytes);
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
        return printFile;
    }


    private Map<String, Object> populateReturnData(DispenseReturn dispenseReturn) {


        DispenseReturnDocument returnDocument = dispenseReturn.getDocument();
        UserDTO user = loadLoggedInUser();
        Organization org = getOrganizationData(dispenseReturn.getDocument().getHsc().getPartOf().getId());
        String invoiceNumber = returnDocument.getCancelledInvoiceRef() != null ? returnDocument.getCancelledInvoiceRef().getReferenceNumber() : null;
        Refund refund = queryForObject(moduleName+"_refund", new CriteriaQuery(new Criteria("refundDocument.refundItems.receiptDetail.receiptNumber.raw").is(invoiceNumber)), elasticsearchTemplate, Refund.class);
        Map<String, List<String>> manufacturerAndSchedule = getManufacturerAndSchedule(returnDocument.getDispenseReturnDocumentLines());
        org.nh.pharmacy.domain.HealthcareServiceCenter hsc = queryForObject("healthcareservicecenter", new CriteriaQuery(new Criteria("id").is(returnDocument.getReturnhsc().getId())),elasticsearchTemplate,  org.nh.pharmacy.domain.HealthcareServiceCenter.class);

        CriteriaQuery sponserInvoiceQuery = new CriteriaQuery(new Criteria("sponsorDocument.source.referenceNumber.raw").is(returnDocument.getSource().getReferenceNumber()));
        List<SponsorInvoice> sponsorInvoices = queryForList(moduleName+"_sponsorinvoice", sponserInvoiceQuery, elasticsearchTemplate, SponsorInvoice.class);

        Map<String, Object> returnData = new HashMap<>();
        returnData.put("patientName", returnDocument.getPatient().getFullName());
        returnData.put("patientMrn", returnDocument.getPatient().getMrn() != null ? returnDocument.getPatient().getMrn() : returnDocument.getPatient().getTempNumber());
        returnData.put("patientPhoneNo", returnDocument.getPatient().getMobileNumber());
        returnData.put("consultantName", returnDocument.getConsultant().getDisplayName());
        returnData.put("invoiceNo", returnDocument.getCancelledInvoiceRef() != null ? returnDocument.getCancelledInvoiceRef().getReferenceNumber() : null);
        returnData.put("refundNo", refund != null ? refund.getRefundNumber() : "-");
        returnData.put("refundType", refund != null ? refund.getRefundDocument().getRefundType() : "-");
        returnData.put("refundAmount", refund != null ? refund.getRefundDocument().getRefundAmount() : Integer.valueOf(0));
        returnData.put("date", returnDocument.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
        //invoiceData.put("visitDate",);
        returnData.put("dispensePlans", returnDocument.getDispensePlans());
        returnData.put("dispenseItems", returnDocument.getDispenseReturnDocumentLines());
        String preparedBy = returnDocument.getCreatedBy() != null ? returnDocument.getCreatedBy().getDisplayName() + ", " + returnDocument.getCreatedBy().getEmployeeNo() : "-";
        returnData.put("preparedBy", preparedBy);
        returnData.put("qualifiedPharmacist", hsc.getContacts());
        returnData.put("generatedBy", user.getDisplayName());
        returnData.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));

        returnData.put("total", add(returnDocument.getNetAmount(), returnDocument.getDiscountAmount()));
        returnData.put("invoiceTaxes", returnDocument.getDispenseTaxes());
        returnData.put("patientDiscount", returnDocument.getPatientDiscount());
        returnData.put("taxDiscount", returnDocument.getTaxDiscount());
        returnData.put("sponsorDiscount", returnDocument.getSponsorDiscount());
        returnData.put("totalSponsorAmount", returnDocument.getSponsorNetAmount());
        returnData.put("returnAmount", returnDocument.getPatientNetAmount());
        returnData.put("netAmount", returnDocument.getNetAmount());
        returnData.put("totalDiscount", returnDocument.getDiscountAmount());
        returnData.put("roundOff", returnDocument.getRoundOff());

        returnData.put("unitDisplayName", dispenseReturn.getDocument().getHsc().getPartOf().getName());
        returnData.put("hscDisplayName", dispenseReturn.getDocument().getHsc().getName());
        returnData.put("unitAddress", org.getAddresses().stream().filter(stringObjectMap -> ((Map) stringObjectMap.get("use")).get("code").toString()
            .equalsIgnoreCase("work")).findAny().orElse(null));
        returnData.put("manufacutererAndSchedule", manufacturerAndSchedule);
        returnData.put("visitNumber", dispenseReturn.getDocument().getEncounter().getVisitNumber());
        //invoiceData.put("signature",);
        returnData.put("dlNo", hsc.getLicenseNumber() != null ? hsc.getLicenseNumber() : "-");
        returnData.put("convertDecimal", new FreeMarkerUtil());
        returnData.put("sponsorInvoices", sponsorInvoices);
        Boolean gstinFound = false;
        if (org.getIdentifier() != null && !org.getIdentifier().isEmpty()) {
            Iterator<OrganizationIdentifier> organizationIdentifierIterator = org.getIdentifier().listIterator();
            while (organizationIdentifierIterator.hasNext()) {
                OrganizationIdentifier organizationIdentifier = organizationIdentifierIterator.next();
                if (organizationIdentifier.getType().equals("GSTIN")) {
                    returnData.put("gstin", organizationIdentifier.getValue());
                    gstinFound = true;
                }
            }
        }
        if (!gstinFound) {
            returnData.put("gstin", "-");
        }
        return returnData;
    }

    private Map<String, List<String>> getManufacturerAndSchedule(List<DispenseReturnDocumentLine> returnItems) {
        List<String> codes = new ArrayList<>();
        returnItems.stream().forEach(returnItem -> codes.add(QueryParser.escape(returnItem.getCode())));
        Map<String, List<String>> itemManufacturerMap = new HashMap<>();
        //CriteriaQuery query = new CriteriaQuery(new Criteria("code.raw").in(codes), PageRequest.of(0, 9999));
        Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("code.raw:("+ StringUtils.join(codes, " OR ")+")")).withPageable(PageRequest.of(0, 10000)).build();
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


    /**
     * Search for the dispense return to get status count corresponding to the query.
     *
     * @param query the query of the search
     * @return the status count
     */

    //.withIndices("dispensereturn").withTypes("dispensereturn")
    @Override
    public Map<String, Long> getStatusCount(String query) {
        log.debug("Request to get Count of DispenseReturns Return based on Status");
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder().withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.patient.displayName").field("document.patient.mrn").field("document.hsc.name")
                .field("document.createdBy.displayName").field("document.patientNetAmount").field("document.returnStatus").field("document.cancelledInvoiceRef.referenceNumber")
                .defaultOperator(Operator.AND))
            .addAggregation(AggregationBuilders.terms("status_count").field("document.returnStatus.raw"))
            .build();
        Aggregations aggregations = getAggregations(searchQuery, elasticsearchTemplate, "dispensereturn");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Dispense Return");
        dispenseReturnSearchRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on DispenseReturn latest=true");
        List<DispenseReturn> data = dispenseReturnRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            dispenseReturnSearchRepository.saveAll(data);
        }
        dispenseReturnSearchRepository.refresh();
    }

    /**
     * Export DispenseReturn corresponding to the query.
     *
     * @param query
     * @param pageable
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, String> exportDispenseReturn(String query, Pageable pageable) throws IOException {
        log.debug("Request to export DispenseReturns in CSV File query {}", query);

        File file = ExportUtil.getCSVExportFile("dispense_return", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter dispenseReturnWriter = new FileWriter(file);
        Map<String, String> returnFileDetails = new HashMap<>();
        returnFileDetails.put("fileName", file.getName());
        returnFileDetails.put("pathReference", "masterExport");

        //Header for dispense csv file
        final String[] dispenseFileHeader = {"Return No", "Date and Time", "Patient Name", "MRN", "Store", "Return By", "Cancelled Invoice No", "Return Amount", "Status"};

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);

        String dateFormat = null;

        try (CSVPrinter csvFilePrinter = new CSVPrinter(dispenseReturnWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(dispenseFileHeader);

            Iterator<DispenseReturn> dispenseReturnIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();
            while (dispenseReturnIterator.hasNext()) {
                DispenseReturn dispenseReturn = dispenseReturnIterator.next();

                if (dateFormat == null)
                    dateFormat = ConfigurationUtil.getConfigurationData("athma_date_format", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);

                List dispenseReturnRow = new ArrayList();
                dispenseReturnRow.add(dispenseReturn.getDocumentNumber());
                dispenseReturnRow.add(DateUtil.getFormattedDateAsFunctionForCSVExport(dispenseReturn.getDocument().getModifiedDate(), dateFormat));
                dispenseReturnRow.add(dispenseReturn.getDocument().getPatient().getFullName());
                if (dispenseReturn.getDocument().getPatient().getMrn() == null) {
                    dispenseReturnRow.add(dispenseReturn.getDocument().getPatient().getMrn());
                } else {
                    dispenseReturnRow.add(new StringBuilder("=\"").append(dispenseReturn.getDocument().getPatient().getMrn()).append("\"").toString());
                }
                dispenseReturnRow.add(dispenseReturn.getDocument().getReturnhsc() != null ? dispenseReturn.getDocument().getReturnhsc().getName() : null);
                dispenseReturnRow.add(dispenseReturn.getDocument().getReceivedBy() != null ? dispenseReturn.getDocument().getReceivedBy().getDisplayName() : dispenseReturn.getDocument().getReceivedBy().getId());
                dispenseReturnRow.add(dispenseReturn.getDocument().getCancelledInvoiceRef() != null ? dispenseReturn.getDocument().getCancelledInvoiceRef().getReferenceNumber() : null);
                dispenseReturnRow.add(dispenseReturn.getDocument().getPatientNetAmount() != null ? dispenseReturn.getDocument().getPatientNetAmount() : BigDecimalUtil.ZERO);
                dispenseReturnRow.add(dispenseReturn.getDocument().getReturnStatus());
                csvFilePrinter.printRecord(dispenseReturnRow);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (dispenseReturnWriter != null)
                dispenseReturnWriter.close();
        }

        return returnFileDetails;
    }

    /**
     * Send for approval
     *
     * @param dispenseReturn
     * @return dispenseReturn object
     * @throws Exception
     */
    public DispenseReturn sendForApproval(DispenseReturn dispenseReturn) throws Exception {
        dispenseReturn.getDocument().setReturnStatus(PENDING_APPROVAL);
        DispenseReturn result;
        if (dispenseReturn.getDocument().getDraft()) {
            dispenseReturn.getDocument().setDraft(Boolean.FALSE);
            dispenseReturn.documentNumber(sequenceGeneratorService.generateSequence("Dispense_Return", "NH", dispenseReturn));
            result = save(dispenseReturn);
        } else {
            result = save(dispenseReturn);
        }
        Map<String, Object> configurations = retrieveWorkflowConfigurations(dispenseReturn, true);
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(result, configurations);
        }
        return result;
    }

    /**
     * Start workflow
     *
     * @param dispenseReturn
     * @param configurations
     */
    public void startWorkflow(DispenseReturn dispenseReturn, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set Content
            content.put("document_id", dispenseReturn.getId());
            content.put("document_type", dispenseReturn.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set Params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_id", dispenseReturn.getId());
            params.put("document_number", dispenseReturn.getDocumentNumber());
            params.put("patient_name", (dispenseReturn.getDocument().getPatient().getDisplayName()==null || dispenseReturn.getDocument().getPatient().getDisplayName().isEmpty() )? (dispenseReturn.getDocument().getPatient().getFullName()==null || dispenseReturn.getDocument().getPatient().getFullName().isEmpty())?"":dispenseReturn.getDocument().getPatient().getFullName() : dispenseReturn.getDocument().getPatient().getDisplayName());
            params.put("mrn", dispenseReturn.getDocument().getPatient().getMrn());
            params.put("return_date", dispenseReturn.getDocument().getReturnDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("unit_id", String.valueOf(dispenseReturn.getDocument().getReturnUnit() != null ? dispenseReturn.getDocument().getReturnUnit().getId() : null));
            params.put("content", content);
            //Start the process
            Long processInstanceId = workflowService.startProcess(deployedUnit, (String) configurations.get("processId"), params);
            //Complete the document creation task
            workflowService.completeUserTaskForProcessInstance(processInstanceId, userId, results);
        }
    }

    /**
     * Execute workflow
     *
     * @param dispenseReturn the entity to save
     * @param transition     to be performed
     * @param taskId         task Id
     * @return result map
     * @throws Exception
     */
    @Override
    @Transactional
    @PamIntegration
    @PublishStockTransaction
    public Map<String, Object> executeWorkflow(DispenseReturn dispenseReturn, String transition, Long taskId) throws Exception {
        Map<String, Object> result = Collections.emptyMap();
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Approved":
                validateDocumentApprover(dispenseReturn);
                action = "APPROVED";
                break;
            case "Rejected":
                action = "REJECTED";
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }
        try {
            result = processDispenseReturn(dispenseReturn, action);
            //Complete the task
            Map<String, Object> results = new HashMap<>();
            results.put("transition_out", transition);
            workflowService.completeUserTask(taskId, userId, results);
        } catch (Exception exception) {
            reIndexBilling(result);
            throw exception;
        }
        return result;
    }

    public void validateDocumentApprover(DispenseReturn dispenseReturn) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(dispenseReturn, "dispense_return_document_approver_validation");
    }

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     */
    @Override
    public Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId) {
        Map<String, Object> configurations, taskDetails;
        DispenseReturn dispenseReturn = dispenseReturnRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(dispenseReturn, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            String createdBy = dispenseReturn.getDocument().getCreatedBy() != null ? dispenseReturn.getDocument().getCreatedBy().getLogin() : null;
//            taskDetails = workflowService.getTaskConstraints(taskId, processId, "document_number", documentNumber, userId, createdBy);
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,dispenseReturn.getDocument().getCreatedBy().getLogin());
            if ((Boolean) taskDetails.get("isGroupTask")) {
                List<String> workflowGroupIdList = (List<String>) taskDetails.get("groupIdList");
                List<String> userGroupIdList = groupService.groupsForUser(userId);
                if (disjoint(workflowGroupIdList, userGroupIdList)) {
                    taskDetails.put("taskId", null);
                    taskDetails.put("isGroupTask", false);
                }
            }
            taskDetails.remove("groupIdList");
        } else {
            taskDetails = new HashMap<String, Object>() {{
                put("taskId", null);
                put("constraints", new HashSet<String>());
                put("isGroupTask", false);
            }};
        }
        return taskDetails;
    }

    /**
     * Get workflow configurations
     *
     * @param dispenseReturn
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(DispenseReturn dispenseReturn, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_return_enable_workflow", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_return_workflow_definition", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(DispenseReturn_Approval_Committee, dispenseReturn.getDocument().getReturnUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    public DispenseReturnDocumentLine taxCalculationForLine(DispenseReturnDocumentLine returnDocumentLine, String unitCode) {
        log.debug("Request for tax Calculation of Return Line : {} ", returnDocumentLine);
        BigDecimal discountAmount = returnDocumentLine.getDiscountAmount() != null ? returnDocumentLine.getDiscountAmount() : BigDecimalUtil.ZERO;
        BigDecimal sponsorAmount = returnDocumentLine.getSponsorAmount() != null ? returnDocumentLine.getSponsorAmount() : BigDecimalUtil.ZERO;
        BigDecimal grossAmount = multiply(returnDocumentLine.getQuantity(), returnDocumentLine.getMrp());
        returnDocumentLine.setGrossAmount(grossAmount);
        returnDocumentLine.setCalculatedGrossAmount(grossAmount);
        returnDocumentLine.setNetAmount(subtract(grossAmount, discountAmount));
        returnDocumentLine.setPatientAmount(subtract(returnDocumentLine.getNetAmount(), sponsorAmount));
        calculateTaxDetailsByTaxLine(returnDocumentLine);
        log.debug("Tax Calculation For dispense return line : {} ", returnDocumentLine);
        return returnDocumentLine;
    }

    private void calculateTaxDetailsByTaxLine(DispenseReturnDocumentLine returnDocumentLine) {
        log.debug("Request to calculateTaxDetailsByTaxLine");
        List<DispenseTax> dispenseTaxList = returnDocumentLine.getItemTaxes();
        if (dispenseTaxList != null && !dispenseTaxList.isEmpty()) {
            dispenseTaxList.forEach(dispenseTax -> {
                BigDecimal taxAmount = calculateTax(dispenseTax.getTaxDefinition(), returnDocumentLine.getNetAmount());
                dispenseTax.setTaxAmount(taxAmount);
                returnDocumentLine.setTaxAmount(add(returnDocumentLine.getTaxAmount(), taxAmount));
            });
        }
    }

    private BigDecimal calculateTax(TaxDefinition taxDefinition, BigDecimal mrp) {
        log.debug("Request to calculateTax- mrp:{} , taxDefinition:{}", mrp, taxDefinition);
        TaxCalculation taxCalculation = taxDefinition.getTaxCalculation();
        Type taxType = taxCalculation.getType();
        BigDecimal taxAmount = BigDecimalUtil.ZERO;
        if (Type.Percentage.equals(taxType)) {
            Float percentage = taxCalculation.getPercentage();
            taxAmount = CalculateTaxUtil.calculateTax(mrp, percentage);
        }

        return taxAmount;
    }


    @Override
    public DispenseReturn taxCalculationForReturn(DispenseReturn dispenseReturn) {
        log.debug("Request to taxCalculationForReturn- dispenseReturn:{}", dispenseReturn);
        dispenseReturn.getDocument().getDispenseReturnDocumentLines().forEach(returnDocumentLine -> {
            this.taxCalculationForLine(returnDocumentLine, dispenseReturn.getDocument().getReturnUnit().getCode().toString());
        });

        return dispenseReturn;
    }

    /**
     * @param taxDefinition
     * @return
     */
    private float calculateTotalTaxInPercentForLine(TaxDefinition taxDefinition) {
        TaxCalculation taxCalculation = taxDefinition.getTaxCalculation();
        if (Type.Percentage.equals(taxCalculation.getType())) {
            return taxCalculation.getPercentage();
        }
        return 0f;
    }

    private void validateDispense(DispenseReturn dispense) {
        KieSession kSession = kieBase.newKieSession();
        try {
            kSession.insert(dispense);
            kSession.fireAllRules(new RuleNameStartsWithAgendaFilter("Dispense_Return_Validation"));
        } finally {
            kSession.dispose();
        }
    }

    /**
     * calculate tax amount based on tax definition.
     *
     * @param dispenseTaxList
     * @param totalTaxAmount
     * @param totalTaxPercentage
     */
    private void splitTaxAmountForLine(List<DispenseTax> dispenseTaxList, BigDecimal totalTaxAmount, Float totalTaxPercentage) {
        dispenseTaxList.forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            BigDecimal taxAmount = multiply(totalTaxAmount, divide(taxCalculation.getPercentage(), totalTaxPercentage));
            taxAmount = taxAmount;// roundOff(taxAmount, 2);
            dispenseTax.setTaxAmount(taxAmount);
        });

    }


    /**
     * Calculate Dispense details
     *
     * @param dispense
     * @return
     */
    @Override
    public DispenseReturn calculateDispenseReturnDetail(DispenseReturn dispense) {

        validateDispense(dispense);

        if (null != dispense && null != dispense.getDocument()) {
            dispense.getDocument().getDispenseReturnDocumentLines().forEach(documentLine -> {
                documentLine.resetReturnLine();
                calculateAmountOnRatioBasis(documentLine);
            });

        }

        dispense.getDocument().summarize();
        return dispense;
    }

    private List<StockEntry> stockReversal(DispenseReturn dispenseReturn) throws Exception {

        DispenseReturnDocument dispenseReturnDocument = dispenseReturn.getDocument();
        List<StockEntry> stockFLowList = new ArrayList<StockEntry>();
        StringBuilder errorMessage = new StringBuilder();
        if (null != dispenseReturnDocument && (null != dispenseReturnDocument.getDispenseReturnDocumentLines() && dispenseReturnDocument.getDispenseReturnDocumentLines().size() > 0)) {
            List<DispenseReturnDocumentLine> returnLineList = dispenseReturnDocument.getDispenseReturnDocumentLines();
            returnLineList.forEach(disReturnDocLine -> {
                if (disReturnDocLine.getQuantity() > 0) {
                    StockEntry stockEntry = new StockEntry();
                    stockEntry.setStoreId(dispenseReturnDocument.getReturnhsc() != null ? dispenseReturnDocument.getReturnhsc().getId() : null);
                    stockEntry.setTransactionDate(dispenseReturn.getDocument().getModifiedDate());
                    stockEntry.setTransactionId(dispenseReturn.getId());
                    stockEntry.setTransactionNumber(dispenseReturn.getDocumentNumber());
                    stockEntry.setTransactionType(dispenseReturn.getDocument().getDocumentType());
                    stockEntry.setUnitId(dispenseReturnDocument.getReturnUnit() == null ? null : Long.valueOf(dispenseReturnDocument.getReturnUnit().getId().toString()));
                    stockEntry.setAvailableQuantity(disReturnDocLine.getQuantity());
                    stockEntry.setBarCode(disReturnDocLine.getBarCode());
                    stockEntry.setBatchNo(disReturnDocLine.getBatchNumber());
                    stockEntry.setConsignment(disReturnDocLine.getConsignment());
                    stockEntry.setCost(disReturnDocLine.getCost());
                    stockEntry.setExpiryDate(disReturnDocLine.getExpiryDate());
                    stockEntry.setItemId(disReturnDocLine.getItemId());
                    stockEntry.setMrp(disReturnDocLine.getMrp());
                    if(PricingMethod.Fixed_Sale.equals(disReturnDocLine.getPricingMethod())){
                        if(isNull(disReturnDocLine.getOriginalMRP())){
                            log.error("Original MRP is Required for item code:{}", disReturnDocLine.getCode());
                            throw new org.nh.ehr.web.rest.erros.CustomParameterizedException("10414", new HashMap<String, Object>() {{
                                put("itemName", disReturnDocLine.getName());
                                put("itemCode", disReturnDocLine.getCode());
                            }});
                        }
                        stockEntry.setMrp(disReturnDocLine.getOriginalMRP());
                    }
                    stockEntry.setOwner(disReturnDocLine.getOwner());
                    stockEntry.setQuantity(disReturnDocLine.getQuantity());
                    stockEntry.setSku(disReturnDocLine.getSku());
                    stockEntry.setTransactionLineId(disReturnDocLine.getLineNumber());
                    stockEntry.setSupplier(disReturnDocLine.getSupplier());
                    stockEntry.setUserId(dispenseReturn.getDocument().getCreatedBy().getId());
                    setLocatorforStockReversal(disReturnDocLine, dispenseReturnDocument, stockEntry, errorMessage);
                    stockFLowList.add(stockEntry);
                }
            });
            if (errorMessage.length() > 0) {
                log.error("Item Store Locator could not found for itemNames:{}", errorMessage.toString());
                throw new IllegalArgumentException(errorMessage.toString());
            }
            stockService.stockIn(stockFLowList);
        }
        return stockFLowList;
    }


    private void setLocatorforStockReversal(DispenseReturnDocumentLine dispenseReturnDocumentLine, DispenseReturnDocument dispenseReturnDocument,
                                            StockEntry stockEntry, StringBuilder errorMessage) {
        if (dispenseReturnDocumentLine.getUom() == null) {
            throw new CustomParameterizedException("10081", "UOM can not be null");
        } else {
            stockEntry.setUomId(dispenseReturnDocumentLine.getUom().getId());
            if (!dispenseReturnDocument.getHsc().getId().equals(dispenseReturnDocument.getReturnhsc().getId())) {
                Page<ItemStoreLocatorMap> itemStoreLocatorMaps = this.itemStoreLocatorMapService.search(
                    "active:true item.id:" + dispenseReturnDocumentLine.getItemId() + " healthCareServiceCenter.id:" + dispenseReturnDocument.getReturnhsc().getId(), PageRequest.of(0,1));
                boolean isLocatorMissing = false;
                if (itemStoreLocatorMaps.hasContent()) {
                    ItemStoreLocatorMap itemStoreLocatorMap = itemStoreLocatorMaps.iterator().next();
                    if (itemStoreLocatorMap != null) {
                        stockEntry.setLocatorId(itemStoreLocatorMap.getLocator().getId());
                    } else {
                        isLocatorMissing = true;
                    }
                } else{
                    isLocatorMissing = true;
                }
                if(isLocatorMissing){
                    errorMessage
                        .append(errorMessage.length() > 0 ? ", " : "")
                        .append(errorMessage.length() == 0 ? "Locators not mapped for items:" : "")
                        .append(dispenseReturnDocumentLine.getName());
                }
                stockEntry.setStockId(null);
                setStockOriginalDetails(dispenseReturnDocumentLine, stockEntry);
            } else {
                stockEntry.setLocatorId(dispenseReturnDocumentLine.getLocator().getId());
                stockEntry.setStockId(dispenseReturnDocumentLine.getStockId());
            }
        }


    }

    private void setStockOriginalDetails(DispenseReturnDocumentLine dispenseReturnDocumentLine, StockEntry stockEntry) {
        Stock dispensedStock = stockService.findOne(dispenseReturnDocumentLine.getStockId());
        stockEntry.setOriginalBatchNo(dispensedStock.getOriginalBatchNo());
        stockEntry.setOriginalExpiryDate(dispensedStock.getOriginalExpiryDate());
        stockEntry.setOriginalMRP(dispensedStock.getOriginalMRP());
    }

    /**
     * @param dispenseNumber
     * @return
     */
    public List<DispenseReturn> getcancelledDispenseDocuments(String dispenseNumber) {
        List<DispenseReturn> disReturnDocList = dispenseReturnSearchRepository.findByDispenseNumber(dispenseNumber);
        return disReturnDocList;
    }

    /**
     * @param dispenseReturn
     * @return
     */
    @Override
    public DispenseReturn getUpdateDispenseDocument(DispenseReturn dispenseReturn) {

        DispenseReturnDocument DispenseReturnDocument = dispenseReturn.getDocument();
        List<DispenseReturn> documnetList = getcancelledDispenseDocuments(DispenseReturnDocument.getDispenseNumber());
        if (null != DispenseReturnDocument) {
            if (null != DispenseReturnDocument.getDispenseReturnDocumentLines()) {
                DispenseReturnDocument.getDispenseReturnDocumentLines().forEach(dispenseReturnLine -> {
                    updateDocumentLine(dispenseReturnLine, documnetList);
                });
            }
        }
        return dispenseReturn;
    }

    /**
     * @param dispenseReturnDocumentLine
     * @param documnetList
     */
    public void updateDocumentLine(DispenseReturnDocumentLine dispenseReturnDocumentLine, List<DispenseReturn> documnetList) {

        if (null != documnetList && documnetList.size() > 0) {
            documnetList.forEach(dispenseReturn -> {
                DispenseReturnDocument dispenseReturnDocument = dispenseReturn.getDocument();
                dispenseReturnDocument.getDispenseReturnDocumentLines().forEach(dispenseReturnDocLine -> {
                    if (dispenseReturnDocLine.getLineNumber().equals(dispenseReturnDocumentLine.getLineNumber()) && (dispenseReturnDocument.getReturnStatus().equals(ReturnStatus.RETURNED))) {
                        dispenseReturnDocumentLine.setPrevReturnQuantity(dispenseReturnDocumentLine.getPrevReturnQuantity() + dispenseReturnDocLine.getQuantity());
                    }
                });
            });
        }
    }

    public void checkPendingForReturnDocument(DispenseReturn dispenseReturn) throws Exception {

        DispenseReturnDocument DispenseReturnDocument = dispenseReturn.getDocument();
        List<DispenseReturn> documentList = getcancelledDispenseDocuments(DispenseReturnDocument.getDispenseNumber());
        if (documentList.size() > 0) {
            documentList.forEach(dispenseReturnDoc -> {
                DispenseReturnDocument dispenseReturnDocument = dispenseReturnDoc.getDocument();
                if (!dispenseReturnDoc.getDocumentNumber().equalsIgnoreCase(dispenseReturn.getDocumentNumber())
                    && !(ReturnStatus.RETURNED.equals(dispenseReturnDocument.getReturnStatus()) || ReturnStatus.CLOSED.equals(dispenseReturnDocument.getReturnStatus()))) {
                    throw new CustomParameterizedException("10075", "Can not create new return for the selected item as return for the document :" + dispenseReturn.getDocumentNumber() + " is still pending.");
                }
            });
        }

    }

    /**
     * @param documentNumber
     * @return
     */
    @Override
    public DispenseReturn createDispenseReturnDocument(String documentNumber) {

        Dispense dispense = dispenseSearchRepository.findByDocumentNumber(documentNumber);
        DispenseReturn dispenseReturn = dispenseToReturnMapper.convertDispenseToDispenseReturn(dispense);
        Source dispenseRef = new Source();
        dispenseRef.setReferenceNumber(dispense.getDocument().getDispenseNumber());
        dispenseRef.setId(dispense.getId());
        dispenseReturn.getDocument().setDispenseRef(dispenseRef);
        dispenseReturn = getUpdateDispenseDocument(dispenseReturn);
        return dispenseReturn;
    }


    /**
     * DispenseReturn Auto close
     */
    @Override
    public void autoClose() {
        log.debug("DispenseReturn Auto close");
        LocalDateTime currentDate = LocalDateTime.now();
        Query query = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery("document.returnStatus.raw:(" + ReturnStatus.DRAFT + " OR " + ReturnStatus.APPROVED + " OR " + ReturnStatus.REJECTED + " OR " + ReturnStatus.PENDING_APPROVAL + ")"))
            .withPageable(PageRequest.of(0, 10000))
            .build();

        Iterator<DispenseReturn> dispenseReturnIterator =  queryForList("dispensereturn", query, elasticsearchTemplate, DispenseReturn.class).listIterator();

        while (dispenseReturnIterator.hasNext()) {
            DispenseReturn dispenseReturn = dispenseReturnIterator.next();
            Long unitId = dispenseReturn.getDocument().getReturnUnit().getId();
            String hoursForAutoClose = null;
            String processId = null;
            try {
                hoursForAutoClose = ConfigurationUtil.getConfigurationData("athma_dispensereturn_autoclose_duration_hours", dispenseReturn.getDocument().getReturnhsc().getId(), unitId, null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
                boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_return_enable_workflow", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
                if (isWorkflowEnabled) {
                    processId = ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_return_workflow_definition", dispenseReturn.getDocument().getHsc().getId(), dispenseReturn.getDocument().getReturnUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService);
                }
            } catch (Exception e) {
                //Do nothing
            }
            LocalDateTime modifiedDate = dispenseReturn.getDocument().getModifiedDate();
            try {
                if (hoursForAutoClose != null && modifiedDate != null) {
                    LocalDateTime addedAutoCloseDuration = modifiedDate.plusHours(Long.parseLong(hoursForAutoClose));
                    if (currentDate.isAfter(addedAutoCloseDuration)) {
                        boolean isProcessInstanceCompleted = dispenseReturn.getDocument().getReturnStatus().equals(ReturnStatus.APPROVED) ||  dispenseReturn.getDocument().getReturnStatus().equals(ReturnStatus.REJECTED);
                        dispenseReturn.getDocument().setReturnStatus(ReturnStatus.CLOSED);
                        dispenseReturn = save(dispenseReturn);
                        index(dispenseReturn);
                        if (processId != null && !isProcessInstanceCompleted) {
                            workflowService.abortActiveProcessInstance(processId, "document_number", dispenseReturn.getDocumentNumber(), "admin");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Dispense Return indexing issue for Return number: {}", dispenseReturn.getDocumentNumber());
                roleBackFromElasticSearch(dispenseReturnIterator);
            }
        }
    }

    @Override
    public void index(DispenseReturn dispenseReturn) {
        dispenseReturnSearchRepository.save(dispenseReturn);
    }

    public void calculateAmountOnRatioBasis(DispenseReturnDocumentLine line) {

        line.setTotalMrp(multiply(line.getDispenseReturnDetails().getMrp(), line.getQuantity()));
        line.setSaleRate(line.getDispenseReturnDetails().getSaleRate());
        line.setSaleAmount(multiply(line.getDispenseReturnDetails().getSaleRate(), line.getQuantity()));
        line.setTaxAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getTaxAmount()));
        line.setNetAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getNetAmount()));
        line.setGrossAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getGrossAmount()));
        line.setGrossRate(divide(line.getGrossAmount(), line.getQuantity()));
        line.setPatientTaxAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getPatientTaxAmount()));
        line.setPatientGrossAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getPatientGrossAmount()));
        line.setPatientTotalDiscAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getPatientDiscountAmount()));
        line.setTotalDiscountAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getTotalDiscountAmount()));
        line.setPatientSaleAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getPatientSaleAmount()));
        line.setSponsorTaxAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getSponsorTaxAmount()));
        line.setSponsorGrossAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getSponsorGrossAmount()));
        line.setSponsorDiscAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getSponsorDiscountAmount()));
        line.setSponsorSaleAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getSponsorSaleAmount()));
        line.setPlanDiscountAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getPlanDiscountAmount()));
        line.setUnitDiscount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getUnitDiscountAmount()));
        line.setUserDiscount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getUserDiscountAmount()));
        line.setTaxDiscountAmount(CalculateTaxUtil.calculateAmountToReturn(line.getQuantity(), line.getDispenseReturnDetails().getQuantity(), line.getDispenseReturnDetails().getTaxDiscountAmount()));
        line.setPatientNetAmount(subtract(add(line.getPatientGrossAmount(), line.getPatientTaxAmount()),
            (subtract(add(line.getPatientTotalDiscAmount(), line.getPlanDiscountAmount()), line.getSponsorDiscAmount()))));
        line.setPatientNetAmount(subtract(add(line.getPatientGrossAmount(), line.getPatientTaxAmount()),
            add(line.getPatientTotalDiscAmount(), line.getTaxDiscountAmount())));
        line.setSponsorNetAmount(subtract(add(line.getSponsorGrossAmount(), line.getSponsorTaxAmount()), line.getSponsorDiscAmount()));
        line.setNetAmount(add(line.getGrossAmount(), subtract(line.getTaxAmount(), line.getTotalDiscountAmount())));
        line.setPatientTotalTaxAmount(add(line.getSponsorNetAmount(), subtract(line.getSponsorTaxAmount(), getBigDecimal(line.getTotalTaxInPercent()))));

        line.setTaxAmount(BigDecimalUtil.ZERO);
        BigDecimal sponsorTaxAmount = line.getSponsorTaxAmount();
        BigDecimal patientTaxAmount = line.getPatientTaxAmount();
        line.setSponsorTaxAmount(BigDecimalUtil.ZERO);
        line.setPatientTaxAmount(BigDecimalUtil.ZERO);
        line.getDispenseTaxes().forEach(taxes -> {
            if (line.getTotalTaxInPercent() > 0) {
                taxes.setPatientTaxAmount(roundOff(
                    divide(multiply(patientTaxAmount, taxes.getTaxDefinition().getTaxCalculation().getPercentage()), line.getTotalTaxInPercent()), 2));
                taxes.setTaxAmount(roundOff(
                    divide(multiply(sponsorTaxAmount, taxes.getTaxDefinition().getTaxCalculation().getPercentage()), line.getTotalTaxInPercent()), 2));
            }
            line.setTaxAmount(sum(line.getTaxAmount(), taxes.getPatientTaxAmount(), taxes.getTaxAmount()));
            line.setPatientTaxAmount(add(line.getPatientTaxAmount(), taxes.getPatientTaxAmount()));
            line.setSponsorTaxAmount(add(line.getSponsorTaxAmount(), taxes.getTaxAmount()));
        });
    }

    public void deleteValidation(DispenseReturn dispenseReturn) {
        log.debug("validate before delete Dispense Return  : {}", dispenseReturn);
        if (dispenseReturn.getDocument().getReturnStatus() != DRAFT) {
            throw new CustomParameterizedException("10088", "Can not delete document ,Only Draft Status document can be deleted");
        }
    }

    public void saveValidation(DispenseReturn dispenseReturnRef) {
        DispenseReturn dispenseReturn = dispenseReturnSearchRepository.findByDocumentNumber(dispenseReturnRef.getDocumentNumber());
        if (null == dispenseReturn) {
            dispenseReturn = dispenseReturnRef;
        }
        Preferences preferences = UserPreferencesUtils.getCurrentUserPreferences();
        if (null != preferences && !(dispenseReturn.getDocument().getDispenseUnit().getId().equals(preferences.getHospital().getId()))) {
            throw new CustomParameterizedException("10089", "Current Document unit doesn't match with selected logged in unit");
        }
    }

    /**
     * Re-index all entities for given details
     *
     * @param documentMap
     */
    @Override
    public void reIndexBilling(Map<String, Object> documentMap) {

        if (null == documentMap || documentMap.isEmpty()) {
            return;
        }

        DispenseReturn dispenseReturn = (DispenseReturn) documentMap.get("dispenseReturn");
        if (dispenseReturn != null && dispenseReturn.getId() != null) {
            reIndex(dispenseReturn.getId());
        }

        Invoice invoice = (Invoice) documentMap.get("invoice");
        if (invoice != null && invoice.getId() != null) {
            invoiceService.reIndex(invoice.getId());
        }

        List<SponsorInvoiceDTO> sponsorInvoices = (List<SponsorInvoiceDTO>) documentMap.get("sponsorInvoices");
        if (sponsorInvoices != null && !sponsorInvoices.isEmpty()) {
            sponsorInvoices.forEach(sponsorInvoice -> {
                sponsorInvoiceService.reIndex(sponsorInvoice.getId());
            });
        }

        Receipt receipt = (Receipt) documentMap.get("receipt");
        if (receipt != null) {
            receiptService.reIndex(receipt.getId());
        }

        InvoiceReceipt invoiceReceipts = (InvoiceReceipt) documentMap.get("invoiceReceipts");
        if (invoiceReceipts != null) {
            invoiceReceiptService.reIndex(invoiceReceipts.getId());
        }

        Refund refundRecipt = (Refund) documentMap.get("refund");
        if (refundRecipt != null) {
            refundService.reIndex(refundRecipt.getId());
        }
    }

    public DispenseReturn reIndex(Long id) {
        log.debug("Request to do re-index for dispense  return: {}", id);
        DispenseReturn dispenseReturn = null;
        if (id != null) {
            dispenseReturn = dispenseReturnRepository.findOne(id);
            log.debug("dispense return: {}", dispenseReturn);
            if (dispenseReturn == null) {
                if (dispenseReturnSearchRepository.existsById(id)) {
                    dispenseReturnSearchRepository.deleteById(id);
                }
            } else {
                dispenseReturn = dispenseReturnSearchRepository.save(dispenseReturn);
            }
        }
        log.debug("Request to do reIndex dispense ends : {}", LocalTime.now());
        return dispenseReturn;
    }

    public void reIndex(DispenseReturn dispenseReturn) {
        if (nonNull(dispenseReturn) && nonNull(dispenseReturn.getId())) {
            this.reIndex(dispenseReturn.getId());
            Page<Invoice> invoiceList = invoiceService.search("invoiceDocument.source.id:" + dispenseReturn.getId(), PageRequest.of(0, 9999));
            for (Invoice invoice : invoiceList) {
                invoiceService.reIndex(invoice.getId());
                Iterator<Receipt> receipts = receiptService.search("receiptNumber.raw:"+ invoice.getInvoiceNumber(),PageRequest.of(0, 9999)).iterator();
                while (receipts.hasNext()) {
                    receiptService.reIndex(receipts.next().getId());
                }
                Iterator<Refund> refunds = refundService.search("refundDocument.refundItems.receiptDetail.receiptNumber.raw:"+ invoice.getInvoiceNumber(),PageRequest.of(0, 9999)).iterator();
                while (refunds.hasNext()) {
                    refundService.reIndex(refunds.next().getId());
                }
                Iterator<SponsorInvoice> sponsorInvoices = sponsorInvoiceService.search("sponsorDocument.source.id:"+invoice.getId(), PageRequest.of(0, 9999)).iterator();
                while (sponsorInvoices.hasNext()) {
                    SponsorInvoice sponsorInvoice = sponsorInvoices.next();
                    sponsorInvoiceService.reIndex(sponsorInvoice.getId());
                }
                Iterator<InvoiceReceipt> invoiceReceipts = invoiceReceiptService.search("invoiceId:"+invoice.getId(), PageRequest.of(0, 9999)).iterator();
                while (invoiceReceipts.hasNext()) {
                    InvoiceReceipt invoiceReceipt = invoiceReceipts.next();
                    invoiceReceiptService.reIndex(invoiceReceipt.getId());
                    receipts = receiptService.search("id:"+ invoiceReceipt.getReceiptId(),PageRequest.of(0, 9999)).iterator();
                    while (receipts.hasNext()) {
                        receiptService.reIndex(receipts.next().getId());
                    }
                }
            }
        }
    }

    private boolean isGroupExist(DispenseReturn dispenseReturn) {
        String group = getGroupData(DispenseReturn_Approval_Committee, dispenseReturn.getDocument().getReturnUnit().getId());
        if (null != group && !group.isEmpty())
            return true;
        return false;
    }

    private void roleBackFromElasticSearch(Iterator<DispenseReturn> dispenseReturns) {
        dispenseReturns.forEachRemaining(dispenseReturn -> {
            reIndex(dispenseReturn.getId());
        });
    }

    private void validateDocument(DispenseReturn dispenseReturn) {
        List<DispenseReturn> dispenseReturns = dispenseReturnSearchRepository.findByInvoiceNumber(dispenseReturn.getDocument().getInvoiceRef().getReferenceNumber());
        Float quantity = 0.0f;

        for (DispenseReturnDocumentLine lines : dispenseReturn.getDocument().getDispenseReturnDocumentLines()) {
            for (DispenseReturn dispenseReturnDoc : dispenseReturns) {

                for (DispenseReturnDocumentLine documentLine : dispenseReturnDoc.getDocument().getDispenseReturnDocumentLines()) {

                    if (lines.getItemId().equals(documentLine.getItemId())) {
                        quantity += documentLine.getPrevReturnQuantity();
                    }
                }

                if (null != lines.getReturnQuantity() && lines.getReturnQuantity() > quantity) {
                    throw new CustomParameterizedException("10133", "Return quantity should not be more than previous return quantity");
                }
            }
            quantity = 0.0f;
        }

    }

    /***
     *
     * @param dispenseReturnNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    public Map<String, Object> compareVersion(String dispenseReturnNumber, Integer versionFirst, Integer versionSecond) {
        log.debug("comparing for the dispenceReturn:" + dispenseReturnNumber + " Version First:" + versionFirst + " Version Second:" + versionSecond);

        List<DispenseReturn> dispenseReturnList = dispenseReturnRepository.findDispenseReturnByVersions(dispenseReturnNumber, versionFirst, versionSecond);
        Map<String, Object> documents = new HashedMap();
        for (DispenseReturn dispenseReturn : dispenseReturnList) {
            if (versionFirst.equals(dispenseReturn.getVersion())) {
                Map<String, Object> firstDoc = new HashedMap();
                firstDoc.put("dispenseReturnNumber", dispenseReturn.getDocumentNumber());
                firstDoc.put("version", dispenseReturn.getVersion());
                firstDoc.put("document", dispenseReturn);
                documents.put("firstDocument", firstDoc);
            } else {
                Map<String, Object> secondDoc = new HashedMap();
                secondDoc.put("dispenseReturnNumber", dispenseReturn.getDocumentNumber());
                secondDoc.put("version", dispenseReturn.getVersion());
                secondDoc.put("document", dispenseReturn);
                documents.put("secondDocument", secondDoc);
            }
        }
        return documents;
    }

    /***
     *
     * @param dispenseReturnNumber
     * @return
     */
    public List<Integer> getAllVersion(String dispenseReturnNumber) {
        log.debug("get All Version for the dispenseReturn:" + dispenseReturnNumber);
        List<Integer> versionList = dispenseReturnRepository.filndALlVersion(dispenseReturnNumber);
        return versionList;
    }


    @Override
    //@Transactional(rollbackFor = Exception.class)
    public DispenseReturn processIPDispenseReturn(DispenseReturn dispenseReturn) throws Exception {

        log.debug("Request to save IP dispenseReturn. dispense return ={} : {}", dispenseReturn);
        DispenseReturn result = save(dispenseReturn);
        stockReversal(result);
        log.debug("Dispense return is saved successfully");
        try{
            publishDispenseReturnDocument(result, applicationProperties.getAthmaBucket().getDocBasePath());
            //Save html file to docbasepath for Patient Records in DMS
            getReturnHTMLByReturnId(result.getId(), result.getDocumentNumber());
        }catch (Exception ex){
            log.error("Error while publishing dispense return document : ", ex);
        }
        return result;
    }

    /**
     * Publish DispenseReturn
     * @param dispenseReturn
     */
    private void publishDispenseReturnDocument(DispenseReturn dispenseReturn, String docBasePath) {
        log.debug("Request to publishing dispense return to DMS: {}", dispenseReturn);
        DocumentRecordDTO documentRecord = dispenseReturnToDocumentRecordMapper.dispenseReturnToDocumentRecord(dispenseReturn, docBasePath);
        documentRecordService.produce(documentRecord);
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<DispenseReturn> search = this.search("documentNumber.raw:" + documentNumber, PageRequest.of(0, 1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        DispenseReturn dispenseReturn = search.iterator().next();
        Map<String, Object> configurations = retrieveWorkflowConfigurations(dispenseReturn, true);
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(dispenseReturn, configurations);
        }
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return ConfigurationUtil.getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
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
}
