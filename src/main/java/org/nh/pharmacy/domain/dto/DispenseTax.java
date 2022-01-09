package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.dto.TaxDefinition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import org.nh.common.util.BigDecimalUtil;
/**
 * Created by Nirbhay on 5/26/17.
 */
public class DispenseTax implements Serializable {

    private BigDecimal taxAmount = BigDecimalUtil.ZERO;
    private BigDecimal patientTaxAmount = BigDecimalUtil.ZERO;
    private String taxCode;
    private String definition;
    private TaxDefinition taxDefinition;
    private Map<String, String> attributes;

    public BigDecimal getPatientTaxAmount() {
        return patientTaxAmount;
    }

    public void setPatientTaxAmount(BigDecimal patientTaxAmount) {
        this.patientTaxAmount = patientTaxAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public TaxDefinition getTaxDefinition() {
        return taxDefinition;
    }

    public void setTaxDefinition(TaxDefinition taxDefinition) {
        this.taxDefinition = taxDefinition;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseTax that = (DispenseTax) o;

        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) return false;
        if (taxCode != null ? !taxCode.equals(that.taxCode) : that.taxCode != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (taxDefinition != null ? !taxDefinition.equals(that.taxDefinition) : that.taxDefinition != null)
            return false;
        return attributes != null ? attributes.equals(that.attributes) : that.attributes == null;
    }

    @Override
    public int hashCode() {
        int result = taxAmount != null ? taxAmount.hashCode() : 0;
        result = 31 * result + (taxCode != null ? taxCode.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (taxDefinition != null ? taxDefinition.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseTax{" +
            "taxAmount=" + taxAmount +
            ", patientTaxAmount=" + patientTaxAmount +
            ", taxCode='" + taxCode + '\'' +
            ", definition='" + definition + '\'' +
            ", taxDefinition=" + taxDefinition +
            ", attributes=" + attributes +
            '}';
    }

    public void roundOff(int scale) {
        setTaxAmount(BigDecimalUtil.roundOff(getTaxAmount(), scale));
        setPatientTaxAmount(BigDecimalUtil.roundOff(getPatientTaxAmount(), scale));
    }
}
