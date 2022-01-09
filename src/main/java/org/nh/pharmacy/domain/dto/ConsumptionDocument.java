package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.common.dto.UserDTO;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * A ConsumptionDocument.
 */
public class ConsumptionDocument extends StockDocument {

    private UserDTO consumedBy;
    private LocalDateTime consumedDate;

    @Field(type = FieldType.Object)
    private List<ConsumptionDocumentLine> lines;

    @Field(type = FieldType.Object)
    private OrganizationDTO forDepartment;

    private UserDTO requestedBy;

    @Field(type = FieldType.Object)
    private PatientDTO forPatient;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO forHSC;

    private OrganizationDTO consumptionUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO consumptionStore;

    @Field(type = FieldType.Object)
    private List<SourceDocument> sourceDocument;

    public UserDTO getConsumedBy() {
        return consumedBy;
    }

    public void setConsumedBy(UserDTO consumedBy) {
        this.consumedBy = consumedBy;
    }

    public LocalDateTime getConsumedDate() {
        return consumedDate;
    }

    public void setConsumedDate(LocalDateTime consumedDate) {
        this.consumedDate = consumedDate;
    }

    public List<ConsumptionDocumentLine> getLines() {
        return lines;
    }

    public void setLines(List<ConsumptionDocumentLine> lines) {
        this.lines = lines;
    }

    public OrganizationDTO getForDepartment() {
        return forDepartment;
    }

    public void setForDepartment(OrganizationDTO forDepartment) {
        this.forDepartment = forDepartment;
    }

    public UserDTO getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UserDTO requestedBy) {
        this.requestedBy = requestedBy;
    }

    public PatientDTO getForPatient() {
        return forPatient;
    }

    public void setForPatient(PatientDTO forPatient) {
        this.forPatient = forPatient;
    }

    public HealthcareServiceCenterDTO getForHSC() {
        return forHSC;
    }

    public void setForHSC(HealthcareServiceCenterDTO forHSC) {
        this.forHSC = forHSC;
    }

    public OrganizationDTO getConsumptionUnit() {
        return consumptionUnit;
    }

    public void setConsumptionUnit(OrganizationDTO consumptionUnit) {
        this.consumptionUnit = consumptionUnit;
    }

    public HealthcareServiceCenterDTO getConsumptionStore() {
        return consumptionStore;
    }

    public void setConsumptionStore(HealthcareServiceCenterDTO consumptionStore) {
        this.consumptionStore = consumptionStore;
    }

    public List<SourceDocument> getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(List<SourceDocument> sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ConsumptionDocument that = (ConsumptionDocument) o;

        if (consumedBy != null ? !consumedBy.equals(that.consumedBy) : that.consumedBy != null) return false;
        if (consumedDate != null ? !consumedDate.equals(that.consumedDate) : that.consumedDate != null) return false;
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
        if (forDepartment != null ? !forDepartment.equals(that.forDepartment) : that.forDepartment != null)
            return false;
        if (requestedBy != null ? !requestedBy.equals(that.requestedBy) : that.requestedBy != null) return false;
        if (forPatient != null ? !forPatient.equals(that.forPatient) : that.forPatient != null) return false;
        if (forHSC != null ? !forHSC.equals(that.forHSC) : that.forHSC != null) return false;
        if (consumptionUnit != null ? !consumptionUnit.equals(that.consumptionUnit) : that.consumptionUnit != null)
            return false;
        if (consumptionStore != null ? consumptionStore.equals(that.consumptionStore) : that.consumptionStore == null)
         return false;
        return (sourceDocument != null ? !sourceDocument.equals(that.sourceDocument) : that.sourceDocument != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (consumedBy != null ? consumedBy.hashCode() : 0);
        result = 31 * result + (consumedDate != null ? consumedDate.hashCode() : 0);
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + (forDepartment != null ? forDepartment.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        result = 31 * result + (forPatient != null ? forPatient.hashCode() : 0);
        result = 31 * result + (forHSC != null ? forHSC.hashCode() : 0);
        result = 31 * result + (consumptionUnit != null ? consumptionUnit.hashCode() : 0);
        result = 31 * result + (consumptionStore != null ? consumptionStore.hashCode() : 0);
        result = 31 * result + (sourceDocument != null ? sourceDocument.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConsumptionDocument{" +
            "consumedBy=" + consumedBy +
            ", consumedDate=" + consumedDate +
            ", lines=" + lines +
            ", forDepartment=" + forDepartment +
            ", requestedBy=" + requestedBy +
            ", forPatient=" + forPatient +
            ", forHSC=" + forHSC +
            ", documentType=" + getDocumentType() +
            ", consumptionUnit=" + consumptionUnit +
            ", consumptionStore=" + consumptionStore +
            ", sourceDocument="+ sourceDocument +
            '}';
    }
}
