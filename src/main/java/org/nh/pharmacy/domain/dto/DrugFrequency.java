package org.nh.pharmacy.domain.dto;

import java.io.Serializable;

/**
 * Created by Indrajeet on 12/04/17
 */
public class DrugFrequency implements Serializable {

    private String code;

    private String name;

    private Long frequency;

    private Float dailyIntake;

    private String description;

    private Long period;

    private PeriodUnit periodUnit;

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

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Float getDailyIntake() {
        return dailyIntake;
    }

    public void setDailyIntake(Float dailyIntake) {
        this.dailyIntake = dailyIntake;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public PeriodUnit getPeriodUnit() {
        return periodUnit;
    }

    public void setPeriodUnit(PeriodUnit periodUnit) {
        this.periodUnit = periodUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrugFrequency that = (DrugFrequency) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (frequency != null ? !frequency.equals(that.frequency) : that.frequency != null) return false;
        if (dailyIntake != null ? !dailyIntake.equals(that.dailyIntake) : that.dailyIntake != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (period != null ? !period.equals(that.period) : that.period != null) return false;
        return periodUnit != null ? periodUnit.equals(that.periodUnit) : that.periodUnit == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (frequency != null ? frequency.hashCode() : 0);
        result = 31 * result + (dailyIntake != null ? dailyIntake.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        result = 31 * result + (periodUnit != null ? periodUnit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DrugFrequency{" +
            "code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", frequency=" + frequency +
            ", dailyIntake=" + dailyIntake +
            ", description='" + description + '\'' +
            ", period=" + period +
            ", periodUnit='" + periodUnit + '\'' +
            '}';
    }
}
