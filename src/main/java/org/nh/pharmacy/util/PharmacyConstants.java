package org.nh.pharmacy.util;

public class PharmacyConstants {

    public static final String IP_DISPENSE="ipDispense";
    public static final String IP_DISPENSE_RETURN="ipDispenseReturn";
    public static final String IP_DISPENSE_RETURN_REQUEST="ipPharmacyReturnRequest";
    public static final String ACCEPT="ACCEPT";
    public static final String REJECT="REJECT";
    public static final String DISPENSED_PAGE="DISPENSED_EXPORT";
    public static final String CANCELLED_PAGE="CANCELLED_EXPORT";
    public static final String DOCUMENT ="document";
    public static final String CREATED_DATE ="createdDate";
    public static final String CREATED_BY ="createdBy";
    public static final String ERR_CODE_10186="10186";
    public static final String ERR_CODE_10161="10161";
    public static final Integer MAX_STATUS_COUNT = 100;

    public static final String[] IP_DISPENSE_RETURN_REQUEST_HEADER={"Return Request No","Requested Date & Time","MRN","Patient Name","Gender","Age",
                                    "Patient Location (Ward Name/Bed No)","Patient Status","Requested By","Requesting HSC","Return Store","Rejected By",
                                    "Rejected Date","Status"};
    public static final String[] IP_DISPENSE_RETURN_HEADER={"Return No.","Return Date (& Time)","MRN", "Patient Name", "Gender", "Age","Return By","Return Store","Patient Location (Ward Name/Bed No)","Return Request No","Requested Date (& Time)","Requested By","Requesting HSC"};

    public static final String[] IP_DISPENSE_HEADER={"Order No","Order Date","Dispense No", "Dispense Date", "MRN","Patient Name",
        "Visit No", "Entered By", "Ordering HSC","Dispensed By","Dispensing HSC", "Sponsor","Consultant","Total Items"};

    public static final String[] MEDICATION_REQUEST_HEADER = {"Priority","Order No","Order Date & Time", "MRN","Patient Name","Gender","Age","Visit No","Patient Status","Ordering HSC","Entered By",
        "Dispensing HSC", "Item Count","Plan","Consultant","Status"};

    public static final String[] PRESCRIPTION_AUDIT_REQUEST_HEADER = {"Pharmacy Order No", "MRN","Patient Name","Gender","Age","Order Date","Consultant","Department","Entered By",
        "Audited By", "Audit Date","Modified","Status"};






}
