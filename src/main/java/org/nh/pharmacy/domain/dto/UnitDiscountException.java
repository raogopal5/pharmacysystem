package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.common.dto.OrganizationDTO;

import java.io.Serializable;

/**
 * A UnitDiscountException.
 */
public class UnitDiscountException implements Serializable {

    private Long id;
    private Boolean active;
    private ItemDTO item;
    private OrganizationDTO unit;
    private Float discount;

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public OrganizationDTO getUnit() {
        return unit;
    }

    public void setUnit(OrganizationDTO unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitDiscountException that = (UnitDiscountException) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        return unit != null ? unit.equals(that.unit) : that.unit == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UnitDiscountException{" +
            "id=" + id +
            ", active=" + active +
            ", item=" + item +
            ", unit=" + unit +
            '}';
    }
}
