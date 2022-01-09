package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.PendingAuditRequestStatus;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class PendingAuditRequestDocument extends MedicationRequestDocument implements Serializable {

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private PendingAuditRequestStatus pendingAuditRequestStatus = PendingAuditRequestStatus.PENDING_AUDIT;

    private UserDTO auditBy;

    private LocalDateTime auditDate;

    private String remarks;

    private List<Remarks> remarksHistory;

    private boolean modified;

    private UserDTO claimedBy;


    public List<MedicationRequestDocumentLine> getDocumentLines() {
        return documentLines;
    }

    public void setDocumentLines(List<MedicationRequestDocumentLine> documentLines) {
        this.documentLines = documentLines;
    }


    public PendingAuditRequestStatus getPendingAuditRequestStatus() {
        return pendingAuditRequestStatus;
    }

    public void setPendingAuditRequestStatus(PendingAuditRequestStatus pendingAuditRequestStatus) {
        this.pendingAuditRequestStatus = pendingAuditRequestStatus;
    }

    public UserDTO getAuditBy() {
        return auditBy;
    }

    public void setAuditBy(UserDTO auditBy) {
        this.auditBy = auditBy;
    }

    public LocalDateTime getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDateTime auditDate) {
        this.auditDate = auditDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public UserDTO getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(UserDTO claimedBy) {
        this.claimedBy = claimedBy;
    }


    public List<Remarks> getRemarksHistory() {
        return remarksHistory;
    }

    public void setRemarksHistory(List<Remarks> remarksHistory) {
        this.remarksHistory = remarksHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingAuditRequestDocument)) return false;

        PendingAuditRequestDocument that = (PendingAuditRequestDocument) o;

        if (getDocumentLines() != null ? !getDocumentLines().equals(that.getDocumentLines()) : that.getDocumentLines() != null)
            return false;
        if (getConsultant() != null ? !getConsultant().equals(that.getConsultant()) : that.getConsultant() != null)
            return false;
        if (getUnit() != null ? !getUnit().equals(that.getUnit()) : that.getUnit() != null) return false;
        if (getRecorder() != null ? !getRecorder().equals(that.getRecorder()) : that.getRecorder() != null)
            return false;
        if (getEncounter() != null ? !getEncounter().equals(that.getEncounter()) : that.getEncounter() != null)
            return false;
        if (getPatient() != null ? !getPatient().equals(that.getPatient()) : that.getPatient() != null) return false;
        if (getCreatedDate() != null ? !getCreatedDate().equals(that.getCreatedDate()) : that.getCreatedDate() != null)
            return false;
        if (getCreatedBy() != null ? !getCreatedBy().equals(that.getCreatedBy()) : that.getCreatedBy() != null)
            return false;
        if (getModifiedBy() != null ? !getModifiedBy().equals(that.getModifiedBy()) : that.getModifiedBy() != null)
            return false;
        if (getModifiedDate() != null ? !getModifiedDate().equals(that.getModifiedDate()) : that.getModifiedDate() != null)
            return false;
        return getMedicationRequestStatus() == that.getMedicationRequestStatus();
    }

    @Override
    public int hashCode() {
        int result = getDocumentLines() != null ? getDocumentLines().hashCode() : 0;
        result = 31 * result + (getConsultant() != null ? getConsultant().hashCode() : 0);
        result = 31 * result + (getUnit() != null ? getUnit().hashCode() : 0);
        result = 31 * result + (getRecorder() != null ? getRecorder().hashCode() : 0);
        result = 31 * result + (getEncounter() != null ? getEncounter().hashCode() : 0);
        result = 31 * result + (getPatient() != null ? getPatient().hashCode() : 0);
        result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
        result = 31 * result + (getCreatedBy() != null ? getCreatedBy().hashCode() : 0);
        result = 31 * result + (getModifiedBy() != null ? getModifiedBy().hashCode() : 0);
        result = 31 * result + (getModifiedDate() != null ? getModifiedDate().hashCode() : 0);
        result = 31 * result + (getMedicationRequestStatus() != null ? getMedicationRequestStatus().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PendingAuditRequestDocument{" +
            "documentLines=" + documentLines +
            '}';
    }
}
