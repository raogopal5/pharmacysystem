package org.nh.pharmacy.domain.dto;

import org.nh.common.dto.ItemDTO;
import org.nh.pharmacy.domain.ValueSetCode;

/**
 * Created by Nitesh on 3/13/17.
 */
public class ReceiptDocumentLine extends StockDocumentLine {

    private Quantity acceptedQuantity;
    private Quantity rejectedQuantity;
    private ValueSetCode reason;
    private ItemDTO originalItem;
    private Quantity reversalQuantity;
    private ValueSetCode reversalReason;

    public Quantity getAcceptedQuantity() {
        return acceptedQuantity;
    }

    public void setAcceptedQuantity(Quantity acceptedQuantity) {
        this.acceptedQuantity = acceptedQuantity;
    }

    public Quantity getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(Quantity rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public ValueSetCode getReason() {
        return reason;
    }

    public void setReason(ValueSetCode reason) {
        this.reason = reason;
    }

    public ItemDTO getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(ItemDTO originalItem) {
        this.originalItem = originalItem;
    }

    public Quantity getReversalQuantity() {
        return reversalQuantity;
    }

    public void setReversalQuantity(Quantity reversalQuantity) {
        this.reversalQuantity = reversalQuantity;
    }

    public ValueSetCode getReversalReason() {
        return reversalReason;
    }

    public void setReversalReason(ValueSetCode reversalReason) {
        this.reversalReason = reversalReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReceiptDocumentLine that = (ReceiptDocumentLine) o;

        if (acceptedQuantity != null ? !acceptedQuantity.equals(that.acceptedQuantity) : that.acceptedQuantity != null)
            return false;
        if (rejectedQuantity != null ? !rejectedQuantity.equals(that.rejectedQuantity) : that.rejectedQuantity != null)
            return false;
        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        if (originalItem != null ? !originalItem.equals(that.originalItem) : that.originalItem != null) return false;
        if (reversalQuantity != null ? !reversalQuantity.equals(that.reversalQuantity) : that.reversalQuantity != null)
            return false;
        return reversalReason != null ? reversalReason.equals(that.reversalReason) : that.reversalReason == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (acceptedQuantity != null ? acceptedQuantity.hashCode() : 0);
        result = 31 * result + (rejectedQuantity != null ? rejectedQuantity.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (originalItem != null ? originalItem.hashCode() : 0);
        result = 31 * result + (reversalQuantity != null ? reversalQuantity.hashCode() : 0);
        result = 31 * result + (reversalReason != null ? reversalReason.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "ReceiptDocumentLine{" +
            "acceptedQuantity=" + acceptedQuantity +
            ", rejectedQuantity=" + rejectedQuantity +
            ", reason=" + reason +
            ", originalItem=" + originalItem +
            ", reversalQuantity=" + reversalQuantity +
            ", reversalReason=" + reversalReason +
            '}';
    }
}
