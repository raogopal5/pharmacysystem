package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.ItemBatchInfo;
import org.nh.pharmacy.domain.Stock;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.service.ItemBatchInfoService;
import org.nh.pharmacy.service.StockService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.util.UserPreferencesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST controller for managing Stock.
 */
@RestController
@RequestMapping("/api")
public class StockResource {

    private final Logger log = LoggerFactory.getLogger(StockResource.class);

    private static final String ENTITY_NAME = "stock";

    private final StockService stockService;
    private final ItemBatchInfoService itemBatchInfoService;

    public StockResource(StockService stockService, ItemBatchInfoService itemBatchInfoService) {
        this.stockService = stockService;
        this.itemBatchInfoService = itemBatchInfoService;
    }

    /**
     * POST  /stocks : Create a new stock.
     *
     * @param stock the stock to create
     * @return the ResponseEntity with status 201 (Created) and with body the new stock, or with status 400 (Bad Request) if the stock has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/stocks")
    //@Timed
    public ResponseEntity<Stock> createStock(@Valid @RequestBody Stock stock) throws URISyntaxException {
        log.debug("REST request to save Stock : {}", stock);
        if (stock.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new stock cannot already have an ID")).body(null);
        }
        Stock result = stockService.save(stock);
        return ResponseEntity.created(new URI("/api/stocks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /stocks : Updates an existing stock.
     *
     * @param stock the stock to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated stock,
     * or with status 400 (Bad Request) if the stock is not valid,
     * or with status 500 (Internal Server Error) if the stock couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/stocks")
    //@Timed
    public ResponseEntity<Stock> updateStock(@Valid @RequestBody Stock stock) throws URISyntaxException {
        log.debug("REST request to update Stock : {}", stock);
        if (stock.getId() == null) {
            return createStock(stock);
        }
        Stock result = stockService.save(stock);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stock.getId().toString()))
            .body(result);
    }

    /**
     * GET  /stocks : get all the stocks.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stocks in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stocks")
    //@Timed
    public ResponseEntity<List<Stock>> getAllStocks(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Stocks");
        Page<Stock> page = stockService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stocks");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stocks/:id : get the "id" stock.
     *
     * @param id the id of the stock to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the stock, or with status 404 (Not Found)
     */
    @GetMapping("/stocks/{id}")
    //@Timed
    public ResponseEntity<Stock> getStock(@PathVariable Long id) {
        log.debug("REST request to get Stock : {}", id);
        Stock stock = stockService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(stock));
    }

    /**
     * DELETE  /stocks/:id : delete the "id" stock.
     *
     * @param id the id of the stock to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/stocks/{id}")
    //@Timed
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        log.debug("REST request to delete Stock : {}", id);
        stockService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * POST  /external/stocks : Perform StockInOut.
     *
     * @param stockEntry the stock to create
     * @return the ResponseEntity with status 200 (Created) and with body the new stock entry, or with status 500 (Internal Server Error)
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/external/stocks")
    //@Timed
    public ResponseEntity<StockEntry> stockInOut(@RequestBody StockEntry stockEntry) throws URISyntaxException {
        if (stockEntry.getAvailableQuantity()>0){
            try {
                stockService.createStockSourceHeader(stockEntry);
            }
            catch (Exception e){
                log.error("Exception occured while saving StockSourceHeader", e);
            }
        }
        stockEntry.setUserId(UserPreferencesUtils.getCurrentUserPreferences().getUser().getId());
        if (stockEntry.getTransactionType().equals(TransactionType.GRN)) {
            stockEntry.setOriginalBatchNo(stockEntry.getBatchNo());
            stockEntry.setOriginalExpiryDate(stockEntry.getExpiryDate());
            stockEntry.setOriginalMRP(stockEntry.getMrp());
            stockEntry.setStockFlowId(stockService.stockInWithTxn((Arrays.asList(stockEntry))).get(0).getId());
            itemBatchInfoService.createIfNotExists(new ItemBatchInfo(stockEntry.getItemId(), stockEntry.getBatchNo()));
        } else {
            try {
                stockService.reserveStockForStockEntry(stockEntry);
                stockEntry.setStockFlowId(stockService.stockOut(stockEntry.getTransactionNumber()).get(0).getId());
            } catch (Exception e) {
                stockService.deleteReservedStock(stockEntry.getTransactionId(), stockEntry.getTransactionType());
                throw e;
            }
        }
        return ResponseEntity.ok().body(stockEntry);
    }

    /**
     * GET  /stocks/{id}/{code} : get the batch details for "itemCode".
     *
     * @param  storeId,itemCode
     * @return the ResponseEntity with status 200 (OK) and the batch details in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stocks/{storeId}/{docNumber}")
    //@Timed
    public ResponseEntity<List<Stock>> getBatchDetails(@PathVariable("storeId") Long storeId,
                                                       @PathVariable("docNumber") String docNumber,
                                                       @RequestParam(value = "code", required = true) String itemCode,
                                                       @RequestParam(value = "batchNo", required = false) String batchNo,
                                                       @RequestParam(required = false, value = "filterBlockedBatch") Boolean filterBlockedBatch,
                                                       @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of batch details");
        Page<Stock> page = stockService.getBatchDetails(storeId, itemCode, batchNo, docNumber,filterBlockedBatch, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stocks");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stocks/batch : get the batch details for "itemCode" and filterQuantity.
     *
     * @param  storeId,itemCode
     * @return the ResponseEntity with status 200 (OK) and the batch details in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stocks/batch")
    //@Timed
    public ResponseEntity<List<Stock>> getAllBatchDetail(@RequestParam(value = "storeId") Long storeId, @RequestParam(value = "itemCode") String itemCode,
             @RequestParam(required = false, value = "filterBlockedBatch") Boolean filterBlockedBatch, @RequestParam(value = "filterQuantity") Boolean filterQty, @ApiParam Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get a page of batch details for storeId:{}, itemCode:{}, filterQty:{}, filterBlockedBatch:{}",storeId, itemCode, filterQty, filterBlockedBatch);
        if(null == filterBlockedBatch)filterBlockedBatch = false;
        Page<Stock> page = stockService.getAllBatchDetails(storeId, itemCode, filterQty, filterBlockedBatch, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stocks");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * GET  /stocks/alt-items/{storeId}/{itemId} : get the items list based on the dispensableGenericName for given itemId and storeId.
     *
     * @param  storeId,itemId
     * @return the ResponseEntity with status 200 (OK) and the items list in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stocks/alt-items/{storeId}/{itemId}")
    //@Timed
    public ResponseEntity<List<Map<String, Object>>> getItemListByDispensableGenericName(@PathVariable("storeId") Long storeId, @PathVariable("itemId") Long itemId, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get list of items based on the dispensableGenericName for given itemId and storeId");
        Page<Map<String, Object>> page = stockService.getItemListByDispensableGenericName(storeId, itemId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/stocks");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /stock-value/{unitId} : get the current stock available.
     *
     * @param  unitId,consignment,storeId,itemId
     * @return the ResponseEntity with status 200 (OK) and the current available stock details in body
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("/stock-value/{unitId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101116102')")
    public ResponseEntity<List<Map<String, Object>>> getCurrentAvailableStock(@PathVariable("unitId") Long unitId, @RequestParam(value = "consignment") String consignment, @RequestParam(value = "storeId", required = false) Long storeId, @RequestParam(value = "itemId", required = false) Long itemId)
        throws URISyntaxException {
        log.debug("REST request to get the current stock available for given details");
        Boolean isConsignment = false;
        if(consignment.equalsIgnoreCase("both")){
            isConsignment = null;
        }else{
            isConsignment = Boolean.parseBoolean(consignment);
        }
        List<Map<String, Object>> stockList = stockService.getCurrentAvailableStock(unitId, isConsignment, storeId, itemId);
        return ResponseEntity.ok().body(stockList);
    }

    /**
     * EXPORT  /stock-value/_export/{unitId} : export csv file for the stocks corresponding to the query.
     *
     * @param  unitId,consignment,storeId,itemId
     * @throws URISyntaxException,IOException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/stock-value/_export/{unitId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101116102')")
    public Map<String, String> exportStocks(@PathVariable("unitId") Long unitId, @RequestParam(value = "consignment") String consignment,  @RequestParam(value = "storeId", required = false) Long storeId, @RequestParam(value = "itemId", required = false) Long itemId) throws URISyntaxException, IOException {
        log.debug("REST request to export stocks");
        Boolean isConsignment = false;
        if(consignment.equalsIgnoreCase("both")){
            isConsignment = null;
        }else{
            isConsignment = Boolean.parseBoolean(consignment);
        }
        return stockService.exportStocks(unitId, isConsignment, storeId, itemId);
    }

    @GetMapping("/_refresh/stock/{storeId}")
    //@Timed
    public void refreshStockData(@PathVariable("storeId") Long storeId) {
        log.debug("Request to refresh stock data");
        stockService.updateStockFields(storeId);
    }


    /**
     * GET  /expiry-items/{unitId} : get the expiry items from stock.
     *
     * @param  unitId,consignment,storeId,itemId,categoryId,fromDate,toDate
     * @return the ResponseEntity with status 200 (OK) and the expiry items details in the body
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("/expiry-items/stock/{unitId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101128102')")
    public ResponseEntity<List<Map<String, Object>>> getExpiryItems(@PathVariable("unitId") Long unitId, @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fromDateTime, @RequestParam(value = "toDate") @DateTimeFormat (pattern = "dd/MM/yyyy") LocalDate toDateTime,
                                                                    @RequestParam(value = "consignment") String consignment,@RequestParam(value = "storeId", required = false) Long storeId)
        throws URISyntaxException {
        log.debug("REST request to get the expiry items available for given details");


        LocalDate fromDate = LocalDate.parse(fromDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        LocalDate toDate = LocalDate.parse(toDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Boolean isConsignment = false;
        if(consignment.equalsIgnoreCase("both")){
            isConsignment = null;
        }else{
            isConsignment = Boolean.parseBoolean(consignment);
        }

        List<Map<String, Object>> stockList = stockService.getExpiryItems(unitId,fromDate,toDate,storeId,isConsignment);
        return ResponseEntity.ok().body(stockList);
    }

    /**
     * GET  /expiry-items/_export/{unitId} : export csv file for the expiry items corresponding to the query.
     *
     * @param  unitId,consignment,storeId,itemId,categoryId,fromDate,toDate
     * @return the ResponseEntity with status 200 (OK) and the expiry items details in the body
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("/_export/expiry-items/stock/{unitId}")
    //@Timed
    @PreAuthorize("hasPrivilege('101128102')")
    public Map<String, String> exportExpiryItems(@PathVariable("unitId") Long unitId, @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fromDateTime, @RequestParam(value = "toDate") @DateTimeFormat (pattern = "dd/MM/yyyy") LocalDate toDateTime,
                                                                    @RequestParam(value = "consignment") String consignment,@RequestParam(value = "storeId", required = false) Long storeId)
        throws URISyntaxException, IOException {
        log.debug("REST request to export the expiry items available for given details");

        LocalDate fromDate = LocalDate.parse(fromDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        LocalDate toDate = LocalDate.parse(toDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Boolean isConsignment = false;
        if(consignment.equalsIgnoreCase("both")){
            isConsignment = null;
        }else{
            isConsignment = Boolean.parseBoolean(consignment);
        }

        return stockService.exportExpiryItems(unitId,fromDate,toDate,storeId,isConsignment);
    }

}
