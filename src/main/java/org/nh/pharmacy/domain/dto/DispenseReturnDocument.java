package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.Source;
import org.nh.common.dto.*;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.IPReturnType;
import org.nh.pharmacy.domain.enumeration.ReturnStatus;
import org.nh.pharmacy.domain.enumeration.TransactionType;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Transient;
import java.io.Serializable;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.nh.common.util.BigDecimalUtil.*;
/**
 * Created by prashanth on 7/5/17
 */
public class DispenseReturnDocument extends DispenseDocument implements Serializable {


    public DispenseReturnDocument() {
        setDocumentType(TransactionType.Dispense_Return);
    }

//    @Field(type = FieldType.Object)
//    private HealthcareServiceCenterDTO hsc;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO returnhsc;

//    private LocalDateTime dispenseDate;
//    private UserDTO approvedBy;
//    private LocalDateTime approvedDate;
//    private UserDTO dispenseUser;
//    private OrganizationDTO dispenseUnit;
//    private DispenseStatus dispenseStatus;
//    private EncounterDTO encounter;

/*    @Field(type = FieldType.Object)
    private PatientDTO patient;*/

//    private Map<String, Object> orderSource;
//    private List<DispensePlan> dispensePlans;
//    private List<DispenseTax> dispenseTaxes;
//    private List<PaymentDetail> paymentDetails;
//    private BigDecimal grossAmount = BigDecimalUtil.ZERO;
//    private BigDecimal discountAmount = BigDecimalUtil.ZERO;
//    private BigDecimal netAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientAmount = BigDecimalUtil.ZERO;
//    private BigDecimal totalSponsorAmount = BigDecimalUtil.ZERO;
//    private BigDecimal patientPaidAmount = BigDecimalUtil.ZERO;
//    private BigDecimal userDiscountAmount = BigDecimalUtil.ZERO;
//    private Float userDiscountPercentage = 0f;
//    private Float unitDiscountPercentage = 0f;
//    private BigDecimal unitDiscountAmount = BigDecimalUtil.ZERO;
//    private ConsultantDTO consultant;
//    private Boolean isDraft = true;
//    private BigDecimal sponsorDiscount = BigDecimalUtil.ZERO;
//    private BigDecimal patientDiscount = BigDecimalUtil.ZERO;
//    private BigDecimal taxDiscount = BigDecimalUtil.ZERO;
//    private TransactionType documentType = TransactionType.Dispense_Return;
//    private Boolean isDiscountPercentage = Boolean.TRUE;
    private LocalDateTime returnDate;
    private OrganizationDTO returnUnit;

    @Field(type = FieldType.Object)
    private Source dispenseRef;

    @Field(type = FieldType.Object)
    private SourceDTO invoiceRef;

    @Field(type = FieldType.Object)
    private List<DispenseReturnDocumentLine> dispenseReturnDocumentLines;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private ReturnStatus returnStatus;

    private UserDTO receivedBy;
    private List<DispensePlan> plans;
    private List<DispenseTax> taxes;
    private Boolean refundRequired = Boolean.TRUE;
    private ValueSetCodeDTO returnReason;

    @Field(type = FieldType.Object)
    private SourceDTO cancelledInvoiceRef;

//    private BigDecimal calculatedGrossAmount = BigDecimalUtil.ZERO;
//    private BigDecimal calculatedPatientGrossAmount = BigDecimalUtil.ZERO;
//    private BigDecimal calculatedSponsorGrossAmount = BigDecimalUtil.ZERO;
//    private BigDecimal patientNetAmount = BigDecimalUtil.ZERO; //patient net payable
//    private BigDecimal patientSaleAmount = BigDecimalUtil.ZERO;
//    private BigDecimal patientGrossAmount = BigDecimalUtil.ZERO; //patient Gross
//    private BigDecimal sponsorNetAmount = BigDecimalUtil.ZERO;
//    private BigDecimal roundOff = BigDecimalUtil.ZERO;
//    @Transient
//    private String taxCalculationType;
//    @Transient
//    private String discountType;
//    @Transient
//    private String discountFormula;
//    @Transient
//    private String discountSlab;
//    private BigDecimal planDiscountAmount;
//    private BigDecimal saleAmount = BigDecimalUtil.ZERO;
    private boolean ipDispense=false;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String returnRequestNumber;
    private String bedNumber;

