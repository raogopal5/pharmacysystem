package org.nh.pharmacy.domain.dto;

/**
 * Created by Nirbhay on 3/10/17.
 */
public class IndentDocumentLine extends StockDocumentLine {

    private Consumption consumedQuantity;
    private Float availableStock;
    private Quantity quantity;
    private Float transitQuantity;

    public Consumption getConsumedQuantity() {
        return consumedQuantity;
    }

    public void setConsumedQuantity(Consumption consumedQuantity) {
        this.consumedQuantity = consumedQuantity;
    }

    public Float getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Float availableStock) {
        this.availableStock = availableStock;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Float getTransitQuantity() {
        return transitQuantity;
    }

    public void setTransitQuantity(Float transitQuantity) {
        this.transitQuantity = transitQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IndentDocumentLine line = (IndentDocumentLine) o;

        if (consumedQuantity != null ? !consumedQuantity.equals(line.consumedQuantity) : line.consumedQuantity != null)
            return false;
        if (availableStock != null ? !availableStock.equals(line.availableStock) : line.availableStock != null)
            return false;
        if (quantity != null ? !quantity.equals(line.quantity) : line.quantity != null) return false;
        return transitQuantity != null ? transitQuantity.equals(line.transitQuantity) : line.transitQuantity == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (consumedQuantity != null ? consumedQuantity.hashCode() : 0);
        result = 31 * result + (availableStock != null ? availableStock.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (transitQuantity != null ? transitQuantity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IndentDocumentLine{" +
            "consumedQuantity=" + consumedQuantity +
            ", availableStock=" + availableStock +
            ", quantity=" + quantity +
            ", transitQuantity=" + transitQuantity +
            '}';
    }
}

