package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockIndentNotificationService.
 */
public interface StockAuditNotificationService {

    void notifyAuditApprovalCommittee(Map content);

    void notifyAuditors(Map content);

    void notifyAuditInitiator(String content);
}
