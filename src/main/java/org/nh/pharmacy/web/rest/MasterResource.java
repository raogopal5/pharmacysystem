package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.domain.dto.FlowTypeEnumDto;
import org.nh.pharmacy.domain.dto.StatusEnumDto;
import org.nh.pharmacy.domain.dto.TransactionTypeEnumDto;
import org.nh.pharmacy.service.MasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing Master Data.
 */
@RestController
@RequestMapping("/api")
public class MasterResource {

    private final Logger log = LoggerFactory.getLogger(MasterResource.class);

    private final MasterService masterService;

    public MasterResource(MasterService masterService) {
        this.masterService = masterService;
    }

    /**
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/transactiontype/all")
    //@Timed
    public ResponseEntity<List<TransactionTypeEnumDto>> getAllTransactionTypes() throws URISyntaxException {
        log.debug("REST request to get all TransactionTypeEnumDto");
        List<TransactionTypeEnumDto> transactionTypes = masterService.getAllTransactionTypes();
        return new ResponseEntity<>(transactionTypes, HttpStatus.OK);
    }

    /**
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/flowtype/all")
    //@Timed
    public ResponseEntity<List<FlowTypeEnumDto>> getAllFlowTypes() throws URISyntaxException {
        log.debug("REST request to get all Flow Type");
        List<FlowTypeEnumDto> flowTypes = masterService.getAllFlowTypes();
        return new ResponseEntity<>(flowTypes, HttpStatus.OK);
    }

    /**
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/status/all")
    //@Timed
    public ResponseEntity<List<StatusEnumDto>> getAllStatusTypes() throws URISyntaxException {
        log.debug("REST request to get all Status Type");
        List<StatusEnumDto> status = masterService.getAllStatus();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
