package org.nh.pharmacy.domain.enumeration;

public enum  AdmissionStatus {
    DRAFT("Draft"),
    PENDING_FC("Peding FC"),
    BILLING_CLEARED("Billing Cleared"),
    WAITING_FOR_ADMISSION("Waiting For Admission"),
    UNDER_IP_CARE("Under IP Care"),
    UNDER_DAY_CARE("Under Day Care"),
    UNDER_EMERGENCY("Under Emergency"),
    ABSCONDED("Absconded"),
    MARKED_FOR_DISCHARGE("Marked For Discharge"),
    DISCHARGED("Discharged"),
    DISCHARGE_INTIMATED("Discharge Intimated"),
    MARKED_FOR_IP("Marked For IP"),
    ADMITTED_TO_IP("Admitted To IP"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected"),
    APPROVED("Approved"),
    PENDING("Pending"),
    FC_REJECTED("FC Rejected");

    private final String displayStatus;

    AdmissionStatus(String displayStatus){
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus(){
        return displayStatus;
    }
}
