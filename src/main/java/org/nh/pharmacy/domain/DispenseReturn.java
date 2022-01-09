package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.billing.domain.dto.AppliedOnBasePatientSponsor;
import org.nh.billing.domain.dto.PlanRef;
import org.nh.billing.domain.dto.PlanRuleDetail;
import org.nh.billing.domain.dto.TaxCalculation;
import org.nh.billing.domain.enumeration.BaseSponsorPatientType;
import org.nh.billing.domain.enumeration.BaseSponsorPatientValueType;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.dto.*;
import org.nh.pharmacy.util.CalculateTaxUtil;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.math.BigDecimal;

import static org.nh.common.util.BigDecimalUtil.*;

/**
 * A DispenseReturn.
 */
@Entity
@Table(name = "dispense_return")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "dispensereturn", type = "dispensereturn")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class DispenseReturn implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "document_number", nullable = false)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String documentNumber;

    @NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "document", nullable = false)
    @Field(type = FieldType.Object)
    private DispenseReturnDocument document;

    @Id
    private Integer version;

    @NotNull
    @Column(name = "latest", nullable = false)
    private Boolean latest;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        if (isNew) {
            isNew = false;
            return true;
        }
        return isNew;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public DispenseReturn documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public DispenseReturnDocument getDocument() {
        return document;
    }

    public void setDocument(DispenseReturnDocument document) {
        this.document = document;
    }

    public DispenseReturn document(DispenseReturnDocument document) {
        this.document = document;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public DispenseReturn version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public DispenseReturn latest(Boolean latest) {
        this.latest = latest;
        return this;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DispenseReturn dispenseReturn = (DispenseReturn) o;
        if (dispenseReturn.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, dispenseReturn.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DispenseReturn{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + "'" +
            ", document='" + document + "'" +
            ", version='" + version + "'" +
            ", latest='" + latest + "'" +
            '}';
    }

    /**
     * @param line
     * @param planRuleDetail
     */
    public void updateLineWithPlanRuleDetail(DispenseReturnDocumentLine line, PlanRuleDetail planRuleDetail, DispensePlan dispensePlan) {
        line.resetLine(line);
        line.setTotalMrp(roundOff(multiply(line.getMrp(), line.getQuantity()), 2));
        // line.setSaleRate(line.getMrp());
        line.setSaleAmount(multiply(line.getSaleRate(), line.getQuantity()));

        calculateSaleRateForItem(line, planRuleDetail);
        calculateGrossAmountForLine(line, planRuleDetail);

        calculateCoPaymentBasedOnPlan(line, planRuleDetail);
        //calculate unit and user Discount
        document.calculateUserAndUnitDiscount(line, 0f);

        line.setPatientNetAmount(add(line.getPatientGrossAmount(), subtract(line.getPatientTaxAmount(), line.getPatientTotalDiscAmount())));
        line.setSponsorNetAmount(add(line.getSponsorGrossAmount(), subtract(line.getSponsorTaxAmount(), line.getSponsorDiscAmount())));
        line.setNetAmount(add(line.getGrossAmount(), subtract(line.getTaxAmount(), line.getTotalDiscountAmount())));

        populateDispenseItemPlanForLine(line, planRuleDetail, dispensePlan);

        line.round();
    }

    public void updateLineWithoutPlanRuleDetail(DispenseReturnDocumentLine line) {
        line.resetLine(line);
        line.setTotalMrp(roundOff(multiply(line.getMrp(), line.getQuantity()), 2));
        ///line.setSaleRate(line.getMrp());
        line.setSaleAmount(multiply(line.getSaleRate(), line.getQuantity()));
        BigDecimal totalTaxAmount = CalculateTaxUtil.calculateTax(line.getSaleAmount(), line.getTotalTaxInPercent());
        totalTaxAmount = roundOff(totalTaxAmount, 2);
        line.setTaxAmount(totalTaxAmount);
        line.setGrossAmount(subtract(line.getSaleAmount(), totalTaxAmount));
        line.setGrossRate(divide(line.getGrossAmount(), line.getQuantity()));

        line.setPatientSaleAmount(line.getSaleAmount());
        line.setPatientGrossAmount(line.getGrossAmount());
        line.setPatientTaxAmount(totalTaxAmount);
        splitPatientTaxAmountForLine(line.getDispenseTaxes(), totalTaxAmount, line.getTotalTaxInPercent());

        document.calculateUserAndUnitDiscount(line, 0f);

        line.setPatientNetAmount(add(line.getPatientGrossAmount(), subtract(line.getPatientTaxAmount(), line.getPatientTotalDiscAmount())));
        line.setNetAmount(add(line.getGrossAmount(), subtract(line.getTaxAmount(), line.getTotalDiscountAmount())));

        line.round();
    }

    private DispenseDocumentLine calculateGrossAmountForLine(DispenseReturnDocumentLine line, PlanRuleDetail planRuleDetail) {
        BigDecimal totalTaxAmount = CalculateTaxUtil.calculateTax(line.getSaleAmount(), line.getTotalTaxInPercent());
        line.setTaxAmount(roundOff(totalTaxAmount, 2));
        line.setGrossAmount(subtract(line.getSaleAmount(), line.getTaxAmount()));
        line.setGrossRate(divide(line.getGrossAmount(), line.getQuantity()));
        return line;
    }

    private void calculateSaleRateForItem(DispenseReturnDocumentLine line, PlanRuleDetail planRuleDetail) {
        AppliedOnBasePatientSponsor appliedOnBase = planRuleDetail.getAppliedOnBase();
        BigDecimal saleRate = line.getMrp();
        //Base discount will change mrp
        if (appliedOnBase != null) {
            if (BaseSponsorPatientType.Discount.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    BigDecimal discAmt = CalculateTaxUtil.calculatePercentAmount(line.getMrp(), appliedOnBase.getValue());
                    saleRate = subtract(saleRate, discAmt);
                }
            } else if (BaseSponsorPatientType.Addon.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    saleRate = saleRate.add(CalculateTaxUtil.calculatePercentAmount(line.getMrp(), appliedOnBase.getValue()));
                } else if (BaseSponsorPatientValueType.Amount.equals(appliedOnBase.getValueType())) {
                    saleRate = saleRate.add(getBigDecimal(appliedOnBase.getValue()));
                }
            } else if (BaseSponsorPatientType.Fixed.equals(appliedOnBase.getAppliedType())) {
                saleRate = getBigDecimal(appliedOnBase.getValue());
            }
        }
        line.setSaleRate(saleRate);
        line.setSaleAmount(roundOff(multiply(saleRate, line.getQuantity()), 2));
        line.setTotalDiscountAmount(subtract(line.getTotalMrp(), line.getSaleAmount()));
    }

    private void calculateCoPaymentBasedOnPlan(DispenseReturnDocumentLine line, PlanRuleDetail planRuleDetail) {

        BigDecimal sponsorGrossAmount = CalculateTaxUtil.calculatePercentAmount(line.getGrossAmount(), planRuleDetail.getSponsorPayment());
        BigDecimal patientGrossAmount = CalculateTaxUtil.calculatePercentAmount(line.getGrossAmount(), planRuleDetail.getPatientCopayment());
        line.setSponsorGrossAmount(roundOff(sponsorGrossAmount, 2));
        line.setPatientGrossAmount(roundOff(patientGrossAmount, 2));

        Float patientTaxPayablePercent = planRuleDetail.getPatientCopayment();
        BigDecimal sponsorTax = BigDecimalUtil.ZERO;
        if (planRuleDetail.isSponsorPayTax()) {
            sponsorTax = CalculateTaxUtil.calculatePercentAmount(line.getTaxAmount(), planRuleDetail.getSponsorPayment());
            sponsorTax = roundOff(sponsorTax, 2);
            line.setSponsorTaxAmount(sponsorTax);
        } else {
            patientTaxPayablePercent = 100f;
        }
        calculatePatientTaxForLine(line, patientTaxPayablePercent);

        if (planRuleDetail.getAppliedOnSponsorAmount() != null) {
            calculateAmountBasedOnPlanValueType(line, planRuleDetail.getAppliedOnSponsorAmount(), true);
        }

        if (planRuleDetail.getAppliedOnPatientAmount() != null) {
            calculateAmountBasedOnPlanValueType(line, planRuleDetail.getAppliedOnPatientAmount(), false);
        }
    }

    private void calculatePatientTaxForLine(DispenseReturnDocumentLine line, Float patientTaxPayablePercent) {
        BigDecimal patientTax = CalculateTaxUtil.calculatePercentAmount(line.getTaxAmount(), patientTaxPayablePercent);
        patientTax = roundOff(patientTax, 2);
        line.setPatientTaxAmount(patientTax);
        splitPatientTaxAmountForLine(line.getDispenseTaxes(), patientTax, line.getTotalTaxInPercent());
    }

    private void populateDispenseItemPlanForLine(DispenseReturnDocumentLine line, PlanRuleDetail planRuleDetail, DispensePlan dispensePlan) {
        PlanRef docPlanRef = dispensePlan.getPlanRef();
        PlanRef linePlanRef = new PlanRef();
        linePlanRef.setId(docPlanRef.getId());
        linePlanRef.setCode(docPlanRef.getCode());
        linePlanRef.setName(docPlanRef.getName());
        //Create new item plan and populate required values
        DispenseItemPlan itemPlan = new DispenseItemPlan();
        itemPlan.setPlanRef(linePlanRef);
        itemPlan.setPlanRuleDetail(planRuleDetail);

        itemPlan.setSaleAmount(line.getSaleAmount());
        itemPlan.setTaxAmount(line.getTaxAmount());
        itemPlan.setGrossAmount(line.getGrossAmount());

        itemPlan.setSponsorGrossAmount(line.getSponsorGrossAmount());
        itemPlan.setPatientGrossAmount(line.getPatientGrossAmount());

        //tax amount for patient and sponsor
        itemPlan.setPatientTaxAmount(line.getPatientTaxAmount());
        itemPlan.setSponsorTaxAmount(line.getSponsorTaxAmount());
        List<DispenseTax> dispenseTaxes = splitTaxAmountForLine(line.getDispenseTaxes(), line.getSponsorTaxAmount(), line.getTotalTaxInPercent());
        itemPlan.setPlanTaxList(dispenseTaxes);

        itemPlan.setSponsorDiscAmount(line.getSponsorDiscAmount());
        itemPlan.setPatientDiscAmount(subtract(line.getPlanDiscAmount(), line.getSponsorDiscAmount()));

        itemPlan.setPatientNetAmount(subtract(add(itemPlan.getPatientGrossAmount(), itemPlan.getPatientTaxAmount()), itemPlan.getPatientDiscAmount()));
        itemPlan.setSponsorNetAmount(subtract(add(itemPlan.getSponsorGrossAmount(), itemPlan.getSponsorTaxAmount()), itemPlan.getSponsorDiscAmount()));

        List<DispenseItemPlan> itemPlans = new ArrayList<>();
        itemPlans.add(itemPlan);
        line.setDispenseItemPlans(itemPlans);

    }

    private void calculateAmountBasedOnPlanValueType(DispenseReturnDocumentLine line, AppliedOnBasePatientSponsor appliedOn, boolean isAppliedOnSponsor) {
        BigDecimal amount = line.getPatientGrossAmount();
        if (isAppliedOnSponsor) {
            amount = line.getSponsorGrossAmount();
        }
        if (BaseSponsorPatientType.Discount.equals(appliedOn.getAppliedType())) {
            BigDecimal discAmount = BigDecimalUtil.ZERO;
            //Calculate Discount based on type
            if (BaseSponsorPatientValueType.Percentage.equals(appliedOn.getValueType())) {
                discAmount = CalculateTaxUtil.calculatePercentAmount(amount, appliedOn.getValue());
            } else if (BaseSponsorPatientValueType.Amount.equals(appliedOn.getValueType())) {
                discAmount = getBigDecimal(appliedOn.getValue());
            }
            discAmount = roundOff(discAmount, 2);
            //populate patient/sponsor disc values
            if (isAppliedOnSponsor) {
                line.setSponsorDiscAmount(discAmount);
            } else {
                line.setPatientTotalDiscAmount(discAmount);
            }
            line.setPlanDiscAmount(add(line.getPlanDiscAmount(), discAmount));

        } else if (BaseSponsorPatientType.Addon.equals(appliedOn.getAppliedType())) {

        } else if (BaseSponsorPatientType.Fixed.equals(appliedOn.getAppliedType())) {

        }
    }

    //item Plan
    public List<DispenseTax> splitTaxAmountForLine(List<DispenseTax> dispenseTaxList, BigDecimal totalTaxAmount, Float totalTaxPercentage) {

        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        dispenseTaxes.addAll(dispenseTaxList);

        dispenseTaxes.forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            BigDecimal taxAmount = multiply(totalTaxAmount, divide(taxCalculation.getPercentage(), totalTaxPercentage));
            taxAmount = roundOff(taxAmount, 2);
            dispenseTax.setTaxAmount(taxAmount);
        });
        return dispenseTaxes;
    }

    //patient tax details after co-payment
    public void splitPatientTaxAmountForLine(List<DispenseTax> dispenseTaxes, BigDecimal totalTaxAmount, Float totalTaxPercentage) {
        dispenseTaxes.forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            BigDecimal taxAmount = multiply(totalTaxAmount,  divide(taxCalculation.getPercentage(), totalTaxPercentage));
            taxAmount = roundOff(taxAmount, 2);
            dispenseTax.setPatientTaxAmount(taxAmount);
        });
    }

    public void applyUnitDiscount() {
        if (document.getUnitDiscountPercentage() != null && document.getUnitDiscountPercentage() > 0) {
            document.getDispenseReturnDocumentLines().forEach(line -> {
                line.setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
                document.calculateUserAndUnitDiscount(line, line.getDispenseSource().getUnitDiscountAmount().floatValue());
                line.setPatientNetAmount(subtract(line.getPatientNetAmount(), line.getUnitDiscount()));
                line.setNetAmount(subtract(line.getNetAmount(), line.getUnitDiscount()));
                line.round();
            });
        }
    }
}
