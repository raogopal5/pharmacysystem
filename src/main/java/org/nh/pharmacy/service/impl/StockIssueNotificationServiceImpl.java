package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.dto.IssueDocumentLine;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockIssueRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.StockIssueNotificationService;
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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A StockIssueNotificationServiceImpl.
 */
@Service("stockIssueNotificationService")
public class StockIssueNotificationServiceImpl extends NotificationGenericImpl implements StockIssueNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockIssueNotificationServiceImpl.class);

    private final StockIssueRepository stockIssueRepository;

    public StockIssueNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockIssueRepository stockIssueRepository) {
        super(userService, groupService, notificationChannel);
        this.stockIssueRepository = stockIssueRepository;
    }

    @Override
    public void notifyIssueApprovalCommittee(Map content) {

        StockIssue stockIssue = stockIssueRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-issues/" + stockIssue.getId();
        String title = new StringBuilder().append("Issue (").append(stockIssue.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Issue (").append(stockIssue.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", stockIssue.getId());
            put("documentNumber", stockIssue.getDocumentNumber());
            put("documentCreatedDate", stockIssue.getDocument().getCreatedDate());
            put("documentType", stockIssue.getDocument().getDocumentType());
            put("documentStatus", stockIssue.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing issue approval message for document number:{}", stockIssue.getDocumentNumber());
    }

    @Override
    public void notifyIssueInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockIssue stockIssue = stockIssueRepository.findOneByDocumentNumber(documentNumber);
        String issueCreatedBy = stockIssue.getDocument().getIssuedBy().getLogin();

        Member member = super.retrieveMemberDetail(issueCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockIssue.getId());
            put("documentNumber", stockIssue.getDocumentNumber());
            put("documentCreatedDate", stockIssue.getDocument().getCreatedDate());
            put("documentType", stockIssue.getDocument().getDocumentType());
            put("documentStatus", stockIssue.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Issue (").append(stockIssue.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Issue (").append(stockIssue.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing issue approve/reject message for document number:{}", stockIssue.getDocumentNumber());
    }

    @Override
    public void notifyIndentInitiator(Map content) {

        StockIssue stockIssue = stockIssueRepository.findOne(((Long) content.get("document_id")).longValue());
        String indentCreatedBy = null;
        String indentDocumentNumber = null;
        List<Member> memberList = new ArrayList<>();
        List<IssueDocumentLine> issueDocumentLineList = stockIssue.getDocument().getLines();
        if (isNotEmpty(issueDocumentLineList)) {
            List<SourceDocument> sourceDocumentList = issueDocumentLineList.get(0).getSourceDocument();
            SourceDocument indentDocument = sourceDocumentList
                .stream()
                .filter(sourceDocument -> TransactionType.Stock_Indent.equals(sourceDocument.getType()) || TransactionType.Inter_Unit_Stock_Indent.equals(sourceDocument.getType()))
                .findAny()
                .orElse(null);
            if (nonNull(indentDocument)) {
                indentDocumentNumber = indentDocument.getDocumentNumber();
                indentCreatedBy = indentDocument.getCreatedBy().getLogin();
            }
        }
        Member member = super.retrieveMemberDetail(indentCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockIssue.getId());
            put("documentNumber", stockIssue.getDocumentNumber());
            put("documentCreatedDate", stockIssue.getDocument().getCreatedDate());
            put("documentType", stockIssue.getDocument().getDocumentType());
            put("documentStatus", stockIssue.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Issue is done for indent (").append(indentDocumentNumber).append(")").toString();
        String body = new StringBuilder().append("Issue is done for indent (").append(indentDocumentNumber).append(")").append(". Please click on below link to view the details.").toString();

        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing issue completed message to indenter for document number:{}", stockIssue.getDocumentNumber());
    }
}
