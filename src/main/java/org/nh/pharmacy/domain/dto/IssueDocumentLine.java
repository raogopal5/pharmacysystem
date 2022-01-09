package org.nh.pharmacy.domain.dto;


import org.nh.common.dto.ItemDTO;

/**
 * Created by Nitesh on 3/14/17.
 */
public class IssueDocumentLine extends StockDocumentLine {

    private Quantity currentIndentStockQuantity;
    private Quantity currentIssueStockQuantity;
    private Quantity issuedQuantity;
    private ItemDTO originalItem;

    public Quantity getCurrentIndentStock() {
        return currentIndentStockQuantity;
    }

    public void setCurrentIndentStock(Quantity currentIndentStockQuantity) {
        this.currentIndentStockQuantity = currentIndentStockQuantity;
    }

    public Quantity getCurrentIssueStock() {
        return currentIssueStockQuantity;

    }

    public void setCurrentIssueStock(Quantity currentIssueStockQuantity) {
        this.currentIssueStockQuantity = currentIssueStockQuantity;
    }

    public Quantity getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(Quantity issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public ItemDTO getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(ItemDTO originalItem) {
        this.originalItem = originalItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IssueDocumentLine that = (IssueDocumentLine) o;

        if (currentIndentStockQuantity != null ? !currentIndentStockQuantity.equals(that.currentIndentStockQuantity) : that.currentIndentStockQuantity != null)
            return false;
        if (currentIssueStockQuantity != null ? !currentIssueStockQuantity.equals(that.currentIssueStockQuantity) : that.currentIssueStockQuantity != null)
            return false;
        return issuedQuantity != null ? issuedQuantity.equals(that.issuedQuantity) : that.issuedQuantity == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (currentIndentStockQuantity != null ? currentIndentStockQuantity.hashCode() : 0);
        result = 31 * result + (currentIssueStockQuantity != null ? currentIssueStockQuantity.hashCode() : 0);
        result = 31 * result + (issuedQuantity != null ? issuedQuantity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "IssueDocumentLine{" +
            "currentIndentStockQuantity=" + currentIndentStockQuantity +
            ", currentIssueStockQuantity=" + currentIssueStockQuantity +
            ", issuedQuantity=" + issuedQuantity +
            ", originalItem=" + originalItem +
            '}';
    }
}
