package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.repository.StockReceiptRepository;
import org.nh.pharmacy.repository.search.StockReceiptSearchRepository;
import org.nh.pharmacy.service.StockReceiptService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for managing StockReceipt.
 */
@RestController
@RequestMapping("/api")
public class StockReceiptResource {

    private final Logger log = LoggerFactory.getLogger(StockReceiptResource.class);

    private static final String ENTITY_NAME = "stockReceipt";

    private final StockReceiptService stockReceiptService;
    private final StockReceiptRepository stockReceiptRepository;
    private final StockReceiptSearchRepository stockReceiptSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockReceiptResource(StockReceiptService stockReceiptService, StockReceiptRepository stockReceiptRepository,
                                StockReceiptSearchRepository stockReceiptSearchRepository, ApplicationProperties applicationProperties) {
        this.stockReceiptService = stockReceiptService;
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockReceiptSearchRepository = stockReceiptSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /stock-receipts : Create a new stockReceipt.
     *
     * @param stockReceipt the stockReceipt to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockReceipt, or with status 400 (Bad Request) if the stockReceipt has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-receipts")
    //@Timed
    @PreAuthorize("hasPrivilege('101103101')")
    public ResponseEntity<StockReceipt> createStockReceipt(@Valid @RequestBody StockReceipt stockReceipt, @RequestParam(required = false) String act) throws Exception {
        log.debug("REST request to save StockReceipt : {}", stockReceipt);
        if (stockReceipt.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockReceipt cannot already have an ID")).body(null);
        }
        StockReceipt result;
        if (act == null) {
            act = "DRAFT";
        }
        try{
            result = stockReceiptService.save(stockReceipt, act);
            stockReceiptService.checkForCompleteConversionOfSourceDocument(result,act);
        } catch (Exception e) {
            if(stockReceipt.getId() != null) stockReceiptService.reIndex(stockReceipt.getId());
            throw e;
        }

        return ResponseEntity.created(new URI("/api/stock-receipts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-receipts : Updates an existing stockReceipt.
     *
     * @param stockReceipt the stockReceipt to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockReceipt,
     * or with status 400 (Bad Request) if the stockReceipt is not valid,
     * or with status 500 (Internal Server Error) if the stockReceipt couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-receipts")
    //@Timed
    @PreAuthorize("hasPrivilege('101103101') OR hasPrivilege('101103105')")
    public ResponseEntity<StockReceipt> updateStockReceipt(@Valid @RequestBody StockReceipt stockReceipt, @RequestParam(required = false) String act) throws Exception {
        log.debug("REST request to update StockReceipt : {}", stockReceipt);
        if (stockReceipt.getId() == null) {
            return createStockReceipt(stockReceipt, act);
        }
        if (act == null) {
            act = "DRAFT";
        }
        StockReceipt result;
        try{
            result = stockReceiptService.save(stockReceipt, act);
            stockReceiptService.checkForCompleteConversionOfSourceDocument(result,act);
        } catch (Exception e) {
            if(stockReceipt.getId() != null) stockReceiptService.reIndex(stockReceipt.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockReceipt.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-receipts : get all the stockReceipts.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockReceipts in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-receipts")
    //@Timed
    @PreAuthorize("hasPrivilege('101103102')")
    public ResponseEntity<List<StockReceipt>> getAllStockReceipts(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockReceipts");
        Page<StockReceipt> page = stockReceiptService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-receipts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-receipts/:id : get the "id" stockReceipt.
     *
     * @param id the id of the stockReceipt to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockReceipt, or with status 404 (Not Found)
     */
    @GetMapping("/stock-receipts/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101103102')")
    public ResponseEntity<StockReceipt> getStockReceipt(@PathVariable Long id) {
        log.debug("REST request to get StockReceipt : {}", id);
        StockReceipt stockReceipt = stockReceiptService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockReceipt));
    }

    /**
     * DELETE  /stock-receipts/:id : delete the "id" stockReceipt.
     *
     * @param id the id of the stockReceipt to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-receipts/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101103101')")
    public ResponseEntity<Void> deleteStockReceipt(@PathVariable Long id) throws BusinessRuleViolationException {
        log.debug("REST request to delete StockReceipt : {}", id);
        stockReceiptService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-receipts?query=:query : search for the stockReceipt corresponding
     * to the query.
     *
     * @param query the query of the stockReceipt search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-receipts")
    //@Timed
    public ResponseEntity<List<StockReceipt>> searchStockReceipts(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockReceipts for query {}", query);
        Page<StockReceipt> page = stockReceiptService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-receipts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/stock-receipts?query=:query : search for the stockReceipt corresponding
     * to the query.
     *
     * @param query the query of the stockReceipt search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-receipts/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockReceipt>> searchStockReceipts(@RequestParam String query, @ApiParam Pageable pageable,
                                                                @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockReceipts for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockReceipt> page = stockReceiptService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-receipts");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-receipts"),
                HttpStatus.OK);
        }
    }

    /**
     * Convert Stock Issue to Stock Receipt
     * @param docId (Stock Issue id)
     * @param docNo (Stock Issue Document Number)
     * @return
     * @throws Exception
     */
    @GetMapping("/_convert/stock-receipts/from-stock-issue")
    //@Timed
    public ResponseEntity<StockReceipt> convertIssueToReceipt(@RequestParam(required = false) Long docId, @RequestParam(required = false) String docNo) throws Exception {
        log.debug("REST request to do convert Receipt Document DocumentId : {} or Document Number : {}", docId, docNo);
        StockReceipt result = stockReceiptService.convertIssueToReceipt(docId, docNo);
        return ResponseEntity.ok()
            .body(result);
    }

    /**
     * Convert Stock Reversal to Stock Receipt
     * @param docId (Stock Reversal Id)
     * @param docNo (Stock Reversal Dccument Number)
     * @return
     * @throws Exception
     */
    @GetMapping("/_convert/stock-receipts/from-stock-reversal")
    //@Timed
    public ResponseEntity<StockReceipt> convertReversalToReceipt(@RequestParam(required = false) Long docId, @RequestParam(required = false) String docNo) throws Exception {
        log.debug("REST request to do convert Receipt Document DocumentId : {} or Document Number : {}", docId, docNo);
        StockReceipt result = stockReceiptService.convertReversalToReceipt(docId, docNo);
        return ResponseEntity.ok()
            .body(result);
    }

    /**
     * GET  /status-count/stock-receipts?query=:query : get the status count for the stockReceipt corresponding
     * to the query.
     *
     * @param query the query of the stockReceipt search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-receipts")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockReceiptStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a status count of StockReceipts");
        Map<String, Long> countMap = stockReceiptService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * PUT  /_workflow/stock-receipts : call execute workflow to complete the task and save the stock issue object.
     *
     * @param stockReceipt
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-receipts")
    //@Timed
    public ResponseEntity<StockReceipt> executeWorkflow(@Valid @RequestBody StockReceipt stockReceipt, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        StockReceipt result;
        try {
            result = stockReceiptService.executeWorkflow(stockReceipt, transition, taskId);
            String action = null;
            if (Status.WAITING_FOR_APPROVAL.equals(result.getDocument().getStatus())) {
                action = "SENDFORAPPROVAL";
            }
            if (Status.APPROVED.equals(result.getDocument().getStatus())) {
                action = "APPROVED";
            }
            if (Status.REJECTED.equals(result.getDocument().getStatus())) {
                action = "REJECTED";
            }
            stockReceiptService.checkForCompleteConversionOfSourceDocument(result,action);
        } catch (Exception e) {
            stockReceiptService.reIndex(stockReceipt.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockReceipt.getId().toString()))
            .body(result);
    }

    /**
     *  Get  /_workflow/stock-receipts : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-receipts")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = stockReceiptService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * INDEX  /_index/stock-receipts : do elastic index for the stock receipts
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-receipts")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockIndent(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Receipt");
        long resultCount = stockReceiptRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockReceiptService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockReceiptSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    @GetMapping("/_relatedDocuments/stock-receipts")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = stockReceiptService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }

    /**
     * to print stock receipt
     * @param receiptId
     * @param documentNumber
     * @return
     * @throws Exception
     */
    @GetMapping("_print/stock-receipts")
    //@Timed
    @PreAuthorize("hasPrivilege('101103102')")
    public ResponseEntity<byte[]> getStockRecieptHTMLByReceiptId(@RequestParam(required = false) Long receiptId, @RequestParam(required = false) String documentNumber) throws Exception {
        log.debug("REST request to get Stock Receipt by receiptId : {}, receiptNumber : {}", receiptId, documentNumber);
        Map<String, Object> fileOutPut = new HashMap<>();
        fileOutPut = stockReceiptService.getStockRecieptHTMLByReceiptId(receiptId, documentNumber);
        byte[] content = (byte[]) fileOutPut.get("content");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + fileOutPut.get("fileName"));
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    /**
     * to generate pdf of stock receipt
     * @param receiptId
     * @param documentNumber
     * @param original
     * @return
     * @throws Exception
     */
    @GetMapping("_print/pdf/stock-receipts")
    //@Timed
    @PreAuthorize("hasPrivilege('101103102')")
    public ResponseEntity<Resource> getStockReceiptPdfByReceiptId(@RequestParam(required = false) Long receiptId, @RequestParam(required = false) String documentNumber,
                                                                @RequestParam(required = false) String original) throws Exception {
        log.debug("REST request to get Stock Receipt by receiptId : {}, documentNumber : {}", receiptId, documentNumber);

        byte[] content = stockReceiptService.getStockReceiptPdfByReceiptId(receiptId, documentNumber, original);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    @GetMapping("/_regenerate_workflow/stock-receipts")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to regenerate workflow for the document: {}", documentNumber);

        stockReceiptService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }

}
