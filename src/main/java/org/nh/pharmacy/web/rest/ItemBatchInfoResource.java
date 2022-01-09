package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ItemBatchInfo;
import org.nh.pharmacy.repository.ItemBatchInfoRepository;
import org.nh.pharmacy.repository.search.ItemBatchInfoSearchRepository;
import org.nh.pharmacy.service.ItemBatchInfoService;
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
 * REST controller for managing ItemBatchInfo.
 */
@RestController
@RequestMapping("/api")
public class ItemBatchInfoResource {

    private final Logger log = LoggerFactory.getLogger(ItemBatchInfoResource.class);

    private static final String ENTITY_NAME = "itemBatchInfo";

    private final ItemBatchInfoService itemBatchInfoService;
    private final ItemBatchInfoRepository itemBatchInfoRepository;
    private final ItemBatchInfoSearchRepository itemBatchInfoSearchRepository;
    private final ApplicationProperties applicationProperties;

    public ItemBatchInfoResource(ItemBatchInfoService itemBatchInfoService, ItemBatchInfoRepository itemBatchInfoRepository,
                                 ItemBatchInfoSearchRepository itemBatchInfoSearchRepository, ApplicationProperties applicationProperties) {
        this.itemBatchInfoService = itemBatchInfoService;
        this.itemBatchInfoRepository = itemBatchInfoRepository;
        this.itemBatchInfoSearchRepository = itemBatchInfoSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * @param itemBatchInfo
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/item-batch-infos")
    //@Timed
    public ResponseEntity<ItemBatchInfo> createItemBatchInfo(@RequestBody ItemBatchInfo itemBatchInfo) throws URISyntaxException {
        log.debug("REST request to save ItemBatchInfo : {}", itemBatchInfo);
        if (itemBatchInfo.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("itemBatchInfo", "idexists", "A new itemBatchInfo cannot already have an ID")).body(null);
        }
        ItemBatchInfo result = itemBatchInfoService.save(itemBatchInfo);
        return ResponseEntity.created(new URI("/api/item-batch-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("itemBatchInfo", result.getId().toString()))
            .body(result);
    }

    /**
     * @param itemBatchInfo
     * @return
     * @throws URISyntaxException
     */
    @PutMapping("/item-batch-infos")
    //@Timed
    public ResponseEntity<ItemBatchInfo> updateItemBatchInfo(@RequestBody ItemBatchInfo itemBatchInfo) throws URISyntaxException {
        log.debug("REST request to update ItemBatchInfo : {}", itemBatchInfo);
        if (itemBatchInfo.getId() == null) {
            return createItemBatchInfo(itemBatchInfo);
        }
        ItemBatchInfo result = itemBatchInfoService.save(itemBatchInfo);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("itemBatchInfo", itemBatchInfo.getId().toString()))
            .body(result);
    }

    /**
     * @param pageable
     * @return
     */
    @GetMapping("/item-batch-infos")
    //@Timed
    public ResponseEntity<List<ItemBatchInfo>> getAllItemBatchInfos(@ApiParam Pageable pageable) {
        log.debug("REST request to get all ItemBatchInfos");
        Page<ItemBatchInfo> page = itemBatchInfoService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/item-batch-infos");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/item-batch-infos/{id}")
    //@Timed
    public ResponseEntity<ItemBatchInfo> getItemBatchInfo(@PathVariable Long id) {
        log.debug("REST request to get ItemBatchInfo : {}", id);
        ItemBatchInfo itemBatchInfo = itemBatchInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(itemBatchInfo));
    }

    /**
     * @param id
     * @return
     */
    @DeleteMapping("/item-batch-infos/{id}")
    //@Timed
    public ResponseEntity<Void> deleteItemBatchInfo(@PathVariable Long id) {
        log.debug("REST request to delete ItemBatchInfo : {}", id);
        itemBatchInfoService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * @return
     */
    @GetMapping("/_index/item-batch-infos")
    //@Timed
    @PreAuthorize(PrivilegeConstant.SUPER_USER_PRIVILEGE_EXPRESSION)
    public ResponseEntity<Void> indexItemBatchInfos(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on Item BatchInfo");
        long resultCount = itemBatchInfoRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            itemBatchInfoService.doIndex(i, pageSize, fromDate, toDate);
        }
        itemBatchInfoSearchRepository.refresh();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }

}
