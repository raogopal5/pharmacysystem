package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class DispenseReturnDetails implements Serializable {
    private Float quantity;
    private BigDecimal mrp;
    private BigDecimal saleAmount;
    private BigDecimal totalMrp;
    private BigDecimal saleRate;
    private BigDecimal grossAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal patientSaleAmount;
    private BigDecimal patientDiscountAmount;
    private BigDecimal patientGrossAmount;
    private BigDecimal patientTaxAmount;
    private BigDecimal sponsorSaleAmount;
    private BigDecimal sponsorDiscountAmount;
    private BigDecimal sponsorGrossAmount;
    private BigDecimal sponsorTaxAmount;
    private BigDecimal userDiscountAmount;
    private BigDecimal unitDiscountAmount;
    private BigDecimal planDiscountAmount;
    private Float totalTaxPercentage;
    private BigDecimal taxDiscountAmount;

    public BigDecimal getTaxDiscountAmount() {
        return taxDiscountAmount;
    }

    public void setTaxDiscountAmount(BigDecimal taxDiscountAmount) {
        this.taxDiscountAmount = taxDiscountAmount;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getTotalMrp() {
        return totalMrp;
    }

    public void setTotalMrp(BigDecimal totalMrp) {
        this.totalMrp = totalMrp;
    }

    public BigDecimal getSaleRate() {
        return saleRate;
    }

    public void setSaleRate(BigDecimal saleRate) {
        this.saleRate = saleRate;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public BigDecimal getPatientSaleAmount() {
        return patientSaleAmount;
    }

    public void setPatientSaleAmount(BigDecimal patientSaleAmount) {
        this.patientSaleAmount = patientSaleAmount;
    }

    public BigDecimal getPatientDiscountAmount() {
        return patientDiscountAmount;
    }

    public void setPatientDiscountAmount(BigDecimal patientDiscountAmount) {
        this.patientDiscountAmount = patientDiscountAmount;
    }

    public BigDecimal getPatientGrossAmount() {
        return patientGrossAmount;
    }

    public void setPatientGrossAmount(BigDecimal patientGrossAmount) {
        this.patientGrossAmount = patientGrossAmount;
    }

    public BigDecimal getPatientTaxAmount() {
        return patientTaxAmount;
    }

    public void setPatientTaxAmount(BigDecimal patientTaxAmount) {
        this.patientTaxAmount = patientTaxAmount;
    }

    public BigDecimal getSponsorSaleAmount() {
        return sponsorSaleAmount;
    }

    public void setSponsorSaleAmount(BigDecimal sponsorSaleAmount) {
        this.sponsorSaleAmount = sponsorSaleAmount;
    }

    public BigDecimal getSponsorDiscountAmount() {
        return sponsorDiscountAmount;
    }

    public void setSponsorDiscountAmount(BigDecimal sponsorDiscountAmount) {
        this.sponsorDiscountAmount = sponsorDiscountAmount;
    }

    public BigDecimal getSponsorGrossAmount() {
        return sponsorGrossAmount;
    }

    public void setSponsorGrossAmount(BigDecimal sponsorGrossAmount) {
        this.sponsorGrossAmount = sponsorGrossAmount;
    }

    public BigDecimal getSponsorTaxAmount() {
        return sponsorTaxAmount;
    }

    public void setSponsorTaxAmount(BigDecimal sponsorTaxAmount) {
        this.sponsorTaxAmount = sponsorTaxAmount;
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

    public void setUnitDiscountAmount(BigDecimal initDiscountAmount) {
        this.unitDiscountAmount = initDiscountAmount;
    }

    public BigDecimal getPlanDiscountAmount() {
        return planDiscountAmount;
    }

    public void setPlanDiscountAmount(BigDecimal planDiscountAmount) {
        this.planDiscountAmount = planDiscountAmount;
    }

    public Float getTotalTaxPercentage() {
        return totalTaxPercentage;
    }

    public void setTotalTaxPercentage(Float totalTaxPercentage) {
        this.totalTaxPercentage = totalTaxPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispenseReturnDetails that = (DispenseReturnDetails) o;
        return Objects.equals(quantity, that.quantity) &&
            Objects.equals(mrp, that.mrp) &&
            Objects.equals(saleAmount, that.saleAmount) &&
            Objects.equals(totalMrp, that.totalMrp) &&
            Objects.equals(saleRate, that.saleRate) &&
            Objects.equals(grossAmount, that.grossAmount) &&
            Objects.equals(taxAmount, that.taxAmount) &&
            Objects.equals(netAmount, that.netAmount) &&
            Objects.equals(totalDiscountAmount, that.totalDiscountAmount) &&
            Objects.equals(patientSaleAmount, that.patientSaleAmount) &&
            Objects.equals(patientDiscountAmount, that.patientDiscountAmount) &&
            Objects.equals(patientGrossAmount, that.patientGrossAmount) &&
            Objects.equals(patientTaxAmount, that.patientTaxAmount) &&
            Objects.equals(sponsorSaleAmount, that.sponsorSaleAmount) &&
            Objects.equals(sponsorDiscountAmount, that.sponsorDiscountAmount) &&
            Objects.equals(sponsorGrossAmount, that.sponsorGrossAmount) &&
            Objects.equals(sponsorTaxAmount, that.sponsorTaxAmount) &&
            Objects.equals(userDiscountAmount, that.userDiscountAmount) &&
            Objects.equals(unitDiscountAmount, that.unitDiscountAmount) &&
            Objects.equals(planDiscountAmount, that.planDiscountAmount) &&
            Objects.equals(totalTaxPercentage, that.totalTaxPercentage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(quantity, mrp, saleAmount, totalMrp, saleRate, grossAmount, taxAmount, netAmount, totalDiscountAmount, patientSaleAmount, patientDiscountAmount, patientGrossAmount, patientTaxAmount, sponsorSaleAmount, sponsorDiscountAmount, sponsorGrossAmount, sponsorTaxAmount, userDiscountAmount, unitDiscountAmount, planDiscountAmount, totalTaxPercentage);
    }

    @Override
    public String toString() {
        return "DispenseReturnDetails{" +
            "quantity=" + quantity +
            ", mrp=" + mrp +
            ", saleAmount=" + saleAmount +
            ", totalMrp=" + totalMrp +
            ", saleRate=" + saleRate +
            ", grossAmount=" + grossAmount +
            ", taxAmount=" + taxAmount +
            ", netAmount=" + netAmount +
            ", totalDiscountAmount=" + totalDiscountAmount +
            ", patientSaleAmount=" + patientSaleAmount +
            ", patientDiscountAmount=" + patientDiscountAmount +
            ", patientGrossAmount=" + patientGrossAmount +
            ", patientTaxAmount=" + patientTaxAmount +
            ", sponsorSaleAmount=" + sponsorSaleAmount +
            ", sponsorDiscountAmount=" + sponsorDiscountAmount +
            ", sponsorGrossAmount=" + sponsorGrossAmount +
            ", sponsorTaxAmount=" + sponsorTaxAmount +
            ", userDiscountAmount=" + userDiscountAmount +
            ", initDiscountAmount=" + unitDiscountAmount +
            ", planDiscountAmount=" + planDiscountAmount +
            ", totalTaxPercentage=" + totalTaxPercentage +
            '}';
    }
}
