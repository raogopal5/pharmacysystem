package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.repository.StockIndentRepository;
import org.nh.pharmacy.repository.search.StockIndentSearchRepository;
import org.nh.pharmacy.service.StockIndentService;
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
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for managing StockIndent.
 */
@RestController
@RequestMapping("/api")
public class StockIndentResource {

    private final Logger log = LoggerFactory.getLogger(StockIndentResource.class);

    private static final String ENTITY_NAME = "stockIndent";

    private final StockIndentService stockIndentService;
    private final StockIndentRepository stockIndentRepository;
    private final StockIndentSearchRepository stockIndentSearchRepository;
    private final ApplicationProperties applicationProperties;

    public StockIndentResource(StockIndentService stockIndentService, ApplicationProperties applicationProperties,StockIndentRepository stockIndentRepository,
                               StockIndentSearchRepository stockIndentSearchRepository) {
        this.stockIndentService = stockIndentService;
        this.applicationProperties = applicationProperties;
        this.stockIndentRepository = stockIndentRepository;
        this.stockIndentSearchRepository = stockIndentSearchRepository;
    }

    /**
     * POST  /stock-indents : Create a new stockIndent.
     *
     * @param stockIndent the stockIndent to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stockIndent, or with status 400 (Bad Request) if the stockIndent has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100101')")
    public ResponseEntity<StockIndent> createStockIndent(@Valid @RequestBody StockIndent stockIndent, @RequestParam(required = false) String act) throws Exception {
        log.debug("REST request to save StockIndent : {}", stockIndent);
        if (act == null) {
            act = "DRAFT";
        }
        if (stockIndent.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stockIndent cannot already have an ID")).body(null);
        }
        StockIndent result = stockIndentService.save(stockIndent, act);
        stockIndentService.index(result);
        return ResponseEntity.created(new URI("/api/stock-indents/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-indents : Updates an existing stockIndent.
     *
     * @param stockIndent the stockIndent to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockIndent,
     * or with status 400 (Bad Request) if the stockIndent is not valid,
     * or with status 500 (Internal Server Error) if the stockIndent couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100101') OR hasPrivilege('101100105')")
    public ResponseEntity<StockIndent> updateStockIndent(@Valid @RequestBody StockIndent stockIndent, @RequestParam(required = false) String act) throws Exception {
        log.debug("REST request to update StockIndent : {}", stockIndent);
        if (act == null) {
            act = "DRAFT";
        }
        if (stockIndent.getId() == null) {
            return createStockIndent(stockIndent, act);
        }
        StockIndent result = stockIndentService.save(stockIndent, act);
        stockIndentService.index(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockIndent.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-indents : get all the stockIndents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockIndents in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100102')")
    public ResponseEntity<List<StockIndent>> getAllStockIndents(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockIndents");
        Page<StockIndent> page = stockIndentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-indents");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-indents/:id : get the "id" stockIndent.
     *
     * @param id the id of the stockIndent to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockIndent, or with status 404 (Not Found)
     */
    @GetMapping("/stock-indents/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101100102')")
    public ResponseEntity<StockIndent> getStockIndent(@PathVariable Long id) {
        log.debug("REST request to get StockIndent : {}", id);
        StockIndent stockIndent = stockIndentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockIndent));
    }

    /**
     * DELETE  /stock-indents/:id : delete the "id" stockIndent.
     *
     * @param id the id of the stockIndent to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-indents/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101100101')")
    public ResponseEntity<Void> deleteStockIndent(@PathVariable Long id) throws BusinessRuleViolationException {
        log.debug("REST request to delete StockIndent : {}", id);
        stockIndentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-indents?query=:query : search for the stockIndent corresponding
     * to the query.
     *
     * @param query    the query of the stockIndent search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-indents")
    //@Timed
    public ResponseEntity<List<StockIndent>> searchStockIndents(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockIndents for query {}", query);
        Page<StockIndent> page = stockIndentService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-indents");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/stock-indents?query=:query : search for the stockIndent corresponding
     * to the query.
     *
     * @param query the query of the stockIndent search
     * @param pageable the pagination information
     * @param type the type which should be either i for include or e for exclude fields
     * @param fields the fields which should be considered to include or exclude
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-indents/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockIndent>> searchStockIndents(@RequestParam String query, @ApiParam Pageable pageable,
                                                    @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockIndents for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockIndent> page = stockIndentService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-indents");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch(SearchPhaseExecutionException e) {
            log.error("No Index found for {}",e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-indents"),
                HttpStatus.OK);
        }
    }

    /**
     * PUT  /_workflow/stock-indents : call execute workflow to complete the task and save the stock indent object.
     *
     * @param stockIndent
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-indents")
    //@Timed
    public ResponseEntity<StockIndent> executeWorkflow(@Valid @RequestBody StockIndent stockIndent, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {}", taskId);
        StockIndent result = stockIndentService.executeWorkflow(stockIndent, transition, taskId);
        stockIndentService.index(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockIndent.getId().toString()))
            .body(result);
    }

    /**
     * GET  /status-count/stock-indents?query=:query : get the status count for the stockIndent corresponding
     * to the query.
     *
     * @param query    the query of the stockIndent search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-indents")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockIndentStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a status count of StockIndents");
        Map<String, Long> countMap = stockIndentService.getStatusCount(query);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     *  Get  /_workflow/stock-indents : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-indents")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {}", documentNumber);

        Map<String, Object> taskDetails = stockIndentService.getTaskConstraints(documentNumber, userId, taskId);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * GET  /_copy/stock-indents/ : copies stock Indent based on indent id or document number.
     * @param id
     * @param docNum
     * @return
     */
    @GetMapping("/_copy/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100101')")
    public ResponseEntity<StockIndent> copyStockIndent(@RequestParam(required = false) Long id, @RequestParam(required = false) String docNum) {
        log.debug("REST request to copy StockIndent ");
        StockIndent stockIndent = stockIndentService.copyStockIndent(id,docNum);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockIndent));
    }

    /**
     * INDEX  /_index/stock-indents : do elastic index for the stock indents
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-indents")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockIndent(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Features");
        long resultCount = stockIndentRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockIndentService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockIndentSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     * GET  /_import/stock-indents/ : import csv files of stock document lines
     * @param file
     * @param storeId
     * @param indentStoreId
     * @return
     */
    @PostMapping("/_import/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100101') OR hasPrivilege('101100103')")
    public ResponseEntity<Map> importStockIndentDocumentLine(@RequestBody MultipartFile file, @RequestParam Long storeId, @RequestParam Long indentStoreId) throws IOException {
        log.debug("REST request to import StockIndentDocumentLines ");
        Map map = stockIndentService.importStockIndentDocumentLine(file,storeId,indentStoreId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }

    /**
     * GET  /_export/stock-indents/template : export stock indent sample template csv file
     * @return
     */
    @GetMapping("/_download/stock-indents/template")
    //@Timed
    public void downloadStockIndentTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("REST request to download Stock Indent Template");

        String parentPath = applicationProperties.getAthmaBucket().getTemplate();
        String fileName = "stock_indent_import_template.csv";
        File file = new File(parentPath + fileName);
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

        try (FileInputStream inputStream = new FileInputStream(file);
             OutputStream outStream = response.getOutputStream();) {
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            // write bytes read from the input stream into the output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
        response.flushBuffer();
    }

    @GetMapping("/_relatedDocuments/stock-indents")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = stockIndentService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }

    @GetMapping("_print/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100102')")
    public ResponseEntity<byte[]> getStockIndentHTMLByIndentId(@RequestParam(required = false) Long indentId,String documentNumber) throws Exception {
        log.debug("REST request to get Stock Indent by indentId : {}  indentNumber : {}", indentId, documentNumber);
        Map<String, Object> fileOutPut = new HashMap<>();
        fileOutPut = stockIndentService.getStockIndentHTMLByIndentId(indentId,documentNumber);
        byte[] content = (byte[]) fileOutPut.get("content");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + fileOutPut.get("fileName"));
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentLength(content.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
    }

    @GetMapping("_print/pdf/stock-indents")
    //@Timed
    @PreAuthorize("hasPrivilege('101100102')")
    public ResponseEntity<Resource> getStockIndentPdfByIndentId(@RequestParam(required = false) Long indentId, @RequestParam(required = false) String documentNumber,
                                                              @RequestParam(required = false) String original) throws Exception {
        log.debug("REST request to get Stock Indent by indentId : {}, documentNumber : {}", indentId, documentNumber);

        byte[] content = stockIndentService.getStockIndentPdfByIndentId(indentId, documentNumber, original);

        return ResponseEntity.ok()
            .contentLength(content.length)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(content));
    }

    @GetMapping("/_regenerate_workflow/stock-indents")
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Map<String, Object>> regenerateWorkflow(@RequestParam String documentNumber) throws Exception {
        log.debug("REST request to regenerate workflow for the document: {}", documentNumber);

        stockIndentService.regenerateWorkflow(documentNumber);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Regenerated workflow for the document {}", documentNumber))
            .body(null);
    }
}
