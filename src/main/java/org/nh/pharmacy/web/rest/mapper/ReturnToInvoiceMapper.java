package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.billing.domain.Invoice;
import org.nh.billing.domain.dto.InvoiceItem;
import org.nh.billing.domain.dto.InvoiceItemPlan;
import org.nh.billing.domain.dto.InvoicePlan;
import org.nh.billing.domain.dto.InvoiceTax;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirbhay on 5/9/17.
 */
@Mapper(componentModel = "spring")
public interface ReturnToInvoiceMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "invoiceNumber", ignore = true),
        @Mapping(target = "invoiceDocument", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "invoiceDocument.invoiceDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "invoiceDocument.invoiceType", constant = "REVERSAL"),
        @Mapping(target = "invoiceDocument.invoiceStatus", constant = "DRAFT"),
        @Mapping(target = "invoiceDocument.unit", expression = "java(mapUnit(dispenseReturn.getDocument().getReturnUnit()))"),
        @Mapping(target = "invoiceDocument.grossAmount", source = "document.grossAmount"),
        @Mapping(target = "invoiceDocument.discountAmount", source = "document.discountAmount"),
        @Mapping(target = "invoiceDocument.netAmount", source = "document.netAmount"),
        @Mapping(target = "invoiceDocument.totalSponsorAmount", source = "document.totalSponsorAmount"),
        @Mapping(target = "invoiceDocument.patientPaidAmount", source = "document.patientNetAmount"),
        @Mapping(target = "invoiceDocument.sponsorDiscount", source = "document.sponsorDiscount"),
        @Mapping(target = "invoiceDocument.patientDiscount", source = "document.patientDiscount"),
        @Mapping(target = "invoiceDocument.taxDiscount", source = "document.taxDiscount"),
        @Mapping(target = "invoiceDocument.roundOff", source = "document.roundOff"),
        @Mapping(target = "invoiceDocument.sponsorNetAmount", source = "document.sponsorNetAmount"),
        @Mapping(target = "invoiceDocument.patientAmount", source = "document.patientNetAmount"),
        @Mapping(target = "invoiceDocument.patient", expression = "java(mapPatient(dispenseReturn.getDocument().getPatient()))"),
        @Mapping(target = "invoiceDocument.invoiceTaxes", expression = "java(convertReturnTaxToInvoiceTax(dispenseReturn.getDocument().getDispenseTaxes()))"),
        @Mapping(target = "invoiceDocument.invoicePlans", expression = "java(convertReturnPlanToInvoicePlan(dispenseReturn.getDocument().getDispensePlans()))"),
        @Mapping(target = "invoiceDocument.invoiceItems", expression = "java(convertReturnItemsToInvoiceItems(dispenseReturn.getDocument().getDispenseReturnDocumentLines()))"),
        @Mapping(target = "invoiceDocument.baseCurrency", source = "document.transactionCurrency"),
        @Mapping(target = "invoiceDocument.encounter", source = "document.encounter"),
        @Mapping(target = "invoiceDocument.createdBy", source = "document.createdBy"),
        @Mapping(target = "invoiceDocument.approvedBy", source = "document.approvedBy"),
        @Mapping(target = "invoiceDocument.modifiedBy", source = "document.receivedBy"),
        @Mapping(target = "invoiceDocument.invoicedBy", source = "document.receivedBy"),
        @Mapping(target = "invoiceDocument.approvedDate", source = "document.approvedDate"),
        @Mapping(target = "invoiceDocument.partOf", ignore = true),
        @Mapping(target = "invoiceDocument.documentType",ignore = true),
        @Mapping(target = "invoiceDocument.reasonForCancellation", source = "document.returnReason")
    })

    Invoice convertDispenseReturnToInvoice(DispenseReturn dispenseReturn);
    //Unit
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name")
    })
    org.nh.common.dto.OrganizationDTO mapUnit(OrganizationDTO organization);
    //Patient
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "mrn", source = "mrn"),
        @Mapping(target = "fullName", source = "fullName"),
        @Mapping(target = "age", source = "age"),
        @Mapping(target = "gender", source = "gender"),
        @Mapping(target = "mobileNumber", source = "mobileNumber"),
        @Mapping(target = "email", source = "email")
    })
    org.nh.common.dto.PatientDTO mapPatient(PatientDTO patient);

    //header taxes
    @Mappings({
        @Mapping(target = "taxAmount", source = "taxAmount"),
        @Mapping(target = "taxCode", source = "taxCode"),
        @Mapping(target = "taxDefinition", source = "taxDefinition")
    })
    @Named("returnTaxToInvoiceTax")
    InvoiceTax mapTaxes(DispenseTax dispenseTax);

    @IterableMapping(qualifiedByName = "returnTaxToInvoiceTax")
    List<InvoiceTax> convertReturnTaxToInvoiceTax(List<DispenseTax> dispenseTaxes);


    //Header Plans
   /* @Mappings({
        @Mapping(target = "sponserId", source = "sponserId"),
        @Mapping(target = "amount", source = "amount"),
        @Mapping(target = "status", source = "status")
    })
    @Named("returnPlanToInvoicePlan")
    InvoicePlan mapPlans(DispensePlan dispensePlan);*/

    @IterableMapping(qualifiedByName = "returnPlanToInvoicePlan")
    default List<InvoicePlan> convertReturnPlanToInvoicePlan(List<DispensePlan> dispensePlans){
        List<InvoicePlan> invocePlanList=new ArrayList<>();
        if(null!=dispensePlans) {
            dispensePlans.forEach(dispensePlan -> {
                InvoicePlan invoicePlan = new InvoicePlan();
                List<InvoiceTax> invoiceTaxList = new ArrayList<>();
                invoicePlan.setPatientDiscount(dispensePlan.getPatientDiscount());
                invoicePlan.setPlanRef(dispensePlan.getPlanRef());

                if (null != dispensePlan.getPlanTaxList()) {
                    dispensePlan.getPlanTaxList().forEach(planTaxes -> {
                        InvoiceTax invoiceTax = new InvoiceTax();
                        invoiceTax.setAttributes(planTaxes.getAttributes());
                        invoiceTax.setDefinition(planTaxes.getDefinition());
                        invoiceTax.setPatientTaxAmount(planTaxes.getPatientTaxAmount());
                        invoiceTax.setTaxAmount(planTaxes.getTaxAmount());
                        invoiceTax.setTaxCode(planTaxes.getTaxCode());
                        invoiceTax.setTaxDefinition(planTaxes.getTaxDefinition());
                        invoiceTaxList.add(invoiceTax);
                    });
                }
                invoicePlan.setPlanTaxList(invoiceTaxList);
                invoicePlan.setRoundOff(dispensePlan.getRoundOff());
                invoicePlan.setSponsorDiscount(dispensePlan.getSponsorDiscount());
                invoicePlan.setSponsorGrossAmount(dispensePlan.getSponsorGrossAmount());
                invoicePlan.setSponsorPayable(dispensePlan.getSponsorPayable());
                invoicePlan.setSponsorRef(dispensePlan.getSponsorRef());
                invocePlanList.add(invoicePlan);
            });
        }
        return invocePlanList;
    }
