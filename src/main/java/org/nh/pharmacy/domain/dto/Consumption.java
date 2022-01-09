package org.nh.pharmacy.domain.dto;

import java.io.Serializable;

/**
 * Created by Nirbhay on 3/7/17.
 */
public class Consumption implements Serializable {

    private Float consumedInLastMonth;
    private Float consumedInCurrentMonth;

    public Float getConsumedInLastMonth() {
        return consumedInLastMonth;
    }

    public void setConsumedInLastMonth(Float consumedInLastMonth) {
        this.consumedInLastMonth = consumedInLastMonth;
    }

    public Float getConsumedInCurrentMonth() {
        return consumedInCurrentMonth;
    }

    public void setConsumedInCurrentMonth(Float consumedInCurrentMonth) {
        this.consumedInCurrentMonth = consumedInCurrentMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Consumption that = (Consumption) o;

        if (consumedInLastMonth != null ? !consumedInLastMonth.equals(that.consumedInLastMonth) : that.consumedInLastMonth != null)
            return false;
        return consumedInCurrentMonth != null ? !consumedInCurrentMonth.equals(that.consumedInCurrentMonth) : that.consumedInCurrentMonth != null;
    }

    @Override
    public int hashCode() {
        int result = consumedInLastMonth != null ? consumedInLastMonth.hashCode() : 0;
        result = 31 * result + (consumedInCurrentMonth != null ? consumedInCurrentMonth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Consumption{" +
            "consumedInLastMonth=" + consumedInLastMonth +
            ", consumedInCurrentMonth=" + consumedInCurrentMonth +
            '}';
    }
}
