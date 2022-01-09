package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.UserStatus;
import org.nh.pharmacy.domain.enumeration.UserType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A User.
 */
@Entity
@Table(name = "user_master")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "user", type = "user", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "prefix")
    private String prefix;

    @NotNull
    @Column(name = "employee_no", nullable = false)
    private String employeeNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "designation")
    private String designation;

    @Column(name = "mobile_no")
    private String mobileNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @ManyToOne
    private Organization organizationUnit;

    @ManyToOne
    private Organization department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public User login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public User firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public User lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public User email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isActive() {
        return active;
    }

    public User active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public User displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public User prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public User employeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
        return this;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public UserStatus getStatus() {
        return status;
    }

    public User status(UserStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public User dateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDesignation() {
        return designation;
    }

    public User designation(String designation) {
        this.designation = designation;
        return this;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public User mobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
        return this;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public UserType getUserType() {
        return userType;
    }

    public User userType(UserType userType) {
        this.userType = userType;
        return this;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Organization getOrganizationUnit() {
        return organizationUnit;
    }

    public User organizationUnit(Organization organization) {
        this.organizationUnit = organization;
        return this;
    }

    public void setOrganizationUnit(Organization organization) {
        this.organizationUnit = organization;
    }

    public Organization getDepartment() {
        return department;
    }

    public User department(Organization organization) {
        this.department = organization;
        return this;
    }

    public void setDepartment(Organization organization) {
        this.department = organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        if (user.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", login='" + login + "'" +
            ", firstName='" + firstName + "'" +
            ", lastName='" + lastName + "'" +
            ", email='" + email + "'" +
            ", active='" + active + "'" +
            ", displayName='" + displayName + "'" +
            ", prefix='" + prefix + "'" +
            ", employeeNo='" + employeeNo + "'" +
            ", status='" + status + "'" +
            ", dateOfBirth='" + dateOfBirth + "'" +
            ", designation='" + designation + "'" +
            ", mobileNo='" + mobileNo + "'" +
            ", userType='" + userType + "'" +
            '}';
    }
}
