package org.nh.pharmacy.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.nh.billing.domain.AuthorizationUtilization;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.BaseSponsorPatientType;
import org.nh.billing.domain.enumeration.BaseSponsorPatientValueType;
import org.nh.billing.domain.enumeration.Days;
import org.nh.common.dto.*;
import org.nh.common.enumeration.PricingMethod;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.util.CalculateTaxUtil;
import org.nh.pharmacy.util.UOMDeserializer;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.nh.common.util.BigDecimalUtil.*;
import static org.nh.pharmacy.util.CalculateTaxUtil.*;

/**
 * Created by Nirbhay on 6/7/17.
 */
public class DispenseDocumentLine implements Serializable {

    private Long lineNumber;
    private Long itemId;
    private String owner;
    private Long medicationId;
    private Long stockId;
    private Long stockQuantity;
    private Long batchQuantity;
    private String code;
    private String name;
    private String batchNumber;
    private String instruction;
    private String note;
    private LocalDate expiryDate;
    private LocatorDTO locator;
    private Boolean substitute;
    private String supplier;
    @JsonDeserialize(using = UOMDeserializer.class)
    private UOMDTO uom;
    private String barCode;
    private String sku;
    private ValueSetCodeDTO group;
    private Float quantity;
    private Float returnQuantity;
    private BigDecimal mrp = BigDecimalUtil.ZERO;
    private BigDecimal totalMrp = BigDecimalUtil.ZERO;
    private BigDecimal saleRate = BigDecimalUtil.ZERO;
    private BigDecimal grossRate = BigDecimalUtil.ZERO;
    private BigDecimal saleAmount = BigDecimalUtil.ZERO;
    private BigDecimal grossAmount = BigDecimalUtil.ZERO;
    private BigDecimal taxAmount = BigDecimalUtil.ZERO;
    private BigDecimal taxDiscountAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientTaxAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorDiscAmount = BigDecimalUtil.ZERO;
    private BigDecimal unitDiscount = BigDecimalUtil.ZERO;
    private BigDecimal userDiscount = BigDecimalUtil.ZERO;
    private Float enteredUserDiscount = 0f;
    private BigDecimal totalDiscountAmount = BigDecimalUtil.ZERO;
    private BigDecimal netAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientNetAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientTotalDiscAmount = BigDecimalUtil.ZERO;
    @Deprecated
    private BigDecimal patientTotalTaxAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientSaleAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorSaleAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorNetAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorTaxAmount = BigDecimalUtil.ZERO;
    private BigDecimal totalSponsorAmount = BigDecimalUtil.ZERO;
    private Boolean isPercentDiscount = Boolean.FALSE;
    private Boolean isItemDiscount = Boolean.FALSE;
    private Boolean consignment = Boolean.FALSE;
    private OrderItem orderItem;
    @Field(type = FieldType.Object)
    private Source source;
    private ConsultantDTO consultant;
    private List<DispenseItemPlan> dispenseItemPlans;
    private List<DispenseTax> dispenseTaxes;
    private Float totalTaxInPercent = 0f;
    private BigDecimal planDiscAmount = BigDecimalUtil.ZERO;
    private transient Boolean executePlan = false;
    private BigDecimal cost = BigDecimalUtil.ZERO;
    private Boolean fullDiscount = Boolean.FALSE;
    private UOMDTO trackUOM;
    private ValueSetCodeDTO itemType;
    private ItemCategoryDTO itemCategory;
    private ValueSetCodeDTO itemGroup;
    private GroupDTO materialGroup;
    private Medication medication;
    @Transient
    private PlanRuleDetail planRuleDetail;
    private PlanRuleDetail planAuthorizationRuleDetail;
    @Transient
    private DispensePlan dispensePlan;
    private BigDecimal appliedUserDiscount = BigDecimalUtil.ZERO;
    private BigDecimal itemUnitAvgCost = BigDecimalUtil.ZERO;
    private BigDecimal calculatedGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedPatientGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedSponsorGrossAmount = BigDecimalUtil.ZERO;
    @Transient
    private BigDecimal sponsorTaxForPatient;
    private String manufacturer;

    @Transient
    private BigDecimal authorizationSponsorDiscount =new BigDecimal(0);
    private List<AuthorizationUtilization> authorizationUtilizationList=new ArrayList<>();


    @Transient
    private BigDecimal accumulatedItemAmount;
    @Transient
    private BigDecimal accumulatedGroupAmount;
    @Transient
    private BigDecimal accumulatedPlanAmount;


    @Transient
    private Float accumulatedItemQty;
    @Transient
    private Float accumulatedGroupQty;
    @Transient
    private Float accumulatedPlanQty;

    @Transient
    private Boolean itemInclusionRuleAdded = Boolean.FALSE;

    private HealthcareServiceCenterDTO renderingHSC;

    private List<TariffAddOnParametersDTO> addOnParams=new ArrayList<>();

    private BigDecimal originalMRP=BigDecimal.ZERO;

    private BigDecimal sellingPrice=BigDecimal.ZERO;

    private Long medicationRequestDocLineId;

    private Float requestedQunatity =0f;

    //to handle ip dispense draft scenario
    private Float previousDispensedQty=0f;

    private String pricingMethod;

    private List<DosageInstruction> dosageInstruction;

    private Long duration;

    private ItemPricingDTO itemPricingDTO;

    private Set<Long> excludedPlans = new HashSet<>();

    private String genericName;

    @JsonSerialize
    @JsonDeserialize
    @Transient
    private Integer printQuantity;

    private String stockBarCode;

    public Boolean getItemInclusionRuleAdded() {return itemInclusionRuleAdded;}

    public void setItemInclusionRuleAdded(Boolean itemInclusionRuleAdded) {this.itemInclusionRuleAdded = itemInclusionRuleAdded;}

    public ItemPricingDTO getItemPricingDTO() {
        return itemPricingDTO;
    }

    public void setItemPricingDTO(ItemPricingDTO itemPricingDTO) {
        this.itemPricingDTO = itemPricingDTO;
    }

    public String getPricingMethod() {return pricingMethod;}

    public void setPricingMethod(String pricingMethod) {this.pricingMethod = pricingMethod;}

    public BigDecimal getOriginalMRP() {
        return originalMRP;
    }

    public void setOriginalMRP(BigDecimal originalMRP) {
        this.originalMRP = originalMRP;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getAccumulatedItemAmount() {
        return accumulatedItemAmount;
    }

    public void setAccumulatedItemAmount(BigDecimal accumulatedItemAmount) {
        this.accumulatedItemAmount = accumulatedItemAmount;
    }

    public BigDecimal getAccumulatedGroupAmount() {
        return accumulatedGroupAmount;
    }

    public void setAccumulatedGroupAmount(BigDecimal accumulatedGroupAmount) {
        this.accumulatedGroupAmount = accumulatedGroupAmount;
    }

    public BigDecimal getAccumulatedPlanAmount() {
        return accumulatedPlanAmount;
    }

    public void setAccumulatedPlanAmount(BigDecimal accumulatedPlanAmount) {
        this.accumulatedPlanAmount = accumulatedPlanAmount;
    }

    public Float getAccumulatedItemQty() {
        return accumulatedItemQty;
    }

    public void setAccumulatedItemQty(Float accumulatedItemQty) {
        this.accumulatedItemQty = accumulatedItemQty;
    }

    public Float getAccumulatedGroupQty() {
        return accumulatedGroupQty;
    }

    public void setAccumulatedGroupQty(Float accumulatedGroupQty) {
        this.accumulatedGroupQty = accumulatedGroupQty;
    }

    public Float getAccumulatedPlanQty() {
        return accumulatedPlanQty;
    }

    public void setAccumulatedPlanQty(Float accumulatedPlanQty) {
        this.accumulatedPlanQty = accumulatedPlanQty;
    }

    public List<AuthorizationUtilization> getAuthorizationUtilizationList() {
        return authorizationUtilizationList;
    }

    public void setAuthorizationUtilizationList(List<AuthorizationUtilization> authorizationUtilizationList) {
        this.authorizationUtilizationList = authorizationUtilizationList;
    }

    public PlanRuleDetail getPlanAuthorizationRuleDetail() {return planAuthorizationRuleDetail;}

    public void setPlanAuthorizationRuleDetail(PlanRuleDetail planAuthorizationRuleDetail) {
            this.planAuthorizationRuleDetail = planAuthorizationRuleDetail;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getSponsorTaxForPatient() {
        return sponsorTaxForPatient;
    }

    public void setSponsorTaxForPatient(BigDecimal sponsorTaxForPatient) {
        this.sponsorTaxForPatient = sponsorTaxForPatient;
    }

    public BigDecimal getCalculatedGrossAmount() {
        return calculatedGrossAmount;
    }

    public void setCalculatedGrossAmount(BigDecimal calculatedGrossAmount) {
        this.calculatedGrossAmount = calculatedGrossAmount;
    }

    public BigDecimal getCalculatedPatientGrossAmount() {
        return calculatedPatientGrossAmount;
    }

    public void setCalculatedPatientGrossAmount(BigDecimal calculatedPatientGrossAmount) {
        this.calculatedPatientGrossAmount = calculatedPatientGrossAmount;
    }

    public BigDecimal getCalculatedSponsorGrossAmount() {
        return calculatedSponsorGrossAmount;
    }

    public void setCalculatedSponsorGrossAmount(BigDecimal calculatedSponsorGrossAmount) {
        this.calculatedSponsorGrossAmount = calculatedSponsorGrossAmount;
    }

    public BigDecimal getItemUnitAvgCost() {
        return itemUnitAvgCost;
    }

    public void setItemUnitAvgCost(BigDecimal itemUnitAvgCost) {
        this.itemUnitAvgCost = itemUnitAvgCost;
    }

    public BigDecimal getAppliedUserDiscount() {
        return appliedUserDiscount;
    }

    public void setAppliedUserDiscount(BigDecimal appliedUserDiscount) {
        this.appliedUserDiscount = appliedUserDiscount;
    }

    public PlanRuleDetail getPlanRuleDetail() {
        return planRuleDetail;
    }

    public void setPlanRuleDetail(PlanRuleDetail planRuleDetail) {
        this.planRuleDetail = planRuleDetail;
    }

    public DispensePlan getDispensePlan() {
        return dispensePlan;
    }

    public void setDispensePlan(DispensePlan dispensePlan) {
        this.dispensePlan = dispensePlan;
    }

    public UOMDTO getTrackUOM() {
        return trackUOM;
    }

    public void setTrackUOM(UOMDTO trackUOM) {
        this.trackUOM = trackUOM;
    }

    public ValueSetCodeDTO getItemType() {
        return itemType;
    }

    public void setItemType(ValueSetCodeDTO itemType) {
        this.itemType = itemType;
    }

    public ItemCategoryDTO getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategoryDTO itemCategory) {
        this.itemCategory = itemCategory;
    }

    public ValueSetCodeDTO getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ValueSetCodeDTO itemGroup) {
        this.itemGroup = itemGroup;
    }

