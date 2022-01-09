package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * StockEntry DTO
 */
public class StockEntry implements Serializable {

    @NotNull
    private Long itemId;
    @NotNull
    private Long locatorId;
    private Long stockId;
    @NotNull
    private Long storeId;
    private String batchNo;
    @NotNull
    private String owner;
    private String supplier;
    @NotNull
    private Long unitId;
    @NotNull
    private Long uomId;

    private BigDecimal taxPerUnit;
    private String taxName;
    private String taxType;
    private LocalDate firstStockInDate;
    private LocalDate lastStockOutDate;
    private Boolean consignment;
    @NotNull
    private BigDecimal cost;
    private BigDecimal mrp;
    private String sku;
    @NotNull
    private Float quantity;
    private Float availableQuantity;
    private LocalDate expiryDate;
    private LocalDateTime transactionDate;
    @NotNull
    private Long transactionId;
    @NotNull
    private Long transactionLineId;
    @NotNull
    private String transactionNumber;
    private String transactionRefNo;
    @NotNull
    private TransactionType transactionType;
    private String barCode;

    private Long stockFlowId;

    private String originalBatchNo;

    private LocalDate originalExpiryDate;

    private BigDecimal originalMRP;

    @Field(type = FieldType.Object)
    private Map<String, Object> taxes;

    private Boolean recoverableTax;
    private BigDecimal costWithoutTax;
    private Long userId;

    public Boolean getRecoverableTax() {return recoverableTax;}

    public void setRecoverableTax(Boolean recoverableTax) {this.recoverableTax = recoverableTax;}

    public BigDecimal getCostWithoutTax() {return costWithoutTax;}

    public void setCostWithoutTax(BigDecimal costWithoutTax) {
        this.costWithoutTax = costWithoutTax;
    }

    public Long getStockId() {
        return stockId;
    }

    public StockEntry stockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public StockEntry transactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getTransactionLineId() {
        return transactionLineId;
    }

