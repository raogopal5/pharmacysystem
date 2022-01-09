package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.*;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.DispenseType;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.nh.pharmacy.util.CalculateTaxUtil;
import org.nh.pharmacy.util.MarginBasedDiscountUtil;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nh.common.util.BigDecimalUtil.*;

/**
 * Created by Nirbhay on 6/7/17.
 */
public class DispenseDocument implements Serializable {

    private String dispenseNumber;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO hsc;

    @Field(type = FieldType.Object)
    private UserDTO createdBy;
    @Field(type = FieldType.Date)
    private LocalDateTime createdDate;
    @Field(type = FieldType.Date)
    private LocalDateTime dispenseDate;
    private UserDTO approvedBy;
    @Field(type = FieldType.Date)
    private LocalDateTime approvedDate;

    @Field(type = FieldType.Object)
    private UserDTO dispenseUser;

    private OrganizationDTO dispenseUnit;
    private DispenseDocument partOf;

    @Field(type = FieldType.Object)
    private SourceDTO source;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private DispenseStatus dispenseStatus;

    @Field(type = FieldType.Object)
    private EncounterDTO encounter;

    @Field(type = FieldType.Object)
    private PatientDTO patient;

    private Map<String, Object> orderSource;
    private List<DispensePlan> dispensePlans;
    private List<DispenseTax> dispenseTaxes;
    private List<DispenseDocumentLine> dispenseDocumentLines;
    private List<PaymentDetail> paymentDetails;
    private BigDecimal grossAmount = BigDecimalUtil.ZERO;
    private BigDecimal netAmount = BigDecimalUtil.ZERO; //net Payable

    @Mapping(mappingPath = "/es/big-decimal-mapping.json")
    private BigDecimal patientNetAmount = BigDecimalUtil.ZERO; //patient net payable
    private BigDecimal patientSaleAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientGrossAmount = BigDecimalUtil.ZERO; //patient Gross
    private BigDecimal totalSponsorAmount = BigDecimalUtil.ZERO;//sponsor payable
    private BigDecimal patientDiscount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorDiscount = BigDecimalUtil.ZERO;
    private BigDecimal roundOff = BigDecimalUtil.ZERO;
    private BigDecimal discountAmount = BigDecimalUtil.ZERO;

    @Mapping(mappingPath = "/es/big-decimal-mapping.json")
    private BigDecimal patientPaidAmount = BigDecimalUtil.ZERO;

    private Float totalDiscountPercentage = 0f;
    private BigDecimal userDiscountAmount = BigDecimalUtil.ZERO;
    private BigDecimal fullDiscountFlag=BigDecimalUtil.ZERO;
    private Float userDiscountPercentage = 0f;
    private Float unitDiscountPercentage = 0f;
    private BigDecimal unitDiscountAmount = BigDecimalUtil.ZERO;

    @Field(type = FieldType.Object)
    private ConsultantDTO consultant;

    private Boolean isDraft = true;
    private BigDecimal taxDiscount = BigDecimalUtil.ZERO;
    private TransactionType documentType = TransactionType.Dispense;
    private Boolean isDiscountPercentage = Boolean.TRUE;
    private BigDecimal planDiscountAmount;
    private Boolean sendEmail = true;
    @Field(type = FieldType.Date)
    private LocalDateTime modifiedDate;
    private BigDecimal saleAmount = BigDecimalUtil.ZERO;
    private BigDecimal sponsorNetAmount = BigDecimalUtil.ZERO;
    @Transient
    private String taxCalculationType;
    @Transient
    private String discountType;
    @Transient
    private String discountFormula;
    @Transient
    private String discountSlab;
    private BigDecimal calculatedGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedPatientGrossAmount = BigDecimalUtil.ZERO;
    private BigDecimal calculatedSponsorGrossAmount = BigDecimalUtil.ZERO;
    private String transactionCurrency;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO orderingHSC;

    @Field(type = FieldType.Object)
    private OrganizationDTO department;

    private LocalDateTime orderedDate;

    private VersionDTO orderedPackageVersion;

    private List<SourceDTO> sourceDTOList;

    private DispenseType dispenseType=DispenseType.ORDER;

    private String remarks;

    @Field(type = FieldType.Object)
    private OrganizationDTO orderingUnit;


    @Transient
    private Map<Long, BigDecimal> authorizationBalanceAmount = new HashMap<>();

    public BigDecimal getFullDiscountFlag() {
        return fullDiscountFlag;
    }

    public void setFullDiscountFlag(BigDecimal fullDiscountFlag) {
        this.fullDiscountFlag = fullDiscountFlag;
    }

    public Map<Long, BigDecimal> getAuthorizationBalanceAmount() {return authorizationBalanceAmount;}

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getDiscountFormula() {
        return discountFormula;
    }

    public void setDiscountFormula(String discountFormula) {
        this.discountFormula = discountFormula;
    }

    public String getDiscountSlab() {
        return discountSlab;
    }

