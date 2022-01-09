package org.nh.pharmacy.service.impl;

import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocumentLine;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.repository.InventoryAdjustmentRepository;
import org.nh.pharmacy.repository.StockAuditRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.InventoryAdjustmentNotificationService;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.service.UserService;
import org.nh.pharmacy.util.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.POSITIVE_ADJUSTMENT;
import static org.nh.pharmacy.domain.enumeration.NotificationOf.workflow;
import static org.nh.pharmacy.domain.enumeration.NotificationType.application;
import static org.nh.pharmacy.domain.enumeration.NotificationType.email;

/**
 * A InventoryAdjustmentNotificationServiceImpl.
 */
@Service("inventoryAdjustmentNotificationService")
public class InventoryAdjustmentNotificationServiceImpl extends NotificationGenericImpl implements InventoryAdjustmentNotificationService {

    private final Logger log = LoggerFactory.getLogger(InventoryAdjustmentNotificationServiceImpl.class);

    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final StockAuditRepository stockAuditRepository;
    private final ElasticsearchOperations elasticsearchTemplate;
    private final ApplicationProperties applicationProperties;
    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    public InventoryAdjustmentNotificationServiceImpl(UserService userService, GroupService groupService, @Qualifier(Channels.NOTIFICATION_OUTPUT) MessageChannel notificationChannel, InventoryAdjustmentRepository inventoryAdjustmentRepository, StockAuditRepository stockAuditRepository, ElasticsearchOperations elasticsearchTemplate, ApplicationProperties applicationProperties, PharmacyRedisCacheService pharmacyRedisCacheService) {
        super(userService, groupService, notificationChannel);
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.stockAuditRepository = stockAuditRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.applicationProperties = applicationProperties;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
    }

    @Override
    public void notifyLevelOneApprovalCommittee(Map content) {

        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("level_one_approval_group"));

