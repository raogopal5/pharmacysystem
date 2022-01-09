package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.common.dto.*;
import org.nh.pharmacy.domain.dto.MedicationOrderDocumentLine;
import org.nh.pharmacy.domain.enumeration.MedicationOrderStatus;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * A MedicationOrder.
 */
@Entity
@Table(name = "medication_order")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "#{@moduleName}_medicationorder")
@Setting(settingPath = "/es/settings.json")
public class MedicationOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medication_order_number")
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String medicationOrderNumber;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "document_lines", nullable = false)
    @Field(type = FieldType.Object)
    private MedicationOrderDocumentLine documentLines;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "consultant", nullable = false)
    @Field(type = FieldType.Object)
    private UserDTO consultant;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "unit", nullable = false)
    @Field(type = FieldType.Object)
    private OrganizationDTO unit;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "recorder", nullable = false)
    private UserDTO recorder;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "encounter", nullable = false)
    @Field(type = FieldType.Object)
    private EncounterDTO encounter;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "patient", nullable = false)
    @Field(type = FieldType.Object)
    private PatientDTO patient;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "created_by", nullable = false)
    @Field(type = FieldType.Object)
    private UserDTO createdBy;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "modified_by", nullable = false)
    private UserDTO modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "medication_order_date")
    private LocalDateTime medicationOrderDate;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "rendering_hsc", nullable = false)
    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO renderingHSC;

    @Enumerated
    @Column(name = "medication_order_status")
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private MedicationOrderStatus medicationOrderStatus;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "ordering_hsc", nullable = false)
    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO orderingHSC;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "department", nullable = false)
    @Field(type = FieldType.Object)
    private OrganizationDTO department;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "source_documents", nullable = false)
    @Field(type = FieldType.Object)
    private List<SourceDTO> sourceDocumentList;

    @Column(name ="medication_request_id",nullable = false)
    private Long medicationRequestId;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String medicationRequestNumber;

    @NotNull
    @Column(name = "ordering_department", nullable = false)
    @Type(type = "jsonb")
    @Field(type = FieldType.Object)
    private OrganizationDTO orderingDepartment;

    @NotNull
    @Column(name = "ordering_consultant", nullable = false)
    @Type(type = "jsonb")
    @Field(type = FieldType.Object)
    private ConsultantDTO orderingConsultant;

    @Column(name = "rendering_department")
    @Type(type = "jsonb")
    @Field(type = FieldType.Object)
    private OrganizationDTO renderingDepartment;

    @Column(name = "rendering_consultant")
    @Type(type = "jsonb")
    @Field(type = FieldType.Object)
    private ConsultantDTO renderingConsultant;

    @Column(name = "discharge_medication")
    private boolean dischargeMedication =  false;

    public boolean isDischargeMedication() {
        return dischargeMedication;
    }

    public void setDischargeMedication(boolean dischargeMedication) {
        this.dischargeMedication = dischargeMedication;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicationOrderNumber() {
        return medicationOrderNumber;
    }

    public void setMedicationOrderNumber(String medicationOrderNumber) {
        this.medicationOrderNumber = medicationOrderNumber;
    }

    public MedicationOrderDocumentLine getDocumentLines() {
        return documentLines;
    }

    public void setDocumentLines(MedicationOrderDocumentLine documentLines) {
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

    public LocalDateTime getMedicationOrderDate() {
        return medicationOrderDate;
    }

    public void setMedicationOrderDate(LocalDateTime medicationOrderDate) {
        this.medicationOrderDate = medicationOrderDate;
    }

    public HealthcareServiceCenterDTO getRenderingHSC() {
        return renderingHSC;
    }

    public void setRenderingHSC(HealthcareServiceCenterDTO renderingHSC) {
        this.renderingHSC = renderingHSC;
    }

    public MedicationOrderStatus getMedicationOrderStatus() {
        return medicationOrderStatus;
    }

    public void setMedicationOrderStatus(MedicationOrderStatus medicationOrderStatus) {
        this.medicationOrderStatus = medicationOrderStatus;
    }

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

    public MedicationOrder medicationOrderNumber(String medicationOrderNumber) {
        this.medicationOrderNumber = medicationOrderNumber;
        return this;
    }

    public MedicationOrder documentLines(MedicationOrderDocumentLine documentLines){
        this.documentLines= documentLines;
        return this;
    }

    public MedicationOrder consultant(UserDTO consultant){
        this.consultant= consultant;
        return this;
    }

    public MedicationOrder unit(OrganizationDTO unit){
        this.unit= unit;
        return this;
    }
    public MedicationOrder recorder(UserDTO recorder){
        this.recorder= recorder;
        return this;
    }
    public MedicationOrder encounter(EncounterDTO encounter){
        this.encounter= encounter;
        return this;
    }

    public MedicationOrder patient(PatientDTO patient){
        this.patient= patient;
        return this;
    }
    public MedicationOrder createdDate(LocalDateTime createdDate){
        this.createdDate= createdDate;
        return this;
    }
    public MedicationOrder createdBy(UserDTO createdBy){
        this.createdBy= createdBy;
        return this;
    }
    public MedicationOrder modifiedBy(UserDTO modifiedBy){
        this.modifiedBy= modifiedBy;
        return this;
    }
    public MedicationOrder modifiedDate(LocalDateTime modifiedDate){
        this.modifiedDate= modifiedDate;
        return this;
    }
    public MedicationOrder medicationOrderDate(LocalDateTime medicationOrderDate){
        this.medicationOrderDate= medicationOrderDate;
        return this;
    }

    public MedicationOrder renderingHSC(HealthcareServiceCenterDTO renderingHSC){
        this.renderingHSC= renderingHSC;
        return this;
    }
    public MedicationOrder medicationOrderStatus(MedicationOrderStatus medicationOrderStatus){
        this.medicationOrderStatus= medicationOrderStatus;
        return this;
    }
    public MedicationOrder orderingHSC(HealthcareServiceCenterDTO orderingHSC){
        this.orderingHSC= orderingHSC;
        return this;
    }
    public MedicationOrder department(OrganizationDTO department){
        this.department= department;
        return this;
    }

    public List<SourceDTO> getSourceDocumentList() {
        return sourceDocumentList;
    }

    public void setSourceDocumentList(List<SourceDTO> sourceDocumentList) {
        this.sourceDocumentList = sourceDocumentList;
    }

    public Long getMedicationRequestId() {
        return medicationRequestId;
    }

    public void setMedicationRequestId(Long medicationRequestId) {
        this.medicationRequestId = medicationRequestId;
    }

    public String getMedicationRequestNumber() {
        return medicationRequestNumber;
    }

    public void setMedicationRequestNumber(String medicationRequestNumber) {
        this.medicationRequestNumber = medicationRequestNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MedicationOrder medicationOrder = (MedicationOrder) o;
        if(medicationOrder.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, medicationOrder.id) && Objects.equals(medicationOrderNumber,medicationOrder.medicationOrderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MedicationOrder{" +
                "id=" + id +
                ", medicationOrderNumber='" + medicationOrderNumber + '\'' +
                ", documentLines=" + documentLines +
                ", consultant=" + consultant +
                ", unit=" + unit +
                ", recorder=" + recorder +
                ", encounter=" + encounter +
                ", patient=" + patient +
                ", createdDate=" + createdDate +
                ", createdBy=" + createdBy +
                ", modifiedBy=" + modifiedBy +
                ", modifiedDate=" + modifiedDate +
                ", medicationOrderDate=" + medicationOrderDate +
                ", renderingHSC=" + renderingHSC +
                ", medicationOrderStatus=" + medicationOrderStatus +
                ", orderingHSC=" + orderingHSC +
                ", department=" + department +
                '}';
    }
}
