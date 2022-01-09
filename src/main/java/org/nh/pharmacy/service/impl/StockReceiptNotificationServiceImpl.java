package org.nh.pharmacy.service.impl;

import org.elasticsearch.index.query.QueryBuilder;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.StockReceiptRepository;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.StockReceiptNotificationService;
import org.nh.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A StockReceiptNotificationServiceImpl.
 */
@Service("stockReceiptNotificationService")
public class StockReceiptNotificationServiceImpl extends NotificationGenericImpl implements StockReceiptNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockReceiptNotificationServiceImpl.class);

    private final StockReceiptRepository stockReceiptRepository;
    private final StockReversalSearchRepository stockReversalSearchRepository;

    public StockReceiptNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockReceiptRepository stockReceiptRepository, StockReversalSearchRepository stockReversalSearchRepository) {
        super(userService, groupService, notificationChannel);
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockReversalSearchRepository = stockReversalSearchRepository;
    }

    @Override
    public void notifyReceiptApprovalCommittee(Map content) {

        StockReceipt stockReceipt = stockReceiptRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-receipts/" + stockReceipt.getId();
        String title = new StringBuilder().append("Receipt (").append(stockReceipt.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Receipt (").append(stockReceipt.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", stockReceipt.getId());
            put("documentNumber", stockReceipt.getDocumentNumber());
            put("documentCreatedDate", stockReceipt.getDocument().getCreatedDate());
            put("documentType", stockReceipt.getDocument().getDocumentType());
            put("documentStatus", stockReceipt.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing receipt approval message for document number:{}", stockReceipt.getDocumentNumber());
    }

    @Override
    public void notifyReceiptInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockReceipt stockReceipt = stockReceiptRepository.findOneByDocumentNumber(documentNumber);
        String receiptCreatedBy = stockReceipt.getDocument().getReceivedBy().getLogin();

        Member member = super.retrieveMemberDetail(receiptCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockReceipt.getId());
            put("documentNumber", stockReceipt.getDocumentNumber());
            put("documentCreatedDate", stockReceipt.getDocument().getCreatedDate());
            put("documentType", stockReceipt.getDocument().getDocumentType());
            put("documentStatus", stockReceipt.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Receipt (").append(stockReceipt.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Receipt (").append(stockReceipt.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing receipt approve/reject message for document number:{}", stockReceipt.getDocumentNumber());
    }

    @Override
    public void notifyIssueInitiatorOnRejection(Map content) {

        StockReceipt stockReceipt = stockReceiptRepository.findOne(((Long) content.get("document_id")).longValue());

        List<ReceiptDocumentLine> receiptDocumentLineList = stockReceipt.getDocument().getLines();
        if (isNotEmpty(receiptDocumentLineList)) {
            List<SourceDocument> sourceDocumentList = receiptDocumentLineList.get(0).getSourceDocument();
            SourceDocument issueDocument = sourceDocumentList.stream().filter(sourceDocument -> (TransactionType.Stock_Issue.equals(sourceDocument.getType()) || TransactionType.Inter_Unit_Stock_Issue.equals(sourceDocument.getType()) || TransactionType.Stock_Direct_Transfer.equals(sourceDocument.getType()))).findAny().orElse(null);
            String issueCreatedBy = null;
            if (nonNull(issueDocument)) {
                issueCreatedBy = issueDocument.getCreatedBy().getLogin();
            }
            if (isRejectExists(receiptDocumentLineList)) {

                String title = new StringBuilder().append("Items rejected for issue (").append(issueDocument.getDocumentNumber()).append(")").toString();
                String body = new StringBuilder().append("Following items are rejected for issue ").append(issueDocument.getDocumentNumber()).append(". Please click on below link to view the details.").toString();

                Map params = new HashMap<String, Object>() {{
                    put("documentId", stockReceipt.getId());
                    put("documentNumber", stockReceipt.getDocumentNumber());
                    put("documentCreatedDate", stockReceipt.getDocument().getCreatedDate());
                    put("documentType", stockReceipt.getDocument().getDocumentType());
                    put("documentStatus", stockReceipt.getDocument().getStatus());
                    put("tableContent", getTableContent(receiptDocumentLineList));
                }};
                super.publishNotification(new Notification(title, body, application, workflow, params, singletonList(super.retrieveMemberDetail(issueCreatedBy))));
                log.debug("Publishing receipt rejected item message for document number:{}", stockReceipt.getDocumentNumber());
                notifyNewReversalCreation(stockReceipt, issueDocument, issueCreatedBy);
            }
        }
    }

    public void notifyNewReversalCreation(StockReceipt stockReceipt, SourceDocument issueDocument, String issueCreatedBy) {

        QueryBuilder queryBuilder = boolQuery()
            .must(queryStringQuery(new StringBuilder("document.lines.sourceDocument.documentNumber.raw:").append(stockReceipt.getDocumentNumber()).toString()))
            .must(queryStringQuery(new StringBuilder("latest:").append(true).toString()));

        Page<StockReversal> stockReversalPage = stockReversalSearchRepository.search(queryBuilder, PageRequest.of(0, 1));
        StockReversal stockReversal = stockReversalPage.getContent().get(0);

        String title = new StringBuilder().append("Reversal (").append(stockReversal.getDocumentNumber()).append(") is created").toString();
        String body = new StringBuilder().append("Reversal (").append(stockReversal.getDocumentNumber()).append(") is created for issue (").append(issueDocument.getDocumentNumber()).append("). Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockReversal.getId());
            put("documentNumber", stockReversal.getDocumentNumber());
            put("documentCreatedDate", stockReversal.getDocument().getCreatedDate());
            put("documentType", stockReversal.getDocument().getDocumentType());
            put("documentStatus", stockReversal.getDocument().getStatus());
        }};

        super.publishNotification(new Notification(title, body, application, workflow, params, singletonList(super.retrieveMemberDetail(issueCreatedBy))));
        log.debug("Publishing reversal creation message for document number:{}", stockReceipt.getDocumentNumber());
    }

    public List<String[]> getTableContent(List<ReceiptDocumentLine> receiptDocumentLineList) {
        List<String[]> tableContentList = new LinkedList<>();
        tableContentList.add(new String[]{"Item Name", "Rejected Quantity"});
        receiptDocumentLineList.stream().filter(receiptDocumentLine -> (nonNull(receiptDocumentLine.getRejectedQuantity()) && (receiptDocumentLine.getRejectedQuantity().getValue() > 0f))).map(receiptDocumentLine -> new String[]{receiptDocumentLine.getItem().getName(), String.valueOf(receiptDocumentLine.getRejectedQuantity().getValue().intValue())}).forEach(tableContentList::add);
        return tableContentList;
    }

    public Boolean isRejectExists(List<ReceiptDocumentLine> receiptDocumentLineList) {
        Boolean isRejectExists = false;
        for (ReceiptDocumentLine receiptDocumentLine : receiptDocumentLineList) {
            if (nonNull(receiptDocumentLine.getRejectedQuantity()) && (receiptDocumentLine.getRejectedQuantity().getValue() > 0f)) {
                isRejectExists = true;
            }
        }
        return isRejectExists;
    }
}
