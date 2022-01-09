package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.dto.InvoiceItem;
import org.nh.billing.domain.dto.InvoiceItemPlan;
import org.nh.billing.domain.dto.InvoicePlan;
import org.nh.billing.domain.dto.InvoiceTax;
import org.nh.common.dto.UOMDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.dto.*;

import java.util.List;

/**
 * Created by Nirbhay on 5/9/17.
 */
@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mappings({
        @Mapping(target = "id", expression = "java(getInvoiceId(dispense.getDocument()))"),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "invoiceNumber", expression = "java(getInvoiceNumber(dispense.getDocument()))"),
        @Mapping(target = "invoiceDocument", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "invoiceDocument.invoiceDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "invoiceDocument.invoiceType", constant = "NORMAL"),
        @Mapping(target = "invoiceDocument.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "invoiceDocument.createdBy", source = "document.createdBy"),
        @Mapping(target = "invoiceDocument.approvedDate", source = "document.approvedDate"),
        @Mapping(target = "invoiceDocument.approvedBy", source = "document.approvedBy"),
        @Mapping(target = "invoiceDocument.modifiedBy", source = "document.dispenseUser"),
        @Mapping(target = "invoiceDocument.invoicedBy", source = "document.dispenseUser"),
        @Mapping(target = "invoiceDocument.unit", source = "document.dispenseUnit"),
        @Mapping(target = "invoiceDocument.hsc", source = "document.hsc"),
        @Mapping(target = "invoiceDocument.grossAmount", source = "document.grossAmount"),
        @Mapping(target = "invoiceDocument.saleAmount", source = "document.saleAmount"),
        @Mapping(target = "invoiceDocument.discountAmount", source = "document.discountAmount"),
        @Mapping(target = "invoiceDocument.netAmount", source = "document.netAmount"),
        @Mapping(target = "invoiceDocument.roundOff", source = "document.roundOff"),
        @Mapping(target = "invoiceDocument.patientAmount", source = "document.patientNetAmount"),
        @Mapping(target = "invoiceDocument.totalSponsorAmount", source = "document.totalSponsorAmount"),
        @Mapping(target = "invoiceDocument.patientPaidAmount", source = "document.patientPaidAmount"),
        @Mapping(target = "invoiceDocument.sponsorDiscount", source = "document.sponsorDiscount"),
        @Mapping(target = "invoiceDocument.patientDiscount", source = "document.patientDiscount"),
        @Mapping(target = "invoiceDocument.taxDiscount", source = "document.taxDiscount"),
        @Mapping(target = "invoiceDocument.sponsorNetAmount", source = "document.sponsorNetAmount"),
        @Mapping(target = "invoiceDocument.unitDiscountAmount", source = "document.unitDiscountAmount"),
        @Mapping(target = "invoiceDocument.userDiscountAmount", source = "document.userDiscountAmount"),
        @Mapping(target = "invoiceDocument.planDiscountAmount", source = "document.planDiscountAmount"),
        @Mapping(target = "invoiceDocument.consultant", source = "document.consultant"),
        @Mapping(target = "invoiceDocument.encounter", source = "document.encounter"),
        @Mapping(target = "invoiceDocument.patient", source = "document.patient"),
        @Mapping(target = "invoiceDocument.invoiceTaxes", expression = "java(convertDispenseTaxToInvoiceTax(dispense.getDocument().getDispenseTaxes()))"),
        @Mapping(target = "invoiceDocument.invoicePlans", expression = "java(convertDispensePlanToInvoicePlan(dispense.getDocument().getDispensePlans()))"),
        @Mapping(target = "invoiceDocument.invoiceItems", expression = "java(convertDispenseItemsToInvoiceItems(dispense.getDocument().getDispenseDocumentLines()))"),
        @Mapping(target = "invoiceDocument.baseCurrency", source = "document.transactionCurrency"),
        @Mapping(target = "invoiceDocument.documentType", ignore = true)
    })
    Invoice convertDispenseToInvoice(Dispense dispense);

    @Named("getInvoiceId")
    default Long getInvoiceId(DispenseDocument document) {
        if (document.getSource() != null) {
            return document.getSource().getId();
        }
        return null;
    }

    @Named("getInvoiceNumber")
    default String getInvoiceNumber(DispenseDocument document) {
        if (document.getSource() != null) {
            return document.getSource().getReferenceNumber();
        }
        return null;
    }

    //header taxes
    @Mappings({
        @Mapping(target = "taxAmount", source = "taxAmount"),
        @Mapping(target = "patientTaxAmount", source = "patientTaxAmount"),
        @Mapping(target = "taxCode", source = "taxCode"),
        @Mapping(target = "definition", source = "definition"),
        @Mapping(target = "taxDefinition", source = "taxDefinition"),
        @Mapping(target = "attributes", source = "attributes")
    })
    @Named("dispenseTaxToInvoiceTax")
    InvoiceTax mapTaxes(DispenseTax dispenseTax);

    @IterableMapping(qualifiedByName = "dispenseTaxToInvoiceTax")
    List<InvoiceTax> convertDispenseTaxToInvoiceTax(List<DispenseTax> dispenseTaxes);

    //Header Plans
    @Mappings({
        @Mapping(target = "planRef", source = "planRef"),
        @Mapping(target = "sponsorRef", source = "sponsorRef"),
        @Mapping(target = "sponsorGrossAmount", source = "sponsorGrossAmount"),
        @Mapping(target = "sponsorDiscount", source = "sponsorDiscount"),
        @Mapping(target = "sponsorPayable", source = "sponsorPayable"),
        @Mapping(target = "patientDiscount", source = "patientDiscount"),
        @Mapping(target = "roundOff", source = "roundOff"),
        @Mapping(target = "planTaxList", expression = "java(convertDispenseTaxToInvoiceTax(dispensePlan.getPlanTaxList()))")
    })
    @Named("dispensePlanToInvoicePlan")
    InvoicePlan mapPlans(DispensePlan dispensePlan);

    @IterableMapping(qualifiedByName = "dispensePlanToInvoicePlan")
    List<InvoicePlan> convertDispensePlanToInvoicePlan(List<DispensePlan> dispensePlans);

    //Convert Items
    @IterableMapping(qualifiedByName = "convertDispenseItemsToInvoiceItems")
    List<InvoiceItem> convertDispenseItemsToInvoiceItems(List<DispenseDocumentLine> documentLines);

    @Mappings({
        @Mapping(target = "item.code", source = "code"),
        @Mapping(target = "item.id", source = "itemId"),
        @Mapping(target = "item.name", source = "name"),
        @Mapping(target = "item.group", source = "group"),
        @Mapping(target = "item.batchNumber", source = "batchNumber"),
        @Mapping(target = "item.expiryDate", source = "expiryDate"),
        @Mapping(target = "invoiceItemType", constant = "ITEM"),
        @Mapping(target = "quantity", source = "quantity"),
        @Mapping(target = "mrp", source = "mrp"),
        @Mapping(target = "totalMrp", source = "totalMrp"),
        @Mapping(target = "saleRate", source = "saleRate"),
        @Mapping(target = "grossRate", source = "grossRate"),
        @Mapping(target = "saleAmount", source = "saleAmount"),
        @Mapping(target = "grossAmount", source = "grossAmount"),
        @Mapping(target = "taxAmount", source = "taxAmount"),
        @Mapping(target = "taxDiscountAmount", source = "taxDiscountAmount"),
        @Mapping(target = "patientTaxAmount", source = "patientTaxAmount"),
        @Mapping(target = "sponsorDiscAmount", source = "sponsorDiscAmount"),
        @Mapping(target = "totalDiscountAmount", source = "totalDiscountAmount"),
        @Mapping(target = "netAmount", source = "netAmount"),
        @Mapping(target = "patientGrossAmount", source = "patientGrossAmount"),
        @Mapping(target = "patientNetAmount", source = "patientNetAmount"),
        @Mapping(target = "patientTotalDiscAmount", source = "patientTotalDiscAmount"),
        @Mapping(target = "patientTotalTaxAmount", source = "patientTotalTaxAmount"),
        @Mapping(target = "sponsorAmount", source = "sponsorGrossAmount"),
        @Mapping(target = "sponsorTaxAmount", source = "sponsorTaxAmount"),
        @Mapping(target = "planDiscountAmount", source = "planDiscAmount"),
        @Mapping(target = "unitDiscountAmount", source = "unitDiscount"),
        @Mapping(target = "userDiscount", source = "userDiscount"),
        @Mapping(target = "consultant", source = "consultant"),
        @Mapping(target = "itemCategory", source = "itemCategory"),
        @Mapping(target = "itemGroup", source = "itemGroup"),
        @Mapping(target = "materialGroup", source = "materialGroup"),
        @Mapping(target = "medication", source = "medication"),
        @Mapping(target = "trackUOM", expression = "java(mapUOM(documentLine.getTrackUOM()))"),
        @Mapping(target = "itemType", source = "itemType"),
        @Mapping(target = "sourceLineId", source = "lineNumber"),
        @Mapping(target = "authorizationUtilizationList", source = "authorizationUtilizationList"),
        @Mapping(target = "invoiceItemPlans", expression = "java(convertDispenseItemPlansToInvoiceItemPlans(documentLine.getDispenseItemPlans()))"),
        @Mapping(target = "invoiceItemTaxes", expression = "java(convertDispenseTaxToInvoiceTax(documentLine.getDispenseTaxes()))")
    })
    @Named("convertDispenseItemsToInvoiceItems")
    InvoiceItem convertItem(DispenseDocumentLine documentLine);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "code", source = "code")
    })
    @Named("mapUOM")
    org.nh.common.dto.UOMDTO mapUOM(UOMDTO uom);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "display", source = "display")
    })
    @Named("mapItemType")
    org.nh.common.dto.ValueSetCodeDTO mapItemType(ValueSetCode itemType);

    //Item Plan
    @Mappings({
        @Mapping(target = "planRef", source = "planRef"),
        @Mapping(target = "sponsorRef", source = "sponsorRef"),
        @Mapping(target = "grossAmount", source = "grossAmount"),
        @Mapping(target = "saleAmount", source = "saleAmount"),
        @Mapping(target = "taxAmount", source = "taxAmount"),
        @Mapping(target = "roundOff", source = "roundOff"),
        @Mapping(target = "sponsorGrossAmount", source = "sponsorGrossAmount"),
        @Mapping(target = "sponsorNetAmount", source = "sponsorNetAmount"),
        @Mapping(target = "sponsorTaxAmount", source = "sponsorTaxAmount"),
        @Mapping(target = "sponsorDiscAmount", source = "sponsorDiscAmount"),
        @Mapping(target = "patientGrossAmount", source = "patientGrossAmount"),
        @Mapping(target = "patientNetAmount", source = "patientNetAmount"),
        @Mapping(target = "patientTaxAmount", source = "patientTaxAmount"),
        @Mapping(target = "patientDiscAmount", source = "patientDiscAmount"),
        @Mapping(target = "planRuleDetail", source = "planRuleDetail"),
        @Mapping(target = "planTaxList", expression = "java(convertDispenseTaxToInvoiceTax(dispenseItemPlan.getPlanTaxList()))")
    })
    @Named("dispenseItemPlansToInvoiceItemPlans")
    InvoiceItemPlan mapItemPlan(DispenseItemPlan dispenseItemPlan);

    @IterableMapping(qualifiedByName = "dispenseItemPlansToInvoiceItemPlans")
    List<InvoiceItemPlan> convertDispenseItemPlansToInvoiceItemPlans(List<DispenseItemPlan> itemPlans);
}
