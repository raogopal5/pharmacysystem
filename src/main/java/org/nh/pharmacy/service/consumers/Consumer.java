package org.nh.pharmacy.service.consumers;

import org.nh.billing.domain.PamIntegration;
import org.nh.billing.domain.dto.PamDocument;
import org.nh.billing.service.PamIntegrationService;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.StockEntry;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Objects.nonNull;

/**
 * Created by Nitesh on 3/27/17.
 */
@Component
public class Consumer {

    private final Logger log = LoggerFactory.getLogger(Consumer.class);

    private final StockService stockService;

    private final StockSourceService stockSourceService;

    private final ItemStoreStockViewService itemStoreStockViewService;

    private final PamIntegrationService pamIntegrationService;

    private final MedicationRequestService medicationRequestService;

    private final MedicationOrderService medicationOrderService;

    private final StockConsumptionService stockConsumptionService;

    private final ReserveStockService reserveStockService;

    private final PrescriptionAuditRequestService prescriptionAuditRequestService;

    @Autowired
    SystemAlertService systemAlertService;

    public Consumer(StockService stockService, StockSourceService stockSourceService, ItemStoreStockViewService itemStoreStockViewService,
                    PamIntegrationService pamIntegrationService, MedicationRequestService medicationRequestService,
                    MedicationOrderService medicationOrderService, StockConsumptionService stockConsumptionService, ReserveStockService reserveStockService,
                    PrescriptionAuditRequestService prescriptionAuditRequestService) {
        this.stockService = stockService;
        this.stockSourceService = stockSourceService;
        this.itemStoreStockViewService = itemStoreStockViewService;
        this.pamIntegrationService = pamIntegrationService;
        this.medicationRequestService = medicationRequestService;
        this.medicationOrderService = medicationOrderService;
        this.stockConsumptionService = stockConsumptionService;
        this.reserveStockService = reserveStockService;
        this.prescriptionAuditRequestService = prescriptionAuditRequestService;
    }

