package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.TaxCalculation;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.util.CalculateTaxUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.nh.common.util.BigDecimalUtil.*;

/**
 * Created by prashanth on 7/5/17.
 */
public class DispenseReturnDocumentLine extends  DispenseDocumentLine {

    private BigDecimal returnAmount;
    private Boolean sustitute;
    private Float orderedQuantity;
    private Float issuedQuantity;
    private Float prevReturnQuantity;
    private BigDecimal discountAmount;
    private BigDecimal patientAmount;
    private BigDecimal sponsorAmount;
    private List<DispenseItemPlan> itemPlans;
    private List<DispenseTax> itemTaxes;
    private BigDecimal planDiscountAmount;
    private DispenseSource dispenseSource;
    private DispenseReturnDetails dispenseReturnDetails;
    private Float requestedReturnQuantity=0f;
    private LocalDateTime dispenseDate;

    public DispenseReturnDetails getDispenseReturnDetails() {
        return dispenseReturnDetails;
    }

    public void setDispenseReturnDetails(DispenseReturnDetails dispenseReturnDetails) {
        this.dispenseReturnDetails = dispenseReturnDetails;
    }

    public DispenseSource getDispenseSource() {
        return dispenseSource;
    }

    public void setDispenseSource(DispenseSource dispenseSource) {
        this.dispenseSource = dispenseSource;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public Boolean getSustitute() {
        return sustitute;
    }

    public void setSustitute(Boolean sustitute) {
        this.sustitute = sustitute;
    }

    public Float getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Float orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public Float getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(Float issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public Float getPrevReturnQuantity() {
        return prevReturnQuantity;
    }

    public void setPrevReturnQuantity(Float prevReturnQuantity) {
        this.prevReturnQuantity = prevReturnQuantity;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPatientAmount() {
        return patientAmount;
    }

    public void setPatientAmount(BigDecimal patientAmount) {
        this.patientAmount = patientAmount;
    }

    public BigDecimal getSponsorAmount() {
        return sponsorAmount;
    }

    public void setSponsorAmount(BigDecimal sponsorAmount) {
        this.sponsorAmount = sponsorAmount;
    }

    public List<DispenseItemPlan> getItemPlans() {
        return itemPlans;
    }

    public void setItemPlans(List<DispenseItemPlan> itemPlans) {
        this.itemPlans = itemPlans;
    }

    public List<DispenseTax> getItemTaxes() {
        return itemTaxes;
    }

    public void setItemTaxes(List<DispenseTax> itemTaxes) {
        this.itemTaxes = itemTaxes;
    }

    public BigDecimal getPlanDiscountAmount() {
        return planDiscountAmount;
    }

    public void setPlanDiscountAmount(BigDecimal planDiscountAmount) {
        this.planDiscountAmount = planDiscountAmount;
    }

    public Float getRequestedReturnQuantity() {
        return requestedReturnQuantity;
    }

    public void setRequestedReturnQuantity(Float requestedReturnQuantity) {
        this.requestedReturnQuantity = requestedReturnQuantity;
    }

    public LocalDateTime getDispenseDate() {
        return dispenseDate;
    }

    public void setDispenseDate(LocalDateTime dispenseDate) {
        this.dispenseDate = dispenseDate;
    }

    /**
     * Split total tax amount for given tax definitions
     */
    @Deprecated
    public void splitTaxAmountBasedOnTaxDefinition(){
        getDispenseTaxes().forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            BigDecimal taxAmount = multiply(getTaxAmount(), (taxCalculation.getPercentage() / getTotalTaxInPercent()));
            dispenseTax.setTaxAmount(taxAmount);
        });
    }

    public void round() {
//        this.setMrp(roundOff(this.getMrp(), 2));
        this.setTotalMrp(roundOff(this.getTotalMrp(), 2));
        this.setUserDiscount(roundOff(this.getUserDiscount(), 2));
        this.setPlanDiscAmount(roundOff(this.getPlanDiscAmount(), 2));
        this.setUnitDiscount(roundOff(this.getUnitDiscount(), 2));
        this.setNetAmount(roundOff(this.getNetAmount(), 2));
        this.setSaleRate(roundOff(this.getSaleRate(), 2));
        this.setSaleAmount(roundOff(this.getSaleAmount(), 2));
        if(this.getDispenseTaxes() != null && !this.getDispenseTaxes().isEmpty()) {
            this.getDispenseTaxes().forEach(tax -> {
                tax.setTaxAmount(roundOff(tax.getTaxAmount(), 2));
                tax.setPatientTaxAmount(roundOff(tax.getPatientTaxAmount(), 2));
            });
        }
        if(this.getDispenseItemPlans() != null && !this.getDispenseItemPlans().isEmpty()){
            this.getDispenseItemPlans().forEach(itemPlan -> {
                itemPlan.setGrossAmount(roundOff(itemPlan.getGrossAmount(), 2));
                itemPlan.setSaleAmount(roundOff(itemPlan.getSaleAmount(), 2));
                itemPlan.setTaxAmount(roundOff(itemPlan.getTaxAmount(), 2));
                itemPlan.setSponsorGrossAmount(roundOff(itemPlan.getSponsorGrossAmount(), 2));
                itemPlan.setSponsorNetAmount(roundOff(itemPlan.getSponsorNetAmount(), 2));
                itemPlan.setSponsorTaxAmount(roundOff(itemPlan.getSponsorTaxAmount(), 2));
                itemPlan.setSponsorDiscAmount(roundOff(itemPlan.getSponsorDiscAmount(), 2));
                itemPlan.setPatientGrossAmount(roundOff(itemPlan.getPatientGrossAmount(), 2));
                itemPlan.setPatientNetAmount(roundOff(itemPlan.getPatientNetAmount(), 2));
                itemPlan.setPatientTaxAmount(roundOff(itemPlan.getPatientTaxAmount(), 2));
                itemPlan.setPatientDiscAmount(roundOff(itemPlan.getPatientDiscAmount(), 2));
            });
        }
    }

    public void resetReturnLine() {
        setNetAmount(BigDecimalUtil.ZERO);
        setGrossAmount(BigDecimalUtil.ZERO);
        setPatientSaleAmount(BigDecimalUtil.ZERO);
        setPatientGrossAmount(BigDecimalUtil.ZERO);
        setPatientNetAmount(BigDecimalUtil.ZERO);
        setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
        setPatientTotalTaxAmount(BigDecimalUtil.ZERO);
        setPatientTaxAmount(BigDecimalUtil.ZERO);
        setSponsorGrossAmount(BigDecimalUtil.ZERO);
        setSponsorNetAmount(BigDecimalUtil.ZERO);
        setSponsorSaleAmount(BigDecimalUtil.ZERO);
        setSponsorTaxAmount(BigDecimalUtil.ZERO);
        setSponsorDiscAmount(BigDecimalUtil.ZERO);
        setTotalSponsorAmount(BigDecimalUtil.ZERO);
        setTotalDiscountAmount(BigDecimalUtil.ZERO);
        setTaxDiscountAmount(BigDecimalUtil.ZERO);
        setPlanDiscAmount(BigDecimalUtil.ZERO);
        if (getDispenseItemPlans() != null) {
            getDispenseItemPlans().forEach(dispenseItemPlan -> {
                dispenseItemPlan.setRoundOff(BigDecimalUtil.ZERO);
                dispenseItemPlan.setPatientNetAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setPatientTaxAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setPatientGrossAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setPatientDiscAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setPatientTaxPercentage(0f);
                dispenseItemPlan.setSponsorNetAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setSponsorTaxAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setSponsorDiscAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setSponsorGrossAmount(BigDecimalUtil.ZERO);
                dispenseItemPlan.setSponsorTaxPercentage(0f);
            });
        }
        if(getDispenseTaxes() != null) {
            getDispenseTaxes().forEach(dispenseTax -> {
                dispenseTax.setTaxAmount(BigDecimalUtil.ZERO);
                dispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            });
        }
    }

    public void resetLine(DispenseReturnDocumentLine line){
        line.setNetAmount(BigDecimalUtil.ZERO);
        line.setGrossAmount(BigDecimalUtil.ZERO);
        line.setPatientSaleAmount(BigDecimalUtil.ZERO);
        line.setPatientGrossAmount(BigDecimalUtil.ZERO);
        line.setPatientNetAmount(BigDecimalUtil.ZERO);
       // line.setUserDiscount(BigDecimalUtil.ZERO);
        line.setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
        line.setPatientTotalTaxAmount(BigDecimalUtil.ZERO);
        line.setPatientTaxAmount(BigDecimalUtil.ZERO);
        line.setSponsorGrossAmount(BigDecimalUtil.ZERO);
        line.setSponsorNetAmount(BigDecimalUtil.ZERO);
        line.setSponsorSaleAmount(BigDecimalUtil.ZERO);
        line.setSponsorTaxAmount(BigDecimalUtil.ZERO);
        line.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        line.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        line.setTotalDiscountAmount(BigDecimalUtil.ZERO);
        line.setSponsorDiscAmount(BigDecimalUtil.ZERO);
        line.setTaxDiscountAmount(BigDecimalUtil.ZERO);
        line.setPlanDiscAmount(BigDecimalUtil.ZERO);
        line.setDispenseItemPlans(new ArrayList<>());
        if(line.getDispenseTaxes() != null) {
            line.getDispenseTaxes().forEach(dispenseTax -> {
                dispenseTax.setTaxAmount(BigDecimalUtil.ZERO);
                dispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DispenseReturnDocumentLine that = (DispenseReturnDocumentLine) o;
        return Objects.equals(returnAmount, that.returnAmount) &&
            Objects.equals(sustitute, that.sustitute) &&
            Objects.equals(orderedQuantity, that.orderedQuantity) &&
            Objects.equals(issuedQuantity, that.issuedQuantity) &&
            Objects.equals(prevReturnQuantity, that.prevReturnQuantity) &&
            Objects.equals(discountAmount, that.discountAmount) &&
            Objects.equals(patientAmount, that.patientAmount) &&
            Objects.equals(sponsorAmount, that.sponsorAmount) &&
            Objects.equals(itemPlans, that.itemPlans) &&
            Objects.equals(itemTaxes, that.itemTaxes) &&
            Objects.equals(planDiscountAmount, that.planDiscountAmount) &&
            Objects.equals(dispenseSource, that.dispenseSource) &&
            Objects.equals(dispenseReturnDetails, that.dispenseReturnDetails) &&
            Objects.equals(requestedReturnQuantity, that.requestedReturnQuantity) &&
            Objects.equals(dispenseDate, that.dispenseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), returnAmount, sustitute, orderedQuantity, issuedQuantity, prevReturnQuantity, discountAmount, patientAmount, sponsorAmount, itemPlans, itemTaxes, planDiscountAmount, dispenseSource, dispenseReturnDetails, requestedReturnQuantity, dispenseDate);
    }

    @Override
    public String toString() {
        return "DispenseReturnDocumentLine{" +
            "returnAmount=" + returnAmount +
            ", lineNumber=" + getLineNumber() +
            ", itemId=" + getItemId() +
            ", medicationId=" + getMedicationId() +
            ", code='" + getCode() + '\'' +
            ", name='" + getName() + '\'' +
            ", batchNumber='" + getBatchNumber() + '\'' +
            ", expiryDate=" + getExpiryDate() +
            ", instruction='" + getInstruction() + '\'' +
            ", note='" + getNote() + '\'' +
            ", sustitute=" + sustitute +
            ", orderedQuantity=" + orderedQuantity +
            ", issuedQuantity=" + issuedQuantity +
            ", prevReturnQuantity=" + prevReturnQuantity +
            ", quantity=" + getQuantity() +
            ", sku='" + getSku() + '\'' +
            ", mrp=" + this.getMrp() +
            ", cost=" + getCost() +
            ", group='" + getGroup() + '\'' +
            ", stockId=" + getStockId() +
            ", grossAmount=" + getGrossAmount() +
            ", discountAmount=" + discountAmount +
            ", netAmount=" + getNetAmount() +
            ", taxAmount=" + getTaxAmount() +
            ", patientAmount=" + patientAmount +
            ", sponsorAmount=" + sponsorAmount +
            ", itemPlans=" + itemPlans +
            ", itemTaxes=" + itemTaxes +
            ", stockQuantity=" + getStockQuantity() +
            ", locator=" + getLocator() +
            ", substitute=" + getSubstitute() +
            ", supplier='" + getSupplier() + '\'' +
            ", barCode='" + getBarCode() + '\'' +
            ", returnQuantity=" + getReturnQuantity() +
            ", totalMrp=" + getTotalMrp() +
            ", saleRate=" + getSaleRate() +
            ", grossRate=" + getGrossRate() +
            ", saleAmount=" + getSaleAmount() +
            ", taxDiscountAmount=" + getTaxDiscountAmount() +
            ", patientTaxAmount=" + getPatientTaxAmount() +
            ", planDiscountAmount=" + planDiscountAmount +
            ", unitDiscount=" + getUnitDiscount() +
            ", userDiscount=" + getUserDiscount() +
            ", totalDiscountAmount=" + getTotalDiscountAmount() +
            ", patientGrossAmount=" + getPatientGrossAmount() +
            ", patientNetAmount=" + getPatientNetAmount() +
            ", patientTotalDiscAmount=" + getPatientTotalDiscAmount() +
            ", patientTotalTaxAmount=" + getPatientTotalTaxAmount() +
            ", sponsorTaxAmount=" + getSponsorTaxAmount() +
            ", totalSponsorAmount=" + getTotalSponsorAmount() +
            ", isPercentDiscount=" + isPercentDiscount() +
            ", isItemDiscount=" + isItemDiscount() +
            ", consignment=" + getConsignment() +
            ", orderItem=" + getOrderItem() +
            ", getDispenseItemPlans()=" + getDispenseItemPlans() +
            ", getDispenseTaxes()=" + getDispenseTaxes() +
            ", totalTaxInPercent=" + getTotalTaxInPercent() +
            ", dispenseSource=" + dispenseSource +
            '}';
    }


    public void calculateGross() {
        int calculationScale = 6;
        //setMrp(roundOff( getMrp(), calculationScale));
        setTotalMrp(roundOff(multiply(getQuantity(), getMrp()), calculationScale));
        setSaleAmount(roundOff(multiply(getQuantity(), getSaleRate()), calculationScale));
        setGrossAmount(roundOff(subtract(getSaleAmount(), getTaxAmount()), calculationScale));
        setCalculatedGrossAmount(getGrossAmount());
        setGrossRate(divide(getGrossAmount(),getQuantity()));
    }

    public BigDecimal calculateTotalTax(BigDecimal saleAmount) {
        int calculationScale = 2;
        BigDecimal totalTaxAmount = BigDecimalUtil.ZERO;
        for (DispenseTax tax : getDispenseTaxes()) {
            BigDecimal taxAmount = CalculateTaxUtil.reverseCalculateTaxAmount(saleAmount,
                tax.getTaxDefinition().getTaxCalculation().getPercentage(), getTotalTaxInPercent());
            totalTaxAmount = add(totalTaxAmount, roundOff(taxAmount, calculationScale));
        }
        return totalTaxAmount;
    }
    public void summarize(String taxCalculationType) {
        int calculationScale = 2;
        setPatientTotalDiscAmount(roundOff(subtract(sum(getUnitDiscount(), getUserDiscount(), getPlanDiscAmount()), getSponsorDiscAmount()),
            calculationScale));
        setTotalDiscountAmount(roundOff(sum(getUnitDiscount(), getUserDiscount(), getPlanDiscAmount(), getTaxDiscountAmount()),
            calculationScale));

        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            setPatientNetAmount(roundOff(subtract(getPatientSaleAmount(), add(getPatientTotalDiscAmount(), getTaxDiscountAmount())),
                calculationScale));
            setSponsorNetAmount(roundOff(subtract(getSponsorSaleAmount(), getSponsorDiscAmount()), calculationScale));
            setNetAmount(roundOff(subtract(getSaleAmount(), getTotalDiscountAmount()), calculationScale));
            setCalculatedGrossAmount(subtract(getNetAmount(), getTaxAmount()).add(getTotalDiscountAmount()));
            setCalculatedPatientGrossAmount(subtract(sum(getPatientNetAmount(), getPatientTotalDiscAmount(), getTaxDiscountAmount()),  getPatientTaxAmount()));
            setCalculatedSponsorGrossAmount(subtract(getSponsorNetAmount(), getSponsorTaxAmount()).add(getSponsorDiscAmount()));
        } else {
            setPatientNetAmount(roundOff(
                subtract(add(getPatientGrossAmount(), getPatientTaxAmount()),  add(getPatientTotalDiscAmount(), getTaxDiscountAmount())), calculationScale));
            setSponsorNetAmount(roundOff(subtract(add(getSponsorGrossAmount(), getSponsorTaxAmount()), getSponsorDiscAmount()), calculationScale));
            setNetAmount(roundOff(subtract(add(getGrossAmount(), getTaxAmount()), getTotalDiscountAmount()), calculationScale));
        }
        setGrossAmount(subtract(add(getNetAmount(), getTotalDiscountAmount()), getTaxAmount()));
        setPatientGrossAmount(subtract(sum(getPatientNetAmount(),getPatientTotalDiscAmount(), getTaxDiscountAmount()),  getPatientTaxAmount()));
        setSponsorGrossAmount(subtract(add(getSponsorNetAmount(), getSponsorDiscAmount()), getSponsorTaxAmount()));
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            setGrossRate(divide(getGrossAmount(), getQuantity()));
        }
    }

    // set item level plan details
    public void setItemLevelPlanDetails() {
        if(getDispenseItemPlans() == null || getDispenseItemPlans().isEmpty()){
            return;
        }
        DispenseItemPlan itemPlan = getDispenseItemPlans().get(0);
        itemPlan.setSaleAmount(getSaleAmount());
        itemPlan.setTaxAmount(getTaxAmount());
        itemPlan.setGrossAmount(getGrossAmount());

        itemPlan.setSponsorGrossAmount(getSponsorGrossAmount());
        itemPlan.setPatientGrossAmount(getPatientGrossAmount());

        //tax amount for patient and sponsor
        itemPlan.setPatientTaxAmount(getPatientTaxAmount());
        itemPlan.setSponsorTaxAmount(getSponsorTaxAmount());
        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        for (DispenseTax lineDispenseTax : getDispenseTaxes()) {
            DispenseTax sponsorDispenseTax = new DispenseTax();
            sponsorDispenseTax.setDefinition(lineDispenseTax.getDefinition());
            sponsorDispenseTax.setTaxCode(lineDispenseTax.getTaxCode());
            sponsorDispenseTax.setTaxDefinition(lineDispenseTax.getTaxDefinition());
            sponsorDispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            sponsorDispenseTax.setTaxAmount(lineDispenseTax.getTaxAmount());
            dispenseTaxes.add(sponsorDispenseTax);
        }
        itemPlan.setPlanTaxList(dispenseTaxes);

        itemPlan.setSponsorDiscAmount(getSponsorDiscAmount());
        itemPlan.setPatientDiscAmount(subtract(getPlanDiscAmount(), getSponsorDiscAmount()));

        itemPlan.setPatientNetAmount(subtract(add(itemPlan.getPatientGrossAmount(), itemPlan.getPatientTaxAmount()), itemPlan.getPatientDiscAmount()));
        itemPlan.setSponsorNetAmount(subtract(add(itemPlan.getSponsorGrossAmount(), itemPlan.getSponsorTaxAmount()), itemPlan.getSponsorDiscAmount()));
    }

    public void adjustGrossAmounts(){
        setGrossAmount(add(subtract(getNetAmount(), getTaxAmount()), getTotalDiscountAmount()));
        setPatientGrossAmount(sum(subtract(getPatientNetAmount(), getPatientTaxAmount()), getPatientTotalDiscAmount(), getTaxDiscountAmount()));
        setSponsorGrossAmount(subtract(getSponsorNetAmount(),add(getSponsorTaxForPatient(), getSponsorTaxAmount())).add(getSponsorDiscAmount()));
        setPatientGrossAmount(returnZeroIfNegative(getPatientGrossAmount()));
        setSponsorGrossAmount(returnZeroIfNegative(getSponsorGrossAmount()));
        setGrossAmount(returnZeroIfNegative(getGrossAmount()));
    }

    public void roundOffToRequiredScale() {
        int requiredScale = 2;
        setPatientSaleAmount(roundOff(getPatientSaleAmount(),requiredScale));
        setPatientGrossAmount(roundOff(getPatientGrossAmount(),requiredScale));
        setPatientTotalDiscAmount(roundOff(getPatientTotalDiscAmount(),requiredScale));
        setPatientTaxAmount(roundOff(getPatientTaxAmount(),requiredScale));
        setPatientNetAmount(roundOff(getPatientNetAmount(),requiredScale));
        setPatientTotalTaxAmount(roundOff(getPatientTotalTaxAmount(),requiredScale));

        setSponsorSaleAmount(roundOff(getSponsorSaleAmount(),requiredScale));
        setSponsorGrossAmount(roundOff(getSponsorGrossAmount(),requiredScale));
        setSponsorDiscAmount(roundOff(getSponsorDiscAmount(),requiredScale));
        setPlanDiscAmount(roundOff(getPlanDiscAmount(), requiredScale));
        setSponsorTaxAmount(roundOff(getSponsorTaxAmount(),requiredScale));
        setSponsorNetAmount(roundOff(getSponsorNetAmount(),requiredScale));

        setTaxAmount(roundOff(getTaxAmount(),requiredScale));
        setSaleAmount(roundOff(getSaleAmount(),requiredScale));
        setGrossAmount(roundOff(getGrossAmount(),requiredScale));
        setTotalDiscountAmount(roundOff(getTotalDiscountAmount(),requiredScale));
        setNetAmount(roundOff(getNetAmount(),requiredScale));
        setUnitDiscount(roundOff(getUnitDiscount(), requiredScale));
        setUserDiscount(roundOff(getUserDiscount(), requiredScale));

        setCalculatedGrossAmount(roundOff(getCalculatedGrossAmount(), requiredScale));
        setCalculatedPatientGrossAmount(roundOff(getCalculatedPatientGrossAmount(), requiredScale));
        setCalculatedSponsorGrossAmount(roundOff(getCalculatedSponsorGrossAmount(), requiredScale));
    }
}
