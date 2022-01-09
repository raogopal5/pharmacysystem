package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.dto.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IPDispenseReturnDocumentLine extends DocumentLine{
    private BigDecimal returnAmount;
    private BigDecimal mrp;
    private BigDecimal cost;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BigDecimal taxAmount;


    private SourceDTO dispenseRef;
    private UserDTO dispenseUser;
    private OrganizationDTO dispenseUnit;
    private LocalDateTime dispenseDate;

    private Float dispensedQuantity;
    private Float earlierReturnQuantity=0f;
    private Float requestedReturnQuantity=0f;
    private Float acceptedReturnQuantity=0f;
    private Float previousAcceptedReturnQty =0f;
    private Float pendingReturnQuantity=0f;
    private String  dispenseRefNumber;


    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public Float getDispensedQuantity() {
        return dispensedQuantity;
    }

    public void setDispensedQuantity(Float dispensedQuantity) {
        this.dispensedQuantity = dispensedQuantity;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
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

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public SourceDTO getDispenseRef() {
        return dispenseRef;
    }

    public void setDispenseRef(SourceDTO dispenseRef) {
        this.dispenseRef = dispenseRef;
    }

    public Float getEarlierReturnQuantity() {
        return earlierReturnQuantity;
    }

    public void setEarlierReturnQuantity(Float earlierReturnQuantity) {
        this.earlierReturnQuantity = earlierReturnQuantity;
    }

    public Float getRequestedReturnQuantity() {
        return requestedReturnQuantity;
    }

    public void setRequestedReturnQuantity(Float requestedReturnQuantity) {
        this.requestedReturnQuantity = requestedReturnQuantity;
    }

    public Float getAcceptedReturnQuantity() {
        return acceptedReturnQuantity;
    }

    public void setAcceptedReturnQuantity(Float acceptedReturnQuantity) {
        this.acceptedReturnQuantity = acceptedReturnQuantity;
    }

    public Float getPendingReturnQuantity() {
        return pendingReturnQuantity;
    }

    public void setPendingReturnQuantity(Float pendingReturnQuantity) {
        this.pendingReturnQuantity = pendingReturnQuantity;
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

    public LocalDateTime getDispenseDate() {
        return dispenseDate;
    }

    public void setDispenseDate(LocalDateTime dispenseDate) {
        this.dispenseDate = dispenseDate;
    }

    public String getDispenseRefNumber() {
        return dispenseRefNumber;
    }

    public void setDispenseRefNumber(String dispenseRefNumber) {
        this.dispenseRefNumber = dispenseRefNumber;
    }

    public Float getPreviousAcceptedReturnQty() {
        return previousAcceptedReturnQty;
    }

    public void setPreviousAcceptedReturnQty(Float previousAcceptedReturnQty) {
        this.previousAcceptedReturnQty = previousAcceptedReturnQty;
    }
}
