package org.nh.pharmacy.service.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.StockFlow;
import org.nh.pharmacy.repository.StockFlowRepository;
import org.nh.pharmacy.service.StockFlowService;
import org.nh.pharmacy.util.CalculateTaxUtil;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static java.util.Objects.nonNull;
import static org.nh.common.util.BigDecimalUtil.roundOff;


/**
 * Service Implementation for managing StockFlow.
 */
@Service
@Transactional
public class StockFlowServiceImpl implements StockFlowService {

    private final Logger log = LoggerFactory.getLogger(StockFlowServiceImpl.class);

    private final StockFlowRepository stockFlowRepository;

    private final ApplicationProperties applicationProperties;

    private final EntityManager entityManager;

    public StockFlowServiceImpl(StockFlowRepository stockFlowRepository, ApplicationProperties applicationProperties, EntityManager entityManager) {
        this.stockFlowRepository = stockFlowRepository;
        this.applicationProperties = applicationProperties;
        this.entityManager = entityManager;
    }

    /**
     * Save a stockFlow.
     *
     * @param stockFlow the entity to save
     * @return the persisted entity
     */
    @Override
    public StockFlow save(StockFlow stockFlow) {
        log.debug("Request to save StockFlow : {}", stockFlow);
        StockFlow result = stockFlowRepository.save(stockFlow);
        return result;
    }

