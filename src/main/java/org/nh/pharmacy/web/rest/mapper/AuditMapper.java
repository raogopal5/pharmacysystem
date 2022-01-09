package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.pharmacy.domain.StockAudit;
import org.nh.pharmacy.domain.StockAuditPlan;
import org.nh.pharmacy.domain.dto.AuditDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import java.util.List;

/**
 * A AuditMapper
 */
@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.unit", source = "auditPlan.document.unit"),
        @Mapping(target = "document.store", source = "auditPlan.document.store"),
        @Mapping(target = "document.useBarCode", source = "auditPlan.document.useBarCode"),
        @Mapping(target = "document.referenceDocumentNumber", source = "auditPlan.documentNumber"),
        @Mapping(target = "document.referenceDocumentDate", source = "auditPlan.document.documentDate"),
        @Mapping(target = "document.createdBy", source = "auditPlan.document.createdBy"),
        @Mapping(target = "document.documentDate", expression = "java(LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(convertFromAuditPlanLine(auditPlan.getDocument().getLines()))"),
        @Mapping(target = "document.status", constant = "DRAFT")
    })
    StockAudit convertFromAuditPlan(StockAuditPlan auditPlan);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "item", source = "auditDocumentLine.item"),
        @Mapping(target = "batchNumber", source = "auditDocumentLine.batchNumber"),
        @Mapping(target = "locator", source = "auditDocumentLine.locator"),
        @Mapping(target = "stockId", source = "auditDocumentLine.stockId"),
        @Mapping(target = "owner", source = "auditDocumentLine.owner"),
        @Mapping(target = "cost", source = "auditDocumentLine.cost"),
        @Mapping(target = "sku", source = "auditDocumentLine.sku"),
        @Mapping(target = "consignment", source = "auditDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "auditDocumentLine.expiryDate"),
        @Mapping(target = "stockQuantity", source = "auditDocumentLine.stockQuantity"),
        @Mapping(target = "auditQuantity", ignore = true),
        @Mapping(target = "discrepantQuantity", ignore = true),
        @Mapping(target = "discrepantValue", ignore = true),
        @Mapping(target = "hasDiscrepancy", ignore = true),
        @Mapping(target = "remarks", ignore = true)
    })
    @Named("auditPlanLineToAuditLine")
    AuditDocumentLine mapDocumentLine(AuditDocumentLine auditDocumentLine);

    @IterableMapping(qualifiedByName = "auditPlanLineToAuditLine")
    List<AuditDocumentLine> convertFromAuditPlanLine(List<AuditDocumentLine> auditPlanDocumentLines);

    @AfterMapping
    default void assignSourceDocument(@MappingTarget StockAudit stockAudit, StockAuditPlan stockAuditPlan) {
        SourceDocument sourceDocument = new SourceDocument();
        sourceDocument.setDocumentNumber(stockAuditPlan.getDocumentNumber());
        sourceDocument.setDocumentDate(stockAuditPlan.getDocument().getDocumentDate());
        sourceDocument.setType(TransactionType.Stock_Audit_Plan);
        stockAudit.getDocument().getLines().forEach(auditDocumentLine -> auditDocumentLine.setSourceDocument(sourceDocument));
    }
}


