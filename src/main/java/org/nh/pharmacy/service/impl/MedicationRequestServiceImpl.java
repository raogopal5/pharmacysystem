package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.Operator;
import org.nh.billing.web.rest.util.ConfigurationUtility;
import org.nh.common.dto.ConsultantDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.enumeration.BillingType;
import org.nh.common.util.ExportUtil;
import org.nh.common.util.ExportUtilConstant;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.domain.MedicationOrder;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.nh.pharmacy.domain.dto.DispenseRequest;
import org.nh.pharmacy.domain.dto.MedicationOrderDocumentLine;
import org.nh.pharmacy.domain.dto.MedicationRequestDocument;
import org.nh.pharmacy.domain.dto.MedicationRequestDocumentLine;
import org.nh.pharmacy.domain.enumeration.MedicationOrderStatus;
import org.nh.pharmacy.domain.enumeration.MedicationRequestStatus;
import org.nh.pharmacy.domain.enumeration.PendingAuditRequestStatus;
import org.nh.pharmacy.repository.ItemRepository;
import org.nh.pharmacy.repository.MedicationRequestRepository;
import org.nh.pharmacy.repository.search.MedicationRequestSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.ItemService;
import org.nh.pharmacy.service.MedicationOrderService;
import org.nh.pharmacy.service.MedicationRequestService;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.mapper.MedicationReqDocLineToMedicationOrderDocLine;
import org.nh.pharmacy.web.rest.mapper.MedicationRequestToMedicationOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing MedicationRequest.
 */
@Service
@Transactional
public class MedicationRequestServiceImpl implements MedicationRequestService {

    private final Logger log = LoggerFactory.getLogger(MedicationRequestServiceImpl.class);

    private final MedicationRequestRepository medicationRequestRepository;

    private final MedicationRequestSearchRepository medicationRequestSearchRepository;

    private final ApplicationProperties applicationProperties;

    private final MedicationRequestToMedicationOrderMapper medicationRequestToMedicationOrderMapper;

    private final MedicationReqDocLineToMedicationOrderDocLine medicationReqDocLineToMedicationOrderDocLine;

    private final MedicationOrderService medicationOrderService;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ItemService itemService;

    private final ItemRepository itemRepository;

    public MedicationRequestServiceImpl(MedicationRequestRepository medicationRequestRepository, MedicationRequestSearchRepository medicationRequestSearchRepository, ApplicationProperties applicationProperties, MedicationRequestToMedicationOrderMapper medicationRequestToMedicationOrderMapper, MedicationReqDocLineToMedicationOrderDocLine medicationReqDocLineToMedicationOrderDocLine, MedicationOrderService medicationOrderService, ElasticsearchOperations elasticsearchTemplate, ItemService itemService, ItemRepository itemRepository) {
        this.medicationRequestRepository = medicationRequestRepository;
        this.medicationRequestSearchRepository = medicationRequestSearchRepository;
        this.applicationProperties = applicationProperties;
        this.medicationRequestToMedicationOrderMapper = medicationRequestToMedicationOrderMapper;
        this.medicationReqDocLineToMedicationOrderDocLine = medicationReqDocLineToMedicationOrderDocLine;
        this.medicationOrderService = medicationOrderService;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.itemService = itemService;
        this.itemRepository = itemRepository;
    }

