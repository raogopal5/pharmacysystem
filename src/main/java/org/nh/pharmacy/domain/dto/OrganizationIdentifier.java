package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Date;

public class OrganizationIdentifier implements Serializable {

    private String type;
    private String value;
    private Date validFrom;
    private Date validTo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganizationIdentifier that = (OrganizationIdentifier) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (validFrom != null ? !validFrom.equals(that.validFrom) : that.validFrom != null) return false;
        return validTo != null ? validTo.equals(that.validTo) : that.validTo == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (validFrom != null ? validFrom.hashCode() : 0);
        result = 31 * result + (validTo != null ? validTo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrganizationIdentifier{" +
            "type='" + type + '\'' +
            ", value='" + value + '\'' +
            ", validFrom=" + validFrom +
            ", validTo=" + validTo +
            '}';
    }
}
