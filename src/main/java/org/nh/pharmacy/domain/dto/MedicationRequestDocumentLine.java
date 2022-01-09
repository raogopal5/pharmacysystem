package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.Medication;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.dto.ValueSetCodeDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by Indrajeet 11/30/2017
 */
public class MedicationRequestDocumentLine implements Serializable {

    private Long documentLineId;

    private String status;

    private String intent;

    private String priority;

    private String category;

    private Date authoredOn;

    private Medication medication;

    private List<DosageInstruction> dosageInstruction;

    private Subtitution subtitution;

    private DispenseRequest dispenseRequest;

    private HealthcareServiceCenterDTO renderingHSC;

    private HealthcareServiceCenterDTO orderingHSC;

    private String remarks;

    private String instructions;

    private UserDTO cancelledBy;

    private ValueSetCodeDTO cancellationReason;

    private LocalDateTime cancelledDate;

    private HealthcareServiceCenterDTO cancellingHsc;

    private String medicationOrderNumber;

    private Long duration;

    private LocalDateTime startDate;

    private BigDecimal unitPrice =BigDecimal.ZERO;
    private BigDecimal totalPrice= BigDecimal.ZERO;
    private BigDecimal provisionalAmountCoverage= BigDecimal.ZERO;

    private Long pendingAuditDocLineId;

    private Long noOfRepeat;

    private boolean modified;

    private boolean drugItem;


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

    public Long getNoOfRepeat() {
        return noOfRepeat;
    }

    public void setNoOfRepeat(Long noOfRepeat) {
        this.noOfRepeat = noOfRepeat;
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

    public Subtitution getSubtitution() {
        return subtitution;
    }

    public void setSubtitution(Subtitution subtitution) {
        this.subtitution = subtitution;
    }

    public DispenseRequest getDispenseRequest() {
        return dispenseRequest;
    }

    public void setDispenseRequest(DispenseRequest dispenseRequest) {
        this.dispenseRequest = dispenseRequest;
    }

    public HealthcareServiceCenterDTO getRenderingHSC() {
        return renderingHSC;
    }

    public void setRenderingHSC(HealthcareServiceCenterDTO renderingHSC) {
        this.renderingHSC = renderingHSC;
    }

    public HealthcareServiceCenterDTO getOrderingHSC() {
        return orderingHSC;
    }

    public boolean isDrugItem() {
        return drugItem;
    }

    public void setDrugItem(boolean drugItem) {
        this.drugItem = drugItem;
    }

    public void setOrderingHSC(HealthcareServiceCenterDTO orderingHSC) {
        this.orderingHSC = orderingHSC;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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

    public String getMedicationOrderNumber() {
        return medicationOrderNumber;
    }

    public void setMedicationOrderNumber(String medicationOrderNumber) {
        this.medicationOrderNumber = medicationOrderNumber;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
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

    public Long getPendingAuditDocLineId() {
        return pendingAuditDocLineId;
    }

    public void setPendingAuditDocLineId(Long pendingAuditDocLineId) {
        this.pendingAuditDocLineId = pendingAuditDocLineId;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedicationRequestDocumentLine that = (MedicationRequestDocumentLine) o;

        if (documentLineId != null ? !documentLineId.equals(that.documentLineId) : that.documentLineId != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (intent != null ? !intent.equals(that.intent) : that.intent != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (category != null ? !category.equals(that.category) : that.category != null) return false;
        if (authoredOn != null ? !authoredOn.equals(that.authoredOn) : that.authoredOn != null) return false;
        if (medication != null ? !medication.equals(that.medication) : that.medication != null) return false;
        if (dosageInstruction != null ? !dosageInstruction.equals(that.dosageInstruction) : that.dosageInstruction != null)
            return false;
        if (subtitution != null ? !subtitution.equals(that.subtitution) : that.subtitution != null) return false;
        return dispenseRequest != null ? dispenseRequest.equals(that.dispenseRequest) : that.dispenseRequest == null;
    }

    @Override
    public int hashCode() {
        int result = documentLineId != null ? documentLineId.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (intent != null ? intent.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (authoredOn != null ? authoredOn.hashCode() : 0);
        result = 31 * result + (medication != null ? medication.hashCode() : 0);
        result = 31 * result + (dosageInstruction != null ? dosageInstruction.hashCode() : 0);
        result = 31 * result + (subtitution != null ? subtitution.hashCode() : 0);
        result = 31 * result + (dispenseRequest != null ? dispenseRequest.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MedicationRequestDocumentLine{" +
            "documentLineId=" + documentLineId +
            ", status='" + status + '\'' +
            ", intent='" + intent + '\'' +
            ", priority='" + priority + '\'' +
            ", category='" + category + '\'' +
            ", authoredOn=" + authoredOn +
            ", medication=" + medication +
            ", dosageInstruction=" + dosageInstruction +
            ", subtitution=" + subtitution +
            ", dispenseRequest=" + dispenseRequest +
            '}';
    }
}

