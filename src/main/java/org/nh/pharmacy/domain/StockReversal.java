package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.ReversalDocument;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A StockReversal.
 */
@Entity
@Table(name = "stock_reversal")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "stockreversal", type = "stockreversal")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class StockReversal implements Serializable, Persistable<Long> {

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
    private ReversalDocument document;


    @NotNull
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

    public StockReversal id(Long id) {
        this.id = id;
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public StockReversal documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public ReversalDocument getDocument() {
        return document;
    }

    public StockReversal document(ReversalDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(ReversalDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public StockReversal version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public StockReversal latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public String getSequenceValueForReversalSource() {
        if (TransactionType.Stock_Issue.equals(document.getSourceType()) || TransactionType.Inter_Unit_Stock_Issue.equals(document.getSourceType())) {
            return "I";
        }

        if (TransactionType.Stock_Direct_Transfer.equals(document.getSourceType())) {
            return "D";
        }

        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        StockReversal that = (StockReversal) obj;

        return (id != null && id.equals(that.id)) &&
            (version != null && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "StockReversal{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }
}
