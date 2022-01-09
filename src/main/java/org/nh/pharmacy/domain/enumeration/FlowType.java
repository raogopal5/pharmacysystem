package org.nh.pharmacy.domain.enumeration;

/**
 * The FlowType enumeration.
 */
public enum FlowType {
    StockIn("StockIn", "Stock In"),
    StockOut("StockOut", "Stock Out");

    private final String flowType;
    private final String flowTypeDisplay;

    FlowType(String flowType, String flowTypeDisplay) {
        this.flowType = flowType;
        this.flowTypeDisplay = flowTypeDisplay;
    }

    public String getFlowType() {
        return flowType;
    }

    public String getFlowTypeDisplay() {
        return flowTypeDisplay;
    }

    public static FlowType findByFlowType(String flowType) {
        FlowType result = null;
        for (FlowType val : FlowType.values()) {
            if (flowType.equals(val.getFlowType())) {
                result = val;
                break;
            }
        }
        return result;
    }

}
