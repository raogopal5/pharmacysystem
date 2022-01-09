package org.nh.pharmacy.dto;

import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Objects;

@Document(indexName = "valueset")
public class ValueSet {

    private Long id;

    private String code;

    private String name;

    private String definition;

    private Boolean active;

    private String source;

    private String definingURL;

    private String oid;

    private String systemURL;

    private String systemOID;

    public ValueSet id(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public ValueSet code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public ValueSet name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public ValueSet definition(String definition) {
        this.definition = definition;
        return this;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Boolean isActive() {
        return active;
    }

    public ValueSet active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSource() {
        return source;
    }

    public ValueSet source(String source) {
        this.source = source;
        return this;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDefiningURL() {
        return definingURL;
    }

    public ValueSet definingURL(String definingURL) {
        this.definingURL = definingURL;
        return this;
    }

    public void setDefiningURL(String definingURL) {
        this.definingURL = definingURL;
    }

    public String getOid() {
        return oid;
    }

    public ValueSet oid(String oid) {
        this.oid = oid;
        return this;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getSystemURL() {
        return systemURL;
    }

    public ValueSet systemURL(String systemURL) {
        this.systemURL = systemURL;
        return this;
    }

    public void setSystemURL(String systemURL) {
        this.systemURL = systemURL;
    }

    public String getSystemOID() {
        return systemOID;
    }

    public ValueSet systemOID(String systemOID) {
        this.systemOID = systemOID;
        return this;
    }

    public void setSystemOID(String systemOID) {
        this.systemOID = systemOID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueSet valueSet = (ValueSet) o;
        if (valueSet.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, valueSet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ValueSet{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", definition='" + definition + "'" +
            ", active='" + active + "'" +
            ", source='" + source + "'" +
            ", definingURL='" + definingURL + "'" +
            ", oid='" + oid + "'" +
            ", systemURL='" + systemURL + "'" +
            ", systemOID='" + systemOID + "'" +
            '}';
    }
}
