package org.nh.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A Stock.
 */
@Entity
@Table(name = "stock")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @NotNull
    @Column(name = "owner", nullable = false)
    private String owner;

    @NotNull
    @Column(name = "cost", nullable = false)
    @Convert(converter = BigDecimalConverter.class)
    private BigDecimal cost;

    @Column(name = "mrp")
    @Convert(converter = BigDecimalConverter.class)
    private BigDecimal mrp;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @NotNull
    @Column(name = "stock_value", nullable = false)
    private BigDecimal stockValue;

    @NotNull
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @NotNull
    @Column(name = "locator_id", nullable = false)
    private Long locatorId;

    @NotNull
    @Column(name = "supplier", nullable = false)
    private String supplier;

    @NotNull
    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @NotNull
    @Column(name = "sku", nullable = false)
    private String sku;

    @NotNull
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "consignment")
    private Boolean consignment = false;

    @Column(name = "original_batch_no")
    private String originalBatchNo;

    @Column(name = "original_mrp")
    @Convert(converter = BigDecimalConverter.class)
    private BigDecimal originalMRP;

    @Column(name = "original_expiry_date")
    private LocalDate originalExpiryDate;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "transit_quantity", nullable = false)
    private Float transitQuantity=0f;

    @Transient
    @JsonProperty
    @Convert(converter = BigDecimalConverter.class)
    private BigDecimal itemUnitAvgCost;

    @Column(name = "blocked")
    private Boolean blocked = false;

    public Stock() {
    }

    public Stock(Long id, Long itemId, String batchNo, LocalDate expiryDate, String owner, BigDecimal cost, BigDecimal mrp, Float quantity, BigDecimal stockValue, Long storeId, Long locatorId, String supplier, Long uomId, String sku, Long unitId, Boolean consignment) {
        this(id, itemId, batchNo, expiryDate, owner, cost, mrp, quantity, stockValue, storeId, locatorId, supplier, uomId, sku, unitId, consignment, null);
    }

    public Stock(Long id, Long itemId, String batchNo, LocalDate expiryDate, String owner, BigDecimal cost, BigDecimal mrp, Float quantity, BigDecimal stockValue, Long storeId, Long locatorId, String supplier, Long uomId, String sku, Long unitId, Boolean consignment, String barcode) {
        this(id, itemId, batchNo, expiryDate, owner, cost, mrp, quantity, stockValue, storeId, locatorId, supplier, uomId, sku, unitId, consignment, barcode, null);
    }

    public Stock(Long id, Long itemId, String batchNo, LocalDate expiryDate, String owner, BigDecimal cost, BigDecimal mrp, Float quantity, BigDecimal stockValue, Long storeId, Long locatorId, String supplier, Long uomId, String sku, Long unitId, Boolean consignment, String barcode, BigDecimal itemUnitAvgCost) {
        this(id, false, itemId, batchNo, expiryDate, owner, cost, mrp, quantity, stockValue, storeId, locatorId, supplier, uomId, sku, unitId, consignment, barcode, itemUnitAvgCost);
    }

    public Stock(Long id, Boolean blocked, Long itemId, String batchNo, LocalDate expiryDate, String owner, BigDecimal cost, BigDecimal mrp, Float quantity, BigDecimal stockValue, Long storeId, Long locatorId, String supplier, Long uomId, String sku, Long unitId, Boolean consignment, String barcode, BigDecimal itemUnitAvgCost) {
        this.id = id;
        this.itemId = itemId;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.owner = owner;
        this.cost = cost.setScale(6, BigDecimal.ROUND_CEILING);
        this.mrp = mrp.setScale(6, BigDecimal.ROUND_CEILING);
        this.quantity = quantity;
        this.stockValue = stockValue;
        this.storeId = storeId;
        this.locatorId = locatorId;
        this.supplier = supplier;
        this.uomId = uomId;
        this.sku = sku;
        this.unitId = unitId;
        this.consignment = consignment;
        this.barcode = barcode;
        this.itemUnitAvgCost = itemUnitAvgCost;
        this.blocked = blocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public Stock itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public Float getTransitQuantity() {
        return transitQuantity;
    }

    public void setTransitQuantity(Float transitQuantity) {
        this.transitQuantity = transitQuantity;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Stock batchNo(String batchNo) {
        this.batchNo = batchNo;
        return this;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Stock expiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getOwner() {
        return owner;
    }

    public Stock owner(String owner) {
        this.owner = owner;
        return this;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public Stock cost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost.setScale(6, BigDecimal.ROUND_CEILING);
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public Stock mrp(BigDecimal mrp) {
        this.mrp = mrp;
        return this;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp.setScale(6, BigDecimal.ROUND_CEILING);
    }

    public Float getQuantity() {
        return quantity;
    }

    public Stock quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public Stock stockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
        return this;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Stock storeId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getLocatorId() {
        return locatorId;
    }

    public Stock locatorId(Long locatorId) {
        this.locatorId = locatorId;
        return this;
    }

    public void setLocatorId(Long locatorId) {
        this.locatorId = locatorId;
    }

    public String getSupplier() {
        return supplier;
    }

    public Stock supplier(String supplier) {
        this.supplier = supplier;
        return this;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Long getUomId() {
        return uomId;
    }

    public Stock uomId(Long uomId) {
        this.uomId = uomId;
        return this;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getSku() {
        return sku;
    }

    public Stock sku(String sku) {
        this.sku = sku;
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getUnitId() {
        return unitId;
    }

    public Stock unitId(Long unitId) {
        this.unitId = unitId;
        return this;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Boolean isConsignment() {
        return consignment;
    }

    public Stock consignment(Boolean consignment) {
        this.consignment = consignment;
        return this;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public String getOriginalBatchNo() {
        return originalBatchNo;
    }

    public Stock originalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
        return this;
    }

    public void setOriginalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
    }

    public BigDecimal getOriginalMRP() {
        return originalMRP;
    }

    public Stock originalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
        return this;
    }

    public void setOriginalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
    }

    public LocalDate getOriginalExpiryDate() {
        return originalExpiryDate;
    }

    public Stock originalExpiryDate(LocalDate originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
        return this;
    }

    public void setOriginalExpiryDate(LocalDate originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public Stock barcode(String barcode) {
        this.barcode = barcode;
        return this;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }


    public BigDecimal getItemUnitAvgCost() {
        return itemUnitAvgCost;
    }

    public void setItemUnitAvgCost(BigDecimal itemUnitAvgCost) {
        this.itemUnitAvgCost = itemUnitAvgCost;
    }

    public Boolean getBlocked() {return blocked;}

    public void setBlocked(Boolean blocked) {this.blocked = blocked;}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Stock stock = (Stock) o;
        if (stock.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, stock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Stock{" +
            "id=" + id +
            ", itemId=" + itemId +
            ", batchNo='" + batchNo + '\'' +
            ", expiryDate=" + expiryDate +
            ", owner='" + owner + '\'' +
            ", cost=" + cost +
            ", mrp=" + mrp +
            ", quantity=" + quantity +
            ", stockValue=" + stockValue +
            ", storeId=" + storeId +
            ", locatorId=" + locatorId +
            ", supplier='" + supplier + '\'' +
            ", uomId=" + uomId +
            ", sku='" + sku + '\'' +
            ", unitId=" + unitId +
            ", consignment=" + consignment +
            ", originalBatchNo='" + originalBatchNo + '\'' +
            ", originalMRP=" + originalMRP +
            ", originalExpiryDate=" + originalExpiryDate +
            ", barcode='" + barcode + '\'' +
            ", itemUnitAvgCost=" + itemUnitAvgCost +
            '}';
    }
}
