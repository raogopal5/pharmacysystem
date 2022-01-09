package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.UserDTO;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Remarks implements Serializable {

    private String remark;
    private UserDTO enteredBy;
    private LocalDateTime enteredDate;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public UserDTO getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(UserDTO enteredBy) {
        this.enteredBy = enteredBy;
    }

    public LocalDateTime getEnteredDate() {
        return enteredDate;
    }

    public void setEnteredDate(LocalDateTime enteredDate) {
        this.enteredDate = enteredDate;
    }
}
