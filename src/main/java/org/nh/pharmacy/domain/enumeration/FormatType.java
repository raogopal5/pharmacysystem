package org.nh.pharmacy.domain.enumeration;

/**
 * The FormatType enumeration.
 */
public enum FormatType {

    STOCK_BARCODE("STOCK_BARCODE","STOCK BARCODE");

    private final String formatType;
    private final String formatTypeDisplay;

    FormatType(String formatType, String formatTypeDisplay){
        this.formatType = formatType;
        this.formatTypeDisplay = formatTypeDisplay;
    }

    public String getFormatType(){ return formatType;}
    public String getFormatTypeDisplay(){return formatTypeDisplay;}

    public static FormatType findByFormatType(String formatType){
        FormatType result = null;
        for(FormatType val : FormatType.values()){
            if(formatType.equals(val.getFormatType())){
                result = val;
                break;
            }
        }
        return result;
    }
}
