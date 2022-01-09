package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.nh.pharmacy.domain.enumeration.Context;
import org.nh.repository.hibernate.type.JsonBinaryType;
import org.nh.repository.hibernate.type.JsonStringType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A Group.
 */
@Entity
@Table(name = "group_master")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@TypeDefs({
    @TypeDef(name = "json", typeClass = JsonStringType.class),
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
}
)
@Document(indexName = "group", type = "group", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Column(name = "actual", nullable = false)
    private Boolean actual;

    @Enumerated(EnumType.STRING)
    @Column(name = "context")
    private Context context;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "members", nullable = false)
    private List<GroupMember> members;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "group_type")
    private ValueSetCode type;

    @ManyToOne
    private Organization partOf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Group name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public Group code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean isActive() {
        return active;
    }

    public Group active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isActual() {
        return actual;
    }

    public Group actual(Boolean actual) {
        this.actual = actual;
        return this;
    }

    public void setActual(Boolean actual) {
        this.actual = actual;
    }

    public Context getContext() {
        return context;
    }

    public Group context(Context context) {
        this.context = context;
        return this;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public Group members(List<GroupMember> members) {
        this.members = members;
        return this;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    public Group type(ValueSetCode type) {
        this.type = type;
        return this;
    }

    public ValueSetCode getType() {
        return type;
    }

    public void setType(ValueSetCode type) {
        this.type = type;
    }

    public Organization getPartOf() {
        return partOf;
    }

    public Group partOf(Organization Organization) {
        this.partOf = Organization;
        return this;
    }

    public void setPartOf(Organization Organization) {
        this.partOf = Organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group group = (Group) o;
        if (group.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Group{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", code='" + getCode() + "'" +
            ", active='" + isActive() + "'" +
            ", actual='" + isActual() + "'" +
            ", context='" + getContext() + "'" +
            ", members='" + getMembers() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
