package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ItemStoreLocatorMap.
 */
@Entity
@Table(name = "item_store_locator_map", uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id", "health_care_service_center_id", "locator_id"})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itemstorelocatormap", type = "itemstorelocatormap", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class ItemStoreLocatorMap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne(optional = false)
    @NotNull
    private Item item;

    @ManyToOne(optional = false)
    @NotNull
    private HealthcareServiceCenter healthCareServiceCenter;

    @ManyToOne(optional = false)
    @NotNull
    private Locator locator;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isActive() {
        return active;
    }

    public ItemStoreLocatorMap active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Item getItem() {
        return item;
    }

    public ItemStoreLocatorMap item(Item Item) {
        this.item = Item;
        return this;
    }

    public void setItem(Item Item) {
        this.item = Item;
    }

    public HealthcareServiceCenter getHealthCareServiceCenter() {
        return healthCareServiceCenter;
    }

    public ItemStoreLocatorMap healthCareServiceCenter(HealthcareServiceCenter HealthcareServiceCenter) {
        this.healthCareServiceCenter = HealthcareServiceCenter;
        return this;
    }

    public void setHealthCareServiceCenter(HealthcareServiceCenter HealthcareServiceCenter) {
        this.healthCareServiceCenter = HealthcareServiceCenter;
    }

    public Locator getLocator() {
        return locator;
    }

    public ItemStoreLocatorMap locator(Locator Locator) {
        this.locator = Locator;
        return this;
    }

    public void setLocator(Locator Locator) {
        this.locator = Locator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemStoreLocatorMap itemStoreLocatorMap = (ItemStoreLocatorMap) o;
        if (itemStoreLocatorMap.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemStoreLocatorMap.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemStoreLocatorMap{" +
            "id=" + id +
            ", active='" + active + "'" +
            '}';
    }
}
