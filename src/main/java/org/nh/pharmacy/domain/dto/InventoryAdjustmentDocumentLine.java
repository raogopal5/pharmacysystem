package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.LocatorDTO;
import org.nh.pharmacy.domain.ValueSetCode;
import org.nh.pharmacy.domain.enumeration.AdjustmentType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by Nitesh on 4/5/17.l
 */
public class InventoryAdjustmentDocumentLine implements Serializable {

    private Long id;                                        // Line Id
    private ItemDTO item;                                      // Item details
    private String batchNumber;
    private LocatorDTO locator;                                // location of the item
    private Long stockId;
    private String owner;
    private String sku;                                     // sku Id
    private BigDecimal cost;
    private BigDecimal mrp;
    private BigDecimal adjustValue;                              // Value of the adjusted stock
    private Boolean consignment = false;
    private LocalDate expiryDate;                           // Expiry date of the item
    private Quantity stockQuantity;                        // Quantity available in Store
    private Quantity adjustQuantity;                        // Quantity to be adjusted
    private String barcode;
    private ValueSetCode reason;                            // Reason for the stock adjustment
    private AdjustmentType adjustmentType;                      // +ve or -ve Adjustment

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getAdjustValue() {
        return adjustValue;
    }

    public void setAdjustValue(BigDecimal adjustValue) {
        this.adjustValue = adjustValue;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Quantity getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Quantity stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Quantity getAdjustQuantity() {
        return adjustQuantity;
    }

    public void setAdjustQuantity(Quantity adjustQuantity) {
        this.adjustQuantity = adjustQuantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ValueSetCode getReason() {
        return reason;
    }

    public void setReason(ValueSetCode reason) {
        this.reason = reason;
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(AdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryAdjustmentDocumentLine that = (InventoryAdjustmentDocumentLine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (batchNumber != null ? !batchNumber.equals(that.batchNumber) : that.batchNumber != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (stockId != null ? !stockId.equals(that.stockId) : that.stockId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (sku != null ? !sku.equals(that.sku) : that.sku != null) return false;
        if (cost != null ? !cost.equals(that.cost) : that.cost != null) return false;
        if (mrp != null ? !mrp.equals(that.mrp) : that.mrp != null) return false;
        if (adjustValue != null ? !adjustValue.equals(that.adjustValue) : that.adjustValue != null) return false;
        if (consignment != null ? !consignment.equals(that.consignment) : that.consignment != null) return false;
        if (expiryDate != null ? !expiryDate.equals(that.expiryDate) : that.expiryDate != null) return false;
        if (stockQuantity != null ? !stockQuantity.equals(that.stockQuantity) : that.stockQuantity != null)
            return false;
        if (adjustQuantity != null ? !adjustQuantity.equals(that.adjustQuantity) : that.adjustQuantity != null)
            return false;
        if (barcode != null ? !barcode.equals(that.barcode) : that.barcode != null) return false;
        if (adjustmentType != that.adjustmentType) return false;
        return reason != null ? reason.equals(that.reason) : that.reason == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        result = 31 * result + (stockId != null ? stockId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (sku != null ? sku.hashCode() : 0);
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        result = 31 * result + (mrp != null ? mrp.hashCode() : 0);
        result = 31 * result + (adjustValue != null ? adjustValue.hashCode() : 0);
        result = 31 * result + (consignment != null ? consignment.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (stockQuantity != null ? stockQuantity.hashCode() : 0);
        result = 31 * result + (adjustQuantity != null ? adjustQuantity.hashCode() : 0);
        result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (adjustmentType != null ? adjustmentType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDocumentLine{" +
            "id=" + id +
            ", item=" + item +
            ", batchNumber='" + batchNumber + '\'' +
            ", locator=" + locator +
            ", stockId=" + stockId +
            ", owner='" + owner + '\'' +
            ", sku='" + sku + '\'' +
            ", cost=" + cost +
            ", mrp=" + mrp +
            ", adjustValue=" + adjustValue +
            ", consignment=" + consignment +
            ", expiryDate=" + expiryDate +
            ", stockQuantity=" + stockQuantity +
            ", adjustQuantity=" + adjustQuantity +
            ", barcode='" + barcode + '\'' +
            ", reason=" + reason +
            ", adjustmentType=" + adjustmentType +
            '}';
    }
}
