package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.HolidayType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Holidays.
 */
@Entity
@Table(name = "holidays")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "holidays", type = "holidays", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Holidays implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "date", nullable = false)
    private ZonedDateTime date;

    @NotNull
    @Column(name = "occasion", nullable = false)
    private String occasion;

    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HolidayType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public Holidays date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public String getOccasion() {
        return occasion;
    }

    public Holidays occasion(String occasion) {
        this.occasion = occasion;
        return this;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public String getDescription() {
        return description;
    }

    public Holidays description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HolidayType getType() {
        return type;
    }

    public Holidays type(HolidayType type) {
        this.type = type;
        return this;
    }

    public void setType(HolidayType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Holidays holidays = (Holidays) o;
        if (holidays.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, holidays.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Holidays{" +
            "id=" + id +
            ", date='" + date + "'" +
            ", occasion='" + occasion + "'" +
            ", description='" + description + "'" +
            ", type='" + type + "'" +
            '}';
    }
}
