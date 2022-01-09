package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Calendar.
 */
@Entity
@Table(name = "calendar")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "calendar", type = "calendar", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Calendar implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "status", nullable = false)
    private Boolean status;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @NotNull
    private Organization createdFor;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "calendar_id", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Holidays> holidays = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Calendar name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isStatus() {
        return status;
    }

    public Calendar status(Boolean status) {
        this.status = status;
        return this;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public Calendar description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organization getCreatedFor() {
        return createdFor;
    }

    public Calendar createdFor(Organization Organization) {
        this.createdFor = Organization;
        return this;
    }

    public void setCreatedFor(Organization Organization) {
        this.createdFor = Organization;
    }

    public Set<Holidays> getHolidays() {
        return holidays;
    }

    public Calendar holidays(Set<Holidays> Holidays) {
        this.holidays = Holidays;
        return this;
    }

    public Calendar addHolidays(Holidays Holidays) {
        this.holidays.add(Holidays);
        return this;
    }

    public Calendar removeHolidays(Holidays Holidays) {
        this.holidays.remove(Holidays);
        return this;
    }

    public void setHolidays(Set<Holidays> Holidays) {
        this.holidays = Holidays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Calendar calendar = (Calendar) o;
        if (calendar.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, calendar.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Calendar{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", status='" + status + "'" +
            ", description='" + description + "'" +
            '}';
    }
}
