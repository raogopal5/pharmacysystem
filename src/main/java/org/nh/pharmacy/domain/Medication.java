package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.enumeration.DrugSchedule;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Medication.
 */
@Entity
@Table(name = "medication")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "medication", type = "medication", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Medication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "brand", nullable = false)
    private Boolean brand;

    @Column(name = "drug_strength")
    private String drugStrength;

    @Column(name = "manufacturer")
    private String manufacturer;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active=Boolean.TRUE;

    @NotNull
    @Column(name = "authorization_required", nullable = false)
    private Boolean authorizationRequired;

    @NotNull
    @Column(name = "narcotic", nullable = false)
    private Boolean narcotic;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "drug_form")
    private ValueSetCode drugForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "drug_schedule")
    private DrugSchedule drugSchedule;

    @ManyToOne
    private UOM dispensingUom;

    @ManyToOne
    private Medication generic;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "medication_ingredients",
        joinColumns = @JoinColumn(name = "medications_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "ingredients_id", referencedColumnName = "id"))
    private Set<Ingredient> ingredients = new HashSet<>();

    @ManyToOne
    private Organization createdFor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Medication code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Medication name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isBrand() {
        return brand;
    }

    public Medication brand(Boolean brand) {
        this.brand = brand;
        return this;
    }

    public void setBrand(Boolean brand) {
        this.brand = brand;
    }

    public String getDrugStrength() {
        return drugStrength;
    }

    public Medication drugStrength(String drugStrength) {
        this.drugStrength = drugStrength;
        return this;
    }

    public void setDrugStrength(String drugStrength) {
        this.drugStrength = drugStrength;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Medication manufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Boolean isActive() {
        return active;
    }

    public Medication active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isAuthorizationRequired() {
        return authorizationRequired;
    }

    public Medication authorizationRequired(Boolean authorizationRequired) {
        this.authorizationRequired = authorizationRequired;
        return this;
    }

    public void setAuthorizationRequired(Boolean authorizationRequired) {
        this.authorizationRequired = authorizationRequired;
    }

    public Boolean isNarcotic() {
        return narcotic;
    }

    public Medication narcotic(Boolean narcotic) {
        this.narcotic = narcotic;
        return this;
    }

    public void setNarcotic(Boolean narcotic) {
        this.narcotic = narcotic;
    }

    public ValueSetCode getDrugForm() {
        return drugForm;
    }

    public Medication drugForm(ValueSetCode drugForm) {
        this.drugForm = drugForm;
        return this;
    }

    public void setDrugForm(ValueSetCode drugForm) {
        this.drugForm = drugForm;
    }

    public DrugSchedule getDrugSchedule() {
        return drugSchedule;
    }

    public Medication drugSchedule(DrugSchedule drugSchedule) {
        this.drugSchedule = drugSchedule;
        return this;
    }

    public void setDrugSchedule(DrugSchedule drugSchedule) {
        this.drugSchedule = drugSchedule;
    }

    public UOM getDispensingUom() {
        return dispensingUom;
    }

    public Medication dispensingUom(UOM UOM) {
        this.dispensingUom = UOM;
        return this;
    }

    public void setDispensingUom(UOM UOM) {
        this.dispensingUom = UOM;
    }

    public Medication getGeneric() {
        return generic;
    }

    public Medication generic(Medication Medication) {
        this.generic = Medication;
        return this;
    }

    public void setGeneric(Medication Medication) {
        this.generic = Medication;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public Medication ingredients(Set<Ingredient> Ingredients) {
        this.ingredients = Ingredients;
        return this;
    }

    public Medication addIngredients(Ingredient Ingredient) {
        this.ingredients.add(Ingredient);
        return this;
    }

    public Medication removeIngredients(Ingredient Ingredient) {
        this.ingredients.remove(Ingredient);
        return this;
    }

    public void setIngredients(Set<Ingredient> Ingredients) {
        this.ingredients = Ingredients;
    }

    public Organization getCreatedFor() {
        return createdFor;
    }

    public Medication createdFor(Organization Organization) {
        this.createdFor = Organization;
        return this;
    }

    public void setCreatedFor(Organization Organization) {
        this.createdFor = Organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Medication medication = (Medication) o;
        if (medication.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, medication.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Medication{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", brand='" + brand + "'" +
            ", drugStrength='" + drugStrength + "'" +
            ", manufacturer='" + manufacturer + "'" +
            ", active='" + active + "'" +
            ", authorizationRequired='" + authorizationRequired + "'" +
            ", narcotic='" + narcotic + "'" +
            ", drugForm='" + drugForm + "'" +
            ", drugSchedule='" + drugSchedule + "'" +
            '}';
    }
}
