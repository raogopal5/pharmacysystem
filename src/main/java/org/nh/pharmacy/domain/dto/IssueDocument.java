package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.enumeration.DeliveryMode;
import org.nh.pharmacy.domain.enumeration.Priority;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Nitesh on 3/14/17.
 */
public class IssueDocument extends StockDocument {

    private UserDTO issuedBy;
    @Field(type = FieldType.Date)
    private LocalDateTime issueDate;
    private DeliveryMode mode;

    @Field(type = FieldType.Object)
    List<IssueDocumentLine> lines;

    @Field(type = FieldType.Object)
    private OrganizationDTO issueUnit;

    @Field(type = FieldType.Object)
    private OrganizationDTO indentUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO issueStore;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO indentStore;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private Priority priority;
    private Boolean conversionCompleted = false;

    public List<IssueDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<IssueDocumentLine> lines) {
        this.lines = lines;
    }

    public UserDTO getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(UserDTO issuedBy) {
        this.issuedBy = issuedBy;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public DeliveryMode getMode() {
        return mode;
    }

    public void setMode(DeliveryMode mode) {
        this.mode = mode;
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Boolean getConversionCompleted() {
        return conversionCompleted;
    }

    public void setConversionCompleted(Boolean conversionCompleted) {
        this.conversionCompleted = conversionCompleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IssueDocument that = (IssueDocument) o;

        if (issuedBy != null ? !issuedBy.equals(that.issuedBy) : that.issuedBy != null) return false;
        if (issueDate != null ? !issueDate.equals(that.issueDate) : that.issueDate != null) return false;
        if (mode != that.mode) return false;
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        if (issueUnit != null ? !issueUnit.equals(that.issueUnit) : that.issueUnit != null) return false;
        if (indentUnit != null ? !indentUnit.equals(that.indentUnit) : that.indentUnit != null) return false;
        if (issueStore != null ? !issueStore.equals(that.issueStore) : that.issueStore != null) return false;
        if (indentStore != null ? !indentStore.equals(that.indentStore) : that.indentStore != null) return false;
        if (priority != that.priority) return false;
        return conversionCompleted != null ? conversionCompleted.equals(that.conversionCompleted) : that.conversionCompleted == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issuedBy != null ? issuedBy.hashCode() : 0);
        result = 31 * result + (issueDate != null ? issueDate.hashCode() : 0);
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (issueUnit != null ? issueUnit.hashCode() : 0);
        result = 31 * result + (indentUnit != null ? indentUnit.hashCode() : 0);
        result = 31 * result + (issueStore != null ? issueStore.hashCode() : 0);
        result = 31 * result + (indentStore != null ? indentStore.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (conversionCompleted != null ? conversionCompleted.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "IssueDocument{" +
            "issuedBy=" + issuedBy +
            ", issueDate=" + issueDate +
            ", mode=" + mode +
            ", lines=" + lines +
            ", issueUnit=" + issueUnit +
            ", indentUnit=" + indentUnit +
            ", issueStore=" + issueStore +
            ", indentStore=" + indentStore +
            ", priority=" + priority +
            ", conversionCompleted=" + conversionCompleted +
            '}';
    }
}
