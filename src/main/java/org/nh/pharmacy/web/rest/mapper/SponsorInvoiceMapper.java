package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.billing.domain.SponsorInvoice;
import org.nh.billing.domain.dto.SponsorInvoiceItem;
import org.nh.billing.domain.dto.SponsorInvoiceItemPlan;
import org.nh.billing.domain.dto.SponsorInvoicePlan;
import org.nh.billing.domain.dto.SponsorInvoiceTax;
import org.nh.common.dto.EncounterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.*;

import java.util.List;

/**
 * Created by Rohit on 22/9/17.
 */
@Mapper(componentModel = "spring")
public interface SponsorInvoiceMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "sponsorInvoiceNumber", ignore = true),
        @Mapping(target = "sponsorDocument", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "sponsorDocument.sponsorInvoiceDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "sponsorDocument.sponsorInvoiceType", constant = "NORMAL"),
        @Mapping(target = "sponsorDocument.sponsorInvoiceStatus", constant = "DRAFT"),
        @Mapping(target = "sponsorDocument.unit", expression = "java(mapUnit(dispense.getDocument().getDispenseUnit()))"),
        @Mapping(target = "sponsorDocument.grossAmount", source = "document.grossAmount"),
        @Mapping(target = "sponsorDocument.discountAmount", source = "document.discountAmount"),
        @Mapping(target = "sponsorDocument.netAmount", source = "document.netAmount"),
        @Mapping(target = "sponsorDocument.patientAmount", source = "document.patientNetAmount"),
        @Mapping(target = "sponsorDocument.totalSponsorAmount", source = "document.totalSponsorAmount"),
        @Mapping(target = "sponsorDocument.patientPaidAmount", source = "document.patientPaidAmount"),
        @Mapping(target = "sponsorDocument.sponsorDiscount", source = "document.sponsorDiscount"),
        @Mapping(target = "sponsorDocument.patientDiscount", source = "document.patientDiscount"),
        @Mapping(target = "sponsorDocument.taxDiscount", source = "document.taxDiscount"),
        @Mapping(target = "sponsorDocument.encounter", expression = "java(mapEncounter(dispense.getDocument().getEncounter()))"),
        @Mapping(target = "sponsorDocument.patient", source = "document.patient"),
        @Mapping(target = "sponsorDocument.sponsorInvoiceTaxes", expression = "java(convertDispenseTaxToSponsorInvoiceTax(dispense.getDocument().getDispenseTaxes()))"),
        @Mapping(target = "sponsorDocument.sponsorInvoiceItems", expression = "java(convertDispenseItemsToSponsorInvoiceItems(dispense.getDocument().getDispenseDocumentLines()))"),
        @Mapping(target = "sponsorDocument.approvedBy", ignore = true),
        @Mapping(target = "sponsorDocument.partOf", ignore = true)
    })
    SponsorInvoice convertDispenseToSponsorInvoice(Dispense dispense);

    //Encounter
    @Mappings({
        @Mapping(target = "consultant", source = "consultant"),
        @Mapping(target = "encounterClass", source = "encounterClass")
    })
    EncounterDTO mapEncounter(EncounterDTO encounter);

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
        @Mapping(target = "definition", source = "definition"),
        @Mapping(target = "taxDefinition", source = "taxDefinition")
    })
    @Named("dispenseTaxToSponsorInvoiceTax")
    SponsorInvoiceTax mapTaxes(DispenseTax dispenseTax);

    @IterableMapping(qualifiedByName = "dispenseTaxToSponsorInvoiceTax")
    List<SponsorInvoiceTax> convertDispenseTaxToSponsorInvoiceTax(List<DispenseTax> dispenseTaxes);

    //Header Plans
    @Mappings({
        @Mapping(target = "planRef", source = "planRef"),
        @Mapping(target = "sponsorRef", source = "sponsorRef"),
        @Mapping(target = "sponsorGrossAmount", source = "sponsorGrossAmount"),
        @Mapping(target = "sponsorDiscount", source = "sponsorDiscount"),
        @Mapping(target = "sponsorPayable", source = "sponsorPayable"),
        @Mapping(target = "patientDiscount", source = "patientDiscount"),
        @Mapping(target = "roundOff", source = "roundOff"),
        @Mapping(target = "planTaxList", expression = "java(convertDispenseTaxToSponsorInvoiceTax(dispensePlan.getPlanTaxList()))")
    })
    @Named("dispensePlanToSponsorInvoicePlan")
    SponsorInvoicePlan mapPlans(DispensePlan dispensePlan);

    @IterableMapping(qualifiedByName = "dispensePlanToSponsorInvoicePlan")
    List<SponsorInvoicePlan> convertDispensePlanToSponsorInvoicePlan(List<DispensePlan> dispensePlans);

    //Convert Items
    @IterableMapping(qualifiedByName = "convertDispenseItemsSponsorToInvoiceItems")
    List<SponsorInvoiceItem> convertDispenseItemsToSponsorInvoiceItems(List<DispenseDocumentLine> documentLines);

    @Mappings({
        @Mapping(target = "item.code", source = "code"),
        @Mapping(target = "item.name", source = "name"),
        @Mapping(target = "item.group", source = "group"),
        @Mapping(target = "item.batchNumber", source = "batchNumber"),
        @Mapping(target = "item.expiryDate", source = "expiryDate"),
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
        @Mapping(target = "planDiscountAmount", source = "sponsorDiscAmount"),
        @Mapping(target = "totalDiscountAmount", source = "totalDiscountAmount"),
        @Mapping(target = "netAmount", source = "netAmount"),
        @Mapping(target = "patientGrossAmount", source = "patientGrossAmount"),
        @Mapping(target = "patientNetAmount", source = "patientNetAmount"),
        @Mapping(target = "patientTotalDiscAmount", source = "patientTotalDiscAmount"),
        @Mapping(target = "patientTotalTaxAmount", source = "patientTotalTaxAmount"),
        @Mapping(target = "sponsorAmount", source = "sponsorGrossAmount"),
        @Mapping(target = "sponsorTaxAmount", source = "sponsorTaxAmount"),
        @Mapping(target = "consultant", source = "consultant"),
        @Mapping(target = "sponsorInvoiceItemPlans", expression = "java(convertDispenseItemPlansToSponsorInvoiceItemPlans(documentLine.getDispenseItemPlans()))"),
        @Mapping(target = "sponsorInvoiceTaxes", expression = "java(convertDispenseTaxToSponsorInvoiceTax(documentLine.getDispenseTaxes()))")
    })
    @Named("convertDispenseItemsToSponsorInvoiceItems")
    SponsorInvoiceItem convertItem(DispenseDocumentLine documentLine);

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
        @Mapping(target = "planTaxList", expression = "java(convertDispenseTaxToSponsorInvoiceTax(dispenseItemPlan.getPlanTaxList()))")
    })
    @Named("dispenseItemPlansToSponsorInvoiceItemPlans")
    SponsorInvoiceItemPlan mapItemPlan(DispenseItemPlan dispenseItemPlan);

    @IterableMapping(qualifiedByName = "dispenseItemPlansToSponsorInvoiceItemPlans")
    List<SponsorInvoiceItemPlan> convertDispenseItemPlansToSponsorInvoiceItemPlans(List<DispenseItemPlan> itemPlans);
}