    /**
     * Save a medicationRequest.;
     *
     * @param medicationRequest the entity to save
     * @return the persisted entity
     */
    public MedicationRequest save(MedicationRequest medicationRequest) {
        log.debug("Request to save MedicationRequest : {}", medicationRequest);
        MedicationRequest result = medicationRequestRepository.save(medicationRequest);
        medicationRequestSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the medicationRequests.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MedicationRequest> findAll(Pageable pageable) {
        log.debug("Request to get all MedicationRequests");
        Page<MedicationRequest> result = medicationRequestRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one medicationRequest by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public MedicationRequest findOne(Long id) {
        log.debug("Request to get MedicationRequest : {}", id);
        Optional<MedicationRequest> medicationRequest = medicationRequestRepository.findById(id);
        return medicationRequest.orElse(null);
    }

    /**
     * Delete the  medicationRequest by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete MedicationRequest : {}", id);
        medicationRequestRepository.deleteById(id);
        medicationRequestSearchRepository.deleteById(id);
    }

    /**
     * Search for the medicationRequest corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MedicationRequest> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of MedicationRequests for query {}", query);
        Page<MedicationRequest> result = medicationRequestSearchRepository.search(queryStringQuery(query)
                .field("documentNumber").field("document.patient.displayName")
                .field("document.patient.mrn").field("document.medicationRequestStatus")
                .field("document.orderingHSC.name").field("document.orderingHSC.code")
                .field("document.createdBy.login").field("document.createdBy.displayName")
                .field("document.consultant.name").field("document.consultant.displayName")
                .field("document.patientStatus").field("document.priority")
                .field("document.encounter.visitNumber")
                .defaultOperator(Operator.AND)
            , pageable);
        return result;
    }

    @Override
//    @Async(value = Constants.PHR_KAFKA_TASK_THREAD_POOL)
    public void updateMedicationRequest(MedicationRequest medicationRequest) {
        log.debug("updating the medication request status for document number={}", medicationRequest.getDocumentNumber());
        MedicationRequest savedMedicationRequest = medicationRequestRepository.findByDocumentNumber(medicationRequest.getDocumentNumber());
        log.debug("medication request = {}", medicationRequest);
        if (null != savedMedicationRequest) {
            log.debug("Medication request already exists. So updating the document. Document number ={}", medicationRequest.getDocumentNumber());
            //validate item is already dispensed. FEAT-36648
            boolean isDocumentModified = validatePharmacyMedicationRequestStatus(savedMedicationRequest,medicationRequest);
            if(isDocumentModified)
                return;
            savedMedicationRequest.setDocument(medicationRequest.getDocument());

            for(MedicationRequestDocumentLine medicationRequestDocumentLine :savedMedicationRequest.getDocument().getDocumentLines()) {
                updateMedicationOrder(savedMedicationRequest,medicationRequestDocumentLine);
            }
            checkAndUpdatePrescriptionAuditedFlag(savedMedicationRequest);
            save(savedMedicationRequest);

        } else {
            savedMedicationRequest = medicationRequest;
            updatePrescriptionAuditedFlag(savedMedicationRequest);
            savedMedicationRequest = save(savedMedicationRequest);
            log.debug("Saved medication request id ={}",savedMedicationRequest.getId());
            createMedicationOrder(savedMedicationRequest);
        }
    }

    private void checkAndUpdatePrescriptionAuditedFlag(MedicationRequest savedMedicationRequest) {
        try
        {
            PrescriptionAuditRequest prescriptionAuditRequest = elasticsearchTemplate.queryForObject(new CriteriaQuery(new Criteria("documentNumber.raw").is(savedMedicationRequest.getDocumentNumber())), PrescriptionAuditRequest.class,IndexCoordinates.of("prescriptionauditrequest"));
            if(Objects.nonNull(prescriptionAuditRequest) && PendingAuditRequestStatus.APPROVED.equals(prescriptionAuditRequest.getDocument().getPendingAuditRequestStatus()))
                savedMedicationRequest.setPrescriptionAudited(true);
        }catch(Exception ex)
        {
            log.error("error while checking prescription audit request flag. ex: {} ", ex);
        }

    }

    private void updatePrescriptionAuditedFlag(MedicationRequest savedMedicationRequest) {
        List<String> itemIds = savedMedicationRequest.getDocument().getDocumentLines().stream()
            .filter(medicationRequestDocumentLine -> null != medicationRequestDocumentLine.getMedication().getCode()).collect(Collectors.toList()).stream()
            .map(medicationRequestDocumentLine -> medicationRequestDocumentLine.getMedication().getCode())
            .collect(Collectors.toList());

        if(CollectionUtils.isEmpty(itemIds))
            return;

        Long hscId = savedMedicationRequest.getDocument().getOrderingHSC().getId();
        Long unitId = savedMedicationRequest.getDocument().getUnit().getId();
        savedMedicationRequest.setPrescriptionAudited(true);

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
            List<Item> drugItems = itemService.search(query, PageRequest.of(0, 1)).getContent();

            if (CollectionUtils.isNotEmpty(drugItems)) {
                if (StringUtils.isNotBlank(prescriptionAudit))
                    savedMedicationRequest.setPrescriptionAudited(false);
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
                        if (null != drugItem) {
                            if (StringUtils.isNotBlank(prescriptionAudit))
                                savedMedicationRequest.setPrescriptionAudited(false);
                        }
                    } catch (Exception ex) {
                        log.error("error while finding dispensable generic items with drug type. ex: {}", ex);
                    }

                }
            }
        }
    }

    /***
     *
     */
    @Override
    @Transactional(readOnly = true)
    public void deleteIndex() {
        log.debug("Request to delete elastic index of Medication Request");
        medicationRequestSearchRepository.deleteAll();
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
        List<MedicationRequest> data = medicationRequestRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            medicationRequestSearchRepository.saveAll(data);
        }
    }

    /**
     * This method is used to update the pharmacy medication request after dispense is happened
     * @param sourceDTO
     */
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
                throw new CustomParameterizedException("10427","Medication request is already dispensed");

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

    }

