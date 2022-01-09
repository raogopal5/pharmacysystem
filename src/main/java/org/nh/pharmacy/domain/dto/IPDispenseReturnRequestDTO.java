package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.pharmacy.domain.enumeration.IPReturnRequestStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A IPDispenseReturnRequestDTO class
 */
public class IPDispenseReturnRequestDTO implements Serializable {

    //properties
    private String documentNumber;
    private String itemName;
    private HealthcareServiceCenterDTO returnhsc;
    private IPReturnRequestStatus ipReturnRequestStatus;
    private Float pendingQuantity;
    private String visitNumber;
    private LocalDateTime returnRequestDate;

    public String getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public HealthcareServiceCenterDTO getReturnhsc() {
        return returnhsc;
    }

    public void setReturnhsc(HealthcareServiceCenterDTO returnhsc) {
        this.returnhsc = returnhsc;
    }

    public IPReturnRequestStatus getIpReturnRequestStatus() {
        return ipReturnRequestStatus;
    }

    public void setIpReturnRequestStatus(IPReturnRequestStatus ipReturnRequestStatus) {
        this.ipReturnRequestStatus = ipReturnRequestStatus;
    }

    public Float getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(Float pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }


    public LocalDateTime getReturnRequestDate() {
        return returnRequestDate;
    }

    public void setReturnRequestDate(LocalDateTime returnRequestDate) {
        this.returnRequestDate = returnRequestDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPDispenseReturnRequestDTO that = (IPDispenseReturnRequestDTO) o;
        return Objects.equals(documentNumber, that.documentNumber) &&
            Objects.equals(itemName, that.itemName) &&
            Objects.equals(returnhsc, that.returnhsc) &&
            ipReturnRequestStatus == that.ipReturnRequestStatus &&
            Objects.equals(pendingQuantity, that.pendingQuantity) &&
            Objects.equals(returnRequestDate, that.returnRequestDate) &&
            Objects.equals(visitNumber, that.visitNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentNumber, itemName, returnhsc, ipReturnRequestStatus, pendingQuantity, visitNumber, returnRequestDate);
    }

    @Override
    public String toString() {
        return "IPDispenseReturnRequestDTO{" +
            "documentNumber='" + documentNumber + '\'' +
            ", itemName='" + itemName + '\'' +
            ", returnhsc=" + returnhsc +
            ", ipReturnRequestStatus=" + ipReturnRequestStatus +
            ", pendingQuantity=" + pendingQuantity +
            ", visitNumber='" + visitNumber + '\'' +
            ", returnRequestDate='" + returnRequestDate + '\'' +
            '}';
    }
}
