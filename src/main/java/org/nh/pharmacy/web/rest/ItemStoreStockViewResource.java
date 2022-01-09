package org.nh.pharmacy.web.rest;


import io.swagger.annotations.ApiParam;
import org.nh.common.dto.ItemStoreStockViewDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.nh.pharmacy.domain.Medication;
import org.nh.pharmacy.domain.dto.ItemStoreStockViewGroup;
import org.nh.pharmacy.repository.ItemStoreStockViewRepository;
import org.nh.pharmacy.repository.search.ItemStoreStockViewSearchRepository;
import org.nh.pharmacy.service.ItemStoreStockViewService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for managing ItemStoreStockView.
 */
@RestController
@RequestMapping("/api")
public class ItemStoreStockViewResource {

    private final Logger log = LoggerFactory.getLogger(ItemStoreStockViewResource.class);

    private static final String ENTITY_NAME = "itemStoreStockView";

    private final ItemStoreStockViewService itemStoreStockViewService;
    private final ItemStoreStockViewRepository itemStoreStockViewRepository;
    private final ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;
    private final ApplicationProperties applicationProperties;

    public ItemStoreStockViewResource(ItemStoreStockViewService itemStoreStockViewService, ItemStoreStockViewRepository itemStoreStockViewRepository,
                                      ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository, ApplicationProperties applicationProperties) {
        this.itemStoreStockViewService = itemStoreStockViewService;
        this.itemStoreStockViewRepository = itemStoreStockViewRepository;
        this.itemStoreStockViewSearchRepository = itemStoreStockViewSearchRepository;
        this.applicationProperties = applicationProperties;
    }