    @Override
    public void reIndex(Long id) {
        log.debug("Request to do re-index for medication request with id : {}", id);
        if (null != id) {
            medicationOrderService.reIndexWithMedicationRequestId(id);
            Optional<MedicationRequest> medicationRequest = medicationRequestRepository.findById(id);
            if ( !medicationRequest.isPresent()) {
                if (medicationRequestSearchRepository.existsById(id)) {
                    medicationRequestSearchRepository.deleteById(id);
                }
            } else {
                medicationRequestSearchRepository.save(medicationRequest.get());
            }
        }
        log.debug("Request to do reIndex medication ends : {}", LocalTime.now());
    }

    @Override
    public Map<String, String> exportPharmacyRequest(String query, Pageable pageable) throws IOException {
        log.debug("Request to export IP Pharmacy Request in CSV File query {}", query);
        File file = ExportUtil.getCSVExportFile("IPPharmacyRequest", applicationProperties.getAthmaBucket().getTempExport(), SecurityUtils.getCurrentUserLogin().get());
        FileWriter pharmacyRequestFileWriter = new FileWriter(file);
        Map<String, String> pharmacyRequestFileDetails = new HashMap<>();
        pharmacyRequestFileDetails.put(ExportUtilConstant.FILE_NAME,file.getName());
        pharmacyRequestFileDetails.put(ExportUtilConstant.PATH_REFERENCE,ExportUtilConstant.TEMP_EXPORT);

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(pharmacyRequestFileWriter, csvFileFormat)) {

            csvFilePrinter.printRecord((Object[]) PharmacyConstants.MEDICATION_REQUEST_HEADER);

            Iterator<MedicationRequest> medicationRequestIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();

            while (medicationRequestIterator.hasNext()) {

                MedicationRequest medicationRequest = medicationRequestIterator.next();
                MedicationRequestDocument medicationRequestDocument = medicationRequest.getDocument();
                List<MedicationRequestDocumentLine> medicationRequestDocumentLines = medicationRequestDocument.getDocumentLines();

                List<Object> medicationRequestRow = new ArrayList();

                Optional<MedicationRequestDocumentLine> priority = medicationRequestDocument.getDocumentLines().stream().filter(medicationRequestDocumentLine -> "URGENT".equalsIgnoreCase(medicationRequestDocumentLine.getPriority())).findFirst();
                if(priority.isPresent())
                {
                    medicationRequestRow.add("URGENT");
                }else
                {
                    medicationRequestRow.add("NORMAL");
                }
                medicationRequestRow.add(medicationRequest.getDocumentNumber());
                medicationRequestRow.add(medicationRequest.getDocument().getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getMrn() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient()!= null ? medicationRequestDocument.getPatient().getDisplayName() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient()!= null ? medicationRequestDocument.getPatient().getGender() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatient() != null ? medicationRequestDocument.getPatient().getAge() : "");
                medicationRequestRow.add(medicationRequestDocument.getEncounter() != null ? medicationRequestDocument.getEncounter().getVisitNumber() : "");
                medicationRequestRow.add(medicationRequestDocument.getPatientStatus() != null ? medicationRequestDocument.getPatientStatus(): "");
                medicationRequestRow.add(medicationRequestDocument.getOrderingHSC() != null ? medicationRequestDocument.getOrderingHSC().getName() : "");
                medicationRequestRow.add(medicationRequestDocument.getCreatedBy() != null ? medicationRequestDocument.getCreatedBy().getDisplayName(): "");
                medicationRequestRow.add(CollectionUtils.isNotEmpty(medicationRequestDocumentLines)?medicationRequestDocumentLines.get(0).getRenderingHSC().getName():"");
                medicationRequestRow.add(CollectionUtils.isNotEmpty(medicationRequestDocument.getDocumentLines())?medicationRequestDocument.getDocumentLines().size():0);
                medicationRequestRow.add(medicationRequestDocument.getPlanName());
                medicationRequestRow.add(medicationRequestDocument.getConsultant() != null ? medicationRequestDocument.getConsultant().getDisplayName(): "");
                medicationRequestRow.add(null == medicationRequestDocument.getMedicationRequestStatus()?"":medicationRequestDocument.getMedicationRequestStatus().getDisplayStatus());

                csvFilePrinter.printRecord(medicationRequestRow);

            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            pharmacyRequestFileWriter.close();
        }

        return pharmacyRequestFileDetails;
    }

