package org.nh.pharmacy.domain.dto;

import java.io.Serializable;

public class Member implements Serializable {

    private Long id;
    private String login;
    private String displayName;
    private String employeeNumber;
    private String employeeUnit;
    private String email;
    private String mobileNumber;

    public Member() {
    }

    public Member(Long id, String login, String displayName, String employeeNumber, String employeeUnit, String email, String mobileNumber) {
        this.id = id;
        this.login = login;
        this.displayName = displayName;
        this.employeeNumber = employeeNumber;
        this.employeeUnit = employeeUnit;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeUnit() {
        return employeeUnit;
    }

    public void setEmployeeUnit(String employeeUnit) {
        this.employeeUnit = employeeUnit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        if (id != null ? !id.equals(member.id) : member.id != null) return false;
        if (login != null ? !login.equals(member.login) : member.login != null) return false;
        if (displayName != null ? !displayName.equals(member.displayName) : member.displayName != null) return false;
        if (employeeNumber != null ? !employeeNumber.equals(member.employeeNumber) : member.employeeNumber != null)
            return false;
        if (employeeUnit != null ? !employeeUnit.equals(member.employeeUnit) : member.employeeUnit != null)
            return false;
        if (email != null ? !email.equals(member.email) : member.email != null) return false;
        return mobileNumber != null ? mobileNumber.equals(member.mobileNumber) : member.mobileNumber == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (employeeNumber != null ? employeeNumber.hashCode() : 0);
        result = 31 * result + (employeeUnit != null ? employeeUnit.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (mobileNumber != null ? mobileNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Member{" +
            "id=" + id +
            ", login='" + login + '\'' +
            ", displayName='" + displayName + '\'' +
            ", employeeNumber='" + employeeNumber + '\'' +
            ", employeeUnit='" + employeeUnit + '\'' +
            ", email='" + email + '\'' +
            ", mobileNumber='" + mobileNumber + '\'' +
            '}';
    }
}
