package org.nh.pharmacy.domain;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.AutoStockConsumption;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ItemStoreAutoConsumption.
 */
@Entity

@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itemstoreautoconsumption", type = "itemstoreautoconsumption", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class ItemStoreAutoConsumption implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @NotNull
    private Item item;

    @ManyToOne(optional = false)
    @NotNull
    private HealthcareServiceCenter store;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "auto_consumption", nullable = false)
    private AutoStockConsumption autoConsumption;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public ItemStoreAutoConsumption item(Item item) {
        this.item = item;
        return this;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public HealthcareServiceCenter getStore() {
        return store;
    }

    public ItemStoreAutoConsumption store(HealthcareServiceCenter store) {
        this.store = store;
        return this;
    }

    public void setStore(HealthcareServiceCenter store) {
        this.store = store;
    }

    public AutoStockConsumption getAutoConsumption() {
        return autoConsumption;
    }

    public ItemStoreAutoConsumption autoConsumption(AutoStockConsumption autoConsumption) {
        this.autoConsumption = autoConsumption;
        return this;
    }

    public void setAutoConsumption(AutoStockConsumption autoConsumption) {
        this.autoConsumption = autoConsumption;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ItemStoreAutoConsumption)) {
            return false;
        }
        return id != null && id.equals(((ItemStoreAutoConsumption) obj).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemStoreAutoConsumption{" +
            "id=" + getId() +
            ", item='" + getItem() + "'" +
            ", store='" + getStore() + "'" +
            ", autoConsumption='" + getAutoConsumption() + "'" +
            "}";
    }
}
