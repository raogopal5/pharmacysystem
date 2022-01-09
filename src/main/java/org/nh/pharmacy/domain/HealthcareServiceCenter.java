package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.common.dto.ValueSetCodeDTO;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * A HealthcareServiceCenter.
 */
@Entity
@Table(name = "healthcare_service_center")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "healthcareservicecenter", type = "healthcareservicecenter", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class HealthcareServiceCenter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name", nullable = true)
    private String displayName;

    @Column(name = "license_number", nullable = true)
    private String licenseNumber;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "available_time")
    private List<AvailableTime> availableTime;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "not_available_time")
    private List<NotAvailableTime> notAvailableTime;

    @NotNull
    @Column(name = "appointment_required", nullable = false)
    private Boolean appointmentRequired;

    @Column(name = "started_on")
    private LocalDateTime startedOn;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "telecom")
    private Set<Map<String, Object>> telecom = new HashSet<Map<String, Object>>();

    @Column(name = "comments")
    private String comments;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "service_category")
    private ValueSetCode serviceCategory;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "capabilities")
    private List<ValueSetCodeDTO> capabilities;

    @ManyToOne
    @NotNull
    private Organization partOf;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Calendar calendar;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "attributes")
    private Map<String, Object> attributes;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "contacts")
    private Set<Map<String, Object>> contacts = new HashSet<Map<String, Object>>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public HealthcareServiceCenter code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public HealthcareServiceCenter name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public HealthcareServiceCenter active(Boolean active) {
        this.active = active;
        return this;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<AvailableTime> getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(List<AvailableTime> availableTime) {
        this.availableTime = availableTime;
    }

    public HealthcareServiceCenter availableTime(List<AvailableTime> availableTime) {
        this.availableTime = availableTime;
        return this;
    }

    public List<NotAvailableTime> getNotAvailableTime() {
        return notAvailableTime;
    }

    public void setNotAvailableTime(List<NotAvailableTime> notAvailableTime) {
        this.notAvailableTime = notAvailableTime;
    }

    public HealthcareServiceCenter notAvailableTime(List<NotAvailableTime> notAvailableTime) {
        this.notAvailableTime = notAvailableTime;
        return this;
    }

    public Set<Map<String, Object>> getTelecom() {
        return telecom;
    }

    public void setTelecom(Set<Map<String, Object>> telecom) {
        this.telecom = telecom;
    }

    public HealthcareServiceCenter telecom(Set<Map<String, Object>> telecom) {
        this.telecom = telecom;
        return this;
    }

    public Boolean isAppointmentRequired() {
        return appointmentRequired;
    }

    public HealthcareServiceCenter appointmentRequired(Boolean appointmentRequired) {
        this.appointmentRequired = appointmentRequired;
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

    public Boolean getAppointmentRequired() {
        return appointmentRequired;
    }

    public void setAppointmentRequired(Boolean appointmentRequired) {
        this.appointmentRequired = appointmentRequired;
    }

    public LocalDateTime getStartedOn() {
        return startedOn;
    }

    public HealthcareServiceCenter startedOn(LocalDateTime startedOn) {
        this.startedOn = startedOn;
        return this;
    }

    public void setStartedOn(LocalDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public String getComments() {
        return comments;
    }

    public HealthcareServiceCenter comments(String comments) {
        this.comments = comments;
        return this;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ValueSetCode getServiceCategory() {
        return serviceCategory;
    }

    public HealthcareServiceCenter serviceCategory(ValueSetCode ServiceCategory) {
        this.serviceCategory = ServiceCategory;
        return this;
    }

    public void setServiceCategory(ValueSetCode ServiceCategory) {
        this.serviceCategory = ServiceCategory;
    }

   /* public ValueSetCode getSubCategory() {
        return subCategory;
    }

    public HealthcareServiceCenter subCategory(ValueSetCode SubCategory) {
        this.subCategory = SubCategory;
        return this;
    }

    public void setSubCategory(ValueSetCode SubCategory) {
        this.subCategory = SubCategory;
    }*/

    public List<ValueSetCodeDTO> getCapabilities() {
        return capabilities;
    }

    public HealthcareServiceCenter capabilities(List<ValueSetCodeDTO> capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public void setCapabilities(List<ValueSetCodeDTO> capabilities) {
        this.capabilities = capabilities;
    }

    public Organization getPartOf() {
        return partOf;
    }

    public HealthcareServiceCenter partOf(Organization Organization) {
        this.partOf = Organization;
        return this;
    }

    public void setPartOf(Organization Organization) {
        this.partOf = Organization;
    }

    public Location getLocation() {
        return location;
    }

    public HealthcareServiceCenter location(Location location) {
        this.location = location;
        return this;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public HealthcareServiceCenter calendar(Calendar Calendar) {
        this.calendar = Calendar;
        return this;
    }

    public void setCalendar(Calendar Calendar) {
        this.calendar = Calendar;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Set<Map<String, Object>> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Map<String, Object>> contacts) {
        this.contacts = contacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HealthcareServiceCenter healthcareServiceCenter = (HealthcareServiceCenter) o;
        if (healthcareServiceCenter.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, healthcareServiceCenter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "HealthcareServiceCenter{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            ", licenseNumber='" + licenseNumber + '\'' +
            ", active=" + active +
            ", availableTime=" + availableTime +
            ", notAvailableTime=" + notAvailableTime +
            ", appointmentRequired=" + appointmentRequired +
            ", startedOn=" + startedOn +
            ", telecom=" + telecom +
            ", comments='" + comments + '\'' +
            ", serviceCategory=" + serviceCategory +
            ", capabilities = " + capabilities +
            ", partOf=" + partOf +
            ", location=" + location +
            ", calendar=" + calendar +
            ", attributes=" + attributes +
            ", contacts=" + contacts +
            '}';
    }
}
