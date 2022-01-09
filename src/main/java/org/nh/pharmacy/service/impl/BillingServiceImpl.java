package org.nh.pharmacy.service.impl;

import org.jbpm.services.api.model.DeployedUnit;
import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.InvoiceReceipt;
import org.nh.billing.domain.Receipt;
import org.nh.billing.domain.SponsorInvoice;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.SponsorInvoiceType;
import org.nh.billing.repository.search.InvoiceReceiptSearchRepository;
import org.nh.billing.repository.search.ReceiptSearchRepository;
import org.nh.billing.repository.search.SponsorInvoiceSearchRepository;
import org.nh.billing.service.*;
import org.nh.common.dto.DocumentRecordDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.util.BigDecimalUtil;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.annotation.PamIntegration;
import org.nh.pharmacy.annotation.PublishPharmacyMedicationRequest;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.DispensePlan;
import org.nh.pharmacy.domain.dto.PaymentDetail;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.mapper.*;
import org.nh.seqgen.exception.SequenceGenerateException;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static java.util.Objects.nonNull;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.nh.common.util.BigDecimalUtil.add;
import static org.nh.common.util.BigDecimalUtil.gtZero;
import static org.nh.pharmacy.domain.enumeration.Context.Discount_Approval_Committee;
import static org.nh.pharmacy.domain.enumeration.DispenseStatus.*;

/**
 * Created by Nirbhay on 6/23/17.
 */
@Service("billingService")
@Transactional
public class BillingServiceImpl implements BillingService {

    private final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final DispenseService dispenseService;

    private final InvoiceService invoiceService;

    private final StockService stockService;

    private final ReceiptService receiptService;

    private final ReceiptSearchRepository receiptSearchRepository;

    private final InvoiceReceiptService invoiceReceiptService;

    private final InvoiceReceiptSearchRepository invoiceReceiptSearchRepository;

    private final WorkflowService workflowService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final DispenseRepository dispenseRepository;

    private final DispenseToReceiptMapper dispenseToReceiptMapper;

    private final InvoiceMapper invoiceMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final SponsorInvoiceMapper sponsorInvoiceMapper;

    private final DispenseReturnToSponsorInvoiceMapper dispenseReturnToSponsorInvoiceMapper;

    private final SponsorInvoiceService sponsorInvoiceService;

    private final GroupService groupService;

    private final SponsorInvoiceSearchRepository sponsorInvoiceSearchRepository;

    private final PlanExecutionServiceImpl planExecutionService;

    private final RuleExecutorService ruleExecutorService;

    private final DocumentRecordService documentRecordService;

    private final DispenseToDocumentRecordMapper dispenseToDocumentRecordMapper;

