package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockCorrection;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.repository.StockCorrectionRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.StockCorrectionNotificationService;
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
 * A StockCorrectionNotificationServiceImpl.
 */
@Service("stockCorrectionNotificationService")
public class StockCorrectionNotificationServiceImpl extends NotificationGenericImpl implements StockCorrectionNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockCorrectionNotificationServiceImpl.class);

    private final StockCorrectionRepository stockCorrectionRepository;

    public StockCorrectionNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockCorrectionRepository stockCorrectionRepository) {
        super(userService, groupService, notificationChannel);
        this.stockCorrectionRepository = stockCorrectionRepository;
    }

    @Override
    public void notifyCorrectionApprovalCommittee(Map content) {

        StockCorrection stockCorrection = stockCorrectionRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-corrections/" + stockCorrection.getId();
        String title = new StringBuilder().append("Correction (").append(stockCorrection.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Correction (").append(stockCorrection.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));

        params = new HashMap<String, Object>() {{
            put("documentId", stockCorrection.getId());
            put("documentNumber", stockCorrection.getDocumentNumber());
            put("documentCreatedDate", stockCorrection.getDocument().getCreatedDate());
            put("documentType", stockCorrection.getDocument().getType());
            put("documentStatus", stockCorrection.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing correction approval message for document number:{}", stockCorrection.getDocumentNumber());
    }

    @Override
    public void notifyCorrectionInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockCorrection stockCorrection = stockCorrectionRepository.findOneByDocumentNumber(documentNumber);
        String createdBy = stockCorrection.getDocument().getCreatedBy().getLogin();

        Member member = super.retrieveMemberDetail(createdBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockCorrection.getId());
            put("documentNumber", stockCorrection.getDocumentNumber());
            put("documentCreatedDate", stockCorrection.getDocument().getCreatedDate());
            put("documentType", stockCorrection.getDocument().getType());
            put("documentStatus", stockCorrection.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Correction (").append(stockCorrection.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Correction (").append(stockCorrection.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing correction approve/reject message for document number:{}", stockCorrection.getDocumentNumber());
    }
}