    @Field(type = FieldType.Object)
    private HealthcareServiceCenterDTO patientLocation;

    @Field(type = FieldType.Object)
    private UserDTO requestedBy;
    private LocalDateTime requestedDate;
//    private String remarks;

    private IPReturnType ipReturnType=IPReturnType.RETURN_REQUEST;
/*
    @Override
    public HealthcareServiceCenterDTO getHsc() {
        return hsc;
    }

    @Override
    public void setHsc(HealthcareServiceCenterDTO hsc) {
        this.hsc = hsc;
    }
*/
    public HealthcareServiceCenterDTO getReturnhsc() {
        return returnhsc;
    }

    public void setReturnhsc(HealthcareServiceCenterDTO returnhsc) {
        this.returnhsc = returnhsc;
    }

/*
    @Override
    public LocalDateTime getDispenseDate() {
        return dispenseDate;
    }

    @Override
    public void setDispenseDate(LocalDateTime dispenseDate) {
        this.dispenseDate = dispenseDate;
    }
*/
/*

    @Override
    public UserDTO getApprovedBy() {
        return approvedBy;
    }

    @Override
    public void setApprovedBy(UserDTO approvedBy) {
        this.approvedBy = approvedBy;
    }

    @Override
    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    @Override
    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    @Override
    public UserDTO getDispenseUser() {
        return dispenseUser;
    }

    @Override
    public void setDispenseUser(UserDTO dispenseUser) {
        this.dispenseUser = dispenseUser;
    }

    @Override
    public OrganizationDTO getDispenseUnit() {
        return dispenseUnit;
    }

    @Override
    public void setDispenseUnit(OrganizationDTO dispenseUnit) {
        this.dispenseUnit = dispenseUnit;
    }

    @Override
    public DispenseStatus getDispenseStatus() {
        return dispenseStatus;
    }

    @Override
    public void setDispenseStatus(DispenseStatus dispenseStatus) {
        this.dispenseStatus = dispenseStatus;
    }

    @Override
    public EncounterDTO getEncounter() {
        return encounter;
    }

    @Override
    public void setEncounter(EncounterDTO encounter) {
        this.encounter = encounter;
    }

    @Override
    public PatientDTO getPatient() {
        return patient;
    }

    @Override
    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }
*/
/*

    @Override
    public Map<String, Object> getOrderSource() {
        return orderSource;
    }

    @Override
    public void setOrderSource(Map<String, Object> orderSource) {
        this.orderSource = orderSource;
    }

    @Override
    public List<DispensePlan> getDispensePlans() {
        return dispensePlans;
    }

    @Override
    public void setDispensePlans(List<DispensePlan> dispensePlans) {
        this.dispensePlans = dispensePlans;
    }

    @Override
    public List<DispenseTax> getDispenseTaxes() {
        return dispenseTaxes;
    }

    @Override
    public void setDispenseTaxes(List<DispenseTax> dispenseTaxes) {
        this.dispenseTaxes = dispenseTaxes;
    }

    @Override
    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    @Override
    public void setPaymentDetails(List<PaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    @Override
    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    @Override
    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    @Override
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal getNetAmount() {
        return netAmount;
    }

    @Override
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
*/

    public BigDecimal getPatientAmount() {
        return patientAmount;
    }

