package org.nh.pharmacy.domain.enumeration;

public enum PendingAuditRequestStatus {

    PENDING_AUDIT("Pending Audit"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String displayStatus;

    PendingAuditRequestStatus(String displayStatus) {
        this.displayStatus= displayStatus;
    }

    public String getDisplayStatus(){
        return displayStatus;
    }
}
