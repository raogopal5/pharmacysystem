package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockConsumption;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockConsumptionRepository;
import org.nh.pharmacy.repository.search.StockConsumptionSearchRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.StockConsumptionNotificationService;
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
 * A StockConsumptionNotificationServiceImpl.
 */
@Service("stockConsumptionNotificationService")
public class StockConsumptionNotificationServiceImpl extends NotificationGenericImpl implements StockConsumptionNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockConsumptionNotificationServiceImpl.class);

    private final StockConsumptionSearchRepository consumptionSearchRepository;
    private final StockConsumptionRepository consumptionRepository;

    public StockConsumptionNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockConsumptionSearchRepository stockConsumptionRepository, StockConsumptionRepository consumptionRepository) {
        super(userService, groupService, notificationChannel);
        this.consumptionSearchRepository = stockConsumptionRepository;
        this.consumptionRepository = consumptionRepository;
    }

    @Override
    public void notifyConsumptionApprovalCommittee(Map content) {
        log.debug("Stock consumption content map: {}", content);
        StockConsumption stockConsumption = consumptionRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-consumptions/" + stockConsumption.getId();
        String txnType = "Consumption";
        if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
            txnType = "Consumption Reversal";
        }
        String title = new StringBuilder().append(txnType).append(" (").append(stockConsumption.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append(txnType).append(" (").append(stockConsumption.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));

        params = new HashMap<String, Object>() {{
            put("documentId", stockConsumption.getId());
            put("documentNumber", stockConsumption.getDocumentNumber());
            put("documentCreatedDate", stockConsumption.getDocument().getCreatedDate());
            put("documentType", stockConsumption.getDocument().getDocumentType());
            put("documentStatus", stockConsumption.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing {} approval message for document number:{}", txnType, stockConsumption.getDocumentNumber());
    }

    @Override
    public void notifyConsumptionInitiator(String content) {
        log.debug("Stock consumption content: {}", content);
        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();
        StockConsumption stockConsumption = consumptionSearchRepository.findByDocumentNumber(documentNumber);
        log.debug("Stock consumption init: {} for document number:{}", stockConsumption, documentNumber);
        String createdBy = stockConsumption.getDocument().getCreatedBy().getLogin();
        Member member = super.retrieveMemberDetail(createdBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        String requestedBy = null != stockConsumption.getDocument().getRequestedBy() ? stockConsumption.getDocument().getRequestedBy().getLogin() : null;
        if (requestedBy != null && !createdBy.equals(requestedBy)) {
            member = super.retrieveMemberDetail(requestedBy);
            if (nonNull(member)) {
                memberList.add(member);
            }
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockConsumption.getId());
            put("documentNumber", stockConsumption.getDocumentNumber());
            put("documentCreatedDate", stockConsumption.getDocument().getCreatedDate());
            put("documentType", stockConsumption.getDocument().getDocumentType());
            put("documentStatus", stockConsumption.getDocument().getStatus());
        }};
        String txnType = "Consumption";
        if(TransactionType.Stock_Reversal_Consumption.equals(stockConsumption.getDocument().getDocumentType())){
            txnType = "Consumption Reversal";
        }
        String title = new StringBuilder().append(txnType).append(" (").append(stockConsumption.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append(txnType).append(" (").append(stockConsumption.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing {} approve/reject message for document number:{}", txnType, stockConsumption.getDocumentNumber());
    }
}
