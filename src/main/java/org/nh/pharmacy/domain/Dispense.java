package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nh.pharmacy.domain.dto.DispenseDocument;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;


/**
 * A Dispense.
 */
@Entity
@Table(name = "dispense")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "dispense", type = "dispense")
@Setting(settingPath = "/es/settings.json")
@IdClass(DocumentId.class)
public class Dispense implements Serializable, Persistable<Long> {

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
    private DispenseDocument document;

    @Id
    private Integer version;

    @NotNull
    @Column(name = "latest", nullable = false)
    private Boolean latest;

    @Transient
    private boolean isNew = true;

    /**
     * Returns if the {@code Persistable} is new or was persisted already.
     *
     * @return if the object is new
     */
    @Override
    public boolean isNew() {
        if (isNew) {
            isNew = false;
            return true;
        }
        return isNew;
    }

    public void setIsNew(boolean isNew){
        this.isNew = isNew;
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

    public Dispense documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public DispenseDocument getDocument() {
        return document;
    }

    public Dispense document(DispenseDocument document) {
        this.document = document;
        return this;
    }

    public void setDocument(DispenseDocument document) {
        this.document = document;
    }

    public Integer getVersion() {
        return version;
    }

    public Dispense version(Integer version) {
        this.version = version;
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isLatest() {
        return latest;
    }

    public Dispense latest(Boolean latest) {
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
        Dispense dispense = (Dispense) o;
        return (id != null && id.equals(dispense.id)) &&
            (version != null && version.equals(dispense.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "Dispense{" +
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
    /*public void updateLineWithPlanRuleDetail(DispenseDocumentLine line, PlanRuleDetail planRuleDetail, DispensePlan dispensePlan) {
        line.reset();
        line.resetForPlan();
        line.applyOnBaseDetailsIfAny(planRuleDetail);
        line.calculateGross();

        line.calculateSponsorGross(planRuleDetail.getSponsorPayment());
        line.calculatePatientGross(planRuleDetail.getPatientCopayment());

        *//*line.applyOnSponsorAmount(planRuleDetail);
        line.applyOnPatientAmount(planRuleDetail);*//*

        line.calculateTax(planRuleDetail.isSponsorPayTax() ? planRuleDetail.getPatientCopayment() : 100f);

        populateDispenseItemPlanForLine(line, planRuleDetail, dispensePlan);
    }

    public void updateLineWithPlanRuleDetail(DispenseDocumentLine line) {
        PlanRuleDetail planRuleDetail = line.getPlanRuleDetail();
        DispensePlan dispensePlan = line.getDispensePlan();
        line.calculateSponsorGross(line.getPlanRuleDetail().getSponsorPayment());
        line.calculatePatientGross(planRuleDetail.getPatientCopayment());

        *//*line.applyOnSponsorAmount(planRuleDetail);
        line.applyOnPatientAmount(planRuleDetail);*//*

        line.calculateTax(planRuleDetail.isSponsorPayTax() ? planRuleDetail.getPatientCopayment() : 100f);

        populateDispenseItemPlanForLine(line, planRuleDetail, dispensePlan);
    }

    public void updateLineWithoutPlanRuleDetail(DispenseDocumentLine line) {
        *//*line.resetLine(line);
        line.setTotalMrp(roundOff(line.getMrp() * line.getQuantity(), 2));
        line.setSaleRate(line.getMrp());
        line.setSaleAmount(line.getTotalMrp());*//*
        calculateGrossAmountForLine(line);

        line.setPatientSaleAmount(line.getSaleAmount());
        line.setPatientGrossAmount(line.getGrossAmount());
        line.setPatientTaxAmount(line.getTaxAmount());
        splitPatientTaxAmountForLine(line.getDispenseTaxes(), line.getTaxAmount(), line.getTotalTaxInPercent(), 100f);

        line.setPatientNetAmount(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount());
        line.setNetAmount(line.getGrossAmount() + line.getTaxAmount() - line.getTotalDiscountAmount());

        line.round();
    }

    private DispenseDocumentLine calculateGrossAmountForLine(DispenseDocumentLine line) {
        Float totalTaxAmount = CalculateTaxUtil.calculateTax(line.getSaleAmount(), line.getTotalTaxInPercent());
        line.setTaxAmount(roundOff(totalTaxAmount, 2));
        line.setGrossAmount(line.getSaleAmount() - line.getTaxAmount());
        line.setCalculatedGrossAmount(line.getGrossAmount());
        line.setGrossRate(line.getGrossAmount() / line.getQuantity());
        return line;
    }

    private void calculateSaleRateForItem(DispenseDocumentLine line, PlanRuleDetail planRuleDetail) {
        AppliedOnBasePatientSponsor appliedOnBase = planRuleDetail.getAppliedOnBase();
        Float saleRate = line.getMrp();
        if (appliedOnBase != null) {
            if (BaseSponsorPatientType.Discount.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    float discAmt = CalculateTaxUtil.calculatePercentAmount(line.getMrp(), appliedOnBase.getValue());
                    saleRate = saleRate - discAmt;
                }
            } else if (BaseSponsorPatientType.Addon.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    saleRate += CalculateTaxUtil.calculatePercentAmount(line.getMrp(), appliedOnBase.getValue());
                } else if (BaseSponsorPatientValueType.Amount.equals(appliedOnBase.getValueType())) {
                    saleRate += appliedOnBase.getValue();
                }
            } else if (BaseSponsorPatientType.Fixed.equals(appliedOnBase.getAppliedType())) {
                saleRate = appliedOnBase.getValue();
            }
        }
        line.setSaleRate(saleRate);
    }

    private void calculateCoPaymentBasedOnPlan(DispenseDocumentLine line, PlanRuleDetail planRuleDetail) {

        line.setSponsorSaleAmount(roundOff(CalculateTaxUtil.calculatePercentAmount(line.getSaleAmount(), planRuleDetail.getSponsorPayment()), 2));
        line.setPatientSaleAmount(roundOff(CalculateTaxUtil.calculatePercentAmount(line.getSaleAmount(), planRuleDetail.getPatientCopayment()), 2));

        float sponsorGrossAmount = CalculateTaxUtil.calculatePercentAmount(line.getGrossAmount(), planRuleDetail.getSponsorPayment());
        float patientGrossAmount = CalculateTaxUtil.calculatePercentAmount(line.getGrossAmount(), planRuleDetail.getPatientCopayment());
        line.setSponsorGrossAmount(roundOff(sponsorGrossAmount, 2));
        line.setPatientGrossAmount(roundOff(patientGrossAmount, 2));

        if (planRuleDetail.getAppliedOnSponsorAmount() != null) {
            calculateAmountBasedOnPlanValueType(line, planRuleDetail.getAppliedOnSponsorAmount(), true);
        }

        if (planRuleDetail.getAppliedOnPatientAmount() != null) {
            calculateAmountBasedOnPlanValueType(line, planRuleDetail.getAppliedOnPatientAmount(), false);
        }

        float patientTaxPayablePercent = planRuleDetail.getPatientCopayment();
        float sponsorTax = 0;
        if (planRuleDetail.isSponsorPayTax()) {
            sponsorTax = CalculateTaxUtil.calculatePercentAmount(line.getTaxAmount(), planRuleDetail.getSponsorPayment());
            sponsorTax = roundOff(sponsorTax, 2);
            line.setSponsorTaxAmount(sponsorTax);
        } else {
            patientTaxPayablePercent = 100f;
        }
        calculatePatientTaxForLine(line, patientTaxPayablePercent);
    }

    private void calculatePatientTaxForLine(DispenseDocumentLine line, float patientTaxPayablePercent) {
        float patientTax = CalculateTaxUtil.calculatePercentAmount(line.getTaxAmount(), patientTaxPayablePercent);
        patientTax = roundOff(patientTax, 2);
        line.setPatientTaxAmount(patientTax);
        splitPatientTaxAmountForLine(line.getDispenseTaxes(), line.getTaxAmount(), line.getTotalTaxInPercent(), patientTaxPayablePercent);
    }

    private void populateDispenseItemPlanForLine(DispenseDocumentLine line, PlanRuleDetail planRuleDetail, DispensePlan dispensePlan) {
        PlanRef docPlanRef = dispensePlan.getPlanRef();
        PlanRef linePlanRef = new PlanRef();
        linePlanRef.setId(docPlanRef.getId());
        linePlanRef.setCode(docPlanRef.getCode());
        linePlanRef.setName(docPlanRef.getName());
        //Create new item plan and populate required values
        DispenseItemPlan itemPlan = new DispenseItemPlan();
        itemPlan.setPlanRef(linePlanRef);
        itemPlan.setSponsorRef(dispensePlan.getSponsorRef());
        itemPlan.setPlanRuleDetail(planRuleDetail);

        itemPlan.setSaleAmount(line.getSaleAmount());
        itemPlan.setTaxAmount(line.getTaxAmount());
        itemPlan.setGrossAmount(line.getGrossAmount());

        itemPlan.setSponsorGrossAmount(line.getSponsorGrossAmount());
        itemPlan.setPatientGrossAmount(line.getPatientGrossAmount());

        //tax amount for patient and sponsor
        itemPlan.setPatientTaxAmount(line.getPatientTaxAmount());
        itemPlan.setSponsorTaxAmount(line.getSponsorTaxAmount());
        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        for (DispenseTax lineDispenseTax : line.getDispenseTaxes()) {
            DispenseTax sponsorDispenseTax = new DispenseTax();
            sponsorDispenseTax.setDefinition(lineDispenseTax.getDefinition());
            sponsorDispenseTax.setTaxCode(lineDispenseTax.getTaxCode());
            sponsorDispenseTax.setTaxDefinition(lineDispenseTax.getTaxDefinition());
            sponsorDispenseTax.setPatientTaxAmount(0f);
            sponsorDispenseTax.setTaxAmount(lineDispenseTax.getTaxAmount());
            dispenseTaxes.add(sponsorDispenseTax);
        }
//        List<DispenseTax> dispenseTaxes = splitTaxAmountForLine(line.getDispenseTaxes(), line.getSponsorTaxAmount(), line.getTotalTaxInPercent());
        itemPlan.setPlanTaxList(dispenseTaxes);

        itemPlan.setSponsorDiscAmount(line.getSponsorDiscAmount());
        itemPlan.setPatientDiscAmount(line.getPlanDiscAmount() - line.getSponsorDiscAmount());

        itemPlan.setPatientNetAmount(itemPlan.getPatientGrossAmount() + itemPlan.getPatientTaxAmount() - itemPlan.getPatientDiscAmount());
        itemPlan.setSponsorNetAmount(itemPlan.getSponsorGrossAmount() + itemPlan.getSponsorTaxAmount() - itemPlan.getSponsorDiscAmount());

        List<DispenseItemPlan> itemPlans = new ArrayList<>();
        itemPlans.add(itemPlan);
        line.setDispenseItemPlans(itemPlans);

    }

    private void calculateAmountBasedOnPlanValueType(DispenseDocumentLine line, AppliedOnBasePatientSponsor appliedOn, boolean isAppliedOnSponsor) {
        float amount = line.getPatientGrossAmount();
        if (isAppliedOnSponsor) {
            amount = line.getSponsorGrossAmount();
        }
        if (BaseSponsorPatientType.Discount.equals(appliedOn.getAppliedType())) {
            float discAmount = 0f;
            //Calculate Discount based on type
            if (BaseSponsorPatientValueType.Percentage.equals(appliedOn.getValueType())) {
                discAmount = CalculateTaxUtil.calculatePercentAmount(amount, appliedOn.getValue());
            } else if (BaseSponsorPatientValueType.Amount.equals(appliedOn.getValueType())) {
                discAmount = appliedOn.getValue();
            }
            discAmount = roundOff(discAmount, 2);
            //populate patient/sponsor disc values
            if (isAppliedOnSponsor) {
                line.setSponsorDiscAmount(discAmount);
            } else {
                discAmount = line.getPatientGrossAmount() == 0 ? 0 : discAmount;
                line.setPatientTotalDiscAmount(discAmount);//setting patient plan discount
            }
            line.setPlanDiscAmount(roundOff(line.getPlanDiscAmount() + discAmount, 2));

        } else if (BaseSponsorPatientType.Addon.equals(appliedOn.getAppliedType())) {
            float addonAmount = 0f;
            if (BaseSponsorPatientValueType.Percentage.equals(appliedOn.getValueType())) {
                addonAmount = CalculateTaxUtil.calculatePercentAmount(amount, appliedOn.getValue());
            } else if (BaseSponsorPatientValueType.Amount.equals(appliedOn.getValueType())) {
                addonAmount = appliedOn.getValue();
            }
            addonAmount = roundOff(addonAmount, 2);
            if (isAppliedOnSponsor) {
                line.setSponsorGrossAmount(line.getSponsorGrossAmount() + addonAmount);
            } else {
                line.setPatientGrossAmount(line.getPatientGrossAmount() + addonAmount);
            }
        }
    }

    //item Plan
    @Deprecated
    public List<DispenseTax> splitTaxAmountForLine(List<DispenseTax> dispenseTaxList, Float totalTaxAmount, Float totalTaxPercentage) {

        List<DispenseTax> dispenseTaxes = new ArrayList<>();
        dispenseTaxes.addAll(dispenseTaxList);

        dispenseTaxes.forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            Float taxAmount = totalTaxAmount * (taxCalculation.getPercentage() / totalTaxPercentage);
            taxAmount = roundOff(taxAmount, 2);
            dispenseTax.setTaxAmount(taxAmount);
        });
        return dispenseTaxes;
    }

    //patient tax details after co-payment
    public void splitPatientTaxAmountForLine(List<DispenseTax> dispenseTaxes, Float totalTaxAmount, Float totalTaxPercentage, Float totalPatientTaxPercentage) {
        dispenseTaxes.forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            Float taxAmount = totalTaxAmount * (taxCalculation.getPercentage() / totalTaxPercentage);
            taxAmount = roundOff(taxAmount, 2);
            dispenseTax.setPatientTaxAmount(roundOff(taxAmount * totalPatientTaxPercentage * 0.01f, 2));
            dispenseTax.setTaxAmount(roundOff(taxAmount - dispenseTax.getPatientTaxAmount(), 2));
        });
    }

    public void applyUnitDiscount() {
        document.getDispenseDocumentLines().forEach(line -> {
            line.setPatientTotalDiscAmount(roundOff(line.getPlanDiscAmount() - line.getSponsorDiscAmount(), 2));
            document.calculateUserAndUnitDiscount(line, document.getUnitDiscountPercentage());
            line.setPatientNetAmount(line.getPatientGrossAmount() + line.getPatientTaxAmount() - line.getPatientTotalDiscAmount());
            line.setNetAmount(line.getGrossAmount() + line.getTaxAmount() - line.getTotalDiscountAmount());
            line.round();
        });
    }*/
}