    /**
     * SEARCH  /_update/item-store-stock-views : update for the itemStoreStockView corresponding
     * to the item ids passed.
     *
     * @param itemIds the item ids for updating itemStoreStockView
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @PostMapping("/_update/item-store-stock-views")
    //@Timed()
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> doUpdate(@RequestParam Long storeId, @RequestBody Set<Long> itemIds) {
        this.itemStoreStockViewService.doUpdate(storeId, itemIds);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Update initiated")).build();
    }

    /**
     * SEARCH  /_update/item-store-stock-views : update for the itemStoreStockView corresponding
     * to the item ids passed.
     *
     * @param itemIds the item ids for updating itemStoreStockView
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @PostMapping("/_update/item-store-stock-views/sync")
    //@Timed()
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> doUpdateSync(@RequestParam Long storeId, @RequestBody Set<Long> itemIds) {
        this.itemStoreStockViewService.updateItemStoreStockView(itemIds, storeId);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Updated")).build();
    }

    /***
     *
     * @param query
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/unique-item-store-stock-views")
    //@Timed
    public ResponseEntity<List<ItemStoreStockView>> searchUniqueItemStoreStockViews(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search Unique Item StoreStockViews for query {}", query);
        Page<ItemStoreStockView> page = itemStoreStockViewService.searchUniqueItems(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/unique-item-store-stock-views");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * SEARCH  /_search/item-store-stock-views?query=:query : search for the itemStoreStockView corresponding
     * to the query.
     *
     * @param query    the query of the itemStoreStockView search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/item-store-stock-views")
    //@Timed
    public ResponseEntity<List<ItemStoreStockView>> searchItemStoreStockViews(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemStoreStockViews for query {}", query);
        Page<ItemStoreStockView> page = itemStoreStockViewService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-store-stock-views");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param query
     * @param forStoreId
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/items-for-store")
    //@Timed
    public ResponseEntity<List<ItemStoreStockView>> searchDistinctItemStoreStock(@RequestParam String query, @RequestParam(required = true) Long forStoreId, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of searchDistinctItemStoreStock for query {}", query);
        Page<ItemStoreStockView> page = itemStoreStockViewService.searchItems(query, forStoreId, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/items-for-store");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param query
     * @param forStoreId
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/barcode/items-for-store")
    public ResponseEntity<List<ItemStoreStockView>> searchBarcodeItemStoreStock(@RequestParam(required = false)  String query, @RequestParam Long forStoreId,
        @RequestParam String barcode, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of searchBarcodeItemStoreStock for query {}", query);
        Page<ItemStoreStockView> page = itemStoreStockViewService.searchBarcodeItems(query, barcode, forStoreId, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/items-for-store");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param batch
     * @param forStoreId
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/batch/items-for-store")
    public ResponseEntity<List<ItemStoreStockView>> searchByBatchItemStoreStock(@RequestParam Long forStoreId, @RequestParam String batch,
                                                                                @ApiParam Pageable pageable) throws URISyntaxException {
        log.debug("REST request to search for a page of searchByBatchItemStoreStock for forStoreId:{}, batch:{}", forStoreId, batch);
        Page<ItemStoreStockView> page = itemStoreStockViewService.searchBatchItems(batch, forStoreId, pageable);
        return new ResponseEntity<>(page.getContent(), HttpStatus.OK);
    }

    /**
     * INDEX  /_index/item-store-stock-views : do elastic index for the itemStoreStockView
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/item-store-stock-views")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> index(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on itemStoreStockView");
        long resultCount = itemStoreStockViewRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            itemStoreStockViewService.doIndex(i, pageSize, fromDate, toDate);
        }
        itemStoreStockViewSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

    /**
     * POST  /item-store-stock-views : Create a new itemStoreStockView.
     *
     * @param itemStoreStockView the itemStoreStockView to create
     * @return the ResponseEntity with status 201 (Created) and with body the new itemStoreStockView, or with status 400 (Bad Request) if the itemStoreStockView has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/item-store-stock-views")
    //@Timed
    public ResponseEntity<ItemStoreStockView> createItemStoreStockView(@Valid @RequestBody ItemStoreStockView itemStoreStockView) throws URISyntaxException {
        log.debug("REST request to save ItemStoreStockView : {}", itemStoreStockView);
        if (itemStoreStockView.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new itemStoreStockView cannot already have an ID")).body(null);
        }
        ItemStoreStockView result = itemStoreStockViewService.save(itemStoreStockView);
        return ResponseEntity.created(new URI("/api/item-store-stock-views/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * GET  _search/_generic/item-store-stock : Search Generic type Items.
     *
     * @param genericId the generic id of madication
     * @return return list of ItemStoreStockViewDTO
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("_search/_generic/item-store-stock-views")
    //@Timed
    public ResponseEntity<List<ItemStoreStockViewDTO>> searchGenericItemStoreStock(@RequestParam Long genericId, @ApiParam(required = false) Pageable pageable,@RequestParam Long storeId,@RequestParam Boolean availableStock)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemStoreStock for query {}", genericId);
        List<ItemStoreStockViewDTO> listOfItem = itemStoreStockViewService.searchGenericItem(genericId, storeId,pageable,availableStock);
        return new ResponseEntity<>(listOfItem,HttpStatus.OK);
    }



    /**
     * POST  _search/item-store-stock : Search Generic type Items.
     *
     * @param medication the generic id of madication
     * @return return list of ItemStoreStockViewDTO
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("_search/_generic/item-store-stock-views")
    //@Timed
    public ResponseEntity<List<ItemStoreStockViewDTO>> searchItemStoreStock(@Valid @RequestBody Medication medication,@RequestParam Long storeId)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemStoreStock for query {}", medication.getCode());
        List<ItemStoreStockViewDTO> listofItem = itemStoreStockViewService.searchStockItem(medication,storeId);
        return new ResponseEntity<>(listofItem,HttpStatus.OK);
    }

    /**
     * SEARCH  /_search/_item-availability/item-store-stock-views?query=:query : search for the ItemStoreStockViewGroup corresponding
     * to the query.
     *
     * @param query    the query of the ItemStoreStockViewGroup search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/_item-availability/item-store-stock-views")
    //@Timed
    public ResponseEntity<List<ItemStoreStockViewGroup>> searchItemAvailabilityItemStoreStockViews(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ItemStoreStockViews for query {}", query);
        List<ItemStoreStockViewGroup> result = itemStoreStockViewService.searchItemAvailabilityItemStoreStockViews(query, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /***
     *
     * @param storeId
     * @param categoryCode
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/item/stocks/{storeId}")
    //@Timed
    public ResponseEntity<List<Map<String, Object>>> getItemStockByStoreIdAndCategoryCode(@PathVariable Long storeId, @RequestParam String categoryCode)
        throws URISyntaxException {
        log.debug("REST Request to get Item Stock for given storeId and categoryCode");
        List<Map<String, Object>> result = itemStoreStockViewService.getItemStockByStoreIdAndCategoryCode(storeId, categoryCode);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
