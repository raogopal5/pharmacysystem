package org.nh.pharmacy.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * A StockSource.
 */
@Entity
@Table(name = "stock_source")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StockSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

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
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull
    @Column(name = "transaction_ref_no", nullable = false)
    private String transactionRefNo;

    @NotNull
    @Column(name = "first_stock_in_date", nullable = false)
    private LocalDate firstStockInDate;

    @Column(name = "last_stock_out_date")
    private LocalDate lastStockOutDate;

    @Column(name = "consignment")
    private Boolean consignment = false;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @NotNull
    @Column(name = "available_quantity", nullable = false)
    private Float availableQuantity;

    @NotNull
    @Column(name = "supplier", nullable = false)
    private String supplier;

    @Column(name = "tax_name")
    private String taxName;

    @Column(name = "tax_per_unit")
    private BigDecimal taxPerUnit;

    @Column(name = "tax_type")
    private String taxType;

    @Column(name = "bar_code")
    private String barCode;

    @Column(name = "original_batch_no")
    private String originalBatchNo;

    @Column(name = "original_mrp")
    private BigDecimal originalMRP;

    @Column(name = "original_expiry_date")
    private LocalDate originalExpiryDate;

    @Column(name = "mfr_barcode")
    private String mfrBarcode;

    @JsonSerialize
    @JsonDeserialize
    @Transient
    private Integer printQuantity;

    @JsonSerialize
    @JsonDeserialize
    @Transient
    private String itemName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "taxes")
    private Map<String, Object> taxes;

    @Column(name = "blocked")
    private Boolean blocked = false;

    @Column(name = "recoverable_tax")
    private Boolean recoverableTax = false;

    @Column(name = "cost_without_tax")
    private BigDecimal costWithoutTax;

    public StockSource() {
    }

    public StockSource(Long id, Long itemId, Long uomId, String sku, String batchNo, LocalDate expiryDate, String owner, BigDecimal cost, BigDecimal mrp, TransactionType transactionType, String transactionRefNo, LocalDate firstStockInDate, LocalDate lastStockOutDate, Boolean consignment, Float quantity, Float availableQuantity, String supplier, String taxName, BigDecimal taxPerUnit, String taxType, String barCode, String originalBatchNo, BigDecimal originalMRP, LocalDate originalExpiryDate, String mfrBarcode, String itemName) {
        this.id = id;
        this.itemId = itemId;
        this.uomId = uomId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.owner = owner;
        this.cost = cost;
        this.mrp = mrp;
        this.transactionType = transactionType;
        this.transactionRefNo = transactionRefNo;
        this.firstStockInDate = firstStockInDate;
        this.lastStockOutDate = lastStockOutDate;
        this.consignment = consignment;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.supplier = supplier;
        this.taxName = taxName;
        this.taxPerUnit = taxPerUnit;
        this.taxType = taxType;
        this.barCode = barCode;
        this.originalBatchNo = originalBatchNo;
        this.originalMRP = originalMRP;
        this.originalExpiryDate = originalExpiryDate;
        this.mfrBarcode = mfrBarcode;
        this.itemName = itemName;
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

    public StockSource itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getUomId() {
        return uomId;
    }

    public StockSource uomId(Long uomId) {
        this.uomId = uomId;
        return this;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getSku() {
        return sku;
    }

    public StockSource sku(String sku) {
        this.sku = sku;
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public StockSource batchNo(String batchNo) {
        this.batchNo = batchNo;
        return this;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public StockSource expiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getOwner() {
        return owner;
    }

    public StockSource owner(String owner) {
        this.owner = owner;
        return this;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public StockSource cost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public StockSource mrp(BigDecimal mrp) {
        this.mrp = mrp;
        return this;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public StockSource transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionRefNo() {
        return transactionRefNo;
    }

    public StockSource transactionRefNo(String transactionRefNo) {
        this.transactionRefNo = transactionRefNo;
        return this;
    }

    public void setTransactionRefNo(String transactionRefNo) {
        this.transactionRefNo = transactionRefNo;
    }

    public LocalDate getFirstStockInDate() {
        return firstStockInDate;
    }

    public StockSource firstStockInDate(LocalDate firstStockInDate) {
        this.firstStockInDate = firstStockInDate;
        return this;
    }

    public void setFirstStockInDate(LocalDate firstStockInDate) {
        this.firstStockInDate = firstStockInDate;
    }

    public LocalDate getLastStockOutDate() {
        return lastStockOutDate;
    }

    public StockSource lastStockOutDate(LocalDate lastStockOutDate) {
        this.lastStockOutDate = lastStockOutDate;
        return this;
    }

    public void setLastStockOutDate(LocalDate lastStockOutDate) {
        this.lastStockOutDate = lastStockOutDate;
    }

    public Boolean isConsignment() {
        return consignment;
    }

    public StockSource consignment(Boolean consignment) {
        this.consignment = consignment;
        return this;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public Float getQuantity() {
        return quantity;
    }

    public StockSource quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public Float getAvailableQuantity() {
        return availableQuantity;
    }

    public StockSource availableQuantity(Float availableQuantity) {
        this.availableQuantity = availableQuantity;
        return this;
    }

    public void setAvailableQuantity(Float availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public StockSource supplier(String supplier) {
        this.supplier = supplier;
        return this;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getTaxName() {
        return taxName;
    }

    public StockSource taxName(String taxName) {
        this.taxName = taxName;
        return this;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public BigDecimal getTaxPerUnit() {
        return taxPerUnit;
    }

    public StockSource taxPerUnit(BigDecimal taxPerUnit) {
        this.taxPerUnit = taxPerUnit;
        return this;
    }

    public void setTaxPerUnit(BigDecimal taxPerUnit) {
        this.taxPerUnit = taxPerUnit;
    }

    public String getTaxType() {
        return taxType;
    }

    public StockSource taxType(String taxType) {
        this.taxType = taxType;
        return this;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getBarCode() {
        return barCode;
    }

    public StockSource barCode(String barCode) {
        this.barCode = barCode;
        return this;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getOriginalBatchNo() {
        return originalBatchNo;
    }

    public StockSource originalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
        return this;
    }

    public void setOriginalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
    }

    public BigDecimal getOriginalMRP() {
        return originalMRP;
    }

    public StockSource originalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
        return this;
    }

    public void setOriginalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
    }

    public LocalDate getOriginalExpiryDate() {
        return originalExpiryDate;
    }

    public StockSource originalExpiryDate(LocalDate originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
        return this;
    }

    public void setOriginalExpiryDate(LocalDate originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
    }

    public String getMfrBarcode() {
        return mfrBarcode;
    }

    public StockSource mfrBarcode(String mfrBarcode) {
        this.mfrBarcode = mfrBarcode;
        return this;
    }

    public void setMfrBarcode(String mfrBarcode) {
        this.mfrBarcode = mfrBarcode;
    }

    public String getItemName() {
        return itemName;
    }

    public StockSource itemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Map<String, Object> getTaxes() {
        return taxes;
    }

    public StockSource taxes(Map<String, Object> taxes) {
        this.taxes = taxes;
        return this;
    }

    public void setTaxes(Map<String, Object> taxes) {
        this.taxes = taxes;
    }

    public Integer getPrintQuantity() {return printQuantity;}

    public void setPrintQuantity(Integer printQuantity) {
        this.printQuantity = printQuantity;
    }

    public Boolean getBlocked() {return blocked;}

    public void setBlocked(Boolean blocked) {this.blocked = blocked;}

    public Boolean getRecoverableTax() {return recoverableTax;}

    public void setRecoverableTax(Boolean recoverableTax) {this.recoverableTax = recoverableTax;}

    public BigDecimal getCostWithoutTax() {return costWithoutTax;}

    public void setCostWithoutTax(BigDecimal costWithoutTax) {this.costWithoutTax = costWithoutTax;}

    public StockSource recoverableTax(Boolean recoverableTax) {
        this.recoverableTax = recoverableTax;
        return this;
    }

    public StockSource costWithoutTax(BigDecimal costWithoutTax) {
        this.costWithoutTax = costWithoutTax;
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
        StockSource stockSource = (StockSource) o;
        if (stockSource.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, stockSource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "StockSource{" +
            "id=" + id +
            ", itemId=" + itemId +
            ", uomId=" + uomId +
            ", sku='" + sku + '\'' +
            ", batchNo='" + batchNo + '\'' +
            ", expiryDate=" + expiryDate +
            ", owner='" + owner + '\'' +
            ", cost=" + cost +
            ", mrp=" + mrp +
            ", transactionType=" + transactionType +
            ", transactionRefNo='" + transactionRefNo + '\'' +
            ", firstStockInDate=" + firstStockInDate +
            ", lastStockOutDate=" + lastStockOutDate +
            ", consignment=" + consignment +
            ", quantity=" + quantity +
            ", availableQuantity=" + availableQuantity +
            ", supplier='" + supplier + '\'' +
            ", taxName='" + taxName + '\'' +
            ", taxPerUnit=" + taxPerUnit +
            ", taxType='" + taxType + '\'' +
            ", barCode='" + barCode + '\'' +
            ", originalBatchNo='" + originalBatchNo + '\'' +
            ", originalMRP=" + originalMRP +
            ", originalExpiryDate=" + originalExpiryDate +
            ", mfrBarcode='" + mfrBarcode + '\'' +
            ", itemName='" + itemName + '\'' +
            ", recoverableTax='" + recoverableTax + '\'' +
            ", costWithoutTax='" + costWithoutTax + '\'' +
            ", taxes='" + taxes + '\'' +
            '}';
    }
}
