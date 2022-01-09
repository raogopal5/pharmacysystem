package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by vagrant on 6/7/17.
 */
public class OrderItem implements Serializable {

    private String lineNumber;
    private String code;
    private String name;
    private Boolean isGeneric;
    private Boolean sustitute;
    private Float quantity;
    private String uom;
    private String dosageInstruction;
    private String note;
    private Map<String, Object> medication;
    private Map<String, Object> item;

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getGeneric() {
        return isGeneric;
    }

    public void setGeneric(Boolean generic) {
        isGeneric = generic;
    }

    public Boolean getSustitute() {
        return sustitute;
    }

    public void setSustitute(Boolean sustitute) {
        this.sustitute = sustitute;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getDosageInstruction() {
        return dosageInstruction;
    }

    public void setDosageInstruction(String dosageInstruction) {
        this.dosageInstruction = dosageInstruction;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Map<String, Object> getMedication() {
        return medication;
    }

    public void setMedication(Map<String, Object> medication) {
        this.medication = medication;
    }

    public Map<String, Object> getItem() {
        return item;
    }

    public void setItem(Map<String, Object> item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        if (lineNumber != null ? !lineNumber.equals(orderItem.lineNumber) : orderItem.lineNumber != null) return false;
        if (code != null ? !code.equals(orderItem.code) : orderItem.code != null) return false;
        if (name != null ? !name.equals(orderItem.name) : orderItem.name != null) return false;
        if (isGeneric != null ? !isGeneric.equals(orderItem.isGeneric) : orderItem.isGeneric != null) return false;
        if (sustitute != null ? !sustitute.equals(orderItem.sustitute) : orderItem.sustitute != null) return false;
        if (quantity != null ? !quantity.equals(orderItem.quantity) : orderItem.quantity != null) return false;
        if (uom != null ? !uom.equals(orderItem.uom) : orderItem.uom != null) return false;
        if (dosageInstruction != null ? !dosageInstruction.equals(orderItem.dosageInstruction) : orderItem.dosageInstruction != null)
            return false;
        if (note != null ? !note.equals(orderItem.note) : orderItem.note != null) return false;
        if (medication != null ? !medication.equals(orderItem.medication) : orderItem.medication != null) return false;
        return item != null ? item.equals(orderItem.item) : orderItem.item == null;
    }

    @Override
    public int hashCode() {
        int result = lineNumber != null ? lineNumber.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isGeneric != null ? isGeneric.hashCode() : 0);
        result = 31 * result + (sustitute != null ? sustitute.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (uom != null ? uom.hashCode() : 0);
        result = 31 * result + (dosageInstruction != null ? dosageInstruction.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (medication != null ? medication.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
            "lineNumber='" + lineNumber + '\'' +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", isGeneric=" + isGeneric +
            ", sustitute=" + sustitute +
            ", quantity=" + quantity +
            ", uom='" + uom + '\'' +
            ", dosageInstruction='" + dosageInstruction + '\'' +
            ", note='" + note + '\'' +
            ", medication=" + medication +
            ", item=" + item +
            '}';
    }
}
