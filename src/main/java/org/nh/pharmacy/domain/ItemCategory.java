package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.common.dto.ItemCategoryDTO;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ItemCategory.
 */
@Entity
@Table(name = "item_category")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itemcategory", type = "itemcategory", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class ItemCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "is_group", nullable = false)
    private Boolean group;

    @ManyToOne
    private ItemCategory partOf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public ItemCategory code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public ItemCategory description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActive() {
        return active;
    }

    public ItemCategory active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isGroup() {
        return group;
    }

    public ItemCategory group(Boolean group) {
        this.group = group;
        return this;
    }

    public void setGroup(Boolean group) {
        this.group = group;
    }

    public ItemCategory getPartOf() {
        return partOf;
    }

    public ItemCategory partOf(ItemCategory ItemCategory) {
        this.partOf = ItemCategory;
        return this;
    }

    public void setPartOf(ItemCategory ItemCategory) {
        this.partOf = ItemCategory;
    }


    public ItemCategoryDTO getItemCategoryDTO() {
        ItemCategoryDTO itemCategoryDTO = new ItemCategoryDTO();
        itemCategoryDTO.setId(this.getId());
        itemCategoryDTO.setCode(this.getCode());
        itemCategoryDTO.setActive(this.isActive());
        itemCategoryDTO.setGroup(this.isGroup());
        itemCategoryDTO.setDescription(this.getDescription());
        return itemCategoryDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemCategory itemCategory = (ItemCategory) o;
        if (itemCategory.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemCategory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemCategory{" +
            "id=" + id +
            ", code='" + code + "'" +
            ", description='" + description + "'" +
            ", active='" + active + "'" +
            ", group='" + group + "'" +
            '}';
    }
}
