package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.repository.StockIssueRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.service.StockDirectTransferNotificationService;
import org.nh.pharmacy.service.UserService;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.nh.pharmacy.domain.enumeration.Context.DirectTransfer_Notification_Committee;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.message;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A StockDirectTransferNotificationServiceImpl.
 */
@Service("stockDirectTransferNotificationService")
public class StockDirectTransferNotificationServiceImpl extends NotificationGenericImpl implements StockDirectTransferNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockDirectTransferNotificationServiceImpl.class);

    private final StockIssueRepository stockIssueRepository;
    private final ElasticsearchOperations elasticsearchTemplate;
    private final PharmacyRedisCacheService pharmacyRedisCacheService;
    private final ApplicationProperties applicationProperties;

    public StockDirectTransferNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockIssueRepository stockIssueRepository, ElasticsearchOperations elasticsearchTemplate, PharmacyRedisCacheService pharmacyRedisCacheService, ApplicationProperties applicationProperties) {
        super(userService, groupService, notificationChannel);
        this.stockIssueRepository = stockIssueRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void notifyDirectTransferApprovalCommittee(Map content) {

        StockIssue stockDirectTransfer = stockIssueRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-issues/" + stockDirectTransfer.getId();
        String title = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", stockDirectTransfer.getId());
            put("documentNumber", stockDirectTransfer.getDocumentNumber());
            put("documentCreatedDate", stockDirectTransfer.getDocument().getCreatedDate());
            put("documentType", stockDirectTransfer.getDocument().getDocumentType());
            put("documentStatus", stockDirectTransfer.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing direct transfer message for document number:{}", stockDirectTransfer.getDocumentNumber());
    }

    @Override
    public void notifyDirectTransferInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockIssue stockDirectTransfer = stockIssueRepository.findOneByDocumentNumber(documentNumber);
        String issueCreatedBy = stockDirectTransfer.getDocument().getIssuedBy().getLogin();

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
            put("documentId", stockDirectTransfer.getId());
            put("documentNumber", stockDirectTransfer.getDocumentNumber());
            put("documentCreatedDate", stockDirectTransfer.getDocument().getCreatedDate());
            put("documentType", stockDirectTransfer.getDocument().getDocumentType());
            put("documentStatus", stockDirectTransfer.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing direct transfer approve/reject message for document number:{}", stockDirectTransfer.getDocumentNumber());
    }

    @Override
    public void notifyReceivingStore(Map content) {

        StockIssue stockDirectTransfer = stockIssueRepository.findOne(((Long) content.get("document_id")).longValue());

        String title = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is created").toString();
        String body = new StringBuilder().append("Direct transfer (").append(stockDirectTransfer.getDocumentNumber()).append(") is created by store - ").append(stockDirectTransfer.getDocument().getIssueStore().getName()).toString();

        super.publishNotification(new Notification(title, body, application, message, super.retrieveMemberDetailsForGroup(getNotificationCommitteeGroupIds(stockDirectTransfer))));
        log.debug("Publishing direct transfer receiving store message for document number:{}", stockDirectTransfer.getDocumentNumber());
    }

    private String getNotificationCommitteeGroupIds(StockIssue stockDirectTransfer) {
        return getGroupData(DirectTransfer_Notification_Committee, stockDirectTransfer.getDocument().getIndentUnit().getId());
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return ConfigurationUtil.getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
    }
}
