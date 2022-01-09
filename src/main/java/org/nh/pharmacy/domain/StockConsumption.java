package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.ConsumptionDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A StockConsumption.
 */
@Entity
@Table(name = "stock_consumption")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "stockconsumption", type = "stockconsumption")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class StockConsumption implements Serializable, Persistable<Long> {

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
    private ConsumptionDocument document;

    @Column(name = "latest", nullable = false)
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

    public StockConsumption id(Long id) {
        this.id = id;
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public StockConsumption documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public ConsumptionDocument getDocument() {
        return document;
    }

    public StockConsumption document(ConsumptionDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(ConsumptionDocument document) {
        this.document = document;
    }

    public Boolean isLatest() {
        return latest;
    }

    public StockConsumption latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public Boolean getLatest() {
        return latest;
    }

    public Integer getVersion() {
        return version;
    }

    public StockConsumption version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        StockConsumption that = (StockConsumption) obj;

        return (id != null && id.equals(that.id)) &&
            (version != null && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "StockConsumption{" +
            "id=" + id +
            ", version=" + version +
            ", documentNumber='" + documentNumber + '\'' +
            ", document=" + document +
            ", latest=" + latest +
            '}';
    }
}
