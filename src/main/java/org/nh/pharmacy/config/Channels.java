package org.nh.pharmacy.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.SubscribableChannel;

/**
 * @Author Nirbhay
 * This interface will have all input and output channel.
 */
public interface Channels {

    String ORGANIZATION_INPUT = "organization-input";

    String HSC_INPUT = "healthcareservicecenter-input";

    String UOM_INPUT = "uom-input";

    String USER_INPUT = "user-input";

    String GROUP_INPUT = "group-input";

    String LOCATOR_INPUT = "locator-input";

    String ITEM_INPUT="item-input";

    String ITEMCATEGORY_INPUT="itemcategory-input";

    String ITEMPRICINGMETHOD_INPUT = "itemPricingMethod-input";

    String ITEMSTORELOCATORMAP_INPUT = "itemStoreLocatorMap-input";

    String HSCGROUPMAPPING_INPUT="hscGroupMapping-input";

    String STOCK_OUTPUT ="stock-output";

    String STOCK_INPUT = "stock-input";

    String STOCK_MOVE_OUTPUT = "stock-move-output";

    String  STOCK_MOVE_INPUT = "stock-move-input";

    String REMOVE_HSC_GROUP_MAPPING_INPUT="remove-hscGroupMapping-input";

    String ITEM_STORE_STOCK_OUTPUT  = "item_store_stock_output";

    String ITEM_STORE_STOCK_INPUT = "item_store-stock_input";

    String CALENDAR_INPUT = "calendar-input";

    String LOCATION_INPUT = "location-input";

    String PAM_ACKNOWLEDGEMENT_INTPUT = "pam-acknowledgement-input";

    String MEDICATION_INPUT = "medication-input";

    String NOTIFICATION_OUTPUT ="jbpm-notification-output";

    String STOCK_SOURCE_INPUT = "stock-source-input";

    String STOCK_SOURCE_OUTPUT = "stock-source-output";

    String MEDICATION_REQUEST_INPUT = "medicationRequest-input";

    String MEDICATION_REQUEST_PRESCRIPTION_AUDIT_INPUT = "medicationRequest-pa-input";

    String MOVE_TO_TRANSIT_STOCK_OUTPUT ="move-to-stock-transit-output";

    String MOVE_TO_TRANSIT_STOCK_INPUT ="move-to-stock-transit-input";

    String ITEM_STORE_STOCK_VIEW_OUTPUT  = "item_store_stock_view_output";

    String ITEM_STORE_STOCK_VIEW_INPUT  = "item_store_stock_view_input";

    String CHARGE_RECORD_OUTPUT ="chargeRecord-output";

    String UPDATE_CHARGE_RECORD_OUTPUT ="chargeRecord-update-output";

    String DISPENSE_OUTPUT ="dispense-output";
    String MEDICATION_REQUEST_STATUS_INPUT ="medicationRequest-status-input";

    String MEDICATION_ORDER_OUTPUT="phr-medicationOrder-output";

    String MEDICATION_ORDER_INPUT ="cpoe-medicationOrder-input";

    String STORE_AUTO_CONSUMPTION_OUTPUT = "store-auto-consumption-output";

    String STORE_AUTO_CONSUMPTION_INPUT = "store-auto-consumption-input";

    String DISPENSE_RECORD_ENCOUNTER_UPDATE_INPUT = "dispenseRecordEncounterUpdate-input";

    String DISPENSE_CHARGE_RECORD_ENCOUNTER_UPDATE_OUTPUT = "dispenseChargeRecordEncounterUpdate-output";

    String TASK_INFO_OUTPUT = "task-info-output";

    /*String TASK_DETAIL_REQUEST_INPUT = "task-detail-request-input";

    String TASK_DETAIL_RESPONSE_OUTPUT = "task-detail-response-output";*/

    String DMS_DOCUMENT_AUDIT = "dms-document-audit-output";

    String EXTERNAL_STOCK_CONSUMPTION_INPUT = "external-stock-consumption-input";

    String BLOOD_BAG_STOCK_CONSUMPTION_INPUT = "blood-bag-stock-consumption-input";

    String BLOOD_BAG_STOCK_REVERSAL_INPUT = "blood-bag-stock-reversal-input";

    String PRESCRIPTION_AUDIT_REQUEST_UPDATE = "prescription-audit-request-output";

    @Input(ORGANIZATION_INPUT)
    SubscribableChannel organizationInput();

    @Input(HSC_INPUT)
    SubscribableChannel hscInput();

    @Input(UOM_INPUT)
    SubscribableChannel uOMInput();

    @Input(GROUP_INPUT)
    SubscribableChannel groupInput();

    @Input(USER_INPUT)
    SubscribableChannel userInput();

    @Input(LOCATOR_INPUT)
    SubscribableChannel locatorInput();

    @Input(ITEM_INPUT)
    SubscribableChannel itemInput();

    @Input(ITEMCATEGORY_INPUT)
    SubscribableChannel itemCategoryInput();

