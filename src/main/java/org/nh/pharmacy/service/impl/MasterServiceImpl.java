package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.domain.dto.FlowTypeEnumDto;
import org.nh.pharmacy.domain.dto.StatusEnumDto;
import org.nh.pharmacy.domain.dto.TransactionTypeEnumDto;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.service.MasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Service
@Transactional
public class MasterServiceImpl implements MasterService {

    private final Logger log = LoggerFactory.getLogger(MasterServiceImpl.class);


    public MasterServiceImpl() {
    }

    /**
     * @return
     */
    public List<TransactionTypeEnumDto> getAllTransactionTypes() {
        List<TransactionTypeEnumDto> txnTypes = new ArrayList<>();
        for (TransactionType txn : TransactionType.values()) {
            TransactionTypeEnumDto txnTypeEnumDto = new TransactionTypeEnumDto();
            txnTypeEnumDto.setName(txn.getTransactionType());
            txnTypeEnumDto.setDisplayName(txn.getTransactionTypeDisplay());
            txnTypes.add(txnTypeEnumDto);
        }
        Collections.sort(txnTypes, TransactionTypeEnumDto.displayNameComparator);
        return txnTypes;
    }

    /**
     * @return
     */
    public List<FlowTypeEnumDto> getAllFlowTypes() {
        List<FlowTypeEnumDto> flowTypes = new ArrayList<>();
        for (FlowType flow : FlowType.values()) {
            FlowTypeEnumDto flowTypeEnumDto = new FlowTypeEnumDto();
            flowTypeEnumDto.setName(flow.getFlowType());
            flowTypeEnumDto.setDisplayName(flow.getFlowTypeDisplay());
            flowTypes.add(flowTypeEnumDto);
        }
        Collections.sort(flowTypes, FlowTypeEnumDto.displayNameComparator);
        return flowTypes;
    }


    public List<StatusEnumDto> getAllStatus() {
        List<StatusEnumDto> statusTypes = new ArrayList<>();
        for (Status status : Status.values()) {
            StatusEnumDto statusEnumDto = new StatusEnumDto();
            statusEnumDto.setName(status.getStatus());
            statusEnumDto.setDisplayName(status.getStatusDisplay());
            statusTypes.add(statusEnumDto);
        }
        Collections.sort(statusTypes, StatusEnumDto.displayNameComparator);
        return statusTypes;
    }
}
