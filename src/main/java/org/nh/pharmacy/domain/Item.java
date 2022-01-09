package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.enumeration.AutoStockConsumption;
import org.nh.pharmacy.domain.enumeration.FSNType;
import org.nh.pharmacy.domain.enumeration.VEDCategory;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Item.
 */
@Entity
@Table(name = "item")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "item", type = "item", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "batch_tracked", nullable = false)
    private Boolean batchTracked;

    @NotNull
    @Column(name = "expiry_date_required", nullable = false)
    private Boolean expiryDateRequired;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "remarks")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "fsn_type")
    private FSNType fsnType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ved_category")
    private VEDCategory vedCategory;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "item_type")
    private ValueSetCode type;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "item_group")
    private ValueSetCode group;

    @ManyToOne(optional = false)
    @NotNull
    private ItemCategory category;

    @ManyToOne
    private Group materialGroup;

    @ManyToOne
    private UOM purchaseUOM;

    @ManyToOne
    private UOM saleUOM;

    @ManyToOne(optional = false)
    @NotNull
    private UOM trackUOM;

    @Column(name = "dispensable_generic_name")
    private String dispensableGenericName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "auto_stock_consumption", nullable = false)
    private AutoStockConsumption autoStockConsumption;

    @Column(name = "generic")
    private String generic;

    public String getGeneric() {return generic;}

    public Item generic(String generic) {
        this.generic = generic;
        return this;
    }

    public void setGeneric(String generic) {this.generic = generic;}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Item code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Item name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Item description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isBatchTracked() {
        return batchTracked;
    }

    public Item batchTracked(Boolean batchTracked) {
        this.batchTracked = batchTracked;
        return this;
    }

    public void setBatchTracked(Boolean batchTracked) {
        this.batchTracked = batchTracked;
    }

    public Boolean isExpiryDateRequired() {
        return expiryDateRequired;
    }

    public Item expiryDateRequired(Boolean expiryDateRequired) {
        this.expiryDateRequired = expiryDateRequired;
        return this;
    }

    public void setExpiryDateRequired(Boolean expiryDateRequired) {
        this.expiryDateRequired = expiryDateRequired;
    }

    public Boolean isActive() {
        return active;
    }

    public Item active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRemarks() {
        return remarks;
    }

    public Item remarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public FSNType getFsnType() {
        return fsnType;
    }

    public Item fsnType(FSNType fsnType) {
        this.fsnType = fsnType;
        return this;
    }

    public void setFsnType(FSNType fsnType) {
        this.fsnType = fsnType;
    }

    public VEDCategory getVedCategory() {
        return vedCategory;
    }

    public Item vedCategory(VEDCategory vedCategory) {
        this.vedCategory = vedCategory;
        return this;
    }

    public void setVedCategory(VEDCategory vedCategory) {
        this.vedCategory = vedCategory;
    }

    public ValueSetCode getType() {
        return type;
    }

    public void setType(ValueSetCode type) {
        this.type = type;
    }

    public ValueSetCode getGroup() {
        return group;
    }

    public void setGroup(ValueSetCode group) {
        this.group = group;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public Item category(ItemCategory ItemCategory) {
        this.category = ItemCategory;
        return this;
    }

    public void setCategory(ItemCategory ItemCategory) {
        this.category = ItemCategory;
    }

    public Group getMaterialGroup() {
        return materialGroup;
    }

    public Item materialGroup(Group Group) {
        this.materialGroup = Group;
        return this;
    }

    public void setMaterialGroup(Group Group) {
        this.materialGroup = Group;
    }

    public UOM getPurchaseUOM() {
        return purchaseUOM;
    }

    public Item purchaseUOM(UOM UOM) {
        this.purchaseUOM = UOM;
        return this;
    }

    public void setPurchaseUOM(UOM UOM) {
        this.purchaseUOM = UOM;
    }

    public UOM getSaleUOM() {
        return saleUOM;
    }

    public Item saleUOM(UOM UOM) {
        this.saleUOM = UOM;
        return this;
    }

    public void setSaleUOM(UOM UOM) {
        this.saleUOM = UOM;
    }

    public UOM getTrackUOM() {
        return trackUOM;
    }

    public Item trackUOM(UOM UOM) {
        this.trackUOM = UOM;
        return this;
    }

    public String getDispensableGenericName() {
        return dispensableGenericName;
    }

    public void setDispensableGenericName(String dispensableGenericName) {
        this.dispensableGenericName = dispensableGenericName;
    }

    public void setTrackUOM(UOM UOM) {
        this.trackUOM = UOM;
    }

    public AutoStockConsumption getAutoStockConsumption() {
        return autoStockConsumption;
    }

    public void setAutoStockConsumption(AutoStockConsumption autoStockConsumption) {
        this.autoStockConsumption = autoStockConsumption;
    }

    public Item autoStockConsumption(AutoStockConsumption autoStockConsumption){
        this.autoStockConsumption = autoStockConsumption;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        if (item.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Item{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", batchTracked=" + batchTracked +
            ", expiryDateRequired=" + expiryDateRequired +
            ", active=" + active +
            ", remarks='" + remarks + '\'' +
            ", fsnType=" + fsnType +
            ", vedCategory=" + vedCategory +
            ", type=" + type +
            ", group=" + group +
            ", category=" + category +
            ", materialGroup=" + materialGroup +
            ", purchaseUOM=" + purchaseUOM +
            ", saleUOM=" + saleUOM +
            ", trackUOM=" + trackUOM +
            ", dispensableGenericName='" + dispensableGenericName + '\'' +
            ", autoStockConsumption=" + autoStockConsumption +
            '}';
    }
}
