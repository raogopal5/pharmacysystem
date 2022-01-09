package org.nh.pharmacy.web.rest.mapper;

import org.elasticsearch.index.query.Operator;
import org.mapstruct.*;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.domain.StockIssue;
import org.nh.pharmacy.domain.StockReversal;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.repository.search.StockIssueSearchRepository;
import org.nh.pharmacy.repository.search.StockReversalSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.pharmacy.domain.enumeration.TransactionType.*;

/**
 * Created by Nitesh on 4/20/17.
 */
@Mapper(componentModel = "spring")
public abstract class IssueMapper {

    @Autowired
    public StockIssueSearchRepository stockIssueSearchRepository;

    @Autowired
    public StockReversalSearchRepository stockReversalSearchRepository;

    Map<Long, Float> map = new HashMap<>();

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "document", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.issueUnit", source = "stockIndent.document.issueUnit"),
        @Mapping(target = "document.indentUnit", source = "stockIndent.document.indentUnit"),
        @Mapping(target = "document.issueStore", source = "stockIndent.document.issueStore"),
        @Mapping(target = "document.indentStore", source = "stockIndent.document.indentStore"),
        @Mapping(target = "document.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(convertFromstockIndentLine(stockIndent.getDocument().getLines()))"),
        @Mapping(target = "document.status", constant = "DRAFT"),
        @Mapping(target = "document.modifiedDate", ignore = true),
        @Mapping(target = "document.approvedDate", ignore = true),
        @Mapping(target = "document.approvedBy", ignore = true),
        @Mapping(target = "document.remarks", ignore = true),
        @Mapping(target = "document.documentType", expression = "java(documentType(stockIndent.getDocument().getDocumentType()))"),
        @Mapping(target = "document.priority", source = "stockIndent.document.priority"),

    })
    public abstract StockIssue convertFromStockIndent(StockIndent stockIndent);


    @Mappings({
        @Mapping(target = "id", source = "indentDocumentLine.id"),
        @Mapping(target = "item", source = "indentDocumentLine.item"),
        @Mapping(target = "medication", source = "indentDocumentLine.medication"),
        @Mapping(target = "generic", source = "indentDocumentLine.generic"),
        @Mapping(target = "batchNumber", source = "indentDocumentLine.batchNumber"),
        @Mapping(target = "locator", source = "indentDocumentLine.locator"),
        @Mapping(target = "stockId", source = "indentDocumentLine.stockId"),
        @Mapping(target = "owner", source = "indentDocumentLine.owner"),
        @Mapping(target = "cost", source = "indentDocumentLine.cost"),
        @Mapping(target = "sku", source = "indentDocumentLine.sku"),
        @Mapping(target = "consignment", source = "indentDocumentLine.consignment"),
        @Mapping(target = "expiryDate", source = "indentDocumentLine.expiryDate"),
        @Mapping(target = "sourceDocument", ignore = true),
        @Mapping(target = "remarks", ignore = true),
        @Mapping(target = "supplier", source = "indentDocumentLine.supplier"),
        @Mapping(target = "mrp", source = "indentDocumentLine.mrp"),
    })
    @Named("stockIndentLineToIssueLine")
    abstract IssueDocumentLine mapDocumentLine(IndentDocumentLine indentDocumentLine);

    @IterableMapping(qualifiedByName = "stockIndentLineToIssueLine")
    abstract List<IssueDocumentLine> convertFromstockIndentLine(List<IndentDocumentLine> indentDocumentLines);

    @AfterMapping
    void assignSourceDocument(@MappingTarget StockIssue stockIssue, StockIndent stockIndent) {

        for (IndentDocumentLine list : stockIndent.getDocument().getLines()) {
            List<SourceDocument> sourceDocuments = new ArrayList<>();
            Quantity quantity = new Quantity();
            quantity.setValue(map.isEmpty() ? list.getQuantity().getValue() : map.get(list.getId()));
            quantity.setUom(list.getQuantity().getUom());
            SourceDocument sourceDocument = new SourceDocument();
            sourceDocument.setId(stockIndent.getId());
            sourceDocument.setDocumentNumber(stockIndent.getDocumentNumber());
            sourceDocument.setDocumentDate(stockIndent.getDocument().getApprovedDate());
            sourceDocument.setType(stockIndent.getDocument().getDocumentType());
            sourceDocument.setLineId(list.getId());
            sourceDocument.setPendingQuantity(quantity);
            sourceDocument.setQuantity(list.getQuantity());
            sourceDocument.setPriority(stockIndent.getDocument().getPriority());
            sourceDocument.setCreatedBy(stockIndent.getDocument().getIndenterName());
            sourceDocuments.add(sourceDocument);
            Optional<IssueDocumentLine> issueDocumentLineOptional = stockIssue.getDocument().getLines()
                .stream().filter(stockIssueLine -> stockIssueLine.getId() != null
                    && stockIssueLine.getId().equals(list.getId()))
                .findFirst();
            if (issueDocumentLineOptional.isPresent()) {
                IssueDocumentLine issueDocumentLine = issueDocumentLineOptional.get();
                issueDocumentLine.setId(null);
                issueDocumentLine.setIssuedQuantity(quantity);
                issueDocumentLine.setSourceDocument(sourceDocuments);
            }
        }

    }

    @BeforeMapping
    void checkIndentDocumentLine(StockIndent stockIndent) {
        Map<Long, Float> rejectedQuantityValue = new HashMap<>();
        Map<Long, Float> issuedQuantityValue = new HashMap<>();
        //if(stockIndent.getDocument().getStatus().equals(Status.PARTIALLY_ISSUED) || stockIndent.getDocument().getStatus().equals(Status.PARTIALLY_PROCESSED)){
        for (IndentDocumentLine line : stockIndent.getDocument().getLines()) {
            map.put(line.getId(), line.getQuantity().getValue());
            Page<StockReversal> stockReversals = stockReversalSearchRepository.search(queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + stockIndent.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockReversal> stockReversalIterator = stockReversals.iterator();
            while (stockReversalIterator.hasNext()) {
                StockReversal stockReversal = stockReversalIterator.next();
                for (ReversalDocumentLine itemLine : stockReversal.getDocument().getLines()) {
                    Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().
                        filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIndent.getDocumentNumber())).findAny();
                    if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                        rejectedQuantityValue.put(line.getId(), rejectedQuantityValue.get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) + itemLine.getRejectedQuantity().getValue()
                            : itemLine.getRejectedQuantity().getValue());
                    }
                }

            }
            Page<StockIssue> stockIssues = stockIssueSearchRepository.search(queryStringQuery("document.lines.sourceDocument.documentNumber.raw:" + stockIndent.getDocumentNumber() + " document.lines.sourceDocument.lineId:" + line.getId())
                .defaultOperator(Operator.AND), PageRequest.of(0,10000));
            Iterator<StockIssue> stockIssueIterator = stockIssues.iterator();
            while (stockIssueIterator.hasNext()) {
                StockIssue stockIssue = stockIssueIterator.next();
                if (!stockIssue.getDocument().getStatus().equals(Status.REJECTED)) {
                    for (IssueDocumentLine itemLine : stockIssue.getDocument().getLines()) {
                        Optional<SourceDocument> sourceDocument = itemLine.getSourceDocument().stream().
                            filter(sourceDocument1 -> sourceDocument1.getDocumentNumber().equals(stockIndent.getDocumentNumber())).findAny();
                        if (sourceDocument.isPresent() && sourceDocument.get().getLineId().equals(line.getId())) {
                            issuedQuantityValue.put(line.getId(), issuedQuantityValue.get(line.getId()) != null ? issuedQuantityValue.get(line.getId()) + itemLine.getIssuedQuantity().getValue()
                                : itemLine.getIssuedQuantity().getValue());

                        }
                    }
                }

            }
        }
        List<IndentDocumentLine> newLines = new ArrayList<>();
        for (IndentDocumentLine line : stockIndent.getDocument().getLines()) {
            map.put(line.getId(),
                (map.get(line.getId()) + (rejectedQuantityValue.get(line.getId()) != null ? rejectedQuantityValue.get(line.getId()) : 0f)
                    - (issuedQuantityValue.get(line.getId()) != null ? issuedQuantityValue.get(line.getId()) : 0f)));
            if (map.get(line.getId()) > 0) {
                newLines.add(line);
            }
        }
        stockIndent.getDocument().setLines(newLines);
        //}
    }

    TransactionType documentType(TransactionType documentType) {
        if (Inter_Unit_Stock_Indent.equals(documentType)) {
            return Inter_Unit_Stock_Issue;
        }
        return Stock_Issue;
    }
}
