package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.LocatorDTO;
import org.nh.pharmacy.domain.Medication;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Nirbhay on 3/7/17.
 */
public abstract class StockDocumentLine implements Serializable {

    private Long id;
    private ItemDTO item;
    private Medication medication;
    private boolean generic = false;
    private String remarks;
    private String batchNumber;
    private LocatorDTO locator;
    private Long stockId;
    private String owner;
    private BigDecimal cost;
    private String sku;
    private Boolean consignment = false;

    @Field(type = FieldType.Object)
    private List<SourceDocument> sourceDocument;

    private LocalDate expiryDate;
    private String barCode;
    private String supplier;
    private Boolean allowAlternate = true;
    private BigDecimal mrp;

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
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

    public boolean isGeneric() {
        return generic;
    }

    public boolean getGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
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

    public List<SourceDocument> getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(List<SourceDocument> sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Boolean getAllowAlternate() {
        return allowAlternate;
    }

    public void setAllowAlternate(Boolean allowAlternate) {
        this.allowAlternate = allowAlternate;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockDocumentLine that = (StockDocumentLine) o;

        if (generic != that.generic) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (medication != null ? !medication.equals(that.medication) : that.medication != null) return false;
        if (remarks != null ? !remarks.equals(that.remarks) : that.remarks != null) return false;
        if (batchNumber != null ? !batchNumber.equals(that.batchNumber) : that.batchNumber != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (stockId != null ? !stockId.equals(that.stockId) : that.stockId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (cost != null ? !cost.equals(that.cost) : that.cost != null) return false;
        if (sku != null ? !sku.equals(that.sku) : that.sku != null) return false;
        if (consignment != null ? !consignment.equals(that.consignment) : that.consignment != null) return false;
        if (sourceDocument != null ? !sourceDocument.equals(that.sourceDocument) : that.sourceDocument != null)
            return false;
        if (mrp != null ? !mrp.equals(that.mrp) : that.mrp != null) return false;
        return expiryDate != null ? expiryDate.equals(that.expiryDate) : that.expiryDate == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (medication != null ? medication.hashCode() : 0);
        result = 31 * result + (generic ? 1 : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        result = 31 * result + (stockId != null ? stockId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        result = 31 * result + (sku != null ? sku.hashCode() : 0);
        result = 31 * result + (consignment != null ? consignment.hashCode() : 0);
        result = 31 * result + (sourceDocument != null ? sourceDocument.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (mrp != null ? mrp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockDocumentLine{" +
            "id=" + id +
            ", item=" + item +
            ", medication=" + medication +
            ", generic=" + generic +
            ", remarks='" + remarks + '\'' +
            ", batchNumber='" + batchNumber + '\'' +
            ", locator=" + locator +
            ", stockId=" + stockId +
            ", owner='" + owner + '\'' +
            ", cost=" + cost +
            ", sku='" + sku + '\'' +
            ", consignment=" + consignment +
            ", sourceDocument=" + sourceDocument +
            ", expiryDate=" + expiryDate +
            ", allowAlternate=" + allowAlternate +
            ", mrp=" + mrp +
            '}';
    }
}
