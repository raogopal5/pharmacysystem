package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.UserDTO;

import java.io.Serializable;

public class UserHSCMapping implements Serializable {

    private Long id;
    private UserDTO user;
    private HealthcareServiceCenterDTO healthcareServiceCenter;

    public UserHSCMapping() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public HealthcareServiceCenterDTO getHealthcareServiceCenter() {
        return healthcareServiceCenter;
    }

    public void setHealthcareServiceCenter(HealthcareServiceCenterDTO healthcareServiceCenter) {
        this.healthcareServiceCenter = healthcareServiceCenter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserHSCMapping that = (UserHSCMapping) o;

        if (!id.equals(that.id)) return false;
        if (!user.equals(that.user)) return false;
        return healthcareServiceCenter.equals(that.healthcareServiceCenter);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + healthcareServiceCenter.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserHSCMapping{" +
            "id=" + id +
            ", user=" + user +
            ", healthcareServiceCenter=" + healthcareServiceCenter +
            '}';
    }
}

