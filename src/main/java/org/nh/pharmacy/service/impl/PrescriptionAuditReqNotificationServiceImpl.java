package org.nh.pharmacy.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.nh.billing.domain.Invoice;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.NotificationOf;
import org.nh.pharmacy.domain.enumeration.NotificationType;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.PrescriptionAuditReqNotificationService;
import org.nh.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service("prescriptionAuditReqNotificationService")
public class PrescriptionAuditReqNotificationServiceImpl extends NotificationGenericImpl implements PrescriptionAuditReqNotificationService {

    private final Logger log = LoggerFactory.getLogger(PrescriptionAuditReqNotificationService.class);

    public PrescriptionAuditReqNotificationServiceImpl(UserService userService, GroupService groupService,
                                                       @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel) {
        super(userService, groupService, notificationChannel);
    }

    @Override
    public void notifyPrescriptionAuditRequestRejection(PrescriptionAuditRequest prescriptionAuditRequest) {
        List<Member> memberList = new ArrayList<>();
       log.info("notify prescription audit rejection to users: {} ", prescriptionAuditRequest.getDocumentNumber());
        String link = "http://localhost:8080/api/prescription-audit-requests/" + prescriptionAuditRequest.getId();

        String title = new StringBuilder().append("Pharmacy order[").append(prescriptionAuditRequest.getDocumentNumber())
            .append("] is cancelled by prescription auditor\"").append(prescriptionAuditRequest.getDocument().getAuditBy().getDisplayName())
            .append("\"").toString();

        String body = new StringBuilder().append("Pharmacy order[").append(prescriptionAuditRequest.getDocumentNumber())
            .append("] is cancelled by prescription auditor\"").append(prescriptionAuditRequest.getDocument().getAuditBy().getDisplayName())
            .append("\"").toString();

        Member auditedBy = super.retrieveMemberDetail(prescriptionAuditRequest.getDocument().getAuditBy().getLogin());
        Member createdBy = super.retrieveMemberDetail(prescriptionAuditRequest.getDocument().getCreatedBy().getLogin());

        if (nonNull(auditedBy)) {
            memberList.add(auditedBy);
        }
        if (nonNull(createdBy)) {
            memberList.add(createdBy);
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", prescriptionAuditRequest.getId());
            put("documentType", "Prescription_Audit_Request");
        }};

        super.publishNotification(new Notification(title, body, NotificationType.application, NotificationOf.workflow, params, memberList));

    }
}
