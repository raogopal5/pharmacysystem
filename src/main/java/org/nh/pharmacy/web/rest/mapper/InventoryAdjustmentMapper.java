package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.pharmacy.domain.InventoryAdjustment;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocumentLine;
import org.nh.pharmacy.domain.enumeration.AdjustmentType;

import java.util.List;
import java.util.ListIterator;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.NEGATIVE_ADJUSTMENT;
import static org.nh.pharmacy.domain.enumeration.AdjustmentType.POSITIVE_ADJUSTMENT;

/**
 * A InventoryAdjustmentMapper
 */
@Mapper(componentModel = "spring")
public interface InventoryAdjustmentMapper {

    @Mappings(value = {
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.unit", source = "stockAudit.document.unit"),
        @Mapping(target = "document.store", source = "stockAudit.document.store"),
        @Mapping(target = "document.createdBy", source = "stockAudit.document.createdBy"),
        @Mapping(target = "document.createdDate", expression = "java(LocalDateTime.now())"),
        @Mapping(target = "document.documentDate", expression = "java(LocalDateTime.now())"),
        @Mapping(target = "document.referenceDocumentNumber", source = "stockAudit.documentNumber"),
        @Mapping(target = "document.referenceDocumentDate", source = "stockAudit.document.approvedDate"),
        @Mapping(target = "document.lines", expression = "java(convertFromAuditDocumentLine(stockAudit.getDocument().getLines()))"),
        @Mapping(target = "document.documentType", constant = "Inventory_Adjustment"),
        @Mapping(target = "document.status", constant = "DRAFT"),
        @Mapping(target = "document.storeContact", source = "stockAudit.document.storeContact")
    })
    InventoryAdjustment convertToAdjustmentFromAudit(StockAudit stockAudit);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "item", source = "auditDocumentLine.item"),
        @Mapping(target = "batchNumber", source = "auditDocumentLine.batchNumber"),
        @Mapping(target = "locator", source = "auditDocumentLine.locator"),
        @Mapping(target = "stockId", source = "auditDocumentLine.stockId"),
        @Mapping(target = "sku", source = "auditDocumentLine.sku"),
        @Mapping(target = "cost", source = "auditDocumentLine.cost"),
        @Mapping(target = "mrp", ignore = true),
        @Mapping(target = "adjustValue", ignore = true),
        @Mapping(target = "consignment", source = "auditDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "auditDocumentLine.expiryDate"),
        @Mapping(target = "stockQuantity", source = "auditDocumentLine.stockQuantity"),
        @Mapping(target = "adjustQuantity.value", expression = "java(Math.abs(auditDocumentLine.getDiscrepantQuantity().getValue()))"),
        @Mapping(target = "adjustQuantity.uom", source = "auditDocumentLine.discrepantQuantity.uom"),
        @Mapping(target = "barcode", source = "auditDocumentLine.barcode"),
        @Mapping(target = "reason",  source = "auditDocumentLine.discrepantReason"),
        @Mapping(target = "adjustmentType", expression = "java(assignAdjustmentTypeToLine(auditDocumentLine))")
    })
    @Named("auditDocLineToAdjustmentDocLine")
    InventoryAdjustmentDocumentLine mapDocumentLine(AuditDocumentLine auditDocumentLine);

    @IterableMapping(qualifiedByName = "auditDocLineToAdjustmentDocLine")
    List<InventoryAdjustmentDocumentLine> convertFromAuditDocumentLine(List<AuditDocumentLine> auditDocumentLines);

    default AdjustmentType assignAdjustmentTypeToLine(AuditDocumentLine auditDocumentLine) {
        return auditDocumentLine.getHasDiscrepancy() ? ((auditDocumentLine.getDiscrepantQuantity().getValue() > 0) ? NEGATIVE_ADJUSTMENT : POSITIVE_ADJUSTMENT) : null;
    }

    @AfterMapping
    default void removeZeroAdjustmentLines(@MappingTarget InventoryAdjustment inventoryAdjustment) {
        List<InventoryAdjustmentDocumentLine> adjustmentDocumentLineList = inventoryAdjustment.getDocument().getLines();
        if (isNotEmpty(adjustmentDocumentLineList)) {
            ListIterator<InventoryAdjustmentDocumentLine> documentLineItr = adjustmentDocumentLineList.listIterator();
            while (documentLineItr.hasNext()) {
                if (documentLineItr.next().getAdjustQuantity().getValue() == 0.0f) {
                    documentLineItr.remove();
                }
            }
            inventoryAdjustment.getDocument().setLines(adjustmentDocumentLineList);
        }
    }
}

