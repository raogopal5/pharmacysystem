package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.PlanRef;
import org.nh.billing.domain.dto.PlanRuleDetail;
import org.nh.common.dto.OrganizationDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.nh.common.util.BigDecimalUtil;
/**
 * Created by Nirbhay on 6/13/17.
 */
public class DispenseItemPlan implements Serializable {

    private PlanRef planRef;
    private OrganizationDTO sponsorRef;
    private BigDecimal grossAmount;
    private BigDecimal saleAmount;
    private BigDecimal taxAmount;
    private BigDecimal roundOff = BigDecimalUtil.ZERO;
    private BigDecimal sponsorGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorNetAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorTaxAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorDiscAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientNetAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientTaxAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientDiscAmount = BigDecimalUtil.ZERO;
    private List<DispenseTax> planTaxList;
    private PlanRuleDetail planRuleDetail;
    private Float patientTaxPercentage;
    private Float sponsorTaxPercentage;
    private boolean isTaxPayable;

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    private String aliasCode="";
    private String aliasName="";



    public PlanRef getPlanRef() {
        return planRef;
    }

    public void setPlanRef(PlanRef planRef) {
        this.planRef = planRef;
    }

    public OrganizationDTO getSponsorRef() {
        return sponsorRef;
    }

    public void setSponsorRef(OrganizationDTO sponsorRef) {
        this.sponsorRef = sponsorRef;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getRoundOff() {
        return roundOff;
    }

    public void setRoundOff(BigDecimal roundOff) {
        this.roundOff = roundOff;
    }

    public List<DispenseTax> getPlanTaxList() {
        return planTaxList;
    }

    public void setPlanTaxList(List<DispenseTax> planTaxList) {
        this.planTaxList = planTaxList;
    }

    public Float getPatientTaxPercentage() {
        return patientTaxPercentage;
    }

    public void setPatientTaxPercentage(Float patientTaxPercentage) {
        this.patientTaxPercentage = patientTaxPercentage;
    }

    public Float getSponsorTaxPercentage() {
        return sponsorTaxPercentage;
    }

    public void setSponsorTaxPercentage(Float sponsorTaxPercentage) {
        this.sponsorTaxPercentage = sponsorTaxPercentage;
    }

    public PlanRuleDetail getPlanRuleDetail() {return planRuleDetail;}

    public void setPlanRuleDetail(PlanRuleDetail planRuleDetail) {this.planRuleDetail = planRuleDetail;}

    public boolean isTaxPayable() {
        return isTaxPayable;
    }

    public void setTaxPayable(boolean taxPayable) {
        isTaxPayable = taxPayable;
    }

    public BigDecimal getSponsorTaxAmount() {
        return sponsorTaxAmount;
    }

    public void setSponsorTaxAmount(BigDecimal sponsorTaxAmount) {
        this.sponsorTaxAmount = sponsorTaxAmount;
    }

    public BigDecimal getSponsorDiscAmount() {
        return sponsorDiscAmount;
    }

    public void setSponsorDiscAmount(BigDecimal sponsorDiscAmount) {
        this.sponsorDiscAmount = sponsorDiscAmount;
    }

    public BigDecimal getPatientTaxAmount() {
        return patientTaxAmount;
    }

    public void setPatientTaxAmount(BigDecimal patientTaxAmount) {
        this.patientTaxAmount = patientTaxAmount;
    }

    public BigDecimal getPatientDiscAmount() {
        return patientDiscAmount;
    }

    public void setPatientDiscAmount(BigDecimal patientDiscAmount) {
        this.patientDiscAmount = patientDiscAmount;
    }

    public BigDecimal getSponsorGrossAmount() {
        return sponsorGrossAmount;
    }

    public void setSponsorGrossAmount(BigDecimal sponsorGrossAmount) {
        this.sponsorGrossAmount = sponsorGrossAmount;
    }

    public BigDecimal getSponsorNetAmount() {
        return sponsorNetAmount;
    }

    public void setSponsorNetAmount(BigDecimal sponsorNetAmount) {
        this.sponsorNetAmount = sponsorNetAmount;
    }

    public BigDecimal getPatientGrossAmount() {
        return patientGrossAmount;
    }

    public void setPatientGrossAmount(BigDecimal patientGrossAmount) {
        this.patientGrossAmount = patientGrossAmount;
    }

    public BigDecimal getPatientNetAmount() {
        return patientNetAmount;
    }

    public void setPatientNetAmount(BigDecimal patientNetAmount) {
        this.patientNetAmount = patientNetAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseItemPlan that = (DispenseItemPlan) o;

        if (isTaxPayable != that.isTaxPayable) return false;
        if (planRef != null ? !planRef.equals(that.planRef) : that.planRef != null) return false;
        if (sponsorRef != null ? !sponsorRef.equals(that.sponsorRef) : that.sponsorRef != null) return false;
        if (grossAmount != null ? !grossAmount.equals(that.grossAmount) : that.grossAmount != null) return false;
        if (saleAmount != null ? !saleAmount.equals(that.saleAmount) : that.saleAmount != null) return false;
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) return false;
        if (roundOff != null ? !roundOff.equals(that.roundOff) : that.roundOff != null) return false;
        if (sponsorGrossAmount != null ? !sponsorGrossAmount.equals(that.sponsorGrossAmount) : that.sponsorGrossAmount != null)
            return false;
        if (sponsorNetAmount != null ? !sponsorNetAmount.equals(that.sponsorNetAmount) : that.sponsorNetAmount != null)
            return false;
        if (sponsorTaxAmount != null ? !sponsorTaxAmount.equals(that.sponsorTaxAmount) : that.sponsorTaxAmount != null)
            return false;
        if (sponsorDiscAmount != null ? !sponsorDiscAmount.equals(that.sponsorDiscAmount) : that.sponsorDiscAmount != null)
            return false;
        if (patientGrossAmount != null ? !patientGrossAmount.equals(that.patientGrossAmount) : that.patientGrossAmount != null)
            return false;
        if (patientNetAmount != null ? !patientNetAmount.equals(that.patientNetAmount) : that.patientNetAmount != null)
            return false;
        if (patientTaxAmount != null ? !patientTaxAmount.equals(that.patientTaxAmount) : that.patientTaxAmount != null)
            return false;
        if (patientDiscAmount != null ? !patientDiscAmount.equals(that.patientDiscAmount) : that.patientDiscAmount != null)
            return false;
        if (planTaxList != null ? !planTaxList.equals(that.planTaxList) : that.planTaxList != null) return false;
        if (planRuleDetail != null ? !planRuleDetail.equals(that.planRuleDetail) : that.planRuleDetail != null)
            return false;
        if (patientTaxPercentage != null ? !patientTaxPercentage.equals(that.patientTaxPercentage) : that.patientTaxPercentage != null)
            return false;
        return sponsorTaxPercentage != null ? sponsorTaxPercentage.equals(that.sponsorTaxPercentage) : that.sponsorTaxPercentage == null;
    }

