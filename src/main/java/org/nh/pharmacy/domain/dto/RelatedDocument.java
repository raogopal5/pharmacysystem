package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.enumeration.Status;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Sanjit Vimal on 1/8/17.
 */
public class RelatedDocument implements Serializable, Comparable<RelatedDocument> {

    private String id;
    private String documentNumber;
    private LocalDateTime createdDate;
    private TransactionType documentType;
    private Status status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public TransactionType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelatedDocument that = (RelatedDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (documentNumber != null ? !documentNumber.equals(that.documentNumber) : that.documentNumber != null)
            return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (documentType != that.documentType) return false;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (documentNumber != null ? documentNumber.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RelatedDocument{" +
            "id='" + id + '\'' +
            ", documentNumber='" + documentNumber + '\'' +
            ", createdDate=" + createdDate +
            ", documentType=" + documentType +
            ", status=" + status +
            '}';
    }

    @Override
    public int compareTo(RelatedDocument o) {
        int cmp = o.getCreatedDate().toLocalDate().compareTo(this.getCreatedDate().toLocalDate());
        if (cmp == 0) {
            cmp = o.getCreatedDate().toLocalTime().compareTo(this.getCreatedDate().toLocalTime());
        }
        return cmp;

    }
}
