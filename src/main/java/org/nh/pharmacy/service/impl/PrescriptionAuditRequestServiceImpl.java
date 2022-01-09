package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jbpm.services.api.model.DeployedUnit;
import org.nh.billing.domain.Invoice;
import org.nh.billing.web.rest.util.ConfigurationUtility;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.util.ExportUtil;
import org.nh.common.util.ExportUtilConstant;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.jbpm.service.RuleExecutorService;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.nh.pharmacy.domain.dto.MedicationRequestDocumentLine;
import org.nh.pharmacy.domain.dto.PendingAuditRequestDocument;
import org.nh.pharmacy.domain.dto.PrescriptionAuditRequestDTO;
import org.nh.pharmacy.domain.enumeration.MedicationRequestStatus;
import org.nh.pharmacy.domain.enumeration.PendingAuditRequestStatus;
import org.nh.pharmacy.repository.ItemRepository;
import org.nh.pharmacy.repository.PrescriptionAuditRequestRepository;
import org.nh.pharmacy.repository.search.PrescriptionAuditRequestSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.*;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.mapper.MedicationReqToPendingAuditReqMapper;
import org.nh.pharmacy.web.rest.util.EmptyPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.disjoint;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing MedicationRequest.
 */
@Service("prescriptionAuditRequestService")
@Transactional
public class PrescriptionAuditRequestServiceImpl implements PrescriptionAuditRequestService {

    private final Logger log = LoggerFactory.getLogger(PrescriptionAuditRequestServiceImpl.class);

    private final PrescriptionAuditRequestRepository prescriptionAuditRequestRepository;

    private final PrescriptionAuditRequestSearchRepository prescriptionAuditRequestSearchRepository;

    private final ApplicationProperties applicationProperties;

    private final MedicationReqToPendingAuditReqMapper medicationReqToPendingAuditReqMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final WorkflowService workflowService;

    private final GroupService groupService;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    private final RuleExecutorService ruleExecutorService;

    private final PrescriptionAuditReqNotificationService prescriptionAuditReqNotificationService;

    private final ItemService itemService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final MedicationRequestService medicationRequestService;

    private final ItemRepository itemRepository;


    public PrescriptionAuditRequestServiceImpl(PrescriptionAuditRequestRepository prescriptionAuditRequestRepository, PrescriptionAuditRequestSearchRepository prescriptionAuditRequestSearchRepository, ApplicationProperties applicationProperties, MedicationReqToPendingAuditReqMapper medicationReqToPendingAuditReqMapper, ElasticsearchOperations elasticsearchTemplate, WorkflowService workflowService, GroupService groupService, PharmacyRedisCacheService pharmacyRedisCacheService, RuleExecutorService ruleExecutorService, PrescriptionAuditReqNotificationService prescriptionAuditReqNotificationService, ItemService itemService, ApplicationEventPublisher applicationEventPublisher, MedicationRequestService medicationRequestService, ItemRepository itemRepository) {
        this.prescriptionAuditRequestRepository = prescriptionAuditRequestRepository;
        this.prescriptionAuditRequestSearchRepository = prescriptionAuditRequestSearchRepository;
        this.applicationProperties = applicationProperties;
        this.medicationReqToPendingAuditReqMapper = medicationReqToPendingAuditReqMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.workflowService = workflowService;
        this.groupService = groupService;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.ruleExecutorService = ruleExecutorService;
        this.prescriptionAuditReqNotificationService = prescriptionAuditReqNotificationService;
        this.itemService = itemService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.medicationRequestService = medicationRequestService;
        this.itemRepository = itemRepository;
    }

