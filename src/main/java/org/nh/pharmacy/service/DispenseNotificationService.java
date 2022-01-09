package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A DispenseNotificationService.
 */
public interface DispenseNotificationService {

    void notifyDiscountApprovalCommittee(Map content);
    void notifyDiscountInitiator(String content);
}
