package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.Days;
import org.nh.billing.domain.enumeration.PlanRuleType;
import org.nh.billing.domain.enumeration.VisitType;
import org.nh.common.dto.GroupDTO;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Document(indexName = "planruledocument", type = "planruledocument",createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class PlanRuleDocument implements Serializable {

    @Id
    private Long id;

    private PlanRuleType planRuleType;

    private List<VisitType> visitType;

    private String gender;

    private Integer minAge;

    private Integer maxAge;

    private Days days;

    private Float minAmount;

    private Float maxAmount;

    private Integer minQuantity;

    private Integer maxQuantity;

    private Boolean active;

    private Boolean authorizationExclusion;

    private Boolean exclusion;

    private Float patientCopayment;

    private Float sponsorPayment;

    private AppliedOnBasePatientSponsor appliedOnBase;

    private AppliedOnBasePatientSponsor appliedOnPatientAmount;

    private AppliedOnBasePatientSponsor appliedOnSponsorAmount;

    private Boolean isGeneric;

    private GroupDTO group;

    private String aliasName;

    private String aliasCode;

    private boolean sponsorPayTax;

    private String tarrifClass;

    private String tarrifClassValue;

    private Long parentRuleId;

    private Long level;

    private PlanRuleComponent component;

    private ItemGroup itemGroup;

    private ItemCategory itemCategory;

    private org.nh.billing.domain.dto.ItemType itemType;

    private String type;

    private Long typeId;

    private String typeCode;

    private Integer typeLevel;

    private Integer version;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public PlanRuleType getPlanRuleType() {
        return planRuleType;
    }

    public void setPlanRuleType(PlanRuleType planRuleType) {
        this.planRuleType = planRuleType;
    }

    public List<VisitType> getVisitType() {
        return visitType;
    }

    public void setVisitType(List<VisitType> visitType) {
        this.visitType = visitType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Days getDays() {
        return days;
    }

    public void setDays(Days days) {
        this.days = days;
    }

    public Float getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Float minAmount) {
        this.minAmount = minAmount;
    }

    public Float getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Float maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAuthorizationExclusion() {
        return authorizationExclusion;
    }

    public void setAuthorizationExclusion(Boolean authorizationExclusion) {
        this.authorizationExclusion = authorizationExclusion;
    }

    public Boolean getExclusion() {
        return exclusion;
    }

    public void setExclusion(Boolean exclusion) {
        this.exclusion = exclusion;
    }

    public Float getPatientCopayment() {
        return patientCopayment;
    }

    public void setPatientCopayment(Float patientCopayment) {
        this.patientCopayment = patientCopayment;
    }

    public Float getSponsorPayment() {
        return sponsorPayment;
    }

    public void setSponsorPayment(Float sponsorPayment) {
        this.sponsorPayment = sponsorPayment;
    }

    public AppliedOnBasePatientSponsor getAppliedOnBase() {
        return appliedOnBase;
    }

    public void setAppliedOnBase(AppliedOnBasePatientSponsor appliedOnBase) {
        this.appliedOnBase = appliedOnBase;
    }

    public AppliedOnBasePatientSponsor getAppliedOnPatientAmount() {
        return appliedOnPatientAmount;
    }

    public void setAppliedOnPatientAmount(AppliedOnBasePatientSponsor appliedOnPatientAmount) {
        this.appliedOnPatientAmount = appliedOnPatientAmount;
    }

    public AppliedOnBasePatientSponsor getAppliedOnSponsorAmount() {
        return appliedOnSponsorAmount;
    }

    public void setAppliedOnSponsorAmount(AppliedOnBasePatientSponsor appliedOnSponsorAmount) {
        this.appliedOnSponsorAmount = appliedOnSponsorAmount;
    }

    public Boolean getGeneric() {
        return isGeneric;
    }

    public void setGeneric(Boolean generic) {
        isGeneric = generic;
    }

    public GroupDTO getGroup() {
        return group;
    }

    public void setGroup(GroupDTO group) {
        this.group = group;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public boolean isSponsorPayTax() {
        return sponsorPayTax;
    }

    public void setSponsorPayTax(boolean sponsorPayTax) {
        this.sponsorPayTax = sponsorPayTax;
    }

    public String getTarrifClass() {
        return tarrifClass;
    }

    public void setTarrifClass(String tarrifClass) {
        this.tarrifClass = tarrifClass;
    }

    public String getTarrifClassValue() {
        return tarrifClassValue;
    }

    public void setTarrifClassValue(String tarrifClassValue) {
        this.tarrifClassValue = tarrifClassValue;
    }

    public Long getParentRuleId() {
        return parentRuleId;
    }

    public void setParentRuleId(Long parentRuleId) {
        this.parentRuleId = parentRuleId;
    }

    public Long getLevel() {
        return level;
    }

    public void setLevel(Long level) {
        this.level = level;
    }

    public PlanRuleComponent getComponent() {
        return component;
    }

    public void setComponent(PlanRuleComponent component) {
        this.component = component;
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    public org.nh.billing.domain.dto.ItemType getItemType() {
        return itemType;
    }

    public void setItemType(org.nh.billing.domain.dto.ItemType itemType) {
        this.itemType = itemType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Integer getTypeLevel() {
        return typeLevel;
    }

    public void setTypeLevel(Integer typeLevel) {
        this.typeLevel = typeLevel;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanRuleDocument)) return false;
        PlanRuleDocument that = (PlanRuleDocument) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "PlanRuleDetailDocument{" +
            "id='" + id + '\'' +
            ", planRuleType=" + planRuleType +
            ", visitType=" + visitType +
            ", gender='" + gender + '\'' +
            ", minAge=" + minAge +
            ", maxAge=" + maxAge +
            ", days=" + days +
            ", minAmount=" + minAmount +
            ", maxAmount=" + maxAmount +
            ", minQuantity=" + minQuantity +
            ", maxQuantity=" + maxQuantity +
            ", active=" + active +
            ", authorizationExclusion=" + authorizationExclusion +
            ", exclusion=" + exclusion +
            ", patientCopayment=" + patientCopayment +
            ", sponsorPayment=" + sponsorPayment +
            ", appliedOnBase=" + appliedOnBase +
            ", appliedOnPatientAmount=" + appliedOnPatientAmount +
            ", appliedOnSponsorAmount=" + appliedOnSponsorAmount +
            ", isGeneric=" + isGeneric +
            ", group=" + group +
            ", aliasName='" + aliasName + '\'' +
            ", aliasCode='" + aliasCode + '\'' +
            ", sponsorPayTax=" + sponsorPayTax +
            ", tarrifClass='" + tarrifClass + '\'' +
            ", tarrifClassValue='" + tarrifClassValue + '\'' +
            ", parentRuleId=" + parentRuleId +
            ", level=" + level +
            ", component=" + component +
            ", itemGroup=" + itemGroup +
            ", itemCategory=" + itemCategory +
            ", itemType=" + itemType +
            ", type='" + type + '\'' +
            ", typeId=" + typeId +
            ", typeCode='" + typeCode + '\'' +
            ", typeLevel=" + typeLevel +
            ", version=" + version +
            ", effectiveFrom=" + effectiveFrom +
            ", effectiveTo=" + effectiveTo +
            '}';
    }

}