    /**
     * Save a prescriptionAuditRequest.
     *
     * @param prescriptionAuditRequest the entity to save
     * @return the persisted entity
     */
    public PrescriptionAuditRequest save(PrescriptionAuditRequest prescriptionAuditRequest) {
        log.debug("Request to save PrescriptionAuditRequest : {}", prescriptionAuditRequest);
        //using flush for updated db version in elastic
        PrescriptionAuditRequest result = prescriptionAuditRequestRepository.saveAndFlush(prescriptionAuditRequest);
        prescriptionAuditRequestSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the pendingAuditRequest.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionAuditRequest> findAll(Pageable pageable) {
        log.debug("Request to get all PrescriptionAuditRequest");
        Page<PrescriptionAuditRequest> result = prescriptionAuditRequestRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one medicationRequest by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PrescriptionAuditRequest findOne(Long id) {
        log.debug("Request to get PrescriptionAuditRequest : {}", id);
        Optional<PrescriptionAuditRequest> pendingAuditRequest = prescriptionAuditRequestRepository.findById(id);
        return pendingAuditRequest.orElse(null);
    }

    /**
     * Delete the  medicationRequest by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete PrescriptionAuditRequest : {}", id);
        prescriptionAuditRequestRepository.deleteById(id);
        prescriptionAuditRequestSearchRepository.deleteById(id);
    }

    /**
     * Search for the medicationRequest corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionAuditRequest> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of MedicationRequests for query {}", query);
        Page<PrescriptionAuditRequest> result = prescriptionAuditRequestSearchRepository.search(queryStringQuery(query)
                .field("documentNumber").field("document.patient.displayName")
                .field("document.patient.mrn").field("document.medicationRequestStatus")
                .field("document.orderingHSC.name").field("document.orderingHSC.code")
                .field("document.createdBy.login").field("document.createdBy.displayName")
                .field("document.consultant.name").field("document.consultant.displayName")
                .field("document.patientStatus").field("document.priority")
                .field("document.encounter.visitNumber").field("document.pendingAuditRequestStatus")
                .defaultOperator(Operator.AND)
            , pageable);
        return result;
    }


    /***
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Pending Audit Request");
        prescriptionAuditRequestSearchRepository.deleteAll();
    }

    /****
     *
     * @param pageNo
     * @param pageSize
     */
    @Override
    @Transactional(readOnly = true)
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to do elastic index on Medication Request latest=true");
        List<PrescriptionAuditRequest> data = prescriptionAuditRequestRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            prescriptionAuditRequestSearchRepository.saveAll(data);
        }
    }

    /* */

    /**
     * This method is used to update the pharmacy medication request after dispense is happened
     *
     * @param
     *//*
    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMedicationRequest(SourceDTO sourceDTO) {
        log.debug("received message to update medication request after dispense. Source Dto ={}",sourceDTO);
        log.info("received message to update medication request after dispense. medication request number ={}",sourceDTO.getReferenceNumber());
        try
        {
            MedicationRequest medicationRequest  =  medicationRequestRepository.findByDocumentNumber(sourceDTO.getReferenceNumber());

            if(null == medicationRequest)
                return;

            if(MedicationRequestStatus.DISPENSED.equals(medicationRequest.getDocument().getMedicationRequestStatus()))
                throw new CustomParameterizedException("Medication request is already dispensed");

            Map<String,Object> otherInfo = sourceDTO.getOtherDetails();
            Boolean partiallyDispensed= Boolean.FALSE;
            Boolean ordered = Boolean.FALSE;
            boolean dispensed = false;

            for(MedicationRequestDocumentLine documentLine :medicationRequest.getDocument().getDocumentLines())
            {
                Map<String,Float> issuedQuantity = (Map<String,Float>)otherInfo.get("issuedQuantity");
                log.debug("issued quantity map ={}, document line id ={}",issuedQuantity,documentLine.getDocumentLineId());
                if(issuedQuantity.containsKey(String.valueOf(documentLine.getDocumentLineId()))) {
                    log.debug("Updating issued quantity for line item ={}",documentLine.getDocumentLineId());
                    DispenseRequest dispenseRequest = documentLine.getDispenseRequest();
                    Float quantity = 0f;
                    if(null != dispenseRequest.getIssuedQuantity())
                        quantity = dispenseRequest.getIssuedQuantity();
                    quantity = quantity+Double.valueOf(String.valueOf(issuedQuantity.get(String.valueOf(documentLine.getDocumentLineId())))).floatValue();
                    dispenseRequest.setIssuedQuantity(quantity);
                    documentLine.getDispenseRequest().setIssuedQuantity(quantity);
                    if(dispenseRequest.getIssuedQuantity() > dispenseRequest.getQuantity() && BillingType.POST_BILLING.equals(medicationRequest.getDocument().getEncounter().getBillingType())) {
                        throw new CustomParameterizedException("Medication request items are already dispensed");
                    }
                    if(dispenseRequest.getIssuedQuantity() >= dispenseRequest.getQuantity()) {
                        documentLine.setStatus(MedicationRequestStatus.DISPENSED.name());
                        dispensed =true;
                    }
                    else if(dispenseRequest.getIssuedQuantity() > 0) {
                        documentLine.setStatus(MedicationRequestStatus.PARTIALLY_DISPENSED.name());
                        partiallyDispensed= Boolean.TRUE;
                    }
                }
                if(MedicationRequestStatus.ORDERED.name().equalsIgnoreCase(documentLine.getStatus()))
                    ordered = Boolean.TRUE;
                else if(MedicationRequestStatus.DISPENSED.name().equalsIgnoreCase(documentLine.getStatus()))
                    dispensed = Boolean.TRUE;
                else if(MedicationRequestStatus.PARTIALLY_DISPENSED.name().equalsIgnoreCase(documentLine.getStatus()))
                    partiallyDispensed = Boolean.TRUE;

                log.debug("Updating medication order with medication request id={}, medication order document line id={}",medicationRequest.getId(),documentLine.getDocumentLineId());
                updateMedicationOrder(medicationRequest,documentLine);
                log.debug("Medication order is updated successfully");
            }
            if(!partiallyDispensed && !ordered)
                medicationRequest.getDocument().setMedicationRequestStatus(org.nh.pharmacy.domain.enumeration.MedicationRequestStatus.DISPENSED);
            if(partiallyDispensed || (dispensed && ordered))
                medicationRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.PARTIALLY_DISPENSED);
            save(medicationRequest);
        }catch (Exception ex)
        {
            log.error("Error while updating the medication request. medication request number = {}, Ex={}",ex,sourceDTO.getReferenceNumber());
            reIndex(sourceDTO.getId());
            throw ex;
        }

    }*/
    @Override
    public void reIndex(Long id) {
        log.debug("Request to do re-index for pending audit request with id : {}", id);
        if (null != id) {
            Optional<PrescriptionAuditRequest> pendingAuditRequest = prescriptionAuditRequestRepository.findById(id);
            if (!pendingAuditRequest.isPresent()) {
                if (prescriptionAuditRequestSearchRepository.existsById(id)) {
                    prescriptionAuditRequestSearchRepository.deleteById(id);
                }
            } else {
                prescriptionAuditRequestSearchRepository.save(pendingAuditRequest.get());
            }
        }
        log.debug("Request to do reIndex pending audit request ends : {}", LocalTime.now());
    }

