package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;

import java.io.Serializable;

public class PrescriptionAuditRequestDTO implements Serializable {

    private PrescriptionAuditRequest prescriptionAuditRequest;
    private String transition;
    private MedicationRequest medicatonRequest;

    public PrescriptionAuditRequest getPrescriptionAuditRequest() {
        return prescriptionAuditRequest;
    }

    public void setPrescriptionAuditRequest(PrescriptionAuditRequest prescriptionAuditRequest) {
        this.prescriptionAuditRequest = prescriptionAuditRequest;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    public MedicationRequest getMedicatonRequest() {
        return medicatonRequest;
    }

    public void setMedicatonRequest(MedicationRequest medicatonRequest) {
        this.medicatonRequest = medicatonRequest;
    }
}