    public void setPatientAmount(BigDecimal patientAmount) {
        this.patientAmount = patientAmount;
    }
/*

    @Override
    public BigDecimal getTotalSponsorAmount() {
        return totalSponsorAmount;
    }

    @Override
    public void setTotalSponsorAmount(BigDecimal totalSponsorAmount) {
        this.totalSponsorAmount = totalSponsorAmount;
    }

    @Override
    public BigDecimal getPatientPaidAmount() {
        return patientPaidAmount;
    }

    @Override
    public void setPatientPaidAmount(BigDecimal patientPaidAmount) {
        this.patientPaidAmount = patientPaidAmount;
    }

    @Override
    public BigDecimal getUserDiscountAmount() {
        return userDiscountAmount;
    }

    @Override
    public void setUserDiscountAmount(BigDecimal userDiscountAmount) {
        this.userDiscountAmount = userDiscountAmount;
    }

    @Override
    public Float getUserDiscountPercentage() {
        return userDiscountPercentage;
    }

    @Override
    public void setUserDiscountPercentage(Float userDiscountPercentage) {
        this.userDiscountPercentage = userDiscountPercentage;
    }

    @Override
    public Float getUnitDiscountPercentage() {
        return unitDiscountPercentage;
    }

    @Override
    public void setUnitDiscountPercentage(Float unitDiscountPercentage) {
        this.unitDiscountPercentage = unitDiscountPercentage;
    }

    @Override
    public BigDecimal getUnitDiscountAmount() {
        return unitDiscountAmount;
    }

    @Override
    public void setUnitDiscountAmount(BigDecimal unitDiscountAmount) {
        this.unitDiscountAmount = unitDiscountAmount;
    }

    @Override
    public ConsultantDTO getConsultant() {
        return consultant;
    }

    @Override
    public void setConsultant(ConsultantDTO consultant) {
        this.consultant = consultant;
    }

    @Override
    public Boolean getDraft() {
        return isDraft;
    }

    @Override
    public void setDraft(Boolean draft) {
        isDraft = draft;
    }

    @Override
    public BigDecimal getSponsorDiscount() {
        return sponsorDiscount;
    }

    @Override
    public void setSponsorDiscount(BigDecimal sponsorDiscount) {
        this.sponsorDiscount = sponsorDiscount;
    }

    @Override
    public BigDecimal getPatientDiscount() {
        return patientDiscount;
    }

    @Override
    public void setPatientDiscount(BigDecimal patientDiscount) {
        this.patientDiscount = patientDiscount;
    }

    @Override
    public BigDecimal getTaxDiscount() {
        return taxDiscount;
    }

    @Override
    public void setTaxDiscount(BigDecimal taxDiscount) {
        this.taxDiscount = taxDiscount;
    }

    @Override
    public TransactionType getDocumentType() {
        return documentType;
    }

    @Override
    public void setDocumentType(TransactionType documentType) {
        this.documentType = documentType;
    }

    @Override
    public Boolean getDiscountPercentage() {
        return isDiscountPercentage;
    }

    @Override
    public void setDiscountPercentage(Boolean discountPercentage) {
        isDiscountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal getSponsorNetAmount() {
        return sponsorNetAmount;
    }

    @Override
    public void setSponsorNetAmount(BigDecimal sponsorNetAmount) {
        this.sponsorNetAmount = sponsorNetAmount;
    }
*/

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public OrganizationDTO getReturnUnit() {
        return returnUnit;
    }

    public void setReturnUnit(OrganizationDTO returnUnit) {
        this.returnUnit = returnUnit;
    }

    public Source getDispenseRef() {
        return dispenseRef;
    }

    public void setDispenseRef(Source dispenseRef) {
        this.dispenseRef = dispenseRef;
    }

    public SourceDTO getInvoiceRef() {
        return invoiceRef;
    }

    public void setInvoiceRef(SourceDTO invoiceRef) {
        this.invoiceRef = invoiceRef;
    }

    public List<DispenseReturnDocumentLine> getDispenseReturnDocumentLines() {
        return dispenseReturnDocumentLines;
    }

