package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A InventoryAdjustmentNotificationService.
 */
public interface InventoryAdjustmentNotificationService {

    void notifyLevelOneApprovalCommittee(Map content);

    void notifyLevelTwoApprovalCommittee(Map content);

    void notifyAdjustmentInitiator(String content);
}
