package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.*;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.enumeration.AdmissionStatus;
import org.nh.pharmacy.domain.enumeration.IPReturnRequestStatus;
import org.nh.pharmacy.domain.enumeration.IPReturnType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class IPDispenseReturnRequestDocument implements Serializable {

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO sourceHSC;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO returnTOHSC;


    @Field(type = FieldType.Object)
    private UserDTO requestedBy;
    private LocalDateTime requestedDate;

    @Field(type = FieldType.Object)
    private UserDTO rejectedBy;
    private LocalDateTime rejectedDate;

    @Field(type = FieldType.Object)
    private UserDTO createdBy;
    private LocalDateTime createdDate;

    @Field(type = FieldType.Object)
    private UserDTO modifiedBy;
    private LocalDateTime modifiedDate;


    @Field(type = FieldType.Object)
    private EncounterDTO encounter;

    @Field(type = FieldType.Object)
    private PatientDTO patient;

    @Field(type = FieldType.Object)
    private ConsultantDTO consultant;

    private TransactionType documentType = TransactionType.Dispense_Return;

    private LocalDateTime returnDate;
    private OrganizationDTO returnTOUnit;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO patientLocation;

    private OrganizationDTO unit;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private IPReturnRequestStatus returnStatus;

    private UserDTO receivedBy;
    private ValueSetCodeDTO returnReason;
    private BigDecimal calculatedGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedPatientGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedSponsorGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal roundOff = BigDecimalUtil.ZERO;

    @Field(type = FieldType.Object)
    private List<IPDispenseReturnDocumentLine> dispenseDocumentLines;

    @Field(type = FieldType.Object)
    private List<IPDispenseReturnDocumentLine> dispenseReturnDocumentLines;

    @Field(type = FieldType.Text)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private AdmissionStatus patientStatus;

    private String remarks;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String bedNumber;

    private IPReturnType ipReturnType=IPReturnType.RETURN_REQUEST;

    private ValueSetCodeDTO rejectReason;

    public HealthcareServiceCenterDTO getSourceHSC() {
        return sourceHSC;
    }

    public void setSourceHSC(HealthcareServiceCenterDTO sourceHSC) {
        this.sourceHSC = sourceHSC;
    }

    public HealthcareServiceCenterDTO getReturnTOHSC() {
        return returnTOHSC;
    }

    public void setReturnTOHSC(HealthcareServiceCenterDTO returnTOHSC) {
        this.returnTOHSC = returnTOHSC;
    }

    public UserDTO getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UserDTO requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public UserDTO getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(UserDTO rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public LocalDateTime getRejectedDate() {
        return rejectedDate;
    }

    public void setRejectedDate(LocalDateTime rejectedDate) {
        this.rejectedDate = rejectedDate;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UserDTO getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserDTO modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public EncounterDTO getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterDTO encounter) {
        this.encounter = encounter;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public ConsultantDTO getConsultant() {
        return consultant;
    }

    public void setConsultant(ConsultantDTO consultant) {
        this.consultant = consultant;
    }

    public TransactionType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public OrganizationDTO getReturnTOUnit() {
        return returnTOUnit;
    }

    public void setReturnTOUnit(OrganizationDTO returnTOUnit) {
        this.returnTOUnit = returnTOUnit;
    }

    public HealthcareServiceCenterDTO getPatientLocation() {
        return patientLocation;
    }

    public void setPatientLocation(HealthcareServiceCenterDTO patientLocation) {
        this.patientLocation = patientLocation;
    }

    public IPReturnRequestStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(IPReturnRequestStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public UserDTO getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(UserDTO receivedBy) {
        this.receivedBy = receivedBy;
    }

    public ValueSetCodeDTO getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(ValueSetCodeDTO returnReason) {
        this.returnReason = returnReason;
    }

    public BigDecimal getCalculatedGrossAmount() {
        return calculatedGrossAmount;
    }

    public void setCalculatedGrossAmount(BigDecimal calculatedGrossAmount) {
        this.calculatedGrossAmount = calculatedGrossAmount;
    }

    public BigDecimal getCalculatedPatientGrossAmount() {
        return calculatedPatientGrossAmount;
    }

    public void setCalculatedPatientGrossAmount(BigDecimal calculatedPatientGrossAmount) {
        this.calculatedPatientGrossAmount = calculatedPatientGrossAmount;
    }

    public BigDecimal getCalculatedSponsorGrossAmount() {
        return calculatedSponsorGrossAmount;
    }

    public void setCalculatedSponsorGrossAmount(BigDecimal calculatedSponsorGrossAmount) {
        this.calculatedSponsorGrossAmount = calculatedSponsorGrossAmount;
    }

    public BigDecimal getRoundOff() {
        return roundOff;
    }

    public void setRoundOff(BigDecimal roundOff) {
        this.roundOff = roundOff;
    }

    public List<IPDispenseReturnDocumentLine> getDispenseDocumentLines() {
        return dispenseDocumentLines;
    }

    public void setDispenseDocumentLines(List<IPDispenseReturnDocumentLine> dispenseDocumentLines) {
        this.dispenseDocumentLines = dispenseDocumentLines;
    }

    public List<IPDispenseReturnDocumentLine> getDispenseReturnDocumentLines() {
        return dispenseReturnDocumentLines;
    }

    public void setDispenseReturnDocumentLines(List<IPDispenseReturnDocumentLine> dispenseReturnDocumentLines) {
        this.dispenseReturnDocumentLines = dispenseReturnDocumentLines;
    }

    public OrganizationDTO getUnit() {
        return unit;
    }

    public void setUnit(OrganizationDTO unit) {
        this.unit = unit;
    }

    public AdmissionStatus getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(AdmissionStatus patientStatus) {
        this.patientStatus = patientStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public IPReturnType getIpReturnType() {
        return ipReturnType;
    }

    public void setIpReturnType(IPReturnType ipReturnType) {
        this.ipReturnType = ipReturnType;
    }

    public ValueSetCodeDTO getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(ValueSetCodeDTO rejectReason) {
        this.rejectReason = rejectReason;
    }

    @Override
    public String toString() {
        return "IPDispenseReturnRequestDocument{" +
            "sourceHSC=" + sourceHSC +
            ", returnTOHSC=" + returnTOHSC +
            ", requestedBy=" + requestedBy +
            ", requestedDate=" + requestedDate +
            ", rejectedBy=" + rejectedBy +
            ", rejectedDate=" + rejectedDate +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", modifiedBy=" + modifiedBy +
            ", modifiedDate=" + modifiedDate +
            ", encounter=" + encounter +
            ", patient=" + patient +
            ", consultant=" + consultant +
            ", documentType=" + documentType +
            ", returnDate=" + returnDate +
            ", returnTOUnit=" + returnTOUnit +
            ", patientLocation=" + patientLocation +
            ", returnStatus=" + returnStatus +
            ", receivedBy=" + receivedBy +
            ", returnReason=" + returnReason +
            ", calculatedGrossAmount=" + calculatedGrossAmount +
            ", calculatedPatientGrossAmount=" + calculatedPatientGrossAmount +
            ", calculatedSponsorGrossAmount=" + calculatedSponsorGrossAmount +
            ", roundOff=" + roundOff +
            ", dispenseDocumentLines=" + dispenseDocumentLines +
            ", dispenseReturnDocumentLines=" + dispenseReturnDocumentLines +
            '}';
    }
}
