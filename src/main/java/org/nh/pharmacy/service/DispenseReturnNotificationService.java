package org.nh.pharmacy.service;

import java.util.Map;

/**
 * A DispenseReturnNotificationService.
 */
public interface DispenseReturnNotificationService {

    void notifyReturnApprovalCommittee(Map content);
    void notifyReturnInitiator(String content);
}
