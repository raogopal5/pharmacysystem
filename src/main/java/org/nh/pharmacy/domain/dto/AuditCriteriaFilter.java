package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Map;

public class AuditCriteriaFilter implements Serializable {

    private String field;
    private String operator;
    private String value;
    private Map<String, Object> entity;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, Object> getEntity() {
        return entity;
    }

    public void setEntity(Map<String, Object> entity) {
        this.entity = entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditCriteriaFilter that = (AuditCriteriaFilter) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return entity != null ? entity.equals(that.entity) : that.entity == null;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}
