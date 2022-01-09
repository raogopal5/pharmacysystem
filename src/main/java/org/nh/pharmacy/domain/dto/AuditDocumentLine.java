package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.LocatorDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.ValueSetCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A AuditDocumentLine.
 */
public class AuditDocumentLine implements Serializable {

    private Long id;
    private ItemDTO item;
    private String batchNumber;
    private LocatorDTO locator;
    private Long stockId;
    private String owner;
    private BigDecimal cost;
    private String sku;
    private Boolean consignment = false;
    private SourceDocument sourceDocument;
    private LocalDate expiryDate;
    private Quantity stockQuantity;
    private Quantity auditQuantity;
    private Quantity discrepantQuantity;
    private BigDecimal discrepantValue;
    private Boolean hasDiscrepancy;
    private String remarks;
    private UserDTO auditingUser;
    private ValueSetCode discrepantReason;
    private BigDecimal mrp;
    private String barcode;

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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public SourceDocument getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(SourceDocument sourceDocument) {
        this.sourceDocument = sourceDocument;
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

    public Quantity getAuditQuantity() {
        return auditQuantity;
    }

    public void setAuditQuantity(Quantity auditQuantity) {
        this.auditQuantity = auditQuantity;
    }

    public Quantity getDiscrepantQuantity() {
        return discrepantQuantity;
    }

    public void setDiscrepantQuantity(Quantity discrepantQuantity) {
        this.discrepantQuantity = discrepantQuantity;
    }

    public BigDecimal getDiscrepantValue() {
        return discrepantValue;
    }

    public void setDiscrepantValue(BigDecimal discrepantValue) {
        this.discrepantValue = discrepantValue;
    }

    public Boolean getHasDiscrepancy() {
        return hasDiscrepancy;
    }

    public void setHasDiscrepancy(Boolean hasDiscrepancy) {
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public UserDTO getAuditingUser() {
        return auditingUser;
    }

    public void setAuditingUser(UserDTO auditingUser) {
        this.auditingUser = auditingUser;
    }

    public ValueSetCode getDiscrepantReason() {
        return discrepantReason;
    }

    public void setDiscrepantReason(ValueSetCode discrepantReason) {
        this.discrepantReason = discrepantReason;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditDocumentLine that = (AuditDocumentLine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (batchNumber != null ? !batchNumber.equals(that.batchNumber) : that.batchNumber != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (stockId != null ? !stockId.equals(that.stockId) : that.stockId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (cost != null ? !cost.equals(that.cost) : that.cost != null) return false;
        if (sku != null ? !sku.equals(that.sku) : that.sku != null) return false;
        if (consignment != null ? !consignment.equals(that.consignment) : that.consignment != null) return false;
        if (sourceDocument != null ? !sourceDocument.equals(that.sourceDocument) : that.sourceDocument != null)
            return false;
        if (expiryDate != null ? !expiryDate.equals(that.expiryDate) : that.expiryDate != null) return false;
        if (stockQuantity != null ? !stockQuantity.equals(that.stockQuantity) : that.stockQuantity != null)
            return false;
        if (auditQuantity != null ? !auditQuantity.equals(that.auditQuantity) : that.auditQuantity != null)
            return false;
        if (discrepantQuantity != null ? !discrepantQuantity.equals(that.discrepantQuantity) : that.discrepantQuantity != null)
            return false;
        if (discrepantValue != null ? !discrepantValue.equals(that.discrepantValue) : that.discrepantValue != null)
            return false;
        if (hasDiscrepancy != null ? !hasDiscrepancy.equals(that.hasDiscrepancy) : that.hasDiscrepancy != null)
            return false;
        if (remarks != null ? !remarks.equals(that.remarks) : that.remarks != null) return false;
        if (auditingUser != null ? !auditingUser.equals(that.auditingUser) : that.auditingUser != null) return false;
        if (discrepantReason != null ? !discrepantReason.equals(that.discrepantReason) : that.discrepantReason != null)
            return false;
        if (mrp != null ? !mrp.equals(that.mrp) : that.mrp != null) return false;
        return barcode != null ? barcode.equals(that.barcode) : that.barcode == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        result = 31 * result + (stockId != null ? stockId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        result = 31 * result + (sku != null ? sku.hashCode() : 0);
        result = 31 * result + (consignment != null ? consignment.hashCode() : 0);
        result = 31 * result + (sourceDocument != null ? sourceDocument.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (stockQuantity != null ? stockQuantity.hashCode() : 0);
        result = 31 * result + (auditQuantity != null ? auditQuantity.hashCode() : 0);
        result = 31 * result + (discrepantQuantity != null ? discrepantQuantity.hashCode() : 0);
        result = 31 * result + (discrepantValue != null ? discrepantValue.hashCode() : 0);
        result = 31 * result + (hasDiscrepancy != null ? hasDiscrepancy.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (auditingUser != null ? auditingUser.hashCode() : 0);
        result = 31 * result + (discrepantReason != null ? discrepantReason.hashCode() : 0);
        result = 31 * result + (mrp != null ? mrp.hashCode() : 0);
        result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuditDocumentLine{" +
            "id=" + id +
            ", item=" + item +
            ", batchNumber='" + batchNumber + '\'' +
            ", locator=" + locator +
            ", stockId=" + stockId +
            ", owner='" + owner + '\'' +
            ", cost=" + cost +
            ", sku='" + sku + '\'' +
            ", consignment=" + consignment +
            ", sourceDocument=" + sourceDocument +
            ", expiryDate=" + expiryDate +
            ", stockQuantity=" + stockQuantity +
            ", auditQuantity=" + auditQuantity +
            ", discrepantQuantity=" + discrepantQuantity +
            ", discrepantValue=" + discrepantValue +
            ", hasDiscrepancy=" + hasDiscrepancy +
            ", remarks='" + remarks + '\'' +
            ", auditingUser=" + auditingUser +
            ", discrepantReason=" + discrepantReason +
            ", mrp=" + mrp +
            ", barcode='" + barcode + '\'' +
            '}';
    }
}
