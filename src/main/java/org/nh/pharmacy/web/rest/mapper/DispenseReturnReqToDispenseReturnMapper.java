package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.billing.domain.dto.Source;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.IPDispenseReturnRequest;
import org.nh.pharmacy.domain.dto.DispenseReturnDetails;
import org.nh.pharmacy.domain.dto.DispenseReturnDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DispenseReturnReqToDispenseReturnMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.hsc", source = "document.sourceHSC"),
        @Mapping(target = "document.returnhsc", source = "document.returnTOHSC"),
        @Mapping(target = "document.dispenseDate", ignore = true),
        @Mapping(target = "document.approvedBy", source = "document.modifiedBy"),
        @Mapping(target = "document.approvedDate", source = "document.modifiedDate"),
        @Mapping(target = "document.dispenseUser",ignore = true),
        @Mapping(target = "document.dispenseUnit",ignore = true),
        @Mapping(target = "document.dispenseStatus", ignore = true),
        @Mapping(target = "document.encounter", source = "document.encounter"),
        @Mapping(target = "document.patient",  source = "document.patient"),
        @Mapping(target = "document.orderSource",  ignore = true),
        @Mapping(target = "document.dispensePlans", ignore = true),
        @Mapping(target = "document.dispenseTaxes", ignore = true),
        @Mapping(target = "document.paymentDetails", ignore = true),
        @Mapping(target = "document.grossAmount",ignore = true),
        @Mapping(target = "document.discountAmount", ignore = true),
        @Mapping(target = "document.netAmount", ignore = true),
        @Mapping(target = "document.patientAmount", ignore = true),
        @Mapping(target = "document.totalSponsorAmount", ignore = true),
        @Mapping(target = "document.patientPaidAmount", ignore = true),
        @Mapping(target = "document.userDiscountAmount", ignore = true),
        @Mapping(target = "document.userDiscountPercentage", ignore = true),
        @Mapping(target = "document.unitDiscountPercentage", ignore = true),
        @Mapping(target = "document.unitDiscountAmount", ignore = true),
        @Mapping(target = "document.consultant", source = "document.consultant"),
        @Mapping(target = "document.draft", constant = "false"),
        @Mapping(target = "document.sponsorDiscount", ignore = true),
        @Mapping(target = "document.patientDiscount", ignore = true),
        @Mapping(target = "document.taxDiscount", ignore = true),
        @Mapping(target = "document.documentType", ignore = true),
        @Mapping(target = "document.discountPercentage", constant = "false"),
        @Mapping(target = "document.returnDate",expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.returnUnit", source = "document.returnTOUnit"),
        @Mapping(target = "document.dispenseRef", ignore = true),
        @Mapping(target = "document.invoiceRef", ignore = true),
        @Mapping(target = "document.returnStatus", constant = "RETURNED"),
        @Mapping(target = "document.receivedBy", source = "document.receivedBy"),
        @Mapping(target = "document.plans",ignore = true),
        @Mapping(target = "document.taxes", ignore = true),
        @Mapping(target = "document.refundRequired", constant = "false"),
        @Mapping(target = "document.returnReason", source = "document.returnReason"),
        @Mapping(target = "document.cancelledInvoiceRef", ignore = true),
        @Mapping(target = "document.calculatedGrossAmount", source = "document.calculatedGrossAmount"),
        @Mapping(target = "document.calculatedPatientGrossAmount", ignore = true),
        @Mapping(target = "document.calculatedSponsorGrossAmount", ignore = true),
        @Mapping(target = "document.patientNetAmount", ignore = true),
        @Mapping(target = "document.patientSaleAmount", ignore = true),
        @Mapping(target = "document.patientGrossAmount", ignore = true),
        @Mapping(target = "document.sponsorNetAmount", ignore = true),
        @Mapping(target = "document.roundOff", source = "document.roundOff"),
        @Mapping(target = "document.planDiscountAmount", ignore = true),
        @Mapping(target = "document.saleAmount", ignore = true),
        @Mapping(target = "document.ipDispense", constant = "true"),
        @Mapping(target = "document.dispenseNumber", ignore = true),
        @Mapping(target = "document.createdBy", source = "document.modifiedBy"),
        @Mapping(target = "document.createdDate", source = "document.createdDate"),
        @Mapping(target = "document.partOf", ignore = true),
        @Mapping(target = "document.source", ignore = true),
        @Mapping(target = "document.dispenseDocumentLines", ignore = true),
        @Mapping(target = "document.totalDiscountPercentage",  ignore = true),
        @Mapping(target = "document.sendEmail", constant = "false"),
        @Mapping(target = "document.modifiedDate", source = "document.modifiedDate"),
        @Mapping(target = "document.transactionCurrency",  ignore = true),
        @Mapping(target = "document.orderingHSC",  ignore = true),
        @Mapping(target = "document.department",  ignore = true),
        @Mapping(target = "document.orderedDate", ignore = true),
        @Mapping(target = "document.orderedPackageVersion", ignore = true),
        @Mapping(target = "document.sourceDTOList", ignore = true),
        @Mapping(target = "document.returnRequestNumber", source = "documentNumber"),
        @Mapping(target = "document.bedNumber", source = "document.bedNumber"),
        @Mapping(target = "document.patientLocation", source = "document.patientLocation"),
        @Mapping(target = "document.requestedDate", source = "document.createdDate"),
        @Mapping(target = "document.requestedBy", source = "document.createdBy"),
        @Mapping(target = "document.remarks", source = "document.remarks"),
        @Mapping(target = "document.dispenseReturnDocumentLines", expression = "java(copyIPDispenseReturnItemsToReturnItems(ipDispenseReturnRequest.getDocument().getDispenseReturnDocumentLines()))"),
    })
    DispenseReturn convertDispenseRequestToDispenseReturn(IPDispenseReturnRequest ipDispenseReturnRequest);

    @Named("copyDispenseItemsToReturnItems")
    default List<DispenseReturnDocumentLine> copyIPDispenseReturnItemsToReturnItems(List<IPDispenseReturnDocumentLine> dispenseDocumentLines){
        List<DispenseReturnDocumentLine> returnDocumentLines = new ArrayList<>();
        dispenseDocumentLines.forEach(dispenseDocumentLine -> {
            //don't process if accepted return quantity is zero
            if(dispenseDocumentLine.getAcceptedReturnQuantity() == 0f )
                return;
            DispenseReturnDocumentLine returnDocumentLine = new DispenseReturnDocumentLine();
            DispenseReturnDetails dispenseReturnDetails=new DispenseReturnDetails();
            dispenseReturnDetails.setMrp(dispenseDocumentLine.getMrp());
            dispenseReturnDetails.setGrossAmount(dispenseDocumentLine.getGrossAmount());
            dispenseReturnDetails.setTaxAmount(dispenseDocumentLine.getTaxAmount());
            dispenseReturnDetails.setNetAmount(dispenseDocumentLine.getNetAmount());
            //line level data
            returnDocumentLine.setDispenseDate(dispenseDocumentLine.getDispenseDate());
            returnDocumentLine.setTrackUOM(dispenseDocumentLine.getTrackUOM());
            returnDocumentLine.setDispenseReturnDetails(dispenseReturnDetails);
            returnDocumentLine.setCode(dispenseDocumentLine.getCode());
            returnDocumentLine.setName(dispenseDocumentLine.getName());
            returnDocumentLine.setBatchNumber(dispenseDocumentLine.getBatchNumber());
            returnDocumentLine.setExpiryDate(dispenseDocumentLine.getExpiryDate());
            returnDocumentLine.setOrderedQuantity(dispenseDocumentLine.getDispensedQuantity());
            //returnDocumentLine.setIssuedQuantity(dispenseDocumentLine.getEarlierReturnQuantity());
            returnDocumentLine.setMrp(dispenseDocumentLine.getMrp());
            returnDocumentLine.setCost(dispenseDocumentLine.getCost());
            returnDocumentLine.setLineNumber(dispenseDocumentLine.getLineNumber());
            returnDocumentLine.setItemId(dispenseDocumentLine.getItemId());
            returnDocumentLine.setMedicationId(dispenseDocumentLine.getMedicationId());
            returnDocumentLine.setMedication(dispenseDocumentLine.getMedication());
            returnDocumentLine.setNote(dispenseDocumentLine.getNote());
            returnDocumentLine.setSubstitute(dispenseDocumentLine.getSubstitute());
            returnDocumentLine.setQuantity(dispenseDocumentLine.getAcceptedReturnQuantity());
            returnDocumentLine.setSku(dispenseDocumentLine.getSku());
            returnDocumentLine.setGroup(dispenseDocumentLine.getGroup());
            returnDocumentLine.setStockId(dispenseDocumentLine.getStockId());
            returnDocumentLine.setGrossAmount(dispenseDocumentLine.getGrossAmount());
            returnDocumentLine.setNetAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setTaxAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setStockQuantity(dispenseDocumentLine.getStockQuantity());
            returnDocumentLine.setLocator(dispenseDocumentLine.getLocator());
            returnDocumentLine.setSubstitute(dispenseDocumentLine.getSubstitute());
            returnDocumentLine.setSupplier(dispenseDocumentLine.getSupplier());
            returnDocumentLine.setBarCode(dispenseDocumentLine.getBarCode());
            returnDocumentLine.setReturnQuantity(dispenseDocumentLine.getAcceptedReturnQuantity());
            returnDocumentLine.setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
            returnDocumentLine.setConsignment(dispenseDocumentLine.getConsignment());
            returnDocumentLine.setOrderItem(dispenseDocumentLine.getOrderItem());
            returnDocumentLine.setConsultant(dispenseDocumentLine.getConsultant());
            returnDocumentLine.setUom(dispenseDocumentLine.getUom());
            returnDocumentLine.setPrevReturnQuantity(dispenseDocumentLine.getEarlierReturnQuantity());
            returnDocumentLine.setOwner(dispenseDocumentLine.getOwner());
            returnDocumentLine.setItemCategory(dispenseDocumentLine.getItemCategory());
            returnDocumentLine.setItemGroup(dispenseDocumentLine.getItemGroup());
            returnDocumentLine.setItemType(dispenseDocumentLine.getItemType());
            returnDocumentLine.setTotalMrp(dispenseDocumentLine.getMrp().multiply(new BigDecimal(dispenseDocumentLine.getAcceptedReturnQuantity())));
            returnDocumentLine.setDiscountAmount(BigDecimal.ZERO);
            Source source = new Source();
            source.setLineItemId(dispenseDocumentLine.getLineNumber());
            source.setId(dispenseDocumentLine.getDispenseRef().getId());
            source.setReferenceNumber(dispenseDocumentLine.getDispenseRefNumber());
            returnDocumentLine.setSource(source);
            returnDocumentLine.setRequestedReturnQuantity(dispenseDocumentLine.getRequestedReturnQuantity());
            if(null!=dispenseDocumentLine.getAcceptedReturnQuantity() && dispenseDocumentLine.getAcceptedReturnQuantity() > 0f)
                returnDocumentLines.add(returnDocumentLine);
        });

        return returnDocumentLines;
    }
}
