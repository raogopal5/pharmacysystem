package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.UserDTO;

import java.io.Serializable;
import java.util.List;

public class AuditCriteria implements Serializable {

    public List<AuditCriteriaFilter> auditFilter;
    public UserDTO auditingUser;

    public List<AuditCriteriaFilter> getAuditFilter() {
        return auditFilter;
    }

    public void setAuditFilter(List<AuditCriteriaFilter> auditFilter) {
        this.auditFilter = auditFilter;
    }

    public UserDTO getAuditingUser() {
        return auditingUser;
    }

    public void setAuditingUser(UserDTO auditingUser) {
        this.auditingUser = auditingUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditCriteria that = (AuditCriteria) o;

        if (auditFilter != null ? !auditFilter.equals(that.auditFilter) : that.auditFilter != null) return false;
        return auditingUser != null ? auditingUser.equals(that.auditingUser) : that.auditingUser == null;
    }

    @Override
    public int hashCode() {
        int result = auditFilter != null ? auditFilter.hashCode() : 0;
        result = 31 * result + (auditingUser != null ? auditingUser.hashCode() : 0);
        return result;
    }
}
