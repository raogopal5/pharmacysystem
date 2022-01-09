package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Rohit on 4/6/17.
 */
public class CorrectionDocument implements Serializable {

    private Long id;
    private List<CorrectionDocumentLine> lines;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO store;

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
    private LocalDateTime modifiedDate;
    @Field(type = FieldType.Date)
    private LocalDateTime approvedDate;
    private UserDTO createdBy;

    @Field(type = FieldType.Object)
    private UserDTO approvedBy;

    private UserDTO modifiedBy;
    private String remarks;
    private TransactionType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CorrectionDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<CorrectionDocumentLine> lines) {
        this.lines = lines;
    }

    public HealthcareServiceCenterDTO getStore() {
        return store;
    }

    public void setStore(HealthcareServiceCenterDTO store) {
        this.store = store;
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

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserDTO getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UserDTO approvedBy) {
        this.approvedBy = approvedBy;
    }

    public UserDTO getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserDTO modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorrectionDocument)) return false;

        CorrectionDocument that = (CorrectionDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        if (store != null ? !store.equals(that.store) : that.store != null) return false;
        if (status != that.status) return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(that.modifiedDate) : that.modifiedDate != null) return false;
        if (approvedDate != null ? !approvedDate.equals(that.approvedDate) : that.approvedDate != null) return false;
        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (modifiedBy != null ? !modifiedBy.equals(that.modifiedBy) : that.modifiedBy != null) return false;
        if (remarks != null ? !remarks.equals(that.remarks) : that.remarks != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (store != null ? store.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (approvedDate != null ? approvedDate.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        result = 31 * result + (modifiedBy != null ? modifiedBy.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CorrectionDocument{" +
            "id=" + id +
            ", lines=" + lines +
            ", store=" + store +
            ", status=" + status +
            ", createdDate=" + createdDate +
            ", modifiedDate=" + modifiedDate +
            ", approvedDate=" + approvedDate +
            ", createdBy=" + createdBy +
            ", approvedBy=" + approvedBy +
            ", modifiedBy=" + modifiedBy +
            ", remarks='" + remarks + '\'' +
            ", type=" + type +
            '}';
    }
}
