package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.InventoryAdjustmentDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A InventoryAdjustment.
 */
@Entity
@Table(name = "inventory_adjustment")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "inventoryadjustment", type = "inventoryadjustment")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class InventoryAdjustment implements Serializable, Persistable<Long> {

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
    @Column(name = "document", columnDefinition = "jsonb")
    @Field(type = FieldType.Object)
    private InventoryAdjustmentDocument document;

    @Column(name = "latest")
    private Boolean latest;

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

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public InventoryAdjustment documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public InventoryAdjustmentDocument getDocument() {
        return document;
    }

    public InventoryAdjustment document(InventoryAdjustmentDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(InventoryAdjustmentDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public InventoryAdjustment version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public InventoryAdjustment latest(Boolean latest) {
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
        InventoryAdjustment that = (InventoryAdjustment) obj;

        return (id != null && id.equals(that.id)) &&
            (version != null && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "InventoryAdjustment{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }
}
