package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.dto.IssueDocumentLine;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockIssueRepository;
import org.nh.pharmacy.repository.search.StockIssueSearchRepository;
import org.nh.pharmacy.service.StockIssueService;
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
 * REST controller for managing StockIssue.
 */
@RestController
@RequestMapping("/api")
public class StockIssueResource {

    private final Logger log = LoggerFactory.getLogger(StockIssueResource.class);

    private static final String ENTITY_NAME = "stockIssue";

    private final StockIssueService stockIssueService;
    private final StockIssueRepository stockIssueRepository;
    private final StockIssueSearchRepository stockIssueSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockIssueResource(StockIssueService stockIssueService, StockIssueRepository stockIssueRepository, StockIssueSearchRepository stockIssueSearchRepository,
                              ApplicationProperties applicationProperties) {
        this.stockIssueService = stockIssueService;
        this.stockIssueRepository = stockIssueRepository;
        this.stockIssueSearchRepository = stockIssueSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /stock-issues : Create a new stockIssue.
     *
     * @param stockIssue the stockIssue to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockIssue, or with status 400 (Bad Request) if the stockIssue has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-issues")
    //@Timed
    @PreAuthorize("hasPrivilege('101101101') OR hasPrivilege('101114101')")
    public ResponseEntity<StockIssue> createStockIssue(@Valid @RequestBody StockIssue stockIssue,@RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save StockIssue : {}", stockIssue);
        if (stockIssue.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockIssue cannot already have an ID")).body(null);
        }
        StockIssue result;
        if(action == null) action = "DRAFT";
        try {
            result = stockIssueService.save(stockIssue,action);
            if("SENDFORAPPROVAL".equals(action) || "APPROVED".equals(action)){
                if (result.getDocument().getDocumentType().equals(TransactionType.Stock_Issue) || result.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Issue)) {
                    stockIssueService.checkForIndentConversionCompletion(result);
                }
            }
        } catch (Exception e){
            stockIssueService.reIndex(stockIssue.getId());
            throw e;
        }
        return ResponseEntity.created(new URI("/api/stock-issues/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-issues : Updates an existing stockIssue.
     *
     * @param stockIssue the stockIssue to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockIssue,
     * or with status 400 (Bad Request) if the stockIssue is not valid,
     * or with status 500 (Internal Server Error) if the stockIssue couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-issues")
    //@Timed
    @PreAuthorize("hasPrivilege('101101101') OR hasPrivilege('101101105') OR hasPrivilege('101114101')")
    public ResponseEntity<StockIssue> updateStockIssue(@Valid @RequestBody StockIssue stockIssue, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update StockIssue : {}", stockIssue);
        if(action == null) action = "DRAFT";
        if (stockIssue.getId() == null) {
            return createStockIssue(stockIssue, action);
        }
        StockIssue result;
        try {
            result = stockIssueService.save(stockIssue, action);
            if("SENDFORAPPROVAL".equals(action) || "APPROVED".equals(action)){
                if (result.getDocument().getDocumentType().equals(TransactionType.Stock_Issue) || result.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Issue)) {
                    stockIssueService.checkForIndentConversionCompletion(result);
                }
            }
        } catch (Exception e) {
            stockIssueService.reIndex(stockIssue.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockIssue.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-issues : get all the stockIssues.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockIssues in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-issues")
    //@Timed
    @PreAuthorize("hasPrivilege('101101102') OR hasPrivilege('101114102')")
    public ResponseEntity<List<StockIssue>> getAllStockIssues(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockIssues");
        Page<StockIssue> page = stockIssueService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-issues");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-issues/:id : get the "id" stockIssue.
     *
     * @param id the id of the stockIssue to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockIssue, or with status 404 (Not Found)
     */
    @GetMapping("/stock-issues/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101101102') OR hasPrivilege('101114102')")
    public ResponseEntity<StockIssue> getStockIssue(@PathVariable Long id) {
        log.debug("REST request to get StockIssue : {}", id);
        StockIssue stockIssue = stockIssueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockIssue));
    }

    /**
     * DELETE  /stock-issues/:id : delete the "id" stockIssue.
     *
     * @param id the id of the stockIssue to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-issues/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101101101') OR hasPrivilege('101114101')")
    public ResponseEntity<Void> deleteStockIssue(@PathVariable Long id) throws BusinessRuleViolationException {
        log.debug("REST request to delete StockIssue : {}", id);
        stockIssueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-issues?query=:query : search for the stockIssue corresponding
     * to the query.
     *
     * @param query the query of the stockIssue search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-issues")
    //@Timed
    public ResponseEntity<List<StockIssue>> searchStockIssues(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockIssues for query {}", query);
        Page<StockIssue> page = stockIssueService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-issues");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/stock-issues?query=:query : search for the stockIssue corresponding
     * to the query.
     *
     * @param query the query of the stockIssue search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-issues/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockIssue>> searchStockIssues(@RequestParam String query, @ApiParam Pageable pageable,
                                                              @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockIssues for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockIssue> page = stockIssueService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-issues");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-issues"),
                HttpStatus.OK);
        }
    }

    /**
     * Convert Stock Indent to Stock Issue
     *
     * @return message about completion of index
     */
    @GetMapping("/_convert/stock-issues/from-stock-indent")
    //@Timed
    public ResponseEntity<StockIssue> convertIndentToIssue(@RequestParam(required = false) Long docId, @RequestParam(required = false) String docNo) throws Exception {
        log.debug("REST request to do convert Issue Document from Indent Document DocumentId : {} or Document Number : {}", docId, docNo);
        StockIssue result = stockIssueService.convertIndentToIssue(docId,docNo);
        return ResponseEntity.ok()
            .body(result);
    }

    /**
     * GET  /status-count/stock-issues?query=:query : get the status count for the stockIssue corresponding
     * to the query.
     *
     * @param query the query of the stockIssue search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-issues")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockIssueStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a status count of StockIssues");
        Map<String, Long> countMap = stockIssueService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * PUT  /_workflow/stock-issues : call execute workflow to complete the task and save the stock issue object.
     *
     * @param stockIssue
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-issues")
    //@Timed
    public ResponseEntity<StockIssue> executeWorkflow(@Valid @RequestBody StockIssue stockIssue, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        StockIssue result;
        try {
            result = stockIssueService.executeWorkflow(stockIssue, transition, taskId);
            if(Status.WAITING_FOR_APPROVAL.equals(result.getDocument().getStatus()) || Status.APPROVED.equals(result.getDocument().getStatus())){
                if (result.getDocument().getDocumentType().equals(TransactionType.Stock_Issue) || result.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Issue)) {
                    stockIssueService.checkForIndentConversionCompletion(result);
                }
            }
        }catch (Exception e) {
            stockIssueService.reIndex(stockIssue.getId());
            throw e;
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockIssue.getId().toString()))
            .body(result);
    }

    /**
     *  Get  /_workflow/stock-issues : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-issues")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = stockIssueService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * INDEX  /_index/stock-issues : do elastic index for the stock issues
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-issues")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockIndent(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Issue");
        long resultCount = stockIssueRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockIssueService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockIssueSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     *  Get  /_newline/stock-issues : get stockIssueLineItem.
     *
     * @param storeId
     * @param itemId
     * @return issueDocumentLine
     * @throws Exception
     */
    @GetMapping("/_newline/stock-issues")
    //@Timed
    public ResponseEntity<IssueDocumentLine> getStockIssueLineItem(@RequestParam Long storeId, @RequestParam Long itemId) throws Exception {
        log.debug("REST request to get stockIssueLineItem for given storeId: {} against itemId: {} ", storeId, itemId);
        IssueDocumentLine issueDocumentLine = stockIssueService.getStockIssueLineItem(storeId, itemId);
        return ResponseEntity.ok()
            .body(issueDocumentLine);
    }

    @GetMapping("/_relatedDocuments/stock-issues")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = stockIssueService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }

    @GetMapping("_issue/print")
    //@Timed
    @PreAuthorize("hasPrivilege('101101102')")
    public ResponseEntity<byte[]> getInvoiceHTML(@RequestParam(required = false) Long issueId, @RequestParam(required = false) String issueNumber) throws Exception {
        log.debug("REST request to get Issue Document by id  : {}, isuse number : {}", issueId, issueNumber);
        Map<String, Object> fileOutPut = new HashMap<>();
        String docType="";
        fileOutPut = stockIssueService.getStockIssueHTML(issueId, issueNumber,docType);
        byte[] content = (byte[]) fileOutPut.get("content");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + fileOutPut.get("fileName"));
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @GetMapping("_issue/print/pdf")
    //@Timed
    @PreAuthorize("hasPrivilege('101101102')")
    public ResponseEntity<Resource> getInvoicePdfByDispenseId(@RequestParam(required = false) Long issueId, @RequestParam(required = false) String documentNumber,
                                                              @RequestParam(required = false) String original,@RequestParam(required = false) String documentType) throws Exception {
        log.debug("REST request to Print Issue Document by id : {}, documentNumber : {}", issueId, documentNumber);

        byte[] content = stockIssueService.getStockIssuePDF(issueId, documentNumber, original,documentType);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }
    @GetMapping("_direct-transfer/print/pdf/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101101102') OR hasPrivilege('101114102')")
    public ResponseEntity<Resource> getDirectTransferPrint(@PathVariable Long id)throws Exception {
        log.debug("REST request to get StockIssue : {}", id);
        StockIssue stockIssue = stockIssueService.findOne(id);
        byte[] content = stockIssueService.getDirectTransferPDF(stockIssue);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    @GetMapping("/_regenerate_workflow/stock-issues")
    //@Timed
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to regenerate workflow for the document: {}", documentNumber);

        stockIssueService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }
}
