package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.IPDispenseReturnRequestDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A IPDispenseReturnRequest.
 */
@Entity
@Table(name = "ipdispense_return_request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Setting(settingPath = "/es/settings.json")
@Document(indexName = "ipdispensereturnrequest")
@IdClass(DocumentId.class)
public class IPDispenseReturnRequest implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "document_number")
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
    private IPDispenseReturnRequestDocument document;

    @Id
    private Integer version;

    @NotNull
    @Column(name = "latest", nullable = false)
    private Boolean latest=Boolean.TRUE;

    @Transient
    private boolean isNew = true;

    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        if (isNew) {
            isNew = false;
            return true;
        }
        return isNew;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public IPDispenseReturnRequest documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public IPDispenseReturnRequestDocument getDocument() {
        return document;
    }

    public IPDispenseReturnRequest document(IPDispenseReturnRequestDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(IPDispenseReturnRequestDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public IPDispenseReturnRequest version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public IPDispenseReturnRequest latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPDispenseReturnRequest iPDispenseReturnRequest = (IPDispenseReturnRequest) o;
        if(iPDispenseReturnRequest.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, iPDispenseReturnRequest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "IPDispenseReturnRequest{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }
}
