package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.DispenseReturnDetails;
import org.nh.pharmacy.domain.dto.DispenseReturnDocumentLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirbhay on 7/7/17.
 */
@Mapper(componentModel = "spring")
public interface DispenseToReturnMapper {


    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.returnDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.returnUnit", source = "document.dispenseUnit"),
        @Mapping(target = "document.patient", source = "document.patient"),
        @Mapping(target = "document.dispenseNumber", source = "document.dispenseNumber"),
        @Mapping(target = "document.hsc", source = "document.hsc"),
        @Mapping(target = "document.dispenseDate", source = "document.dispenseDate"),
        @Mapping(target = "document.dispenseUser", source = "document.dispenseUser"),
        @Mapping(target = "document.dispenseUnit", source = "document.dispenseUnit"),
        @Mapping(target = "document.dispenseStatus", source = "document.dispenseStatus"),
        @Mapping(target = "document.encounter", source = "document.encounter"),
        @Mapping(target = "document.orderSource", source = "document.orderSource"),
        @Mapping(target = "document.dispensePlans", source = "document.dispensePlans"),
        @Mapping(target = "document.dispenseTaxes", source = "document.dispenseTaxes"),
        @Mapping(target = "document.userDiscountAmount", source = "document.userDiscountAmount"),
        @Mapping(target = "document.userDiscountPercentage", source = "document.userDiscountPercentage"),
        @Mapping(target = "document.unitDiscountPercentage", source = "document.unitDiscountPercentage"),
        @Mapping(target = "document.unitDiscountAmount", source = "document.unitDiscountAmount"),
        @Mapping(target = "document.consultant", source = "document.consultant"),
        @Mapping(target = "document.documentType", constant = "Dispense_Return"),
        @Mapping(target = "document.taxDiscount", source = "document.taxDiscount"),
        @Mapping(target = "document.dispenseRef", ignore = true),
        @Mapping(target = "document.invoiceRef", source = "document.source"),
        @Mapping(target = "document.receivedBy", ignore = true),
        @Mapping(target = "document.refundRequired", ignore = true),
        @Mapping(target = "document.returnReason", ignore = true),
        @Mapping(target = "document.transactionCurrency", source = "document.transactionCurrency"),
        @Mapping(target = "document.dispenseReturnDocumentLines", expression = "java(copyDispenseItemsToReturnItems(dispense.getDocument().getDispenseDocumentLines()))"),
    })
    DispenseReturn convertDispenseToDispenseReturn(Dispense dispense);
    //Line items
    @Named("copyDispenseItemsToReturnItems")
    default List<DispenseReturnDocumentLine> copyDispenseItemsToReturnItems(List<DispenseDocumentLine> dispenseDocumentLines){

        List<DispenseReturnDocumentLine> returnDocumentLines = new ArrayList<>();
        dispenseDocumentLines.forEach(dispenseDocumentLine -> {
            DispenseReturnDocumentLine returnDocumentLine = new DispenseReturnDocumentLine();
            DispenseReturnDetails dispenseReturnDetails=new DispenseReturnDetails();
            dispenseReturnDetails.setMrp(dispenseDocumentLine.getMrp());
            dispenseReturnDetails.setSaleRate(dispenseDocumentLine.getSaleRate());
            dispenseReturnDetails.setGrossAmount(dispenseDocumentLine.getGrossAmount());
            dispenseReturnDetails.setQuantity(dispenseDocumentLine.getQuantity());
            dispenseReturnDetails.setTotalMrp(dispenseDocumentLine.getTotalMrp());
            dispenseReturnDetails.setSaleAmount(dispenseDocumentLine.getSaleAmount());
            dispenseReturnDetails.setTaxAmount(dispenseDocumentLine.getTaxAmount());
            dispenseReturnDetails.setNetAmount(dispenseDocumentLine.getNetAmount());
            dispenseReturnDetails.setTotalDiscountAmount(dispenseDocumentLine.getTotalDiscountAmount());
            dispenseReturnDetails.setPatientSaleAmount(dispenseDocumentLine.getPatientSaleAmount());
            dispenseReturnDetails.setPatientDiscountAmount(dispenseDocumentLine.getPatientTotalDiscAmount());
            dispenseReturnDetails.setPatientGrossAmount(dispenseDocumentLine.getPatientGrossAmount());
            dispenseReturnDetails.setPatientTaxAmount(dispenseDocumentLine.getPatientTaxAmount());
            dispenseReturnDetails.setSponsorSaleAmount(dispenseDocumentLine.getSponsorSaleAmount());
            dispenseReturnDetails.setSponsorDiscountAmount(dispenseDocumentLine.getSponsorDiscAmount());
            dispenseReturnDetails.setSponsorGrossAmount(dispenseDocumentLine.getSponsorGrossAmount());
            dispenseReturnDetails.setSponsorTaxAmount(dispenseDocumentLine.getSponsorTaxAmount());
            dispenseReturnDetails.setSponsorDiscountAmount(dispenseDocumentLine.getSponsorDiscAmount());
            dispenseReturnDetails.setUserDiscountAmount(dispenseDocumentLine.getUserDiscount());
            dispenseReturnDetails.setUnitDiscountAmount(dispenseDocumentLine.getUnitDiscount());
            dispenseReturnDetails.setPlanDiscountAmount(dispenseDocumentLine.getPlanDiscAmount());
            dispenseReturnDetails.setTotalTaxPercentage(dispenseDocumentLine.getTotalTaxInPercent());
            dispenseReturnDetails.setTaxDiscountAmount(dispenseDocumentLine.getTaxDiscountAmount());
            //line level data
            returnDocumentLine.setTrackUOM(dispenseDocumentLine.getTrackUOM());
            returnDocumentLine.setDispenseReturnDetails(dispenseReturnDetails);
            returnDocumentLine.setCode(dispenseDocumentLine.getCode());
            returnDocumentLine.setName(dispenseDocumentLine.getName());
            returnDocumentLine.setBatchNumber(dispenseDocumentLine.getBatchNumber());
            returnDocumentLine.setExpiryDate(dispenseDocumentLine.getExpiryDate());
            returnDocumentLine.setOrderedQuantity(dispenseDocumentLine.getQuantity());
            returnDocumentLine.setIssuedQuantity(dispenseDocumentLine.getQuantity());
            returnDocumentLine.setMrp(dispenseDocumentLine.getMrp());
            returnDocumentLine.setCost(dispenseDocumentLine.getCost());
            returnDocumentLine.setDispenseItemPlans(dispenseDocumentLine.getDispenseItemPlans());
            returnDocumentLine.setItemTaxes(dispenseDocumentLine.getDispenseTaxes());
            returnDocumentLine.setLineNumber(dispenseDocumentLine.getLineNumber());
            returnDocumentLine.setItemId(dispenseDocumentLine.getItemId());
            returnDocumentLine.setMedicationId(dispenseDocumentLine.getMedicationId());
            returnDocumentLine.setNote(dispenseDocumentLine.getNote());
            returnDocumentLine.setSubstitute(dispenseDocumentLine.getSubstitute());
            returnDocumentLine.setQuantity(0f);
            returnDocumentLine.setSku(dispenseDocumentLine.getSku());
            returnDocumentLine.setGroup(dispenseDocumentLine.getGroup());
            returnDocumentLine.setStockId(dispenseDocumentLine.getStockId());
            returnDocumentLine.setGrossAmount(dispenseDocumentLine.getGrossAmount());
            returnDocumentLine.setNetAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setTaxAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setPatientAmount(dispenseDocumentLine.getPatientGrossAmount());
            returnDocumentLine.setSponsorAmount(dispenseDocumentLine.getSponsorGrossAmount());
            returnDocumentLine.setStockQuantity(dispenseDocumentLine.getStockQuantity());
            returnDocumentLine.setLocator(dispenseDocumentLine.getLocator());
            returnDocumentLine.setSubstitute(dispenseDocumentLine.getSubstitute());
            returnDocumentLine.setSupplier(dispenseDocumentLine.getSupplier());
            returnDocumentLine.setBarCode(dispenseDocumentLine.getBarCode());
            returnDocumentLine.setReturnQuantity(dispenseDocumentLine.getReturnQuantity());
            returnDocumentLine.setTotalMrp(BigDecimalUtil.ZERO);
            returnDocumentLine.setSaleRate(dispenseDocumentLine.getSaleRate());
            returnDocumentLine.setGrossRate(dispenseDocumentLine.getGrossRate());
            returnDocumentLine.setTaxDiscountAmount(dispenseDocumentLine.getTaxDiscountAmount());
            returnDocumentLine.setPatientTaxAmount(dispenseDocumentLine.getPatientTaxAmount());
            returnDocumentLine.setUnitDiscount(dispenseDocumentLine.getUnitDiscount());
            returnDocumentLine.setUserDiscount(dispenseDocumentLine.getUserDiscount());
            returnDocumentLine.setEnteredUserDiscount(dispenseDocumentLine.getEnteredUserDiscount());
            returnDocumentLine.setPatientGrossAmount(dispenseDocumentLine.getPatientGrossAmount());
            returnDocumentLine.setPatientNetAmount(dispenseDocumentLine.getPatientNetAmount());
            returnDocumentLine.setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setPatientTotalTaxAmount(dispenseDocumentLine.getPatientTotalTaxAmount());
            returnDocumentLine.setSponsorTaxAmount(dispenseDocumentLine.getSponsorTaxAmount());
            returnDocumentLine.setTotalSponsorAmount(dispenseDocumentLine.getTotalSponsorAmount());
            returnDocumentLine.setConsignment(dispenseDocumentLine.getConsignment());
            returnDocumentLine.setOrderItem(dispenseDocumentLine.getOrderItem());
            returnDocumentLine.setSource(dispenseDocumentLine.getSource());
            returnDocumentLine.setConsultant(dispenseDocumentLine.getConsultant());
            returnDocumentLine.setTotalTaxInPercent(dispenseDocumentLine.getTotalTaxInPercent());
            returnDocumentLine.setUom(dispenseDocumentLine.getUom());
            returnDocumentLine.setPrevReturnQuantity(0f);
            returnDocumentLine.setOwner(dispenseDocumentLine.getOwner());
            returnDocumentLine.setItemCategory(dispenseDocumentLine.getItemCategory());
            returnDocumentLine.setManufacturer(dispenseDocumentLine.getManufacturer());
            returnDocumentLine.setDispenseTaxes(dispenseDocumentLine.getDispenseTaxes());
            returnDocumentLine.setItemGroup(dispenseDocumentLine.getItemGroup());
            returnDocumentLine.setItemType(dispenseDocumentLine.getItemType());
            returnDocumentLines.add(returnDocumentLine);
        });

        return returnDocumentLines;
    }



}

