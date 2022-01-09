package org.nh.pharmacy.web.rest.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.*;
import org.nh.billing.domain.ChargeRecord;
import org.nh.common.dto.*;
import org.nh.common.dto.billing.EncounterLiteDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.common.enumeration.ItemType;
import org.nh.common.enumeration.OrderStatus;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.util.PharmacyConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface DispenseDocLineToChargeRecordMapper {

    @Mappings(
        {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "item",source ="" ),
            @Mapping(target = "consultant", source = "dispense.document.consultant"),
            @Mapping(target = "encounter", expression = "java(convertToLiteDTO(dispense.getDocument().getEncounter()))"),
            @Mapping(target = "patient", source = "dispense.document.patient"),
            @Mapping(target = "createdBy", source = "dispense.document.createdBy"),
            @Mapping(target = "createdDate", source = "dispense.document.createdDate"),
            @Mapping(target = "department",source = "dispense.document.department"),
            @Mapping(target = "orderingHSC",source = "dispense.document.orderingHSC"),
            @Mapping(target = "renderingHSC",source = "dispenseDocumentLine.renderingHSC"),
            @Mapping(target = "baseTariff", source = "dispenseDocumentLine.mrp"),
            @Mapping(target = "package", defaultValue = "false"),
            @Mapping(target = "refChargeRecordId", ignore = true),
            @Mapping(target = "tariffPercentage", ignore = true),//
            @Mapping(target = "invoiceId", ignore = true),
            @Mapping(target = "qty", source = "dispenseDocumentLine.quantity"),
            @Mapping(target = "tariffReference", ignore = true),
            @Mapping(target = "unbilled",defaultValue = "false"),
            @Mapping(target = "orderedTariffClass", ignore = true),//
            @Mapping(target = "packageService", ignore = true),
            @Mapping(target = "packageOrderId", ignore = true),//
            @Mapping(target = "orderId",source ="dispense.id"),
            @Mapping(target = "orderedDate",source ="dispense.document.createdDate"),
            @Mapping(target = "unit",source ="dispense.document.orderingUnit"),
            @Mapping(target = "addOnParams",source = "dispenseDocumentLine.addOnParams"),
            @Mapping(target = "orderedPackageVersion",source ="dispense.document.orderedPackageVersion"),
            @Mapping(target = "profile",ignore = true),
            @Mapping(target = "startDate",ignore = true),
            @Mapping(target = "endDate",ignore = true),
            @Mapping(target = "actualBaseTariff",source = "dispenseDocumentLine.mrp"),
            @Mapping(target = "mrp", source = "dispenseDocumentLine.mrp"),
            @Mapping(target = "itemUnitAvgCost", source = "dispenseDocumentLine.itemUnitAvgCost"),
            @Mapping(target = "itemPricingDTO", source = "dispenseDocumentLine.itemPricingDTO")
        }
    )
    ChargeRecord convertDispenseDocumentToChargeRecord(Dispense dispense, DispenseDocumentLine dispenseDocumentLine, ObjectMapper objectMapper);

    @Named("convertToLiteDTO")
    default EncounterLiteDTO convertToLiteDTO(EncounterDTO encounterDTO) {
        return encounterDTO.convertLiteDTO();
    }


    @AfterMapping
    default void assignSourceDocument(@MappingTarget ChargeRecord chargeRecord, Dispense dispense,DispenseDocumentLine dispenseDocumentLine,ObjectMapper objectMapper) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(dispenseDocumentLine.getItemId());
        itemDTO.setCode(dispenseDocumentLine.getCode());
        itemDTO.setName(dispenseDocumentLine.getName());
        itemDTO.setBatchNumber(dispenseDocumentLine.getBatchNumber());
        itemDTO.setExpiryDate(dispenseDocumentLine.getExpiryDate());
        itemDTO.setCategory(dispenseDocumentLine.getItemCategory());
        itemDTO.setGroup(dispenseDocumentLine.getItemGroup());
        itemDTO.setType(dispenseDocumentLine.getItemType());
        itemDTO.setGenericName(dispenseDocumentLine.getGenericName());
        chargeRecord.setItem(itemDTO);
        chargeRecord.setOrderStatus(OrderStatus.DISPENSED);
        chargeRecord.setOrderedPackageVersion(dispense.getDocument().getOrderedPackageVersion());
        SourceDTO sourceDTO = new SourceDTO();
        sourceDTO.setId(dispense.getId());
        sourceDTO.setDocumentType(DocumentType.DISPENSE);
        sourceDTO.setReferenceNumber(dispense.getDocumentNumber());
        sourceDTO.setLineItemId(dispenseDocumentLine.getLineNumber());
        chargeRecord.setSourceList(Arrays.asList(sourceDTO));
        chargeRecord.setOrderedTariffClass(dispense.getDocument().getEncounter().getTariffClass());
        chargeRecord.setDocumentType(ItemType.ITEM);
        chargeRecord.setFinancialClearance(Boolean.TRUE);
        try {
            ArrayList orderStatusHistory = new ArrayList();
            LocalDateTime orderedDate = (null != dispense.getDocument().getOrderSource() && StringUtils.isNotBlank((String) ((Map) dispense.getDocument().getOrderSource().get(PharmacyConstants.DOCUMENT)).get(PharmacyConstants.CREATED_DATE))) ?
                LocalDateTime.parse((String) ((Map) dispense.getDocument().getOrderSource().get(PharmacyConstants.DOCUMENT)).get(PharmacyConstants.CREATED_DATE)) : null;

            UserDTO created = objectMapper.convertValue(((Map) dispense.getDocument().getOrderSource().get(PharmacyConstants.DOCUMENT))
                                .get(PharmacyConstants.CREATED_BY), new TypeReference<UserDTO>() {});
            OrderStatusHistoryDTO orderStatusHistoryDTO = new OrderStatusHistoryDTO();
            if(null != created)
             orderStatusHistoryDTO.setModifiedBy(new UserLiteDTO(created.getId(),created.getLogin(),created.getDisplayName()));
            orderStatusHistoryDTO.setModifiedDate(orderedDate);
            orderStatusHistoryDTO.setCurrentStatus(OrderStatus.ORDERED);
            orderStatusHistoryDTO.setPreviousStatus(OrderStatus.ORDERED);
            orderStatusHistory.add(orderStatusHistoryDTO);
            if(OrderStatus.DISPENSED.equals(chargeRecord.getOrderStatus())){
                orderStatusHistoryDTO = new OrderStatusHistoryDTO();
                orderStatusHistoryDTO.setCurrentStatus(OrderStatus.DISPENSED);
                orderStatusHistoryDTO.setPreviousStatus(OrderStatus.DISPENSED);
                orderStatusHistoryDTO.setModifiedDate(LocalDateTime.now());
            }

            orderStatusHistory.add(orderStatusHistoryDTO);
            chargeRecord.setOrderStatusHistory(orderStatusHistory);
        }catch (Exception ex)
        {
        }

    }


}
