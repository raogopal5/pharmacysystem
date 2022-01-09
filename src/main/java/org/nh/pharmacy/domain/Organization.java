package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.nh.pharmacy.domain.dto.OrganizationIdentifier;
import org.nh.repository.hibernate.type.JsonBinaryType;
import org.nh.repository.hibernate.type.JsonStringType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * A Organization.
 */
@Entity
@Table(name = "organization")
@TypeDefs({
    @TypeDef(name = "json", typeClass = JsonStringType.class),
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "organization", type = "organization", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name", nullable = true)
    private String displayName;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "organization_type")
    private ValueSetCode type;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "address")
    private Set<Map<String, Object>> addresses = new HashSet<Map<String, Object>>();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "telecoms")
    private Set<Map<String, Object>> telecoms = new HashSet<Map<String, Object>>();

    @ManyToOne
    private Organization partOf;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "contacts")
    private Set<Map<String, Object>> contacts = new HashSet<Map<String, Object>>();

    @ManyToOne
    private Calendar calendar;

    @Column(name = "started_on")
    private LocalDate startedOn;

    @Column(name = "clinical")
    private Boolean clinical = false;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "website")
    private String website;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "identifier")
    private List<OrganizationIdentifier> identifier;

    @Column(name = "short_name")
    private String shortName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OrganizationIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<OrganizationIdentifier> identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public Organization name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public Organization active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public Organization code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(ValueSetCode type) {
        this.type = type;
    }

    public ValueSetCode getType() {
        return type;
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

    public Organization getPartOf() {
        return partOf;
    }

    public void setPartOf(Organization partOf) {
        this.partOf = partOf;
    }

    public Set<Map<String, Object>> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Map<String, Object>> contacts) {
        this.contacts = contacts;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public LocalDate getStartedOn() {
        return startedOn;
    }

    public Organization startedOn(LocalDate startedOn) {
        this.startedOn = startedOn;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getClinical() {
        return clinical;
    }

    public void setStartedOn(LocalDate startedOn) {
        this.startedOn = startedOn;
    }

    public Boolean isClinical() {
        return clinical;
    }

    public Organization clinical(Boolean clinical) {
        this.clinical = clinical;
        return this;
    }

    public void setClinical(Boolean clinical) {
        this.clinical = clinical;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Organization licenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        return this;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getWebsite() {
        return website;
    }

    public Organization website(String website) {
        this.website = website;
        return this;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Organization organization = (Organization) o;
        if (organization.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, organization.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Organization{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            ", active=" + active +
            ", code='" + code + '\'' +
            ", type=" + type +
            ", addresses=" + addresses +
            ", telecoms=" + telecoms +
            ", partOf=" + partOf +
            ", contacts=" + contacts +
            ", calendar=" + calendar +
            ", startedOn=" + startedOn +
            ", clinical=" + clinical +
            ", licenseNumber='" + licenseNumber + '\'' +
            ", website='" + website + '\'' +
            ", identifier=" + identifier +
            ", shortName=" + shortName +
            '}';
    }
}
