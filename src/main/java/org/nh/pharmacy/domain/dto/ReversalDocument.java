package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Indrajeet on 3/10/17.
 */
public class ReversalDocument extends StockDocument implements Serializable {

    @Field(type = FieldType.Object)
    private List<ReversalDocumentLine> lines;

    private UserDTO reversalMadeBy;
    private LocalDateTime reversalDate;

    @Field(type = FieldType.Object)
    private OrganizationDTO issueUnit;

    @Field(type = FieldType.Object)
    private OrganizationDTO indentUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO issueStore;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO indentStore;

    private TransactionType sourceType;
    private Boolean conversionCompleted = false;

    public List<ReversalDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<ReversalDocumentLine> lines) {
        this.lines = lines;
    }

    public UserDTO getReversalMadeBy() {
        return reversalMadeBy;
    }

    public void setReversalMadeBy(UserDTO reversalMadeBy) {
        this.reversalMadeBy = reversalMadeBy;
    }

    public LocalDateTime getReversalDate() {
        return reversalDate;
    }

    public void setReversalDate(LocalDateTime reversalDate) {
        this.reversalDate = reversalDate;
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

    public Boolean getConversionCompleted() {
        return conversionCompleted;
    }

    public void setConversionCompleted(boolean conversionCompleted) {
        this.conversionCompleted = conversionCompleted;
    }

    /**
     * Generate IndentDocumentLine id
     *
     * @return
     */
    public ReversalDocumentLine assignId(ReversalDocumentLine stockReversalDocumentLine) {
        Long lastLineId = 0l;
        if (lines == null && lines.isEmpty()) {
            stockReversalDocumentLine.setId(++lastLineId);
            return stockReversalDocumentLine;
        }
        for (StockDocumentLine line : lines) {
            if (lastLineId < line.getId()) {
                lastLineId = line.getId();
            }
        }
        stockReversalDocumentLine.setId(++lastLineId);
        return stockReversalDocumentLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReversalDocument that = (ReversalDocument) o;

        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        if (reversalMadeBy != null ? !reversalMadeBy.equals(that.reversalMadeBy) : that.reversalMadeBy != null)
            return false;
        if (reversalDate != null ? !reversalDate.equals(that.reversalDate) : that.reversalDate != null) return false;
        if (issueUnit != null ? !issueUnit.equals(that.issueUnit) : that.issueUnit != null) return false;
        if (indentUnit != null ? !indentUnit.equals(that.indentUnit) : that.indentUnit != null) return false;
        if (issueStore != null ? !issueStore.equals(that.issueStore) : that.issueStore != null) return false;
        if (indentStore != null ? !indentStore.equals(that.indentStore) : that.indentStore != null) return false;
        if (sourceType != that.sourceType) return false;
        return conversionCompleted != null ? conversionCompleted.equals(that.conversionCompleted) : that.conversionCompleted == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (reversalMadeBy != null ? reversalMadeBy.hashCode() : 0);
        result = 31 * result + (reversalDate != null ? reversalDate.hashCode() : 0);
        result = 31 * result + (issueUnit != null ? issueUnit.hashCode() : 0);
        result = 31 * result + (indentUnit != null ? indentUnit.hashCode() : 0);
        result = 31 * result + (issueStore != null ? issueStore.hashCode() : 0);
        result = 31 * result + (indentStore != null ? indentStore.hashCode() : 0);
        result = 31 * result + (sourceType != null ? sourceType.hashCode() : 0);
        result = 31 * result + (conversionCompleted != null ? conversionCompleted.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "ReversalDocument{" +
            "lines=" + lines +
            ", reversalMadeBy=" + reversalMadeBy +
            ", reversalDate=" + reversalDate +
            ", issueUnit=" + issueUnit +
            ", indentUnit=" + indentUnit +
            ", issueStore=" + issueStore +
            ", indentStore=" + indentStore +
            ", sourceType=" + sourceType +
            ", conversionCompleted=" + conversionCompleted +
            '}';
    }

}
