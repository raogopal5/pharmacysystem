package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Nitesh on 4/5/17.
 */
public class InventoryAdjustmentDocument implements Serializable{

    private Long id;
    private UserDTO createdBy;
    private LocalDateTime createdDate;
    private UserDTO modifiedBy;
    private LocalDateTime modifiedDate;

    @Field(type = FieldType.Object)
    private OrganizationDTO unit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO store;                      // Store at which adjustment happens

    private LocalDateTime documentDate;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String referenceDocumentNumber;                     // Audit document Number

    private LocalDateTime referenceDocumentDate;                // Audit document Date
    private LocalDateTime approvedDate;
    private UserDTO approvedBy;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private Status status;                                      // Pending for approval, Approved, Rejected

    private TransactionType documentType;                       // Adjustment Transaction

    @Field(type = FieldType.Object)
    private List<InventoryAdjustmentDocumentLine> lines;        // Item List

    private UserDTO storeContact;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UserDTO getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserDTO modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public OrganizationDTO getUnit() {
        return unit;
    }

    public void setUnit(OrganizationDTO unit) {
        this.unit = unit;
    }

    public HealthcareServiceCenterDTO getStore() {
        return store;
    }

    public void setStore(HealthcareServiceCenterDTO store) {
        this.store = store;
    }

    public LocalDateTime getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDateTime documentDate) {
        this.documentDate = documentDate;
    }

    public String getReferenceDocumentNumber() {
        return referenceDocumentNumber;
    }

    public void setReferenceDocumentNumber(String referenceDocumentNumber) {
        this.referenceDocumentNumber = referenceDocumentNumber;
    }

    public LocalDateTime getReferenceDocumentDate() {
        return referenceDocumentDate;
    }

    public void setReferenceDocumentDate(LocalDateTime referenceDocumentDate) {
        this.referenceDocumentDate = referenceDocumentDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public UserDTO getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UserDTO approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TransactionType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    public List<InventoryAdjustmentDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<InventoryAdjustmentDocumentLine> lines) {
        this.lines = lines;
    }

    public UserDTO getStoreContact() { return storeContact; }

    public void setStoreContact(UserDTO storeContact) { this.storeContact = storeContact; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryAdjustmentDocument that = (InventoryAdjustmentDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (modifiedBy != null ? !modifiedBy.equals(that.modifiedBy) : that.modifiedBy != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(that.modifiedDate) : that.modifiedDate != null) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;
        if (store != null ? !store.equals(that.store) : that.store != null) return false;
        if (documentDate != null ? !documentDate.equals(that.documentDate) : that.documentDate != null) return false;
        if (referenceDocumentNumber != null ? !referenceDocumentNumber.equals(that.referenceDocumentNumber) : that.referenceDocumentNumber != null)
            return false;
        if (referenceDocumentDate != null ? !referenceDocumentDate.equals(that.referenceDocumentDate) : that.referenceDocumentDate != null)
            return false;
        if (approvedDate != null ? !approvedDate.equals(that.approvedDate) : that.approvedDate != null) return false;
        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (status != that.status) return false;
        if (documentType != that.documentType) return false;
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        return storeContact != null ? storeContact.equals(that.storeContact) : that.storeContact == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (modifiedBy != null ? modifiedBy.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (store != null ? store.hashCode() : 0);
        result = 31 * result + (documentDate != null ? documentDate.hashCode() : 0);
        result = 31 * result + (referenceDocumentNumber != null ? referenceDocumentNumber.hashCode() : 0);
        result = 31 * result + (referenceDocumentDate != null ? referenceDocumentDate.hashCode() : 0);
        result = 31 * result + (approvedDate != null ? approvedDate.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (storeContact != null ? storeContact.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDocument{" +
            "id=" + id +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", modifiedBy=" + modifiedBy +
            ", modifiedDate=" + modifiedDate +
            ", unit=" + unit +
            ", store=" + store +
            ", documentDate=" + documentDate +
            ", referenceDocumentNumber='" + referenceDocumentNumber + '\'' +
            ", referenceDocumentDate=" + referenceDocumentDate +
            ", approvedDate=" + approvedDate +
            ", approvedBy=" + approvedBy +
            ", status=" + status +
            ", documentType=" + documentType +
            ", lines=" + lines +
            ", storeContact=" + storeContact +
            '}';
    }
}
