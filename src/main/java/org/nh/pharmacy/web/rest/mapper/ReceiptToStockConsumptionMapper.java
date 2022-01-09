package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.pharmacy.domain.StockConsumption;
import org.nh.pharmacy.domain.StockReceipt;
import org.nh.pharmacy.domain.dto.ConsumptionDocumentLine;
import org.nh.pharmacy.domain.dto.ReceiptDocumentLine;
import org.nh.pharmacy.domain.dto.SourceDocument;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReceiptToStockConsumptionMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "documentNumber", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "document.consumedBy", source = "document.receivedBy"),
        @Mapping(target = "document.consumedDate", source = "document.receiptDate"),
        @Mapping(target = "document.createdBy", source = "document.receivedBy"),
        @Mapping(target = "document.lines", expression = "java(convertReceiptLineToConsumptionLine(stockReceipt))"),
        @Mapping(target = "document.forDepartment", expression = "java(getDept())"),
        @Mapping(target = "document.forPatient", ignore = true),
        @Mapping(target = "document.forHSC", source = "document.indentStore"),
        @Mapping(target = "document.consumptionUnit", source = "document.indentUnit"),
        @Mapping(target = "document.consumptionStore", source = "document.indentStore"),
        @Mapping(target = "document.requestedBy", source = "document.receivedBy"),
        @Mapping(target = "document.approvedBy", ignore = true),
        @Mapping(target = "document.approvedDate", ignore = true),
        @Mapping(target = "document.documentType", ignore = true),
        @Mapping(target = "document.status", ignore = true)
    })
    StockConsumption convertReceiptToStockConsumption(StockReceipt stockReceipt);

    default OrganizationDTO getDept() {
        // this is added just to avoid (null pointer exception)error while taking print of auto consumption document.
        return new OrganizationDTO();
    }

    default List<ConsumptionDocumentLine> convertReceiptLineToConsumptionLine(StockReceipt stockReceipt) {
        List<ReceiptDocumentLine> lines = stockReceipt.getDocument().getLines();
        if (lines == null)
            return null;

        List<ConsumptionDocumentLine> list = new ArrayList<>();
        lines.forEach(line -> {
            if (!(line.getAcceptedQuantity() == null || line.getAcceptedQuantity().getValue() == null
                || line.getAcceptedQuantity().getValue() == 0)) {

                ConsumptionDocumentLine cdLine = new ConsumptionDocumentLine();
                cdLine.setQuantity(line.getAcceptedQuantity());
                cdLine.setAllowAlternate(line.getAllowAlternate());
                cdLine.setBarCode(line.getBarCode());
                cdLine.setBatchNumber(line.getBatchNumber());
                cdLine.setBarCode(line.getBarCode());
                cdLine.setCost(line.getCost());
                cdLine.setExpiryDate(line.getExpiryDate());
                cdLine.setGeneric(line.getGeneric());
                cdLine.setItem(line.getItem());
                cdLine.setLocator(line.getLocator());
                cdLine.setMedication(line.getMedication());
                cdLine.setMrp(line.getMrp());
                cdLine.setOwner(line.getOwner());
                cdLine.setRemarks(line.getRemarks());
                cdLine.setSku(line.getSku());
                cdLine.setStockId(line.getStockId());
                cdLine.setConsignment(line.getConsignment());
                SourceDocument sourceDocument = new SourceDocument();
                sourceDocument.setDocumentNumber(stockReceipt.getDocumentNumber());
                sourceDocument.setId(stockReceipt.getId());
                sourceDocument.setType(stockReceipt.getDocument().getDocumentType());
                sourceDocument.setLineId(line.getId());
                List<SourceDocument> docList = new ArrayList<>();
                docList.add(sourceDocument);
                cdLine.setSourceDocument(docList);
                cdLine.setSupplier(line.getSupplier());

                list.add(cdLine);
            }
        });
        return list;
    }
}
