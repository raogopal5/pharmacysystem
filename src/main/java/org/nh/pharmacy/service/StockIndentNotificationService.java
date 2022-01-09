package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockIndentNotificationService.
 */
public interface StockIndentNotificationService {

    void notifyIndentApprovalCommittee(Map content);

    void notifyIndentInitiator(String content);

    void notifyIssueStore(Map content);
}
