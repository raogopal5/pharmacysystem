package org.nh.pharmacy.service.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.ReserveStock;
import org.nh.pharmacy.repository.ReserveStockRepository;
import org.nh.pharmacy.service.ReserveStockService;
import org.nh.pharmacy.web.rest.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.nonNull;

/**
 * Service Implementation for managing ReserveStock.
 */
@Service
@Transactional
public class ReserveStockServiceImpl implements ReserveStockService {

    private final Logger log = LoggerFactory.getLogger(ReserveStockServiceImpl.class);

    private final ReserveStockRepository reserveStockRepository;

    private final ApplicationProperties applicationProperties;

    private final EntityManager entityManager;

    public ReserveStockServiceImpl(ReserveStockRepository reserveStockRepository, ApplicationProperties applicationProperties, EntityManager entityManager) {
        this.reserveStockRepository = reserveStockRepository;
        this.applicationProperties = applicationProperties;
        this.entityManager = entityManager;
    }

    /**
     * Save a reserveStock.
     *
     * @param reserveStock the entity to save
     * @return the persisted entity
     */
    @Override
    public ReserveStock save(ReserveStock reserveStock) {
        log.debug("Request to save ReserveStock : {}", reserveStock);
        ReserveStock result = reserveStockRepository.save(reserveStock);
        return result;
    }

    /**
     * Get all the reserveStocks.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReserveStock> findAll() {
        log.debug("Request to get all ReserveStocks");
        List<ReserveStock> result = reserveStockRepository.findAll();

        return result;
    }

    /**
     * Get one reserveStock by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ReserveStock findOne(Long id) {
        log.debug("Request to get ReserveStock : {}", id);
        ReserveStock reserveStock = reserveStockRepository.findById(id).get();
        return reserveStock;
    }

    /**
     * Delete the  reserveStock by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ReserveStock : {}", id);
        reserveStockRepository.deleteById(id);
    }

    @Override
    public List<ReserveStock> findAllReservedStockByStockId(Long stockId) {
        return reserveStockRepository.findAllReservedStockByStockId(stockId);
    }

    @Override
    public Map<String, String> exportReserveStocks(List<String> unitCodes, String storeCode, LocalDate fromDate, LocalDate toDate) throws Exception {
        log.debug("Request to export ReserveStocks unitCodes:{}, storeCode:{}, fromDate:{}, toDate:{}", unitCodes,storeCode, fromDate, toDate);
        Iterator<Map<String, Object>> stockIterator = getReserveStockReport(unitCodes, storeCode, fromDate, toDate).iterator();
        File file = ExportUtil.getCSVExportFile("reserve_stock_report", applicationProperties.getAthmaBucket().getMasterExport());
        FileWriter stockFileWriter = new FileWriter(file);
        Map<String, String> reserveStockFileDetails = new HashMap<>();
        reserveStockFileDetails.put("fileName", file.getName());
        reserveStockFileDetails.put("pathReference", "masterExport");

        final String[] reserveStockHeader = {"Unit Code", "Unit Name", "Store Code", "Store Name", "Item Code", "Item Name", "Transaction No",
            "Transaction Id", "Transaction Type", "Reserved Date", "Quantity", "Stock Value", "Document Status"};
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()).withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter csvFilePrinter = new CSVPrinter(stockFileWriter, csvFileFormat)) {
            csvFilePrinter.printRecord(reserveStockHeader);
            while (stockIterator.hasNext()) {
                Map<String, Object> map = stockIterator.next();
                List reserveStockRow = new ArrayList();
                reserveStockRow.add(map.get("unitCode"));
                reserveStockRow.add(map.get("unitName"));
                reserveStockRow.add(map.get("storeCode"));
                reserveStockRow.add(map.get("storeName"));
                reserveStockRow.add(map.get("itemCode"));
                reserveStockRow.add(map.get("itemName"));
                reserveStockRow.add(map.get("transactionNo"));
                reserveStockRow.add(map.get("transactionId"));
                reserveStockRow.add(map.get("transactionType"));
                reserveStockRow.add(map.get("reservedDate"));
                reserveStockRow.add(map.get("quantity"));
                reserveStockRow.add(map.get("stockValue"));
                reserveStockRow.add(map.get("documentStatus"));
                csvFilePrinter.printRecord(reserveStockRow);
            }
        }
        return reserveStockFileDetails;
    }

    @Override
    public List<Map<String, Object>> getReserveStockReport(List<String> unitCodes, String storeCode, LocalDate fromDate, LocalDate toDate) throws Exception{
        List<Object[]> results = getReserveStock(unitCodes, storeCode, fromDate, toDate);
        return parseResponse(results);
    }

    private List<Object[]> getReserveStock(List<String> unitCodes, String storeCode, LocalDate fromDate, LocalDate toDate) {
        LocalDate fromDat = LocalDate.parse(fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        LocalDate toDat = LocalDate.parse(toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).plusDays(1);//to show till midnight
        StringBuilder query = new StringBuilder("select t.* from ( select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value, jsonb_extract_path_text(document,'dispenseStatus') document_status from stock_reserve sr  ,dispense d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Dispense' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id, sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr, stock_correction d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Stock_Correction' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr  ,stock_consumption d, stock s, item i, healthcare_service_center h, organization u where d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Stock_Consumption' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr  ,stock_issue d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Stock_Issue' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr  ,stock_issue d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Inter_Unit_Stock_Issue' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr  ,stock_issue d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Stock_Direct_Transfer' union all select u.code unit_code, u.name unit_name, h.code store_code, h.name store_name, i.code item_code, i.name item_name, sr.transaction_no,sr.transaction_id,sr.transaction_type, sr.reserved_date,sr.quantity,s.stock_value,jsonb_extract_path_text(document,'status') document_status from stock_reserve sr  ,inventory_adjustment d, stock s, item i, healthcare_service_center h, organization u where  d.document_number = sr.transaction_no and d.latest = true and s.id = sr.stock_id and i.id = s.item_id and s.store_id = h.id and u.id = h.part_of_id and sr.transaction_type = 'Inventory_Adjustment') t");
        Map<String, Object> params = new HashMap<>();
        query.append(" where t.unit_code in (:unitCodes)");
        params.put("unitCodes", unitCodes);
        if (nonNull(storeCode)) {
            query.append(" and t.store_code = :storeCode");
            params.put("storeCode", storeCode);
        }
        query.append(" and t.reserved_date between :fromDate and :toDate");
        params.put("fromDate", fromDat);
        params.put("toDate", toDat);
        query.append(" order by t.unit_code, t.store_code, t.transaction_type, t.reserved_date");
        log.debug("reverse stock query:{}",query.toString());
        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        return nativeQuery.getResultList();
    }

    private List<Map<String, Object>> parseResponse(List<Object[]> objects) throws Exception{
        List<Map<String, Object>> reserveStocks = new ArrayList<>();
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        for (Object[] object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("unitCode", object[0]);
            map.put("unitName", object[1]);
            map.put("storeCode", object[2]);
            map.put("storeName", object[3]);
            map.put("itemCode", object[4]);
            map.put("itemName", object[5]);
            map.put("transactionNo", object[6]);
            map.put("transactionId", object[7]);
            map.put("transactionType", object[8]);
            Date date = oldFormat.parse(String.valueOf(object[9]));
            map.put("reservedDate", newFormat.format(date));
            map.put("quantity", object[10]);
            map.put("stockValue", object[11]);
            map.put("documentStatus", object[12]);
            reserveStocks.add(map);
        }
        return reserveStocks;
    }

}
