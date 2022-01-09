package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.ReversalDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import java.util.ArrayList;
import java.util.List;

import static org.nh.pharmacy.domain.enumeration.TransactionType.*;

/**
 * Created by Nitesh on 5/8/17.
 */
@Mapper(componentModel = "spring")
public interface ReversalToReceiptMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.issueUnit", source = "stockReversal.document.indentUnit"),
        @Mapping(target = "document.indentUnit", source = "stockReversal.document.issueUnit"),
        @Mapping(target = "document.issueStore", source = "stockReversal.document.indentStore"),
        @Mapping(target = "document.indentStore", source = "stockReversal.document.issueStore"),
        @Mapping(target = "document.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(convertFromstockReversalLine(stockReversal.getDocument().getLines()))"),
        @Mapping(target = "document.status", constant = "DRAFT"),
        @Mapping(target = "document.modifiedDate", ignore = true),
        @Mapping(target = "document.approvedDate", ignore = true),
        @Mapping(target = "document.approvedBy", ignore = true),
        @Mapping(target = "document.remarks", ignore = true),
        @Mapping(target = "document.documentType", expression = "java(documentType(stockReversal.getDocument().getDocumentType()))"),
        @Mapping(target = "document.sourceType", expression = "java(stockReversal.getDocument().getDocumentType())")
    })
    StockReceipt convertFromstockReversal(StockReversal stockReversal);


    @Mappings({
        @Mapping(target = "id", source = "reversalDocumentLine.id"),
        @Mapping(target = "item", source = "reversalDocumentLine.item"),
        @Mapping(target = "medication", source = "reversalDocumentLine.medication"),
        @Mapping(target = "generic", source = "reversalDocumentLine.generic"),
        @Mapping(target = "batchNumber", source = "reversalDocumentLine.batchNumber"),
        @Mapping(target = "locator", ignore = true),
        @Mapping(target = "stockId", ignore = true),
        @Mapping(target = "owner", source = "reversalDocumentLine.owner"),
        @Mapping(target = "cost", source = "reversalDocumentLine.cost"),
        @Mapping(target = "mrp", source = "reversalDocumentLine.mrp"),
        @Mapping(target = "sku", source = "reversalDocumentLine.sku"),
        @Mapping(target = "consignment", source = "reversalDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "reversalDocumentLine.expiryDate"),
        @Mapping(target = "acceptedQuantity", source = "reversalDocumentLine.rejectedQuantity"),
        @Mapping(target = "sourceDocument", expression = "java(convertSourceDocument(reversalDocumentLine.getSourceDocument()))"),
        @Mapping(target = "remarks", ignore = true),
        @Mapping(target = "supplier", source = "reversalDocumentLine.supplier"),
        @Mapping(target = "reversalQuantity", source = "reversalDocumentLine.rejectedQuantity"),
        @Mapping(target = "rejectedQuantity", expression = "java(setRejectedQuantity(reversalDocumentLine.getRejectedQuantity()))"),
        @Mapping(target = "reversalReason", source = "reversalDocumentLine.reason")
    })
    @Named("stockReversalLineToReceiptLine")
    ReceiptDocumentLine mapDocumentLine(ReversalDocumentLine reversalDocumentLine);

    @IterableMapping(qualifiedByName = "stockReversalLineToReceiptLine")
    List<ReceiptDocumentLine> convertFromstockReversalLine(List<ReversalDocumentLine> reversalDocumentLines);


    @Mappings({
        @Mapping(target = "id", source = "sourceDocument.id"),
        @Mapping(target = "documentNumber", source = "sourceDocument.documentNumber"),
        @Mapping(target = "documentDate", source = "sourceDocument.documentDate"),
        @Mapping(target = "type", expression = "java(sourceDocument.getType())"),
        @Mapping(target = "lineId", source = "sourceDocument.lineId"),
        @Mapping(target = "pendingQuantity", source = "sourceDocument.pendingQuantity"),
        @Mapping(target = "quantity", source = "sourceDocument.quantity"),
        @Mapping(target = "createdBy", source = "sourceDocument.createdBy"),
    })
    @Named("stockReversalLineToReceiptLine")
    SourceDocument mapSourceDocument(SourceDocument sourceDocument);

    @IterableMapping(qualifiedByName = "stockReversalLineToReceiptLine")
    List<SourceDocument> convertSourceDocument(List<SourceDocument> sourceDocument);

    @Named("stockReversalLineToReceiptLine")
    default Quantity setRejectedQuantity(Quantity rejectedQuantity) {
        Quantity tempQuantity = new Quantity();
        tempQuantity.setValue(0f);
        tempQuantity.setUom(rejectedQuantity.getUom());
        return tempQuantity;
    }

    @AfterMapping
    default void assignSourceDocument(@MappingTarget StockReceipt stockReceipt, StockReversal stockReversal) {
        for (ReversalDocumentLine reversalDocumentLine : stockReversal.getDocument().getLines()) {
            List<SourceDocument> sourceDocuments = new ArrayList<>();
            SourceDocument sourceDocument = new SourceDocument();
            sourceDocument.setId(stockReversal.getId());
            sourceDocument.setDocumentNumber(stockReversal.getDocumentNumber());
            sourceDocument.setDocumentDate(stockReversal.getDocument().getApprovedDate());
            sourceDocument.setType(stockReversal.getDocument().getDocumentType());
            sourceDocument.setLineId(reversalDocumentLine.getId());
            sourceDocument.setQuantity(reversalDocumentLine.getRejectedQuantity());
            sourceDocument.setPendingQuantity(reversalDocumentLine.getRejectedQuantity());
            sourceDocument.setCreatedBy(stockReversal.getDocument().getReversalMadeBy());
            sourceDocuments.add(sourceDocument);
            ReceiptDocumentLine receiptDocumentLine = stockReceipt.getDocument().getLines().stream().filter(stockReceiptLine -> stockReceiptLine.getId() != null &&
                stockReceiptLine.getId().equals(reversalDocumentLine.getId())).findFirst().get();
            receiptDocumentLine.setId(null);
            if (stockReversal.getDocument().getDocumentType().equals(TransactionType.Stock_Reversal) || stockReversal.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                receiptDocumentLine.getSourceDocument().add(sourceDocument);
            } else receiptDocumentLine.setSourceDocument(sourceDocuments);
        }
    }

    default TransactionType documentType(TransactionType documentType) {
        if (Inter_Unit_Stock_Reversal.equals(documentType)) {
            return Inter_Unit_Stock_Receipt;
        }
        return Stock_Receipt;
    }
}
