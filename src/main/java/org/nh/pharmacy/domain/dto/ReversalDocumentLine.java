package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.ValueSetCode;

/**
 * Created by Indrajeet on 3/10/17.
 */
public class ReversalDocumentLine extends StockDocumentLine {

    private ValueSetCode reason;
    private Quantity rejectedQuantity;

    public ValueSetCode getReason() {
        return reason;
    }

    public void setReason(ValueSetCode reason) {
        this.reason = reason;
    }

    public Quantity getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(Quantity rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReversalDocumentLine that = (ReversalDocumentLine) o;

        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        return rejectedQuantity != null ? rejectedQuantity.equals(that.rejectedQuantity) : that.rejectedQuantity == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (rejectedQuantity != null ? rejectedQuantity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "ReversalDocumentLine{" +
            "reason=" + reason +
            ", rejectedQuantity=" + rejectedQuantity +
            '}';
    }
}


