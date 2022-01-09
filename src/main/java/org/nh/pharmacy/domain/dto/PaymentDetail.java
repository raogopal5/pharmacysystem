package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.enumeration.AdjustmentStatus;
import org.nh.billing.domain.enumeration.PaymentMode;
import org.nh.billing.domain.enumeration.ReceiptType;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.UserDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Created by Nirbhay on 6/22/17.
 */
public class PaymentDetail implements Serializable {

    private String transactionCurrency;
    private BigDecimal transactionAmount;
    private String baseCurrency;
    private BigDecimal totalAmount;
    private BigDecimal unsettledAmount;
    private BigDecimal exchangeRate;
    private PaymentMode paymentMode;
    private String instrumentNumber;
    private LocalDate instrumentValidityDate;
    private AdjustmentStatus adjustmentStatus;
    private ReceiptType receiptType;
    private Map<String, Object> bankDetails;
    private Map<String, Object> machineDetails;
    private UserDTO receivedBy;
    private LocalDateTime receivedDate;
    private UserDTO approvedBy;
    private LocalDateTime approvedDate;
    private HealthcareServiceCenterDTO hsc;
    private String interfaceValue;
    private Map<String, Object> transactionInfo;

    public Map<String, Object> getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(Map<String, Object> transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public HealthcareServiceCenterDTO getHsc() {
        return hsc;
    }

    public void setHsc(HealthcareServiceCenterDTO hsc) {
        this.hsc = hsc;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getUnsettledAmount() {
        return unsettledAmount;
    }

    public void setUnsettledAmount(BigDecimal unsettledAmount) {
        this.unsettledAmount = unsettledAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getInstrumentNumber() {
        return instrumentNumber;
    }

    public void setInstrumentNumber(String instrumentNumber) {
        this.instrumentNumber = instrumentNumber;
    }

    public LocalDate getInstrumentValidityDate() {
        return instrumentValidityDate;
    }

    public void setInstrumentValidityDate(LocalDate instrumentValidityDate) {
        this.instrumentValidityDate = instrumentValidityDate;
    }

    public AdjustmentStatus getAdjustmentStatus() {
        return adjustmentStatus;
    }

    public void setAdjustmentStatus(AdjustmentStatus adjustmentStatus) {
        this.adjustmentStatus = adjustmentStatus;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public Map<String, Object> getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(Map<String, Object> bankDetails) {
        this.bankDetails = bankDetails;
    }

    public Map<String, Object> getMachineDetails() {
        return machineDetails;
    }

    public void setMachineDetails(Map<String, Object> machineDetails) {
        this.machineDetails = machineDetails;
    }

    public UserDTO getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(UserDTO receivedBy) {
        this.receivedBy = receivedBy;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
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

    public String getInterfaceValue() {
        return interfaceValue;
    }

    public void setInterfaceValue(String interfaceValue) {
        this.interfaceValue = interfaceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentDetail that = (PaymentDetail) o;

        if (transactionCurrency != null ? !transactionCurrency.equals(that.transactionCurrency) : that.transactionCurrency != null)
            return false;
        if (transactionAmount != null ? !transactionAmount.equals(that.transactionAmount) : that.transactionAmount != null)
            return false;
        if (baseCurrency != null ? !baseCurrency.equals(that.baseCurrency) : that.baseCurrency != null) return false;
        if (totalAmount != null ? !totalAmount.equals(that.totalAmount) : that.totalAmount != null) return false;
        if (unsettledAmount != null ? !unsettledAmount.equals(that.unsettledAmount) : that.unsettledAmount != null)
            return false;
        if (exchangeRate != null ? !exchangeRate.equals(that.exchangeRate) : that.exchangeRate != null) return false;
        if (paymentMode != that.paymentMode) return false;
        if (instrumentNumber != null ? !instrumentNumber.equals(that.instrumentNumber) : that.instrumentNumber != null)
            return false;
        if (adjustmentStatus != that.adjustmentStatus) return false;
        if (receiptType != that.receiptType) return false;
        if (bankDetails != null ? !bankDetails.equals(that.bankDetails) : that.bankDetails != null) return false;
        if (machineDetails != null ? !machineDetails.equals(that.machineDetails) : that.machineDetails != null)
            return false;
        if (receivedBy != null ? !receivedBy.equals(that.receivedBy) : that.receivedBy != null) return false;
        if (receivedDate != null ? !receivedDate.equals(that.receivedDate) : that.receivedDate != null) return false;
        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (hsc != null ? !hsc.equals(that.hsc) : that.hsc != null) return false;
        return interfaceValue != null ? interfaceValue.equals(that.interfaceValue) : that.interfaceValue == null;
    }

    @Override
    public int hashCode() {
        int result = transactionCurrency != null ? transactionCurrency.hashCode() : 0;
        result = 31 * result + (transactionAmount != null ? transactionAmount.hashCode() : 0);
        result = 31 * result + (baseCurrency != null ? baseCurrency.hashCode() : 0);
        result = 31 * result + (totalAmount != null ? totalAmount.hashCode() : 0);
        result = 31 * result + (unsettledAmount != null ? unsettledAmount.hashCode() : 0);
        result = 31 * result + (exchangeRate != null ? exchangeRate.hashCode() : 0);
        result = 31 * result + (paymentMode != null ? paymentMode.hashCode() : 0);
        result = 31 * result + (instrumentNumber != null ? instrumentNumber.hashCode() : 0);
        result = 31 * result + (adjustmentStatus != null ? adjustmentStatus.hashCode() : 0);
        result = 31 * result + (receiptType != null ? receiptType.hashCode() : 0);
        result = 31 * result + (bankDetails != null ? bankDetails.hashCode() : 0);
        result = 31 * result + (machineDetails != null ? machineDetails.hashCode() : 0);
        result = 31 * result + (receivedBy != null ? receivedBy.hashCode() : 0);
        result = 31 * result + (receivedDate != null ? receivedDate.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        result = 31 * result + (hsc != null ? hsc.hashCode() : 0);
        result = 31 * result + (approvedDate != null ? approvedDate.hashCode() : 0);
        result = 31 * result + (interfaceValue != null ? interfaceValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PaymentDetail{" +
            "transactionCurrency='" + transactionCurrency + '\'' +
            ", transactionAmount=" + transactionAmount +
            ", baseCurrency='" + baseCurrency + '\'' +
            ", totalAmount=" + totalAmount +
            ", unsettledAmount=" + unsettledAmount +
            ", exchangeRate=" + exchangeRate +
            ", paymentMode=" + paymentMode +
            ", instrumentNumber='" + instrumentNumber + '\'' +
            ", instrumentValidityDate='" + instrumentValidityDate + '\'' +
            ", adjustmentStatus=" + adjustmentStatus +
            ", receiptType=" + receiptType +
            ", bankDetails=" + bankDetails +
            ", machineDetails=" + machineDetails +
            ", receivedBy=" + receivedBy +
            ", receivedDate=" + receivedDate +
            ", approvedBy=" + approvedBy +
            ", hsc=" + hsc +
            ", approvedDate=" + approvedDate +
            ", interfaceValue='" + interfaceValue +
            '}';
    }
}
