package org.nh.pharmacy.service;

import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;

public interface PrescriptionAuditReqNotificationService {
    void notifyPrescriptionAuditRequestRejection(PrescriptionAuditRequest creatorAndOrderedUserInfo);

}
