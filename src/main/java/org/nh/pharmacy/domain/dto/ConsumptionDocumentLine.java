package org.nh.pharmacy.domain.dto;

import java.time.LocalDateTime;

/**
 * A ConsumptionDocumentLine.
 */
public class ConsumptionDocumentLine extends StockDocumentLine {
    //quantity consumed in case of consumption and reversal quantity in case of reversal consumption
    private Quantity quantity;
    private Quantity currentStock;
    private  Float previousReturnedQuantity;
    //source document(stock consumption) quantity consumed
    private  Float consumedQuantity;
    //required only consumption reversal
    private  String documentNumber;
    //required only consumption reversal
    private LocalDateTime consumptionDate;

    public LocalDateTime getConsumptionDate() {return consumptionDate;}

    public void setConsumptionDate(LocalDateTime consumptionDate) {this.consumptionDate = consumptionDate;}

    public String getDocumentNumber() {return documentNumber;}

    public void setDocumentNumber(String documentNumber) {this.documentNumber = documentNumber;}

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Quantity getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Quantity currentStock) {
        this.currentStock = currentStock;
    }

    public Float getPreviousReturnedQuantity() {return previousReturnedQuantity;}

    public void setPreviousReturnedQuantity(Float previousReturnedQuantity) {this.previousReturnedQuantity = previousReturnedQuantity;}

    public Float getConsumedQuantity() {return consumedQuantity;}

    public void setConsumedQuantity(Float consumedQuantity) {this.consumedQuantity = consumedQuantity;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ConsumptionDocumentLine that = (ConsumptionDocumentLine) o;

        return quantity != null ? quantity.equals(that.quantity) : that.quantity == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConsumptionDocumentLine{" +
            "quantity=" + quantity +
            ", currentStock=" + currentStock +
            ", previousReturnedQuantity=" + previousReturnedQuantity +
            ", consumedQuantity=" + consumedQuantity +
            ", documentNumber=" + documentNumber +
            '}';
    }
}
