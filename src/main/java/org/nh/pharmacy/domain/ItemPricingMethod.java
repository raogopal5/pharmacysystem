package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.PricingMethod;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


/**
 * A ItemPricingMethod.
 */
@Entity
@Table(name = "item_pricing_method")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itempricingmethod", type = "itempricingmethod", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class ItemPricingMethod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_method", nullable = false)
    private PricingMethod pricingMethod;

    @NotNull
    @Column(name = "selling_price", nullable = false)
    private Float sellingPrice;

    @ManyToOne(optional = false)
    @NotNull
    private Organization organization;

    @ManyToOne(optional = false)
    @NotNull
    private Item item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public ItemPricingMethod effectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
        return this;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Boolean isActive() {
        return active;
    }

    public ItemPricingMethod active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public PricingMethod getPricingMethod() {
        return pricingMethod;
    }

    public ItemPricingMethod pricingMethod(PricingMethod pricingMethod) {
        this.pricingMethod = pricingMethod;
        return this;
    }

    public void setPricingMethod(PricingMethod pricingMethod) {
        this.pricingMethod = pricingMethod;
    }

    public Float getSellingPrice() {
        return sellingPrice;
    }

    public ItemPricingMethod sellingPrice(Float sellingPrice) {
        this.sellingPrice = sellingPrice;
        return this;
    }

    public void setSellingPrice(Float sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Organization getOrganization() {
        return organization;
    }

    public ItemPricingMethod organization(Organization Organization) {
        this.organization = Organization;
        return this;
    }

    public void setOrganization(Organization Organization) {
        this.organization = Organization;
    }

    public Item getItem() {
        return item;
    }

    public ItemPricingMethod item(Item Item) {
        this.item = Item;
        return this;
    }

    public void setItem(Item Item) {
        this.item = Item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemPricingMethod itemPricingMethod = (ItemPricingMethod) o;
        if (itemPricingMethod.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemPricingMethod.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemPricingMethod{" +
            "id=" + id +
            ", effectiveFrom='" + effectiveFrom + "'" +
            ", active='" + active + "'" +
            ", pricingMethod='" + pricingMethod + "'" +
            ", sellingPrice='" + sellingPrice + "'" +
            '}';
    }
}
