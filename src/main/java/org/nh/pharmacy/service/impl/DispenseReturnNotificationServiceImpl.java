package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.DispenseReturnRepository;
import org.nh.pharmacy.service.DispenseReturnNotificationService;
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
 * A DispenseReturnNotificationServiceImpl.
 */
@Service("dispenseReturnNotificationService")
public class DispenseReturnNotificationServiceImpl extends NotificationGenericImpl implements DispenseReturnNotificationService {

    private final Logger log = LoggerFactory.getLogger(DispenseReturnNotificationServiceImpl.class);

    private final DispenseReturnRepository dispenseReturnRepository;

    public DispenseReturnNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, DispenseReturnRepository dispenseReturnRepository) {
        super(userService, groupService, notificationChannel);
        this.dispenseReturnRepository = dispenseReturnRepository;
    }

    @Override
    public void notifyReturnApprovalCommittee(Map content) {

        DispenseReturn dispenseReturn = dispenseReturnRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/dispense-returns/" + dispenseReturn.getId();
        String title = new StringBuilder().append("Return (").append(dispenseReturn.getDocumentNumber()).append(") for  Patient (").append((dispenseReturn.getDocument().getPatient().getDisplayName()==null||dispenseReturn.getDocument().getPatient().getDisplayName().isEmpty())?(dispenseReturn.getDocument().getPatient().getFullName()==null|| dispenseReturn.getDocument().getPatient().getFullName().isEmpty())?"":dispenseReturn.getDocument().getPatient().getFullName():dispenseReturn.getDocument().getPatient().getDisplayName()).append(") MRN:").append((dispenseReturn.getDocument().getPatient().getMrn()==null||dispenseReturn.getDocument().getPatient().getMrn().isEmpty())?dispenseReturn.getDocument().getPatient().getTempNumber():dispenseReturn.getDocument().getPatient().getMrn()).append(") is ready for Approval").toString();
        String body = new StringBuilder().append("A new request for Return (").append(dispenseReturn.getDocumentNumber()).append(") is created For  Patient (").append((dispenseReturn.getDocument().getPatient().getDisplayName()==null||dispenseReturn.getDocument().getPatient().getDisplayName().isEmpty())?(dispenseReturn.getDocument().getPatient().getFullName()==null|| dispenseReturn.getDocument().getPatient().getFullName().isEmpty())?"":dispenseReturn.getDocument().getPatient().getFullName():dispenseReturn.getDocument().getPatient().getDisplayName()).append(") MRN:").append((dispenseReturn.getDocument().getPatient().getMrn()==null||dispenseReturn.getDocument().getPatient().getMrn().isEmpty())?dispenseReturn.getDocument().getPatient().getTempNumber():dispenseReturn.getDocument().getPatient().getMrn()).append(") is ready for Approval. Please click on below link to view the details.").toString();
        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", dispenseReturn.getId());
            put("documentNumber", dispenseReturn.getDocumentNumber());
            put("documentCreatedDate", dispenseReturn.getDocument().getCreatedDate());
            put("documentType", TransactionType.Dispense_Return);
            put("documentStatus", dispenseReturn.getDocument().getDispenseStatus());
            put("cancelledInvoiceNumber", nonNull(dispenseReturn.getDocument().getCancelledInvoiceRef()) ? dispenseReturn.getDocument().getCancelledInvoiceRef().getReferenceNumber() : null);
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing return approval message for document number:{}", dispenseReturn.getDocumentNumber());
    }

    @Override
    public void notifyReturnInitiator(String content) {

        String[] contentDetail = content.split("~");
        String documentId = contentDetail[0];
        String transition = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        DispenseReturn dispenseReturn = dispenseReturnRepository.findOne(Long.valueOf(documentId));

        String returnReceivedBy = dispenseReturn.getDocument().getReceivedBy().getLogin();

        Member member = super.retrieveMemberDetail(returnReceivedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", dispenseReturn.getId());
            put("documentNumber", dispenseReturn.getDocumentNumber());
            put("documentCreatedDate", dispenseReturn.getDocument().getCreatedDate());
            put("documentType", TransactionType.Dispense_Return);
            put("documentStatus", dispenseReturn.getDocument().getDispenseStatus());
            put("cancelledInvoiceNumber", nonNull(dispenseReturn.getDocument().getCancelledInvoiceRef()) ? dispenseReturn.getDocument().getCancelledInvoiceRef().getReferenceNumber() : null);
        }};

        String title = new StringBuilder().append("Return (").append(dispenseReturn.getDocumentNumber()).append(") is ").append(transition.toLowerCase()).toString();
        String body = new StringBuilder().append("Return (").append(dispenseReturn.getDocumentNumber()).append(") is ").append(transition.toLowerCase()).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing return approve/reject message for document number:{}", dispenseReturn.getDocumentNumber());
    }
}
