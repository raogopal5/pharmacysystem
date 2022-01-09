package org.nh.pharmacy.util;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FreeMarkerUtil {

    public static String decimalFormater(Object amount) {
        String returnString="0.00";
        DecimalFormat df = new DecimalFormat("0.00");
        if (amount != null) {
            if (amount instanceof ArrayList) {
                ArrayList amountList = (ArrayList) amount;
                for (int i = 0; i < amountList.size(); i++) {
                    returnString=returnString+ df.format(amountList.get(i));
                }
            } else {
                returnString=df.format(amount);
            }

        }
        return returnString;

    }
}
