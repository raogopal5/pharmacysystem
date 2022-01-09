package org.nh.pharmacy.domain.dto;


import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Nirbhay on 3/7/17.
 */
public abstract class StockDocument implements Serializable {

    private String id;

    private String documentNumber;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private Status status;
    @Field(type = FieldType.Date)
    private LocalDateTime createdDate;
    private UserDTO createdBy;
    private LocalDateTime modifiedDate;
    @Field(type = FieldType.Date)
    private LocalDateTime approvedDate;
    private UserDTO approvedBy;
    private String remarks;
    private TransactionType documentType;
    @Field(name = "draft",type = FieldType.Boolean)
    private boolean isDraft = true;
    private UserDTO modifiedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
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

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public TransactionType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public boolean getDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public UserDTO getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserDTO modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockDocument that = (StockDocument) o;

        if (isDraft != that.isDraft) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (documentNumber != null ? !documentNumber.equals(that.documentNumber) : that.documentNumber != null)
            return false;
        if (status != that.status) return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(that.modifiedDate) : that.modifiedDate != null) return false;
        if (approvedDate != null ? !approvedDate.equals(that.approvedDate) : that.approvedDate != null) return false;
        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
        if (remarks != null ? !remarks.equals(that.remarks) : that.remarks != null) return false;
        return documentType == that.documentType;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (documentNumber != null ? documentNumber.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (approvedDate != null ? approvedDate.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
        result = 31 * result + (isDraft ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockDocument{" +
            "id='" + id + '\'' +
            ", documentNumber='" + documentNumber + '\'' +
            ", status=" + status +
            ", createdDate=" + createdDate +
            ", modifiedDate=" + modifiedDate +
            ", approvedDate=" + approvedDate +
            ", approvedBy=" + approvedBy +
            ", createdBy=" + createdBy +
            ", remarks='" + remarks + '\'' +
            ", documentType=" + documentType +
            ", isDraft=" + isDraft +
            ", modifiedBy=" + modifiedBy +
            '}';
    }
}
