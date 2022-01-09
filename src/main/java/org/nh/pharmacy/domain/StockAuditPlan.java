package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.AuditDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A StockAuditPlan.
 */
@Entity
@Table(name = "stock_audit_plan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "stockauditplan")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class StockAuditPlan implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Id
    private Integer version;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "document", nullable = false)
    private AuditDocument document;

    @Column(name = "latest", nullable = false)
    private Boolean latest;

    private transient boolean isNew = true;

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

    public StockAuditPlan id(Long id) {
        this.id = id;
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public StockAuditPlan documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public AuditDocument getDocument() {
        return document;
    }

    public StockAuditPlan document(AuditDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(AuditDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public StockAuditPlan version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public StockAuditPlan latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        StockAuditPlan that = (StockAuditPlan) obj;

        return (id != null && id.equals(that.id)) &&
            (version != null && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "StockAuditPlan{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }
}