    @Override
    public int hashCode() {
        int result = planRef != null ? planRef.hashCode() : 0;
        result = 31 * result + (sponsorRef != null ? sponsorRef.hashCode() : 0);
        result = 31 * result + (grossAmount != null ? grossAmount.hashCode() : 0);
        result = 31 * result + (saleAmount != null ? saleAmount.hashCode() : 0);
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (roundOff != null ? roundOff.hashCode() : 0);
        result = 31 * result + (sponsorGrossAmount != null ? sponsorGrossAmount.hashCode() : 0);
        result = 31 * result + (sponsorNetAmount != null ? sponsorNetAmount.hashCode() : 0);
        result = 31 * result + (sponsorTaxAmount != null ? sponsorTaxAmount.hashCode() : 0);
        result = 31 * result + (sponsorDiscAmount != null ? sponsorDiscAmount.hashCode() : 0);
        result = 31 * result + (patientGrossAmount != null ? patientGrossAmount.hashCode() : 0);
        result = 31 * result + (patientNetAmount != null ? patientNetAmount.hashCode() : 0);
        result = 31 * result + (patientTaxAmount != null ? patientTaxAmount.hashCode() : 0);
        result = 31 * result + (patientDiscAmount != null ? patientDiscAmount.hashCode() : 0);
        result = 31 * result + (planTaxList != null ? planTaxList.hashCode() : 0);
        result = 31 * result + (planRuleDetail != null ? planRuleDetail.hashCode() : 0);
        result = 31 * result + (patientTaxPercentage != null ? patientTaxPercentage.hashCode() : 0);
        result = 31 * result + (sponsorTaxPercentage != null ? sponsorTaxPercentage.hashCode() : 0);
        result = 31 * result + (isTaxPayable ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseItemPlan{" +
            "planRef=" + planRef +
            ", sponsorRef=" + sponsorRef +
            ", grossAmount=" + grossAmount +
            ", saleAmount=" + saleAmount +
            ", taxAmount=" + taxAmount +
            ", roundOff=" + roundOff +
            ", sponsorGrossAmount=" + sponsorGrossAmount +
            ", sponsorNetAmount=" + sponsorNetAmount +
            ", sponsorTaxAmount=" + sponsorTaxAmount +
            ", sponsorDiscAmount=" + sponsorDiscAmount +
            ", patientGrossAmount=" + patientGrossAmount +
            ", patientNetAmount=" + patientNetAmount +
            ", patientTaxAmount=" + patientTaxAmount +
            ", patientDiscAmount=" + patientDiscAmount +
            ", planTaxList=" + planTaxList +
            ", planRuleDetail=" + planRuleDetail +
            ", patientTaxPercentage=" + patientTaxPercentage +
            ", sponsorTaxPercentage=" + sponsorTaxPercentage +
            ", isTaxPayable=" + isTaxPayable +
            '}';
    }

    public void roundOff() {
        int requiredScale = 2;
        setSaleAmount(BigDecimalUtil.roundOff(getSaleAmount(), requiredScale));
        setGrossAmount(BigDecimalUtil.roundOff(getGrossAmount(), requiredScale));
        setTaxAmount(BigDecimalUtil.roundOff(getTaxAmount(), requiredScale));
        setSponsorGrossAmount(BigDecimalUtil.roundOff(getSponsorGrossAmount(), requiredScale));
        setSponsorDiscAmount(BigDecimalUtil.roundOff(getSponsorDiscAmount(), requiredScale));
        setSponsorTaxAmount(BigDecimalUtil.roundOff(getSponsorTaxAmount(), requiredScale));
        setSponsorNetAmount(BigDecimalUtil.roundOff(getSponsorNetAmount(), requiredScale));
        setPatientGrossAmount(BigDecimalUtil.roundOff(getPatientGrossAmount(), requiredScale));
        setPatientDiscAmount(BigDecimalUtil.roundOff(getPatientDiscAmount(), requiredScale));
        setPatientTaxAmount(BigDecimalUtil.roundOff(getPatientTaxAmount(), requiredScale));
        setPatientNetAmount(BigDecimalUtil.roundOff(getPatientNetAmount(), requiredScale));
    }
}
