package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseReturnDocumentLine;

@Mapper(componentModel = "spring")
public interface DispenseToIPDispenseReturnRequestMapper {

    @Mappings({
        @Mapping(target = "lineNumber", source = "lineNumber"),
        @Mapping(target = "itemId", source = "itemId"),
        @Mapping(target = "owner", source = "owner"),
        @Mapping(target = "medicationId", source = "medicationId"),
        @Mapping(target = "stockId", source = "stockId"),
        @Mapping(target = "stockQuantity", source = "stockQuantity"),
        @Mapping(target = "batchQuantity", source = "batchQuantity"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "batchNumber", source = "batchNumber"),
        @Mapping(target = "instruction", source = "instruction"),
        @Mapping(target = "note", source = "note"),
        @Mapping(target = "expiryDate", source = "expiryDate"),
        @Mapping(target = "locator", source = "locator"),
        @Mapping(target = "substitute", source = "substitute"),
        @Mapping(target = "supplier", source = "supplier"),
        @Mapping(target = "uom", source = "uom"),
        @Mapping(target = "barCode", source = "barCode"),
        @Mapping(target = "sku", source = "sku"),
        @Mapping(target = "group", source = "group"),
        @Mapping(target = "consignment", source = "consignment"),
        @Mapping(target = "orderItem", source = "orderItem"),
        @Mapping(target = "consultant", source = "consultant"),
        @Mapping(target = "trackUOM", source = "trackUOM"),
        @Mapping(target = "itemType", source = "itemType"),
        @Mapping(target = "itemCategory", source = "itemCategory"),
        @Mapping(target = "itemGroup", source = "itemGroup"),
        @Mapping(target = "materialGroup", source = "materialGroup"),
        @Mapping(target = "medication", source = "medication"),
        @Mapping(target = "renderingHSC", source = "renderingHSC"),
        @Mapping(target = "addOnParams", ignore = true),
        @Mapping(target = "returnAmount", ignore = true),
        @Mapping(target = "mrp", source = "mrp"),
        @Mapping(target = "cost", source = "cost"),
        @Mapping(target = "grossAmount", source = "grossAmount"),
        @Mapping(target = "discountAmount", ignore = true),
        @Mapping(target = "netAmount", source = "netAmount"),
        @Mapping(target = "taxAmount", source = "taxAmount"),
        @Mapping(target = "dispenseRef", ignore = true),
        @Mapping(target = "dispenseUser",  ignore = true),
        @Mapping(target = "dispenseUnit", ignore = true),
        @Mapping(target = "dispenseDate",ignore = true),
        @Mapping(target = "dispensedQuantity", source = "quantity"),
        @Mapping(target = "earlierReturnQuantity",constant = "0f"),
        @Mapping(target = "requestedReturnQuantity", constant = "0f"),
        @Mapping(target = "acceptedReturnQuantity",constant = "0f"),
        @Mapping(target = "pendingReturnQuantity",constant = "0f"),
        @Mapping(target = "previousAcceptedReturnQty",constant = "0f"),
        @Mapping(target = "dispenseRefNumber", ignore = true)
    })
    IPDispenseReturnDocumentLine convertDispenseToDispenseDocumentLine(DispenseDocumentLine dispenseDocumentLine);
}

