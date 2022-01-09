package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.PlanRule;
import org.nh.billing.domain.dto.PlanRef;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.util.BigDecimalUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirbhay on 6/7/17.
 */
public class DispensePlan implements Serializable {

    private PlanRef planRef;
    private OrganizationDTO sponsorRef;
    private BigDecimal sponsorGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorDiscount = BigDecimalUtil.ZERO;
    private BigDecimal roundOff = BigDecimalUtil.ZERO;
    private BigDecimal sponsorPayable = BigDecimalUtil.ZERO;
    private BigDecimal patientDiscount = BigDecimalUtil.ZERO;
    private List<DispenseTax> planTaxList;
    private PlanRule planRule;
    private BigDecimal totalTax = BigDecimalUtil.ZERO;
    private String policyNo="";

    public String getPolicyNo() {return policyNo;}

    public void setPolicyNo(String policyNo) {this.policyNo = policyNo;}

    public BigDecimal getTotalTax() {return totalTax;}

    public void setTotalTax(BigDecimal totalTax) {this.totalTax = totalTax;}

    public PlanRule getPlanRule() {
        return planRule;
    }

    public void setPlanRule(PlanRule planRule) {
        this.planRule = planRule;
    }

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

    public BigDecimal getSponsorGrossAmount() {
        return sponsorGrossAmount;
    }

    public void setSponsorGrossAmount(BigDecimal sponsorGrossAmount) {
        this.sponsorGrossAmount = sponsorGrossAmount;
    }

    public BigDecimal getSponsorDiscount() {
        return sponsorDiscount;
    }

    public void setSponsorDiscount(BigDecimal sponsorDiscount) {
        this.sponsorDiscount = sponsorDiscount;
    }

    public BigDecimal getRoundOff() {
        return roundOff;
    }

    public void setRoundOff(BigDecimal roundOff) {
        this.roundOff = roundOff;
    }

    public BigDecimal getSponsorPayable() {
        return sponsorPayable;
    }

    public void setSponsorPayable(BigDecimal sponsorPayable) {
        this.sponsorPayable = sponsorPayable;
    }

    public BigDecimal getPatientDiscount() {
        return patientDiscount;
    }

    public void setPatientDiscount(BigDecimal patientDiscount) {
        this.patientDiscount = patientDiscount;
    }

    public List<DispenseTax> getPlanTaxList() {
        return planTaxList;
    }

    public void setPlanTaxList(List<DispenseTax> planTaxList) {
        this.planTaxList = planTaxList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispensePlan that = (DispensePlan) o;

        if (planRef != null ? !planRef.equals(that.planRef) : that.planRef != null) return false;
        if (sponsorRef != null ? !sponsorRef.equals(that.sponsorRef) : that.sponsorRef != null) return false;
        if (sponsorGrossAmount != null ? !sponsorGrossAmount.equals(that.sponsorGrossAmount) : that.sponsorGrossAmount != null)
            return false;
        if (sponsorDiscount != null ? !sponsorDiscount.equals(that.sponsorDiscount) : that.sponsorDiscount != null)
            return false;
        if (roundOff != null ? !roundOff.equals(that.roundOff) : that.roundOff != null) return false;
        if (sponsorPayable != null ? !sponsorPayable.equals(that.sponsorPayable) : that.sponsorPayable != null)
            return false;
        if (patientDiscount != null ? !patientDiscount.equals(that.patientDiscount) : that.patientDiscount != null)
            return false;
        return planTaxList != null ? planTaxList.equals(that.planTaxList) : that.planTaxList == null;
    }

    @Override
    public int hashCode() {
        int result = planRef != null ? planRef.hashCode() : 0;
        result = 31 * result + (sponsorRef != null ? sponsorRef.hashCode() : 0);
        result = 31 * result + (sponsorGrossAmount != null ? sponsorGrossAmount.hashCode() : 0);
        result = 31 * result + (sponsorDiscount != null ? sponsorDiscount.hashCode() : 0);
        result = 31 * result + (roundOff != null ? roundOff.hashCode() : 0);
        result = 31 * result + (sponsorPayable != null ? sponsorPayable.hashCode() : 0);
        result = 31 * result + (patientDiscount != null ? patientDiscount.hashCode() : 0);
        result = 31 * result + (planTaxList != null ? planTaxList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispensePlan{" +
            "planRef=" + planRef +
            ", sponsorRef=" + sponsorRef +
            ", sponsorGrossAmount=" + sponsorGrossAmount +
            ", sponsorDiscount=" + sponsorDiscount +
            ", roundOff=" + roundOff +
            ", sponsorPayable=" + sponsorPayable +
            ", patientDiscount=" + patientDiscount +
            ", totalTax=" + totalTax +
            ", planRule=" + planRule +
            ", planTaxList=" + planTaxList +
            '}';
    }

    public void reset() {
        setRoundOff(BigDecimalUtil.ZERO);
        setSponsorPayable(BigDecimalUtil.ZERO);
        setTotalTax(BigDecimalUtil.ZERO);
        setPatientDiscount(BigDecimalUtil.ZERO);
        setSponsorDiscount(BigDecimalUtil.ZERO);
        setSponsorGrossAmount(BigDecimalUtil.ZERO);
        setPlanTaxList(new ArrayList<>());
    }
}
