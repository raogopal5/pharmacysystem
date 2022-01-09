package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ItemBarcode;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.nh.pharmacy.repository.ItemBarcodeRepository;
import org.nh.pharmacy.repository.search.ItemBarcodeSearchRepository;
import org.nh.pharmacy.service.ItemBarcodeService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.nh.security.PrivilegeConstant;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ItemBarcode.
 */
@RestController
@RequestMapping("/api")
public class ItemBarcodeResource {

    private final Logger log = LoggerFactory.getLogger(ItemBarcodeResource.class);

    private static final String ENTITY_NAME = "itemBarcode";

    private final ItemBarcodeService itemBarcodeService;
    private final ItemBarcodeRepository itemBarcodeRepository;
    private final ItemBarcodeSearchRepository itemBarcodeSearchRepository;
    private final ApplicationProperties applicationProperties;

    public ItemBarcodeResource(ItemBarcodeService itemBarcodeService, ItemBarcodeRepository itemBarcodeRepository,
                               ItemBarcodeSearchRepository itemBarcodeSearchRepository, ApplicationProperties applicationProperties) {
        this.itemBarcodeService = itemBarcodeService;
        this.itemBarcodeRepository = itemBarcodeRepository;
        this.itemBarcodeSearchRepository = itemBarcodeSearchRepository;
        this.applicationProperties = applicationProperties;
    }
    /**
     * @param itemBarcode
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/item-barcodes")
    //@Timed
    public ResponseEntity<ItemBarcode> createItemBarcode(@RequestBody ItemBarcode itemBarcode) throws URISyntaxException {
        log.debug("REST request to save ItemBarcode : {}", itemBarcode);
        if (itemBarcode.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("itemBarcode", "idexists", "A new itemBarcode cannot already have an ID")).body(null);
        }
        ItemBarcode result = itemBarcodeService.save(itemBarcode);
        return ResponseEntity.created(new URI("/api/item-barcodes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("itemBarcode", result.getId().toString()))
            .body(result);
    }

    /**
     * @param itemBarcode
     * @return
     * @throws URISyntaxException
     */
    @PutMapping("/item-barcodes")
    //@Timed
    public ResponseEntity<ItemBarcode> updateItemBarcode(@RequestBody ItemBarcode itemBarcode) throws URISyntaxException {
        log.debug("REST request to update ItemBarcode : {}", itemBarcode);
        if (itemBarcode.getId() == null) {
            return createItemBarcode(itemBarcode);
        }
        ItemBarcode result = itemBarcodeService.save(itemBarcode);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("itemBarcode", itemBarcode.getId().toString()))
            .body(result);
    }

    /**
     * @param pageable
     * @return
     */
    @GetMapping("/item-barcodes")
    //@Timed
    public ResponseEntity<List<ItemBarcode>> getAllItemBarcodes(@ApiParam Pageable pageable) {
        log.debug("REST request to get all ItemBarcodes");
        Page<ItemBarcode> page = itemBarcodeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/item-barcodes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/item-barcodes/{id}")
    //@Timed
    public ResponseEntity<ItemBarcode> getItemBarcode(@PathVariable Long id) {
        log.debug("REST request to get ItemBarcode : {}", id);
        ItemBarcode itemBarcode = itemBarcodeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemBarcode));
    }

    /**
     * @param id
     * @return
     */
    @DeleteMapping("/item-barcodes/{id}")
    //@Timed
    public ResponseEntity<Void> deleteItemBarcode(@PathVariable Long id) {
        log.debug("REST request to delete ItemBarcode : {}", id);
        itemBarcodeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * @param query
     * @param pageable
     * @return
     */
    @GetMapping("/_search/item-barcodes")
    //@Timed
    public ResponseEntity<List<ItemBarcode>> searchItemBarcodes(@RequestParam String query, @ApiParam Pageable pageable) {
        log.debug("REST request to search for a page of ItemBarcode for query {}", query);
        Page<ItemBarcode> page = itemBarcodeService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/item-barcodes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param barcode
     * @param storeId
     * @return
     */
    @GetMapping("/_search/item-for-barcode/{barcode}/{storeId}")
    //@Timed
    public ResponseEntity<List<ItemStoreStockView>> searchItemForBarcode(@PathVariable String barcode, @PathVariable String storeId) {
        log.debug("REST request to search for a page of ItemBarcode for Strore Id:" + storeId + "Barcode:" + barcode);
        List<ItemStoreStockView> items = itemBarcodeService.searchStoreItemForBarcode(barcode, storeId);
        HttpHeaders headers = HeaderUtil.createAlert(ENTITY_NAME, "Items for stores");
        return new ResponseEntity<>(items, headers, HttpStatus.OK);
    }

    /**
     * @return
     */
    @GetMapping("/_index/item-barcodes")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexItemBarcodes(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Item Barcode");
        long resultCount = itemBarcodeRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            itemBarcodeService.doIndex(i, pageSize, fromDate, toDate);
        }
        itemBarcodeSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

}
