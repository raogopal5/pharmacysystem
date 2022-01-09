package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.dto.FlowTypeEnumDto;
import org.nh.pharmacy.domain.dto.StatusEnumDto;
import org.nh.pharmacy.domain.dto.TransactionTypeEnumDto;

import java.util.List;

/**
 *
 */
public interface MasterService {
    /**
     * @return
     */
    List<TransactionTypeEnumDto> getAllTransactionTypes();

    /**
     * @return
     */
    List<FlowTypeEnumDto> getAllFlowTypes();

    /**
     * @return
     */
    List<StatusEnumDto> getAllStatus();
}
