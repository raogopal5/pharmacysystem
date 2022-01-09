package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ValueSetCodeDTO;

import java.io.Serializable;

public class DosageInstruction implements Serializable {

    private String patientInstruction;

    private Integer sequence;

    private DrugFrequency drugFrequency;

    private Float doseQuantity;

    private String when;  //food Instruction

    private String site;

    private String route;

    private String method;

   // private ValueSetCodeDTO foodInstruction;

    private DailyDosage dayWiseDosage;

 /*   public ValueSetCodeDTO getFoodInstruction() {
        return foodInstruction;
    }

    public void setFoodInstruction(ValueSetCodeDTO foodInstruction) {
        this.foodInstruction = foodInstruction;
    } */

    public DailyDosage getDayWiseDosage() {
        return dayWiseDosage;
    }

    public void setDayWiseDosage(DailyDosage dayWiseDosage) {
        this.dayWiseDosage = dayWiseDosage;
    }

    public String getPatientInstruction() {
        return patientInstruction;
    }

    public void setPatientInstruction(String patientInstruction) {
        this.patientInstruction = patientInstruction;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public DrugFrequency getDrugFrequency() {
        return drugFrequency;
    }

    public void setDrugFrequency(DrugFrequency drugFrequency) {
        this.drugFrequency = drugFrequency;
    }

    public Float getDoseQuantity() {
        return doseQuantity;
    }

    public void setDoseQuantity(Float doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DosageInstruction that = (DosageInstruction) o;

        if (patientInstruction != null ? !patientInstruction.equals(that.patientInstruction) : that.patientInstruction != null)
            return false;
        if (sequence != null ? !sequence.equals(that.sequence) : that.sequence != null) return false;
        if (drugFrequency != null ? !drugFrequency.equals(that.drugFrequency) : that.drugFrequency != null)
            return false;
        if (doseQuantity != null ? !doseQuantity.equals(that.doseQuantity) : that.doseQuantity != null) return false;
        if (when != null ? !when.equals(that.when) : that.when != null) return false;
        if (site != null ? !site.equals(that.site) : that.site != null) return false;
        if (route != null ? !route.equals(that.route) : that.route != null) return false;
        return method != null ? method.equals(that.method) : that.method == null;
    }

    @Override
    public int hashCode() {
        int result = patientInstruction != null ? patientInstruction.hashCode() : 0;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (drugFrequency != null ? drugFrequency.hashCode() : 0);
        result = 31 * result + (doseQuantity != null ? doseQuantity.hashCode() : 0);
        result = 31 * result + (when != null ? when.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (route != null ? route.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DosageInstruction{" +
            "patientInstruction='" + patientInstruction + '\'' +
            ", sequence=" + sequence +
            ", drugFrequency=" + drugFrequency +
            ", doseQuantity=" + doseQuantity +
            ", when='" + when + '\'' +
            ", site='" + site + '\'' +
            ", route='" + route + '\'' +
            ", method='" + method + '\'' +
            '}';
    }
}
