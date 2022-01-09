package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.ReversalDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import java.util.ArrayList;
import java.util.List;

import static org.nh.pharmacy.domain.enumeration.TransactionType.*;

/**
 * Created by Nitesh on 5/9/17.
 */
@Mapper(componentModel = "spring")
public interface ReversalMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.issueUnit", source = "stockReceipt.document.issueUnit"),
        @Mapping(target = "document.indentUnit", source = "stockReceipt.document.indentUnit"),
        @Mapping(target = "document.issueStore", source = "stockReceipt.document.issueStore"),
        @Mapping(target = "document.indentStore", source = "stockReceipt.document.indentStore"),
        @Mapping(target = "document.reversalDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.approvedDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(removeZeroQuantityItemsAndConvert(stockReceipt.getDocument().getLines()))"),
        @Mapping(target = "document.modifiedDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.createdBy", source = "stockReceipt.document.createdBy"),
        @Mapping(target = "document.approvedBy", source = "stockReceipt.document.approvedBy"),
        @Mapping(target = "document.remarks", ignore = true),
        @Mapping(target = "document.documentType", expression = "java(documentType(stockReceipt.getDocument().getDocumentType()))"),
        @Mapping(target = "document.sourceType", expression = "java(stockReceipt.getDocument().getSourceType())"),
        @Mapping(target = "document.reversalMadeBy", source = "stockReceipt.document.receivedBy")
    })
    StockReversal convertFromstockReceipt(StockReceipt stockReceipt);


    @Mappings({
        @Mapping(target = "id", source = "receiptDocumentLine.id"),
        @Mapping(target = "item", source = "receiptDocumentLine.item"),
        @Mapping(target = "medication", source = "receiptDocumentLine.medication"),
        @Mapping(target = "generic", source = "receiptDocumentLine.generic"),
        @Mapping(target = "batchNumber", source = "receiptDocumentLine.batchNumber"),
        @Mapping(target = "locator", ignore = true),
        @Mapping(target = "stockId", ignore = true),
        @Mapping(target = "owner", source = "receiptDocumentLine.owner"),
        @Mapping(target = "cost", source = "receiptDocumentLine.cost"),
        @Mapping(target = "mrp", source = "receiptDocumentLine.mrp"),
        @Mapping(target = "sku", source = "receiptDocumentLine.sku"),
        @Mapping(target = "consignment", source = "receiptDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "receiptDocumentLine.expiryDate"),
        @Mapping(target = "rejectedQuantity", source = "receiptDocumentLine.rejectedQuantity"),
        @Mapping(target = "sourceDocument", expression = "java(convertSourceDocument(receiptDocumentLine.getSourceDocument()))"),
        @Mapping(target = "remarks", ignore = true),
        @Mapping(target = "supplier", source = "receiptDocumentLine.supplier"),
    })
    @Named("stockReceiptLineToReversalLine")
    ReversalDocumentLine mapDocumentLine(ReceiptDocumentLine receiptDocumentLine);

    @IterableMapping(qualifiedByName = "stockReceiptLineToReversalLine")
    List<ReversalDocumentLine> convertFromstockReceiptLine(List<ReceiptDocumentLine> receiptDocumentLines);

    @Named("stockReceiptLineToReversalLine")
    default List<ReversalDocumentLine> removeZeroQuantityItemsAndConvert(List<ReceiptDocumentLine> receiptDocumentLines) {
        List<ReceiptDocumentLine> newReceiptDocumentLines = new ArrayList<>();

        for (ReceiptDocumentLine receiptDocLine : receiptDocumentLines) {
            if (receiptDocLine.getRejectedQuantity().getValue() > 0) {
                newReceiptDocumentLines.add(receiptDocLine);
            }
        }
        return convertFromstockReceiptLine(newReceiptDocumentLines);
    }


    @Mappings({
        @Mapping(target = "id", source = "sourceDocument.id"),
        @Mapping(target = "documentNumber", source = "sourceDocument.documentNumber"),
        @Mapping(target = "documentDate", source = "sourceDocument.documentDate"),
        @Mapping(target = "type", expression = "java(sourceDocument.getType())"),
        @Mapping(target = "lineId", source = "sourceDocument.lineId"),
        @Mapping(target = "pendingQuantity", source = "sourceDocument.pendingQuantity"),
        @Mapping(target = "quantity", source = "sourceDocument.quantity"),
        @Mapping(target = "stockId", source = "sourceDocument.stockId"),
        @Mapping(target = "createdBy", source = "sourceDocument.createdBy"),
    })
    @Named("stockReversalLineToReceiptLine")
    SourceDocument mapSourceDocument(SourceDocument sourceDocument);

    @IterableMapping(qualifiedByName = "stockReversalLineToReceiptLine")
    List<SourceDocument> convertSourceDocument(List<SourceDocument> sourceDocument);


    @AfterMapping
    default void assignSourceDocument(@MappingTarget StockReversal stockReversal, StockReceipt stockReceipt) {
        for (ReceiptDocumentLine receiptDocumentLine : stockReceipt.getDocument().getLines()) {
            if (receiptDocumentLine.getRejectedQuantity().getValue() > 0) {
                List<SourceDocument> sourceDocuments = new ArrayList<>();
                SourceDocument sourceDocument = new SourceDocument();
                sourceDocument.setId(stockReceipt.getId());
                sourceDocument.setDocumentNumber(stockReceipt.getDocumentNumber());
                sourceDocument.setDocumentDate(stockReceipt.getDocument().getApprovedDate());
                sourceDocument.setType(stockReceipt.getDocument().getDocumentType());
                sourceDocument.setLineId(receiptDocumentLine.getId());
                sourceDocument.setQuantity(receiptDocumentLine.getAcceptedQuantity());
                sourceDocument.setCreatedBy(stockReceipt.getDocument().getReceivedBy());
                sourceDocuments.add(sourceDocument);
                ReversalDocumentLine reversalDocumentLine = stockReversal.getDocument().getLines().stream().filter(stockReversalLine -> stockReversalLine.getId() != null &&
                    stockReversalLine.getId().equals(receiptDocumentLine.getId())).findFirst().get();
                reversalDocumentLine.setId(null);
                if (stockReversal.getDocument().getDocumentType().equals(TransactionType.Stock_Reversal) || stockReversal.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                    reversalDocumentLine.getSourceDocument().add(sourceDocument);
                }
            }
        }
    }

    default TransactionType documentType(TransactionType documentType) {
        if (Inter_Unit_Stock_Receipt.equals(documentType)) {
            return Inter_Unit_Stock_Reversal;
        }
        return Stock_Reversal;
    }
}
