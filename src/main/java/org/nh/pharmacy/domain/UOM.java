package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.common.dto.UOMDTO;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A UOM.
 */
@Entity
@Table(name = "uom")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "uom", type = "uom", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class UOM implements Serializable {

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

    @Column(name = "conversion_factor")
    private Float conversionFactor;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "uom_type")
    private ValueSetCode uomType;

    @ManyToOne
    private Organization createdFor;

    @ManyToOne
    private UOM baseUOM;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public UOM code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public UOM name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public UOM active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Float getConversionFactor() {
        return conversionFactor;
    }

    public UOM conversionFactor(Float conversionFactor) {
        this.conversionFactor = conversionFactor;
        return this;
    }

    public void setConversionFactor(Float conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public UOM uomType(ValueSetCode uomType) {
        this.uomType = uomType;
        return this;
    }

    public ValueSetCode getUomType() {
        return uomType;
    }

    public void setUomType(ValueSetCode uomType) {
        this.uomType = uomType;
    }

    public Organization getCreatedFor() {
        return createdFor;
    }

    public UOM createdFor(Organization createdFor) {
        this.createdFor = createdFor;
        return this;
    }

    public void setCreatedFor(Organization createdFor) {
        this.createdFor = createdFor;
    }

    public UOM getBaseUOM() {
        return baseUOM;
    }

    public UOM baseUOM(UOM UOM) {
        this.baseUOM = UOM;
        return this;
    }

    public void setBaseUOM(UOM UOM) {
        this.baseUOM = UOM;
    }

    public UOMDTO getUOMDTO() {
        UOMDTO uomDTO = new UOMDTO();
        uomDTO.setId(this.getId());
        uomDTO.setCode(this.getCode());
        uomDTO.setName(this.getName());
        //TODO uncomment & fix, if its required
        //uomDTO.setUomType(uom.getUomType());
        //uomDTO.setBaseUOM(uom.getBaseUOM());
        return uomDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UOM uOM = (UOM) o;
        if (uOM.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, uOM.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UOM{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", active='" + active + "'" +
            ", conversionFactor='" + conversionFactor + "'" +
            ", uomType='" + uomType + "'" +
            '}';
    }
}