    private final ApplicationProperties applicationProperties;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    public BillingServiceImpl(DispenseService dispenseService, StockService stockService, DispenseToReceiptMapper dispenseToReceiptMapper,
                              InvoiceService invoiceService, ReceiptService receiptService, ReceiptSearchRepository receiptSearchRepository, InvoiceMapper invoiceMapper,
                              InvoiceReceiptService invoiceReceiptService, InvoiceReceiptSearchRepository invoiceReceiptSearchRepository, WorkflowService workflowService, SequenceGeneratorService sequenceGeneratorService, DispenseRepository dispenseRepository, ElasticsearchOperations elasticsearchTemplate,
                              SponsorInvoiceMapper sponsorInvoiceMapper, SponsorInvoiceService sponsorInvoiceService, DispenseReturnToSponsorInvoiceMapper dispenseReturnToSponsorInvoiceMapper, GroupService groupService, SponsorInvoiceSearchRepository sponsorInvoiceSearchRepository, PlanExecutionServiceImpl planExecutionService, RuleExecutorService ruleExecutorService,
                              DocumentRecordService documentRecordService, DispenseToDocumentRecordMapper dispenseToDocumentRecordMapper, ApplicationProperties applicationProperties, PharmacyRedisCacheService pharmacyRedisCacheService) {
        this.dispenseService = dispenseService;
        this.invoiceService = invoiceService;
        this.stockService = stockService;
        this.receiptService = receiptService;
        this.receiptSearchRepository = receiptSearchRepository;
        this.invoiceReceiptService = invoiceReceiptService;
        this.invoiceReceiptSearchRepository = invoiceReceiptSearchRepository;
        this.workflowService = workflowService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.dispenseToReceiptMapper = dispenseToReceiptMapper;
        this.invoiceMapper = invoiceMapper;
        this.dispenseRepository = dispenseRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.sponsorInvoiceMapper = sponsorInvoiceMapper;
        this.sponsorInvoiceService = sponsorInvoiceService;
        this.dispenseReturnToSponsorInvoiceMapper = dispenseReturnToSponsorInvoiceMapper;
        this.groupService = groupService;
        this.sponsorInvoiceSearchRepository = sponsorInvoiceSearchRepository;
        this.planExecutionService = planExecutionService;
        this.ruleExecutorService = ruleExecutorService;
        this.documentRecordService = documentRecordService;
        this.dispenseToDocumentRecordMapper = dispenseToDocumentRecordMapper;
        this.applicationProperties = applicationProperties;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    @Override
    @PamIntegration
    @PublishStockTransaction
    @PublishPharmacyMedicationRequest
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveDispenseWithAction(Dispense dispense, String action) throws Exception {

        log.debug("Request to save dispense based on Action : {}", action);
        Map<String, Object> documentMap = new HashMap();
        documentMap.put("dispense", dispense);
        Dispense result;
        if (action == null) {
            action = "DRAFT";
        }
        Invoice invoice = null;
        switch (action) {
            case "SENDFORAPPROVAL":
                log.debug("Request to send approval dispense : {}", dispense);
                if(!isGroupExist(dispense))
                    throw  new CustomParameterizedException("10114","Approval Group doesn't exit  for logged in unit");
                generateTempNumberForExternalPatient(dispense);
                result = sendForApproval(dispense);
                documentMap.put("dispense", result);
                invoice = this.createInvoiceByDispense(result, "SENDFORAPPROVAL");
                documentMap.put("invoice", invoice);
                this.callWorkflow(result);
                break;
            case "APPROVED":
                log.debug("Request to approve dispense : {}", dispense);
                dispense.getDocument().setDispenseStatus(APPROVED);
                result = dispenseService.save(dispense);
                documentMap.put("dispense", result);
                invoice = this.createInvoiceByDispense(result, "APPROVED");
                documentMap.put("invoice", invoice);
                break;
            case "REJECTED":
                log.debug("Request to reject dispense : {}", dispense);
                dispense.getDocument().setDispenseStatus(REJECTED);
                result = dispenseService.save(dispense);
                documentMap.put("dispense", result);
                invoice = this.createInvoiceByDispense(result, "REJECTED");
                documentMap.put("invoice", invoice);
                break;
            case "CONFIRMED":
                log.debug("Request to confirm dispense : {}", dispense);
                if(null != dispense.getId()) {//If other user is dispensing already dispensed document
                    Dispense existingDispense = dispenseService.findOne(dispense.getId());
                    if (null != existingDispense && DISPENSED.equals(existingDispense.getDocument().getDispenseStatus())) {
                        throw new IllegalStateException("Document already dispensed");
                    }
                }
                generateTempNumberForExternalPatient(dispense);
                if (DISPENSED.equals(dispense.getDocument().getDispenseStatus())) {
                    throw new IllegalStateException("Document already dispensed");
                } else {
                    if (dispense.getDocument().isDraft() && isApprovalRequired(dispense)) {
                        result = sendForApproval(dispense);
                        this.callWorkflow(result);
                    } else {
                        //Boolean isApproved = APPROVED.equals(dispense.getDocument().getDispenseStatus());
                        dispense.getDocument().setDispenseStatus(DISPENSED);
                        calculatePatientPaidAmount(dispense);
                        if (dispense.getDocument().isDraft()) {
                            dispense.getDocument().setDraft(false);
                            dispense.documentNumber(sequenceGeneratorService.generateSequence("Dispense", "NH", dispense));
                            result = dispenseService.save(dispense);
                            reserveStock(result);
                        } else {
                            //result = isApproved ? dispenseService.save(dispense) : dispenseService.saveOrUpdate(dispense);
                            result = dispenseService.save(dispense);
                            deleteAndReserveStock(result);
                        }
                        documentMap.put("dispense", result);
                        Map<String, Object> tempMap = this.approveBillingProcess(dispense.getId());
                        if(null != tempMap && !tempMap.isEmpty()) documentMap.putAll(tempMap);
                    }
                }
                publishDispenseDocument(result, applicationProperties.getAthmaBucket().getDocBasePath());
                dispenseService.getInvoiceHTMLByDispenseId(result.getId(), result.getDocumentNumber());
                break;
            default:
                if (dispense.getDocument().getDispenseStatus() == null) {
                    dispense.getDocument().setDispenseStatus(DRAFT);
                }
                result = dispenseService.save(dispense);
        }
        documentMap.put("dispense", result);
        return documentMap;
    }

    private void generateTempNumberForExternalPatient(Dispense dispense) {
        PatientDTO patient = dispense.getDocument().getPatient();
        if ((patient.getId() == null || patient.getId() == 0) && null == patient.getTempNumber()) {
            patient.setTempNumber(sequenceGeneratorService.generateNumber("DispenseExternalPatientNumber", "NH", dispense));
            if (null != dispense.getDocument().getEncounter())
                dispense.getDocument().getEncounter().setPatient(patient);
        }
    }

    private void calculatePatientPaidAmount(Dispense dispense) {
        List<PaymentDetail> paymentDetails = dispense.getDocument().getPaymentDetails();
        BigDecimal totalPatientPaidAmount = BigDecimalUtil.ZERO;
        if (paymentDetails != null && !paymentDetails.isEmpty()) {
            for (PaymentDetail paymentDetail : paymentDetails) {
                totalPatientPaidAmount = add(totalPatientPaidAmount, (paymentDetail.getTotalAmount() == null ? BigDecimalUtil.ZERO : paymentDetail.getTotalAmount()));
            }
        }
        dispense.getDocument().setPatientPaidAmount(totalPatientPaidAmount);
    }

    /**
     * Send for approval
     *
     * @param dispense
     * @return dispense object
     * @throws Exception
     */
    public Dispense sendForApproval(Dispense dispense) throws Exception {
        dispense.getDocument().setDispenseStatus(PENDING_APPROVAL);
        Dispense result;
        if (dispense.getDocument().isDraft()) {
            dispense.getDocument().setDraft(false);
            dispense.documentNumber(sequenceGeneratorService.generateSequence("Dispense", "NH", dispense));
            result = dispenseService.save(dispense);
            reserveStock(result);
        } else {
            result = dispenseService.save(dispense);
            deleteAndReserveStock(result);
        }
        return result;
    }

    /**
     * Call workflow
     *
     * @param dispense
     */
    private void callWorkflow(Dispense dispense) {
        Map<String, Object> configurations = retrieveWorkflowConfigurations(dispense, true);
        if ((Boolean) configurations.get("enableWorkflow")) {
            startWorkflow(dispense, configurations);
        }
    }

    /**
     * Create invoice by dispense and update source info in dispense
     *
     * @param dispense
     * @param action
     * @return
     * @throws Exception
     */
    private Invoice createInvoiceByDispense(Dispense dispense, String action) throws Exception {

        Invoice invoice = invoiceMapper.convertDispenseToInvoice(dispense);
        //Check Invoice exist than re-use invoice number
        if (invoice.getId() != null) {
            Invoice queriedInvoice = invoiceService.findOne(invoice.getId());
            if (queriedInvoice != null) {
                invoice.setInvoiceNumber(queriedInvoice.getInvoiceNumber());
                invoice.setVersion(queriedInvoice.getVersion());
                invoice.getInvoiceDocument().setDraft(queriedInvoice.getInvoiceDocument().getDraft());
            } else if (invoice.getVersion() == null) {
                invoice.setVersion(0);
            }
        }
        //set source details of invoice
        SourceDTO invoiceSource = new SourceDTO();
        invoiceSource.setId(dispense.getId());
        invoiceSource.setDocumentType(DocumentType.DISPENSE);
        invoiceSource.setReferenceNumber(dispense.getDocumentNumber());
        invoice.getInvoiceDocument().setSource(invoiceSource);
        if("CONFIRMED".equals(action)){
            List<Receipt> receipts = dispenseToReceiptMapper.convertDispenseToReceipts(dispense);
            invoice.getInvoiceDocument().setReceipts(receipts);
            log.debug("Added receipts to invoice: {}", receipts);
        }
        log.debug("Converted Dispense to Invoice: {}", invoice);
        invoice = invoiceService.createInvoiceDispense(invoice, action);
        SourceDTO sourceRef = new SourceDTO();
        sourceRef.setId(invoice.getId());
        sourceRef.setDocumentType(DocumentType.INVOICE);
        sourceRef.setReferenceNumber(invoice.getInvoiceNumber());
        dispense.getDocument().setSource(sourceRef);
        updateDispenseLine(dispense, invoice);
        dispenseService.saveOrUpdate(dispense);

        return invoice;
    }

    /**
     * Update Source details in dispenseLine
     *
     * @param dispense
     * @param invoice
     */
    private void updateDispenseLine(Dispense dispense, Invoice invoice) {
        List<DispenseDocumentLine> dispenseLines = dispense.getDocument().getDispenseDocumentLines();
        List<InvoiceItem> invoiceLines = invoice.getInvoiceDocument().getInvoiceItems();

        for (int lineIndex = 0; lineIndex < dispenseLines.size(); lineIndex++) {
            Source sourceRef = new Source();
            sourceRef.setId(invoiceLines.get(lineIndex).getId());
            sourceRef.setReferenceNumber(invoice.getInvoiceNumber());
            dispenseLines.get(lineIndex).setSource(sourceRef);
        }
    }

    /**
     * Create invoice, receipt and invoiceReceipt
     *
     * @param dispenseId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> approveBillingProcess(Long dispenseId) throws Exception {
        Map<String, Object> documentMap = new HashMap();
        List<Receipt> receipts=null;
        log.debug("Request for dispenseId :{}", dispenseId);
        Dispense dispense = dispenseService.findOne(dispenseId);
        log.debug("Request for dispense : {}", dispense);
        //Convert Dispense to Invoice document
        Invoice invoice = createInvoiceByDispense(dispense, "CONFIRMED");
        dispense.getDocument().setEncounter(invoice.getInvoiceDocument().getEncounter());
        receipts=invoice.getInvoiceDocument().getReceipts();
        documentMap.put("invoice", invoice);
        documentMap.put("receipts",receipts);
        if(null !=invoice.getInvoiceDocument().getSponsorInvoices() && !invoice.getInvoiceDocument().getSponsorInvoices() .isEmpty()) {
            documentMap.put("sponsorInvoices", invoice.getInvoiceDocument().getSponsorInvoices());
        }
        log.debug("documentMap: {}", documentMap);
        //Stock out for dispense item
        dispenseService.produce(dispense);
        return documentMap;
    }

    /**
     * Re-index all entities for given details
     *
     * @param dispense
     */
    @Override
    public void reIndexBilling(Dispense dispense) {

        if (nonNull(dispense) && nonNull(dispense.getId())) {
            dispenseService.reIndex(dispense.getId());
            Page<Invoice> invoiceList = invoiceService.search("invoiceDocument.source.id:" + dispense.getId(), PageRequest.of(0, 9999));
            for (Invoice invoice : invoiceList) {
                invoiceService.reIndex(invoice.getId());
                Iterator<SponsorInvoice> sponsorInvoices = sponsorInvoiceSearchRepository.search(termQuery("sponsorDocument.source.id", invoice.getId())).iterator();
                while (sponsorInvoices.hasNext()) {
                    SponsorInvoice sponsorInvoice = sponsorInvoices.next();
                    sponsorInvoiceService.reIndex(sponsorInvoice.getId());
                }
                Iterator<InvoiceReceipt> invoiceReceipts = invoiceReceiptSearchRepository.search(termQuery("invoiceId", invoice.getId())).iterator();
                while (invoiceReceipts.hasNext()) {
                    InvoiceReceipt invoiceReceipt = invoiceReceipts.next();
                    invoiceReceiptService.reIndex(invoiceReceipt.getId());
                    Iterator<Receipt> receipts = receiptSearchRepository.search(termQuery("id", invoiceReceipt.getReceiptId())).iterator();
                    while (receipts.hasNext()) {
                        receiptService.reIndex(receipts.next().getId());
                    }
                }
            }
        }
    }

    /**
     * Create sponsor invoice by dispense plans
     *
     * @param dispense
     * @throws SequenceGenerateException
     */
    private List<SponsorInvoice> createSponsorInvoice(Dispense dispense) throws SequenceGenerateException {
        List<SponsorInvoice> sponsorInvoices = new ArrayList<>();
        List<DispensePlan> dispensePlans = dispense.getDocument().getDispensePlans();
        if (dispensePlans != null && !dispensePlans.isEmpty()) {
            for (DispensePlan dispensePlan : dispensePlans) {
                if (gtZero(dispensePlan.getSponsorGrossAmount())) {
                    SponsorInvoice sponsorInvoice = sponsorInvoiceMapper.convertDispenseToSponsorInvoice(dispense);
                    sponsorInvoice.getSponsorDocument().setPlanRef(dispensePlan.getPlanRef());
                    sponsorInvoice.getSponsorDocument().setPreparedBy(dispense.getDocument().getCreatedBy().getId());
                    sponsorInvoice.getSponsorDocument().setPreparedDate(LocalDateTime.now());
                    sponsorInvoice.getSponsorDocument().setSponsorRef(dispensePlan.getSponsorRef());
                    sponsorInvoice.getSponsorDocument().setSponsorGrossAmount(dispensePlan.getSponsorGrossAmount());
                    sponsorInvoice.getSponsorDocument().setSponsorDiscount(dispensePlan.getSponsorDiscount());
                    sponsorInvoice.getSponsorDocument().setRoundOff(dispensePlan.getRoundOff());
                    sponsorInvoice.getSponsorDocument().setSponsorPayable(dispensePlan.getSponsorPayable());
                    sponsorInvoice.getSponsorDocument().setPatientDiscount(dispensePlan.getPatientDiscount());
                    sponsorInvoice.getSponsorDocument().setSource(dispense.getDocument().getSource());
                    sponsorInvoice.getSponsorDocument().setHsc(dispense.getDocument().getHsc());
                    sponsorInvoiceService.createSponsorInvoice(sponsorInvoice, "CONFIRMED", "Sponsor_Invoice");
                    sponsorInvoices.add(sponsorInvoice);
                }
            }
        }
        return sponsorInvoices;
    }

    private void deleteAndReserveStock(Dispense dispense) throws StockException {
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

    /**
     * To reserve stock
     *
     * @param dispense
     * @throws Exception
     */
    private void reserveStock(Dispense dispense) throws Exception {
        List<DispenseDocumentLine> lines = dispense.getDocument().getDispenseDocumentLines();
        for (DispenseDocumentLine dispenseDocumentLine : lines) {
            if (dispenseDocumentLine.getQuantity() > 0) {
                stockService.reserveStock(dispenseDocumentLine.getStockId(), dispenseDocumentLine.getItemId(), dispenseDocumentLine.getBatchNumber(),
                    dispense.getDocument().getHsc().getId(), dispenseDocumentLine.getQuantity(), dispense.getId(),
                    TransactionType.Dispense, dispense.getDocumentNumber(),
                    dispenseDocumentLine.getLineNumber(), dispense.getDocument().getDispenseDate(),dispense.getDocument().getCreatedBy().getId());
            }
        }
    }

    /**
     * Verify user discount amount for approval. This method is called from workflow.
     *
     * @param dispenseId
     * @return boolean
     */
    @Override
    public Boolean isApprovalRequired(Long dispenseId) {
        log.debug("Request to verify user discount for :{}", dispenseId);
        return isApprovalRequired(dispenseService.findOne(dispenseId));
    }

    /**
     * Verify user discount amount for approval
     *
     * @param dispense
     * @return boolean
     */
    public Boolean isApprovalRequired(Dispense dispense) {
        log.debug("Request to verify user discount for :{}", dispense.getId());
        return gtZero(dispense.getDocument().getUserDiscountAmount()) || gtZero(dispense.getDocument().getTaxDiscount());
    }

    /**
     * Confirm dispense. This method is called from workflow.
     *
     * @param dispenseId
     * @return result map
     * @throws Exception
     */
    @Override
    public Map<String, Object> confirmDispense(Long dispenseId) throws Exception {
        Dispense dispense = dispenseService.findOne(dispenseId);
        return saveDispenseWithAction(dispense, "CONFIRMED");
    }

    /**
     * Start workflow
     *
     * @param dispense
     * @Param configurations
     */
    public void startWorkflow(Dispense dispense, Map configurations) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        //Get the deployed unit
        DeployedUnit deployedUnit = workflowService.getDeployedUnit();
        if (deployedUnit != null) {
            //Set Content
            content.put("document_id", dispense.getId());
            content.put("document_type", dispense.getDocument().getDocumentType());
            content.put("group_id", configurations.get("groupIds"));
            //Set Params
            params.put("user_id", userId);
            params.put("group_id", configurations.get("groupIds"));
            params.put("document_id", dispense.getId());
            params.put("document_number", dispense.getDocumentNumber());
            params.put("patient_name", (dispense.getDocument().getPatient().getDisplayName()==null || dispense.getDocument().getPatient().getDisplayName().isEmpty() )? (dispense.getDocument().getPatient().getFullName()==null || dispense.getDocument().getPatient().getFullName().isEmpty())?"":dispense.getDocument().getPatient().getFullName() : dispense.getDocument().getPatient().getDisplayName());
            params.put("mrn", dispense.getDocument().getPatient().getMrn());
            params.put("dispense_date", dispense.getDocument().getDispenseDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm")));
            params.put("unit_id", String.valueOf(dispense.getDocument().getDispenseUnit().getId()));
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
     * @param dispense   the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return result map
     * @throws Exception
     */
    @Override
    @Transactional
    @PamIntegration
    @PublishStockTransaction
    @PublishPharmacyMedicationRequest
    public Map<String, Object> executeWorkflow(Dispense dispense, String transition, Long taskId) throws Exception {
        Map<String, Object> result;
        String action;
        String userId = SecurityUtils.getCurrentUserLogin().get();
        switch (transition) {
            case "Approved":
                validateDocumentApprover(dispense);
                action = "APPROVED";
                break;
            case "Rejected":
                validateDocumentApprover(dispense);
                action = "REJECTED";
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }

        result = saveDispenseWithAction(dispense, action);
        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("transition_out", transition);
        workflowService.completeUserTask(taskId, userId, results);
        return result;
    }

    public void validateDocumentApprover(Dispense dispense) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(dispense, "dispense_document_approver_validation");
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
        Dispense dispense = dispenseRepository.findOneByDocumentNumber(documentNumber);
        configurations = retrieveWorkflowConfigurations(dispense, false);
        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            String createdBy = dispense.getDocument().getCreatedBy() != null ? dispense.getDocument().getCreatedBy().getLogin() : null;
//            taskDetails = workflowService.getTaskConstraints(taskId, processId, "document_number", documentNumber, userId, createdBy);
            taskDetails = taskId != null ? workflowService.getTaskConstraintsByTaskId(taskId) : workflowService.getTaskConstraints(taskId,processId, "document_number", documentNumber, userId,dispense.getDocument().getCreatedBy().getLogin());
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
     * @param dispense
     * @return configuration map
     * @Param isStartWorkflow
     */
    public Map<String, Object> retrieveWorkflowConfigurations(Dispense dispense, boolean isStartWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_enable_workflow", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtil.getConfigurationData("athma_pharmacy_dispense_workflow_definition", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        if (isStartWorkflow) {
            configurations.put("groupIds", getGroupData(Discount_Approval_Committee, dispense.getDocument().getDispenseUnit().getId()));
            configurations.put("dateFormat", ConfigurationUtil.getConfigurationData("athma_date_format", dispense.getDocument().getHsc().getId(), dispense.getDocument().getDispenseUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService));
        }
        return configurations;
    }

    /**
     * Create sponsor invoice by dispense plans
     *
     * @param dispenseReturn
     * @throws SequenceGenerateException
     */
    public List<SponsorInvoiceDTO> createSponsorInvoiceForDispenseReturn(DispenseReturn dispenseReturn) throws SequenceGenerateException {
        List<DispensePlan> dispensePlans = dispenseReturn.getDocument().getDispensePlans();
        List<SponsorInvoice> sponsorInvoices = sponsorInvoiceSearchRepository.findByinvoiceNumber(dispenseReturn.getDocument().getInvoiceRef().getReferenceNumber());
        List<SponsorInvoiceDTO> sponsorInvoicesReturn = new ArrayList<>();
        if (dispensePlans != null && !dispensePlans.isEmpty()) {
            for (DispensePlan dispensePlan : dispensePlans) {
                String sponsorInvoiceNumber = getSponsorInvoice(sponsorInvoices, dispensePlan.getPlanRef().getCode());
                SponsorInvoice sponsorInvoice = dispenseReturnToSponsorInvoiceMapper.convertDispenseReturnToSponsorInvoice(dispenseReturn);
                sponsorInvoice.getSponsorDocument().setSponsorInvoiceType(SponsorInvoiceType.REVERSAL);
                sponsorInvoice.getSponsorDocument().setPlanRef(dispensePlan.getPlanRef());
                sponsorInvoice.getSponsorDocument().setSponsorRef(dispensePlan.getSponsorRef());
                sponsorInvoice.getSponsorDocument().setSponsorGrossAmount(dispensePlan.getSponsorGrossAmount());
                sponsorInvoice.getSponsorDocument().setSponsorDiscount(dispensePlan.getSponsorDiscount());
                sponsorInvoice.getSponsorDocument().setRoundOff(dispensePlan.getRoundOff());
                sponsorInvoice.getSponsorDocument().setSponsorPayable(dispensePlan.getSponsorPayable());
                sponsorInvoice.getSponsorDocument().setPatientDiscount(dispensePlan.getPatientDiscount());
                sponsorInvoice.getSponsorDocument().setSource(dispenseReturn.getDocument().getCancelledInvoiceRef());
                sponsorInvoice.getSponsorDocument().setHsc(dispenseReturn.getDocument().getHsc());
                sponsorInvoice.getSponsorDocument().setOriginalSponsorInvoiceNumber(sponsorInvoiceNumber);
                if (CollectionUtils.isEmpty(sponsorInvoice.getSponsorDocument().getSourceDTOList())) {
                    sponsorInvoice.getSponsorDocument().setSourceDTOList(new ArrayList<>());
                }
                sponsorInvoice.getSponsorDocument().setSponsorInvoiceTaxes(dispenseReturnToSponsorInvoiceMapper.convertDispenseReturnTaxToSponsorInvoiceTax(dispensePlan.getPlanTaxList()));
                sponsorInvoice.getSponsorDocument().getSourceDTOList().add(dispenseReturn.getDocument().getCancelledInvoiceRef());
                sponsorInvoiceService.createSponsorInvoice(sponsorInvoice, "CONFIRMED", "Sponsor_Invoice_Dispense_Returned");
                sponsorInvoicesReturn.add(addReference(sponsorInvoice));
            }
        }
        return sponsorInvoicesReturn;
    }

    private SponsorInvoiceDTO addReference(SponsorInvoice sponsorInvoice) {
        SponsorInvoiceDTO sponsorInvoiceRef = new SponsorInvoiceDTO();
        sponsorInvoiceRef.setId(sponsorInvoice.getId());
        sponsorInvoiceRef.setSponsorInvoiceNumber(sponsorInvoice.getSponsorInvoiceNumber());
        sponsorInvoiceRef.setSponsorDocument(new SponsorDocumentDTO());
        return sponsorInvoiceRef;
    }


    private String getSponsorInvoice(List<SponsorInvoice> sponsorInvoices, final String planName) {
        for (SponsorInvoice sponsorInvoice : sponsorInvoices) {
            if (sponsorInvoice.getSponsorDocument().getPlanRef().getCode().equals(planName)) {
                return sponsorInvoice.getSponsorInvoiceNumber();
            }
        }
        return null;
    }

    public String generateTransactionNumber() throws SequenceGenerateException {
        return sequenceGeneratorService.generateSequence("Plutus_Transaction_Number", "NH", null);
    }

    private boolean isGroupExist(Dispense dispense){
       String group= getGroupData(Discount_Approval_Committee, dispense.getDocument().getDispenseUnit().getId());
       if(null!=group && !group.isEmpty())
           return  true;
       return false;
    }

    @Override
    public void regenerateWorkflow(String documentNumber) {
        Page<Dispense> search = this.dispenseService.search("documentNumber.raw:" + documentNumber, PageRequest.of(0, 1));
        if (!search.hasContent()) {
            return;
        }
        this.pharmacyWorkflowService.clearProcessInstance(documentNumber);
        this.callWorkflow(search.iterator().next());
    }

    /**
     * Publish Dispense
     * @param dispense
     */
    private void publishDispenseDocument(Dispense dispense, String docBasePath) {
        log.debug("Request to publishing dispense return to DMS: {}", dispense);
        DocumentRecordDTO documentRecord = dispenseToDocumentRecordMapper.dispenseToDocumentRecord(dispense, docBasePath);
        documentRecordService.produce(documentRecord);
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

}
