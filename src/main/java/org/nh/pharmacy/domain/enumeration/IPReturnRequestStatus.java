package org.nh.pharmacy.domain.enumeration;

public enum IPReturnRequestStatus {
    PENDING("Pending"),
    RETURNED("Returned"),
    PARTIALLY_RETURNED("Partially Returned"),
    REJECTED("Rejected"),
    PARTIALLY_REJECTED("Partially Rejected");

    private final String displayStatus;

    IPReturnRequestStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus(){
        return displayStatus;
    }
}
