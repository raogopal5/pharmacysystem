package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockIssueNotificationService.
 */
public interface StockIssueNotificationService {

    void notifyIssueApprovalCommittee(Map content);

    void notifyIssueInitiator(String content);

    void notifyIndentInitiator(Map content);
}
