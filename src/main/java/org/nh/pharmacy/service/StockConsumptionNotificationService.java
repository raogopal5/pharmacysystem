package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A StockConsumptionNotificationService.
 */
public interface StockConsumptionNotificationService {

    void notifyConsumptionApprovalCommittee(Map content);

    void notifyConsumptionInitiator(String content);
}
