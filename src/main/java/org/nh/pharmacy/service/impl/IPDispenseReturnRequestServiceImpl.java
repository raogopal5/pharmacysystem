package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.nh.common.dto.EncounterDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.util.ExportUtil;
import org.nh.common.util.ExportUtilConstant;
import org.nh.pharmacy.annotation.PamIntegration;
import org.nh.pharmacy.annotation.PublishStockTransaction;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.IPReturnRequestStatus;
import org.nh.pharmacy.domain.enumeration.IPReturnType;
import org.nh.pharmacy.domain.enumeration.ReturnStatus;
import org.nh.pharmacy.repository.IPDispenseReturnRequestRepository;
import org.nh.pharmacy.repository.search.IPDispenseReturnRequestSearchRepository;
import org.nh.pharmacy.security.SecurityUtils;
import org.nh.pharmacy.service.DispenseReturnService;
import org.nh.pharmacy.service.DispenseService;
import org.nh.pharmacy.service.IPDispenseReturnRequestService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.nh.pharmacy.web.rest.mapper.DispenseReturnReqToDispenseReturnMapper;
import org.nh.pharmacy.web.rest.mapper.DispenseToIPDispenseReturnRequestMapper;
import org.nh.seqgen.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing IPDispenseReturnRequest.
 */
@Service
@Transactional
public class IPDispenseReturnRequestServiceImpl implements IPDispenseReturnRequestService {

    private final Logger log = LoggerFactory.getLogger(IPDispenseReturnRequestServiceImpl.class);

    private final IPDispenseReturnRequestRepository iPDispenseReturnRequestRepository;

    private final IPDispenseReturnRequestSearchRepository iPDispenseReturnRequestSearchRepository;

    private final DispenseService dispenseService;

    private final DispenseToIPDispenseReturnRequestMapper dispenseToIPDispenseReturnRequestMapper;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final DispenseReturnReqToDispenseReturnMapper dispenseReturnReqToDispenseReturnMapper;

    private final DispenseReturnService dispenseReturnService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ApplicationProperties applicationProperties;

    private final ObjectMapper objectMapper;

    private final ElasticsearchOperations elasticsearchTemplate;

    public IPDispenseReturnRequestServiceImpl(IPDispenseReturnRequestRepository iPDispenseReturnRequestRepository, IPDispenseReturnRequestSearchRepository iPDispenseReturnRequestSearchRepository,
                                              DispenseService dispenseService, DispenseToIPDispenseReturnRequestMapper dispenseToIPDispenseReturnRequestMapper, SequenceGeneratorService sequenceGeneratorService,
                                              DispenseReturnReqToDispenseReturnMapper dispenseReturnReqToDispenseReturnMapper, DispenseReturnService dispenseReturnService, ApplicationEventPublisher applicationEventPublisher,
                                              ApplicationProperties applicationProperties, ObjectMapper objectMapper, ElasticsearchOperations elasticsearchTemplate) {
        this.iPDispenseReturnRequestRepository = iPDispenseReturnRequestRepository;
        this.iPDispenseReturnRequestSearchRepository = iPDispenseReturnRequestSearchRepository;
        this.dispenseService = dispenseService;
        this.dispenseToIPDispenseReturnRequestMapper = dispenseToIPDispenseReturnRequestMapper;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.dispenseReturnReqToDispenseReturnMapper = dispenseReturnReqToDispenseReturnMapper;
        this.dispenseReturnService = dispenseReturnService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }


    /**
     * Save a iPDispenseReturnRequest.
     *
     * @param iPDispenseReturnRequest the entity to save
     * @return the persisted entity
     */
    public IPDispenseReturnRequest save(IPDispenseReturnRequest iPDispenseReturnRequest) {
        log.debug("Request to save IPDispenseReturnRequest : {}", iPDispenseReturnRequest);
        if (null == iPDispenseReturnRequest.getId()) {
            iPDispenseReturnRequest.setId(iPDispenseReturnRequestRepository.getId());
            iPDispenseReturnRequest.setVersion(0);
            iPDispenseReturnRequest.setDocumentNumber(sequenceGeneratorService.generateNumber(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST, "NH", iPDispenseReturnRequest));
        } else {
            iPDispenseReturnRequestRepository.updateLatest(iPDispenseReturnRequest.getId());
            iPDispenseReturnRequest.setVersion(iPDispenseReturnRequest.getVersion() + 1);
        }
        iPDispenseReturnRequest.setLatest(true);
        IPDispenseReturnRequest result = iPDispenseReturnRequestRepository.save(iPDispenseReturnRequest);
        iPDispenseReturnRequestSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the iPDispenseReturnRequests.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<IPDispenseReturnRequest> findAll(Pageable pageable) {
        log.debug("Request to get all IPDispenseReturnRequests");
        Page<IPDispenseReturnRequest> result = iPDispenseReturnRequestRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one iPDispenseReturnRequest by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public IPDispenseReturnRequest findOne(Long id) {
        log.debug("Request to get IPDispenseReturnRequest : {}", id);
        IPDispenseReturnRequest iPDispenseReturnRequest = iPDispenseReturnRequestRepository.findOne(id);
        return iPDispenseReturnRequest;
    }

    /**
     * Delete the  iPDispenseReturnRequest by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete IPDispenseReturnRequest : {}", id);
        iPDispenseReturnRequestRepository.delete(id);
        iPDispenseReturnRequestSearchRepository.deleteById(id);
    }

    /**
     * Search for the iPDispenseReturnRequest corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<IPDispenseReturnRequest> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of IPDispenseReturnRequests for query {}", query);
        Page<IPDispenseReturnRequest> result = iPDispenseReturnRequestSearchRepository.search(queryStringQuery(query)
            .field("documentNumber").field("document.patient.displayName").field("document.patient.mrn").field("document.patientLocation.name")
            .field("document.bedNumber").field("document.patientStatus")
            .field("document.createdBy.displayName").field("document.sourceHSC.name")
            .field("document.returnTOHSC.name").field("document.rejectedBy.displayName")
            .field("document.returnStatus")
            .defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IPDispenseReturnRequest> searchByFields(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of IPDispenseReturnRequests for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(query)
            .field("documentNumber").field("document.patient.displayName").field("document.patient.mrn").field("document.patientLocation.name")
            .field("document.bedNumber").field("document.patientStatus")
            .field("document.createdBy.displayName").field("document.sourceHSC.name")
            .field("document.returnTOHSC.name").field("document.rejectedBy.displayName")
            .field("document.returnStatus")
            .defaultOperator(Operator.AND))
            .withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return iPDispenseReturnRequestSearchRepository.search(searchQuery);

    }

    @Override
    public List<IPDispenseReturnDocumentLine> constructIPDispenseReturnRequest(String query, String patientMrn, String visitNumber) {
        log.debug("Fetching all the dispensed items for the patient with query={}", query);
        List<Dispense> dispenseList = dispenseService.search(query, PageRequest.of(0, 5000)).getContent();
        List<IPDispenseReturnDocumentLine> dispenseReturnDocumentLines = new ArrayList<>();
        List<IPDispenseReturnRequest> pendingIpDispenseReturnRequests = getIpDispenseReturnRequestsForPatient(patientMrn, "", visitNumber,false);
        if (CollectionUtils.isNotEmpty(dispenseList)) {
            dispenseList.stream().forEach(dispense ->
            {
                dispense.getDocument().getDispenseDocumentLines().stream().forEach(dispenseDocumentLine ->
                    {
                        IPDispenseReturnDocumentLine ipDispenseReturnDocumentLine = dispenseToIPDispenseReturnRequestMapper.convertDispenseToDispenseDocumentLine(dispenseDocumentLine);
                        SourceDTO sourceDTO = new SourceDTO();
                        sourceDTO.setId(dispense.getId());
                        sourceDTO.setReferenceNumber(dispense.getDocumentNumber());
                        sourceDTO.setLineItemId(dispenseDocumentLine.getLineNumber());
                        sourceDTO.setDocumentType(DocumentType.DISPENSE);
                        ipDispenseReturnDocumentLine.setDispenseRef(sourceDTO);
                        ipDispenseReturnDocumentLine.setDispenseRefNumber(dispense.getDocumentNumber());
                        ipDispenseReturnDocumentLine.setDispenseUser(dispense.getDocument().getDispenseUser());
                        ipDispenseReturnDocumentLine.setDispenseDate(dispense.getDocument().getDispenseDate());
                        ipDispenseReturnDocumentLine.setDispenseUnit(dispense.getDocument().getDispenseUnit());
                        ipDispenseReturnDocumentLine.setDiscountAmount(BigDecimal.ZERO);
                        ipDispenseReturnDocumentLine.setReturnAmount(BigDecimal.ZERO);


                        countReturnedItemsInPendingState(ipDispenseReturnDocumentLine,pendingIpDispenseReturnRequests,false);
                        //fetch returned items for the dispense and dispense document line
                        fetchDispenseReturnedItems(dispense, dispenseDocumentLine.getLineNumber(), ipDispenseReturnDocumentLine);
                        dispenseReturnDocumentLines.add(ipDispenseReturnDocumentLine);
                    }
                );
            });
            log.debug("Converting dispense to ip dispense return request");

        }
        return dispenseReturnDocumentLines;
    }

    private void fetchDispenseReturnedItems(Dispense dispense, Long lineNumber, IPDispenseReturnDocumentLine ipDispenseReturnDocumentLine) {
        String query = "document.dispenseReturnDocumentLines.source.referenceNumber.raw:"+dispense.getDocumentNumber()+" AND document.dispenseReturnDocumentLines.source.lineItemId:"+lineNumber
            +" AND document.returnStatus.raw:"+ReturnStatus.RETURNED;
        List<DispenseReturn> ipDispenseReturns = dispenseReturnService.search(query,PageRequest.of(0, 2000)).getContent();
        Float returnedItemQty = 0f;
        if(CollectionUtils.isNotEmpty(ipDispenseReturns))
        {
            for(DispenseReturn dispenseReturn: ipDispenseReturns)
            {
                for(DispenseReturnDocumentLine dispenseReturnDocumentLine :dispenseReturn.getDocument().getDispenseReturnDocumentLines())
                {
                    if(ipDispenseReturnDocumentLine.getDispenseRef().getId().equals(dispenseReturnDocumentLine.getSource().getId()) &&
                        ipDispenseReturnDocumentLine.getDispenseRef().getLineItemId().equals(dispenseReturnDocumentLine.getSource().getLineItemId()))
                    {
                        log.debug("Return document matched for document number={},line number ={}",dispenseReturnDocumentLine.getSource().getReferenceNumber(),dispenseReturnDocumentLine.getSource().getLineItemId());
                        returnedItemQty+=dispenseReturnDocumentLine.getQuantity();
                    }
                }
            }
        }
        log.debug("Earlier returned items for dispense number ={} is ={} ",dispense.getDocumentNumber(),returnedItemQty);
        ipDispenseReturnDocumentLine.setEarlierReturnQuantity(returnedItemQty);

    }

    /**
     * Method to process ip dispense return request
     * @param ipDispenseReturnRequest
     * @param action
     * @param responseMap
     * @return
     */
    @Override
    @PublishStockTransaction
    @PamIntegration
    public Map<String, Object> processIPDispenseReturnRequest(IPDispenseReturnRequest ipDispenseReturnRequest, String action, Map<String, Object> responseMap) throws Exception {
        log.debug("Received request to process ip dispense return request with action ={}, current status ={}",action,ipDispenseReturnRequest.getDocument().getReturnStatus());
        IPDispenseReturnRequest result;
        switch (action)
        {
            case PharmacyConstants.ACCEPT:
                result = acceptIPDispenseReturnRequest(ipDispenseReturnRequest,responseMap);
                createDispenseReturn(result, responseMap);
                responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,result);
                break;
            case PharmacyConstants.REJECT:
                result =rejectIPDispenseReturnRequest(ipDispenseReturnRequest);
                responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,result);
                break;
            default:
                log.debug("Invalid action found. Action name ={}",action);
                result = ipDispenseReturnRequest;
                responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,result);
                break;
        }
        return responseMap;
    }

    /**
     * This method to count the return quantity across the items
     * @param iPDispenseReturnRequest
     */
    @Override
    public void validatePendingRequests(IPDispenseReturnRequest iPDispenseReturnRequest) throws Exception {
        IPDispenseReturnRequestDocument returnRequestDocument = iPDispenseReturnRequest.getDocument();
        PatientDTO patient = returnRequestDocument.getPatient();
        EncounterDTO encounter = returnRequestDocument.getEncounter();
        List<IPDispenseReturnRequest> ipDispenseReturnRequests = getIpDispenseReturnRequestsForPatient(patient.getMrn(),patient.getTempNumber(), encounter.getVisitNumber(),true);

        for(IPDispenseReturnDocumentLine ipDispenseReturnDocumentLine : iPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines()) {
            //item is directly returned. But print error is coming. So UI is not refreshed. Again user is trying to return the items and able to return more items than dispensed
            countReturnedItemsInPendingState(ipDispenseReturnDocumentLine, ipDispenseReturnRequests,true);
            float pendingQty=ipDispenseReturnDocumentLine.getPendingReturnQuantity();
            float requestedQty = ipDispenseReturnDocumentLine.getRequestedReturnQuantity();
            if(ipDispenseReturnDocumentLine.getDispensedQuantity() < pendingQty+requestedQty)
            {
                log.debug("pending and request quantity sum is greater than dispensed quantity. Dispensed Qty ={}, Pending Qty ={}",ipDispenseReturnDocumentLine.getDispensedQuantity(),(requestedQty+pendingQty));
                if(null != ipDispenseReturnDocumentLine.getOrderItem())
                    log.debug("Item name ={}",ipDispenseReturnDocumentLine.getOrderItem().getName());
                Map<String,Object> errorMap= new HashMap<>();
                errorMap.put("documentNumber",ipDispenseReturnDocumentLine.getDispenseRef().getReferenceNumber());
                errorMap.put("quantity",pendingQty+requestedQty);
                errorMap.put("dispensedQuantity",ipDispenseReturnDocumentLine.getDispensedQuantity());
                errorMap.put("itemName",null ==ipDispenseReturnDocumentLine.getMedication() ?"":ipDispenseReturnDocumentLine.getMedication().getName());
                errorMap.put("itemCode",null ==ipDispenseReturnDocumentLine.getMedication() ?"":ipDispenseReturnDocumentLine.getMedication().getCode());
                errorMap.put("pendingQty",pendingQty);
                errorMap.put("requestedQty",requestedQty);
                throw new CustomParameterizedException(PharmacyConstants.ERR_CODE_10161,errorMap);
            }
        }
    }

    private List<IPDispenseReturnRequest> getIpDispenseReturnRequestsForPatient(String mrn,String tempNumber, String visitNumber,boolean includeReturnedItems) {
        String mrnQuery = "document.patient.mrn.raw:"+mrn;
        if(null == mrn && null == tempNumber)
        {
            throw new CustomParameterizedException("patient MRN or Temp Number is mandatory");
        }
        if(null == mrn)
        {
            mrnQuery ="document.patient.tempNumber.raw:"+tempNumber;
        }
        String query = mrnQuery+ " AND document.encounter.visitNumber.raw:"+visitNumber+
            " AND (document.returnStatus.raw:PENDING OR document.returnStatus.raw:PARTIALLY_RETURNED";
        if(includeReturnedItems)
            query=query+" OR document.returnStatus.raw:RETURNED";
        query= query+" )";
        return search(query, PageRequest.of(0, 5000)).getContent();
    }

    private void countReturnedItemsInPendingState(IPDispenseReturnDocumentLine currIPReturnDocumentLine, List<IPDispenseReturnRequest> prevIPDispenseReturnRequests,boolean includeReturnedItems) {
        float pendingQty = 0f;
        SourceDTO currentLineSourceDTO = currIPReturnDocumentLine.getDispenseRef();
        for(IPDispenseReturnRequest prevIPDispenseReturnRequest : prevIPDispenseReturnRequests)
        {
            for(IPDispenseReturnDocumentLine prevIPDispenseReturnDocumentLine :prevIPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines())
            {
                SourceDTO prevLineSourceDTO = prevIPDispenseReturnDocumentLine.getDispenseRef();
                if(currentLineSourceDTO.getReferenceNumber().equals(prevLineSourceDTO.getReferenceNumber())
                    && currentLineSourceDTO.getLineItemId().equals(prevLineSourceDTO.getLineItemId()))
                {
                    //requestedQty = currIPReturnDocumentLine.getRequestedReturnQuantity();
                    log.debug("Pending qty for line id= {} with dispense number ={} in dispense request ={} is ={}",currentLineSourceDTO.getLineItemId(),
                        currentLineSourceDTO.getReferenceNumber(),prevIPDispenseReturnRequest.getDocumentNumber(),prevIPDispenseReturnDocumentLine.getRequestedReturnQuantity());
                    if(IPReturnRequestStatus.PARTIALLY_RETURNED.equals(prevIPDispenseReturnRequest.getDocument().getReturnStatus()))
                        pendingQty+=(prevIPDispenseReturnDocumentLine.getRequestedReturnQuantity()-prevIPDispenseReturnDocumentLine.getAcceptedReturnQuantity()-prevIPDispenseReturnDocumentLine.getPreviousAcceptedReturnQty());
                    else if(IPReturnRequestStatus.PENDING.equals(prevIPDispenseReturnRequest.getDocument().getReturnStatus()))
                        pendingQty+=prevIPDispenseReturnDocumentLine.getRequestedReturnQuantity();
                    else if(IPReturnRequestStatus.RETURNED.equals(prevIPDispenseReturnRequest.getDocument().getReturnStatus())
                         && includeReturnedItems)
                        pendingQty+=prevIPDispenseReturnDocumentLine.getAcceptedReturnQuantity();

                }
            }
        }
        currIPReturnDocumentLine.setPendingReturnQuantity(pendingQty);
    }



    private IPDispenseReturnRequest rejectIPDispenseReturnRequest(IPDispenseReturnRequest ipDispenseReturnRequest) {
        log.debug("Rejecting ip dispense return request with document number ={}",ipDispenseReturnRequest.getDocumentNumber());
        if(IPReturnRequestStatus.PARTIALLY_RETURNED.equals(ipDispenseReturnRequest.getDocument().getReturnStatus()))
        {
            log.debug("setting partially rejected status.");
            ipDispenseReturnRequest.getDocument().setReturnStatus(IPReturnRequestStatus.PARTIALLY_REJECTED);
        }else{
            ipDispenseReturnRequest.getDocument().setReturnStatus(IPReturnRequestStatus.REJECTED);
        }
        return save(ipDispenseReturnRequest);
    }

    private IPDispenseReturnRequest acceptIPDispenseReturnRequest(IPDispenseReturnRequest ipDispenseReturnRequest, Map<String, Object> responseMap) throws Exception {
        log.debug("Accepting ip dispense return request with document number = {} ", ipDispenseReturnRequest.getDocumentNumber());
        IPReturnRequestStatus ipReturnRequestStatus = IPReturnRequestStatus.RETURNED;
        for(IPDispenseReturnDocumentLine ipDispenseReturnDocumentLine : ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines())
        {
            float previousAcceptedQty = null == ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty()?0f:ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty();
            if(isNull(ipDispenseReturnDocumentLine.getAcceptedReturnQuantity()))
                ipDispenseReturnDocumentLine.setAcceptedReturnQuantity(0f);
            if(ipDispenseReturnDocumentLine.getRequestedReturnQuantity() > ipDispenseReturnDocumentLine.getAcceptedReturnQuantity()+previousAcceptedQty){
                log.debug("updating partially return status");
                ipReturnRequestStatus = IPReturnRequestStatus.PARTIALLY_RETURNED;
                //break;
            }
        }
        ipDispenseReturnRequest.getDocument().setReturnStatus(ipReturnRequestStatus);
        return save(ipDispenseReturnRequest);

    }

    /**
     * Method to create dispense return from ipdispensereturn request
     * @param result
     * @param responseMap
     */
    private DispenseReturn createDispenseReturn(IPDispenseReturnRequest result, Map<String, Object> responseMap) throws Exception {
        log.debug("Creating dispense return from ip return request");

        DispenseReturn dispenseReturn = dispenseReturnReqToDispenseReturnMapper.convertDispenseRequestToDispenseReturn(result);
        if(CollectionUtils.isEmpty(dispenseReturn.getDocument().getDispenseReturnDocumentLines()))
        {
            Map<String,Object> errorMap = new HashMap<>();
            throw new CustomParameterizedException(PharmacyConstants.ERR_CODE_10186,errorMap);
        }

        dispenseReturn.getDocument().setCreatedDate(LocalDateTime.now());
        responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN, dispenseReturn);
        if (IPReturnType.DIRECT_RETURN.equals(result.getDocument().getIpReturnType())) {
            dispenseReturn.documentNumber(sequenceGeneratorService.generateSequence("ipPharmacyDirectReturn", "NH", result));
            dispenseReturn.getDocument().setIpReturnType(IPReturnType.DIRECT_RETURN);
            dispenseReturn.getDocument().setCreatedBy(result.getDocument().getCreatedBy());
        }
        else
            dispenseReturn.documentNumber(sequenceGeneratorService.generateSequence("ipPharmacyReturn", "NH", result));
        DispenseReturn savedDispenseReturn = dispenseReturnService.processIPDispenseReturn(dispenseReturn);
        responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN, dispenseReturn);
        log.debug("Dispense return created successfully");
        applicationEventPublisher.publishEvent(savedDispenseReturn);
        log.debug("Dispense return published successfully");
        return savedDispenseReturn;

    }

    /**
     * Method to export ip dispense return requests
     * @param query
     * @param pageable
     */
    @Override
    public Map<String, String> exportIPDispenseReturnRequest(String query, Pageable pageable) throws IOException {
        log.debug("exporting pending ip pharmacy return request records");
        Iterator<IPDispenseReturnRequest> ipDispenseReturnRequestIterator = this.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();
        String featureName ="IP Pharmacy Return Request";
        File file= ExportUtil.getCSVExportFile(featureName,applicationProperties.getAthmaBucket().getTempExport(),SecurityUtils.getCurrentUserLogin().get());
        FileWriter ipPharmacyReturnReqWriter = new FileWriter(file);
        Map<String,String> exportDetails = new HashMap<>();
        exportDetails.put(ExportUtilConstant.FILE_NAME,file.getName());
        exportDetails.put(ExportUtilConstant.PATH_REFERENCE,ExportUtilConstant.TEMP_EXPORT);
        CSVFormat csvFileFormat= CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(ipPharmacyReturnReqWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST_HEADER);
            while(ipDispenseReturnRequestIterator.hasNext()){
                IPDispenseReturnRequest ipDispenseReturnRequest = ipDispenseReturnRequestIterator.next();
                IPDispenseReturnRequestDocument ipDispenseReturnRequestDocument = ipDispenseReturnRequest.getDocument();
                List<Object> ipDispenseReturnReqList = new ArrayList();
                ipDispenseReturnReqList.add(ipDispenseReturnRequest.getDocumentNumber());
                ipDispenseReturnReqList.add(ipDispenseReturnRequestDocument.getRequestedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                PatientDTO patient = ipDispenseReturnRequestDocument.getPatient();
                ipDispenseReturnReqList.add(patient.getMrn());
                ipDispenseReturnReqList.add(patient.getFullName());
                ipDispenseReturnReqList.add(patient.getGender());
                ipDispenseReturnReqList.add(null == patient.getAge()?"":(-1 == patient.getAge()?"":patient.getAge()));
                String bedNumber = null == ipDispenseReturnRequestDocument.getBedNumber()?"":ipDispenseReturnRequestDocument.getBedNumber();
                ipDispenseReturnReqList.add(null!=ipDispenseReturnRequestDocument.getPatientLocation()?ipDispenseReturnRequestDocument.getPatientLocation().getName()+"/"+bedNumber:"");
                ipDispenseReturnReqList.add(null == ipDispenseReturnRequestDocument.getPatientStatus()?"":ipDispenseReturnRequestDocument.getPatientStatus().getDisplayStatus());
                ipDispenseReturnReqList.add(ipDispenseReturnRequestDocument.getRequestedBy().getDisplayName());
                ipDispenseReturnReqList.add(ipDispenseReturnRequestDocument.getSourceHSC().getName());
                ipDispenseReturnReqList.add(ipDispenseReturnRequestDocument.getReturnTOHSC().getName());
                ipDispenseReturnReqList.add(null == ipDispenseReturnRequestDocument.getRejectedBy()?"":ipDispenseReturnRequestDocument.getRequestedBy().getDisplayName());
                LocalDateTime rejectedDate = ipDispenseReturnRequestDocument.getRejectedDate();
                if(null == rejectedDate)
                    ipDispenseReturnReqList.add("");
                else
                    ipDispenseReturnReqList.add(rejectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                ipDispenseReturnReqList.add(null == ipDispenseReturnRequestDocument.getReturnStatus()?"":ipDispenseReturnRequestDocument.getReturnStatus().getDisplayStatus());
                csvFilePrinter.printRecord(ipDispenseReturnReqList);
            }
        }catch(Exception ex){
            log.debug("Error occurred while exporting the data",ex);
        }finally {
            ipPharmacyReturnReqWriter.close();
        }
        return exportDetails;

    }

    /**
     * Method to update previous accepted return quantity for the same return request document
     * @param iPDispenseReturnRequest
     */
    @Override
    public void updateAcceptedAndPendingReturnQty(IPDispenseReturnRequest iPDispenseReturnRequest) {
        log.debug("Updating accepted and pending return qty for ip dispense request number ={}",iPDispenseReturnRequest.getDocumentNumber());
        iPDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines().forEach( ipDispenseReturnDocumentLine ->
        {
            log.debug("updating the previous accepted return qty");
            ipDispenseReturnDocumentLine.setPreviousAcceptedReturnQty(ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty()+ipDispenseReturnDocumentLine.getAcceptedReturnQuantity());
            ipDispenseReturnDocumentLine.setAcceptedReturnQuantity(0f);
        });
    }

    @Override
    public void deleteIndex() {
        log.debug("Request to delete ip dispense return request index");
        iPDispenseReturnRequestSearchRepository.deleteAll();
    }

    /**
     * Method to index ip dispense return request records
     */
    @Override
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Indexing ip dispense return request records");
        List<IPDispenseReturnRequest> data = iPDispenseReturnRequestRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo, pageSize));
        if (!data.isEmpty()) {
            iPDispenseReturnRequestSearchRepository.saveAll(data);
        }
    }

    @Override
    public void reIndex(Long id) {
        log.debug("Request to do re-index for ip dispense return request record : {}", id);
        if (id != null) {
            IPDispenseReturnRequest ipDispenseReturnRequest = iPDispenseReturnRequestRepository.findOne(id);
            if (ipDispenseReturnRequest == null) {
                if (iPDispenseReturnRequestSearchRepository.existsById(id)) {
                    iPDispenseReturnRequestSearchRepository.deleteById(id);
                }
            } else {
                iPDispenseReturnRequestSearchRepository.save(ipDispenseReturnRequest);
            }
        }
        log.debug("Request to do reIndex dispense return request ends : {}", LocalTime.now());
    }

    @Override
    public Map<String, String> exportIPDispenseReturn(String query, Pageable pageable) throws IOException{
        log.debug("exporting pending ip pharmacy return records");
        Iterator<DispenseReturn> ipDispenseReturnIterator = dispenseReturnService.search(query, PageRequest.of(0, applicationProperties.getConfigs().getExportRowsCount(), pageable.getSort())).iterator();
        String featureName ="IP Pharmacy Return";
        File file= ExportUtil.getCSVExportFile(featureName,applicationProperties.getAthmaBucket().getTempExport(),SecurityUtils.getCurrentUserLogin().get());
        FileWriter ipPharmacyReturnWriter = new FileWriter(file);
        Map<String,String> exportDetails = new HashMap<>();
        exportDetails.put(ExportUtilConstant.FILE_NAME,file.getName());
        exportDetails.put(ExportUtilConstant.PATH_REFERENCE,ExportUtilConstant.TEMP_EXPORT);
        CSVFormat csvFileFormat= CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(ipPharmacyReturnWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(PharmacyConstants.IP_DISPENSE_RETURN_HEADER);
            while(ipDispenseReturnIterator.hasNext()){
                DispenseReturn dispenseReturn = ipDispenseReturnIterator.next();
                DispenseReturnDocument dispenseReturnDocument = dispenseReturn.getDocument();
                List<Object> ipDispenseReturnReqList = new ArrayList();
                ipDispenseReturnReqList.add(dispenseReturn.getDocumentNumber());
                ipDispenseReturnReqList.add(dispenseReturnDocument.getReturnDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                PatientDTO patient = dispenseReturnDocument.getPatient();
                ipDispenseReturnReqList.add(patient.getMrn());
                ipDispenseReturnReqList.add(patient.getFullName());
                ipDispenseReturnReqList.add(patient.getGender());
                ipDispenseReturnReqList.add(null == patient.getAge()?"":(-1 == patient.getAge()?"":patient.getAge()));
                ipDispenseReturnReqList.add(null == dispenseReturnDocument.getCreatedBy()?"":dispenseReturnDocument.getCreatedBy().getDisplayName());
                ipDispenseReturnReqList.add(dispenseReturnDocument.getReturnhsc().getName());
                String bedNumber = null == dispenseReturnDocument.getBedNumber()?"":dispenseReturnDocument.getBedNumber();
                ipDispenseReturnReqList.add(dispenseReturnDocument.getPatientLocation().getName()+"/"+bedNumber);
                ipDispenseReturnReqList.add(dispenseReturnDocument.getReturnRequestNumber());
                LocalDateTime rejectedDate = dispenseReturnDocument.getRequestedDate();
                if(null == rejectedDate)
                    ipDispenseReturnReqList.add("");
                else
                    ipDispenseReturnReqList.add(rejectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                ipDispenseReturnReqList.add(dispenseReturnDocument.getRequestedBy().getDisplayName());
                ipDispenseReturnReqList.add(dispenseReturnDocument.getHsc().getName());
                csvFilePrinter.printRecord(ipDispenseReturnReqList);
            }
        }catch(Exception ex){
            log.debug("Error occurred while exporting ip dispense return data",ex);
        }finally {
            ipPharmacyReturnWriter.close();
        }
        return exportDetails;
    }

    /**
     * This method is used for fetching IP Pending dispense orders
     * @param mrn
     * @param visitNumber
     * @param action
     * @return
     * @throws Exception
     */
    @Override
    public List<IPDispenseReturnRequestDTO> getIPDispenseReturns(String mrn, String visitNumber, String action) throws Exception {
        log.debug("getIPDispenseReturns Method start");
        Map<String, IPDispenseReturnRequestDTO> ipDispenseReturnRequestMap = new HashMap<>();
        List<IPDispenseReturnRequestDTO> ipDispenseReturnRequestDTOS = new ArrayList<>();
        StringBuffer query = new StringBuffer();

        query.append("(document.patient.mrn.raw:").append(mrn).append(") ")
            .append("(document.encounter.visitNumber:").append(visitNumber).append(") ")
            .append("(document.returnStatus.raw:PENDING OR document.returnStatus.raw:PARTIALLY_RETURNED)");

        log.debug("getIPDispenseReturns pending dispense returns query : {}",query.toString());

        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryStringQuery(query.toString()).defaultOperator(Operator.AND))
            .withPageable(PageRequest.of(0, 10000))
            .build();

        List<IPDispenseReturnRequest> ipDispenseReturnRequestList = ElasticSearchUtil.queryForList("ipdispensereturnrequest", searchQuery, elasticsearchTemplate, IPDispenseReturnRequest.class);
        if (action.equals(Constants.ORDERS_QUANTITY)) {
            log.debug("Fetching IPDispenseReturnRequest data for Dispense Return Quantity Item wise");
            ipDispenseReturnRequestList.forEach(ipDispenseReturnRequest -> {
                List<IPDispenseReturnDocumentLine> ipDispenseReturnDocumentLines =
                    ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines();

                ipDispenseReturnDocumentLines.forEach(ipDispenseReturnDocumentLine -> {
                    Float requestedReturnQuantity = ipDispenseReturnDocumentLine.getRequestedReturnQuantity();
                    Float previousAcceptedReturnQty = ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty();
                    Float acceptedReturnQuantity = ipDispenseReturnDocumentLine.getAcceptedReturnQuantity();

                    if (requestedReturnQuantity - (previousAcceptedReturnQty + acceptedReturnQuantity) > 0) {
                        IPDispenseReturnRequestDTO ipDispenseReturnRequestDTO = new IPDispenseReturnRequestDTO();
                        ipDispenseReturnRequestDTO.setDocumentNumber(ipDispenseReturnRequest.getDocumentNumber());
                        ipDispenseReturnRequestDTO.setReturnhsc(ipDispenseReturnRequest.getDocument().getReturnTOHSC());
                        ipDispenseReturnRequestDTO.setIpReturnRequestStatus(ipDispenseReturnRequest.getDocument().getReturnStatus());
                        ipDispenseReturnRequestDTO.setItemName(ipDispenseReturnDocumentLine.getName());
                        ipDispenseReturnRequestDTO.setReturnRequestDate(ipDispenseReturnRequest.getDocument().getRequestedDate());

                        ipDispenseReturnRequestDTO.setPendingQuantity(requestedReturnQuantity - (previousAcceptedReturnQty + acceptedReturnQuantity));
                        ipDispenseReturnRequestDTO.setVisitNumber(ipDispenseReturnRequest.getDocument().getEncounter().getVisitNumber());
                        if (ipDispenseReturnRequestMap.containsKey(ipDispenseReturnDocumentLine.getName())) {
                            ipDispenseReturnRequestDTO.setPendingQuantity(
                                ipDispenseReturnRequestMap.get(ipDispenseReturnDocumentLine.getName()).getPendingQuantity() +
                                    (requestedReturnQuantity - (previousAcceptedReturnQty + acceptedReturnQuantity)));
                            ipDispenseReturnRequestMap.put(ipDispenseReturnDocumentLine.getName(), ipDispenseReturnRequestDTO);
                        } else {
                            ipDispenseReturnRequestMap.put(ipDispenseReturnDocumentLine.getName(), ipDispenseReturnRequestDTO);
                        }
                    }
                });
            });
            return ipDispenseReturnRequestMap.values().stream().collect(Collectors.toList());
        } else {
            log.debug("Fetching IPDispenseReturnRequest data for Dispense Return Quantity Order wise");
            ipDispenseReturnRequestList.forEach(ipDispenseReturnRequest -> {
                List<IPDispenseReturnDocumentLine> ipDispenseReturnDocumentLines =
                    ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines();

                ipDispenseReturnDocumentLines.forEach(ipDispenseReturnDocumentLine -> {
                    Float requestedReturnQuantity = ipDispenseReturnDocumentLine.getRequestedReturnQuantity();
                    Float previousAcceptedReturnQty = ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty();
                    Float acceptedReturnQuantity = ipDispenseReturnDocumentLine.getAcceptedReturnQuantity();

                    if (requestedReturnQuantity - (previousAcceptedReturnQty + acceptedReturnQuantity) > 0) {
                        IPDispenseReturnRequestDTO ipDispenseReturnRequestDTO = new IPDispenseReturnRequestDTO();
                        ipDispenseReturnRequestDTO.setDocumentNumber(ipDispenseReturnRequest.getDocumentNumber());
                        ipDispenseReturnRequestDTO.setReturnhsc(ipDispenseReturnRequest.getDocument().getReturnTOHSC());
                        ipDispenseReturnRequestDTO.setIpReturnRequestStatus(ipDispenseReturnRequest.getDocument().getReturnStatus());
                        ipDispenseReturnRequestDTO.setItemName(ipDispenseReturnDocumentLine.getName());
                        ipDispenseReturnRequestDTO.setReturnRequestDate(ipDispenseReturnRequest.getDocument().getRequestedDate());

                        ipDispenseReturnRequestDTO.setPendingQuantity(requestedReturnQuantity - (previousAcceptedReturnQty + acceptedReturnQuantity));
                        ipDispenseReturnRequestDTO.setVisitNumber(ipDispenseReturnRequest.getDocument().getEncounter().getVisitNumber());
                        ipDispenseReturnRequestDTOS.add(ipDispenseReturnRequestDTO);
                    }
                });
            });
            return ipDispenseReturnRequestDTOS;
        }
    }

    @Override
    @PublishStockTransaction
    @PamIntegration
    public Map<String, Object> processIPDispenseDirectReturn(IPDispenseReturnRequest ipDispenseReturnRequest, Map<String, Object> dispenseReturnMap) throws Exception {
        //IPDispenseReturnRequest result = acceptIPDispenseReturnRequest(iPDispenseReturnRequest, responseMap);
        //don't create dipense return request
        log.debug("Accepting ip dispense return request with document number = {} ", ipDispenseReturnRequest.getDocumentNumber());
        IPReturnRequestStatus ipReturnRequestStatus = IPReturnRequestStatus.RETURNED;
        for(IPDispenseReturnDocumentLine ipDispenseReturnDocumentLine : ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines())
        {
            float previousAcceptedQty = null == ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty()?0f:ipDispenseReturnDocumentLine.getPreviousAcceptedReturnQty();
            if(isNull(ipDispenseReturnDocumentLine.getAcceptedReturnQuantity()))
                ipDispenseReturnDocumentLine.setAcceptedReturnQuantity(0f);
            if(ipDispenseReturnDocumentLine.getRequestedReturnQuantity() > ipDispenseReturnDocumentLine.getAcceptedReturnQuantity()+previousAcceptedQty){
                log.debug("updating partially return status");
                ipReturnRequestStatus = IPReturnRequestStatus.PARTIALLY_RETURNED;
            }
        }
        ipDispenseReturnRequest.getDocument().setReturnStatus(ipReturnRequestStatus);
        dispenseReturnMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,ipDispenseReturnRequest);
        createDispenseReturn(ipDispenseReturnRequest,dispenseReturnMap);
        return dispenseReturnMap;
    }

    @Override
    public void reIndexReturnRequest(Map<String, Object> responseMap) {
        log.error("Reindexing ip pharmacy return request data. value ={}",responseMap);
        if(responseMap.containsKey(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST))
        {
            IPDispenseReturnRequest ipDispenseReturnRequest = (IPDispenseReturnRequest) responseMap.get(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST);
            reIndex(ipDispenseReturnRequest.getId());
            log.error("Re indexing is successful for id={}",ipDispenseReturnRequest.getId());
        }
        if(responseMap.containsKey(PharmacyConstants.IP_DISPENSE_RETURN))
        {
            log.debug("Reindexing dispense return");
            DispenseReturn dispenseReturn = (DispenseReturn)responseMap.get(PharmacyConstants.IP_DISPENSE_RETURN);
            dispenseReturnService.reIndex(dispenseReturn.getId());
            log.error("Reindexing dispense return for id ={}",dispenseReturn.getId());
        }
        log.debug("Re indexing completed");
    }

    /**
     * This method is used for constructing the elastic search query by using the below parameters
     *
     * @param mrn
     * @param visitNumber
     * @return
     */
    private BoolQueryBuilder getIPDispenseReturnsESQuery(String mrn, String visitNumber) {
        log.debug("getDispenseReturnsESQuery start");
        BoolQueryBuilder query = boolQuery()
            .must(matchQuery("document.patient.mrn.raw", mrn))
            .must(matchQuery("document.encounter.visitNumber.raw", visitNumber))
            .must(boolQuery()
                .should(matchQuery("document.returnStatus.raw", "PENDING"))
                .should(matchQuery("document.returnStatus.raw", "PARTIALLY_RETURNED")));
        log.debug("dispense returns query : {}", query.toString());
        return query;
    }

    /**
     * Convert to object
     *
     * @param metadata
     * @param name
     * @param clazz
     * @return T
     * @throws IOException
     */
    private <T> T convertToObject(Map metadata, String name, Class<T> clazz) throws IOException {
        log.debug("convertToObject method start {}", name);
        return objectMapper.readValue(objectMapper.writeValueAsString(metadata.get(name)), clazz);
    }
}