    /**
     * Method to handle medication order cancellation or closing or partially closing or dispensing or partially dispensing of medication order
     * @param medicationRequest
     * @param medicationRequestDocumentLine
     */
    private void updateMedicationOrder(MedicationRequest medicationRequest, MedicationRequestDocumentLine medicationRequestDocumentLine) {
        String medicationOrderQuery = "medicationRequestId:"+medicationRequest.getId()+" AND documentLines.documentLineId:"+medicationRequestDocumentLine.getDocumentLineId();

        log.debug("finding medication order with query={}",medicationOrderQuery);
        List<MedicationOrder> medicationOrders = medicationOrderService.search(medicationOrderQuery, PageRequest.of(0, 1)).getContent();
        if(CollectionUtils.isNotEmpty(medicationOrders))
        {
            MedicationOrder medicationOrder = medicationOrders.get(0);
            log.debug("Found medication order with id={} and setting status ={}",medicationOrder.getId(),medicationRequestDocumentLine.getStatus());
            medicationOrder.getDocumentLines().setStatus(medicationRequestDocumentLine.getStatus());
            medicationOrder.getDocumentLines().setCancelledBy(medicationRequestDocumentLine.getCancelledBy());
            medicationOrder.getDocumentLines().setCancelledDate(medicationRequestDocumentLine.getCancelledDate());
            medicationOrder.getDocumentLines().setCancellingHsc(medicationRequestDocumentLine.getCancellingHsc());
            medicationOrder.getDocumentLines().setCancellationReason(medicationRequestDocumentLine.getCancellationReason());
            medicationOrder.setModifiedBy(medicationRequest.getDocument().getModifiedBy());
            medicationOrder.setModifiedDate(medicationRequest.getDocument().getModifiedDate());
            medicationOrder.setMedicationOrderDate(medicationRequest.getDocument().getMedicationRequestDate());
            medicationOrder.setMedicationOrderStatus(MedicationOrderStatus.valueOf(medicationRequestDocumentLine.getStatus()));

            medicationOrder.getDocumentLines().getDispenseRequest().setIssuedQuantity(medicationRequestDocumentLine.getDispenseRequest().getIssuedQuantity());
            medicationOrder.getDocumentLines().setUnitPrice(medicationRequestDocumentLine.getUnitPrice());
            medicationOrder.getDocumentLines().setTotalPrice(medicationRequestDocumentLine.getTotalPrice());
            medicationOrder.getDocumentLines().setProvisionalAmountCoverage(medicationRequestDocumentLine.getProvisionalAmountCoverage());
            medicationOrderService.save(medicationOrder);
            log.debug("Medication order with id={} is updated successfully with status={}",medicationOrder.getId(),medicationRequestDocumentLine.getStatus());
        }
        //medicationOrderService.refreshIndex();
    }