    public GroupDTO getMaterialGroup() {
        return materialGroup;
    }

    public void setMaterialGroup(GroupDTO materialGroup) {
        this.materialGroup = materialGroup;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public Boolean getFullDiscount() {
        return fullDiscount;
    }

    public void setFullDiscount(Boolean fullDiscount) {
        this.fullDiscount = fullDiscount;
    }

    public BigDecimal getPlanDiscAmount() {
        return planDiscAmount;
    }

    public void setPlanDiscAmount(BigDecimal planDiscAmount) {
        this.planDiscAmount = planDiscAmount;
    }

    public boolean isListNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public Long getBatchQuantity() {
        return batchQuantity;
    }

    public void setBatchQuantity(Long batchQuantity) {
        this.batchQuantity = batchQuantity;
    }

    public BigDecimal getPatientSaleAmount() {
        return patientSaleAmount;
    }

    public void setPatientSaleAmount(BigDecimal patientSaleAmount) {
        this.patientSaleAmount = patientSaleAmount;
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

    public BigDecimal getSponsorSaleAmount() {
        return sponsorSaleAmount;
    }

    public void setSponsorSaleAmount(BigDecimal sponsorSaleAmount) {
        this.sponsorSaleAmount = sponsorSaleAmount;
    }

    public BigDecimal getSponsorTaxAmount() {
        return sponsorTaxAmount;
    }

    public void setSponsorTaxAmount(BigDecimal sponsorTaxAmount) {
        this.sponsorTaxAmount = sponsorTaxAmount;
    }

    public Float getTotalTaxInPercent() {
        return totalTaxInPercent;
    }

    public void setTotalTaxInPercent(Float totalTaxInPercent) {
        this.totalTaxInPercent = totalTaxInPercent;
    }

    public BigDecimal getGrossRate() {
        return grossRate;
    }

    public void setGrossRate(BigDecimal grossRate) {
        this.grossRate = grossRate;
    }

    public BigDecimal getAuthorizationSponsorDiscount() {
        return authorizationSponsorDiscount;
    }

    public void setAuthorizationSponsorDiscount(BigDecimal authorizationSponsorDiscount) {
        this.authorizationSponsorDiscount = authorizationSponsorDiscount;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getUserDiscount() {
        return userDiscount;
    }

    public void setUserDiscount(BigDecimal userDiscount) {
        this.userDiscount = userDiscount;
    }

    public BigDecimal getUnitDiscount() {
        return unitDiscount;
    }

    public void setUnitDiscount(BigDecimal unitDiscount) {
        this.unitDiscount = unitDiscount;
    }

    public Float getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Float returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getSubstitute() {
        return substitute;
    }

    public void setSubstitute(Boolean sustitute) {
        this.substitute = sustitute;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public UOMDTO getUom() {
        return uom;
    }

    public void setUom(UOMDTO uom) {
        this.uom = uom;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getTotalMrp() {
        return totalMrp;
    }

    public void setTotalMrp(BigDecimal totalMrp) {
        this.totalMrp = totalMrp;
    }

    public ValueSetCodeDTO getGroup() {
        return group;
    }

    public void setGroup(ValueSetCodeDTO group) {
        this.group = group;
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

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getPatientNetAmount() {
        return patientNetAmount;
    }

    public void setPatientNetAmount(BigDecimal patientNetAmount) {
        this.patientNetAmount = patientNetAmount;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public ConsultantDTO getConsultant() {
        return consultant;
    }

    public void setConsultant(ConsultantDTO consultant) {
        this.consultant = consultant;
    }

    public List<DispenseItemPlan> getDispenseItemPlans() {
        return dispenseItemPlans;
    }

    public void setDispenseItemPlans(List<DispenseItemPlan> dispenseItemPlans) {
        this.dispenseItemPlans = dispenseItemPlans;
    }

    public List<DispenseTax> getDispenseTaxes() {
        return dispenseTaxes;
    }

    public void setDispenseTaxes(List<DispenseTax> dispenseTaxes) {
        this.dispenseTaxes = dispenseTaxes;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public BigDecimal getTaxDiscountAmount() {
        return taxDiscountAmount;
    }

    public void setTaxDiscountAmount(BigDecimal taxDiscountAmount) {
        this.taxDiscountAmount = taxDiscountAmount;
    }

    public BigDecimal getSponsorDiscAmount() {
        return sponsorDiscAmount;
    }

    public void setSponsorDiscAmount(BigDecimal sponsorDiscAmount) {
        this.sponsorDiscAmount = sponsorDiscAmount;
    }

    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public Boolean isPercentDiscount() {
        return isPercentDiscount;
    }

    public void setPercentDiscount(Boolean percentDiscount) {
        isPercentDiscount = percentDiscount;
    }

    public Long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public BigDecimal getPatientTaxAmount() {
        return patientTaxAmount;
    }

    public void setPatientTaxAmount(BigDecimal patientTaxAmount) {
        this.patientTaxAmount = patientTaxAmount;
    }

    @Deprecated
    public BigDecimal getTotalSponsorAmount() {
        return totalSponsorAmount;
    }

    @Deprecated
    public void setTotalSponsorAmount(BigDecimal totalSponsorAmount) {
        this.totalSponsorAmount = totalSponsorAmount;
    }

    public Boolean isItemDiscount() {
        return isItemDiscount;
    }

    public void setItemDiscount(Boolean itemDiscount) {
        isItemDiscount = itemDiscount;
    }

    public BigDecimal getPatientGrossAmount() {
        return patientGrossAmount;
    }

    public void setPatientGrossAmount(BigDecimal patientGrossAmount) {
        this.patientGrossAmount = patientGrossAmount;
    }

    public BigDecimal getPatientTotalDiscAmount() {
        return patientTotalDiscAmount;
    }

    public void setPatientTotalDiscAmount(BigDecimal patientTotalDiscAmount) {
        this.patientTotalDiscAmount = patientTotalDiscAmount;
    }

    @Deprecated
    public BigDecimal getPatientTotalTaxAmount() {
        return patientTotalTaxAmount;
    }

    @Deprecated
    public void setPatientTotalTaxAmount(BigDecimal patientTotalTaxAmount) {
        this.patientTotalTaxAmount = patientTotalTaxAmount;
    }

    public Boolean getPercentDiscount() {
        return isPercentDiscount;
    }

    public Boolean getItemDiscount() {
        return isItemDiscount;
    }

    public Float getEnteredUserDiscount() {
        return enteredUserDiscount;
    }

    public void setEnteredUserDiscount(Float enteredUserDiscount) {
        this.enteredUserDiscount = enteredUserDiscount;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cast) {
        this.cost = cast;
    }

    public HealthcareServiceCenterDTO getRenderingHSC() {
        return renderingHSC;
    }

    public void setRenderingHSC(HealthcareServiceCenterDTO renderingHSC) {
        this.renderingHSC = renderingHSC;
    }

    public List<TariffAddOnParametersDTO> getAddOnParams() {
        return addOnParams;
    }

    public void setAddOnParams(List<TariffAddOnParametersDTO> addOnParams) {
        this.addOnParams = addOnParams;
    }

    public Long getMedicationRequestDocLineId() {
        return medicationRequestDocLineId;
    }

    public void setMedicationRequestDocLineId(Long medicationRequestDocLineId) {
        this.medicationRequestDocLineId = medicationRequestDocLineId;
    }

    public Float getRequestedQunatity() {
        return requestedQunatity;
    }

    public void setRequestedQunatity(Float requestedQunatity) {
        this.requestedQunatity = requestedQunatity;
    }


    public Float getPreviousDispensedQty() {
        return previousDispensedQty;
    }

    public void setPreviousDispensedQty(Float previousDispensedQty) {
        this.previousDispensedQty = previousDispensedQty;
    }

    public List<DosageInstruction> getDosageInstruction() {
        return dosageInstruction;
    }

    public void setDosageInstruction(List<DosageInstruction> dosageInstruction) {
        this.dosageInstruction = dosageInstruction;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }


    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public Integer getPrintQuantity() {
        return printQuantity;
    }

    public void setPrintQuantity(Integer printQuantity) {
        this.printQuantity = printQuantity;
    }

    public String getStockBarCode() {
        return stockBarCode;
    }

    public void setStockBarCode(String stockBarCode) {
        this.stockBarCode = stockBarCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseDocumentLine that = (DispenseDocumentLine) o;

        if (lineNumber != null ? !lineNumber.equals(that.lineNumber) : that.lineNumber != null) return false;
        if (itemId != null ? !itemId.equals(that.itemId) : that.itemId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (medicationId != null ? !medicationId.equals(that.medicationId) : that.medicationId != null) return false;
        if (stockId != null ? !stockId.equals(that.stockId) : that.stockId != null) return false;
        if (stockQuantity != null ? !stockQuantity.equals(that.stockQuantity) : that.stockQuantity != null)
            return false;
        if (batchQuantity != null ? !batchQuantity.equals(that.batchQuantity) : that.batchQuantity != null)
            return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (batchNumber != null ? !batchNumber.equals(that.batchNumber) : that.batchNumber != null) return false;
        if (instruction != null ? !instruction.equals(that.instruction) : that.instruction != null) return false;
        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        if (expiryDate != null ? !expiryDate.equals(that.expiryDate) : that.expiryDate != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (substitute != null ? !substitute.equals(that.substitute) : that.substitute != null) return false;
        if (supplier != null ? !supplier.equals(that.supplier) : that.supplier != null) return false;
        if (uom != null ? !uom.equals(that.uom) : that.uom != null) return false;
        if (barCode != null ? !barCode.equals(that.barCode) : that.barCode != null) return false;
        if (sku != null ? !sku.equals(that.sku) : that.sku != null) return false;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (returnQuantity != null ? !returnQuantity.equals(that.returnQuantity) : that.returnQuantity != null)
            return false;
        if (mrp != null ? !mrp.equals(that.mrp) : that.mrp != null) return false;
        if (totalMrp != null ? !totalMrp.equals(that.totalMrp) : that.totalMrp != null) return false;
        if (saleRate != null ? !saleRate.equals(that.saleRate) : that.saleRate != null) return false;
        if (grossRate != null ? !grossRate.equals(that.grossRate) : that.grossRate != null) return false;
        if (saleAmount != null ? !saleAmount.equals(that.saleAmount) : that.saleAmount != null) return false;
        if (grossAmount != null ? !grossAmount.equals(that.grossAmount) : that.grossAmount != null) return false;
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) return false;
        if (taxDiscountAmount != null ? !taxDiscountAmount.equals(that.taxDiscountAmount) : that.taxDiscountAmount != null)
            return false;
        if (patientTaxAmount != null ? !patientTaxAmount.equals(that.patientTaxAmount) : that.patientTaxAmount != null)
            return false;
        if (sponsorDiscAmount != null ? !sponsorDiscAmount.equals(that.sponsorDiscAmount) : that.sponsorDiscAmount != null)
            return false;
        if (unitDiscount != null ? !unitDiscount.equals(that.unitDiscount) : that.unitDiscount != null) return false;
        if (userDiscount != null ? !userDiscount.equals(that.userDiscount) : that.userDiscount != null) return false;
        if (enteredUserDiscount != null ? !enteredUserDiscount.equals(that.enteredUserDiscount) : that.enteredUserDiscount != null)
            return false;
        if (totalDiscountAmount != null ? !totalDiscountAmount.equals(that.totalDiscountAmount) : that.totalDiscountAmount != null)
            return false;
        if (netAmount != null ? !netAmount.equals(that.netAmount) : that.netAmount != null) return false;
        if (patientGrossAmount != null ? !patientGrossAmount.equals(that.patientGrossAmount) : that.patientGrossAmount != null)
            return false;
        if (patientNetAmount != null ? !patientNetAmount.equals(that.patientNetAmount) : that.patientNetAmount != null)
            return false;
        if (patientTotalDiscAmount != null ? !patientTotalDiscAmount.equals(that.patientTotalDiscAmount) : that.patientTotalDiscAmount != null)
            return false;
        if (patientTotalTaxAmount != null ? !patientTotalTaxAmount.equals(that.patientTotalTaxAmount) : that.patientTotalTaxAmount != null)
            return false;
        if (patientSaleAmount != null ? !patientSaleAmount.equals(that.patientSaleAmount) : that.patientSaleAmount != null)
            return false;
        if (sponsorSaleAmount != null ? !sponsorSaleAmount.equals(that.sponsorSaleAmount) : that.sponsorSaleAmount != null)
            return false;
        if (sponsorGrossAmount != null ? !sponsorGrossAmount.equals(that.sponsorGrossAmount) : that.sponsorGrossAmount != null)
            return false;
        if (sponsorNetAmount != null ? !sponsorNetAmount.equals(that.sponsorNetAmount) : that.sponsorNetAmount != null)
            return false;
        if (sponsorTaxAmount != null ? !sponsorTaxAmount.equals(that.sponsorTaxAmount) : that.sponsorTaxAmount != null)
            return false;
        if (totalSponsorAmount != null ? !totalSponsorAmount.equals(that.totalSponsorAmount) : that.totalSponsorAmount != null)
            return false;
        if (isPercentDiscount != null ? !isPercentDiscount.equals(that.isPercentDiscount) : that.isPercentDiscount != null)
            return false;
        if (isItemDiscount != null ? !isItemDiscount.equals(that.isItemDiscount) : that.isItemDiscount != null)
            return false;
        if (consignment != null ? !consignment.equals(that.consignment) : that.consignment != null) return false;
        if (orderItem != null ? !orderItem.equals(that.orderItem) : that.orderItem != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (consultant != null ? !consultant.equals(that.consultant) : that.consultant != null) return false;
        if (dispenseItemPlans != null ? !dispenseItemPlans.equals(that.dispenseItemPlans) : that.dispenseItemPlans != null)
            return false;
        if (dispenseTaxes != null ? !dispenseTaxes.equals(that.dispenseTaxes) : that.dispenseTaxes != null)
            return false;
        if (totalTaxInPercent != null ? !totalTaxInPercent.equals(that.totalTaxInPercent) : that.totalTaxInPercent != null)
            return false;
        if (planDiscAmount != null ? !planDiscAmount.equals(that.planDiscAmount) : that.planDiscAmount != null)
            return false;
        if (executePlan != null ? !executePlan.equals(that.executePlan) : that.executePlan != null) return false;
        return cost != null ? cost.equals(that.cost) : that.cost == null;
    }

    @Override
    public int hashCode() {
        int result = lineNumber != null ? lineNumber.hashCode() : 0;
        result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (medicationId != null ? medicationId.hashCode() : 0);
        result = 31 * result + (stockId != null ? stockId.hashCode() : 0);
        result = 31 * result + (stockQuantity != null ? stockQuantity.hashCode() : 0);
        result = 31 * result + (batchQuantity != null ? batchQuantity.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (batchNumber != null ? batchNumber.hashCode() : 0);
        result = 31 * result + (instruction != null ? instruction.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        result = 31 * result + (substitute != null ? substitute.hashCode() : 0);
        result = 31 * result + (supplier != null ? supplier.hashCode() : 0);
        result = 31 * result + (uom != null ? uom.hashCode() : 0);
        result = 31 * result + (barCode != null ? barCode.hashCode() : 0);
        result = 31 * result + (sku != null ? sku.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (returnQuantity != null ? returnQuantity.hashCode() : 0);
        result = 31 * result + (mrp != null ? mrp.hashCode() : 0);
        result = 31 * result + (totalMrp != null ? totalMrp.hashCode() : 0);
        result = 31 * result + (saleRate != null ? saleRate.hashCode() : 0);
        result = 31 * result + (grossRate != null ? grossRate.hashCode() : 0);
        result = 31 * result + (saleAmount != null ? saleAmount.hashCode() : 0);
        result = 31 * result + (grossAmount != null ? grossAmount.hashCode() : 0);
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (taxDiscountAmount != null ? taxDiscountAmount.hashCode() : 0);
        result = 31 * result + (patientTaxAmount != null ? patientTaxAmount.hashCode() : 0);
        result = 31 * result + (sponsorDiscAmount != null ? sponsorDiscAmount.hashCode() : 0);
        result = 31 * result + (unitDiscount != null ? unitDiscount.hashCode() : 0);
        result = 31 * result + (userDiscount != null ? userDiscount.hashCode() : 0);
        result = 31 * result + (enteredUserDiscount != null ? enteredUserDiscount.hashCode() : 0);
        result = 31 * result + (totalDiscountAmount != null ? totalDiscountAmount.hashCode() : 0);
        result = 31 * result + (netAmount != null ? netAmount.hashCode() : 0);
        result = 31 * result + (patientGrossAmount != null ? patientGrossAmount.hashCode() : 0);
        result = 31 * result + (patientNetAmount != null ? patientNetAmount.hashCode() : 0);
        result = 31 * result + (patientTotalDiscAmount != null ? patientTotalDiscAmount.hashCode() : 0);
        result = 31 * result + (patientTotalTaxAmount != null ? patientTotalTaxAmount.hashCode() : 0);
        result = 31 * result + (patientSaleAmount != null ? patientSaleAmount.hashCode() : 0);
        result = 31 * result + (sponsorSaleAmount != null ? sponsorSaleAmount.hashCode() : 0);
        result = 31 * result + (sponsorGrossAmount != null ? sponsorGrossAmount.hashCode() : 0);
        result = 31 * result + (sponsorNetAmount != null ? sponsorNetAmount.hashCode() : 0);
        result = 31 * result + (sponsorTaxAmount != null ? sponsorTaxAmount.hashCode() : 0);
        result = 31 * result + (totalSponsorAmount != null ? totalSponsorAmount.hashCode() : 0);
        result = 31 * result + (isPercentDiscount != null ? isPercentDiscount.hashCode() : 0);
        result = 31 * result + (isItemDiscount != null ? isItemDiscount.hashCode() : 0);
        result = 31 * result + (consignment != null ? consignment.hashCode() : 0);
        result = 31 * result + (orderItem != null ? orderItem.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (consultant != null ? consultant.hashCode() : 0);
        result = 31 * result + (dispenseItemPlans != null ? dispenseItemPlans.hashCode() : 0);
        result = 31 * result + (dispenseTaxes != null ? dispenseTaxes.hashCode() : 0);
        result = 31 * result + (totalTaxInPercent != null ? totalTaxInPercent.hashCode() : 0);
        result = 31 * result + (planDiscAmount != null ? planDiscAmount.hashCode() : 0);
        result = 31 * result + (executePlan != null ? executePlan.hashCode() : 0);
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseDocumentLine{" +
            "lineNumber=" + lineNumber +
            ", itemId=" + itemId +
            ", owner='" + owner + '\'' +
            ", medicationId=" + medicationId +
            ", stockId=" + stockId +
            ", stockQuantity=" + stockQuantity +
            ", batchQuantity=" + batchQuantity +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", batchNumber='" + batchNumber + '\'' +
            ", instruction='" + instruction + '\'' +
            ", note='" + note + '\'' +
            ", expiryDate=" + expiryDate +
            ", locator=" + locator +
            ", substitute=" + substitute +
            ", supplier='" + supplier + '\'' +
            ", uom=" + uom +
            ", barCode='" + barCode + '\'' +
            ", sku='" + sku + '\'' +
            ", group='" + group + '\'' +
            ", quantity=" + quantity +
            ", returnQuantity=" + returnQuantity +
            ", mrp=" + mrp +
            ", totalMrp=" + totalMrp +
            ", saleRate=" + saleRate +
            ", grossRate=" + grossRate +
            ", saleAmount=" + saleAmount +
            ", grossAmount=" + grossAmount +
            ", taxAmount=" + taxAmount +
            ", taxDiscountAmount=" + taxDiscountAmount +
            ", patientTaxAmount=" + patientTaxAmount +
            ", sponsorDiscAmount=" + sponsorDiscAmount +
            ", unitDiscount=" + unitDiscount +
            ", userDiscount=" + userDiscount +
            ", enteredUserDiscount=" + enteredUserDiscount +
            ", totalDiscountAmount=" + totalDiscountAmount +
            ", netAmount=" + netAmount +
            ", patientGrossAmount=" + patientGrossAmount +
            ", patientNetAmount=" + patientNetAmount +
            ", patientTotalDiscAmount=" + patientTotalDiscAmount +
            ", patientTotalTaxAmount=" + patientTotalTaxAmount +
            ", patientSaleAmount=" + patientSaleAmount +
            ", sponsorSaleAmount=" + sponsorSaleAmount +
            ", sponsorGrossAmount=" + sponsorGrossAmount +
            ", sponsorNetAmount=" + sponsorNetAmount +
            ", sponsorTaxAmount=" + sponsorTaxAmount +
            ", totalSponsorAmount=" + totalSponsorAmount +
            ", isPercentDiscount=" + isPercentDiscount +
            ", isItemDiscount=" + isItemDiscount +
            ", pricingMethod=" + pricingMethod +
            ", consignment=" + consignment +
            ", orderItem=" + orderItem +
            ", source=" + source +
            ", consultant=" + consultant +
            ", dispenseItemPlans=" + dispenseItemPlans +
            ", dispenseTaxes=" + dispenseTaxes +
            ", totalTaxInPercent=" + totalTaxInPercent +
            ", planDiscAmount=" + planDiscAmount +
            ", executePlan=" + executePlan +
            ", cast=" + cost +
            ", calculatedGrossAmount=" + calculatedGrossAmount +
            ", calculatedPatientGrossAmount=" + calculatedPatientGrossAmount +
            ", calculatedSponsorGrossAmount=" + calculatedSponsorGrossAmount +
            ", itemUnitAvgCost=" + itemUnitAvgCost +
            ", planAuthorizationRuleDetail=" + planAuthorizationRuleDetail +
            ", planRuleDetail=" + planRuleDetail +
            '}';
    }

    /**
     * Split total tax amount for given tax definitions
     */
    @Deprecated
    public void splitTaxAmountBasedOnTaxDefinition() {
        getDispenseTaxes().forEach(dispenseTax -> {
            TaxCalculation taxCalculation = dispenseTax.getTaxDefinition().getTaxCalculation();
            BigDecimal taxAmount = multiply(getTaxAmount(), (divide(taxCalculation.getPercentage(), getTotalTaxInPercent())));
            dispenseTax.setTaxAmount(taxAmount);
        });
    }

    public void round() {
        this.setTotalMrp(roundOff(this.getTotalMrp(), 2));
        this.setUserDiscount(roundOff(this.getUserDiscount(), 2));
        this.setPlanDiscAmount(roundOff(this.getPlanDiscAmount(), 2));
        this.setUnitDiscount(roundOff(this.getUnitDiscount(), 2));
        this.setNetAmount(roundOff(this.getNetAmount(), 2));
        this.setSaleRate(roundOff(this.getSaleRate(), 6));
        this.setSaleAmount(roundOff(this.getSaleAmount(), 2));
        if (this.getDispenseTaxes() != null && !this.getDispenseTaxes().isEmpty()) {
            this.getDispenseTaxes().forEach(tax -> {
                tax.setTaxAmount(roundOff(tax.getTaxAmount(), 2));
                tax.setPatientTaxAmount(roundOff(tax.getPatientTaxAmount(), 2));
            });
        }
        if (this.getDispenseItemPlans() != null && !this.getDispenseItemPlans().isEmpty()) {
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

    public void reset() {
        this.setNetAmount(BigDecimalUtil.ZERO);
        this.setGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedSponsorGrossAmount(BigDecimalUtil.ZERO);
        this.setPatientSaleAmount(BigDecimalUtil.ZERO);
        this.setPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setPatientNetAmount(BigDecimalUtil.ZERO);
        this.setPatientTotalTaxAmount(BigDecimalUtil.ZERO);
        this.setPatientTaxAmount(BigDecimalUtil.ZERO);
        this.setSponsorDiscAmount(BigDecimalUtil.ZERO);
        this.setSponsorGrossAmount(BigDecimalUtil.ZERO);
        this.setSponsorNetAmount(BigDecimalUtil.ZERO);
        this.setSponsorSaleAmount(BigDecimalUtil.ZERO);
        this.setSponsorTaxAmount(BigDecimalUtil.ZERO);
        this.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        this.setPlanDiscAmount(BigDecimalUtil.ZERO);
        this.resetDiscounts();
        this.setDispenseItemPlans(new ArrayList<>());
        if (this.getDispenseTaxes() != null) {
            this.getDispenseTaxes().forEach(dispenseTax -> {
                dispenseTax.setTaxAmount(BigDecimalUtil.ZERO);
                dispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            });
        }
        this.setSaleRate(this.getMrp());
    }

    public void resetDiscounts() {
        this.setUserDiscount(BigDecimalUtil.ZERO);
        this.setPatientTotalDiscAmount(subtract(getPlanDiscAmount(), getSponsorDiscAmount()));
        this.setTotalDiscountAmount(BigDecimalUtil.ZERO);
        this.setUnitDiscount(BigDecimalUtil.ZERO);
        this.setTaxDiscountAmount(BigDecimalUtil.ZERO);
    }

   /* public void resetForPlan() {
        this.setSaleAmount(multiply(this.getSaleRate(), this.getQuantity()));
        this.setTotalMrp(multiply(this.getMrp(), this.getQuantity()));
    }*/

    public void resetLine(DispenseDocumentLine line) {
        line.setNetAmount(BigDecimalUtil.ZERO);
        line.setGrossAmount(BigDecimalUtil.ZERO);
        line.setPatientSaleAmount(BigDecimalUtil.ZERO);
        line.setPatientGrossAmount(BigDecimalUtil.ZERO);
        line.setPatientNetAmount(BigDecimalUtil.ZERO);
        line.setUserDiscount(BigDecimalUtil.ZERO);
        line.setPatientTotalDiscAmount(BigDecimalUtil.ZERO);
        line.setPatientTotalTaxAmount(BigDecimalUtil.ZERO);
        line.setPatientTaxAmount(BigDecimalUtil.ZERO);
        line.setSponsorGrossAmount(BigDecimalUtil.ZERO);
        line.setSponsorNetAmount(BigDecimalUtil.ZERO);
        line.setSponsorSaleAmount(BigDecimalUtil.ZERO);
        line.setSponsorTaxAmount(BigDecimalUtil.ZERO);
        line.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        line.setTotalDiscountAmount(BigDecimalUtil.ZERO);
        line.setSponsorDiscAmount(BigDecimalUtil.ZERO);
        line.setTaxDiscountAmount(BigDecimalUtil.ZERO);
        line.setPlanDiscAmount(BigDecimalUtil.ZERO);
        line.setDispenseItemPlans(new ArrayList<>());
        if (line.getDispenseTaxes() != null) {
            line.getDispenseTaxes().forEach(dispenseTax -> {
                dispenseTax.setTaxAmount(BigDecimalUtil.ZERO);
                dispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            });
        }
    }

    public Boolean getExecutePlan() {
        return executePlan;
    }

    public void setExecutePlan(Boolean executePlan) {
        this.executePlan = executePlan;
    }

    public void applyUnitDiscountPercentage(Float unitDiscountPercentage, boolean applyUnitDiscount) {
        int calculateScale = 2;
        if (applyUnitDiscount) {
            setUnitDiscount(
                roundOff(calculatePercentAmount(getPatientGrossAmount(), unitDiscountPercentage),
                    calculateScale));
        } else {
            setUnitDiscount(BigDecimalUtil.ZERO);
        }
    }

    public void applyUserPercentageDiscount(Float userDiscountPercentage, boolean applyUnitDiscount) {
        int calculateScale = 2;
        BigDecimal discountOnAmount = getPatientGrossAmount();
        if (!applyUnitDiscount) {
            discountOnAmount = roundOff(subtract(discountOnAmount, getPlanDiscAmount()).add(getSponsorDiscAmount()), calculateScale);
        }
        setUserDiscount(roundOff(calculatePercentAmount(discountOnAmount, userDiscountPercentage),
            calculateScale));
    }

    public void applyAmountDiscount(BigDecimal discountAmountForLine) {
        int calculateScale = 2;
        setUserDiscount(roundOff(subtract(discountAmountForLine, getUnitDiscount()), calculateScale));
    }

    public void applyLineSpecificDiscount(boolean hasPlanDiscount) {
        int calculateScale = 2;
        BigDecimal discountOnAmount = getPatientGrossAmount();
        if (hasPlanDiscount) {
            discountOnAmount = roundOff(subtract(discountOnAmount, getPlanDiscAmount()).add(getSponsorDiscAmount()), calculateScale);
        }
        if (getPercentDiscount()) {
            setUserDiscount(roundOff(calculatePercentAmount(discountOnAmount, getEnteredUserDiscount()), calculateScale));
        } else {
            setUserDiscount(roundOff(getEnteredUserDiscount(), calculateScale));
        }
    }

    /*public void summarizeDiscount() {
        int calculateScale = 2;
        BigDecimal allowableDiscountAmount = roundOff(getPatientGrossAmount().subtract(getUnitDiscount()).subtract(getPlanDiscAmount()).add(getSponsorDiscAmount()), calculateScale);
        if (lt(allowableDiscountAmount, getUserDiscount())) {
            setUserDiscount(allowableDiscountAmount);
        }
        if (ltZero(getUserDiscount())) {
            setUserDiscount(BigDecimalUtil.ZERO);
        }
        setPatientTotalDiscAmount(roundOff(add(getUnitDiscount(), getUserDiscount()), calculateScale));
        setTotalDiscountAmount(roundOff(sum(getPatientTotalDiscAmount(),getPlanDiscAmount(), getTaxAmount()), calculateScale));
    }*/

    /*public void resetUnitDiscountIfPlanDiscountExists(Float unitDiscountPercentage, boolean applyUnitDiscount) {
        int calculateScale = 2;
        BigDecimal discountOnAmount = getPatientGrossAmount();
        if (applyUnitDiscount) {
            setUnitDiscount(
                roundOff(calculatePercentAmount(discountOnAmount, unitDiscountPercentage),
                    calculateScale));
        } else {
            setUnitDiscount(BigDecimalUtil.ZERO);
        }
    }*/

    private AppliedOnBasePatientSponsor checkDiscountExists(AppliedOnBasePatientSponsor appliedOnBase) {
        if (appliedOnBase == null || appliedOnBase.getValue() == null || appliedOnBase.getValue() <= 0) {
            return null;
        }
        return appliedOnBase;
    }

    /*public boolean isPlanDiscountApplied() {
        AppliedOnBasePatientSponsor appliedOnBase = planRuleDetail != null ? checkDiscountExists(planRuleDetail.getAppliedOnBase()) : null;
        AppliedOnBasePatientSponsor appliedOnPatient = planRuleDetail != null ? checkDiscountExists(planRuleDetail.getAppliedOnPatientAmount()) : null;
        AppliedOnBasePatientSponsor appliedOnSponsor = planRuleDetail != null ? checkDiscountExists(planRuleDetail.getAppliedOnSponsorAmount()) : null;
        if (appliedOnBase != null || appliedOnPatient != null || appliedOnSponsor != null) {
            return true;
        }
        return false;
    }*/

    public void calculateSale() {
        setSaleRate(getMrp());
        if (planRuleDetail != null) {
            applyOnBaseDetailsIfAny(planRuleDetail);
        }
        //calculateGross();
        int calculationScale = 2;//TODO remove duplicate calling tomorrow.
        //setMrp(roundOff(getMrp(), calculationScale));
        setTotalMrp(roundOff(multiply(getQuantity(), getMrp()), calculationScale));
        setSaleAmount(roundOff(multiply(getQuantity(), getSaleRate()), calculationScale));
    }

    public void calculateGross() {
        int calculationScale = 2;
        //setMrp(roundOff(getMrp(), calculationScale));
        setTotalMrp(roundOff(multiply(getQuantity(), getMrp()), calculationScale));
        setSaleAmount(roundOff(multiply(getQuantity(), getSaleRate()), calculationScale));
        setGrossAmount(roundOff(subtract(getSaleAmount(), getTaxAmount()), calculationScale));
        setCalculatedGrossAmount(getGrossAmount());
        setGrossRate(roundOff(divide(getGrossAmount(), getQuantity()), 6));
    }

    public void calculatePatientGross(Float patientPaymentPercentage) {
        int calculationScale = 2;
        setPatientSaleAmount(roundOff(
            calculatePercentAmount(getSaleAmount(), patientPaymentPercentage),
            calculationScale));
        BigDecimal patientTax = calculateTotalTax(getPatientSaleAmount());
        setPatientGrossAmount(roundOff(subtract(getPatientSaleAmount(), patientTax), calculationScale));
        setCalculatedPatientGrossAmount(getPatientGrossAmount());
    }

    public void summarize(String taxCalculationType) {
        int calculationScale = 2;
        setPlanDiscAmount(roundOff(getPlanDiscAmount(), calculationScale));
        setSponsorDiscAmount(roundOff(getSponsorDiscAmount(), calculationScale));
        setUnitDiscount(roundOff(getUnitDiscount(), calculationScale));
        setUserDiscount(roundOff(getUserDiscount(), calculationScale));

        setPatientTotalDiscAmount(roundOff(sum(
            getUnitDiscount(), getUserDiscount(), returnPatientPlanDiscount()), calculationScale));
        setTotalDiscountAmount(roundOff(sum(
            getUnitDiscount(), getUserDiscount(), getPlanDiscAmount(), getTaxDiscountAmount()), calculationScale));

        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            setPatientNetAmount(roundOff(subtract(add(getPatientSaleAmount(),getSponsorTaxForPatient()), (sum(getPatientTotalDiscAmount(), getTaxDiscountAmount()))),
                calculationScale));
            setSponsorNetAmount(roundOff(subtract(getSponsorSaleAmount(), add(getSponsorDiscAmount(), getSponsorTaxForPatient())), calculationScale));
            setNetAmount(roundOff(subtract(getSaleAmount(), getTotalDiscountAmount()), calculationScale));
            setPatientNetAmount(returnZeroIfNegative(getPatientNetAmount()));
            setSponsorNetAmount(returnZeroIfNegative(getSponsorNetAmount()));
            setNetAmount(returnZeroIfNegative(getNetAmount()));
            setCalculatedGrossAmount(subtract(getNetAmount(), getTaxAmount()).add(getTotalDiscountAmount()));
            setCalculatedPatientGrossAmount(subtract(getPatientNetAmount(), getPatientTaxAmount()).add(getPatientTotalDiscAmount()).add(getTaxDiscountAmount()));
            setCalculatedSponsorGrossAmount(subtract(getSponsorNetAmount(), (getSponsorTaxAmount())).add(getSponsorDiscAmount()));
        } else {
            setPatientNetAmount(roundOff(
                add(getPatientGrossAmount(), subtract(getPatientTaxAmount(), (add(getPatientTotalDiscAmount(), getTaxDiscountAmount())))), calculationScale));
            setSponsorNetAmount(roundOff(add(getSponsorGrossAmount(), subtract(getSponsorTaxAmount(), getSponsorDiscAmount())), calculationScale));
            setGrossAmount(add(getPatientGrossAmount(), getSponsorGrossAmount()));
            setCalculatedGrossAmount(add(getCalculatedPatientGrossAmount(), getCalculatedSponsorGrossAmount()));
            setNetAmount(roundOff(subtract(getGrossAmount(), getTotalDiscountAmount()).add(getTaxAmount()), calculationScale));
        }
        setGrossAmount(subtract(getNetAmount(), getTaxAmount()).add(getTotalDiscountAmount()));
        setPatientGrossAmount(subtract(getPatientNetAmount(), getPatientTaxAmount()).add(getPatientTotalDiscAmount()).add(getTaxDiscountAmount()));
        setSponsorGrossAmount(subtract(getSponsorNetAmount(), (getSponsorTaxAmount())).add(getSponsorDiscAmount()));
        setPatientGrossAmount(returnZeroIfNegative(getPatientGrossAmount()));
        setSponsorGrossAmount(returnZeroIfNegative(getSponsorGrossAmount()));
        setGrossAmount(returnZeroIfNegative(getGrossAmount()));
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            setGrossRate(roundOff(divide(getGrossAmount(), getQuantity()), 6));
        }
    }

    /*public void calculateTax(Float patientTaxPercentage) {
        int calculationScale = 2;
        for (DispenseTax tax : dispenseTaxes) {
            BigDecimal taxAmount = divide(multiply(getTaxAmount(), tax.getTaxDefinition().getTaxCalculation().getPercentage()),  getTotalTaxInPercent());
            tax.setPatientTaxAmount(roundOff(
                calculatePercentAmount(taxAmount, patientTaxPercentage),
                calculationScale));
            tax.setTaxAmount(roundOff(subtract(taxAmount, tax.getPatientTaxAmount()), calculationScale));
        }

    }*/

    public void calculateTotalTax() {
        int calculationScale = 2;
        BigDecimal totalTaxAmount = BigDecimalUtil.ZERO;
        for (DispenseTax tax : dispenseTaxes) {
            BigDecimal taxAmount = reverseCalculateTaxAmount(getSaleAmount(),
                tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent);
            totalTaxAmount = add(totalTaxAmount, roundOff(taxAmount, calculationScale));
        }
        setTaxAmount(totalTaxAmount);
    }

    public BigDecimal calculateTotalTax(BigDecimal saleAmount) {
        int calculationScale = 2;
        BigDecimal totalTaxAmount = BigDecimalUtil.ZERO;
        for (DispenseTax tax : dispenseTaxes) {
            BigDecimal taxAmount = reverseCalculateTaxAmount(saleAmount, tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent);
            totalTaxAmount = add(totalTaxAmount, roundOff(taxAmount, calculationScale));
        }
        return totalTaxAmount;
    }

    public void calculateTax(String taxCalculationType) {
        BigDecimal applyTaxOnPatientAmount = getPatientSaleAmount();
        BigDecimal applyTaxOnSponsorAmount = getSponsorSaleAmount();
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            applyTaxOnPatientAmount = subtract(applyTaxOnPatientAmount, (sum(getUnitDiscount(), getUserDiscount(), returnPatientPlanDiscount())));
            applyTaxOnSponsorAmount = subtract(applyTaxOnSponsorAmount, getSponsorDiscAmount());
        }
        boolean isSponsorPayTax = false;
        if (planRuleDetail != null) {
            isSponsorPayTax = getPlanRuleDetail().isSponsorPayTax();
        } else if (getDispenseItemPlans() != null && !getDispenseItemPlans().isEmpty()) {
            isSponsorPayTax = getDispenseItemPlans().get(0).getPlanRuleDetail().isSponsorPayTax();
        }
        BigDecimal totalPatientTaxAmount = BigDecimalUtil.ZERO;
        BigDecimal totalSponsorTaxAmount = BigDecimalUtil.ZERO;
        setSponsorTaxForPatient(BigDecimalUtil.ZERO);
        for (DispenseTax tax : dispenseTaxes) {
            BigDecimal patientTaxAmount = roundOff(reverseCalculateTaxAmount(applyTaxOnPatientAmount, tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent), 2);
            BigDecimal sponsorTaxAmount = roundOff(reverseCalculateTaxAmount(applyTaxOnSponsorAmount, tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent), 2);
            if (isSponsorPayTax) {
                tax.setTaxAmount(sponsorTaxAmount);
                tax.setPatientTaxAmount(patientTaxAmount);
            } else {
                tax.setPatientTaxAmount(add(patientTaxAmount, sponsorTaxAmount));
                tax.setTaxAmount(BigDecimalUtil.ZERO);
                setSponsorTaxForPatient(add(getSponsorTaxForPatient(), sponsorTaxAmount));
            }
            totalPatientTaxAmount = add(totalPatientTaxAmount, tax.getPatientTaxAmount());
            totalSponsorTaxAmount = add(totalSponsorTaxAmount, tax.getTaxAmount());
        }
        setSponsorTaxForPatient(roundOff(getSponsorTaxForPatient(), 2));
        if (isSponsorPayTax) {
            setPatientTaxAmount(totalPatientTaxAmount);
            setSponsorTaxAmount(totalSponsorTaxAmount);
        } else {
            setPatientTaxAmount(totalPatientTaxAmount.add(totalSponsorTaxAmount));
            setSponsorTaxAmount(BigDecimalUtil.ZERO);
        }
        setTaxAmount(getPatientTaxAmount().add(getSponsorTaxAmount()));
        setTaxDiscountAmount(BigDecimalUtil.ZERO);
        if (getFullDiscount()) {
            setTaxDiscountAmount(getPatientTaxAmount());
            setFullDiscount(false);
        }
    }

    public BigDecimal returnPatientPlanDiscount() {
        return subtract(getPlanDiscAmount(), getSponsorDiscAmount());
    }

    public void roundOffToRequiredScale() {
        int requiredScale = 2;
        setTotalMrp(roundOff(getTotalMrp(), requiredScale));
        setPatientSaleAmount(roundOff(getPatientSaleAmount(), requiredScale));
        setPatientGrossAmount(roundOff(getPatientGrossAmount(), requiredScale));
        setPatientTotalDiscAmount(roundOff(getPatientTotalDiscAmount(), requiredScale));
        setPatientTaxAmount(roundOff(getPatientTaxAmount(), requiredScale));
        setPatientNetAmount(roundOff(getPatientNetAmount(), requiredScale));

        setSponsorSaleAmount(roundOff(getSponsorSaleAmount(), requiredScale));
        setSponsorGrossAmount(roundOff(getSponsorGrossAmount(), requiredScale));
        setSponsorDiscAmount(roundOff(getSponsorDiscAmount(), requiredScale));
        setPlanDiscAmount(roundOff(getPlanDiscAmount(), requiredScale));
        setSponsorTaxAmount(roundOff(getSponsorTaxAmount(), requiredScale));
        setSponsorNetAmount(roundOff(getSponsorNetAmount(), requiredScale));

        setTaxAmount(roundOff(getTaxAmount(), requiredScale));
        setSaleAmount(roundOff(getSaleAmount(), requiredScale));
        setGrossAmount(roundOff(getGrossAmount(), requiredScale));
        setTotalDiscountAmount(roundOff(getTotalDiscountAmount(), requiredScale));
        setNetAmount(roundOff(getNetAmount(), requiredScale));
        setUnitDiscount(roundOff(getUnitDiscount(), requiredScale));
        setUserDiscount(roundOff(getUserDiscount(), requiredScale));

        setCalculatedGrossAmount(roundOff(getCalculatedGrossAmount(), requiredScale));
        setCalculatedPatientGrossAmount(roundOff(getCalculatedPatientGrossAmount(), requiredScale));
        setCalculatedSponsorGrossAmount(roundOff(getCalculatedSponsorGrossAmount(), requiredScale));
    }

    public void applyOnBaseDetailsIfAny(PlanRuleDetail planRuleDetail) {
        AppliedOnBasePatientSponsor appliedOnBase = planRuleDetail.getAppliedOnBase();
        if(null!=planRuleDetail.getPricingMethod() && PricingMethod.Fixed_Sale.equals(planRuleDetail.getPricingMethod())){
            setMrp(this.getSellingPrice());
        }else if (null!=planRuleDetail.getPricingMethod() && PricingMethod.MRP.equals(planRuleDetail.getPricingMethod())){
            setMrp(this.getOriginalMRP());
        }
        BigDecimal saleRate = getMrp();
        if (appliedOnBase != null) {
            if (BaseSponsorPatientType.Discount.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    BigDecimal discAmt = calculatePercentAmount(getMrp(), appliedOnBase.getValue());
                    saleRate = subtract(saleRate, discAmt);
                } else if (BaseSponsorPatientValueType.Amount.equals(appliedOnBase.getValueType())) {
                    saleRate = subtract(saleRate, getBigDecimal(appliedOnBase.getValue()));
                }
            } else if (BaseSponsorPatientType.Addon.equals(appliedOnBase.getAppliedType())) {
                if (BaseSponsorPatientValueType.Percentage.equals(appliedOnBase.getValueType())) {
                    saleRate = add(saleRate, calculatePercentAmount(getMrp(), appliedOnBase.getValue()));
                } else if (BaseSponsorPatientValueType.Amount.equals(appliedOnBase.getValueType())) {
                    saleRate = add(saleRate, getBigDecimal(appliedOnBase.getValue()));
                }
            } else if (BaseSponsorPatientType.Fixed.equals(appliedOnBase.getAppliedType())) {
                saleRate = getBigDecimal(appliedOnBase.getValue());
            }
        }
        setSaleRate(saleRate);
    }

    public void calculateSponsorGross(Float sponsorPaymentPercentage) {
        int calculationScale = 2;
        setSponsorSaleAmount(roundOff(
            calculatePercentAmount(getSaleAmount(), sponsorPaymentPercentage),
            calculationScale));

        BigDecimal sponsorTax = calculateTotalTax(getSponsorSaleAmount());
        setSponsorGrossAmount(roundOff(subtract(getSponsorSaleAmount(), sponsorTax), calculationScale));
        setCalculatedSponsorGrossAmount(getSponsorGrossAmount());
    }

    public void applyOnSponsorAmount(PlanRuleDetail planRuleDetail, String taxCalculationType) {
        if (planRuleDetail.getSponsorPayment() == 0 || planRuleDetail.getAppliedOnSponsorAmount() == null) {
            return;
        }
        BigDecimal discOnAmount = getSponsorGrossAmount();
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = getSponsorSaleAmount();
        }
        appliedOnAmountType(discOnAmount, planRuleDetail.getAppliedOnSponsorAmount(), true);
    }

    public void applyOnPatientAmount(PlanRuleDetail planRuleDetail, String taxCalculationType) {
        if (planRuleDetail.getPatientCopayment() == 0 || planRuleDetail.getAppliedOnPatientAmount() == null) {
            return;
        }
        BigDecimal discOnAmount = getPatientGrossAmount();
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = getPatientSaleAmount();
        }
        appliedOnAmountType(discOnAmount, planRuleDetail.getAppliedOnPatientAmount(), false);
    }

    private void appliedOnAmountType(BigDecimal amount, AppliedOnBasePatientSponsor appliedOn, Boolean isForSponsor) {
        int calculationScale = 2;
        if (BaseSponsorPatientType.Discount.equals(appliedOn.getAppliedType())) {
            BigDecimal discAmount = BigDecimalUtil.ZERO;
            if (BaseSponsorPatientValueType.Percentage.equals(appliedOn.getValueType())) {
                discAmount = calculatePercentAmount(amount, appliedOn.getValue());
            } else if (BaseSponsorPatientValueType.Amount.equals(appliedOn.getValueType())) {
                discAmount = getBigDecimal(appliedOn.getValue());
            }
            discAmount = roundOff(discAmount, calculationScale);

            if(gt(discAmount, amount)) {
                discAmount = amount;
            }

            if (isForSponsor)
                setSponsorDiscAmount(discAmount);
            else
                setPatientTotalDiscAmount(discAmount);

            setPlanDiscAmount(add(getPlanDiscAmount(), discAmount));
        } else if (BaseSponsorPatientType.Addon.equals(appliedOn.getAppliedType())) {
            BigDecimal addonAmount = BigDecimalUtil.ZERO;
            if (BaseSponsorPatientValueType.Percentage.equals(appliedOn.getValueType())) {
                addonAmount = calculatePercentAmount(amount, appliedOn.getValue());
            } else if (BaseSponsorPatientValueType.Amount.equals(appliedOn.getValueType())) {
                addonAmount = getBigDecimal(appliedOn.getValue());
            }
            addonAmount = roundOff(addonAmount, calculationScale);
            if (isForSponsor)
                setSponsorGrossAmount(add(getSponsorGrossAmount(), addonAmount));
            else
                setPatientGrossAmount(add(getPatientGrossAmount(), addonAmount));
        }
    }

    public void calculateAppliedUserDiscount(String taxCalculationType) {

        BigDecimal discOnAmount = getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = getPatientSaleAmount();
        }

        if (gtZero(returnPatientPlanDiscount())) {
            discOnAmount = subtract(discOnAmount, returnPatientPlanDiscount());
        }
        if (gtZero(unitDiscount)) {
            discOnAmount = subtract(discOnAmount, unitDiscount);
        }
        BigDecimal appliedUserDisc = BigDecimalUtil.ZERO;
        if (isItemDiscount()) { //Line level user Discount either % or amount
            if (isPercentDiscount()) { //In case of %
                appliedUserDisc = multiply(discOnAmount, getEnteredUserDiscount() * 0.01f);
            } else { //In case of Amount
                appliedUserDisc = roundOff(getEnteredUserDiscount(), 2);
            }
        }
        appliedUserDisc = ltZero(appliedUserDisc) ? BigDecimalUtil.ZERO : roundOff(appliedUserDisc, 2);
        if (getFullDiscount()) {
            setTaxDiscountAmount(getPatientTaxAmount());
            // setFullDiscount(false);
        }
        setAppliedUserDiscount(appliedUserDisc);
        setUserDiscount(appliedUserDisc);
    }

    public void validateAndApplyDiscretionaryDiscount(String taxCalculationType) {

        BigDecimal discOnAmount = getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = getPatientSaleAmount();
        }

        BigDecimal allowedDiscretionaryDisc = subtract(discOnAmount, add(returnPatientPlanDiscount(), getUnitDiscount()));
        if (lt(allowedDiscretionaryDisc, getAppliedUserDiscount())) {
            setAppliedUserDiscount(allowedDiscretionaryDisc);
        }
        setUserDiscount(roundOff(getAppliedUserDiscount(), 2));
        setPatientTotalDiscAmount(roundOff(sum(returnPatientPlanDiscount(), getUnitDiscount(),getUserDiscount()), 2));
        setTotalDiscountAmount(roundOff(sum(getPatientTotalDiscAmount(), getSponsorDiscAmount(), getTaxDiscountAmount()), 2));
    }
    public void updateAliasDetails(){
        if(planRuleDetail != null && null != planRuleDetail.getAliasCode() && null != planRuleDetail.getAliasName()){
            if(null != this.getDispenseItemPlans()  && !this.getDispenseItemPlans().isEmpty()){
                this.getDispenseItemPlans().get(0).setAliasCode(planRuleDetail.getAliasCode());
                this.getDispenseItemPlans().get(0).setAliasName(planRuleDetail.getAliasName());
            }
        }
    }

    public void updateCoPayment(String taxCalculationType) {

        Float sponsorPayment = 0f;
        Float patientPayment = 100f; //Need to check, If sponsor co-pay is 100% than need to update patientPayment = 0%?
        if (planRuleDetail != null && planRuleDetail.getSponsorPayment() != null) {
            sponsorPayment = planRuleDetail.getSponsorPayment();
        }

        if (planRuleDetail != null && planRuleDetail.getPatientCopayment() != null) {
            patientPayment = planRuleDetail.getPatientCopayment();
        }

        calculateSponsorGross(sponsorPayment);
        calculatePatientGross(patientPayment);
        if (planRuleDetail != null) {
            applyOnSponsorAmount(planRuleDetail, taxCalculationType);
            applyOnPatientAmount(planRuleDetail, taxCalculationType);
        }
    }

    public void applyAuthorizationAmount(Map<Long, BigDecimal> authorizationBalanceAmount,String taxCalculationType) {
        if (null != planAuthorizationRuleDetail) {
            authorizationSponsorDiscount =new BigDecimal(0);
            BigDecimal balanceAmount = authorizationBalanceAmount.get(planAuthorizationRuleDetail.getId());
            if (null == balanceAmount) {
                balanceAmount = getBigDecimal(planAuthorizationRuleDetail.getAuthorizedAmount());
            }
            if (gtZero(balanceAmount)) {
                AuthorizationUtilization authorizationUtilization = new AuthorizationUtilization();
                authorizationUtilization.setPlanRuleId(planAuthorizationRuleDetail.getId());
                if(null != dispensePlan && null != dispensePlan.getPlanRef()) {
                    authorizationUtilization.setPlanId(dispensePlan.getPlanRef().getId());
                }
                BigDecimal appliedOnAmount = getSponsorNetAmount();
                if (gte(balanceAmount, appliedOnAmount)) {
                  //  if(gte(balanceAmount,getSponsorDiscAmount())){
                  //      authorizationSponsorDiscount = getSponsorDiscAmount();
                  //  }
                    balanceAmount = subtract(balanceAmount, appliedOnAmount);
                    authorizationUtilization.setUtilizedAmount(appliedOnAmount);
                } else {
                    authorizationUtilization.setUtilizedAmount(balanceAmount);
                    BigDecimal extraPatientPayable = subtract(appliedOnAmount, balanceAmount);
                    if(planRuleDetail.isSponsorPayTax()) {
                        BigDecimal sponsorTax =getSponsorTaxAmount();
                        BigDecimal newSponsorTax = calculateTotalTax(balanceAmount);
                        setSponsorGrossAmount(add(subtract(balanceAmount, newSponsorTax),getSponsorDiscAmount()));
                        setSponsorSaleAmount(add(balanceAmount,getSponsorDiscAmount()));
                        setSponsorNetAmount(balanceAmount);
                        setSponsorTaxAmount(newSponsorTax);
                        //BigDecimal extraTax = calculateTotalTax(extraPatientPayable);
                        BigDecimal extraTax = subtract(sponsorTax,newSponsorTax);
                        setPatientSaleAmount(add(getPatientSaleAmount(), extraPatientPayable));
                        setPatientNetAmount(add(getPatientNetAmount(), extraPatientPayable));
                        setPatientTaxAmount(add(getPatientTaxAmount(), extraTax));
                        setPatientGrossAmount(add(getPatientGrossAmount(), subtract(extraPatientPayable, extraTax)));
                        for (DispenseTax tax : dispenseTaxes) {
                            BigDecimal patientTaxAmount = roundOff(splitTaxAmount(getPatientTaxAmount(), tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent), 2);
                            BigDecimal sponsorTaxAmount = roundOff(splitTaxAmount(getSponsorTaxAmount(), tax.getTaxDefinition().getTaxCalculation().getPercentage(), totalTaxInPercent), 2);
                            tax.setTaxAmount(sponsorTaxAmount);
                            tax.setPatientTaxAmount(patientTaxAmount);
                        }
                    } else {
                        setPatientNetAmount(add(getPatientNetAmount(), extraPatientPayable));
                        setPatientSaleAmount(add(getPatientSaleAmount(), extraPatientPayable));
                        setPatientGrossAmount(subtract(getPatientSaleAmount(),(subtract(getPatientTaxAmount(), getSponsorTaxForPatient()))));
                        setSponsorGrossAmount(add(balanceAmount,getSponsorDiscAmount()));
                        setSponsorNetAmount(balanceAmount);
                        setSponsorTaxAmount(BigDecimalUtil.ZERO);
                        setSponsorSaleAmount(balanceAmount);
                    }
                    setNetAmount(sum(getPatientNetAmount(),getSponsorNetAmount()));
                    balanceAmount = BigDecimalUtil.ZERO;
                }

                getAuthorizationUtilizationList().add(authorizationUtilization);
                authorizationBalanceAmount.put(planAuthorizationRuleDetail.getId(), balanceAmount);
            } else {//
                removePlan(taxCalculationType);
            }
        }
    }

    public void removePlan(String taxCalculationType) {
        resetPlanDetails();
        doCalculationWithoutPlan(taxCalculationType);
    }

    private void doCalculationWithoutPlan(String taxCalculationType){
        calculateSale();
        calculateTotalTax();
        calculateGross();
        //co-payment
        updateCoPayment(taxCalculationType);
        calculateTax(taxCalculationType);
        summarize(taxCalculationType);
    }

    private void resetPlanDetails(){
        setDispenseItemPlans(null);
        setPlanRuleDetail(null);
        setDispensePlan(null);
        this.setNetAmount(BigDecimalUtil.ZERO);
        this.setGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedSponsorGrossAmount(BigDecimalUtil.ZERO);
        this.setPatientSaleAmount(BigDecimalUtil.ZERO);
        this.setPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setPatientNetAmount(BigDecimalUtil.ZERO);
        this.setPatientTotalTaxAmount(BigDecimalUtil.ZERO);
        this.setPatientTaxAmount(BigDecimalUtil.ZERO);
        this.setSponsorDiscAmount(BigDecimalUtil.ZERO);
        this.setSponsorGrossAmount(BigDecimalUtil.ZERO);
        this.setSponsorNetAmount(BigDecimalUtil.ZERO);
        this.setSponsorSaleAmount(BigDecimalUtil.ZERO);
        this.setSponsorTaxAmount(BigDecimalUtil.ZERO);
        this.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        this.setPlanDiscAmount(BigDecimalUtil.ZERO);
        this.setPatientTotalDiscAmount(subtract(getPlanDiscAmount(), getSponsorDiscAmount()));
        this.setTotalDiscountAmount(BigDecimalUtil.ZERO);
        this.setTaxDiscountAmount(BigDecimalUtil.ZERO);
        this.setDispenseItemPlans(new ArrayList<>());
        if (this.getDispenseTaxes() != null) {
            this.getDispenseTaxes().forEach(dispenseTax -> {
                dispenseTax.setTaxAmount(BigDecimalUtil.ZERO);
                dispenseTax.setPatientTaxAmount(BigDecimalUtil.ZERO);
            });
        }
        this.setSaleRate(this.getMrp());
    }

    // set item level plan details
    public void setItemLevelPlanDetails() {
        if (dispensePlan == null) {
            return;
        }
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
        itemPlan.setPatientNetAmount(subtract(itemPlan.getPatientGrossAmount(), itemPlan.getPatientDiscAmount()).add(itemPlan.getPatientTaxAmount()));
       // if(null == planAuthorizationRuleDetail){
            itemPlan.setSponsorNetAmount(subtract(itemPlan.getSponsorGrossAmount(), itemPlan.getSponsorDiscAmount()).add(itemPlan.getSponsorTaxAmount()));
       // } else {
       //     itemPlan.setSponsorNetAmount(subtract(add(itemPlan.getSponsorGrossAmount(), itemPlan.getSponsorTaxAmount()),getAuthorizationSponsorDiscount()));
       // }

        List<DispenseItemPlan> itemPlans = new ArrayList<>();
        itemPlans.add(itemPlan);
        setDispenseItemPlans(itemPlans);
    }

    /**
     * Method takes PlanRuleDetail document and synchronously checks if the PlanRuleDetail can be assigned to this line
     * check given planRuleDetail is from excluded plan
     * @param planRuleDetail
     */
    public synchronized boolean checkAndAddPlanRuleDetail(PlanRuleDetail planRuleDetail) {
        if (!excludedPlans.contains(planRuleDetail.getPlanRule().getTypeId()) && null == this.planRuleDetail) {
            this.setPlanRuleDetail(planRuleDetail);
            return true;
        }
        return false;
    }

    /**
     * Method takes PlanRuleDetail document and synchronously checks if the PlanRuleDetail can be removed to this line
     * Add excluded planId in excludedPlans, So other rules of that plan should not include this item.
     * @param planRuleDetail
     */
    public synchronized boolean checkAndRemovePlanRuleDetail(PlanRuleDetail planRuleDetail) {

        if(null != planRuleDetail) {
            excludedPlans.add(planRuleDetail.getPlanRule().getTypeId());
        }

        if (this.planRuleDetail != null) {
            this.setPlanRuleDetail(null);
            return true;
        }
        return false;
    }

    public Days getDayOfWeeK() {
        switch (LocalDateTime.now().getDayOfWeek().getValue()) {
            case 0:
                return Days.Sunday;
            case 1:
                return Days.Monday;
            case 2:
                return Days.Tuesday;
            case 3:
                return Days.Wednesday;
            case 4:
                return Days.Thursday;
            case 5:
                return Days.Friday;
            default:
                return Days.Saturday;
        }
    }

    public void applyUnitExceptionDiscount(Float unitDiscountPercentage,String taxCalculationType) {
        BigDecimal discOnAmount = getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = getPatientSaleAmount();
        }
        BigDecimal unitDiscount = CalculateTaxUtil.calculatePercentAmount(discOnAmount, unitDiscountPercentage);
        setUnitDiscount(unitDiscount);
    }
}
