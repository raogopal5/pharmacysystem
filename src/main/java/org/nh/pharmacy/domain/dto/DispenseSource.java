package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class DispenseSource implements Serializable {
    private BigDecimal userDiscountAmount;
    private BigDecimal unitDiscountAmount;
    private BigDecimal taxDiscountAmount;
    private Float returnQuantity;
    private BigDecimal prevUserDiscountAmount;
    private BigDecimal prevUnitDiscountAmount;
    private BigDecimal prevTaxDiscountAmount;

    public BigDecimal getPrevUserDiscountAmount() {
        return prevUserDiscountAmount;
    }

    public void setPrevUserDiscountAmount(BigDecimal prevUserDiscountAmount) {
        this.prevUserDiscountAmount = prevUserDiscountAmount;
    }

    public BigDecimal getPrevUnitDiscountAmount() {
        return prevUnitDiscountAmount;
    }

    public void setPrevUnitDiscountAmount(BigDecimal prevUnitDiscountAmount) {
        this.prevUnitDiscountAmount = prevUnitDiscountAmount;
    }

    public BigDecimal getPrevTaxDiscountAmount() {
        return prevTaxDiscountAmount;
    }

    public void setPrevTaxDiscountAmount(BigDecimal prevTaxDiscountAmount) {
        this.prevTaxDiscountAmount = prevTaxDiscountAmount;
    }

    public Float getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Float returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public BigDecimal getUserDiscountAmount() {
        return userDiscountAmount;
    }

    public void setUserDiscountAmount(BigDecimal userDiscountAmount) {
        this.userDiscountAmount = userDiscountAmount;
    }

    public BigDecimal getUnitDiscountAmount() {
        return unitDiscountAmount;
    }

    public void setUnitDiscountAmount(BigDecimal unitDiscountAmount) {
        this.unitDiscountAmount = unitDiscountAmount;
    }

    public BigDecimal getTaxDiscountAmount() {
        return taxDiscountAmount;
    }

    public void setTaxDiscountAmount(BigDecimal taxDiscountAmount) {
        this.taxDiscountAmount = taxDiscountAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseSource that = (DispenseSource) o;

        if (userDiscountAmount != null ? !userDiscountAmount.equals(that.userDiscountAmount) : that.userDiscountAmount != null)
            return false;
        if (unitDiscountAmount != null ? !unitDiscountAmount.equals(that.unitDiscountAmount) : that.unitDiscountAmount != null)
            return false;
        if (taxDiscountAmount != null ? !taxDiscountAmount.equals(that.taxDiscountAmount) : that.taxDiscountAmount != null)
            return false;
        return returnQuantity != null ? returnQuantity.equals(that.returnQuantity) : that.returnQuantity == null;
    }

    @Override
    public String toString() {
        return "DispenseSource{" +
            "userDiscountAmount=" + userDiscountAmount +
            ", unitDiscountAmount=" + unitDiscountAmount +
            ", taxDiscountAmount=" + taxDiscountAmount +
            ", returnQuantity=" + returnQuantity +
            '}';
    }

    @Override
    public int hashCode() {
        int result = userDiscountAmount != null ? userDiscountAmount.hashCode() : 0;
        result = 31 * result + (unitDiscountAmount != null ? unitDiscountAmount.hashCode() : 0);
        result = 31 * result + (taxDiscountAmount != null ? taxDiscountAmount.hashCode() : 0);
        result = 31 * result + (returnQuantity != null ? returnQuantity.hashCode() : 0);
        return result;
    }

}