    public Map<String, String> exportPendingAudits(String query, Pageable pageable) throws IOException {
        log.debug("Request to export prescription audit requests in CSV File query {}", query);
        File file = ExportUtil.getCSVExportFile("PrescriptionAuditRequest", applicationProperties.getAthmaBucket().getTempExport(), SecurityUtils.getCurrentUserLogin().get());
        FileWriter pharmacyRequestFileWriter = new FileWriter(file);
        Map<String, String> pharmacyRequestFileDetails = new HashMap<>();
        pharmacyRequestFileDetails.put(ExportUtilConstant.FILE_NAME, file.getName());
        pharmacyRequestFileDetails.put(ExportUtilConstant.PATH_REFERENCE, ExportUtilConstant.TEMP_EXPORT);

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(pharmacyRequestFileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord((Object[]) PharmacyConstants.PRESCRIPTION_AUDIT_REQUEST_HEADER);
            Iterator<PrescriptionAuditRequest> pendingAuditRequestIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();

            while (pendingAuditRequestIterator.hasNext()) {
                PrescriptionAuditRequest prescriptionAuditRequest = pendingAuditRequestIterator.next();
                PendingAuditRequestDocument medicationRequestDocument = prescriptionAuditRequest.getDocument();
                List<MedicationRequestDocumentLine> medicationRequestDocumentLines = medicationRequestDocument.getDocumentLines();
                List<Object> medicationRequestRow = new ArrayList();
                medicationRequestRow.add(prescriptionAuditRequest.getDocumentNumber());
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getMrn() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getDisplayName() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getGender() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getAge() : "");
                medicationRequestRow.add(prescriptionAuditRequest.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                medicationRequestRow.add(medicationRequestDocument.getConsultant() != null ? medicationRequestDocument.getConsultant().getDisplayName() : "");
                medicationRequestRow.add(medicationRequestDocument.getDepartment() != null ? medicationRequestDocument.getDepartment().getName() : "");
                medicationRequestRow.add(medicationRequestDocument.getCreatedBy() != null ? medicationRequestDocument.getCreatedBy().getDisplayName() : "");
                medicationRequestRow.add(medicationRequestDocument.getAuditBy() != null ? medicationRequestDocument.getAuditBy().getDisplayName() : "");
                medicationRequestRow.add(medicationRequestDocument.getAuditDate() != null ? prescriptionAuditRequest.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "");
                medicationRequestRow.add(medicationRequestDocument.isModified());
                medicationRequestRow.add(null == medicationRequestDocument.getPendingAuditRequestStatus() ? "" : medicationRequestDocument.getPendingAuditRequestStatus().getDisplayStatus());
                csvFilePrinter.printRecord(medicationRequestRow);

            }
        } catch (Exception ex) {
            log.error("error while exporting pending audit requests. Ex: {} ",ex);
        } finally {
            pharmacyRequestFileWriter.close();
        }

        return pharmacyRequestFileDetails;
    }

