package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.Priority;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Nirbhay on 3/9/17.
 */
public class SourceDocument implements Serializable {

    private Long id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String documentNumber;

    private LocalDateTime documentDate;
    private Long lineId;
    private Quantity quantity;
    private Quantity pendingQuantity;
    private TransactionType type;
    private Priority priority;
    private UserDTO createdBy;
    private Long stockId;

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public LocalDateTime getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDateTime documentDate) {
        this.documentDate = documentDate;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Quantity getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(Quantity pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceDocument that = (SourceDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (documentNumber != null ? !documentNumber.equals(that.documentNumber) : that.documentNumber != null)
            return false;
        if (documentDate != null ? !documentDate.equals(that.documentDate) : that.documentDate != null) return false;
        if (lineId != null ? !lineId.equals(that.lineId) : that.lineId != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (pendingQuantity != null ? !pendingQuantity.equals(that.pendingQuantity) : that.pendingQuantity != null)
            return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (documentNumber != null ? documentNumber.hashCode() : 0);
        result = 31 * result + (documentDate != null ? documentDate.hashCode() : 0);
        result = 31 * result + (lineId != null ? lineId.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (pendingQuantity != null ? pendingQuantity.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SourceDocument{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + '\'' +
            ", documentDate=" + documentDate +
            ", lineId=" + lineId +
            ", quantity=" + quantity +
            ", pendingQuantity=" + pendingQuantity +
            ", type=" + type +
            ", priority=" + priority +
            ", createdBy=" + createdBy +
            '}';
    }
}
