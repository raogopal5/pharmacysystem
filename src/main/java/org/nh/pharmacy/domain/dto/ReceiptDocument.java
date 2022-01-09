package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Nitesh on 3/13/17.
 */
public class ReceiptDocument extends StockDocument {

    @Field(type = FieldType.Object)
    private List<ReceiptDocumentLine> lines;

    private UserDTO receivedBy;
    private LocalDateTime receiptDate;

    @Field(type = FieldType.Object)
    private OrganizationDTO issueUnit;

    @Field(type = FieldType.Object)
    private OrganizationDTO indentUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO issueStore;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO indentStore;

    private TransactionType sourceType;

    public List<ReceiptDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<ReceiptDocumentLine> lines) {
        this.lines = lines;
    }

    public UserDTO getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(UserDTO receivedBy) {
        this.receivedBy = receivedBy;
    }

    public LocalDateTime getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDateTime receiptDate) {
        this.receiptDate = receiptDate;
    }

    public OrganizationDTO getIssueUnit() {
        return issueUnit;
    }

    public void setIssueUnit(OrganizationDTO issueUnit) {
        this.issueUnit = issueUnit;
    }

    public OrganizationDTO getIndentUnit() {
        return indentUnit;
    }

    public void setIndentUnit(OrganizationDTO indentUnit) {
        this.indentUnit = indentUnit;
    }

    public HealthcareServiceCenterDTO getIssueStore() {
        return issueStore;
    }

    public void setIssueStore(HealthcareServiceCenterDTO issueStore) {
        this.issueStore = issueStore;
    }

    public HealthcareServiceCenterDTO getIndentStore() {
        return indentStore;
    }

    public void setIndentStore(HealthcareServiceCenterDTO indentStore) {
        this.indentStore = indentStore;
    }

    public TransactionType getSourceType() {
        return sourceType;
    }

    public void setSourceType(TransactionType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReceiptDocument that = (ReceiptDocument) o;

        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        return receivedBy != null ? receivedBy.equals(that.receivedBy) : that.receivedBy == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (receivedBy != null ? receivedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "ReceiptDocument{" +
            "lines=" + lines +
            ", receivedBy=" + receivedBy +
            ", receiptDate=" + receiptDate +
            ", issueUnit=" + issueUnit +
            ", indentUnit=" + indentUnit +
            ", issueStore=" + issueStore +
            ", indentStore=" + indentStore +
            ", sourceType=" + sourceType +
            '}';
    }
}
