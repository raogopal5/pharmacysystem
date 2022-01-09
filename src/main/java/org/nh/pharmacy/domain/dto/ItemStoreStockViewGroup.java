package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.ItemStoreStockView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirbhay on 6/18/19.
 */
public class ItemStoreStockViewGroup implements Serializable {

    private Long itemId;
    private String itemCode;
    private String itemName;

    List<ItemStoreStockView> itemStoreStockViews = new ArrayList<>();

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<ItemStoreStockView> getItemStoreStockViews() {
        return itemStoreStockViews;
    }

    public void setItemStoreStockViews(List<ItemStoreStockView> itemStoreStockViews) {
        this.itemStoreStockViews = itemStoreStockViews;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemStoreStockViewGroup that = (ItemStoreStockViewGroup) o;

        if (itemId != null ? !itemId.equals(that.itemId) : that.itemId != null) return false;
        if (itemCode != null ? !itemCode.equals(that.itemCode) : that.itemCode != null) return false;
        if (itemName != null ? !itemName.equals(that.itemName) : that.itemName != null) return false;
        return itemStoreStockViews != null ? itemStoreStockViews.equals(that.itemStoreStockViews) : that.itemStoreStockViews == null;
    }

    @Override
    public int hashCode() {
        int result = itemId != null ? itemId.hashCode() : 0;
        result = 31 * result + (itemCode != null ? itemCode.hashCode() : 0);
        result = 31 * result + (itemName != null ? itemName.hashCode() : 0);
        result = 31 * result + (itemStoreStockViews != null ? itemStoreStockViews.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ItemStoreStockViewGroup{" +
            "itemId=" + itemId +
            ", itemCode='" + itemCode + '\'' +
            ", itemName='" + itemName + '\'' +
            ", itemStoreStockViews=" + itemStoreStockViews +
            '}';
    }
}
