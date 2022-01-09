package org.nh.pharmacy.domain.enumeration;

/**
 * The Context enumeration.
 */
public enum Context {
    Indent_Approval_Committee ("Indent approval Group"),
    Issue_Notification_Committee (""),
    Issue_Approval_Committee ("Issue Approval Group"),
    Receipt_Approval_Committee ("Receipt Approval Group"),
    Consumption_Approval_Committee ("Consumption Approval Group"),
    Correction_Approval_Committee ("Correction Approval Group"),
    Discount_Approval_Committee ("Discount Approval Group"),
    DirectTransfer_Approval_Committee ("Direct Transfer Approval Group"),
    DirectTransfer_Notification_Committee ("Direct Transfer Notification Group"),
    DispenseReturn_Approval_Committee ("Return Approval Group"),
    Audit_Approval_Committee ("Audit Approval Group"),
    Adjustment_Level_One_Approval_Committee ("Adjustment Approval Group Level One"),
    Adjustment_Level_Two_Approval_Committee ("Adjustment Approval Group Level Two"),
    Prescription_Audit_Request_Approval_Committee("Prescription Audit Request Approval Committee");

    private String displayName;

    Context(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