    @ServiceActivator(inputChannel = Channels.STOCK_INPUT)
    public void consume(Map<String, Object> map){
        log.debug("Request to consume for document number : {}", map.get("DocumentNo"));
        try {
            stockService.stockOut(map.get("DocumentNo").toString());
        }
        catch(Exception exception){
            log.error("Exception occurred while performing stock out transaction ", exception);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(exception));
        }
    }
    @ServiceActivator(inputChannel = Channels.MOVE_TO_TRANSIT_STOCK_INPUT)
    public void consumeStockTransit(Map<String, Object> map){
        log.debug("Request to consume for document number : {}", map.get("DocumentNo"));
        try {
            stockService.moveStockToTransit(map.get("DocumentNo").toString(),null);
        }
        catch(Exception exception){
            log.error("Exception occurred while performing Stock Transit", exception);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(exception));
        }
    }
    @ServiceActivator(inputChannel = Channels.STOCK_MOVE_INPUT)
    public void consumeStockMove(Map<String, Object> stockEntryList) {
        try {
            stockService.processStockMove(stockEntryList);
        } catch (Exception exception) {
            log.error("Exception occurred while performing stock correction ", exception);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(exception));
        }
    }

    @ServiceActivator(inputChannel = Channels.ITEM_STORE_STOCK_INPUT)
    public void updateItemStoreStockView(Map<String, Object> ids){
        log.debug("Request to update ItemStoreStockView");
        try{
            Set<Long> itemIds = new HashSet<>(((Collection)ids.get("itemIds")).size());
            for (Object id: (Collection)ids.get("itemIds")) {
                itemIds.add(Long.valueOf(id.toString()));
            }
            Long storeId = null;
            if(ids.get("storeId") != null) {
                storeId = Long.valueOf(ids.get("storeId").toString());
            }
            if(ids.containsKey("transitStoreType")) {
                String transitStoreIds = ids.get("transitStore").toString();
                if((ids.get("transitStoreType")).equals(TransactionType.Stock_Receipt.name()) || (ids.get("transitStoreType")).equals(TransactionType.Inter_Unit_Stock_Receipt.name())){
                    itemStoreStockViewService.updateItemStoreStockView(itemIds, storeId);
                    itemStoreStockViewService.updateTransitQuantity(itemIds, transitStoreIds);
                } else {
                    itemStoreStockViewService.updateTransitQuantity(itemIds, transitStoreIds);
                }
            } else {
                itemStoreStockViewService.updateItemStoreStockView(itemIds, storeId);
            }
        } catch (Exception e){
            log.error("Exception occured while updating ItemStoreStockView", e);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(e));
        }
    }

    @ServiceActivator(inputChannel = Channels.PAM_ACKNOWLEDGEMENT_INTPUT)
    public void updatePamIntegration(PamDocument pamDocument){
        log.debug("Request to update PamIntegration : {} ",pamDocument);
        try{
            List<PamIntegration> pamIntegrationList = pamIntegrationService.findByTxnRefNo(pamDocument.getTransactionRefNo());
            if (Objects.isNull(pamIntegrationList) || pamIntegrationList.isEmpty()) {
                log.info("Request to update PamIntegration cannot be done as record is not found for {}", pamDocument.getTransactionRefNo());
                return;
            }
            for(PamIntegration  pamIntegration:pamIntegrationList) {
                pamIntegration.setProcessedDate(pamDocument.getInsertedDate());
                pamIntegrationService.save(pamIntegration);
            }
        } catch (Exception e){
            log.error("Exception occured while updating PamIntegration", e);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message(new StringBuilder("Error while processing message for pam acknowledge ").append(pamDocument.getTransactionRefNo()).toString())
                .addDescription(e));
        }
    }

    @ServiceActivator(inputChannel = Channels.STOCK_SOURCE_INPUT)
    public void updateStockSource(Map<String, Object> map){
        if(!map.isEmpty()) {
            log.debug("Request to update stock source for transaction : {}", map.get("TransactionNumber"));
            stockSourceService.saveStockFlowStockSourceIds((Map<String, String>) map.get("stockFlowIdSkuMap"));
            Map<String, Double> skuQuantityMap = (Map<String, Double>) map.get("SkuQuantityMap");
            LocalDate transactionDate = (LocalDateTime.parse((String) map.get("TransactionDate"))).toLocalDate();
            FlowType flowType = FlowType.valueOf((String) map.get("FlowType"));
            try {
                if (flowType.equals(FlowType.StockOut)) {
                    stockSourceService.reduceStockSourceQuantity(skuQuantityMap, transactionDate);
                } else if (flowType.equals(FlowType.StockIn)) {
                    stockSourceService.increaseStockSourceQuantity(skuQuantityMap, transactionDate);
                } else {
                    log.error("Invalid Flow Type : " + flowType.getFlowTypeDisplay());
                }
            } catch (Exception exception) {
                log.error("Exception occurred while performing stock source update transaction ", exception);
                systemAlertService.save(new SystemAlert()
                    .fromClass(this.getClass().getName())
                    .onDate(ZonedDateTime.now())
                    .message("Error while processing message ")
                    .addDescription(exception));
            }
        }
    }


    @ServiceActivator(inputChannel = Channels.ITEM_STORE_STOCK_VIEW_INPUT)
    public void updateISSVTransitQty(Map<String, Object> map){
        log.debug("Request to consume for store : {}", map.get("issueStore"));
        try {
            Object changedName = map.get("changedName");
            if(nonNull(changedName) && Boolean.valueOf(changedName.toString())){
                itemStoreStockViewService.updateItemName(map);
            } else {
                itemStoreStockViewService.updateISSVTransitQty(map);
            }
        }
        catch(Exception exception){
            log.error("Exception occurred while performing Stock Transit", exception);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(exception));
        }
    }

    @ServiceActivator(inputChannel = Channels.MEDICATION_REQUEST_INPUT)
    public void consume(MedicationRequest medicationRequest) {
        handleMedicationRequestInput(medicationRequest);
    }

    //@Async(value = Constants.PHR_KAFKA_TASK_THREAD_POOL)
    public void handleMedicationRequestInput(MedicationRequest medicationRequest)
    {
        try {
            log.info("Request to consume MedicationRequest : {}", medicationRequest.getDocumentNumber());
            medicationRequestService.updateMedicationRequest(medicationRequest);
        } catch (Exception ex) {
            log.error("Error occurred while processing the medication request number={}, Ex={}",medicationRequest.getDocumentNumber(),ex);
            medicationRequestService.reIndex(medicationRequest.getId());
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing medication request message ")
                .addDescription(ex));
        }
    }

    @ServiceActivator(inputChannel = Channels.MEDICATION_REQUEST_STATUS_INPUT)
    public void updateMedicationRequest(MedicationRequest medicationRequest){
        try
        {
            log.debug("Request received to update medication request: {}", medicationRequest.getDocumentNumber());
            medicationRequestService.updateMedicationRequest(medicationRequest);
        }catch( Exception ex)
        {
            log.error("Error occurred while processing the medication request number={}, Ex={}",medicationRequest.getDocumentNumber(),ex);
            medicationRequestService.reIndex(medicationRequest.getId());
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }

    @ServiceActivator(inputChannel = Channels.MEDICATION_ORDER_INPUT)
    public void consumeMedicationOrder(MedicationOrder medicationOrder)
    {
        log.debug("Received medication order from cpoe. Medication order ={} ",medicationOrder);
        try
        {
            medicationOrderService.save(medicationOrder);
            //medicationOrderService.refreshIndex();

        }catch(Exception ex)
        {
            log.error("Error while creating the cpoe medication order in pharmacy. Ex={}",ex);
        }
    }

    @ServiceActivator(inputChannel = Channels.STORE_AUTO_CONSUMPTION_INPUT)
    public void consumeStoreStockAutoConsumption(Map<String, Object> params) {
        log.debug("consumeStoreStockAutoConsumption: {}", params);
        try {
            stockConsumptionService.stockAutoConsumption(params);

        } catch(Exception e) {
            log.error("Error while auto consuming the stock receipt: {}", e);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(e));
        }
    }

    @ServiceActivator(inputChannel = Channels.EXTERNAL_STOCK_CONSUMPTION_INPUT)
    public void consumeExternalStockConsumption(StockConsumption stockConsumption) {
        log.debug("Request to Consume BloodBag StockConsumption: {}", stockConsumption);
        try {
            stockConsumptionService.consumeExternalStockConsumption(stockConsumption);
        }
        catch(Exception e) {
            log.error("Error while  saving the stock consumption: {}", e);
            stockService.deleteReservedStock(stockConsumption.getId(),stockConsumption.getDocument().getDocumentType());
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(e));
        }
    }

    @ServiceActivator(inputChannel = Channels.BLOOD_BAG_STOCK_CONSUMPTION_INPUT)
    public void consumeBloodBagConsumption(Map<String, Object> stockMap) {
        log.debug("Request to Consume BloodBag StockConsumption: {}", stockMap);

        try {
            log.debug("Request to reserve stock ");

            Float requestedQuantity = Float.parseFloat(stockMap.get("quantity").toString());
            Long itemId = Long.valueOf(stockMap.get("itemId").toString());
            Long stockId = Long.valueOf(stockMap.get("stockId").toString());
            Long storeId = Long.valueOf(stockMap.get("storeId").toString());
            String batchNumber = stockMap.get("batchNumber").toString();
            Long userId = Long.valueOf(stockMap.get("userId").toString());

            stockService.getStockWithLock(stockId);
            Float availableQuantity = (stockId != null ? getAvailableStock(stockId) : getAvailableStock(itemId, batchNumber, storeId));

            if (requestedQuantity <= availableQuantity) {
                ReserveStock reserveStock = new ReserveStock();
                reserveStock.setStockId(stockId);
                reserveStock.setQuantity(requestedQuantity);
                reserveStock.setReservedDate(LocalDateTime.parse(stockMap.get("transactionDate").toString()));
                reserveStock.setTransactionId(Long.valueOf(stockMap.get("transactionId").toString()));
                reserveStock.setTransactionLineId(Long.valueOf(stockMap.get("transactionId").toString()));
                reserveStock.setTransactionNo(stockMap.get("transactionNumber").toString());
                reserveStock.setTransactionType(TransactionType.Stock_Consumption);
                reserveStock.setTransactionDate(LocalDateTime.parse(stockMap.get("transactionDate").toString()));
                reserveStock.setUserId(userId);
                reserveStockService.save(reserveStock);

                stockService.stockOut(stockMap.get("transactionNumber").toString());
            }
        } catch(Exception e) {
            log.error("Error while saving the blood bag stock consumption: {}", e);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(e));
        }
    }

    private Float getAvailableStock(Long stockId) {
        log.debug("Request to find available stock with Id : {}", stockId);
        Float currentStock = stockService.findOne(stockId).getQuantity();
        List<ReserveStock> reservedStockList = reserveStockService.findAllReservedStockByStockId(stockId);
        Float totalReservedStock = 0F;
        for (ReserveStock reservedStock : reservedStockList) {
            totalReservedStock += reservedStock.getQuantity();
        }

        return (currentStock - totalReservedStock);
    }

    private Float getAvailableStock(Long itemId, String batchNo, Long storeId) throws StockException {
        log.debug("Request to find Available stock with item : {}, batch : {}, store : {}", itemId, batchNo, storeId);
        List<Stock> stocks = stockService.getStockWithFields(itemId, batchNo, storeId);
        if (stocks == null || stocks.isEmpty()) {
            String message = "Insufficient stock for given combination of item :" + itemId + " batch :" + batchNo + " and store :" + storeId;
            throw new StockException(itemId, batchNo, storeId, message);
        }
        Float availableStock = Float.valueOf(0);
        Float totalReservedStock = Float.valueOf(0);

        for (Stock stock: stocks) {
            List<ReserveStock> reservedStockList = reserveStockService.findAllReservedStockByStockId(stock.getId());
            for (ReserveStock reservedStock : reservedStockList) {
                totalReservedStock += reservedStock.getQuantity();
            }
            availableStock += stock.getQuantity();
        }

        return availableStock - totalReservedStock;
    }

    @ServiceActivator(inputChannel = Channels.BLOOD_BAG_STOCK_REVERSAL_INPUT)
    public void bloodBagStockReversal(Map<String, Object> stockMap) {
        log.debug("Stock Reversal for Unused Blood Bags. Stock Map: {}", stockMap);

        try {
            Stock stock = stockService.getStockByUniqueIdAndStoreId(stockMap.get("sku").toString(), Long.valueOf(stockMap.get("storeId").toString()));
            log.debug("Stock Details for SKU and Store Id: {}", stock);
            StockEntry stockEntry = new StockEntry();

            stockEntry.setItemId(Long.valueOf(stockMap.get("itemId").toString()));
            stockEntry.setStoreId(Long.valueOf(stockMap.get("storeId").toString()));
            stockEntry.setStockId(Long.valueOf(stockMap.get("stockId").toString()));
            stockEntry.setBatchNo(stockMap.get("batchNumber").toString());
            stockEntry.setSku(stockMap.get("sku").toString());
            stockEntry.setTransactionNumber(stockMap.get("transactionNumber").toString());
            stockEntry.setTransactionId(Long.valueOf(stockMap.get("transactionId").toString()));
            stockEntry.setTransactionLineId(Long.valueOf(stockMap.get("transactionId").toString()));
            stockEntry.setTransactionDate(LocalDateTime.parse(stockMap.get("transactionDate").toString()));
            stockEntry.setTransactionType(TransactionType.Stock_Reversal_Consumption);
            stockEntry.setQuantity(Float.valueOf(stockMap.get("quantity").toString()));

            stockEntry.setUnitId(stock.getUnitId());
            stockEntry.setLocatorId(stock.getLocatorId());
            stockEntry.setUomId(stock.getUomId());
            stockEntry.setAvailableQuantity(stock.getQuantity());
            stockEntry.setBarCode(stock.getBarcode());
            stockEntry.setConsignment(false);
            stockEntry.setCost(stock.getCost());
            stockEntry.setExpiryDate(stock.getExpiryDate());
            stockEntry.setMrp(stock.getMrp());
            stockEntry.setOwner(stock.getOwner());
            stockEntry.setSupplier(stock.getSupplier());
            stockEntry.setUserId(Long.valueOf(stockMap.get("userId").toString()));

            log.debug("Stock Entry Details: {}", stockEntry);

            stockService.stockIn(Arrays.asList(stockEntry));
        } catch(Exception e) {
            log.error("Error while saving the blood bag stock reversal: {}", e);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(e));
        }
    }


    /**
     * Method to consume medication request and create/update Pending Audit Request document
     * @param medicationRequest
     */
    @ServiceActivator(inputChannel = Channels.MEDICATION_REQUEST_PRESCRIPTION_AUDIT_INPUT)
    public void consumeMedicationRequestForPA(MedicationRequest medicationRequest) {
        try {
            prescriptionAuditRequestService.handleMedicationRequestInput(medicationRequest);
        } catch (Exception ex) {
            log.error("Error while consuming pending audit request. ex: {} ", ex);
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing pending audit message ")
                .addDescription(ex));
        }
    }
}
