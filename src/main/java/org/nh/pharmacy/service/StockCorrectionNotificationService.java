package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockCorrectionNotificationService.
 */
public interface StockCorrectionNotificationService {

    void notifyCorrectionApprovalCommittee(Map content);

    void notifyCorrectionInitiator(String content);
}