    @Override
    public void handleMedicationRequestInput(MedicationRequest medicationRequest) {
        String query = "documentNumber.raw:" + medicationRequest.getDocumentNumber();
        List<PrescriptionAuditRequest> prescriptionAuditRequests = search(query, PageRequest.of(0, 1)).getContent();
        if (CollectionUtils.isNotEmpty(prescriptionAuditRequests)) {
            PrescriptionAuditRequest prescriptionAuditRequest = prescriptionAuditRequests.get(0);
            getPrescriptionAuditCreateFlag(medicationRequest);
            prescriptionAuditRequest.getDocument().setDocumentLines(medicationRequest.getDocument().getDocumentLines());
            if(MedicationRequestStatus.CANCELLED.equals(medicationRequest.getDocument().getMedicationRequestStatus())) {
                prescriptionAuditRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.CANCELLED);
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.CANCELLED);
            }
            save(prescriptionAuditRequest);
        } else {
            //create pending audit from medication request.
            boolean createPrescriptionAudit;
            try {
                createPrescriptionAudit = getPrescriptionAuditCreateFlag(medicationRequest);
            }catch (Exception ex)
            {
                log.error("error while fetching configuration audit configuration data. ex: {} ", ex);
                createPrescriptionAudit= false;
            }
            if(createPrescriptionAudit) {
                PrescriptionAuditRequest prescriptionAuditRequest = medicationReqToPendingAuditReqMapper.medicationRequestToPendingAuditRequest(medicationRequest);
                prescriptionAuditRequest.getDocument().getDocumentLines().forEach(medicationRequestDocumentLine -> {
                    if (null == medicationRequestDocumentLine.getPendingAuditDocLineId() && null != medicationRequestDocumentLine.getDocumentLineId())
                        medicationRequestDocumentLine.setPendingAuditDocLineId(medicationRequestDocumentLine.getDocumentLineId());
                });
                PrescriptionAuditRequest result = save(prescriptionAuditRequest);
                initiateWorkflow(result, true);
            }
        }
    }

    private boolean getPrescriptionAuditCreateFlag(MedicationRequest savedMedicationRequest) {
        List<String> itemIds = savedMedicationRequest.getDocument().getDocumentLines().stream()
            .filter(medicationRequestDocumentLine -> null != medicationRequestDocumentLine.getMedication().getCode()).collect(Collectors.toList()).stream()
            .map(medicationRequestDocumentLine -> medicationRequestDocumentLine.getMedication().getCode())
            .collect(Collectors.toList());

        //medication ids are not there. so no need to create prescription audit flag
        if(CollectionUtils.isEmpty(itemIds))
            return false;

        Long hscId = savedMedicationRequest.getDocument().getOrderingHSC().getId();
        Long unitId = savedMedicationRequest.getDocument().getUnit().getId();

        String prescriptionAudit ="";
        try {
            prescriptionAudit = (String) ConfigurationUtility.getConfiguration("Prescription_Audit", hscId, unitId, null, elasticsearchTemplate, "code", "value", savedMedicationRequest.getDocument().getEncounter().getEncounterClass().getCode(),true);
        }catch (Exception ex)
        {
            log.error("configuration not found for prescription audit in medication request. Document number:{}", savedMedicationRequest.getDocumentNumber());
        }

        if(StringUtils.isNotBlank(prescriptionAudit)) {
            List<String> itemCodes = new ArrayList<>();
            for (String itemCode : itemIds) {
                String code = "("+QueryParser.escape(itemCode)+")";
                itemCodes.add(code);
            }
            String query = "code.raw:("+ StringUtils.join(itemCodes, " OR ")+") AND type.code:DRUG";
            List<Item> drugItems = itemService.search(query, PageRequest.of(0, savedMedicationRequest.getDocument().getDocumentLines().size())).getContent();
            if (CollectionUtils.isNotEmpty(drugItems)) {
                for (MedicationRequestDocumentLine medicationRequestDocumentLine : savedMedicationRequest.getDocument().getDocumentLines()) {
                    Item drugItem = drugItems.stream().filter(item -> item.getCode().equals(medicationRequestDocumentLine.getMedication().getCode())).findFirst().orElse(null);
                    if (null != drugItem)
                        medicationRequestDocumentLine.setDrugItem(true);
                }
                if (StringUtils.isNotBlank(prescriptionAudit))
                    return true;
            } else {
                //check if any generic items are there in the request and of type drug
                List<MedicationRequestDocumentLine> genericItems = savedMedicationRequest.getDocument().getDocumentLines().stream()
                    .filter(documentLine -> null != documentLine.getMedication().getBrand() && !Boolean.TRUE.equals(documentLine.getMedication().getBrand())
                        && StringUtils.isNotBlank(documentLine.getMedication().getName())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(genericItems)) {
                    try {
                        List<String> genericItemNames = genericItems.stream().map(documentLine -> documentLine.getMedication().getName()).collect(Collectors.toList());
                        List<Item> itemsWithGenericName = itemRepository.findItemsWithGenericName(genericItemNames);
                        Item drugItem = itemsWithGenericName.stream().filter(item -> "DRUG".equalsIgnoreCase(item.getType().getCode())).findFirst().orElse(null);
                        for (MedicationRequestDocumentLine medicationRequestDocumentLine : savedMedicationRequest.getDocument().getDocumentLines()) {
                            Item drug = itemsWithGenericName.stream().filter(item -> item.getDispensableGenericName().equals(medicationRequestDocumentLine.getMedication().getName())).findFirst().orElse(null);
                            if (null != drug)
                                medicationRequestDocumentLine.setDrugItem(true);
                        }
                        if (null != drugItem) {
                            if (StringUtils.isNotBlank(prescriptionAudit)) {
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                        log.error("error while finding dispensable generic items with drug type. ex: {}", ex);
                    }

                }
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getTaskConstraintsForPrescriptionAudit(String prescriptionAuditDocNumber, String userId, Long taskId) {
        Map<String, Object> configurations, taskDetails;
        String query = "documentNumber.raw:" + prescriptionAuditDocNumber;

        List<PrescriptionAuditRequest> prescriptionAuditRequests = search(query, PageRequest.of(0, 1)).getContent();
        if (CollectionUtils.isEmpty(prescriptionAuditRequests))
            throw new CustomParameterizedException("Prescription Audit Request not found with document number: " + prescriptionAuditDocNumber);

        PrescriptionAuditRequest prescriptionAuditRequest = prescriptionAuditRequests.get(0);
        configurations = retrieveWorkflowConfigurationsForPrecriptionAudit(prescriptionAuditRequest, true);

        if ((Boolean) configurations.get("enableWorkflow")) {
            String processId = (String) configurations.get("processId");
            String createdBy = prescriptionAuditRequest.getDocument().getCreatedBy() != null ? prescriptionAuditRequest.getDocument().getCreatedBy().getLogin() : null;
            taskDetails = workflowService.getTaskConstraints(taskId, processId, "pa_request_number", prescriptionAuditDocNumber, userId, createdBy);
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

    @Override
    public PrescriptionAuditRequest executeWorkflowForPrescriptionAudit(PrescriptionAuditRequest prescriptionAuditRequest, String transition, Long taskId) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        MedicationRequest medicationRequest=null;
        switch (transition) {
            case Constants.APPROVED:
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.APPROVED);
                prescriptionAuditRequest.getDocument().setAuditBy(loadLoggedInUser());
                prescriptionAuditRequest.getDocument().setAuditDate(LocalDateTime.now());
                medicationRequest= updateMedicationRequestWithPAChanges(prescriptionAuditRequest);
                break;
            case Constants.REJECTED:
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.CANCELLED);
                prescriptionAuditRequest.getDocument().setAuditBy(loadLoggedInUser());
                prescriptionAuditRequest.getDocument().setAuditDate(LocalDateTime.now());
                prescriptionAuditRequest.getDocument().getDocumentLines().forEach(medicationRequestDocumentLine ->
                {
                    medicationRequestDocumentLine.setStatus(MedicationRequestStatus.CANCELLED.name());
                });
                prescriptionAuditRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.CANCELLED);
                medicationRequest= updateMedicationRequestWithPAChanges(prescriptionAuditRequest);
                break;
            case Constants.SEND_FOR_APPROVAL:
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.PENDING_APPROVAL);
                break;
            case Constants.SEND_FOR_PA_APPROVAL:
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }

        //Complete the task
        Map<String, Object> results = new HashMap<>();
        results.put("transition_out", transition);
        PrescriptionAuditRequest result = save(prescriptionAuditRequest);
        workflowService.completeUserTask(taskId, userId, results);
        if(transition.equalsIgnoreCase(Constants.APPROVED) || transition.equalsIgnoreCase(Constants.REJECTED)) {
            PrescriptionAuditRequestDTO prescriptionAuditRequestDTO = new PrescriptionAuditRequestDTO();
            prescriptionAuditRequestDTO.setPrescriptionAuditRequest(prescriptionAuditRequest);
            prescriptionAuditRequestDTO.setTransition(transition);
            prescriptionAuditRequestDTO.setMedicatonRequest(medicationRequest);
            applicationEventPublisher.publishEvent(prescriptionAuditRequestDTO);
        }
        return result;
    }

    private MedicationRequest updateMedicationRequestWithPAChanges(PrescriptionAuditRequest prescriptionAuditRequest) {
        String query = "documentNumber.raw:"+prescriptionAuditRequest.getDocumentNumber();
        List<MedicationRequest> medicationRequests = medicationRequestService.search(query, PageRequest.of(0, 1)).getContent();
        if(CollectionUtils.isNotEmpty(medicationRequests))
        {
            MedicationRequest medicationRequest = medicationRequests.get(0);
            Map<Long,MedicationRequestDocumentLine> medicationDocumentLines= new HashMap<>();
            medicationRequest.getDocument().getDocumentLines().forEach( medicationDocumentLine -> {
                medicationDocumentLines.put(medicationDocumentLine.getDocumentLineId(),medicationDocumentLine);
            });
            List<MedicationRequestDocumentLine> newMedicationLines = new ArrayList<>();
            boolean orderedStatusFlag=false;
            for (MedicationRequestDocumentLine prescriptionAuditLine : prescriptionAuditRequest.getDocument().getDocumentLines()) {
                if (null != prescriptionAuditLine.getDocumentLineId() && 0L != prescriptionAuditLine.getDocumentLineId() && medicationDocumentLines.containsKey(prescriptionAuditLine.getDocumentLineId())) {
                    MedicationRequestDocumentLine medicationRequestDocumentLine = medicationDocumentLines.get(prescriptionAuditLine.getDocumentLineId());
                    medicationRequestDocumentLine.setDosageInstruction(prescriptionAuditLine.getDosageInstruction());
                    medicationRequestDocumentLine.setDispenseRequest(prescriptionAuditLine.getDispenseRequest());
                    medicationRequestDocumentLine.setStartDate(prescriptionAuditLine.getStartDate());
                    medicationRequestDocumentLine.setPriority(prescriptionAuditLine.getPriority());
                    medicationRequestDocumentLine.setStatus(prescriptionAuditLine.getStatus());
                    medicationRequestDocumentLine.setCancelledBy(prescriptionAuditLine.getCancelledBy());
                    medicationRequestDocumentLine.setCancelledDate(prescriptionAuditLine.getCancelledDate());
                    medicationRequestDocumentLine.setDuration(prescriptionAuditLine.getDuration());
                    medicationRequestDocumentLine.setModified(prescriptionAuditLine.isModified());
                    medicationRequestDocumentLine.setInstructions(prescriptionAuditLine.getInstructions());
                    medicationRequestDocumentLine.setRenderingHSC(prescriptionAuditLine.getRenderingHSC());
                    medicationRequestDocumentLine.setSubtitution(prescriptionAuditLine.getSubtitution());
                } else if (null == prescriptionAuditLine.getDocumentLineId()) {
                    newMedicationLines.add(prescriptionAuditLine);
                }
                if ("ORDERED".equalsIgnoreCase(prescriptionAuditLine.getStatus()))
                    orderedStatusFlag = true;
            }

            if(CollectionUtils.isNotEmpty(newMedicationLines))
            {
                log.debug("Adding new lines to existing medication request number: {} ", medicationRequest.getDocumentNumber());
                medicationRequest.getDocument().getDocumentLines().addAll(newMedicationLines);
            }
            if(PendingAuditRequestStatus.APPROVED.equals(prescriptionAuditRequest.getDocument().getPendingAuditRequestStatus()))
                medicationRequest.setPrescriptionAudited(true);
            medicationRequest.getDocument().setMedicationRequestStatus(prescriptionAuditRequest.getDocument().getMedicationRequestStatus());
            if(!orderedStatusFlag)
            {
                medicationRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.CANCELLED);
            }
            return medicationRequest;
        }
        return null;

    }

    @Override
    public PrescriptionAuditRequest executeWorkflowForPrescriptionAuditRequest(String prescriptionAuditRequestNumber, String transition, Long taskId) {
        String userId = SecurityUtils.getCurrentUserLogin().get();
        PrescriptionAuditRequest prescriptionAuditRequest = search("documentNumber.raw:" + prescriptionAuditRequestNumber, PageRequest.of(0, 1)).getContent().get(0);
        switch (transition) {
            case "Approved":
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.APPROVED);
                prescriptionAuditRequest.getDocument().setAuditBy(loadLoggedInUser());
                break;
            case "Rejected":
                prescriptionAuditRequest.getDocument().setPendingAuditRequestStatus(PendingAuditRequestStatus.REJECTED);
                prescriptionAuditRequest.getDocument().setAuditBy(loadLoggedInUser());
                prescriptionAuditRequest.getDocument().getDocumentLines().forEach(medicationRequestDocumentLine ->
                {
                    medicationRequestDocumentLine.setStatus(MedicationRequestStatus.CANCELLED.name());
                });
                prescriptionAuditRequest.getDocument().setMedicationRequestStatus(MedicationRequestStatus.CANCELLED);
                break;
            case "Send For Approval":
                break;
            case "Send For PA Approval":
                break;
            default:
                throw new IllegalStateException("Invalid transition: " + transition);
        }

        Map<String, Object> results = new HashMap<>();
        results.put("transition_out", transition);
        PrescriptionAuditRequest result = save(prescriptionAuditRequest);
        workflowService.completeUserTask(taskId, userId, results);
        return result;
    }

    private void validateDocumentApproverForPrescriptionAudit(Invoice invoice) throws BusinessRuleViolationException {
        ruleExecutorService.executeByGroup(invoice, "prescription_audit_document_approver_validation");
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


    private OrganizationDTO getOrderingDepartment(MedicationRequest createdMedicationRequest) {
        return null == createdMedicationRequest.getDocument().getOrderingDepartment() ? createdMedicationRequest.getDocument().getDepartment() : createdMedicationRequest.getDocument().getOrderingDepartment();
    }

    private Map<String, Object> retrieveWorkflowConfigurationsForPrecriptionAudit(PrescriptionAuditRequest prescriptionAuditRequest,boolean startWorkflow) {
        Map<String, Object> configurations = new HashMap<>();
        Long unitId = prescriptionAuditRequest.getDocument().getUnit().getId();
        Long hscId = prescriptionAuditRequest.getDocument().getOrderingHSC().getId();
        configurations.put("processId", ConfigurationUtility.getConfiguration("athma_phr_prescription_audit_workflow_definition", hscId, unitId, null, elasticsearchTemplate));
        configurations.put("enableWorkflow", startWorkflow);
        configurations.put("groupIds", ConfigurationUtility.getCommaSeparatedGroupCodes("Prescription_Audit_Request_Approval_Committee", unitId, elasticsearchTemplate));
        configurations.put("dateFormat", ConfigurationUtility.getConfiguration("athma_date_format", hscId, unitId, null, elasticsearchTemplate));
        return configurations;
    }


    //this method will be called from workflow
    public void approvePrescriptionAudit(Long prescriptionAuditRequestId) {
        log.info("prescription audit request is approved. id: {}", prescriptionAuditRequestId);
        //PrescriptionAuditRequest prescriptionAuditRequest = search("id:" + prescriptionAuditRequestId, PageRequest.of(0, 1)).getContent().get(0);
        //need to get and set medication request's pending audit flag
    }

    //this method will be called from workflow
    public void rejectPrescriptionAudit(Long prescriptionAuditRequestId) {
        log.info("prescription audit request is rejected. id: {}", prescriptionAuditRequestId);
        PrescriptionAuditRequest prescriptionAuditRequest = search("id:" + prescriptionAuditRequestId, PageRequest.of(0, 1)).getContent().get(0);
        prescriptionAuditReqNotificationService.notifyPrescriptionAuditRequestRejection(prescriptionAuditRequest);
        //need to get and set medication request's pending audit flag
        save(prescriptionAuditRequest);
    }

    private void initiateWorkflow(PrescriptionAuditRequest prescriptionAuditRequest, boolean isWorkflowEnabled) {

        Map<String, Object> configurations = new HashMap<>();
        Long unitId = prescriptionAuditRequest.getDocument().getUnit().getId();
        Long hscId = prescriptionAuditRequest.getDocument().getOrderingHSC().getId();
        //boolean isWorkflowEnabled = ("Yes".equals(ConfigurationUtility.getConfiguration("approval_required_for_invoice_retrospect", hscId, unitId, null, elasticsearchTemplate)));
        if (isWorkflowEnabled) {
            configurations.put("processId", ConfigurationUtility.getConfiguration("athma_phr_prescription_audit_workflow_definition", hscId, unitId, null, elasticsearchTemplate));
        }
        configurations.put("enableWorkflow", isWorkflowEnabled);
        configurations.put("groupIds", ConfigurationUtility.getCommaSeparatedGroupCodes("Prescription_Audit_Request_Approval_Committee", unitId, elasticsearchTemplate));
        configurations.put("dateFormat", ConfigurationUtility.getConfiguration("athma_date_format", hscId, unitId, null, elasticsearchTemplate));
        if (isWorkflowEnabled) {
            String userId = org.nh.billing.security.SecurityUtils.getCurrentUserLogin();
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> results = new HashMap<>();
            //Get the deployed unit
            DeployedUnit deployedUnit = workflowService.getDeployedUnit();
            if (deployedUnit != null) {
                PatientDTO patient = prescriptionAuditRequest.getDocument().getPatient();
                //Set Content
                content.put("source_id", prescriptionAuditRequest.getId());
                content.put("source_type", "Prescription_Audit_Request");
                //Set Params
                params.put("user_id", userId);
                params.put("created_date", String.valueOf(prescriptionAuditRequest.getDocument().getCreatedDate().format(ofPattern("" + configurations.get("dateFormat") + "','HH:mm"))));
                params.put("unit_id", String.valueOf(prescriptionAuditRequest.getDocument().getUnit() != null ? prescriptionAuditRequest.getDocument().getUnit().getId() : null));
                params.put("content", content);
                params.put("patient_name", (StringUtils.isBlank(patient.getDisplayName()) ? (StringUtils.isBlank(patient.getFullName()) ? "" : patient.getFullName()) : patient.getDisplayName()));
                params.put("mrn", patient.getMrn());
                params.put("prescription_audit_group", configurations.get("groupIds"));
                params.put("consultant_id", prescriptionAuditRequest.getDocument().getOrderingConsultant().getLogin());
                params.put("prescription_audit_creator", prescriptionAuditRequest.getDocument().getCreatedBy().getId());
                params.put("pa_request_number", prescriptionAuditRequest.getDocumentNumber());
                params.put("prescription_audit_request_id", prescriptionAuditRequest.getId());
                params.put("group_id", configurations.get("groupIds"));
                params.put("consultant_name",prescriptionAuditRequest.getDocument().getOrderingConsultant().getDisplayName());
                //Start the process
                workflowService.startProcess(deployedUnit, (String) configurations.get("processId"), params);
            }

        }
    }

    public Map<String, Long> getStatusCount(String query) {
        Map<String, Long> statusCount = new HashMap<>();
        Query searchQuery = new NativeSearchQueryBuilder()
            .withPageable(EmptyPage.INSTANCE)
            .withQuery(queryStringQuery(query)
                .field("documentNumber").field("document.patient.displayName")
                .field("document.patient.mrn").field("document.medicationRequestStatus")
                .field("document.orderingHSC.name").field("document.orderingHSC.code")
                .field("document.createdBy.login").field("document.createdBy.displayName")
                .field("document.consultant.name").field("document.consultant.displayName")
                .field("document.patientStatus").field("document.priority")
                .field("document.encounter.visitNumber")
                .defaultOperator(Operator.AND))

            .addAggregation(AggregationBuilders.terms("status_count").field("document.pendingAuditRequestStatus.raw"))
            .build();

        Aggregations aggregations = ElasticSearchUtil.getAggregations(searchQuery, elasticsearchTemplate, "prescriptionauditrequest");
        Terms terms = aggregations.get("status_count");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            statusCount.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return statusCount;
    }
}
