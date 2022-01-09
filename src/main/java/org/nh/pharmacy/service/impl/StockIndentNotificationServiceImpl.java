package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.pharmacy.repository.StockIndentRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.service.StockIndentNotificationService;
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
import static org.nh.pharmacy.domain.enumeration.Context.Issue_Notification_Committee;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;
import static org.nh.pharmacy.util.ConfigurationUtil.getCommaSeparatedGroupCodes;

/**
 * A StockIndentNotificationServiceImpl.
 */
@Service("stockIndentNotificationService")
public class StockIndentNotificationServiceImpl extends NotificationGenericImpl implements StockIndentNotificationService {

    private final Logger log = LoggerFactory.getLogger(StockIndentNotificationServiceImpl.class);

    private final StockIndentRepository stockIndentRepository;

    private final ElasticsearchOperations elasticsearchTemplate;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;
    private final ApplicationProperties applicationProperties;

    public StockIndentNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, StockIndentRepository stockIndentRepository, ElasticsearchOperations elasticsearchTemplate, PharmacyRedisCacheService pharmacyRedisCacheService, ApplicationProperties applicationProperties) {
        super(userService, groupService, notificationChannel);
        this.stockIndentRepository = stockIndentRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void notifyIndentApprovalCommittee(Map content) {

        StockIndent stockIndent = stockIndentRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("group_id"));

        String link = "http://localhost:8080/api/stock-indents/" + stockIndent.getId();
        String title = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();
        String templateName = "documentapproval";

        Map params = new HashMap<String, Object>() {{
            put("templateName", templateName);
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));

        params = new HashMap<String, Object>() {{
            put("documentId", stockIndent.getId());
            put("documentNumber", stockIndent.getDocumentNumber());
            put("documentCreatedDate", stockIndent.getDocument().getCreatedDate());
            put("documentType", stockIndent.getDocument().getDocumentType());
            put("documentStatus", stockIndent.getDocument().getStatus());
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing indent approval message for document number:{}", stockIndent.getDocumentNumber());
    }

    @Override
    public void notifyIndentInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];
        List<Member> memberList = new ArrayList<>();

        StockIndent stockIndent = stockIndentRepository.findOneByDocumentNumber(documentNumber);
        String indentCreatedBy = stockIndent.getDocument().getIndenterName().getLogin();

        Member member = super.retrieveMemberDetail(indentCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockIndent.getId());
            put("documentNumber", stockIndent.getDocumentNumber());
            put("documentCreatedDate", stockIndent.getDocument().getCreatedDate());
            put("documentType", stockIndent.getDocument().getDocumentType());
            put("documentStatus", stockIndent.getDocument().getStatus());
        }};

        String title = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing indent approve/reject message for document number:{}", stockIndent.getDocumentNumber());
    }

    @Override
    public void notifyIssueStore(Map content) {

        StockIndent stockIndent = stockIndentRepository.findOne(((Long) content.get("document_id")).longValue());

        String title = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is created").toString();
        String body = new StringBuilder().append("Indent (").append(stockIndent.getDocumentNumber()).append(") is created.").append(" Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("documentId", stockIndent.getId());
            put("documentNumber", stockIndent.getDocumentNumber());
            put("documentCreatedDate", stockIndent.getDocument().getCreatedDate());
            put("documentType", stockIndent.getDocument().getDocumentType());
            put("documentStatus", stockIndent.getDocument().getStatus());
        }};

        super.publishNotification(new Notification(title, body, application, workflow, params, super.retrieveMemberDetailsForGroup(getIssueCommitteeGroupIds(stockIndent))));
        log.debug("Publishing indent creation message for document number:{}", stockIndent.getDocumentNumber());
    }

    private String getIssueCommitteeGroupIds(StockIndent stockIndent) {
        return getGroupData(Issue_Notification_Committee, stockIndent.getDocument().getIssueUnit().getId());
    }

    private String getGroupData(Context context, Long unitId) {
        String cacheKey = "PHR: context:"+context.name()+" AND active:true AND partOf.id:"+unitId+" !_exists_:partOf";
        if(applicationProperties.getRedisCache().isCacheEnabled())
        {
            return pharmacyRedisCacheService.getCommaSeparatedGroupCodes(context,unitId,elasticsearchTemplate,cacheKey);
        }else {
            return getCommaSeparatedGroupCodes(context, unitId, elasticsearchTemplate);
        }
    }
}
