package org.nh.pharmacy.domain.enumeration;

/**
 * Created by Prahsanth on 03/01/18.
 */
public enum MedicationRequestStatus {
    ORDERED("Ordered"),
    PARTIALLY_DISPENSED("Partially Dispensed"),
    DISPENSED("Dispensed"),
    CANCELLED("Cancelled"),
    CLOSED("Closed"),
    PARTIALLY_CLOSED("Partially Closed");


    private final String displayStatus;

    MedicationRequestStatus(String displayStatus) {
        this.displayStatus= displayStatus;
    }

    public String getDisplayStatus(){
        return displayStatus;
    }
}
