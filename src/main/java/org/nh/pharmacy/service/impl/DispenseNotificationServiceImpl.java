package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.DispenseRepository;
import org.nh.pharmacy.service.DispenseNotificationService;
import org.nh.pharmacy.service.GroupService;
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
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A DispenseNotificationServiceImpl.
 */
@Service("dispenseNotificationService")
public class DispenseNotificationServiceImpl extends NotificationGenericImpl implements DispenseNotificationService {

    private final Logger log = LoggerFactory.getLogger(DispenseNotificationServiceImpl.class);

    private final DispenseRepository dispenseRepository;

    public DispenseNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, DispenseRepository dispenseRepository) {
        super(userService, groupService, notificationChannel);
        this.dispenseRepository = dispenseRepository;
    }

    @Override
    public void notifyDiscountApprovalCommittee(Map content) {

        Dispense dispense = dispenseRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/dispenses/" + dispense.getId();
        String title = new StringBuilder().append("Dispense (").append(dispense.getDocumentNumber()).append(") for  Patient (").append((dispense.getDocument().getPatient().getDisplayName()==null||dispense.getDocument().getPatient().getDisplayName().isEmpty())?(dispense.getDocument().getPatient().getFullName()==null|| dispense.getDocument().getPatient().getFullName().isEmpty())?"":dispense.getDocument().getPatient().getFullName():dispense.getDocument().getPatient().getDisplayName()).append(" MRN:").append(dispense.getDocument().getPatient().getMrn()).append(") is ready for Approval").toString();
        String body = new StringBuilder().append("A new request for Dispense (").append(dispense.getDocumentNumber()).append(") is created For  Patient (").append((dispense.getDocument().getPatient().getDisplayName()==null||dispense.getDocument().getPatient().getDisplayName().isEmpty())?(dispense.getDocument().getPatient().getFullName()==null|| dispense.getDocument().getPatient().getFullName().isEmpty())?"":dispense.getDocument().getPatient().getFullName():dispense.getDocument().getPatient().getDisplayName()).append(" MRN:").append(dispense.getDocument().getPatient().getMrn()).append(") is ready for Approval. Please click on below link to view the details.").toString();
        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", dispense.getId());
            put("documentNumber", dispense.getDocumentNumber());
            put("documentCreatedDate", dispense.getDocument().getCreatedDate());
            put("documentType", TransactionType.Dispense);
            put("documentStatus", dispense.getDocument().getDispenseStatus());
            put("invoiceNumber", nonNull(dispense.getDocument().getSource()) ? dispense.getDocument().getSource().getReferenceNumber() : null);
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing dispense discount approval message for document number:{}", dispense.getDocumentNumber());
    }

    @Override
    public void notifyDiscountInitiator(String content) {

        String[] contentDetail = content.split("~");
        String documentId = contentDetail[0];
        String transition = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        Dispense dispense = dispenseRepository.findOne(Long.valueOf(documentId));
        String dispenseCreatedBy = dispense.getDocument().getDispenseUser().getLogin();

        Member member = super.retrieveMemberDetail(dispenseCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", dispense.getId());
            put("documentNumber", dispense.getDocumentNumber());
            put("documentCreatedDate", dispense.getDocument().getCreatedDate());
            put("documentType", TransactionType.Dispense);
            put("documentStatus", dispense.getDocument().getDispenseStatus());
            put("invoiceNumber", nonNull(dispense.getDocument().getSource()) ? dispense.getDocument().getSource().getReferenceNumber() : null);
        }};

        String title = new StringBuilder().append("Dispense (").append(dispense.getDocumentNumber()).append(") is ").append(transition.toLowerCase()).toString();
        String body = new StringBuilder().append("Dispense (").append(dispense.getDocumentNumber()).append(") is ").append(transition.toLowerCase()).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing dispense discount approve/reject message for document number:{}", dispense.getDocumentNumber());
    }
}