    public void setDispenseReturnDocumentLines(List<DispenseReturnDocumentLine> dispenseReturnDocumentLines) {
        this.dispenseReturnDocumentLines = dispenseReturnDocumentLines;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(ReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public UserDTO getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(UserDTO receivedBy) {
        this.receivedBy = receivedBy;
    }

    public List<DispensePlan> getPlans() {
        return plans;
    }

    public void setPlans(List<DispensePlan> plans) {
        this.plans = plans;
    }

    public List<DispenseTax> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<DispenseTax> taxes) {
        this.taxes = taxes;
    }

    public Boolean getRefundRequired() {
        return refundRequired;
    }

    public void setRefundRequired(Boolean refundRequired) {
        this.refundRequired = refundRequired;
    }

    public ValueSetCodeDTO getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(ValueSetCodeDTO returnReason) {
        this.returnReason = returnReason;
    }

    public SourceDTO getCancelledInvoiceRef() {
        return cancelledInvoiceRef;
    }

    public void setCancelledInvoiceRef(SourceDTO cancelledInvoiceRef) {
        this.cancelledInvoiceRef = cancelledInvoiceRef;
    }
/*

    @Override
    public BigDecimal getCalculatedGrossAmount() {
        return calculatedGrossAmount;
    }

    @Override
    public void setCalculatedGrossAmount(BigDecimal calculatedGrossAmount) {
        this.calculatedGrossAmount = calculatedGrossAmount;
    }

    @Override
    public BigDecimal getCalculatedPatientGrossAmount() {
        return calculatedPatientGrossAmount;
    }

    @Override
    public void setCalculatedPatientGrossAmount(BigDecimal calculatedPatientGrossAmount) {
        this.calculatedPatientGrossAmount = calculatedPatientGrossAmount;
    }

    @Override
    public BigDecimal getCalculatedSponsorGrossAmount() {
        return calculatedSponsorGrossAmount;
    }

    @Override
    public void setCalculatedSponsorGrossAmount(BigDecimal calculatedSponsorGrossAmount) {
        this.calculatedSponsorGrossAmount = calculatedSponsorGrossAmount;
    }

    @Override
    public BigDecimal getPatientNetAmount() {
        return patientNetAmount;
    }

    @Override
    public void setPatientNetAmount(BigDecimal patientNetAmount) {
        this.patientNetAmount = patientNetAmount;
    }

    @Override
    public BigDecimal getPatientSaleAmount() {
        return patientSaleAmount;
    }

    @Override
    public void setPatientSaleAmount(BigDecimal patientSaleAmount) {
        this.patientSaleAmount = patientSaleAmount;
    }

    @Override
    public BigDecimal getPatientGrossAmount() {
        return patientGrossAmount;
    }

    @Override
    public void setPatientGrossAmount(BigDecimal patientGrossAmount) {
        this.patientGrossAmount = patientGrossAmount;
    }

    @Override
    public BigDecimal getRoundOff() {
        return roundOff;
    }

    @Override
    public void setRoundOff(BigDecimal roundOff) {
        this.roundOff = roundOff;
    }
*/

 /*   @Override
    public String getTaxCalculationType() {
        return taxCalculationType;
    }

    @Override
    public void setTaxCalculationType(String taxCalculationType) {
        this.taxCalculationType = taxCalculationType;
    }

    @Override
    public String getDiscountType() {
        return discountType;
    }

    @Override
    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    @Override
    public String getDiscountFormula() {
        return discountFormula;
    }

    @Override
    public void setDiscountFormula(String discountFormula) {
        this.discountFormula = discountFormula;
    }

    @Override
    public String getDiscountSlab() {
        return discountSlab;
    }

    @Override
    public void setDiscountSlab(String discountSlab) {
        this.discountSlab = discountSlab;
    }
*/
/*
    @Override
    public BigDecimal getPlanDiscountAmount() {
        return planDiscountAmount;
    }

    @Override
    public void setPlanDiscountAmount(BigDecimal planDiscountAmount) {
        this.planDiscountAmount = planDiscountAmount;
    }

    @Override
    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    @Override
    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }
*/

    public boolean isIpDispense() {
        return ipDispense;
    }

    public void setIpDispense(boolean ipDispense) {
        this.ipDispense = ipDispense;
    }

    public String getReturnRequestNumber() {
        return returnRequestNumber;
    }

    public void setReturnRequestNumber(String returnRequestNumber) {
        this.returnRequestNumber = returnRequestNumber;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public HealthcareServiceCenterDTO getPatientLocation() {
        return patientLocation;
    }

    public void setPatientLocation(HealthcareServiceCenterDTO patientLocation) {
        this.patientLocation = patientLocation;
    }

    public UserDTO getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UserDTO requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

/*    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }*/

    public IPReturnType getIpReturnType() {
        return ipReturnType;
    }

    public void setIpReturnType(IPReturnType ipReturnType) {
        this.ipReturnType = ipReturnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DispenseReturnDocument that = (DispenseReturnDocument) o;
        if (returnhsc != null ? !returnhsc.equals(that.returnhsc) : that.returnhsc != null) return false;
        if (patientAmount != null ? !patientAmount.equals(that.patientAmount) : that.patientAmount != null)
            return false;
        if (returnDate != null ? !returnDate.equals(that.returnDate) : that.returnDate != null) return false;
        if (returnUnit != null ? !returnUnit.equals(that.returnUnit) : that.returnUnit != null) return false;
        if (dispenseRef != null ? !dispenseRef.equals(that.dispenseRef) : that.dispenseRef != null) return false;
        if (invoiceRef != null ? !invoiceRef.equals(that.invoiceRef) : that.invoiceRef != null) return false;
        if (dispenseReturnDocumentLines != null ? !dispenseReturnDocumentLines.equals(that.dispenseReturnDocumentLines) : that.dispenseReturnDocumentLines != null)
            return false;
        if (returnStatus != that.returnStatus) return false;
        if (receivedBy != null ? !receivedBy.equals(that.receivedBy) : that.receivedBy != null) return false;
        if (plans != null ? !plans.equals(that.plans) : that.plans != null) return false;
        if (taxes != null ? !taxes.equals(that.taxes) : that.taxes != null) return false;
        if (refundRequired != null ? !refundRequired.equals(that.refundRequired) : that.refundRequired != null)
            return false;
        if (returnReason != null ? !returnReason.equals(that.returnReason) : that.returnReason != null) return false;
        return cancelledInvoiceRef != null ? cancelledInvoiceRef.equals(that.cancelledInvoiceRef) : that.cancelledInvoiceRef == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (super.getHsc() != null ? super.getHsc().hashCode() : 0);
        result = 31 * result + (returnhsc != null ? returnhsc.hashCode() : 0);
        result = 31 * result + (getDispenseDate() != null ? getDispenseDate().hashCode() : 0);
        result = 31 * result + (patientAmount != null ? patientAmount.hashCode() : 0);
        result = 31 * result + (returnDate != null ? returnDate.hashCode() : 0);
        result = 31 * result + (returnUnit != null ? returnUnit.hashCode() : 0);
        result = 31 * result + (dispenseRef != null ? dispenseRef.hashCode() : 0);
        result = 31 * result + (invoiceRef != null ? invoiceRef.hashCode() : 0);
        result = 31 * result + (dispenseReturnDocumentLines != null ? dispenseReturnDocumentLines.hashCode() : 0);
        result = 31 * result + (returnStatus != null ? returnStatus.hashCode() : 0);
        result = 31 * result + (receivedBy != null ? receivedBy.hashCode() : 0);
        result = 31 * result + (plans != null ? plans.hashCode() : 0);
        result = 31 * result + (taxes != null ? taxes.hashCode() : 0);
        result = 31 * result + (refundRequired != null ? refundRequired.hashCode() : 0);
        result = 31 * result + (returnReason != null ? returnReason.hashCode() : 0);
        result = 31 * result + (cancelledInvoiceRef != null ? cancelledInvoiceRef.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseReturnDocument{ " +
            super.toString() +
            " , returnhsc=" + returnhsc +
            ", patientAmount=" + patientAmount +
            ", returnDate=" + returnDate +
            ", returnUnit=" + returnUnit +
            ", dispenseRef=" + dispenseRef +
            ", invoiceRef=" + invoiceRef +
            ", dispenseReturnDocumentLines=" + dispenseReturnDocumentLines +
            ", returnStatus=" + returnStatus +
            ", receivedBy=" + receivedBy +
            ", plans=" + plans +
            ", taxes=" + taxes +
            ", refundRequired=" + refundRequired +
            ", returnReason=" + returnReason +
            ", cancelledInvoiceRef=" + cancelledInvoiceRef +
            ", ipDispense=" + ipDispense +
            '}';
    }

    public void calculateUserAndUnitDiscount(DispenseReturnDocumentLine line, Float unitDiscountPercent) {
        BigDecimal userDiscount = BigDecimalUtil.ZERO;
        BigDecimal taxDiscount = BigDecimalUtil.ZERO;
        boolean isFullDiscount = (getUserDiscountPercentage() + unitDiscountPercent) == 100;
        BigDecimal unitDiscount = getBigDecimal(unitDiscountPercent);
        //setLineLevelUserDiscount(line);
        line.getDispenseSource().getUnitDiscountAmount();
        if (!isDiscountPercentage()) {
            unitDiscount = line.getDispenseSource().getUnitDiscountAmount();
        }
        unitDiscount = unitDiscount;// roundOff(unitDiscount,2);
        BigDecimal amount = line.getPatientGrossAmount();
        if (gtZero(subtract(line.getPlanDiscAmount(), line.getSponsorDiscAmount()))) {
            amount = subtract(line.getPatientGrossAmount(), line.getPlanDiscAmount()).add(line.getSponsorDiscAmount());
        }

        if (line.isItemDiscount()) { //Line level user Discount either % or amount
            if (line.isPercentDiscount()) { //In case of %
                userDiscount = line.getDispenseSource().getUserDiscountAmount();
                BigDecimal allowedUserDiscountAmount = subtract(line.getPatientGrossAmount(), unitDiscount);//roundOff(line.getPatientGrossAmount() - unitDiscount,2);
                if (gt(userDiscount, allowedUserDiscountAmount)) {
                    userDiscount = allowedUserDiscountAmount;
                }
            } else { //In case of Amount
                userDiscount = line.getDispenseSource().getUserDiscountAmount();//roundOff(line.getDispenseSource().getUserDiscountAmount(),2);
                BigDecimal allowedUserDiscountAmount = subtract(add(line.getPatientGrossAmount(),line.getSponsorDiscAmount()), add(unitDiscount, line.getPlanDiscAmount()));
                if (gt(userDiscount, allowedUserDiscountAmount)) {
                    userDiscount = allowedUserDiscountAmount;
                }
            }
        } else {//Check Header level discount details
            if (isDiscountPercentage()) {
                userDiscount = line.getDispenseSource().getUserDiscountAmount();
            } else {
                userDiscount = line.getDispenseSource().getUserDiscountAmount();//roundOff(line.getDispenseSource().getUserDiscountAmount(),2);
                BigDecimal allowedUserDiscountAmount = subtract(add(line.getPatientGrossAmount(), line.getSponsorDiscAmount()), add(unitDiscount, line.getPlanDiscAmount()));//roundOff(line.getPatientGrossAmount() - unitDiscount - line.getPlanDiscAmount() + line.getSponsorDiscAmount(),2);
                if (gt(userDiscount, allowedUserDiscountAmount)) {
                    userDiscount = allowedUserDiscountAmount;
                }
            }
        }

        userDiscount = lteZero(userDiscount) ? BigDecimalUtil.ZERO : userDiscount;// roundOff(userDiscount, 2);

        if (eqZero(line.getSponsorDiscAmount())) {
            line.setUnitDiscount(unitDiscount);
        } else {
            line.setUnitDiscount(BigDecimalUtil.ZERO);
        }
        line.setTaxDiscountAmount(line.getDispenseSource().getTaxDiscountAmount());
        line.setNetAmount(line.getNetAmount());
        line.setUserDiscount(userDiscount);
        line.setPatientTotalDiscAmount(sum(line.getPatientTotalDiscAmount(), line.getUserDiscount(), line.getUnitDiscount(), line.getTaxDiscountAmount()));
        line.setTotalDiscountAmount(add(line.getSponsorDiscAmount(), line.getPatientTotalDiscAmount()));//roundOff(line.getSponsorDiscAmount() + line.getPatientTotalDiscAmount(), 2));// sponsor + patient plan + user + unit +tax discounts
    }


    public void updateGrossAmount() {
        dispenseReturnDocumentLines.forEach(line -> {
            setPatientGrossAmount(add(getPatientGrossAmount(), line.getPatientGrossAmount()));
            setGrossAmount(add(getGrossAmount(), line.getGrossAmount()));
        });
    }

    private void reset() {
        //  this.applyUnitDiscount = Boolean.TRUE;
        this.setPatientSaleAmount(BigDecimalUtil.ZERO);
        this.setPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setPatientDiscount(BigDecimalUtil.ZERO);
        this.setPatientNetAmount(BigDecimalUtil.ZERO);
        this.setPlanDiscountAmount(BigDecimalUtil.ZERO);
        this.setSponsorDiscount(BigDecimalUtil.ZERO);
        this.setUserDiscountAmount(BigDecimalUtil.ZERO);
        this.setUnitDiscountAmount(BigDecimalUtil.ZERO);
        this.setDiscountAmount(BigDecimalUtil.ZERO);
        this.setTaxDiscount(BigDecimalUtil.ZERO);
        this.setGrossAmount(BigDecimalUtil.ZERO);
        this.setNetAmount(BigDecimalUtil.ZERO);
        this.setRoundOff(BigDecimalUtil.ZERO);
        //  this.totalDiscountPercentage = BigDecimalUtil.ZERO;
        this.setTotalSponsorAmount(BigDecimalUtil.ZERO);
        this.setSaleAmount(BigDecimalUtil.ZERO);
        this.setSponsorNetAmount(BigDecimalUtil.ZERO);
        this.setCalculatedGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedPatientGrossAmount(BigDecimalUtil.ZERO);
        this.setCalculatedSponsorGrossAmount(BigDecimalUtil.ZERO);
        this.setDispenseTaxes(new ArrayList<>());
        this.getDispensePlans().forEach(dispensePlan -> dispensePlan.reset());
    }

    private void calculateRoundOff() {
        int scale = 2;
        this.setPatientSaleAmount(roundOff(this.getPatientSaleAmount(), scale));
        this.setPatientGrossAmount(roundOff(this.getPatientGrossAmount(), scale));
        this.setPatientDiscount(roundOff(this.getPatientDiscount(), scale));
        this.setPatientNetAmount(roundOff(this.getPatientNetAmount(), scale + 1));
        this.setPlanDiscountAmount(roundOff(this.getPlanDiscountAmount(), scale));
        this.setSponsorDiscount(roundOff(this.getSponsorDiscount(), scale));
        this.setUserDiscountAmount(roundOff(this.getUserDiscountAmount(), scale));
        this.setUnitDiscountAmount(roundOff(this.getUnitDiscountAmount(), scale));
        this.setTaxDiscount(roundOff(this.getTaxDiscount(), scale));
        this.setGrossAmount(roundOff(this.getGrossAmount(), scale));
        this.setNetAmount(roundOff(this.getNetAmount(), scale));
        this.setRoundOff(roundOff(subtract(roundOff(this.getPatientNetAmount(), 0), this.getPatientNetAmount()), scale));
        this.setPatientNetAmount(roundOff(this.getPatientNetAmount(), 0));
        this.setCalculatedGrossAmount(roundOff(this.getCalculatedGrossAmount(), scale));
        this.setCalculatedPatientGrossAmount(roundOff(this.getCalculatedPatientGrossAmount(), scale));
        this.setCalculatedSponsorGrossAmount(roundOff(this.getCalculatedSponsorGrossAmount(), scale));
        for (DispensePlan dispensePlan : getDispensePlans()) {
            dispensePlan.setSponsorGrossAmount(roundOff(dispensePlan.getSponsorGrossAmount(), scale));
            dispensePlan.setSponsorDiscount(roundOff(dispensePlan.getSponsorDiscount(), scale));
            dispensePlan.setPatientDiscount(roundOff(dispensePlan.getPatientDiscount(), scale));
            dispensePlan.setTotalTax(roundOff(dispensePlan.getTotalTax(), scale));
            dispensePlan.setSponsorPayable(roundOff(dispensePlan.getSponsorPayable(), scale));
            dispensePlan.setRoundOff(BigDecimalUtil.ZERO.setScale(2, RoundingMode.HALF_UP));
            //dispensePlan.setRoundOff(roundOff(subtract(roundOff(dispensePlan.getSponsorPayable(), 0), dispensePlan.getSponsorPayable()), scale));
            //dispensePlan.setSponsorPayable(roundOff(dispensePlan.getSponsorPayable(), 0));
            dispensePlan.getPlanTaxList().forEach(dispenseTax -> dispenseTax.roundOff(scale));
        }

        for (DispenseTax dispenseTax : getDispenseTaxes()) {
            dispenseTax.setTaxAmount(roundOff(dispenseTax.getTaxAmount(), scale));
            dispenseTax.setPatientTaxAmount(roundOff(dispenseTax.getPatientTaxAmount(), scale));
        }
    }

    public void summarize() {
        reset();
        for (DispenseReturnDocumentLine line : dispenseReturnDocumentLines) {
            this.summarizeTaxDetails(line);
            line.roundOffToRequiredScale();
            line.adjustGrossAmounts();
            this.setPatientSaleAmount(add(this.getPatientSaleAmount(), line.getPatientSaleAmount()));
            this.setPatientGrossAmount(add(this.getPatientGrossAmount(),  line.getPatientGrossAmount()));
            this.setPatientDiscount(add(this.getPatientDiscount(),  line.getPatientTotalDiscAmount()));
            this.setPatientNetAmount(add(this.getPatientNetAmount(),  line.getPatientNetAmount()));
            this.setSponsorNetAmount(add(this.getSponsorNetAmount(),  line.getSponsorNetAmount()));
            this.setPlanDiscountAmount(add(this.getPlanDiscountAmount(),  line.getPlanDiscAmount()));
            this.setSponsorDiscount(add(this.getSponsorDiscount(),  line.getSponsorDiscAmount()));

            this.setUserDiscountAmount(add(this.getUserDiscountAmount(),  (ltZero(line.getUserDiscount()) ? BigDecimalUtil.ZERO : line.getUserDiscount())));
            this.setUnitDiscountAmount(add(this.getUnitDiscountAmount(),  line.getUnitDiscount()));
            this.setTaxDiscount(add(this.getTaxDiscount(),  line.getTaxDiscountAmount()));

            this.setGrossAmount(add(this.getGrossAmount(),  line.getGrossAmount()));
            this.setNetAmount(add(this.getNetAmount(),  line.getNetAmount()));
            this.setDiscountAmount(add(this.getDiscountAmount(),  line.getTotalDiscountAmount()));
            this.setSaleAmount(add(this.getSaleAmount(),  line.getSaleAmount()));
            this.setCalculatedGrossAmount(add(this.getCalculatedGrossAmount(),  line.getGrossAmount()));
            this.setCalculatedPatientGrossAmount(add(this.getCalculatedPatientGrossAmount(),  line.getPatientGrossAmount()));
            this.setCalculatedSponsorGrossAmount(add(this.getCalculatedSponsorGrossAmount(),  line.getSponsorGrossAmount()));
            line.setItemLevelPlanDetails();
            this.summarizePlanDetails(line);

        }

        BigDecimal discountOnAmount = this.getPatientGrossAmount();
        if (hasPlanDiscount()) {
            discountOnAmount = subtract(discountOnAmount, subtract(this.getPlanDiscountAmount(), this.getSponsorDiscount()));
        }
        if (gtZero(discountOnAmount)) {
            this.setTotalDiscountPercentage(roundOff(
                divide(
                multiply(add(this.getUnitDiscountAmount(), this.getUserDiscountAmount()), 100f),
                discountOnAmount),
                2).floatValue());
        } else {
            this.setTotalDiscountPercentage(0f);
        }
        this.setDiscountAmount(roundOff(this.getDiscountAmount(), 2));
        calculateRoundOff();


    }

    public boolean hasPlanDiscount() {
        return dispenseReturnDocumentLines.stream().anyMatch(line -> gtZero(line.getPlanDiscAmount()));
    }

    private void summarizePlanDetails(DispenseDocumentLine line) {
        if (null == line.getDispenseItemPlans() || line.getDispenseItemPlans().isEmpty()) {
            return;
        }
        for (DispenseItemPlan dispenseItemPlan : line.getDispenseItemPlans()) {
            for (DispensePlan dispensePlan : getDispensePlans()) {
                if (dispensePlan.getPlanRef().getCode().equals(dispenseItemPlan.getPlanRef().getCode())) {
                    dispensePlan.setTotalTax(add(dispensePlan.getTotalTax(), dispenseItemPlan.getSponsorTaxAmount()));
                    dispensePlan.setPatientDiscount(add(dispensePlan.getPatientDiscount(), dispenseItemPlan.getPatientDiscAmount()));
                    dispensePlan.setSponsorDiscount(add(dispensePlan.getSponsorDiscount(), dispenseItemPlan.getSponsorDiscAmount()));
                    dispensePlan.setSponsorPayable(add(dispensePlan.getSponsorPayable(), dispenseItemPlan.getSponsorNetAmount()));
                    dispensePlan.setSponsorGrossAmount(add(dispensePlan.getSponsorGrossAmount(), dispenseItemPlan.getSponsorGrossAmount()));

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

    private void summarizeTaxDetails(DispenseReturnDocumentLine line) {
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
            // dispenseTax.roundOff(calculateScale);
        }
    }

}
