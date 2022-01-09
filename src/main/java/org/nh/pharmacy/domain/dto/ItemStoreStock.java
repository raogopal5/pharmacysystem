package org.nh.pharmacy.domain.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Document(indexName = "itemstorestock")
@Setting(settingPath = "/es/settings.json")
public class ItemStoreStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull
    private String id;

    @NotNull
    private Long itemId;

    @NotNull
    private Long storeId;

    @NotNull
    private float stock;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public float getStock() {
        return stock;
    }

    public void setStock(float stock) {
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStoreStock assignId()
    {
        this.id = itemId+"_"+storeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemStoreStock itemStoreStock = (ItemStoreStock) o;

        if (itemStoreStock.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemStoreStock.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ItemStoreStock{" +
            "itemId=" + itemId +
            ", storeId=" + storeId +
            ", stock=" + stock +
            '}';
    }
}
