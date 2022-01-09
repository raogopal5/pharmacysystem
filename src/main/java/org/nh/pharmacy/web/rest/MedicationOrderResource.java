package org.nh.pharmacy.web.rest;


import io.swagger.annotations.ApiParam;
import org.nh.common.util.PaginationUtil;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.MedicationOrder;
import org.nh.pharmacy.repository.MedicationOrderRepository;
import org.nh.pharmacy.repository.search.MedicationOrderSearchRepository;
import org.nh.pharmacy.service.MedicationOrderService;
import org.nh.pharmacy.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing MedicationOrder.
 */
@RestController
@RequestMapping("/api")
public class MedicationOrderResource {

    private final Logger log = LoggerFactory.getLogger(MedicationOrderResource.class);

    private MedicationOrderService medicationOrderService;

    private final MedicationOrderRepository medicationOrderRepository;

    private final MedicationOrderSearchRepository medicationOrderSearchRepository;

    private final ApplicationProperties applicationProperties;

    public MedicationOrderResource(MedicationOrderService medicationOrderService, MedicationOrderRepository medicationOrderRepository, MedicationOrderSearchRepository medicationOrderSearchRepository, ApplicationProperties applicationProperties) {
        this.medicationOrderService = medicationOrderService;
        this.medicationOrderRepository=medicationOrderRepository;
        this.medicationOrderSearchRepository=medicationOrderSearchRepository;
        this.applicationProperties = applicationProperties;
    }


    /**
     * GET  /medication-orders/:id : get the "id" medicationOrder.
     *
     * @param id the id of the medicationOrder to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the medicationOrder, or with status 404 (Not Found)
     */
    @GetMapping("/medication-orders/{id}")
    //@Timed
    public ResponseEntity<MedicationOrder> getMedicationOrder(@PathVariable Long id) {
        log.debug("REST request to get MedicationOrder : {}", id);
        MedicationOrder medicationOrder = medicationOrderService.findOne(id);
        return Optional.ofNullable(medicationOrder)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * SEARCH  /_search/medication-orders?query=:query : search for the medicationOrder corresponding
     * to the query.
     *
     * @param query the query of the medicationOrder search
     * @return the result of the search
     */
    @GetMapping("/_search/medication-orders")
    //@Timed
    public ResponseEntity<List<MedicationOrder>> searchMedicationOrders(@RequestParam String query, @ApiParam Pageable pageable) throws URISyntaxException {
        log.debug("REST request to search MedicationOrders for query {}", query);
        Page<MedicationOrder> page = medicationOrderService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/_search/medication-orders");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * INDEX  /_index/service-request : do elastic index for the medication order
     *
     * @return message about completion of index
     */
    @GetMapping("/_index/medication-orders")
    public ResponseEntity<Void> indexMedicationOrder(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate) {
        log.debug("REST request to do elastic index on medication order");
        long resultCount = medicationOrderRepository.getTotalRecord(fromDate, toDate);
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            medicationOrderService.doIndex(i, pageSize, fromDate, toDate);
        }
        medicationOrderSearchRepository.refresh();

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Elastic index is completed", "")).build();
    }


}