    @Input(ITEMPRICINGMETHOD_INPUT)
    SubscribableChannel itemPricingMethodInput();

    @Input(ITEMSTORELOCATORMAP_INPUT)
    SubscribableChannel itemStoreLocatorMapInput();

    @Input(HSCGROUPMAPPING_INPUT)
    SubscribableChannel hscGroupMappingInput();

    @Output(STOCK_OUTPUT)
    SubscribableChannel stockOutput();

    @Input(STOCK_INPUT)
    SubscribableChannel stockInput();

    @Output(STOCK_MOVE_OUTPUT)
    SubscribableChannel stockMoveOutput();

    @Input(STOCK_MOVE_INPUT)
    SubscribableChannel stockMoveInput();

    @Input(REMOVE_HSC_GROUP_MAPPING_INPUT)
    SubscribableChannel removeHSCGroupMappingInput();

    @Output(ITEM_STORE_STOCK_OUTPUT)
    SubscribableChannel itemStoreStockOutput();

    @Input(ITEM_STORE_STOCK_INPUT)
    SubscribableChannel itemStoreStockInput();

    @Input(CALENDAR_INPUT)
    SubscribableChannel calendarInput();

    @Input(LOCATION_INPUT)
    SubscribableChannel locationInput();

    @Input(PAM_ACKNOWLEDGEMENT_INTPUT)
    SubscribableChannel pamAcknowledgementInput();

    @Input(MEDICATION_INPUT)
    SubscribableChannel medicationInput();

    @Output(NOTIFICATION_OUTPUT)
    SubscribableChannel notificationOutput();

    @Input(STOCK_SOURCE_INPUT)
    SubscribableChannel stockSourceInput();

    @Output(STOCK_SOURCE_OUTPUT)
    SubscribableChannel stockSourceOutput();

    @Input(MEDICATION_REQUEST_INPUT)
    SubscribableChannel medicationRequestInput();

    @Input(MEDICATION_REQUEST_PRESCRIPTION_AUDIT_INPUT)
    SubscribableChannel medicationRequestPrescriptionAuditInput();

    @Output(MOVE_TO_TRANSIT_STOCK_OUTPUT)
    SubscribableChannel stockTransitOutput();

    @Input(MOVE_TO_TRANSIT_STOCK_INPUT)
    SubscribableChannel stockTransitInput();

    @Output(ITEM_STORE_STOCK_VIEW_OUTPUT)
    SubscribableChannel transitToItemStoreOutput();

    @Input(ITEM_STORE_STOCK_VIEW_INPUT)
    SubscribableChannel transitToItemStoreInput();

    @Output(CHARGE_RECORD_OUTPUT)
    SubscribableChannel dispenseDocLineToChargeRecord();

    @Output(DISPENSE_OUTPUT)
    SubscribableChannel publishDispeseToAmbulatory();

    @Input(MEDICATION_REQUEST_STATUS_INPUT)
    SubscribableChannel medicationRequestStatusUpdate();

    @Output(UPDATE_CHARGE_RECORD_OUTPUT)
    SubscribableChannel publishChargeRecordUpdate();

    @Output(MEDICATION_ORDER_OUTPUT)
    SubscribableChannel publishMedicationOrderOutput();

    @Input(MEDICATION_ORDER_INPUT)
    SubscribableChannel cpoeMedicationOrderInput();

    @Output(STORE_AUTO_CONSUMPTION_OUTPUT)
    SubscribableChannel publishStoreAutoConsumptionOuput();

    @Input(STORE_AUTO_CONSUMPTION_INPUT)
    SubscribableChannel consumeStoreAutoConsumptionInput();

    @Input(DISPENSE_RECORD_ENCOUNTER_UPDATE_INPUT)
    SubscribableChannel dispenseRecordEncounterUpdateInput();

    @Output(DISPENSE_CHARGE_RECORD_ENCOUNTER_UPDATE_OUTPUT)
    SubscribableChannel dispenseChargeRecordEncounterUpdateOutput();

    @Output(TASK_INFO_OUTPUT)
    SubscribableChannel taskInfoOutput();

   /* @Input(TASK_DETAIL_REQUEST_INPUT)
    SubscribableChannel taskDetailRequestInput();

    @Output(TASK_DETAIL_RESPONSE_OUTPUT)
    SubscribableChannel taskDetailResponseOutput();*/

    @Output(DMS_DOCUMENT_AUDIT)
    SubscribableChannel dmsDocumentAuditOutput();

    @Input(EXTERNAL_STOCK_CONSUMPTION_INPUT)
    SubscribableChannel externalStockConsumptionInput();

    @Input(BLOOD_BAG_STOCK_CONSUMPTION_INPUT)
    SubscribableChannel bloodBankStockConsumptionInput();

    @Input(BLOOD_BAG_STOCK_REVERSAL_INPUT)
    SubscribableChannel bloodBankStockReversalInput();

    @Output(PRESCRIPTION_AUDIT_REQUEST_UPDATE)
    SubscribableChannel prescriptionAuditRequestUpdateOutput();
}
