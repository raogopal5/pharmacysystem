package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.*;
import org.nh.common.enumeration.AdmissionAndNursingStatus;
import org.nh.pharmacy.domain.enumeration.MedicationRequestStatus;
import org.springframework.data.elasticsearch.annotations.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Indrajeet 11/30/2017
 */
public class MedicationRequestDocument implements Serializable {

    List<MedicationRequestDocumentLine> documentLines;

    private UserDTO consultant;

    private OrganizationDTO unit;

    private UserDTO recorder;

    @Field(type = FieldType.Object)
    private EncounterDTO encounter;

    @Field(type = FieldType.Object)
    private PatientDTO patient;

    private LocalDateTime createdDate;

    @Field(type = FieldType.Object)
    private UserDTO createdBy;

    private UserDTO modifiedBy;

    private LocalDateTime modifiedDate;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword)
        }
    )
    private MedicationRequestStatus medicationRequestStatus;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO orderingHSC;

    private OrganizationDTO department;

    private List<SourceDTO> sourceDTOList;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword)
        }
    )
    private AdmissionAndNursingStatus patientStatus;

    private String planName;

    private String bedNumber;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO patientLocation;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String priority;

    @Field(type = FieldType.Date)
    private LocalDateTime medicationRequestDate;

    @NotNull
    @Field(type = FieldType.Object)
    private OrganizationDTO orderingDepartment;

    @NotNull
    @Field(type = FieldType.Object)
    private ConsultantDTO orderingConsultant;

    @Field(type = FieldType.Object)
    private OrganizationDTO renderingDepartment;

    @Field(type = FieldType.Object)
    private ConsultantDTO renderingConsultant;

    private boolean pendingAudited;


    private boolean dischargeMedication =  false;

    public boolean isDischargeMedication() {
        return dischargeMedication;
    }

    public void setDischargeMedication(boolean dischargeMedication) {
        this.dischargeMedication = dischargeMedication;
    }

    public AdmissionAndNursingStatus getPatientStatus() {return patientStatus;}

    public void setPatientStatus(AdmissionAndNursingStatus patientStatus) {this.patientStatus = patientStatus;}

    public List<MedicationRequestDocumentLine> getDocumentLines() {
        return documentLines;
    }

    public void setDocumentLines(List<MedicationRequestDocumentLine> documentLines) {
        this.documentLines = documentLines;
    }

    public UserDTO getConsultant() {
        return consultant;
    }

    public void setConsultant(UserDTO consultant) {
        this.consultant = consultant;
    }

    public OrganizationDTO getUnit() {
        return unit;
    }

    public void setUnit(OrganizationDTO unit) {
        this.unit = unit;
    }

    public UserDTO getRecorder() {
        return recorder;
    }

    public void setRecorder(UserDTO recorder) {
        this.recorder = recorder;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
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

    public MedicationRequestStatus getMedicationRequestStatus() {return medicationRequestStatus;}

    public void setMedicationRequestStatus(MedicationRequestStatus medicationRequestStatus) {this.medicationRequestStatus = medicationRequestStatus;}

    public HealthcareServiceCenterDTO getOrderingHSC() {
        return orderingHSC;
    }

    public void setOrderingHSC(HealthcareServiceCenterDTO orderingHSC) {
        this.orderingHSC = orderingHSC;
    }

    public OrganizationDTO getDepartment() {
        return department;
    }

    public void setDepartment(OrganizationDTO department) {
        this.department = department;
    }

    public List<SourceDTO> getSourceDTOList() {
        return sourceDTOList;
    }

    public void setSourceDTOList(List<SourceDTO> sourceDTOList) {
        this.sourceDTOList = sourceDTOList;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public HealthcareServiceCenterDTO getPatientLocation() {
        return patientLocation;
    }

    public void setPatientLocation(HealthcareServiceCenterDTO patientLocation) {
        this.patientLocation = patientLocation;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getMedicationRequestDate() {
        return medicationRequestDate;
    }

    public void setMedicationRequestDate(LocalDateTime medicationRequestDate) {
        this.medicationRequestDate = medicationRequestDate;
    }

    public OrganizationDTO getOrderingDepartment() {
        return orderingDepartment;
    }

    public void setOrderingDepartment(OrganizationDTO orderingDepartment) {
        this.orderingDepartment = orderingDepartment;
    }

    public ConsultantDTO getOrderingConsultant() {
        return orderingConsultant;
    }

    public void setOrderingConsultant(ConsultantDTO orderingConsultant) {
        this.orderingConsultant = orderingConsultant;
    }

    public OrganizationDTO getRenderingDepartment() {
        return renderingDepartment;
    }

    public void setRenderingDepartment(OrganizationDTO renderingDepartment) {
        this.renderingDepartment = renderingDepartment;
    }

    public ConsultantDTO getRenderingConsultant() {
        return renderingConsultant;
    }

    public void setRenderingConsultant(ConsultantDTO renderingConsultant) {
        this.renderingConsultant = renderingConsultant;
    }

    public boolean isPendingAudited() {
        return pendingAudited;
    }

    public void setPendingAudited(boolean pendingAudited) {
        this.pendingAudited = pendingAudited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicationRequestDocument)) return false;

        MedicationRequestDocument that = (MedicationRequestDocument) o;

        if (getDocumentLines() != null ? !getDocumentLines().equals(that.getDocumentLines()) : that.getDocumentLines() != null) return false;
        if (getConsultant() != null ? !getConsultant().equals(that.getConsultant()) : that.getConsultant() != null) return false;
        if (getUnit() != null ? !getUnit().equals(that.getUnit()) : that.getUnit() != null) return false;
        if (getRecorder() != null ? !getRecorder().equals(that.getRecorder()) : that.getRecorder() != null) return false;
        if (getEncounter() != null ? !getEncounter().equals(that.getEncounter()) : that.getEncounter() != null) return false;
        if (getPatient() != null ? !getPatient().equals(that.getPatient()) : that.getPatient() != null) return false;
        if (getCreatedDate() != null ? !getCreatedDate().equals(that.getCreatedDate()) : that.getCreatedDate() != null) return false;
        if (getCreatedBy() != null ? !getCreatedBy().equals(that.getCreatedBy()) : that.getCreatedBy() != null) return false;
        if (getModifiedBy() != null ? !getModifiedBy().equals(that.getModifiedBy()) : that.getModifiedBy() != null) return false;
        if (getModifiedDate() != null ? !getModifiedDate().equals(that.getModifiedDate()) : that.getModifiedDate() != null) return false;
        return getMedicationRequestStatus() == that.getMedicationRequestStatus();
    }

    @Override
    public int hashCode() {
        int result = getDocumentLines() != null ? getDocumentLines().hashCode() : 0;
        result = 31 * result + (getConsultant() != null ? getConsultant().hashCode() : 0);
        result = 31 * result + (getUnit() != null ? getUnit().hashCode() : 0);
        result = 31 * result + (getRecorder() != null ? getRecorder().hashCode() : 0);
        result = 31 * result + (getEncounter() != null ? getEncounter().hashCode() : 0);
        result = 31 * result + (getPatient() != null ? getPatient().hashCode() : 0);
        result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
        result = 31 * result + (getCreatedBy() != null ? getCreatedBy().hashCode() : 0);
        result = 31 * result + (getModifiedBy() != null ? getModifiedBy().hashCode() : 0);
        result = 31 * result + (getModifiedDate() != null ? getModifiedDate().hashCode() : 0);
        result = 31 * result + (getMedicationRequestStatus() != null ? getMedicationRequestStatus().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MedicationRequestDocument{" +
            "documentLines=" + documentLines +
            ", consultant=" + consultant +
            ", unit=" + unit +
            ", recorder=" + recorder +
            ", encounter=" + encounter +
            ", patient=" + patient +
            ", createdDate=" + createdDate +
            ", createdBy=" + createdBy +
            ", modifiedBy=" + modifiedBy +
            ", modifiedDate=" + modifiedDate +
            ", medicationRequestStatus=" + medicationRequestStatus +
            ", patientStatus=" + patientStatus +
            '}';
    }
}
