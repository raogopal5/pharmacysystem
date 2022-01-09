package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.common.dto.LocatorDTO;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Locator.
 */
@Entity
@Table(name = "locator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "locator", type = "locator", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Locator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne
    private Locator partOf;

    @ManyToOne
    private HealthcareServiceCenter managingHSC;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Locator code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Locator name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public Locator active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Locator getPartOf() {
        return partOf;
    }

    public Locator partOf(Locator locator) {
        this.partOf = locator;
        return this;
    }

    public void setPartOf(Locator locator) {
        this.partOf = locator;
    }

    public HealthcareServiceCenter getManagingHSC() {
        return managingHSC;
    }

    public Locator managingHSC(HealthcareServiceCenter HealthcareServiceCenter) {
        this.managingHSC = HealthcareServiceCenter;
        return this;
    }

    public void setManagingHSC(HealthcareServiceCenter HealthcareServiceCenter) {
        this.managingHSC = HealthcareServiceCenter;
    }

    public LocatorDTO getLocatorDTO() {
        LocatorDTO locatorDTO = new LocatorDTO();
        locatorDTO.setId(this.getId());
        locatorDTO.setCode(this.getCode());
        locatorDTO.setName(this.getName());
        return locatorDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Locator locator = (Locator) o;
        if (locator.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, locator.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Locator{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", active='" + active + "'" +
            '}';
    }
}
