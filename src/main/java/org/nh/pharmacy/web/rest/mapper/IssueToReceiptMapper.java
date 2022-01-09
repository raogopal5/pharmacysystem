package org.nh.pharmacy.web.rest.mapper;

import org.elasticsearch.index.query.Operator;
import org.mapstruct.*;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.dto.IssueDocumentLine;
import org.nh.pharmacy.domain.dto.Quantity;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.search.StockReceiptSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.TransactionType.*;

/**
 * Created by Nitesh on 5/2/17.
 */
@Mapper(componentModel = "spring")
public abstract class IssueToReceiptMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.issueUnit", source = "stockIssue.document.issueUnit"),
        @Mapping(target = "document.indentUnit", source = "stockIssue.document.indentUnit"),
        @Mapping(target = "document.issueStore", source = "stockIssue.document.issueStore"),
        @Mapping(target = "document.indentStore", source = "stockIssue.document.indentStore"),
        @Mapping(target = "document.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(convertFromstockIssueLine(stockIssue.getDocument().getLines()))"),
        @Mapping(target = "document.status", constant = "DRAFT"),
        @Mapping(target = "document.modifiedDate", ignore = true),
        @Mapping(target = "document.approvedDate", ignore = true),
        @Mapping(target = "document.approvedBy", ignore = true),
        @Mapping(target = "document.remarks", ignore = true),
        @Mapping(target = "document.documentType", expression = "java(documentType(stockIssue.getDocument().getDocumentType()))"),
        @Mapping(target = "document.sourceType", expression = "java(stockIssue.getDocument().getDocumentType())")
    })
    public abstract StockReceipt convertFromstockIssue(StockIssue stockIssue);

    @Autowired
    public StockReceiptSearchRepository stockReceiptSearchRepository;


    @Mappings({
        @Mapping(target = "id", source = "issueDocumentLine.id"),
        @Mapping(target = "item", source = "issueDocumentLine.item"),
        @Mapping(target = "medication", source = "issueDocumentLine.medication"),
        @Mapping(target = "generic", source = "issueDocumentLine.generic"),
        @Mapping(target = "batchNumber", source = "issueDocumentLine.batchNumber"),
        @Mapping(target = "locator", ignore = true),
        @Mapping(target = "stockId", ignore = true),
        @Mapping(target = "owner", source = "issueDocumentLine.owner"),
        @Mapping(target = "cost", source = "issueDocumentLine.cost"),
        @Mapping(target = "mrp", source = "issueDocumentLine.mrp"),
        @Mapping(target = "sku", source = "issueDocumentLine.sku"),
        @Mapping(target = "consignment", source = "issueDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "issueDocumentLine.expiryDate"),
        @Mapping(target = "barCode", source = "issueDocumentLine.barCode"),
        @Mapping(target = "sourceDocument", expression = "java(convertSourceDocument(issueDocumentLine.getSourceDocument()))"),
        @Mapping(target = "remarks", ignore = true),
        @Mapping(target = "supplier", source = "issueDocumentLine.supplier"),
        @Mapping(target = "originalItem", source = "issueDocumentLine.originalItem"),
    })
    @Named("stockIssueLineToReceiptLine")
    public abstract ReceiptDocumentLine mapDocumentLine(IssueDocumentLine issueDocumentLine);

    @IterableMapping(qualifiedByName = "stockIssueLineToReceiptLine")
    public abstract List<ReceiptDocumentLine> convertFromstockIssueLine(List<IssueDocumentLine> issueDocumentLines);


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
    @Named("stockIssueLineToReceiptLine")
    public abstract SourceDocument mapSourceDocument(SourceDocument sourceDocument);

    @IterableMapping(qualifiedByName = "stockIssueLineToReceiptLine")
    public abstract List<SourceDocument> convertSourceDocument(List<SourceDocument> sourceDocument);


    @AfterMapping
    public void assignSourceDocument(@MappingTarget StockReceipt stockReceipt, StockIssue stockIssue) {
        for (IssueDocumentLine issueDocumentLine : stockIssue.getDocument().getLines()) {
            Float acceptedQuantityValue = 0.0f;
            Page<StockReceipt> stockReceiptPage = stockReceiptSearchRepository
                .search(queryStringQuery("document.sourceType:(" + TransactionType.Stock_Direct_Transfer + " OR " + TransactionType.Stock_Issue + " OR " + TransactionType.Inter_Unit_Stock_Issue + ") document.status.raw:(" + Status.APPROVED + " OR " + Status.WAITING_FOR_APPROVAL + ") document.lines.sourceDocument.documentNumber.raw:" + stockIssue.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + issueDocumentLine.getId())
                    .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockReceipt> receiptIterator = stockReceiptPage.iterator();
            while (receiptIterator.hasNext()) {
                StockReceipt receipt = receiptIterator.next();
                if (!receipt.getDocument().getSourceType().equals(TransactionType.Stock_Reversal) && !receipt.getDocument().getSourceType().equals(TransactionType.Inter_Unit_Stock_Reversal)) {
                    ReceiptDocumentLine receiptDocumentLine1 = receipt.getDocument().getLines().stream().filter(receiptDocumentLine -> receiptDocumentLine.getSourceDocument()
                        .stream().anyMatch(sourceDocument -> sourceDocument.getLineId().equals(issueDocumentLine.getId()))).findAny().get();
                    acceptedQuantityValue += receiptDocumentLine1.getAcceptedQuantity().getValue() +
                        Optional.ofNullable(receiptDocumentLine1.getRejectedQuantity().getValue()).orElse(Float.valueOf(0));
                }
            }
            Quantity quantity = new Quantity();
            quantity.setUom(issueDocumentLine.getIssuedQuantity().getUom());
            quantity.setValue(issueDocumentLine.getIssuedQuantity().getValue() - acceptedQuantityValue);

            List<SourceDocument> sourceDocuments = new ArrayList<>();
            SourceDocument sourceDocument = new SourceDocument();
            sourceDocument.setStockId(issueDocumentLine.getStockId());
            sourceDocument.setId(stockIssue.getId());
            sourceDocument.setDocumentNumber(stockIssue.getDocumentNumber());
            sourceDocument.setDocumentDate(stockIssue.getDocument().getApprovedDate());
            sourceDocument.setType(stockIssue.getDocument().getDocumentType());
            sourceDocument.setLineId(issueDocumentLine.getId());
            sourceDocument.setQuantity(issueDocumentLine.getIssuedQuantity());
            sourceDocument.setPendingQuantity(quantity);
            sourceDocument.setCreatedBy(stockIssue.getDocument().getIssuedBy());
            sourceDocuments.add(sourceDocument);
            ReceiptDocumentLine receiptDocumentLine = stockReceipt.getDocument().getLines().stream().filter(stockReceiptLine -> stockReceiptLine.getId() != null &&
                stockReceiptLine.getId().equals(issueDocumentLine.getId())).findFirst().get();
            receiptDocumentLine.setId(null);
            receiptDocumentLine.setAcceptedQuantity(quantity);
            receiptDocumentLine.setRejectedQuantity(new Quantity(Float.valueOf(0), quantity.getUom()));
            if (stockIssue.getDocument().getDocumentType().equals(TransactionType.Stock_Issue) || stockIssue.getDocument().getDocumentType().equals(TransactionType.Inter_Unit_Stock_Issue)) {
                receiptDocumentLine.getSourceDocument().add(sourceDocument);
            } else receiptDocumentLine.setSourceDocument(sourceDocuments);
        }
    }

    TransactionType documentType(TransactionType documentType) {
        if (Inter_Unit_Stock_Issue.equals(documentType)) {
            return Inter_Unit_Stock_Receipt;
        }
        return Stock_Receipt;
    }
}