    private MedicationOrder createMedicationOrder(MedicationRequest createdMedicationRequest) {
        MedicationOrder medicationOrder = medicationRequestToMedicationOrderMapper.medicationRequestToMedicationOrder(createdMedicationRequest);
        for (MedicationRequestDocumentLine medicationRequestDocumentLine : createdMedicationRequest.getDocument().getDocumentLines())
        {
            MedicationOrderDocumentLine medicationOrderDocumentLine = medicationReqDocLineToMedicationOrderDocLine.MedicationReqDocLineToMedicationOrderDocLine(medicationRequestDocumentLine);
            log.debug("saving medication order with medication request id={}",createdMedicationRequest.getId());
            medicationOrder.setId(null);
            medicationOrder.setMedicationOrderNumber(medicationRequestDocumentLine.getMedicationOrderNumber());
            medicationOrder.setRenderingHSC(medicationRequestDocumentLine.getRenderingHSC());
            medicationOrder.setDocumentLines(medicationOrderDocumentLine);
            medicationOrder.setMedicationRequestId(createdMedicationRequest.getId());
            medicationOrder.setMedicationRequestNumber(createdMedicationRequest.getDocumentNumber());

            UserDTO consultant = createdMedicationRequest.getDocument().getConsultant();
            medicationOrder.setConsultant(consultant);

            medicationOrder.setOrderingDepartment(getOrderingDepartment(createdMedicationRequest));
            medicationOrder.setOrderingConsultant(getOrderingConsultant(createdMedicationRequest));

            SourceDTO sourceDTO = medicationOrder.getSourceDocumentList().get(0);
            sourceDTO.setLineItemId(medicationOrderDocumentLine.getDocumentLineId());
            List<SourceDTO> sourceDTOS =new ArrayList<>();
            sourceDTOS.add(sourceDTO);
            medicationOrder.setSourceDocumentList(sourceDTOS);
            MedicationOrder createdMedicationOrder = medicationOrderService.save(medicationOrder);
            medicationRequestDocumentLine.setMedicationOrderNumber(createdMedicationOrder.getMedicationOrderNumber());
            log.debug("medication order created with order number={}", createdMedicationOrder.getMedicationOrderNumber());

        }
        //medicationOrderService.refreshIndex();
        return medicationOrder;
    }

    /***
     *
     * @param documentNumber
     * @param versionFirst
     * @param versionSecond
     * @return
     */
    public Map<String, Object> compareVersion(String documentNumber, Integer versionFirst, Integer versionSecond) {
        log.debug("comparing for the MedicationRequest:" + documentNumber + " Version First:" + versionFirst + " Version Second:" + versionSecond);

        List<MedicationRequest> medicationRequestList = medicationRequestRepository.findMedicationRequestByVersions(documentNumber, versionFirst, versionSecond);
        Map<String, Object> documents = new HashedMap();
        for (MedicationRequest medicationRequest : medicationRequestList) {
            if (versionFirst.equals(medicationRequest.getVersion())) {
                Map<String, Object> firstDoc = new HashedMap();
                firstDoc.put("documentNumber", medicationRequest.getDocumentNumber());
                firstDoc.put("version", medicationRequest.getVersion());
                firstDoc.put("document", medicationRequest);
                documents.put("firstDocument", firstDoc);
            } else {
                Map<String, Object> secondDoc = new HashedMap();
                secondDoc.put("documentNumber", medicationRequest.getDocumentNumber());
                secondDoc.put("version", medicationRequest.getVersion());
                secondDoc.put("document", medicationRequest);
                documents.put("secondDocument", secondDoc);
            }
        }
        return documents;
    }

