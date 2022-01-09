package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockAuditRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.StockAuditNotificationService;
import org.nh.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A StockAuditNotificationServiceImpl.
 */
@Service("stockAuditNotificationService")
public class StockAuditNotificationServiceImpl extends NotificationGenericImpl implements StockAuditNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockAuditNotificationServiceImpl.class);

    private final StockAuditRepository stockAuditRepository;

    public StockAuditNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockAuditRepository stockAuditRepository) {
        super(userService, groupService, notificationChannel);
        this.stockAuditRepository = stockAuditRepository;
    }

    @Override
    public void notifyAuditApprovalCommittee(Map content) {

        StockAudit stockAudit = stockAuditRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-audits/" + stockAudit.getId();
        String title = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();
        String templateName = "documentapproval";

        Map params = new HashMap<String, Object>() {{
            put("templateName", templateName);
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));

        params = new HashMap<String, Object>() {{
            put("documentId", stockAudit.getId());
            put("documentNumber", stockAudit.getDocumentNumber());
            put("documentCreatedDate", stockAudit.getDocument().getCreatedDate());
            put("documentType", TransactionType.Stock_Audit);
            put("documentStatus", stockAudit.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing audit approval message for document number:{}", stockAudit.getDocumentNumber());
    }

    @Override
    public void notifyAuditors(Map content) {

        StockAudit stockAudit = stockAuditRepository.findOne(((Long) content.get("document_id")).longValue());

        auditorItemDetails(stockAudit).entrySet().forEach(entry -> {
            String title = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is created").toString();
            String body = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is pending for audit.").append(" Please click on below link to view the details.").toString();
            Map params = new HashMap<String, Object>() {{
                put("documentId", stockAudit.getId());
                put("documentNumber", stockAudit.getDocumentNumber());
                put("documentCreatedDate", stockAudit.getDocument().getCreatedDate());
                put("documentType", TransactionType.Stock_Audit);
                put("documentStatus", stockAudit.getDocument().getStatus());
                put("tableContent", getTableContent((Set<String>) entry.getValue()));
            }};
            super.publishNotification(new Notification(title, body, application, workflow, params, singletonList(super.retrieveMemberDetail(entry.getKey()))));
        });
        log.debug("Publishing auditors message for document number:{}", stockAudit.getDocumentNumber());
    }

    @Override
    public void notifyAuditInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockAudit stockAudit = stockAuditRepository.findOneByDocumentNumber(documentNumber);
        String auditCreatedBy = stockAudit.getDocument().getCreatedBy().getLogin();

        Member member = super.retrieveMemberDetail(auditCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockAudit.getId());
            put("documentNumber", stockAudit.getDocumentNumber());
            put("documentCreatedDate", stockAudit.getDocument().getCreatedDate());
            put("documentType", TransactionType.Stock_Audit);
            put("documentStatus", stockAudit.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Audit (").append(stockAudit.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing audit approve/reject message for document number:{}", stockAudit.getDocumentNumber());
    }

    public List<String> getAuditors(StockAudit stockAudit) {
        Set<String> auditorList = null;
        List<AuditDocumentLine> documentLineList = stockAudit.getDocument().getLines();
        if (isNotEmpty(documentLineList)) {
            auditorList = documentLineList.stream().map(documentLine -> documentLine.getAuditingUser().getLogin()).collect(Collectors.toCollection(TreeSet::new));
        }
        return new ArrayList<>(auditorList);
    }


    public Map<String, Object> auditorItemDetails(StockAudit stockAudit) {
        Map<String, Object> auditorItemDetails = new HashMap<>();
        List<String> auditorList = getAuditors(stockAudit);
        List<AuditDocumentLine> documentLineList = stockAudit.getDocument().getLines();
        if (isNotEmpty(documentLineList) && isNotEmpty(auditorList)) {
            auditorList.forEach(auditor -> auditorItemDetails.put(auditor, documentLineList.stream().filter(auditDocumentLine -> auditDocumentLine.getAuditingUser().getLogin().equals(auditor)).map(auditDocumentLine -> auditDocumentLine.getItem().getName()).collect(Collectors.toCollection(TreeSet::new))));
        }
        return auditorItemDetails;
    }

    public List<String[]> getTableContent(Set<String> itemNameList) {
        List<String[]> tableContentList = new LinkedList<>();
        tableContentList.add(new String[]{"Item Name"});
        itemNameList.forEach(itemName -> tableContentList.add(new String[]{itemName}));
        return tableContentList;
    }

}