        String link = "http://localhost:8080/api/inventory-adjustments/" + inventoryAdjustment.getId();
        String title = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", inventoryAdjustment.getId());
            put("documentNumber", inventoryAdjustment.getDocumentNumber());
            put("documentCreatedDate", inventoryAdjustment.getDocument().getCreatedDate());
            put("documentType", inventoryAdjustment.getDocument().getDocumentType());
            put("documentStatus", inventoryAdjustment.getDocument().getStatus());
            put("tableContent", getTableContent(inventoryAdjustment));
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing adjustment level one approval message for document number:{}", inventoryAdjustment.getDocumentNumber());
    }

    @Override
    public void notifyLevelTwoApprovalCommittee(Map content) {

        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOne(((Long) content.get("document_id")).longValue());
        List<Member> memberList = super.retrieveMemberDetailsForGroup((String) content.get("level_two_approval_group"));

        String link = "http://localhost:8080/api/inventory-adjustments/" + inventoryAdjustment.getId();
        String title = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ready for approval").toString();
        String body = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ready for approval. Please click on below link to view the details.").toString();

        Map params = new HashMap<String, Object>() {{
            put("templateName", "documentapproval");
        }};
        super.publishNotification(new Notification(title, body, link, email, workflow, params, memberList));
        params = new HashMap<String, Object>() {{
            put("documentId", inventoryAdjustment.getId());
            put("documentNumber", inventoryAdjustment.getDocumentNumber());
            put("documentCreatedDate", inventoryAdjustment.getDocument().getCreatedDate());
            put("documentType", inventoryAdjustment.getDocument().getDocumentType());
            put("documentStatus", inventoryAdjustment.getDocument().getStatus());
            put("tableContent", getTableContent(inventoryAdjustment));
        }};
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));
        log.debug("Publishing adjustment level two approval message for document number:{}", inventoryAdjustment.getDocumentNumber());
    }

    @Override
    public void notifyAdjustmentInitiator(String content) {

        String[] contentDetail = content.split("~~");
        String documentNumber = contentDetail[0];
        String action = contentDetail[1];

        List<Member> memberList = new ArrayList<>();

        InventoryAdjustment inventoryAdjustment = inventoryAdjustmentRepository.findOneByDocumentNumber(documentNumber);
        String adjustmentCreatedBy = inventoryAdjustment.getDocument().getCreatedBy().getLogin();

        Member member = super.retrieveMemberDetail(adjustmentCreatedBy);
        if (nonNull(member)) {
            memberList.add(member);
        }
        if ("APPROVED".equals(action)) {
            action = "approved";
        } else {
            action = "rejected";
        }

        Map params = new HashMap<String, Object>() {
            {
                put("documentId", inventoryAdjustment.getId());
                put("documentNumber", inventoryAdjustment.getDocumentNumber());
                put("documentCreatedDate", inventoryAdjustment.getDocument().getCreatedDate());
                put("documentType", inventoryAdjustment.getDocument().getDocumentType());
                put("documentStatus", inventoryAdjustment.getDocument().getStatus());
            }
        };

        String title = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ").append(action).toString();
        String body = new StringBuilder().append("Adjustment (").append(inventoryAdjustment.getDocumentNumber()).append(") is ").append(action).append(". Please click on below link to view the details.").toString();
        super.publishNotification(new Notification(title, body, application, workflow, params, memberList));

        log.debug("Publishing adjustment approve/reject message for document number:{}", inventoryAdjustment.getDocumentNumber());
    }

    public List<String[]> getTableContent(InventoryAdjustment inventoryAdjustment) {
        List<String[]> tableContentList = new LinkedList<>();
        Float positiveAdjustmentAmount = 0.0f;
        Float negativeAdjustmentAmount = 0.0f;
        Float totalAdjustmentAmount = 0.0f;
        StockAudit stockAudit = null;
        UserDTO storeContact;
        String storeContactName;
        List<InventoryAdjustmentDocumentLine> documentLineList = inventoryAdjustment.getDocument().getLines();
        if (isNotEmpty(documentLineList)) {
            for (InventoryAdjustmentDocumentLine documentLine : documentLineList) {
                if (POSITIVE_ADJUSTMENT.equals(documentLine.getAdjustmentType())) {
                    positiveAdjustmentAmount += documentLine.getAdjustValue().floatValue();
                } else {
                    negativeAdjustmentAmount += documentLine.getAdjustValue().floatValue();
                }
            }
            totalAdjustmentAmount = positiveAdjustmentAmount - negativeAdjustmentAmount;
        }

        String auditDocumentNumber = inventoryAdjustment.getDocument().getReferenceDocumentNumber();
        if (auditDocumentNumber != null) {
            stockAudit = stockAuditRepository.findOneByDocumentNumber(inventoryAdjustment.getDocument().getReferenceDocumentNumber());
            storeContact = stockAudit.getDocument().getStoreContact();
        } else {
            storeContact = inventoryAdjustment.getDocument().getStoreContact();
        }
        storeContactName = storeContact != null ? String.valueOf(storeContact.getDisplayName()) : EMPTY;

        if (stockAudit != null) {
            tableContentList.add(new String[]{"Adjustment Number", "Audit Date", "Audit Unit", "Audit Store", "Store Contact", "Positive Adjustment Amount", "Negative Adjustment Amount", "Total Adjustment Amount"});
            tableContentList.add(new String[]{
                inventoryAdjustment.getDocumentNumber(),
                stockAudit.getDocument().getDocumentDate().format(ofPattern(ConfigurationUtil.getConfigurationData("athma_date_format", stockAudit.getDocument().getStore().getId(), stockAudit.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService))),
                stockAudit.getDocument().getUnit().getName(),
                stockAudit.getDocument().getStore().getName(),
                storeContactName,
                String.valueOf(positiveAdjustmentAmount),
                String.valueOf(negativeAdjustmentAmount),
                String.valueOf(totalAdjustmentAmount)
            });
        } else {
            tableContentList.add(new String[]{"Adjustment Number", "Adjustment Date", "Adjustment Unit", "Adjustment Store", "Store Contact", "Positive Adjustment Amount", "Negative Adjustment Amount", "Total Adjustment Amount"});
            tableContentList.add(new String[]{
                inventoryAdjustment.getDocumentNumber(),
                inventoryAdjustment.getDocument().getDocumentDate().format(ofPattern(ConfigurationUtil.getConfigurationData("athma_date_format", inventoryAdjustment.getDocument().getStore().getId(), inventoryAdjustment.getDocument().getUnit().getId(), null, elasticsearchTemplate, applicationProperties, pharmacyRedisCacheService))),
                inventoryAdjustment.getDocument().getUnit().getName(),
                inventoryAdjustment.getDocument().getStore().getName(),
                storeContactName,
                String.valueOf(positiveAdjustmentAmount),
                String.valueOf(negativeAdjustmentAmount),
                String.valueOf(totalAdjustmentAmount)
            });
        }

        return tableContentList;
    }
}
