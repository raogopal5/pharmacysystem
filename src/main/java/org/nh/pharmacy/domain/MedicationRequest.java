package org.nh.pharmacy.domain;

import org.elasticsearch.painless.node.EBoolean;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.MedicationRequestDocument;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A MedicationRequest.
 */
@Entity
@Table(name = "medication_request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "medicationrequest")
@Setting(settingPath = "/es/settings.json")
public class MedicationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "document_number", nullable = false)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword)
        }
    )
    private String documentNumber;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "document", nullable = false)
    @Field(type = FieldType.Object)
    private MedicationRequestDocument document;

    @NotNull
    @Column(name = "version", nullable = false)
    private Integer version;

    @NotNull
    @Column(name = "latest", nullable = false)
    private Boolean latest;

    @Column(name ="prescription_audited")
    private boolean prescriptionAudited;

    @Column(name ="discharge_medication")
    private Boolean dischargeMedication =  false;

    public Boolean isDischargeMedication() {
        return dischargeMedication;
    }

    public Boolean getDischargeMedication() {
        return dischargeMedication;
    }

    public void setDischargeMedication(Boolean dischargeMedication) {
        this.dischargeMedication = dischargeMedication;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public MedicationRequest documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public MedicationRequestDocument getDocument() {
        return document;
    }

    public MedicationRequest document(MedicationRequestDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(MedicationRequestDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public MedicationRequest version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public MedicationRequest latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public boolean isPrescriptionAudited() {
        return prescriptionAudited;
    }

    public void setPrescriptionAudited(boolean prescriptionAudited) {
        this.prescriptionAudited = prescriptionAudited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MedicationRequest medicationRequest = (MedicationRequest) o;
        if (medicationRequest.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, medicationRequest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MedicationRequest{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }
}
