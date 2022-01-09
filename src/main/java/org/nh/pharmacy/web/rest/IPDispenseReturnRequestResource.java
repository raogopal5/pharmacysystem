package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.repository.IPDispenseReturnRequestRepository;
import org.nh.pharmacy.repository.search.IPDispenseReturnRequestSearchRepository;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.springframework.core.io.Resource;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDTO;
import org.nh.pharmacy.service.IPDispenseReturnRequestService;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.nh.pharmacy.service.IPDispenseReturnRequestPdfService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for managing IPDispenseReturnRequest.
 */
@RestController
@RequestMapping("/api")
public class IPDispenseReturnRequestResource {

    private final Logger log = LoggerFactory.getLogger(IPDispenseReturnRequestResource.class);

    private final IPDispenseReturnRequestService iPDispenseReturnRequestService;
    private final IPDispenseReturnRequestPdfService iPDispenseReturnRequestPdfService;
    private final IPDispenseReturnRequestRepository ipDispenseReturnRequestRepository;
    private final IPDispenseReturnRequestSearchRepository ipDispenseReturnRequestSearchRepository;
    private final ApplicationProperties applicationProperties;

    public IPDispenseReturnRequestResource(IPDispenseReturnRequestService iPDispenseReturnRequestService, IPDispenseReturnRequestPdfService iPDispenseReturnRequestPdfService, IPDispenseReturnRequestRepository ipDispenseReturnRequestRepository, IPDispenseReturnRequestSearchRepository ipDispenseReturnRequestSearchRepository, ApplicationProperties applicationProperties) {
        this.iPDispenseReturnRequestService = iPDispenseReturnRequestService;
        this.iPDispenseReturnRequestPdfService = iPDispenseReturnRequestPdfService;
        this.ipDispenseReturnRequestRepository = ipDispenseReturnRequestRepository;
        this.ipDispenseReturnRequestSearchRepository = ipDispenseReturnRequestSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /ip-dispense-return-requests : Create a new iPDispenseReturnRequest.
     *
     * @param iPDispenseReturnRequest the iPDispenseReturnRequest to create
     * @return the ResponseEntity with status 201 (Created) and with body the new iPDispenseReturnRequest, or with status 400 (Bad Request) if the iPDispenseReturnRequest has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ip-dispense-return-requests")
    //@Timed
    @PreAuthorize("hasPrivilege('102121104')")
    public ResponseEntity<IPDispenseReturnRequest> createIPDispenseReturnRequest(@RequestBody IPDispenseReturnRequest iPDispenseReturnRequest) throws Exception {
        log.debug("REST request to save IPDispenseReturnRequest : {}", iPDispenseReturnRequest);
        if (iPDispenseReturnRequest.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("iPDispenseReturnRequest", "idexists", "A new iPDispenseReturnRequest cannot already have an ID")).body(null);
        }
        iPDispenseReturnRequestService.validatePendingRequests(iPDispenseReturnRequest);
        try {
            IPDispenseReturnRequest result = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
            return ResponseEntity.created(new URI("/api/ip-dispense-return-requests/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("iPDispenseReturnRequest", result.getId().toString()))
                .body(result);
        }catch(Exception ex)
        {
            log.error("Error while saving ip dispense return request record. Ex={}",ex);
            iPDispenseReturnRequestService.reIndex(iPDispenseReturnRequest.getId());
            throw new Exception("Exception while saving ip dispense return request");
        }
    }

    /**
     * PUT  /ip-dispense-return-requests : Updates an existing iPDispenseReturnRequest.
     *
     * @param iPDispenseReturnRequest the iPDispenseReturnRequest to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated iPDispenseReturnRequest,
     * or with status 400 (Bad Request) if the iPDispenseReturnRequest is not valid,
     * or with status 500 (Internal Server Error) if the iPDispenseReturnRequest couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ip-dispense-return-requests")
    //@Timed
    @PreAuthorize("hasPrivilege('102121104')")
    public ResponseEntity<IPDispenseReturnRequest> updateIPDispenseReturnRequest(@RequestBody IPDispenseReturnRequest iPDispenseReturnRequest) throws Exception {
        log.debug("REST request to update IPDispenseReturnRequest : {}", iPDispenseReturnRequest);
        if (iPDispenseReturnRequest.getId() == null) {
            return createIPDispenseReturnRequest(iPDispenseReturnRequest);
        }
        IPDispenseReturnRequest result = iPDispenseReturnRequestService.save(iPDispenseReturnRequest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("iPDispenseReturnRequest", iPDispenseReturnRequest.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ip-dispense-return-requests : get all the iPDispenseReturnRequests.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of iPDispenseReturnRequests in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/ip-dispense-return-requests")
    //@Timed
    @PreAuthorize("hasPrivilege('102121107') OR hasPrivilege('102121106')")
    public ResponseEntity<List<IPDispenseReturnRequest>> getAllIPDispenseReturnRequests(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of IPDispenseReturnRequests");
        Page<IPDispenseReturnRequest> page = iPDispenseReturnRequestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ip-dispense-return-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ip-dispense-return-requests/:id : get the "id" iPDispenseReturnRequest.
     *
     * @param id the id of the iPDispenseReturnRequest to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the iPDispenseReturnRequest, or with status 404 (Not Found)
     */
    @GetMapping("/ip-dispense-return-requests/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('102121107') OR hasPrivilege('102121106')")
    public ResponseEntity<IPDispenseReturnRequest> getIPDispenseReturnRequest(@PathVariable Long id) {
        log.debug("REST request to get IPDispenseReturnRequest : {}", id);
        IPDispenseReturnRequest iPDispenseReturnRequest = iPDispenseReturnRequestService.findOne(id);
        if(null != iPDispenseReturnRequest)
        {
            iPDispenseReturnRequestService.updateAcceptedAndPendingReturnQty(iPDispenseReturnRequest);
        }
        return Optional.ofNullable(iPDispenseReturnRequest)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /ip-dispense-return-requests/:id : delete the "id" iPDispenseReturnRequest.
     *
     * @param id the id of the iPDispenseReturnRequest to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ip-dispense-return-requests/{id}")
    //@Timed
    public ResponseEntity<Void> deleteIPDispenseReturnRequest(@PathVariable Long id) {
        log.debug("REST request to delete IPDispenseReturnRequest : {}", id);
        iPDispenseReturnRequestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("iPDispenseReturnRequest", id.toString())).build();
    }

    /**
     * SEARCH  /_search/ip-dispense-return-requests?query=:query : search for the iPDispenseReturnRequest corresponding
     * to the query.
     *
     * @param query the query of the iPDispenseReturnRequest search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/ip-dispense-return-requests")
    //@Timed
    //@PreAuthorize("hasPrivilege('102121107') OR hasPrivilege('102121106')")
    public ResponseEntity<List<IPDispenseReturnRequest>> searchIPDispenseReturnRequests(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of IPDispenseReturnRequests for query {}", query);
        Page<IPDispenseReturnRequest> page = iPDispenseReturnRequestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/ip-dispense-return-requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/_search/ip-dispense-return-requests/{type}/{fields:.+}")
    public ResponseEntity<List<IPDispenseReturnRequest>> searchIPDispenseReturnRequestsByFields(@RequestParam String query, Pageable pageable,
                                                                                                @PathVariable String type, @PathVariable String fields) {
        log.debug("REST request to search for a page of IpDispenseReturnRequests for query {}", query);
        try {
            String[] selectedFields = fields.split(",");
            Page<IPDispenseReturnRequest> page = iPDispenseReturnRequestService.searchByFields(query, pageable, type.equals("i") ? selectedFields : null, type.equals("e") ? selectedFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/ip-dispense-return-requests");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("No Index found for {}", e);
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/ip-dispense-return-requests"),
                HttpStatus.OK);
        }
    }

    /**
     * This method used to construct initial object for ip dispense return request with privilege IP Pharmacy Return Request Modify
     * @param query
     * @return
     */
    @GetMapping("/create-ip-dispense-request/ip-dispense-return-requests")
    //@Timed
    @PreAuthorize("hasPrivilege('102121104') OR hasPrivilege('102121106') OR hasPrivilege('102121105')")
    public List<IPDispenseReturnDocumentLine> getIPDispenseLineItems(@RequestParam String query,@RequestParam String patientMrn,@RequestParam String visitNumber)
    {
        log.debug("REST request to create ip dispense return request with query {}", query);
        List<IPDispenseReturnDocumentLine> ipDispenseReturnDocumentLineList=iPDispenseReturnRequestService.constructIPDispenseReturnRequest(query,patientMrn,visitNumber);
        return ipDispenseReturnDocumentLineList;
    }

    /**
     * This method is used to either accept or reject the return request
     * @param ipDispenseReturnRequest
     * @param action either accept or reject
     * @return
     */
    @PostMapping("/process-ip-dispense-request/ip-dispense-return-requests")
//    @Timed
    @PreAuthorize("hasPrivilege('102121106') OR hasPrivilege('102121104')")
    public ResponseEntity<Object> processIPDispenseReturnRequest(@RequestBody IPDispenseReturnRequest ipDispenseReturnRequest, @RequestParam String action) throws Exception
    {
        Map<String, Object> responseMap = new HashMap<>();
        log.debug("REST request to create ip dispense return request with data {}", ipDispenseReturnRequest);
        try {
            responseMap = iPDispenseReturnRequestService.processIPDispenseReturnRequest(ipDispenseReturnRequest, action,responseMap);
            IPDispenseReturnRequest result = (IPDispenseReturnRequest)responseMap.get(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST);
            iPDispenseReturnRequestService.updateAcceptedAndPendingReturnQty(result);
            if (PharmacyConstants.REJECT.equalsIgnoreCase(action)) {
                return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert("iPDispenseReturnRequest", result.getId().toString()))
                    .body(result);
            } else {
                DispenseReturn dispenseReturn = (DispenseReturn) responseMap.get(PharmacyConstants.IP_DISPENSE_RETURN);
                return ResponseEntity.created(new URI("/api/ip-return/" + dispenseReturn.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert("iPDispenseReturnRequest", dispenseReturn.getId().toString()))
                    .body(dispenseReturn);
            }
        }
        catch (Exception ex)
        {
            if(ex instanceof CustomParameterizedException)
            {
                throw ex;
            }
            log.error("Error while processing the ip dispense return request. Ex={}",ex);
            responseMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,ipDispenseReturnRequest);
            iPDispenseReturnRequestService.reIndexReturnRequest(responseMap);
            throw new Exception("Exception while processing ip dispense return request");
        }
    }

    /**
     * Method to export ip dispense return request data
     * @param query
     * @param pageable
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @GetMapping("/_export/ip-dispense-return-requests")
    @PreAuthorize("hasPrivilege('102121107') OR hasPrivilege('102121106')")
    //@Timed
    public Map<String,String> exportIPDispenseReturnRequests(@RequestParam String query, Pageable pageable)
        throws URISyntaxException,IOException {
        log.debug("REST request to export ip pharmacy return request");
        return iPDispenseReturnRequestService.exportIPDispenseReturnRequest(query,pageable);
    }

    @GetMapping("/_index/ip-dispense-return-requests")
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexIPDispenseReturnRequests(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to index ip dispense return request records");
        long resultCount = ipDispenseReturnRequestRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            iPDispenseReturnRequestService.doIndex(i, pageSize, fromDate, toDate);
        }
        ipDispenseReturnRequestSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_export/ip-dispense-return")
    @PreAuthorize("hasPrivilege('102121102')")
    //@Timed
    public Map<String,String> exportIPDispenseReturns(@RequestParam String query, Pageable pageable)
        throws URISyntaxException,IOException {
        log.debug("REST request to export ip pharmacy returns");
        return iPDispenseReturnRequestService.exportIPDispenseReturn(query,pageable);
    }

    @GetMapping("/ip-pending-dispense-return-orders")
    public ResponseEntity<List<IPDispenseReturnRequestDTO>> getIPDispenseReturns(@RequestParam String mrn, @RequestParam String visitNumber) throws Exception{
        log.debug("REST request to get getIPDispenseReturns mrn = {}, visitNumber = {}",mrn,visitNumber);
        List<IPDispenseReturnRequestDTO> dispenseReturns = iPDispenseReturnRequestService.getIPDispenseReturns(mrn,visitNumber, Constants.ORDERS);
        return new ResponseEntity<>(dispenseReturns, HttpStatus.OK);
    }

    @GetMapping("/ip-pending-dispense-return-requests-qty")
    public ResponseEntity<List<IPDispenseReturnRequestDTO>> getIPDispenseReturnsQty(@RequestParam String mrn, @RequestParam String visitNumber) throws Exception{
        log.debug("REST request to getIPDispenseReturnsQty mrn = {}, visitNumber = {}",mrn,visitNumber);
        List<IPDispenseReturnRequestDTO> dispenseReturns = iPDispenseReturnRequestService.getIPDispenseReturns(mrn,visitNumber,Constants.ORDERS_QUANTITY);
        return new ResponseEntity<>(dispenseReturns, HttpStatus.OK);
    }

    /**
     * Method to handle ip pharmacy direct return
     * @param iPDispenseReturnRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/ip-dispense-direct-return")
    //@Timed
    @PreAuthorize("hasPrivilege('102121105')")
    public ResponseEntity<DispenseReturn> createAndProcessIPDispenseReturnRequest(@RequestBody IPDispenseReturnRequest iPDispenseReturnRequest) throws Exception {
        log.debug("REST request to save IPDispenseReturnRequest : {}", iPDispenseReturnRequest);
        if (iPDispenseReturnRequest.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("iPDispenseReturnRequest", "idexists", "A new iPDispenseReturnRequest cannot already have an ID")).body(null);
        }
        iPDispenseReturnRequestService.validatePendingRequests(iPDispenseReturnRequest);
        Map<String,Object> dispenseReturnMap = new HashMap<>();
        try {
            dispenseReturnMap = iPDispenseReturnRequestService.processIPDispenseDirectReturn(iPDispenseReturnRequest,dispenseReturnMap);
            DispenseReturn dispenseReturn = (DispenseReturn) dispenseReturnMap.get(PharmacyConstants.IP_DISPENSE_RETURN);
            return ResponseEntity.created(new URI("/api/ip-return/" + dispenseReturn.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("iPDispenseReturnRequest", dispenseReturn.getId().toString()))
                .body(dispenseReturn);
        }catch(Exception ex)
        {
            log.error("Error while saving ip dispense return request record. Ex={}",ex);
            dispenseReturnMap.put(PharmacyConstants.IP_DISPENSE_RETURN_REQUEST,iPDispenseReturnRequest);
            iPDispenseReturnRequestService.reIndexReturnRequest(dispenseReturnMap);
            throw new Exception("Exception while saving ip dispense return request");
        }
    }
    @GetMapping("/dispense-return-request/print/pdf")
    //@Timed
    public ResponseEntity<Resource> getReturnRequestPdf(@RequestParam(required = false) Long dispenseReturnRequestId,
                                                        @RequestParam(required = false) String dispenseReturnRequestNumber) throws Exception {
        log.debug("REST request to get dispenseReturn by dispenseReturnId : {}, dispenseReturnNumber : {}", dispenseReturnRequestId, dispenseReturnRequestNumber);
        byte[] content = iPDispenseReturnRequestPdfService.getIpDispenseReturnRequestPdf(dispenseReturnRequestId, dispenseReturnRequestNumber);
        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }


}
