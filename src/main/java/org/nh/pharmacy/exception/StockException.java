package org.nh.pharmacy.exception;

/**
 * Created by Nitesh on 2/24/17.
 */
public class StockException extends RuntimeException {

    private Long stockId;
    private Long itemId;
    private String batchNo;
    private Long storeId;
    private Float requestQuantity;
    private String errorCode;
    private String itemName;
    private Float availableQuantity;
    private String storeName;

    public StockException(Long itemId, String batchNo, Long storeId, String message) {
        super(message);
        this.itemId = itemId;
        this.batchNo = batchNo;
        this.storeId = storeId;
    }

    public StockException(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity, String message) {
        this(itemId, batchNo, storeId, message);
        this.stockId = stockId;
        this.requestQuantity = requestedQuantity;
    }

    public StockException(Long stockId, Long itemId, String batchNo, Long storeId, Float requestedQuantity, Float availableQuantity, String message) {
        this(stockId,itemId,batchNo,storeId,requestedQuantity,message);
        this.availableQuantity = availableQuantity;
    }

    public StockException(Long stockId, Long itemId, String itemName, String batchNo, Long storeId, Float requestedQuantity, Float availableQuantity, String message) {
        this(stockId,itemId,batchNo,storeId,requestedQuantity,message);
        this.itemName = itemName;
        this.availableQuantity = availableQuantity;
    }

    public StockException(Long itemId, String batchNo, Long storeId,String itemName, String storeName, String message) {
        this(itemId,batchNo,storeId,message);
        this.itemName = itemName;
        this.storeName = storeName;
    }

    public StockException errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }
    public Long getStockId() {
        return stockId;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Float getRequestQuantity() {
        return requestQuantity;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Float getAvailableQuantity() {
        return this.availableQuantity;
    }

    public String getStoreName() {
        return storeName;
    }

}
