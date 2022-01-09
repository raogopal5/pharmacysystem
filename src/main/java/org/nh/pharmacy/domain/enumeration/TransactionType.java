package org.nh.pharmacy.domain.enumeration;

/**
 * The TransactionType enumeration.
 */
public enum TransactionType {
    GRN("GRN", "GRN"),
    Stock_Receipt("Stock_Receipt", "Receipt"),
    Stock_Issue("Stock_Issue", "Issue"),
    Stock_Direct_Transfer("Stock_Direct_Transfer", "Direct Transfer"),
    Stock_Indent("Stock_Indent", "Indent"),
    Stock_Consumption("Stock_Consumption", "Consumption"),
    Stock_Reversal_Consumption("Stock_Reversal_Consumption", "Reversal Consumption"),
    Stock_Reversal("Stock_Reversal", "Reversal"),
    Inventory_Adjustment("Inventory_Adjustment", "Inventory Adjustment"),
    Stock_Audit_Plan("Stock_Audit_Plan", "Audit Plan"),
    Stock_Audit("Stock_Audit", "Audit"),
    Stock_Correction("Stock_Correction", "Correction"),
    Dispense("Dispense", "Dispense"),
    Dispense_Return("Dispense_Return", "Dispense Return"),
    Return_To_Vendor("Return_To_Vendor", "Return To Vendor"),
    Inter_Unit_Stock_Indent("Inter_Unit_Stock_Indent", "Inter Unit Stock Indent"),
    Inter_Unit_Stock_Issue("Inter_Unit_Stock_Issue", "Inter Unit Stock Issue"),
    Inter_Unit_Stock_Receipt("Inter_Unit_Stock_Receipt", "Inter Unit Stock Receipt"),
    Inter_Unit_Stock_Reversal("Inter_Unit_Stock_Reversal", "Inter Unit Stock Reversal");

    private final String transactionType;
    private final String transactionTypeDisplay;

    TransactionType(String transactionType, String transactionTypeDisplay) {
        this.transactionType = transactionType;
        this.transactionTypeDisplay = transactionTypeDisplay;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getTransactionTypeDisplay() {
        return transactionTypeDisplay;
    }


    public static TransactionType findByTransactionType(String transactionType) {
        TransactionType result = null;
        for (TransactionType val : TransactionType.values()) {
            if (transactionType.equals(val.getTransactionType())) {
                result = val;
                break;
            }
        }
        return result;
    }

}
