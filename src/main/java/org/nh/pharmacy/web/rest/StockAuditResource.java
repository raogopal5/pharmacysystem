package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.repository.StockAuditRepository;
import org.nh.pharmacy.repository.search.StockAuditSearchRepository;
import org.nh.pharmacy.service.InventoryAdjustmentService;
import org.nh.pharmacy.service.StockAuditService;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST controller for managing StockAudit.
 */
@RestController
@RequestMapping("/api")
public class StockAuditResource {

    private final Logger log = LoggerFactory.getLogger(StockAuditResource.class);

    private static final String ENTITY_NAME = "stockAudit";

    private final StockAuditService stockAuditService;
    private final InventoryAdjustmentService inventoryAdjustmentService;
    private final ApplicationProperties applicationProperties;
    private final StockAuditRepository stockAuditRepository;
    private final StockAuditSearchRepository stockAuditSearchRepository;


    public StockAuditResource(StockAuditService stockAuditService, InventoryAdjustmentService inventoryAdjustmentService, ApplicationProperties applicationProperties,
                              StockAuditRepository stockAuditRepository, StockAuditSearchRepository stockAuditSearchRepository) {
        this.stockAuditService = stockAuditService;
        this.applicationProperties = applicationProperties;
        this.inventoryAdjustmentService = inventoryAdjustmentService;
        this.stockAuditRepository = stockAuditRepository;
        this.stockAuditSearchRepository = stockAuditSearchRepository;
    }