    public StockEntry transactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
        return this;
    }

    public void setTransactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public StockEntry transactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public StockEntry transactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
        return this;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public StockEntry unitId(Long unitId) {
        this.unitId = unitId;
        return this;
    }

    public Long getStoreId() {
        return storeId;
    }

    public StockEntry storeId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getItemId() {
        return itemId;
    }

    public StockEntry itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getUomId() {
        return uomId;
    }

    public StockEntry uomId(Long uomId) {
        this.uomId = uomId;
        return this;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getSku() {
        return sku;
    }


    public StockEntry sku(String sku) {
        this.sku = sku;
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getLocatorId() {
        return locatorId;
    }

    public StockEntry locatorId(Long locatorId) {
        this.locatorId = locatorId;
        return this;
    }

    public void setLocatorId(Long locatorId) {
        this.locatorId = locatorId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public StockEntry batchNo(String batchNo) {
        this.batchNo = batchNo;
        return this;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public StockEntry expiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getOwner() {
        return owner;
    }

    public StockEntry owner(String owner) {
        this.owner = owner;
        return this;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public StockEntry cost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public StockEntry mrp(BigDecimal mrp) {
        this.mrp = mrp;
        return this;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public StockEntry transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionRefNo() {
        return transactionRefNo;
    }

    public StockEntry transactionRefNo(String transactionRefNo) {
        this.transactionRefNo = transactionRefNo;
        return this;
    }

    public void setTransactionRefNo(String transactionRefNo) {
        this.transactionRefNo = transactionRefNo;
    }

    public LocalDate getFirstStockInDate() {
        return firstStockInDate;
    }

    public StockEntry firstStockInDate(LocalDate firstStockInDate) {
        this.firstStockInDate = firstStockInDate;
        return this;
    }

    public void setFirstStockInDate(LocalDate firstStockInDate) {
        this.firstStockInDate = firstStockInDate;
    }

    public LocalDate getLastStockOutDate() {
        return lastStockOutDate;
    }

    public StockEntry lastStockOutDate(LocalDate lastStockOutDate) {
        this.lastStockOutDate = lastStockOutDate;
        return this;
    }

    public void setLastStockOutDate(LocalDate lastStockOutDate) {
        this.lastStockOutDate = lastStockOutDate;
    }

    public Boolean isConsignment() {
        return consignment;
    }

    public StockEntry consignment(Boolean consignment) {
        this.consignment = consignment;
        return this;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public Float getQuantity() {
        return quantity;
    }

    public StockEntry quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public Float getAvailableQuantity() {
        return availableQuantity;
    }

    public StockEntry availableQuantity(Float availableQuantity) {
        this.availableQuantity = availableQuantity;
        return this;
    }

    public void setAvailableQuantity(Float availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public StockEntry supplier(String supplier) {
        this.supplier = supplier;
        return this;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getTaxName() {
        return taxName;
    }

    public StockEntry taxName(String taxName) {
        this.taxName = taxName;
        return this;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public BigDecimal getTaxPerUnit() {
        return taxPerUnit;
    }

    public StockEntry taxPerUnit(BigDecimal taxPerUnit) {
        this.taxPerUnit = taxPerUnit;
        return this;
    }

    public void setTaxPerUnit(BigDecimal taxPerUnit) {
        this.taxPerUnit = taxPerUnit;
    }

    public String getTaxType() {
        return taxType;
    }

    public StockEntry taxType(String taxType) {
        this.taxType = taxType;
        return this;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getBarCode() {
        return barCode;
    }

    public StockEntry barCode(String barCode) {
        this.barCode = barCode;
        return this;
    }

    public Long getStockFlowId() {
        return stockFlowId;
    }

    public void setStockFlowId(Long stockFlowId) {
        this.stockFlowId = stockFlowId;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getOriginalBatchNo() {
        return originalBatchNo;
    }

    public void setOriginalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
    }

    public LocalDate getOriginalExpiryDate() {
        return originalExpiryDate;
    }

    public void setOriginalExpiryDate(LocalDate originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
    }

    public BigDecimal getOriginalMRP() {
        return originalMRP;
    }

    public void setOriginalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
    }

    public Map<String, Object> getTaxes() {
        return taxes;
    }

    public void setTaxes(Map<String, Object> taxes) {
        this.taxes = taxes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "StockEntry{" +
            "itemId=" + itemId +
            ", locatorId=" + locatorId +
            ", stockId=" + stockId +
            ", storeId=" + storeId +
            ", batchNo='" + batchNo + '\'' +
            ", owner='" + owner + '\'' +
            ", supplier='" + supplier + '\'' +
            ", unitId=" + unitId +
            ", uomId=" + uomId +
            ", taxPerUnit=" + taxPerUnit +
            ", taxName='" + taxName + '\'' +
            ", taxType='" + taxType + '\'' +
            ", firstStockInDate=" + firstStockInDate +
            ", lastStockOutDate=" + lastStockOutDate +
            ", consignment=" + consignment +
            ", cost=" + cost +
            ", mrp=" + mrp +
            ", sku='" + sku + '\'' +
            ", quantity=" + quantity +
            ", availableQuantity=" + availableQuantity +
            ", expiryDate=" + expiryDate +
            ", transactionDate=" + transactionDate +
            ", transactionId=" + transactionId +
            ", transactionLineId=" + transactionLineId +
            ", transactionNumber='" + transactionNumber + '\'' +
            ", transactionRefNo='" + transactionRefNo + '\'' +
            ", transactionType=" + transactionType +
            ", barCode='" + barCode + '\'' +
            ", stockFlowId=" + stockFlowId +
            ", originalBatchNo='" + originalBatchNo + '\'' +
            ", originalExpiryDate=" + originalExpiryDate +
            ", originalMRP=" + originalMRP +
            ", taxes=" + taxes +
            '}';
    }
}
