package org.nh.pharmacy.domain;

import java.io.Serializable;

public class AvailableTime implements Serializable {

    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean allDay;

    public AvailableTime() {
    }

    public AvailableTime(String dayOfWeek, String startTime, String endTime, boolean allDay) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;

        this.endTime = endTime;
        this.allDay = allDay;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvailableTime that = (AvailableTime) o;

        if (allDay != that.allDay) return false;
        if (dayOfWeek != null ? !dayOfWeek.equals(that.dayOfWeek) : that.dayOfWeek != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        return endTime != null ? endTime.equals(that.endTime) : that.endTime == null;
    }

    @Override
    public int hashCode() {
        int result = dayOfWeek != null ? dayOfWeek.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (allDay ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AvailableTime{" +
            "dayOfWeek='" + dayOfWeek + '\'' +
            ", startTime='" + startTime + '\'' +
            ", endTime='" + endTime + '\'' +
            ", allDay=" + allDay +
            '}';
    }
}
