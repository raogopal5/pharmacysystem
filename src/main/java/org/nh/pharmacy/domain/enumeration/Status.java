

package org.nh.pharmacy.domain.enumeration;

/***
 *
 */
public enum Status {

    DRAFT("DRAFT", "Draft"),
    WAITING_FOR_APPROVAL("WAITING_FOR_APPROVAL", "Waiting For Approval"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected"),
    PARTIALLY_ISSUED("PARTIALLY_ISSUED", "Partially Issued"),
    ISSUED("ISSUED", "Issued"),
    PROCESSED("PROCESSED", "Processed"),
    PARTIALLY_PROCESSED("PARTIALLY_PROCESSED", "Partially Processed"),
    REVERSAL_PENDING("REVERSAL_PENDING", "Reversal Pending"),
    PARTIALLY_RECEIVED("PARTIALLY_RECEIVED", "Partially Received"),
    PENDING_APPROVAL("PENDING_APPROVAL", "Pending Approval"),
    @Deprecated
    CLOSED_BY_SYSTEM("CLOSED_BY_SYSTEM", "Closed By System"), //deprecated
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    CLOSED("CLOSED", "Closed");

    private final String status;
    private final String statusDisplay;

    Status(String status, String statusDisplay) {
        this.status = status;
        this.statusDisplay = statusDisplay;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public static Status findByStatus(String status) {
        Status result = null;
        for (Status val : Status.values()) {
            if (status.equals(val.getStatus())) {
                result = val;
                break;
            }
        }
        return result;
    }
}
