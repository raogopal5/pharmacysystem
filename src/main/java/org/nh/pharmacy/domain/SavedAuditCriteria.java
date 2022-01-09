package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.AuditCriteria;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A SavedAuditCriteria.
 */
@Entity
@Table(name = "saved_audit_criterias")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Setting(settingPath = "/es/settings.json")
@Document(indexName = "savedauditcriteria")
public class SavedAuditCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "audit_criterias")
    private List<AuditCriteria> auditCriterias;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "unit")
    @Field(type = FieldType.Object)
    private Organization unit;

    @Column(name = "name")
    private String name;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "hsc")
    @Field(type = FieldType.Object)
    private HealthcareServiceCenter hsc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AuditCriteria> getAuditCriterias() {
        return auditCriterias;
    }

    public SavedAuditCriteria auditCriterias(List<AuditCriteria> auditCriterias) {
        this.auditCriterias = auditCriterias;
        return this;
    }

    public void setAuditCriterias(List<AuditCriteria> auditCriterias) {
        this.auditCriterias = auditCriterias;
    }

    public Organization getUnit() {
        return unit;
    }

    public SavedAuditCriteria unit(Organization unit) {
        this.unit = unit;
        return this;
    }

    public void setUnit(Organization unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SavedAuditCriteria name(String name) {
        this.name = name;
        return this;
    }

    public HealthcareServiceCenter getHsc() {
        return hsc;
    }

    public void setHsc(HealthcareServiceCenter hsc) {
        this.hsc = hsc;
    }

    public SavedAuditCriteria hsc(HealthcareServiceCenter hsc) {
        this.hsc = hsc;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SavedAuditCriteria savedAuditCriteria = (SavedAuditCriteria) o;
        if (savedAuditCriteria.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, savedAuditCriteria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SavedAuditCriteria{" +
            "id=" + id +
            ", auditCriterias=" + auditCriterias +
            ", unit=" + unit +
            ", name='" + name + '\'' +
            ", hsc=" + hsc +
            '}';
    }
}
