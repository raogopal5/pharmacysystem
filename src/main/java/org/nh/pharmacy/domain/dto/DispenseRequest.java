package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.OrganizationDTO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by Indrajeet 11/30/2017
 */
public class DispenseRequest implements Serializable {

    private Date startDate;

    private Date endDate;

    private Integer noOfRepeatAllowed;

    private Float quantity;

    private Integer duration;

    private OrganizationDTO performer;

    private Float issuedQuantity;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getNoOfRepeatAllowed() {
        return noOfRepeatAllowed;
    }

    public void setNoOfRepeatAllowed(Integer noOfRepeatAllowed) {
        this.noOfRepeatAllowed = noOfRepeatAllowed;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public OrganizationDTO getPerformer() {
        return performer;
    }

    public void setPerformer(OrganizationDTO performer) {
        this.performer = performer;
    }

    public Float getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(Float issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispenseRequest that = (DispenseRequest) o;

        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (noOfRepeatAllowed != null ? !noOfRepeatAllowed.equals(that.noOfRepeatAllowed) : that.noOfRepeatAllowed != null)
            return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
        return performer != null ? performer.equals(that.performer) : that.performer == null;
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (noOfRepeatAllowed != null ? noOfRepeatAllowed.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (performer != null ? performer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DispenseRequest{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            ", noOfRepeatAllowed=" + noOfRepeatAllowed +
            ", quantity=" + quantity +
            ", duration=" + duration +
            ", performer=" + performer +
            '}';
    }
}
