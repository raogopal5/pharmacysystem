package org.nh.pharmacy.domain;

import java.io.Serializable;

/**
 * Created by Nirbhay on 1/29/18.
 */
public class DiscountSlab implements Serializable {

    private Float min;
    private Float max;
    private Float percentage;

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "DiscountSlab{" +
            "min=" + min +
            ", max=" + max +
            ", percentage=" + percentage +
            '}';
    }
}