    /**
     * POST  /stock-audits : Create a new stockAudit.
     *
     * @param stockAudit the stockAudit to create
     * @return the ResponseEntity with status 400 (Bad Request) if the stockAudit don't has an ID
     * @throws Exception
     */
    @PostMapping("/stock-audits")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101')")
    public ResponseEntity<StockAudit> createStockAudit(@Valid @RequestBody StockAudit stockAudit, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to save StockAudit : {} starts: {}", stockAudit.getDocumentNumber(), LocalDateTime.now());
        if (stockAudit.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idnotexists", "Create option is not supported by stock audit")).body(null);
        }
        if (!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        StockAudit result;
        try {
            result = stockAuditService.save(stockAudit, action);
        } catch (Exception e) {
            stockAuditService.reIndex(stockAudit.getId());
            inventoryAdjustmentService.reverseAdjustmentDataForAudit(stockAudit.getDocumentNumber());
            throw e;
        }
        log.debug("REST request to save StockAudit : {} ends: {}", stockAudit.getDocumentNumber(), LocalDateTime.now());
        return ResponseEntity.created(new URI("/api/stock-audits/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stock-audits : Updates an existing stockAudit.
     *
     * @param stockAudit the stockAudit to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stockAudit,
     * or with status 400 (Bad Request) if the stockAudit is not valid,
     * or with status 500 (Internal Server Error) if the stockAudit couldnt be updated
     * @throws Exception
     */
    @PutMapping("/stock-audits")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101') OR hasPrivilege('101110105')")
    public ResponseEntity<StockAudit> updateStockAudit(@Valid @RequestBody StockAudit stockAudit, @RequestParam(required = false) String action) throws Exception {
        log.debug("REST request to update StockAudit : {} starts: {}", stockAudit.getDocumentNumber(), LocalDateTime.now());
        if (!Optional.ofNullable(action).isPresent()) action = (Status.DRAFT).name();
        if (stockAudit.getId() == null) {
            return createStockAudit(stockAudit, action);
        }
        StockAudit result;
        try {
                result = stockAuditService.save(stockAudit, action);
        } catch (Exception e) {
            stockAuditService.reIndex(stockAudit.getId());
            inventoryAdjustmentService.reverseAdjustmentDataForAudit(stockAudit.getDocumentNumber());
            throw e;
        }
        log.debug("REST request to update StockAudit : {} ends: {}", stockAudit.getDocumentNumber(), LocalDateTime.now());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockAudit.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stock-audits : get all the stockAudits.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stockAudits in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-audits")
    //@Timed
    @PreAuthorize("hasPrivilege('101110102')")
    public ResponseEntity<List<StockAudit>> getAllStockAudits(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of StockAudits starts: {}", LocalDateTime.now());
        Page<StockAudit> page = stockAuditService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stock-audits");
        log.debug("REST request to get a page of StockAudits starts: {}", LocalDateTime.now());
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-audits/:id : get the "id" stockAudit.
     *
     * @param id the id of the stockAudit to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stockAudit, or with status 404 (Not Found)
     */
    @GetMapping("/stock-audits/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101110102')")
    public ResponseEntity<StockAudit> getStockAudit(@PathVariable Long id) {
        log.debug("REST request to get StockAudit : {} starts: {}", id, LocalDateTime.now());
        StockAudit stockAudit = stockAuditService.findOne(id);
        log.debug("REST request to get StockAudit : {} ends: {}", id, LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stockAudit));
    }

    /**
     * DELETE  /stock-audits/:id : delete the "id" stockAudit.
     *
     * @param id the id of the stockAudit to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stock-audits/{id}")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101')")
    public ResponseEntity<Void> deleteStockAudit(@PathVariable Long id) {
        log.debug("REST request to delete StockAudit : {} starts : {}", id, LocalDateTime.now());
        stockAuditService.delete(id);
        log.debug("REST request to delete StockAudit : {} ends : {}", id, LocalDateTime.now());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/stock-audits?query=:query : search for the stockAudit corresponding
     * to the query.
     *
     * @param query    the query of the stockAudit search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/stock-audits")
    //@Timed
    public ResponseEntity<List<StockAudit>> searchStockAudits(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of StockAudits for query {} starts {}", query, LocalDateTime.now());
        Page<StockAudit> page = stockAuditService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-audits");
        log.debug("REST request to search for a page of StockAudits for query {} ends {}", query, LocalDateTime.now());
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param query
     * @param pageable
     * @param type
     * @param fields
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/stock-audits/{type}/{fields:.+}")
    //@Timed
    public ResponseEntity<List<StockAudit>> searchStockAudits(@RequestParam String query, @ApiParam Pageable pageable,
                                                              @PathVariable String type, @PathVariable String fields)
        throws URISyntaxException {
        log.debug("REST request to search for a page of stock-audits for query {}", query);
        try {
            String[] selectFields = fields.split(",");
            Page<StockAudit> page = stockAuditService.search(query, pageable, type.equals("i") ? selectFields : null, type.equals("e") ? selectFields : null);
            HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-audits");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        } catch (SearchPhaseExecutionException e) {
            log.error("No Index found for {}", e);// nothing to do with the exception hence mode is debug
            Page page = new PageImpl(Collections.emptyList(), pageable, 0);
            return new ResponseEntity(page.getContent(),
                PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/stock-audits"),
                HttpStatus.OK);
        }
    }

    /**
     * GET  /status-count/stock-audits?query=:query : get the status count for the stock audits corresponding
     * to the query.
     *
     * @param query    the query of the dispense search
     * @param pageable the pagination information
     * @return the result of status count
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/status-count/stock-audits")
    //@Timed
    public ResponseEntity<Map<String, Long>> getAllStockAuditsStatusCount(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {

        log.debug("REST request to get a status count of stock audit starts : {}", LocalDateTime.now());
        Map<String, Long> countMap = stockAuditService.getStatusCount(query);
        log.debug("REST request to get a status count of stock audit ends : {}", LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(countMap));
    }

    /**
     * Get all stocks for given item and in a store
     *
     * @param itemId
     * @param storeId
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/stock-audits/stocks")
    //@Timed
    public ResponseEntity<List<AuditDocumentLine>> getAllAuditDocumentLines(@RequestParam Long itemId, @RequestParam Long storeId)
        throws URISyntaxException, StockException {
        log.debug("REST request to get audit document lines starts : {}", LocalDateTime.now());
        List<AuditDocumentLine> auditDocLine = stockAuditService.getAllDocumentLines(itemId, storeId);
        log.debug("REST request to get audit document lines ends : {}", LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(auditDocLine));
    }

    /**
     * INDEX  /_index/stock-audits : do elastic index for the stock audit
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/stock-audits")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexStockAudit(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Stock Audit starts: {}", LocalDateTime.now());
        long resultCount = stockAuditRepository.getTotalLatestRecord(true, fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            stockAuditService.doIndex(i, pageSize, fromDate, toDate);
        }
        stockAuditSearchRepository.refresh();
        log.debug("REST request to do elastic index on Stock Audit ends: {}", LocalDateTime.now());
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     * PUT  /_workflow/stock-audits : call execute workflow to complete the task and save the stock audit object.
     *
     * @param stockAudit
     * @param transition
     * @param taskId
     * @throws Exception
     */
    @PutMapping("/_workflow/stock-audits")
    //@Timed
    public ResponseEntity<StockAudit> executeWorkflow(@Valid @RequestBody StockAudit stockAudit, @RequestParam String transition, @RequestParam Long taskId) throws Exception {
        log.debug("REST request to call work flow for task: {} starts: {}", taskId, LocalDateTime.now());
        StockAudit result;
        try {
            result = stockAuditService.executeWorkflow(stockAudit, transition, taskId);
            stockAuditService.index(result);
        } catch (Exception e) {
            stockAuditService.reIndex(stockAudit.getId());
            inventoryAdjustmentService.reverseAdjustmentDataForAudit(stockAudit.getDocumentNumber());
            throw e;
        }
        log.debug("REST request to call work flow for task: {} ends: {}", taskId, LocalDateTime.now());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stockAudit.getId().toString()))
            .body(result);
    }

    /**
     * Get  /_workflow/stock-audits : get taskId and corresponding constraints details.
     *
     * @param documentNumber
     * @param userId
     * @return taskId, constraints
     * @throws Exception
     */
    @GetMapping("/_workflow/stock-audits")
    //@Timed
    public ResponseEntity<Map<String, Object>> getTaskConstraints(@RequestParam String documentNumber, @RequestParam String userId, @RequestParam(required = false) Long taskId) throws Exception {
        log.debug("REST request to get task constraints for the document: {} starts :{}", documentNumber, LocalDateTime.now());

        Map<String, Object> taskDetails = stockAuditService.getTaskConstraints(documentNumber, userId, taskId);
        log.debug("REST request to get task constraints for the document: {} ends :{}", documentNumber, LocalDateTime.now());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Task details for the document {}", documentNumber))
            .body(taskDetails);
    }

    /**
     * GET  /_export/stock-audits/template : export stock indent sample template csv file
     *
     * @return
     */
    @GetMapping("/_download/stock-audits/template/{docNumber}")
    //@Timed
    public Map<String, String> downloadStockAuditTemplate(HttpServletRequest request, HttpServletResponse response, @PathVariable("docNumber") String docNumber) throws Exception {
        log.debug("REST request to download Stock Audit Template starts: {}", LocalDateTime.now());
        File stockAuditFile = ExportUtil.getXLSXExportFile("stockaudit", docNumber, applicationProperties.getAthmaBucket().getTempExport());
        stockAuditService.generateExcel(docNumber, stockAuditFile);

        Map<String, String> auditFileDetails = new HashMap<>();
        auditFileDetails.put("fileName", stockAuditFile.getName());
        auditFileDetails.put("pathReference", "tempExport");
        log.debug("REST request to download Stock Audit Template ends: {}", LocalDateTime.now());
        return auditFileDetails;
    }

    /**
     * POST  /stock-audits : Create a new stockAudit.
     *
     * @param stockAudit the entity
     * @return the ResponseEntity the stockAudit created
     * @throws Exception
     */
    @PostMapping("/_create-for-criteria/stock-audits")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101')")
    public ResponseEntity<StockAudit> createStockAuditForCriteria(@RequestBody StockAudit stockAudit) throws Exception {
        log.debug("REST request to create stockAudit {} for criteria : {} starts: {}", stockAudit, stockAudit.getAuditCriterias(), LocalDateTime.now());
        StockAudit result = stockAuditService.createStockAuditForCriteria(stockAudit);
        log.debug("REST request to create stockAudit {} for criteria : {} ends: {}", stockAudit, stockAudit.getAuditCriterias(), LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }

    /**
     * POST  /_import/stock-audits/ : import excel file stock audit document lines
     *
     * @param file
     * @param documentId
     * @return
     */
    @PostMapping("/_import/stock-audits")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101') OR hasPrivilege('101110103')")
    public ResponseEntity<StockAudit> importStockAuditDocumentLines(@RequestBody MultipartFile file, @RequestParam Long documentId) throws Exception {
        log.debug("REST request to import Stock Audit Document Lines starts: {}", LocalDateTime.now());
        StockAudit result = stockAuditService.uploadAuditExcel(file, documentId);
        log.debug("REST request to import Stock Audit Document Lines ends: {}", LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }

    /**
     * POST  /stock-audits/addItems : add new items to stock audit lines
     *
     * @param stockAudit
     * @param itemCode
     * @param batchNo
     * @return
     * @throws Exception
     */
    @PostMapping("/stock-audits/addItems")
    //@Timed
    @PreAuthorize("hasPrivilege('101110101') OR hasPrivilege('101110103')")
    public ResponseEntity<StockAudit> addNewItemToStockAudit(@RequestBody StockAudit stockAudit, @RequestParam String itemCode, @RequestParam String batchNo) throws Exception {
        log.debug("REST request to add items to stock document line starts: {}", LocalDateTime.now());
        StockAudit result = stockAuditService.addItemLines(itemCode, batchNo, stockAudit);
        log.debug("REST request to add items to stock document line ends: {}", LocalDateTime.now());
        return ResponseEntity.created(new URI("/api/stock-audits/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * GET  /_export/stock-audits/all : export stock audit Lists
     *
     * @return
     */
    @GetMapping("/_download/stock-audits/all")
    //@Timed
    public Map<String, String> downloadStockAuditList(@RequestParam String query, Pageable pageable) throws Exception {
        log.debug("REST request to download Stock Audit List starts: {}", LocalDateTime.now());
        File stockAuditFile = ExportUtil.getCSVExportFile("stockaudit", applicationProperties.getAthmaBucket().getTempExport());
        stockAuditService.generateStockAuditList(stockAuditFile, query, pageable);

        Map<String, String> auditFileDetails = new HashMap<>();
        auditFileDetails.put("fileName", stockAuditFile.getName());
        auditFileDetails.put("pathReference", "tempExport");
        log.debug("REST request to download Stock Audit List ends: {}", LocalDateTime.now());
        return auditFileDetails;
    }

    /**
     * GET  /_export/stock-audits/template : export stock audit sample template file
     * @return
     */
    @GetMapping("/_download/stock-audits/template")
    //@Timed
    public void downloadStockAuditTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("REST request to download Stock Audit Template");

        String parentPath = applicationProperties.getAthmaBucket().getTemplate();
        String fileName = "stock_audit_import_template.xls";
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

    /**
     * Upload items from excel file
     *
     * @param file
     * @param storeId
     * @Param userId
     * @return result
     * @throws Exception
     */
    @PostMapping("/_import/stock-audits/addItems")
    //@Timed
    public ResponseEntity<Map> uploadNewItemsToAudit(@RequestBody MultipartFile file, @RequestParam Long storeId, @RequestParam Long userId) throws Exception {
        log.debug("REST request to upload Stock Audit items excel starts: {}", LocalDateTime.now());
        Map result = stockAuditService.addAuditLinesFromExcel(file, storeId, userId);
        log.debug("REST request to upload Stock Audit items excel ends: {}", LocalDateTime.now());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }

    @GetMapping("/_relatedDocuments/stock-audits")
    //@Timed
    public ResponseEntity<Map> relatedDocuments(@RequestParam String documentNumber) throws IOException {
        log.debug("REST request to get all RelatedDocuments");
        Map map = stockAuditService.getRelatedDocuments(documentNumber);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(map));
    }
}
