package org.nh.pharmacy.web.rest.mapper;

import org.elasticsearch.index.query.Operator;
import org.mapstruct.*;
import org.nh.pharmacy.domain.ItemStoreStockView;
import org.nh.pharmacy.domain.StockIndent;
import org.nh.pharmacy.domain.dto.Consumption;
import org.nh.pharmacy.domain.dto.IndentDocumentLine;
import org.nh.pharmacy.repository.search.ItemStoreStockViewSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Created by Nitesh on 6/2/17.
 */
@Mapper(componentModel = "spring")
public abstract class IndentCopyMapper {

    @Autowired
    private ItemStoreStockViewSearchRepository itemStoreStockViewSearchRepository;

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
        @Mapping(target = "document.approvedDate", ignore = true),
        @Mapping(target = "document.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "document.lines", expression = "java(convertFromstockIndentLine(stockIndent.getDocument().getLines()))"),
        @Mapping(target = "document.modifiedDate", ignore = true),
        @Mapping(target = "document.approvedBy", ignore = true),
        @Mapping(target = "document.remarks", ignore = true),
        @Mapping(target = "document.status", constant = "DRAFT"),
        @Mapping(target = "document.priority", source = "stockIndent.document.priority"),
        @Mapping(target = "document.documentType", source = "stockIndent.document.documentType")
    })
    public abstract StockIndent copyStockIndent(StockIndent stockIndent);


    @Mappings({
        @Mapping(target = "id", ignore = true),
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
        @Mapping(target = "remarks", ignore = true),
        @Mapping(target = "barCode", source = "indentDocumentLine.barCode"),
        @Mapping(target = "supplier", source = "indentDocumentLine.supplier"),
        @Mapping(target = "availableStock", source = "indentDocumentLine.availableStock"),
        @Mapping(target = "quantity", source = "indentDocumentLine.quantity"),
    })
    @Named("copyStockIndent")
    abstract IndentDocumentLine mapDocumentLine(IndentDocumentLine indentDocumentLine);

    @IterableMapping(qualifiedByName = "copyStockIndent")
    abstract List<IndentDocumentLine> convertFromstockIndentLine(List<IndentDocumentLine> indentDocumentLines);

    @BeforeMapping
    void updateConsumption(StockIndent stockIndent) {
        List<IndentDocumentLine> indentDocumentLines = stockIndent.getDocument().getLines();

        for (IndentDocumentLine indentDocumentLine : indentDocumentLines) {
            StringBuilder sb = new StringBuilder("store.id:");
            sb.append(stockIndent.getDocument().getIndentStore().getId()).append(" ");
            if (indentDocumentLine.getGeneric()) {
                sb.append("name:\"").append(indentDocumentLine.getItem().getDispensableGenericName()).append("\"");
            } else {
                sb.append("itemId:").append(indentDocumentLine.getItem().getId());
            }
            Page<ItemStoreStockView> itemStoreStockViews = itemStoreStockViewSearchRepository
                .search(queryStringQuery(sb.toString()).defaultOperator(Operator.AND), PageRequest.of(0,1));
            if (itemStoreStockViews.iterator().hasNext()) {
                Consumption consumption = new Consumption();
                ItemStoreStockView itemStoreStockView = itemStoreStockViews.iterator().next();
                consumption.setConsumedInCurrentMonth(itemStoreStockView.getConsumedQtyCurrMonth());
                consumption.setConsumedInLastMonth(itemStoreStockView.getConsumedQtyLastMonth());
                indentDocumentLine.setConsumedQuantity(consumption);
                indentDocumentLine.setAvailableStock(itemStoreStockView.getAvailableStock());
                indentDocumentLine.setTransitQuantity(itemStoreStockView.getTransitQty());
            }
        }
    }
}