/*
    //line item Plans
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "quantity", source = "quantity"),
        @Mapping(target = "amount", source = "amount"),
        @Mapping(target = "rate", source = "rate")
    })
    @Named("returnItemPlansToInvoiceItemPlans")
    InvoiceItemPlan mapItemPlan(DispenseItemPlan dispenseItemPlan);

    @IterableMapping(qualifiedByName = "returnItemPlansToInvoiceItemPlans")
    List<InvoiceItemPlan> convertReturnItemPlansToInvoiceItemPlans(List<DispenseItemPlan> dispenseItemPlans);*/

    //Line items
    @Named("convertReturnItemsToInvoiceItems")
    default List<InvoiceItem> convertReturnItemsToInvoiceItems(List<DispenseReturnDocumentLine> dispenseDocumentLines){

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        dispenseDocumentLines.forEach(dispenseDocumentLine -> {
            InvoiceItem invoiceItem = new InvoiceItem();
            org.nh.common.dto.ItemDTO item = new org.nh.common.dto.ItemDTO();
            item.setCode(dispenseDocumentLine.getCode());
            item.setName(dispenseDocumentLine.getName());
            item.setBatchNumber(dispenseDocumentLine.getBatchNumber());
            item.setExpiryDate(dispenseDocumentLine.getExpiryDate());
            item.setGroup(dispenseDocumentLine.getGroup());
            item.setId(dispenseDocumentLine.getItemId());
            item.setType(dispenseDocumentLine.getItemType());
            invoiceItem.setItem(item);
            invoiceItem.setQuantity(dispenseDocumentLine.getQuantity());
            invoiceItem.setMrp(dispenseDocumentLine.getMrp());
            invoiceItem.setSaleRate(dispenseDocumentLine.getSaleRate());
            invoiceItem.setSaleAmount(dispenseDocumentLine.getSaleAmount());
            invoiceItem.setGrossAmount(dispenseDocumentLine.getGrossAmount());
            invoiceItem.setNetAmount(dispenseDocumentLine.getNetAmount());
            invoiceItem.setTaxAmount(dispenseDocumentLine.getTaxAmount());
            invoiceItem.setSponsorAmount(dispenseDocumentLine.getSponsorGrossAmount());
            invoiceItem.setInvoiceItemPlans(getInvoiceItemPlans(dispenseDocumentLine.getDispenseItemPlans()));
            invoiceItem.setInvoiceItemTaxes(getInvoiceTaxes(dispenseDocumentLine.getItemTaxes()));
            invoiceItem.setGrossRate(dispenseDocumentLine.getGrossRate());
            invoiceItem.setPatientGrossAmount(dispenseDocumentLine.getPatientGrossAmount());
            invoiceItem.setSponsorDiscAmount(dispenseDocumentLine.getSponsorDiscAmount());
            invoiceItem.setSponsorTaxAmount(dispenseDocumentLine.getSponsorTaxAmount());
            invoiceItem.setSponsorNetAmount(dispenseDocumentLine.getSponsorNetAmount());
            invoiceItem.setSponsorGrossAmount(dispenseDocumentLine.getSponsorGrossAmount());

            invoiceItem.setPatientTotalDiscAmount(dispenseDocumentLine.getPatientTotalDiscAmount());
            invoiceItem.setTaxDiscountAmount(dispenseDocumentLine.getTaxDiscountAmount());
            invoiceItem.setUnitDiscount(dispenseDocumentLine.getUnitDiscount());
            invoiceItem.setUserDiscount(dispenseDocumentLine.getUserDiscount());
            invoiceItem.setItemType(dispenseDocumentLine.getItemType());
            invoiceItem.setTotalDiscountAmount(dispenseDocumentLine.getTotalDiscountAmount());
            invoiceItem.setPatientNetAmount(dispenseDocumentLine.getPatientNetAmount());
            invoiceItem.setPlanDiscountAmount(dispenseDocumentLine.getPlanDiscountAmount());
            invoiceItem.setPatientNetAmount(dispenseDocumentLine.getPatientNetAmount());
            invoiceItem.setPatientTaxAmount(dispenseDocumentLine.getPatientTaxAmount());
            invoiceItem.setTotalMrp(dispenseDocumentLine.getTotalMrp());
            invoiceItem.setSourceLineId(dispenseDocumentLine.getLineNumber());
            invoiceItems.add(invoiceItem);
        });

        return invoiceItems;
    }

    default List<InvoiceItemPlan> getInvoiceItemPlans(List<DispenseItemPlan> dispenseItemPlans){
        List<InvoiceItemPlan> invoiceItemPlans = new ArrayList<>();
        if (dispenseItemPlans == null || dispenseItemPlans.isEmpty()){
            return invoiceItemPlans;
        }
        dispenseItemPlans.forEach(dispenseItemPlan -> {
            InvoiceItemPlan invoiceItemPlan = new InvoiceItemPlan();
            /*invoiceItemPlan.setId(dispenseItemPlan.getId());
            invoiceItemPlan.setQuantity(dispenseItemPlan.getQuantity());
            invoiceItemPlan.setRate(dispenseItemPlan.getRate());
            invoiceItemPlan.setAmount(dispenseItemPlan.getAmount());*/
            invoiceItemPlan.setSponsorRef(dispenseItemPlan.getSponsorRef());
            invoiceItemPlan.setPlanRef(dispenseItemPlan.getPlanRef());
            invoiceItemPlans.add(invoiceItemPlan);
        });
        return invoiceItemPlans;
    }

    default List<InvoiceTax> getInvoiceTaxes(List<DispenseTax> dispenseTaxes){
        List<InvoiceTax> invoiceTaxes = new ArrayList<>();
        if (dispenseTaxes == null || dispenseTaxes.isEmpty()){
            return invoiceTaxes;
        }
        dispenseTaxes.forEach(dispenseTax -> {
            InvoiceTax invoiceTax = new InvoiceTax();
            invoiceTax.setTaxCode(dispenseTax.getTaxCode());
            invoiceTax.setTaxDefinition(dispenseTax.getTaxDefinition());
            invoiceTax.setTaxAmount(dispenseTax.getTaxAmount());
            invoiceTaxes.add(invoiceTax);
        });
        return invoiceTaxes;
    }
}
