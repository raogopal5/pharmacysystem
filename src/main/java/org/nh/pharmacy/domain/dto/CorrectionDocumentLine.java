package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.LocatorDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by Rohit on 4/6/17.
 */
public class CorrectionDocumentLine implements Serializable {

    private Long id;
    private ItemDTO item;
    private String fromBatchNumber;
    private String toBatchNumber;
    private BigDecimal fromMrp;
    private BigDecimal toMrp;
    private String fromSku;
    private Quantity stockQuantity;
    private Quantity correctionQuantity;
    private LocalDate fromExpiryDate;
    private LocalDate toExpiryDate;
    private Long stockId;
    private String owner;
    private LocatorDTO locator;
    private Boolean consignment = false;
    private Float batchQuantity;
    private String barCode;

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

    public String getFromBatchNumber() {
        return fromBatchNumber;
    }

    public void setFromBatchNumber(String fromBatchNumber) {
        this.fromBatchNumber = fromBatchNumber;
    }

    public String getToBatchNumber() {
        return toBatchNumber;
    }

    public void setToBatchNumber(String toBatchNumber) {
        this.toBatchNumber = toBatchNumber;
    }

    public BigDecimal getFromMrp() {
        return fromMrp;
    }

    public void setFromMrp(BigDecimal fromMrp) {
        this.fromMrp = fromMrp;
    }

    public BigDecimal getToMrp() {
        return toMrp;
    }

    public void setToMrp(BigDecimal toMrp) {
        this.toMrp = toMrp;
    }

    public String getFromSku() {
        return fromSku;
    }

    public void setFromSku(String fromSku) {
        this.fromSku = fromSku;
    }

    public Quantity getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Quantity stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Quantity getCorrectionQuantity() {
        return correctionQuantity;
    }

    public void setCorrectionQuantity(Quantity correctionQuantity) {
        this.correctionQuantity = correctionQuantity;
    }

    public LocalDate getFromExpiryDate() {
        return fromExpiryDate;
    }

    public void setFromExpiryDate(LocalDate fromExpiryDate) {
        this.fromExpiryDate = fromExpiryDate;
    }

    public LocalDate getToExpiryDate() {
        return toExpiryDate;
    }

    public void setToExpiryDate(LocalDate toExpiryDate) {
        this.toExpiryDate = toExpiryDate;
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

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public Float getBatchQuantity() {
        return batchQuantity;
    }

    public void setBatchQuantity(Float batchQuantity) {
        this.batchQuantity = batchQuantity;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CorrectionDocumentLine that = (CorrectionDocumentLine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (fromBatchNumber != null ? !fromBatchNumber.equals(that.fromBatchNumber) : that.fromBatchNumber != null)
            return false;
        if (toBatchNumber != null ? !toBatchNumber.equals(that.toBatchNumber) : that.toBatchNumber != null)
            return false;
        if (fromMrp != null ? !fromMrp.equals(that.fromMrp) : that.fromMrp != null) return false;
        if (toMrp != null ? !toMrp.equals(that.toMrp) : that.toMrp != null) return false;
        if (fromSku != null ? !fromSku.equals(that.fromSku) : that.fromSku != null) return false;
        if (stockQuantity != null ? !stockQuantity.equals(that.stockQuantity) : that.stockQuantity != null)
            return false;
        if (correctionQuantity != null ? !correctionQuantity.equals(that.correctionQuantity) : that.correctionQuantity != null)
            return false;
        if (fromExpiryDate != null ? !fromExpiryDate.equals(that.fromExpiryDate) : that.fromExpiryDate != null)
            return false;
        if (toExpiryDate != null ? !toExpiryDate.equals(that.toExpiryDate) : that.toExpiryDate != null) return false;
        if (stockId != null ? !stockId.equals(that.stockId) : that.stockId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (consignment != null ? !consignment.equals(that.consignment) : that.consignment != null) return false;
        if (batchQuantity != null ? !batchQuantity.equals(that.batchQuantity) : that.batchQuantity != null)
            return false;
        return barCode != null ? barCode.equals(that.barCode) : that.barCode == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (fromBatchNumber != null ? fromBatchNumber.hashCode() : 0);
        result = 31 * result + (toBatchNumber != null ? toBatchNumber.hashCode() : 0);
        result = 31 * result + (fromMrp != null ? fromMrp.hashCode() : 0);
        result = 31 * result + (toMrp != null ? toMrp.hashCode() : 0);
        result = 31 * result + (fromSku != null ? fromSku.hashCode() : 0);
        result = 31 * result + (stockQuantity != null ? stockQuantity.hashCode() : 0);
        result = 31 * result + (correctionQuantity != null ? correctionQuantity.hashCode() : 0);
        result = 31 * result + (fromExpiryDate != null ? fromExpiryDate.hashCode() : 0);
        result = 31 * result + (toExpiryDate != null ? toExpiryDate.hashCode() : 0);
        result = 31 * result + (stockId != null ? stockId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        result = 31 * result + (consignment != null ? consignment.hashCode() : 0);
        result = 31 * result + (batchQuantity != null ? batchQuantity.hashCode() : 0);
        result = 31 * result + (barCode != null ? barCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CorrectionDocumentLine{" +
            "id=" + id +
            ", item=" + item +
            ", fromBatchNumber='" + fromBatchNumber + '\'' +
            ", toBatchNumber='" + toBatchNumber + '\'' +
            ", fromMrp=" + fromMrp +
            ", toMrp=" + toMrp +
            ", fromSku='" + fromSku + '\'' +
            ", stockQuantity=" + stockQuantity +
            ", correctionQuantity=" + correctionQuantity +
            ", fromExpiryDate=" + fromExpiryDate +
            ", toExpiryDate=" + toExpiryDate +
            ", stockId=" + stockId +
            ", owner='" + owner + '\'' +
            ", locator=" + locator +
            ", consignment=" + consignment +
            ", batchQuantity=" + batchQuantity +
            ", barCode='" + barCode + '\'' +
            '}';
    }
}