    public void setDiscountSlab(String discountSlab) {
        this.discountSlab = discountSlab;
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

    public String getTaxCalculationType() {
        return taxCalculationType;
    }

    public void setTaxCalculationType(String taxCalculationType) {
        this.taxCalculationType = taxCalculationType;
    }

    private Boolean applyUnitDiscount = true;

    public Boolean getApplyUnitDiscount() {
        return applyUnitDiscount;
    }

    public void setApplyUnitDiscount(Boolean applyUnitDiscount) {
        this.applyUnitDiscount = applyUnitDiscount;
    }

    @Field(type = FieldType.Object)
    private StockSource stockSource;

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public BigDecimal getPatientGrossAmount() {
        return patientGrossAmount;
    }

    public void setPatientGrossAmount(BigDecimal patientGrossAmount) {
        this.patientGrossAmount = patientGrossAmount;
    }

    public BigDecimal getRoundOff() {
        return roundOff;
    }

    public void setRoundOff(BigDecimal roundOff) {
        this.roundOff = roundOff;
    }

    public Float getTotalDiscountPercentage() {
        return totalDiscountPercentage;
    }

    public void setTotalDiscountPercentage(Float totalDiscountPercentage) {
        this.totalDiscountPercentage = totalDiscountPercentage;
    }

    public Boolean isDiscountPercentage() {
        return isDiscountPercentage;
    }

    public void setDiscountPercentage(Boolean discountPercentage) {
        isDiscountPercentage = discountPercentage;
    }

    public Float getUnitDiscountPercentage() {
        return unitDiscountPercentage;
    }

    public void setUnitDiscountPercentage(Float unitDiscountPercentage) {
        this.unitDiscountPercentage = unitDiscountPercentage;
    }

    public BigDecimal getUnitDiscountAmount() {
        return unitDiscountAmount;
    }

    public void setUnitDiscountAmount(BigDecimal unitDiscountAmount) {
        this.unitDiscountAmount = unitDiscountAmount;
    }

    public TransactionType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    public BigDecimal getSponsorDiscount() {
        return sponsorDiscount;
    }

    public void setSponsorDiscount(BigDecimal sponsorDiscount) {
        this.sponsorDiscount = sponsorDiscount;
    }

    public BigDecimal getPatientDiscount() {
        return patientDiscount;
    }

    public void setPatientDiscount(BigDecimal patientDiscount) {
        this.patientDiscount = patientDiscount;
    }

    public BigDecimal getTaxDiscount() {
        return taxDiscount;
    }

    public void setTaxDiscount(BigDecimal taxDiscount) {
        this.taxDiscount = taxDiscount;
    }

    public BigDecimal getUserDiscountAmount() {
        return userDiscountAmount;
    }

    public void setUserDiscountAmount(BigDecimal userDiscountAmount) {
        this.userDiscountAmount = userDiscountAmount;
    }

    public Float getUserDiscountPercentage() {
        return userDiscountPercentage;
    }

    public void setUserDiscountPercentage(Float userDiscountPercentage) {
        this.userDiscountPercentage = userDiscountPercentage;
    }

    public String getDispenseNumber() {
        return dispenseNumber;
    }

    public void setDispenseNumber(String dispenseNumber) {
        this.dispenseNumber = dispenseNumber;
    }

    public HealthcareServiceCenterDTO getHsc() {
        return hsc;
    }

    public void setHsc(HealthcareServiceCenterDTO hsc) {
        this.hsc = hsc;
    }

    public LocalDateTime getDispenseDate() {
        return dispenseDate;
    }

    public void setDispenseDate(LocalDateTime dispenseDate) {
        this.dispenseDate = dispenseDate;
    }

    public UserDTO getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UserDTO approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public UserDTO getDispenseUser() {
        return dispenseUser;
    }

    public void setDispenseUser(UserDTO dispenseUser) {
        this.dispenseUser = dispenseUser;
    }

    public OrganizationDTO getDispenseUnit() {
        return dispenseUnit;
    }

    public void setDispenseUnit(OrganizationDTO dispenseUnit) {
        this.dispenseUnit = dispenseUnit;
    }

    public DispenseDocument getPartOf() {
        return partOf;
    }

    public void setPartOf(DispenseDocument partOf) {
        this.partOf = partOf;
    }

    public SourceDTO getSource() {
        return source;
    }

    public void setSource(SourceDTO source) {
        this.source = source;
    }

    public DispenseStatus getDispenseStatus() {
        return dispenseStatus;
    }

    public void setDispenseStatus(DispenseStatus dispenseStatus) {
        this.dispenseStatus = dispenseStatus;
    }

    public EncounterDTO getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterDTO encounter) {
        this.encounter = encounter;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public Map<String, Object> getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(Map<String, Object> orderSource) {
        this.orderSource = orderSource;
    }

    public List<DispensePlan> getDispensePlans() {
        return dispensePlans;
    }

    public void setDispensePlans(List<DispensePlan> dispensePlans) {
        this.dispensePlans = dispensePlans;
    }

    public List<DispenseTax> getDispenseTaxes() {
        return dispenseTaxes;
    }

    public void setDispenseTaxes(List<DispenseTax> dispenseTaxes) {
        this.dispenseTaxes = dispenseTaxes;
    }

    public List<DispenseDocumentLine> getDispenseDocumentLines() {
        return dispenseDocumentLines;
    }

    public void setDispenseDocumentLines(List<DispenseDocumentLine> dispenseDocumentLines) {
        this.dispenseDocumentLines = dispenseDocumentLines;
    }

    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<PaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getPatientNetAmount() {
        return patientNetAmount;
    }

    public void setPatientNetAmount(BigDecimal patientNetAmount) {
        this.patientNetAmount = patientNetAmount;
    }

    public BigDecimal getPatientSaleAmount() {
        return patientSaleAmount;
    }

    public void setPatientSaleAmount(BigDecimal patientSaleAmount) {
        this.patientSaleAmount = patientSaleAmount;
    }

    @Deprecated
    public BigDecimal getTotalSponsorAmount() {
        return totalSponsorAmount;
    }

    @Deprecated
    public void setTotalSponsorAmount(BigDecimal totalSponsorAmount) {
        this.totalSponsorAmount = totalSponsorAmount;
    }

    public BigDecimal getPatientPaidAmount() {
        return patientPaidAmount;
    }

    public void setPatientPaidAmount(BigDecimal patientPaidAmount) {
        this.patientPaidAmount = patientPaidAmount;
    }

    public ConsultantDTO getConsultant() {
        return consultant;
    }

    public void setConsultant(ConsultantDTO consultant) {
        this.consultant = consultant;
    }

    public Boolean getDraft() {
        return isDraft;
    }

    public Boolean isDraft() {
        return isDraft;
    }

    public void setDraft(Boolean draft) {
        isDraft = draft;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public Boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getDiscountPercentage() {
        return isDiscountPercentage;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getSponsorNetAmount() {
        return sponsorNetAmount;
    }

    public void setSponsorNetAmount(BigDecimal sponsorNetAmount) {
        this.sponsorNetAmount = sponsorNetAmount;
    }

    public StockSource getStockSource() {
        return stockSource;
    }

    public void setStockSource(StockSource stockSource) {
        this.stockSource = stockSource;
    }

    public HealthcareServiceCenterDTO getOrderingHSC() {
        return orderingHSC;
    }

    public void setOrderingHSC(HealthcareServiceCenterDTO orderingHSC) {
        this.orderingHSC = orderingHSC;
    }

    public OrganizationDTO getDepartment() {
        return department;
    }

    public void setDepartment(OrganizationDTO department) {
        this.department = department;
    }

    public LocalDateTime getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(LocalDateTime orderedDate) {
        this.orderedDate = orderedDate;
    }

    public VersionDTO getOrderedPackageVersion() {
        return orderedPackageVersion;
    }

    public void setOrderedPackageVersion(VersionDTO orderedPackageVersion) {
        this.orderedPackageVersion = orderedPackageVersion;
    }

    public List<SourceDTO> getSourceDTOList() {
        return sourceDTOList;
    }

    public void setSourceDTOList(List<SourceDTO> sourceDTOList) {
        this.sourceDTOList = sourceDTOList;
    }

    public DispenseType getDispenseType() {
        return dispenseType;
    }

    public void setDispenseType(DispenseType dispenseType) {
        this.dispenseType = dispenseType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public OrganizationDTO getOrderingUnit() {
        return orderingUnit;
    }

    public void setOrderingUnit(OrganizationDTO orderingUnit) {
        this.orderingUnit = orderingUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseDocument that = (DispenseDocument) o;

        if (dispenseNumber != null ? !dispenseNumber.equals(that.dispenseNumber) : that.dispenseNumber != null)
            return false;
        if (hsc != null ? !hsc.equals(that.hsc) : that.hsc != null) return false;
        if (dispenseDate != null ? !dispenseDate.equals(that.dispenseDate) : that.dispenseDate != null) return false;
        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (approvedDate != null ? !approvedDate.equals(that.approvedDate) : that.approvedDate != null) return false;
        if (dispenseUser != null ? !dispenseUser.equals(that.dispenseUser) : that.dispenseUser != null) return false;
        if (dispenseUnit != null ? !dispenseUnit.equals(that.dispenseUnit) : that.dispenseUnit != null) return false;
        if (partOf != null ? !partOf.equals(that.partOf) : that.partOf != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (dispenseStatus != that.dispenseStatus) return false;
        if (encounter != null ? !encounter.equals(that.encounter) : that.encounter != null) return false;
        if (patient != null ? !patient.equals(that.patient) : that.patient != null) return false;
        if (orderSource != null ? !orderSource.equals(that.orderSource) : that.orderSource != null) return false;
        if (dispensePlans != null ? !dispensePlans.equals(that.dispensePlans) : that.dispensePlans != null)
            return false;
        if (dispenseTaxes != null ? !dispenseTaxes.equals(that.dispenseTaxes) : that.dispenseTaxes != null)
            return false;
        if (dispenseDocumentLines != null ? !dispenseDocumentLines.equals(that.dispenseDocumentLines) : that.dispenseDocumentLines != null)
            return false;
        if (paymentDetails != null ? !paymentDetails.equals(that.paymentDetails) : that.paymentDetails != null)
            return false;
        if (grossAmount != null ? !grossAmount.equals(that.grossAmount) : that.grossAmount != null) return false;
        if (discountAmount != null ? !discountAmount.equals(that.discountAmount) : that.discountAmount != null)
            return false;
        if (netAmount != null ? !netAmount.equals(that.netAmount) : that.netAmount != null) return false;
        if (totalSponsorAmount != null ? !totalSponsorAmount.equals(that.totalSponsorAmount) : that.totalSponsorAmount != null)
            return false;
        if (patientPaidAmount != null ? !patientPaidAmount.equals(that.patientPaidAmount) : that.patientPaidAmount != null)
            return false;
        if (userDiscountAmount != null ? !userDiscountAmount.equals(that.userDiscountAmount) : that.userDiscountAmount != null)
            return false;
        if (userDiscountPercentage != null ? !userDiscountPercentage.equals(that.userDiscountPercentage) : that.userDiscountPercentage != null)
            return false;
        if (consultant != null ? !consultant.equals(that.consultant) : that.consultant != null) return false;
        if (isDraft != null ? !isDraft.equals(that.isDraft) : that.isDraft != null) return false;
        if (sponsorDiscount != null ? !sponsorDiscount.equals(that.sponsorDiscount) : that.sponsorDiscount != null)
            return false;
        if (patientDiscount != null ? !patientDiscount.equals(that.patientDiscount) : that.patientDiscount != null)
            return false;
        if (sendEmail != null ? !sendEmail.equals(that.sendEmail) : that.sendEmail != null) return false;
        return taxDiscount != null ? taxDiscount.equals(that.taxDiscount) : that.taxDiscount == null;

    }

    @Override
    public int hashCode() {
        int result = dispenseNumber != null ? dispenseNumber.hashCode() : 0;
        result = 31 * result + (hsc != null ? hsc.hashCode() : 0);
        result = 31 * result + (dispenseDate != null ? dispenseDate.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        result = 31 * result + (approvedDate != null ? approvedDate.hashCode() : 0);
        result = 31 * result + (dispenseUser != null ? dispenseUser.hashCode() : 0);
        result = 31 * result + (dispenseUnit != null ? dispenseUnit.hashCode() : 0);
        result = 31 * result + (partOf != null ? partOf.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (dispenseStatus != null ? dispenseStatus.hashCode() : 0);
        result = 31 * result + (encounter != null ? encounter.hashCode() : 0);
        result = 31 * result + (patient != null ? patient.hashCode() : 0);
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (dispensePlans != null ? dispensePlans.hashCode() : 0);
        result = 31 * result + (dispenseTaxes != null ? dispenseTaxes.hashCode() : 0);
        result = 31 * result + (dispenseDocumentLines != null ? dispenseDocumentLines.hashCode() : 0);
        result = 31 * result + (paymentDetails != null ? paymentDetails.hashCode() : 0);
        result = 31 * result + (grossAmount != null ? grossAmount.hashCode() : 0);
        result = 31 * result + (discountAmount != null ? discountAmount.hashCode() : 0);
        result = 31 * result + (netAmount != null ? netAmount.hashCode() : 0);
        result = 31 * result + (totalSponsorAmount != null ? totalSponsorAmount.hashCode() : 0);
        result = 31 * result + (patientPaidAmount != null ? patientPaidAmount.hashCode() : 0);
        result = 31 * result + (userDiscountAmount != null ? userDiscountAmount.hashCode() : 0);
        result = 31 * result + (userDiscountPercentage != null ? userDiscountPercentage.hashCode() : 0);
        result = 31 * result + (consultant != null ? consultant.hashCode() : 0);
        result = 31 * result + (isDraft != null ? isDraft.hashCode() : 0);
        result = 31 * result + (sponsorDiscount != null ? sponsorDiscount.hashCode() : 0);
        result = 31 * result + (patientDiscount != null ? patientDiscount.hashCode() : 0);
        result = 31 * result + (taxDiscount != null ? taxDiscount.hashCode() : 0);
        result = 31 * result + (sendEmail != null ? sendEmail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseDocument{" +
            "dispenseNumber='" + dispenseNumber + '\'' +
            ", hsc=" + hsc +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", dispenseDate=" + dispenseDate +
            ", dispenseUser=" + dispenseUser +
            ", approvedBy=" + approvedBy +
            ", approvedDate=" + approvedDate +
            ", dispenseUnit=" + dispenseUnit +
            ", partOf=" + partOf +
            ", source=" + source +
            ", dispenseStatus=" + dispenseStatus +
            ", encounter=" + encounter +
            ", patient=" + patient +
            ", orderSource=" + orderSource +
            ", dispensePlans=" + dispensePlans +
            ", dispenseTaxes=" + dispenseTaxes +
            ", dispenseDocumentLines=" + dispenseDocumentLines +
            ", paymentDetails=" + paymentDetails +
            ", grossAmount=" + grossAmount +
            ", netAmount=" + netAmount +
            ", patientNetAmount=" + patientNetAmount +
            ", patientSaleAmount=" + patientSaleAmount +
            ", patientGrossAmount=" + patientGrossAmount +
            ", totalSponsorAmount=" + totalSponsorAmount +
            ", patientDiscount=" + patientDiscount +
            ", sponsorDiscount=" + sponsorDiscount +
            ", roundOff=" + roundOff +
            ", discountAmount=" + discountAmount +
            ", patientPaidAmount=" + patientPaidAmount +
            ", totalDiscountPercentage=" + totalDiscountPercentage +
            ", userDiscountAmount=" + userDiscountAmount +
            ", userDiscountPercentage=" + userDiscountPercentage +
            ", unitDiscountPercentage=" + unitDiscountPercentage +
            ", unitDiscountAmount=" + unitDiscountAmount +
            ", consultant=" + consultant +
            ", isDraft=" + isDraft +
            ", taxDiscount=" + taxDiscount +
            ", documentType=" + documentType +
            ", isDiscountPercentage=" + isDiscountPercentage +
            ", planDiscountAmount=" + planDiscountAmount +
            ", sendEmail=" + sendEmail +
            ", stockSource=" + stockSource +
            '}';
    }

    /*public void calculateUserAndUnitDiscount(DispenseDocumentLine line, Float unitDiscountPercent) {
        BigDecimal userDiscount = BigDecimalUtil.ZERO;
        boolean isFullDiscount = (getUserDiscountPercentage() + unitDiscountPercent) == 100;
        BigDecimal unitDiscount = multiply(line.getPatientGrossAmount(), unitDiscountPercent * 0.01f);
        if (!isDiscountPercentage) {
            unitDiscount = calculateHeaderUnitDiscountInAmount(line);
        }
        unitDiscount = roundOff(unitDiscount, 2);
        BigDecimal amount = line.getPatientGrossAmount();
        if (gtZero(subtract(line.getPlanDiscAmount(), line.getSponsorDiscAmount()))) {
            amount = add(subtract(line.getPatientGrossAmount(), line.getPlanDiscAmount()), line.getSponsorDiscAmount());
        }

        if (line.isItemDiscount()) { //Line level user Discount either % or amount
            if (line.isPercentDiscount()) { //In case of %
                userDiscount = multiply(amount , line.getEnteredUserDiscount() * 0.01f);
                BigDecimal allowedUserDiscountAmount = roundOff(subtract(line.getPatientGrossAmount(), unitDiscount), 2);
                if (gt(userDiscount , allowedUserDiscountAmount)){
                    userDiscount = allowedUserDiscountAmount;
                }
            } else { //In case of Amount
                userDiscount = roundOff(line.getEnteredUserDiscount(), 2);
                BigDecimal allowedUserDiscountAmount = subtract(line.getPatientGrossAmount(), add(unitDiscount, line.getPlanDiscAmount())).add(line.getSponsorDiscAmount());
                if (gt(userDiscount, allowedUserDiscountAmount)) {
                    userDiscount = allowedUserDiscountAmount;
                }
            }
        } else {//Check Header level discount details
            if (isDiscountPercentage) {
                userDiscount = multiply(amount, getUserDiscountPercentage() * 0.01f);
            } else {
                userDiscount = roundOff(calculateHeaderUserDiscountInAmount(line), 2);
                BigDecimal allowedUserDiscountAmount = roundOff(subtract(line.getPatientGrossAmount(), add(unitDiscount, line.getPlanDiscAmount()).add(line.getSponsorDiscAmount())), 2);
                if (gt(userDiscount, allowedUserDiscountAmount)) {
                    userDiscount = allowedUserDiscountAmount;
                }
            }
        }

        userDiscount = ltZero(userDiscount) ? BigDecimalUtil.ZERO : roundOff(userDiscount, 2);

        if (eqZero(line.getSponsorDiscAmount())) {
            line.setUnitDiscount(unitDiscount);
        } else {
            line.setUnitDiscount(BigDecimalUtil.ZERO);
        }
        if (isFullDiscount) {
            line.setTaxDiscountAmount(line.getPatientTaxAmount());
        }
        line.setUserDiscount(userDiscount);
        line.setPatientTotalDiscAmount(sum(line.getPatientTotalDiscAmount(), line.getUserDiscount(), line.getUnitDiscount(), line.getTaxDiscountAmount()));
        line.setTotalDiscountAmount(roundOff(add(line.getSponsorDiscAmount(), line.getPatientTotalDiscAmount()), 2));// sponsor + patient plan + user + unit +tax discounts
    }*/

    /**
     * Calculate user discount By Weightage Formula
     *
     * @param line
     */
    public BigDecimal calculateHeaderUserDiscountInAmount(DispenseDocumentLine line) {

        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            return (eqZero(getPatientSaleAmount())) ? BigDecimalUtil.ZERO :
                multiply(getUserDiscountAmount(),
                    (divide(
                        (subtract(line.getPatientSaleAmount(), add(line.getUnitDiscount(), line.returnPatientPlanDiscount(), 2), 2)),
                        subtract(getPatientSaleAmount(), add(getUnitDiscountAmount(), subtract(getPlanDiscountAmount(), getSponsorDiscount(), 2), 2)), 6
                    )), 2);
        } else {
            return (eqZero(getPatientGrossAmount())) ? BigDecimalUtil.ZERO :
                multiply(getUserDiscountAmount(),
                    (divide(
                        (subtract(line.getPatientGrossAmount(), add(line.getUnitDiscount(), line.returnPatientPlanDiscount(), 2), 2)),
                        subtract(getPatientGrossAmount(), add(getUnitDiscountAmount(), subtract(getPlanDiscountAmount(), getSponsorDiscount(), 2), 2)), 6
                    )), 2);
        }
    }

    private BigDecimal calculateHeaderUnitDiscountInAmount(DispenseDocumentLine line) {
        return (eqZero(getPatientGrossAmount())) ? BigDecimalUtil.ZERO :
            multiply(getUnitDiscountAmount(), (divide(line.getPatientGrossAmount(), getPatientGrossAmount())));
    }

    public void setPlanDiscountAmount(BigDecimal planDiscountAmount) {
        this.planDiscountAmount = planDiscountAmount;
    }

    public BigDecimal getPlanDiscountAmount() {
        return planDiscountAmount;
    }

    public void updateGrossAmount() {
        setPatientGrossAmount(BigDecimalUtil.ZERO);
        setGrossAmount(BigDecimalUtil.ZERO);
        dispenseDocumentLines.forEach(line -> {
            setPatientGrossAmount(add(getPatientGrossAmount(), line.getPatientGrossAmount()));
            setGrossAmount(add(getGrossAmount(), line.getGrossAmount()));
        });
    }

    private void reset() {
        this.applyUnitDiscount = Boolean.TRUE;
        this.patientSaleAmount = BigDecimalUtil.ZERO;
        this.patientGrossAmount = BigDecimalUtil.ZERO;
        this.patientDiscount = BigDecimalUtil.ZERO;
        this.patientNetAmount = BigDecimalUtil.ZERO;
        this.planDiscountAmount = BigDecimalUtil.ZERO;
        this.sponsorDiscount = BigDecimalUtil.ZERO;
        this.userDiscountAmount = BigDecimalUtil.ZERO;
        this.unitDiscountAmount = BigDecimalUtil.ZERO;
        this.discountAmount = BigDecimalUtil.ZERO;
        this.taxDiscount = BigDecimalUtil.ZERO;
        this.grossAmount = BigDecimalUtil.ZERO;
        this.netAmount = BigDecimalUtil.ZERO;
        this.roundOff = BigDecimalUtil.ZERO;
        this.totalDiscountPercentage = 0f;
        this.totalSponsorAmount = BigDecimalUtil.ZERO;
        this.saleAmount = BigDecimalUtil.ZERO;
        this.sponsorNetAmount = BigDecimalUtil.ZERO;
        this.calculatedGrossAmount = BigDecimalUtil.ZERO;
        this.calculatedPatientGrossAmount = BigDecimalUtil.ZERO;
        this.calculatedSponsorGrossAmount = BigDecimalUtil.ZERO;
        this.fullDiscountFlag=BigDecimalUtil.ZERO;
        this.setDispenseTaxes(new ArrayList<>());
        this.getDispensePlans().forEach(dispensePlan -> dispensePlan.reset());
        this.authorizationBalanceAmount = new HashMap<>();
    }

    private void calculateRoundOff() {
        int scale = 2;
        this.patientSaleAmount = roundOff(this.patientSaleAmount, scale);
        this.patientGrossAmount = roundOff(this.patientGrossAmount, scale);
        this.patientDiscount = roundOff(this.patientDiscount, scale);
        this.patientNetAmount = roundOff(this.patientNetAmount, scale + 1);
        this.planDiscountAmount = roundOff(this.planDiscountAmount, scale);
        this.sponsorDiscount = roundOff(this.sponsorDiscount, scale);
        this.userDiscountAmount = roundOff(this.userDiscountAmount, scale);
        this.unitDiscountAmount = roundOff(this.unitDiscountAmount, scale);
        this.taxDiscount = roundOff(this.taxDiscount, scale);
        this.grossAmount = roundOff(this.grossAmount, scale);
        this.netAmount = roundOff(this.netAmount, scale);
        this.roundOff = roundOff(subtract(roundOff(this.patientNetAmount, 0), this.patientNetAmount), scale);
        this.patientNetAmount = roundOff(this.patientNetAmount, 0);
        this.calculatedGrossAmount = roundOff(this.calculatedGrossAmount, scale);
        this.calculatedPatientGrossAmount = roundOff(this.calculatedPatientGrossAmount, scale);
        this.calculatedSponsorGrossAmount = roundOff(this.calculatedSponsorGrossAmount, scale);

        for (DispensePlan dispensePlan : getDispensePlans()) {
            dispensePlan.setSponsorGrossAmount(roundOff(dispensePlan.getSponsorGrossAmount(), scale));
            dispensePlan.setSponsorDiscount(roundOff(dispensePlan.getSponsorDiscount(), scale));
            dispensePlan.setPatientDiscount(roundOff(dispensePlan.getPatientDiscount(), scale));
            dispensePlan.setTotalTax(roundOff(dispensePlan.getTotalTax(), scale));
            dispensePlan.setSponsorPayable(roundOff(dispensePlan.getSponsorPayable(), scale));
            dispensePlan.setRoundOff(BigDecimalUtil.ZERO.setScale(2, RoundingMode.HALF_UP));
            //dispensePlan.setRoundOff(roundOff(subtract(roundOff(dispensePlan.getSponsorPayable(), 0), dispensePlan.getSponsorPayable()), scale));
            //dispensePlan.setSponsorPayable(roundOff(dispensePlan.getSponsorPayable(), 0));
            dispensePlan.getPlanTaxList().forEach(dispenseTax -> dispenseTax.roundOff(3));
        }

        for (DispenseTax dispenseTax : getDispenseTaxes()) {
            dispenseTax.setTaxAmount(roundOff(dispenseTax.getTaxAmount(), 3));
            dispenseTax.setPatientTaxAmount(roundOff(dispenseTax.getPatientTaxAmount(), 3));
        }
    }

    public void updateSponsorNetAmount() {
        if (this.dispensePlans == null || this.dispensePlans.isEmpty()) {
            return;
        }
        this.dispensePlans.forEach(dispensePlan -> {
            this.sponsorNetAmount = add(this.sponsorNetAmount, dispensePlan.getSponsorPayable());
        });
    }

    public void summarize() {
        reset();
        //for (DispenseDocumentLine line : dispenseDocumentLines) {
        for (int index = dispenseDocumentLines.size()-1; index >=0; index--) {
            DispenseDocumentLine line = dispenseDocumentLines.get(index);
            line.summarize(taxCalculationType);
            //apply sponsor authorized amount
            line.applyAuthorizationAmount(getAuthorizationBalanceAmount(), taxCalculationType);
            this.summarizeTaxDetails(line);
            line.roundOffToRequiredScale();
            this.patientSaleAmount = add(this.patientSaleAmount, line.getPatientSaleAmount());
            this.patientGrossAmount = add(this.patientGrossAmount, line.getPatientGrossAmount());
            this.patientDiscount = add(this.patientDiscount, line.getPatientTotalDiscAmount());
            this.patientNetAmount = add(this.patientNetAmount, line.getPatientNetAmount());

            this.planDiscountAmount = add(this.planDiscountAmount, line.getPlanDiscAmount());
            this.sponsorDiscount = add(this.sponsorDiscount, line.getSponsorDiscAmount());

            this.userDiscountAmount = add(this.userDiscountAmount, (ltZero(line.getUserDiscount())) ? BigDecimalUtil.ZERO : line.getUserDiscount());
            //this.fullDiscountFlag=add(this.fullDiscountFlag, (ltZero(line.getUserDiscount())) ? BigDecimalUtil.ZERO : add(line.getUserDiscount(),line.getTaxDiscountAmount()));
            this.fullDiscountFlag=add(this.fullDiscountFlag, (ltZero(line.getTotalDiscountAmount())) ? BigDecimalUtil.ZERO :line.getTotalDiscountAmount());
            this.unitDiscountAmount = add(this.unitDiscountAmount, line.getUnitDiscount());
            this.taxDiscount = add(this.taxDiscount, line.getTaxDiscountAmount());

            this.grossAmount = add(this.grossAmount, line.getGrossAmount());
            this.netAmount = add(this.netAmount, line.getNetAmount());
            this.discountAmount = add(this.discountAmount, line.getTotalDiscountAmount());
            this.saleAmount = add(this.saleAmount, line.getSaleAmount());
            this.calculatedGrossAmount = add(this.calculatedGrossAmount, line.getCalculatedGrossAmount());
            this.calculatedPatientGrossAmount = add(this.calculatedPatientGrossAmount, line.getCalculatedPatientGrossAmount());
            this.calculatedSponsorGrossAmount = add(this.calculatedSponsorGrossAmount, line.getCalculatedSponsorGrossAmount());

            line.setItemLevelPlanDetails();
            this.summarizePlanDetails(line);
        }

        BigDecimal discountOnAmount = this.patientGrossAmount;
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discountOnAmount = this.patientSaleAmount;
        }
        if (hasPlanDiscount()) {
            discountOnAmount = subtract(discountOnAmount, subtract(this.planDiscountAmount, this.sponsorDiscount));
        }
        if (gtZero(discountOnAmount)) {
            this.setTotalDiscountPercentage(roundOff(divide(multiply(add(this.unitDiscountAmount, this.userDiscountAmount), 100f), discountOnAmount), 2).floatValue());
        } else if (userDiscountPercentage == 100f) {
            this.setTotalDiscountPercentage(100f);
        } else {
            this.setTotalDiscountPercentage(0f);
        }
        this.discountAmount = roundOff(this.discountAmount, 2);
        calculateRoundOff();
    }

    private void summarizePlanDetails(DispenseDocumentLine line) {
        if (null == line.getDispenseItemPlans() || line.getDispenseItemPlans().isEmpty()) {
            return;
        }
        for (DispenseItemPlan dispenseItemPlan : line.getDispenseItemPlans()) {
            for (DispensePlan dispensePlan : getDispensePlans()) {
                if (dispensePlan.getPlanRef().getCode().equals(dispenseItemPlan.getPlanRef().getCode())) {
                    dispensePlan.setTotalTax(add(dispensePlan.getTotalTax(), dispenseItemPlan.getSponsorTaxAmount()));
                    dispensePlan.setPatientDiscount(add(dispensePlan.getPatientDiscount() , dispenseItemPlan.getPatientDiscAmount()));
                    dispensePlan.setSponsorDiscount(add(dispensePlan.getSponsorDiscount() , dispenseItemPlan.getSponsorDiscAmount()));
                    dispensePlan.setSponsorPayable(add(dispensePlan.getSponsorPayable() , dispenseItemPlan.getSponsorNetAmount()));
                    dispensePlan.setSponsorGrossAmount(add(dispensePlan.getSponsorGrossAmount() , dispenseItemPlan.getSponsorGrossAmount()));

                    for (DispenseTax planItemTax : dispenseItemPlan.getPlanTaxList()) {
                        boolean isTaxUpdatedToPlanTax = false;
                        for (DispenseTax planTax : dispensePlan.getPlanTaxList()) {
                            if (planTax.getTaxCode().equals(planItemTax.getTaxCode())) {
                                isTaxUpdatedToPlanTax = true;
                                planTax.setPatientTaxAmount(add(planTax.getPatientTaxAmount(), planItemTax.getPatientTaxAmount()));
                                planTax.setTaxAmount(add(planTax.getTaxAmount(), planItemTax.getTaxAmount()));
                                break;
                            }
                        }
                        if (!isTaxUpdatedToPlanTax) {
                            DispenseTax planTax = new DispenseTax();
                            planTax.setTaxCode(planItemTax.getTaxCode());
                            planTax.setDefinition(planItemTax.getDefinition());
                            planTax.setTaxDefinition(planItemTax.getTaxDefinition());
                            planTax.setPatientTaxAmount(planItemTax.getPatientTaxAmount());
                            planTax.setTaxAmount(planItemTax.getTaxAmount());
                            dispensePlan.getPlanTaxList().add(planTax);
                        }
                        planItemTax.roundOff(2);
                    }
                    dispenseItemPlan.roundOff();
                }
            }
        }
    }

    private void summarizeTaxDetails(DispenseDocumentLine line) {
        int calculateScale = 2;
        for (DispenseTax dispenseTax : line.getDispenseTaxes()) {
            boolean foundTaxCode = false;
            for (DispenseTax headerTax : getDispenseTaxes()) {
                if (dispenseTax.getTaxCode().equals(headerTax.getTaxCode())) {
                    headerTax.setPatientTaxAmount(roundOff(add(headerTax.getPatientTaxAmount(), dispenseTax.getPatientTaxAmount()), calculateScale));
                    headerTax.setTaxAmount(roundOff(add(headerTax.getTaxAmount(), dispenseTax.getTaxAmount()), calculateScale));
                    foundTaxCode = true;
                    break;
                }
            }
            if (!foundTaxCode) {
                DispenseTax headerTax = new DispenseTax();
                headerTax.setTaxCode(dispenseTax.getTaxCode());
                headerTax.setDefinition(dispenseTax.getDefinition());
                headerTax.setTaxDefinition(dispenseTax.getTaxDefinition());
                headerTax.setPatientTaxAmount(dispenseTax.getPatientTaxAmount());
                headerTax.setTaxAmount(dispenseTax.getTaxAmount());
                getDispenseTaxes().add(headerTax);
            }
        }
    }

    public boolean hasPlanDiscount() {
        return dispenseDocumentLines.stream().anyMatch(line -> gtZero(line.getPlanDiscAmount()));
    }

    /*public void applyDiscount() {
        boolean applyUnitDiscount = !hasPlanDiscount();
        dispenseDocumentLines.forEach(line -> {
            line.resetDiscounts();
            line.applyUnitDiscountPercentage(getUnitDiscountPercentage(), applyUnitDiscount);
            if (line.getItemDiscount()) {
                line.applyLineSpecificDiscount(!applyUnitDiscount);
            } else if (getDiscountPercentage()) {
                line.applyUserPercentageDiscount(getUserDiscountPercentage(), applyUnitDiscount);
            } else {
                if (gtZero(getPatientGrossAmount())) {
                    line.applyAmountDiscount(divide(multiply(patientDiscount, line.getPatientGrossAmount()), getPatientGrossAmount()));
                }
            }
            line.summarizeDiscount();
        });
    }*/

    public void updateAppliedUserDiscount(Integer lineIndex) {
        if (null == lineIndex) {
            calculateAppliedUserDiscount();
        } else {
            DispenseDocumentLine line = getDispenseDocumentLines().get(lineIndex);
            line.setTaxDiscountAmount(BigDecimalUtil.ZERO);
            line.calculateAppliedUserDiscount(taxCalculationType);
        }
    }

    public void reCalculateTaxes() {
        getDispenseDocumentLines().forEach(line -> {
            line.calculateTax(taxCalculationType);
        });
    }

    /*public void applyDiscountForLine(DispenseDocumentLine line) {
        boolean applyUnitDiscount = !hasPlanDiscount();
        line.resetDiscounts();
        line.applyUnitDiscountPercentage(getUnitDiscountPercentage(), applyUnitDiscount);
        line.applyLineSpecificDiscount(!applyUnitDiscount);
        line.summarizeDiscount();
    }*/

   /* public void applyDiscountForLineModification(DispenseDocumentLine line) {
        line.resetDiscounts();
        if (line.getItemDiscount()) {
            applyDiscountForLine(line);
        } else {
            line.applyUnitDiscountPercentage(getUnitDiscountPercentage(), !hasPlanDiscount());
            line.summarizeDiscount();
        }
    }

    public void checkAndApplyForFullDiscount() {
        for (DispenseDocumentLine line : getDispenseDocumentLines()) {
            if (line.getFullDiscount()) {
                line.setTaxDiscountAmount(line.getPatientTaxAmount());
            }
        }
    }

    public void resetUnitDiscountIfPlanDiscountExists() {
        boolean applyUnitDiscount = !hasPlanDiscount();
        dispenseDocumentLines.forEach(line -> {
            line.resetUnitDiscountIfPlanDiscountExists(unitDiscountPercentage,
                applyUnitDiscount);
        });
    }

    public void calculateGross(DispenseDocumentLine line) {
        line.reset();
        line.calculateGross();
    }

    public void calculatePatientGross(DispenseDocumentLine line, Float patientPaymentPercentage) {
        line.calculatePatientGross(patientPaymentPercentage);
    }

    public void summarizeForLine(DispenseDocumentLine line) {
        line.summarize(taxCalculationType);
    }

    public void calculateTaxForLine(DispenseDocumentLine line, Float patientTaxPercentage) {
        line.calculateTax(patientTaxPercentage);
    } */

    public void isUnitDiscountApplicable() {
        applyUnitDiscount = (getDispensePlans() == null || getDispensePlans().isEmpty());
    }

    public void calculateSale() {
        dispenseDocumentLines.forEach(line -> {
            line.calculateSale();
            line.calculateTotalTax();
            line.calculateGross();
        });
    }

    public void calculateAppliedUserDiscount() {

        getDispenseDocumentLines().forEach(line -> {
            line.setTaxDiscountAmount(BigDecimalUtil.ZERO);
            if (line.getFullDiscount()) {
                line.setTaxDiscountAmount(line.getPatientTaxAmount());
                //   line.setFullDiscount(false);
            }
            BigDecimal discOnAmount = line.getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
            if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
                discOnAmount = line.getPatientSaleAmount();
            }

            BigDecimal appliedUserDiscount = multiply(subtract(discOnAmount, (add(line.returnPatientPlanDiscount(), line.getUnitDiscount()))), userDiscountPercentage * 0.01f);
            if (!isDiscountPercentage) {
                appliedUserDiscount = calculateHeaderUserDiscountInAmount(line);
            }
            appliedUserDiscount = lteZero(appliedUserDiscount) ? BigDecimalUtil.ZERO : roundOff(appliedUserDiscount, 2);
            line.setAppliedUserDiscount(appliedUserDiscount);
            line.setUserDiscount(appliedUserDiscount);
        });
    }

    public void validateAndApplyDiscretionaryDiscount(Integer lineIndex, String taxCalculationType) {
        if (null == lineIndex) {
            dispenseDocumentLines.forEach(line -> {
                line.validateAndApplyDiscretionaryDiscount(taxCalculationType);
            });

        } else {
            dispenseDocumentLines.get(lineIndex).validateAndApplyDiscretionaryDiscount(taxCalculationType);
        }
    }

    public void setUnitLevelDiscountPercentage(DispenseDocumentLine line) {

        if ("MARGIN_BASED_DISCOUNT".equals(discountType)) {
            Map<String, Object> argumentBindings = new HashMap<>();
            argumentBindings.put("saleRate", line.getSaleRate());
            argumentBindings.put("grossRate",line.getGrossRate());
            argumentBindings.put("avgPurchaseCost", line.getItemUnitAvgCost());
            float formulaResult = MarginBasedDiscountUtil.evaluateMarginBasedDiscountFormula(discountFormula, argumentBindings);
            unitDiscountPercentage = MarginBasedDiscountUtil.getDiscountPercentFromSlab(discountSlab, formulaResult);
        }
    }

    public void calculateUnitLevelDiscount(DispenseDocumentLine line, boolean isUnitDiscountApplicable) {
        if (!applyUnitDiscount || !isUnitDiscountApplicable) {
            return;
        }
        BigDecimal discOnAmount = line.getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = line.getPatientSaleAmount();
        }
        BigDecimal unitDiscount = CalculateTaxUtil.calculatePercentAmount(discOnAmount, unitDiscountPercentage);
        line.setUnitDiscount(unitDiscount);
    }

    /*public void calculateDiscretionaryDiscount(DispenseDocumentLine line) {
        BigDecimal discOnAmount = line.getPatientGrossAmount(); //DEFAULT - TAX_BEFORE_DISCOUNT
        if ("TAX_AFTER_DISCOUNT".equals(taxCalculationType)) {
            discOnAmount = line.getPatientSaleAmount();
        }
        discOnAmount = subtract(discOnAmount,  subtract(add(line.getUnitDiscount(), line.getPlanDiscAmount()), line.getSponsorDiscAmount()));
        BigDecimal userDiscount = CalculateTaxUtil.calculatePercentAmount(discOnAmount, userDiscountPercentage);
        line.setUserDiscount(userDiscount);
    }*/
}