    /***
     *
     * @param documentNumber
     * @return
     */
    public List<Integer> getAllVersion(String documentNumber) {
        log.debug("get All Version for the medicationRequest:" + documentNumber);
        List<Integer> versionList = medicationRequestRepository.filndALlVersion(documentNumber);
        return versionList;
    }

    @Override
    public MedicationRequest findByDocumentNumber(String documentNumber) {
        return medicationRequestRepository.findByDocumentNumber(documentNumber);
    }

    private ConsultantDTO getOrderingConsultant(MedicationRequest createdMedicationRequest) {
        UserDTO consultant = createdMedicationRequest.getDocument().getConsultant();
        if( null == createdMedicationRequest.getDocument().getOrderingConsultant() && null != consultant) {
            ConsultantDTO consultantDTO = new ConsultantDTO();
            consultantDTO.setId(consultant.getId());
            consultantDTO.setDisplayName(consultant.getDisplayName());
            consultantDTO.setName(consultant.getDisplayName());
            return consultantDTO;
        }
        return createdMedicationRequest.getDocument().getOrderingConsultant();
    }

    private OrganizationDTO getOrderingDepartment(MedicationRequest createdMedicationRequest) {
        return null == createdMedicationRequest.getDocument().getOrderingDepartment()? createdMedicationRequest.getDocument().getDepartment():createdMedicationRequest.getDocument().getOrderingDepartment();
    }

    private boolean validatePharmacyMedicationRequestStatus(MedicationRequest savedMedicationRequest, MedicationRequest ambMedicationRequest) {
        boolean isDocumentModified = false;
        MedicationRequestStatus pharmacyMedicationReqStatus = savedMedicationRequest.getDocument().getMedicationRequestStatus();
        MedicationRequestStatus ambMedicationReqStatus = ambMedicationRequest.getDocument().getMedicationRequestStatus();
        switch (pharmacyMedicationReqStatus) {
            case DISPENSED:
            case PARTIALLY_DISPENSED:
                if (MedicationRequestStatus.CANCELLED.equals(ambMedicationReqStatus) || MedicationRequestStatus.ORDERED.equals(ambMedicationReqStatus)) {
                    log.info("pharmacy and amb medication request statuses are different. pharmacy status:{}, amb status: {}. so returning the control without updating medication request status ",
                        pharmacyMedicationReqStatus, ambMedicationReqStatus);
                    isDocumentModified = true;
                }
                break;
            default:
                break;
        }
        if (isDocumentModified)
            return true;

        if (!MedicationRequestStatus.CANCELLED.equals(ambMedicationRequest.getDocument().getMedicationRequestStatus())) {
            for (MedicationRequestDocumentLine medicationRequestDocumentLine : savedMedicationRequest.getDocument().getDocumentLines()) {
                if (isDocumentModified)
                    break;
                for (MedicationRequestDocumentLine requestDocumentLine : ambMedicationRequest.getDocument().getDocumentLines()) {
                    String pharmacyStatus = medicationRequestDocumentLine.getStatus();
                    String ambStatus = requestDocumentLine.getStatus();
                    if (requestDocumentLine.getDocumentLineId().equals(medicationRequestDocumentLine.getDocumentLineId())) {
                        switch (pharmacyStatus) {
                            case "DISPENSED":
                            case "PARTIALLY_DISPENSED":
                                if ("CANCELLED".equalsIgnoreCase(ambStatus) || "ORDERED".equalsIgnoreCase(ambStatus)) {
                                    log.info("pharmacy and amb medication request statuses are different. pharmacy status:{}, amb status: {}. so returning the control without updating medication request status ",
                                        pharmacyMedicationReqStatus, ambMedicationReqStatus);
                                    isDocumentModified = true;
                                    break;
                                }

                        }
                    }
                }
            }

        }

        return isDocumentModified;
    }
}
