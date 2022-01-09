package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.FlowType;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A StockFlow.
 */
@Entity
@Table(name = "stock_flow")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StockFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @NotNull
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @NotNull
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @NotNull
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @NotNull
    @Column(name = "locator_id", nullable = false)
    private Long locatorId;

    @NotNull
    @Column(name = "transaction_line_id", nullable = false)
    private Long transactionLineId;

    @NotNull
    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @NotNull
    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @NotNull
    @Column(name = "owner", nullable = false)
    private String owner;

    @NotNull
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @Column(name = "mrp")
    private BigDecimal mrp;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false)
    private FlowType flowType;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "consignment")
    private Boolean consignment = false;

    @NotNull
    @Column(name = "transaction_number", nullable = false)
    private String transactionNumber;

    @NotNull
    @Column(name = "average_cost", nullable = false)
    private BigDecimal averageCost;

    @NotNull
    @Column(name = "average_cost_value", nullable = false)
    private BigDecimal averageCostValue;

    @NotNull
    @Column(name = "cost_value", nullable = false)
    private BigDecimal costValue;

    @Column(name = "entry_date")
    private LocalDateTime entryDate = LocalDateTime.now();

    @Column(name = "bar_code")
    private String barCode;

    @Column(name = "user_id")
    private Long userId;

    @Transient
    private Boolean stockDataProcessor = Boolean.TRUE;

    public Boolean getStockDataProcessor() {return stockDataProcessor;}

    public void setStockDataProcessor(Boolean stockDataProcessor) {this.stockDataProcessor = stockDataProcessor;}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public StockFlow itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getStockId() {
        return stockId;
    }

    public StockFlow stockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public StockFlow storeId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public StockFlow transactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getLocatorId() {
        return locatorId;
    }

    public StockFlow locatorId(Long locatorId) {
        this.locatorId = locatorId;
        return this;
    }

    public void setLocatorId(Long locatorId) {
        this.locatorId = locatorId;
    }

    public Long getTransactionLineId() {
        return transactionLineId;
    }

    public StockFlow transactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
        return this;
    }

    public void setTransactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
    }

    public Long getUomId() {
        return uomId;
    }

    public StockFlow uomId(Long uomId) {
        this.uomId = uomId;
        return this;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getSku() {
        return sku;
    }

    public StockFlow sku(String sku) {
        this.sku = sku;
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public StockFlow batchNo(String batchNo) {
        this.batchNo = batchNo;
        return this;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public StockFlow expiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getOwner() {
        return owner;
    }

    public StockFlow owner(String owner) {
        this.owner = owner;
        return this;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public StockFlow cost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public StockFlow mrp(BigDecimal mrp) {
        this.mrp = mrp;
        return this;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public StockFlow flowType(FlowType flowType) {
        this.flowType = flowType;
        return this;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public Float getQuantity() {
        return quantity;
    }

    public StockFlow quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public StockFlow transactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public StockFlow transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Boolean isConsignment() {
        return consignment;
    }

    public StockFlow consignment(Boolean consignment) {
        this.consignment = consignment;
        return this;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public StockFlow transactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
        return this;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public StockFlow averageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        return this;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getAverageCostValue() {
        return averageCostValue;
    }

    public StockFlow averageCostValue(BigDecimal averageCostValue) {
        this.averageCostValue = averageCostValue;
        return this;
    }

    public void setAverageCostValue(BigDecimal averageCostValue) {
        this.averageCostValue = averageCostValue;
    }

    public BigDecimal getCostValue() {
        return costValue;
    }

    public StockFlow costValue(BigDecimal costValue) {
        this.costValue = costValue;
        return this;
    }

    public void setCostValue(BigDecimal costValue) {
        this.costValue = costValue;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public StockFlow entryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
        return this;
    }

    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public String getBarCode() {
        return barCode;
    }

    public StockFlow barCode(String barCode) {
        this.barCode = barCode;
        return this;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public StockFlow userId(Long userId) {
        this.userId = userId;
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StockFlow stockFlow = (StockFlow) o;
        if (stockFlow.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, stockFlow.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "StockFlow{" +
            "id=" + id +
            ", itemId='" + itemId + "'" +
            ", stockId='" + stockId + "'" +
            ", storeId='" + storeId + "'" +
            ", transactionId='" + transactionId + "'" +
            ", locatorId='" + locatorId + "'" +
            ", transactionLineId='" + transactionLineId + "'" +
            ", uomId='" + uomId + "'" +
            ", sku='" + sku + "'" +
            ", batchNo='" + batchNo + "'" +
            ", expiryDate='" + expiryDate + "'" +
            ", owner='" + owner + "'" +
            ", cost='" + cost + "'" +
            ", mrp='" + mrp + "'" +
            ", flowType='" + flowType + "'" +
            ", quantity='" + quantity + "'" +
            ", transactionDate='" + transactionDate + "'" +
            ", transactionType='" + transactionType + "'" +
            ", consignment='" + consignment + "'" +
            ", transactionNumber='" + transactionNumber + "'" +
            ", averageCost='" + averageCost + "'" +
            ", averageCostValue='" + averageCostValue + "'" +
            ", costValue='" + costValue + "'" +
            ", entryDate='" + entryDate + "'" +
            ", barCode='" + barCode + "'" +
            '}';
    }
}
