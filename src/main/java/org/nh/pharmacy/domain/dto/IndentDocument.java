package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.Priority;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Nirbhay on 3/10/17.
 */
public class IndentDocument extends StockDocument implements Serializable {

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private Priority priority;

    @Field(type = FieldType.Object)
    private List<IndentDocumentLine> lines;

    private UserDTO indenterName;
    @Field(type = FieldType.Date)
    private LocalDateTime indentDate;
    private LocalDateTime indentValidDate;

    @Field(type = FieldType.Object)
    private OrganizationDTO issueUnit;

    @Field(type = FieldType.Object)
    private OrganizationDTO indentUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO issueStore;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO indentStore;

    private Boolean conversionCompleted = false;

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public UserDTO getIndenterName() {
        return indenterName;
    }

    public void setIndenterName(UserDTO indenterName) {
        this.indenterName = indenterName;
    }

    public LocalDateTime getIndentDate() {
        return indentDate;
    }

    public void setIndentDate(LocalDateTime indentDate) {
        this.indentDate = indentDate;
    }

    public List<IndentDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<IndentDocumentLine> lines) {
        this.lines = lines;
    }

    public LocalDateTime getIndentValidDate() {
        return indentValidDate;
    }

    public void setIndentValidDate(LocalDateTime indentValidDate) {
        this.indentValidDate = indentValidDate;
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

    public Boolean getConversionCompleted() {
        return conversionCompleted;
    }

    public void setConversionCompleted(Boolean conversionCompleted) {
        this.conversionCompleted = conversionCompleted;
    }

    /**
     * Generate IndentDocumentLine id
     *
     * @return
     */
    public IndentDocumentLine assignId(IndentDocumentLine stockIndentDocumentLine) {
        Long lastLineId = 0l;
        if (lines == null && lines.isEmpty()) {
            stockIndentDocumentLine.setId(++lastLineId);
            return stockIndentDocumentLine;
        }
        for (StockDocumentLine line : lines) {
            if (lastLineId < line.getId()) {
                lastLineId = line.getId();
            }
        }
        stockIndentDocumentLine.setId(++lastLineId);
        return stockIndentDocumentLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IndentDocument that = (IndentDocument) o;

        if (priority != that.priority) return false;
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        if (indenterName != null ? !indenterName.equals(that.indenterName) : that.indenterName != null) return false;
        if (indentDate != null ? !indentDate.equals(that.indentDate) : that.indentDate != null) return false;
        if (indentValidDate != null ? !indentValidDate.equals(that.indentValidDate) : that.indentValidDate != null)
            return false;
        if (issueUnit != null ? !issueUnit.equals(that.issueUnit) : that.issueUnit != null) return false;
        if (indentUnit != null ? !indentUnit.equals(that.indentUnit) : that.indentUnit != null) return false;
        if (issueStore != null ? !issueStore.equals(that.issueStore) : that.issueStore != null) return false;
        if (indentStore != null ? !indentStore.equals(that.indentStore) : that.indentStore != null) return false;
        return conversionCompleted != null ? conversionCompleted.equals(that.conversionCompleted) : that.conversionCompleted == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (indenterName != null ? indenterName.hashCode() : 0);
        result = 31 * result + (indentDate != null ? indentDate.hashCode() : 0);
        result = 31 * result + (indentValidDate != null ? indentValidDate.hashCode() : 0);
        result = 31 * result + (issueUnit != null ? issueUnit.hashCode() : 0);
        result = 31 * result + (indentUnit != null ? indentUnit.hashCode() : 0);
        result = 31 * result + (issueStore != null ? issueStore.hashCode() : 0);
        result = 31 * result + (indentStore != null ? indentStore.hashCode() : 0);
        result = 31 * result + (conversionCompleted != null ? conversionCompleted.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "IndentDocument{" +
            "priority=" + priority +
            ", lines=" + lines +
            ", indenterName=" + indenterName +
            ", indentDate=" + indentDate +
            ", indentValidDate=" + indentValidDate +
            ", issueUnit=" + issueUnit +
            ", indentUnit=" + indentUnit +
            ", issueStore=" + issueStore +
            ", indentStore=" + indentStore +
            ", conversionCompleted=" + conversionCompleted +
            '}';
    }
}
