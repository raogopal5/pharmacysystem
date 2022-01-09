package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A HSCGroupMapping.
 */
@Entity
@Table(name = "hsc_group_mapping")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "hscgroupmapping", type = "hscgroupmapping", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class HSCGroupMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @ManyToOne(optional = false)
    @NotNull
    private HealthcareServiceCenter healthcareServiceCenter;

    @ManyToOne(optional = false)
    @NotNull
    private Group group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public HSCGroupMapping effectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
        return this;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public HealthcareServiceCenter getHealthcareServiceCenter() {
        return healthcareServiceCenter;
    }

    public HSCGroupMapping healthcareServiceCenter(HealthcareServiceCenter HealthcareServiceCenter) {
        this.healthcareServiceCenter = HealthcareServiceCenter;
        return this;
    }

    public void setHealthcareServiceCenter(HealthcareServiceCenter HealthcareServiceCenter) {
        this.healthcareServiceCenter = HealthcareServiceCenter;
    }

    public Group getGroup() {
        return group;
    }

    public HSCGroupMapping group(Group Group) {
        this.group = Group;
        return this;
    }

    public void setGroup(Group Group) {
        this.group = Group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HSCGroupMapping hSCGroupMapping = (HSCGroupMapping) o;
        if (hSCGroupMapping.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, hSCGroupMapping.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "HSCGroupMapping{" +
            "id=" + id +
            ", effectiveFrom='" + effectiveFrom + "'" +
            '}';
    }
}
