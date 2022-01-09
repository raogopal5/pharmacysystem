package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A Location.
 */
@Entity
@Table(name = "location")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "location", type = "location", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "status")
    private ValueSetCode status;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "mode")
    private ValueSetCode mode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "service_delivery_location_role_type")
    private ValueSetCode type;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "address")
    private Set<Map<String, Object>> addresses = new HashSet<Map<String, Object>>();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "telecoms")
    private Set<Map<String, Object>> telecoms = new HashSet<Map<String, Object>>();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "location_type")
    private ValueSetCode physicalType;

    @ManyToOne
    private Organization managingOrganization;

    @ManyToOne
    private Location partOf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Location code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Location name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Location description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActive() {
        return active;
    }

    public Location active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ValueSetCode getStatus() {
        return status;
    }

    public void setStatus(ValueSetCode status) {
        this.status = status;
    }

    public ValueSetCode getMode() {
        return mode;
    }

    public void setMode(ValueSetCode mode) {
        this.mode = mode;
    }

    public ValueSetCode getType() {
        return type;
    }

    public void setType(ValueSetCode type) {
        this.type = type;
    }

    public Set<Map<String, Object>> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Map<String, Object>> addresses) {
        this.addresses = addresses;
    }

    public Set<Map<String, Object>> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(Set<Map<String, Object>> telecoms) {
        this.telecoms = telecoms;
    }

    public ValueSetCode getPhysicalType() {
        return physicalType;
    }

    public void setPhysicalType(ValueSetCode physicalType) {
        this.physicalType = physicalType;
    }

    public Organization getManagingOrganization() {
        return managingOrganization;
    }

    public Location managingOrganization(Organization organization) {
        this.managingOrganization = organization;
        return this;
    }

    public void setManagingOrganization(Organization organization) {
        this.managingOrganization = organization;
    }

    public Location getPartOf() {
        return partOf;
    }

    public Location partOf(Location location) {
        this.partOf = location;
        return this;
    }

    public void setPartOf(Location location) {
        this.partOf = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        if (location.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Location{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            ", active='" + active + "'" +
            '}';
    }
}
