package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockReceiptNotificationService.
 */
public interface StockReceiptNotificationService {

    void notifyReceiptApprovalCommittee(Map content);

    void notifyReceiptInitiator(String content);

    void notifyIssueInitiatorOnRejection(Map content);
}
