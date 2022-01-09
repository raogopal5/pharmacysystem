package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.PendingAuditRequestDocument;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A PrescriptionAuditRequest.
 */
@Entity
@Table(name = "prescription_audit_request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "prescriptionauditrequest")
@Setting(settingPath = "/es/settings.json")
public class PrescriptionAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "document_number", nullable = false)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String documentNumber;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "document", nullable = false)
    @Field(type = FieldType.Object)
    private PendingAuditRequestDocument document;

    @NotNull
    @Version
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public PrescriptionAuditRequest documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public PendingAuditRequestDocument getDocument() {
        return document;
    }

    public PrescriptionAuditRequest document(PendingAuditRequestDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(PendingAuditRequestDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public PrescriptionAuditRequest version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrescriptionAuditRequest prescriptionAuditRequest = (PrescriptionAuditRequest) o;
        if (prescriptionAuditRequest.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, prescriptionAuditRequest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PrescriptionAuditRequest{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            '}';
    }
}

