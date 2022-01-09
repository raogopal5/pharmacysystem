package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A SystemAlert.l
 */
@Entity
@Table(name = "system_alert")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SystemAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "from_class", nullable = false)
    private String fromClass;

    @NotNull
    @Column(name = "on_date", nullable = false)
    private ZonedDateTime onDate;

    @NotNull
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromClass() {
        return fromClass;
    }

    public SystemAlert fromClass(String fromClass) {
        this.fromClass = fromClass;
        return this;
    }

    public void setFromClass(String fromClass) {
        this.fromClass = fromClass;
    }

    public ZonedDateTime getOnDate() {
        return onDate;
    }

    public SystemAlert onDate(ZonedDateTime onDate) {
        this.onDate = onDate;
        return this;
    }

    public void setOnDate(ZonedDateTime onDate) {
        this.onDate = onDate;
    }

    public String getMessage() {
        return message;
    }

    public SystemAlert message(String message) {
        this.message = message;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public SystemAlert description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemAlert systemAlert = (SystemAlert) o;
        if (systemAlert.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, systemAlert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SystemAlert{" +
            "id=" + id +
            ", fromClass='" + fromClass + "'" +
            ", onDate='" + onDate + "'" +
            ", message='" + message + "'" +
            ", description='" + description + "'" +
            '}';
    }

    public SystemAlert addDescription(Throwable th) {
        StringWriter stack = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stack);
        th.printStackTrace(printWriter);
        this.description = stack.toString();
        return this;
    }
}
