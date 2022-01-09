package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.UOMDTO;

import java.io.Serializable;

/**
 * Created by Nirbhay on 3/9/17.
 */
public class Quantity implements Serializable {

    private Float value;
    private UOMDTO uom;

    public Quantity() {
    }

    public Quantity(Float value, UOMDTO uom) {
        this.value = value;
        this.uom = uom;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public UOMDTO getUom() {
        return uom;
    }

    public void setUom(UOMDTO uom) {
        this.uom = uom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quantity quantity = (Quantity) o;

        if (value != null ? !value.equals(quantity.value) : quantity.value != null) return false;
        return uom != null ? uom.equals(quantity.uom) : quantity.uom == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (uom != null ? uom.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Quantity{" +
            "value=" + value +
            ", uom=" + uom +
            '}';
    }
}
