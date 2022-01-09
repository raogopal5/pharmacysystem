package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockDirectTransferNotificationService.
 */
public interface StockDirectTransferNotificationService {

    void notifyDirectTransferApprovalCommittee(Map content);

    void notifyDirectTransferInitiator(String content);

    void notifyReceivingStore(Map content);
}
