package org.nh.pharmacy.domain.dto;

public class DailyDosage {
    private Double afternoon = 0d;
    private Double evening = 0d;
    private Double morning = 0d;
    private Double night = 0d;
    private String unit = "";

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getAfternoon() {
        return afternoon;
    }

    public void setAfternoon(Double afternoon) {
        this.afternoon = afternoon;
    }

    public Double getEvening() {
        return evening;
    }

    public void setEvening(Double evening) {
        this.evening = evening;
    }

    public Double getMorning() {
        return morning;
    }

    public void setMorning(Double morning) {
        this.morning = morning;
    }

    public Double getNight() {
        return night;
    }

    public void setNight(Double night) {
        this.night = night;
    }
}
