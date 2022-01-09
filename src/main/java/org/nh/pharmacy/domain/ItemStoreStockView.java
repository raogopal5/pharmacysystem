package org.nh.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.common.dto.OrganizationDTO;
import org.nh.pharmacy.domain.enumeration.ItemStoreStockViewType;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * A ItemStoreStockView.
 */
@Entity
@Table(name = "item_store_stock_view")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itemstorestockview", type = "itemstorestockview")
@Setting(settingPath = "/es/settings.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemStoreStockView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "code", nullable = false)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemStoreStockViewType type;

    @Column(name = "available_stock")
    private Float availableStock;

    @Column(name = "stocklast_sync_date")
    private LocalDateTime stocklastSyncDate;

    @NotNull
    @Type(type = "jsonb")
    @Column(name = "store", nullable = false)
    private Map<String, Object> store;

    @Column(name = "consumed_qty_curr_month")
    private Float consumedQtyCurrMonth;

    @Column(name = "consumed_qty_last_month")
    private Float consumedQtyLastMonth;

    @Column(name = "transit_qty")
    private Float transitQty;

    @Type(type = "jsonb")
    @Column(name = "unit")
    @Field(type = FieldType.Object, ignoreFields = {"partOf", "unit"})
    private OrganizationDTO unit;

    @Column(name = "item_group")
    private String itemGroup;

    public String getItemGroup() {return itemGroup;}

    public void setItemGroup(String itemGroup) {this.itemGroup = itemGroup;}

    public OrganizationDTO getUnit() {return unit;}

    public void setUnit(OrganizationDTO unit) {this.unit = unit;}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public ItemStoreStockView itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getCode() {
        return code;
    }

    public ItemStoreStockView code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public ItemStoreStockView name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemStoreStockViewType getType() {
        return type;
    }

    public ItemStoreStockView type(ItemStoreStockViewType type) {
        this.type = type;
        return this;
    }

    public void setType(ItemStoreStockViewType type) {
        this.type = type;
    }

    public Float getAvailableStock() {
        return availableStock;
    }

    public ItemStoreStockView availableStock(Float availableStock) {
        this.availableStock = availableStock;
        return this;
    }

    public void setAvailableStock(Float availableStock) {
        this.availableStock = availableStock;
    }

    public LocalDateTime getStocklastSyncDate() {
        return stocklastSyncDate;
    }

    public ItemStoreStockView stocklastSyncDate(LocalDateTime stocklastSyncDate) {
        this.stocklastSyncDate = stocklastSyncDate;
        return this;
    }

    public void setStocklastSyncDate(LocalDateTime stocklastSyncDate) {
        this.stocklastSyncDate = stocklastSyncDate;
    }

    public Map<String, Object> getStore() {
        return store;
    }

    public ItemStoreStockView store(Map<String, Object> store) {
        this.store = store;
        return this;
    }

    public void setStore(Map<String, Object> store) {
        this.store = store;
    }

    public Float getConsumedQtyCurrMonth() {
        return consumedQtyCurrMonth;
    }

    public ItemStoreStockView consumedQtyCurrMonth(Float consumedQtyCurrMonth) {
        this.consumedQtyCurrMonth = consumedQtyCurrMonth;
        return this;
    }

    public void setConsumedQtyCurrMonth(Float consumedQtyCurrMonth) {
        this.consumedQtyCurrMonth = consumedQtyCurrMonth;
    }

    public Float getConsumedQtyLastMonth() {
        return consumedQtyLastMonth;
    }

    public ItemStoreStockView consumedQtyLastMonth(Float consumedQtyLastMonth) {
        this.consumedQtyLastMonth = consumedQtyLastMonth;
        return this;
    }

    public void setConsumedQtyLastMonth(Float consumedQtyLastMonth) {
        this.consumedQtyLastMonth = consumedQtyLastMonth;
    }

    public Float getTransitQty() {
        return transitQty;
    }

    public ItemStoreStockView transitQty(Float transitQty) {
        this.transitQty = transitQty;
        return this;
    }

    public void setTransitQty(Float transitQty) {
        this.transitQty = transitQty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemStoreStockView itemStoreStockView = (ItemStoreStockView) o;
        if (itemStoreStockView.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemStoreStockView.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemStoreStockView{" +
            "id=" + id +
            ", itemId='" + itemId + "'" +
            ", code='" + code + "'" +
            ", name='" + name + "'" +
            ", type='" + type + "'" +
            ", itemGroup='" + itemGroup + "'" +
            ", availableStock='" + availableStock + "'" +
            ", stocklastSyncDate='" + stocklastSyncDate + "'" +
            ", store='" + store + "'" +
            ", unit='" + unit + "'" +
            ", consumedQtyCurrMonth='" + consumedQtyCurrMonth + "'" +
            ", consumedQtyLastMonth='" + consumedQtyLastMonth + "'" +
            ", transitQty='" + transitQty + "'" +
            '}';
    }
}
