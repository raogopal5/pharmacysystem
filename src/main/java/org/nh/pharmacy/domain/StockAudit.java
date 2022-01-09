package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.AuditCriteria;
import org.nh.pharmacy.domain.dto.AuditDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A StockAudit.
 */
@Entity
@Table(name = "stock_audit")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "stockaudit")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class StockAudit implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Id
    private Integer version;

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
    private AuditDocument document;

    @Column(name = "latest", nullable = false)
    private Boolean latest;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "audit_criteria", nullable = false)
    private List<AuditCriteria> auditCriterias;

    @Transient
    private boolean isNew = true;

    /**
     * Returns if the {@code Persistable} is new or was persisted already.
     *
     * @return if the object is new
     */
    @Override
    public boolean isNew() {
        if (isNew) {
            isNew = false;
            return true;
        }
        return isNew;
    }

    public Long getId() {
        return id;
    }

    public StockAudit id(Long id) {
        this.id = id;
        return this;
    }

    public StockAudit() {
    }

    public StockAudit(AuditDocument document) {
        this.document = document;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public StockAudit documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public AuditDocument getDocument() {
        return document;
    }

    public StockAudit document(AuditDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(AuditDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public StockAudit version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public StockAudit latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public List<AuditCriteria> getAuditCriterias() {
        return auditCriterias;
    }

    public void setAuditCriterias(List<AuditCriteria> auditCriterias) {
        this.auditCriterias = auditCriterias;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        StockAudit that = (StockAudit) obj;

        return (id != null && id.equals(that.id)) &&
            (version != null && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "StockAudit{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            ", auditCriterias='" + auditCriterias + "'" +
            '}';
    }
}
