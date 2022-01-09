package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.Medication;
import org.nh.common.dto.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MedicationOrderDocumentLine implements Serializable {

    private Long documentLineId;

    private String status;

    private String intent;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String priority;

    private String category;

    private Date authoredOn;

    @Field(type = FieldType.Object)
    private Medication medication;

    private List<DosageInstruction> dosageInstruction;

    private Subtitution substitution;

    private Long duration;

    private String instructions;

    private LocalDateTime startDate;

    private SourceDTO sourceDocument;

    private DispenseRequest dispenseRequest;

    private LocatorDTO locator;

    private UserDTO cancelledBy;

    private ValueSetCodeDTO cancellationReason;

    private LocalDateTime cancelledDate;

    private HealthcareServiceCenterDTO cancellingHsc;

    private BigDecimal unitPrice =BigDecimal.ZERO;
    private BigDecimal totalPrice= BigDecimal.ZERO;
    private BigDecimal provisionalAmountCoverage= BigDecimal.ZERO;

    private Integer noOfRepeatAllowed=0;

    private String durationUnit;

    public Integer getNoOfRepeatAllowed() {
        return noOfRepeatAllowed;
    }

    public void setNoOfRepeatAllowed(Integer noOfRepeatAllowed) {
        this.noOfRepeatAllowed = noOfRepeatAllowed;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public DispenseRequest getDispenseRequest() {
        return dispenseRequest;
    }

    public void setDispenseRequest(DispenseRequest dispenseRequest) {
        this.dispenseRequest = dispenseRequest;
    }

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public Long getDocumentLineId() {
        return documentLineId;
    }

    public void setDocumentLineId(Long documentLineId) {
        this.documentLineId = documentLineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getAuthoredOn() {
        return authoredOn;
    }

    public void setAuthoredOn(Date authoredOn) {
        this.authoredOn = authoredOn;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public List<DosageInstruction> getDosageInstruction() {
        return dosageInstruction;
    }

    public void setDosageInstruction(List<DosageInstruction> dosageInstruction) {
        this.dosageInstruction = dosageInstruction;
    }

    public Subtitution getSubstitution() {
        return substitution;
    }

    public void setSubstitution(Subtitution substitution) {
        this.substitution = substitution;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public SourceDTO getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(SourceDTO sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public UserDTO getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(UserDTO cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public ValueSetCodeDTO getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(ValueSetCodeDTO cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(LocalDateTime cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public HealthcareServiceCenterDTO getCancellingHsc() {
        return cancellingHsc;
    }

    public void setCancellingHsc(HealthcareServiceCenterDTO cancellingHsc) {
        this.cancellingHsc = cancellingHsc;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getProvisionalAmountCoverage() {
        return provisionalAmountCoverage;
    }

    public void setProvisionalAmountCoverage(BigDecimal provisionalAmountCoverage) {
        this.provisionalAmountCoverage = provisionalAmountCoverage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicationOrderDocumentLine)) return false;
        MedicationOrderDocumentLine that = (MedicationOrderDocumentLine) o;
        return Objects.equals(documentLineId, that.documentLineId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(intent, that.intent) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(category, that.category) &&
                Objects.equals(authoredOn, that.authoredOn) &&
                Objects.equals(medication, that.medication) &&
                Objects.equals(dosageInstruction, that.dosageInstruction) &&
                Objects.equals(substitution, that.substitution) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(instructions, that.instructions) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(sourceDocument, that.sourceDocument) &&
                Objects.equals(dispenseRequest, that.dispenseRequest) &&
                Objects.equals(locator, that.locator) &&
                Objects.equals(cancelledBy, that.cancelledBy) &&
                Objects.equals(cancellationReason, that.cancellationReason) &&
                Objects.equals(cancelledDate, that.cancelledDate) &&
                Objects.equals(cancellingHsc, that.cancellingHsc);
    }

    @Override
    public int hashCode() {

        return Objects.hash(documentLineId, status, intent, priority, category, authoredOn, medication, dosageInstruction, substitution, duration, instructions, startDate, sourceDocument, dispenseRequest, locator, cancelledBy, cancellationReason, cancelledDate, cancellingHsc);
    }

    @Override
    public String toString() {
        return "MedicationOrderDocumentLine{" +
                "documentLineId=" + documentLineId +
                ", status='" + status + '\'' +
                ", intent='" + intent + '\'' +
                ", priority='" + priority + '\'' +
                ", category='" + category + '\'' +
                ", authoredOn=" + authoredOn +
                ", medication=" + medication +
                ", dosageInstruction=" + dosageInstruction +
                ", substitution=" + substitution +
                '}';
    }
}
