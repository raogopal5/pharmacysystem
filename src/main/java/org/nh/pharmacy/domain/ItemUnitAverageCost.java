package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A ItemUnitAverageCost.
 */
@Entity
@Table(name = "item_unit_average_cost")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemUnitAverageCost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @NotNull
    @Column(name = "average_cost", nullable = false)
    private BigDecimal averageCost;

    @Transient
    private BigDecimal stockValue;

    @Transient
    private Float stockQuantity;


    public ItemUnitAverageCost() {
    }

    public ItemUnitAverageCost(Long unitId, Long itemId, BigDecimal stockValue, Double stockQuantity) {
        this.unitId = unitId;
        this.itemId = itemId;
        this.stockValue = stockValue;
        this.stockQuantity = stockQuantity.floatValue();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUnitId() {
        return unitId;
    }

    public ItemUnitAverageCost unitId(Long unitId) {
        this.unitId = unitId;
        return this;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getItemId() {
        return itemId;
    }

    public ItemUnitAverageCost itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public ItemUnitAverageCost averageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        return this;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }


    public BigDecimal getStockValue() {
        return stockValue;
    }

    public ItemUnitAverageCost stockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
        return this;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public Float getStockQuantity() {
        return stockQuantity;
    }

    public ItemUnitAverageCost stockQuantity(Float stockQuantity) {
        this.stockQuantity = stockQuantity;
        return this;
    }

    public void setStockQuantity(Float stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemUnitAverageCost itemUnitAverageCost = (ItemUnitAverageCost) o;
        if (itemUnitAverageCost.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, itemUnitAverageCost.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ItemUnitAverageCost{" +
            "id=" + id +
            ", unitId=" + unitId +
            ", itemId=" + itemId +
            ", averageCost=" + averageCost +
            ", stockValue=" + stockValue +
            ", stockQuantity=" + stockQuantity +
            '}';
    }
}