    /**
     * Get all the stockFlows.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockFlow> findAll(Pageable pageable) {
        log.debug("Request to get all StockFlows");
        Page<StockFlow> result = stockFlowRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one stockFlow by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockFlow findOne(Long id) {
        log.debug("Request to get StockFlow : {}", id);
        StockFlow stockFlow = stockFlowRepository.findById(id).get();
        return stockFlow;
    }

    /**
     * Delete the  stockFlow by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockFlow : {}", id);
        stockFlowRepository.deleteById(id);
    }

    /**
     * Get all the stock transactions (IN & OUT) for a unit or a store.
     *
     * @param entryDate,consignment,unitId,storeId,itemId
     * @return available stock transactions
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockTransactions(LocalDate entryDate, Boolean consignment, Long unitId, Long storeId, Long itemId) {
        log.debug("REST request to get the record of all the stock transactions");

        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder("select hsc.name AS store_name, ic.description AS item_category, i.item_type ->> 'display' AS item_type, i.code AS item_code, i.name AS item_name, u.name AS uom_name, sf.flow_type AS transaction_type, sf.transaction_number AS ref_doc_no, sf.transaction_date AS date, sf.transaction_type AS document_type, l.name AS locator_name, sf.batch_no AS batch_code, sf.mrp, sf.expiry_date, sf.quantity, sf.cost, sf.average_cost as averageCost, sf.cost_value as costValue, sf.average_cost_value as averageCostValue, sf.entry_date  from stock_flow AS sf INNER JOIN healthcare_service_center AS hsc on sf.store_id = hsc.id INNER JOIN item AS i on i.id = sf.item_id INNER JOIN uom as u on sf.uom_id = u.id INNER JOIN item_category ic on i.category_id=ic.id INNER JOIN locator AS l on l.id = sf.locator_id INNER JOIN organization AS org on org.id = hsc.part_of_id where sf.entry_date >= :entryDate AND org.id = :unitId AND sf.store_id = :storeId");
        params.put("entryDate", entryDate);
        params.put("unitId", unitId);
        params.put("storeId", storeId);
        if (nonNull(itemId)) {
            query.append(" AND sf.item_id = :itemId");
            params.put("itemId", itemId);
        }
        if (nonNull(consignment)) {
            query.append(" AND sf.consignment = :consignment");
            params.put("consignment", consignment);
        }
        query.append(" ORDER BY sf.entry_date desc");
        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        List<Object[]> objects = nativeQuery.getResultList();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> stockList = new ArrayList<>();
        for (Object[] object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("store", object[0]);
            map.put("itemCategory", object[1]);
            map.put("itemType", object[2]);
            map.put("itemCode", object[3]);
            map.put("itemName", object[4]);
            map.put("uomName", object[5]);
            map.put("transactionType", object[6]);
            map.put("refDocNo", object[7]);
            String transactionDate = simpleDateFormat.format(object[8]);
            map.put("date", transactionDate);
            map.put("documentType", object[9]);
            if (object[6].equals(new String("StockIn"))) {
                map.put("stockInQuantity", object[14]);
            } else if (object[6].equals(new String("StockOut"))) {
                map.put("stockOutQuantity", object[14]);
            }
            map.put("locatorName", object[10]);
            map.put("batchCode", object[11]);
            map.put("mrp", roundOff(((BigDecimal) object[12]), 6));
            map.put("expiryDate", object[13]);
            map.put("cost", roundOff(((BigDecimal) object[15]), 6));
            map.put("averageCost", roundOff(((BigDecimal) object[16]), 6));
            map.put("costValue", roundOff(((BigDecimal) object[17]), 6));
            map.put("averageCostValue", roundOff(((BigDecimal) object[18]), 6));
            map.put("entryDate", simpleDateFormat.format(object[19]));
            stockList.add(map);
        }
        return stockList;
    }

    /**
     * Export stock-flow transactions for the given details.
     *
     * @param entryDate,consignment,unitId,storeId,itemId
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, String> exportStockFlowTransactions(LocalDate entryDate, Boolean consignment, Long unitId, Long storeId, Long itemId) throws IOException {
        Iterator<Map<String, Object>> stockFlowIterator = this.getStockTransactions(entryDate, consignment, unitId, storeId, itemId).iterator();
        File file = ExportUtil.getCSVExportFile("stock_ledger", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter stockFlowFileWriter = new FileWriter(file);
        Map<String, String> stockFlowFileDetails = new HashMap<>();
        stockFlowFileDetails.put("fileName", file.getName());
        stockFlowFileDetails.put("pathReference", "masterExport");

        //Header for stock csv file
        final String[] stockFlowFileHeader = {"Store", "Item Category", "Item Type", "Item Code", "Item Name", "Transaction Type", "Ref Doc No.",
            "Date", "Document Type", "UOM", "Stock In Quantity", "Stock Out Quantity", "Cost", "Cost Value", "Average Cost", "Average Cost Value", "Locator", "MRP", "Batch No", "Expiry Date", "Inventory Date"};

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(stockFlowFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(stockFlowFileHeader);
            while (stockFlowIterator.hasNext()) {
                Map<String, Object> map = stockFlowIterator.next();
                List stockFlowRow = new ArrayList();
                stockFlowRow.add(map.get("store"));
                stockFlowRow.add(map.get("itemCategory"));
                stockFlowRow.add(map.get("itemType"));
                stockFlowRow.add(map.get("itemCode"));
                stockFlowRow.add(map.get("itemName"));
                stockFlowRow.add(map.get("transactionType"));
                stockFlowRow.add(map.get("refDocNo"));
                stockFlowRow.add(map.get("date"));
                stockFlowRow.add(map.get("documentType"));
                stockFlowRow.add(map.get("uomName"));
                stockFlowRow.add(map.get("stockInQuantity"));
                stockFlowRow.add(map.get("stockOutQuantity"));
                stockFlowRow.add(map.get("cost"));
                stockFlowRow.add(map.get("costValue"));
                stockFlowRow.add(map.get("averageCost"));
                stockFlowRow.add(map.get("averageCostValue"));
                stockFlowRow.add(map.get("locatorName"));
                stockFlowRow.add(map.get("mrp"));
                stockFlowRow.add(map.get("batchCode"));
                stockFlowRow.add(map.get("expiryDate"));
                stockFlowRow.add(map.get("entryDate"));
                csvFilePrinter.printRecord(stockFlowRow);
            }
        }
        return stockFlowFileDetails;
    }
}
