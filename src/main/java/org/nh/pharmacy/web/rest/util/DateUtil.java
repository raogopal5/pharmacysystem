package org.nh.pharmacy.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for date format.
 */
public final class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    private DateUtil(){
    }

    public static String getDateFormatForExportCSVFileName(Date date){
        SimpleDateFormat stf= new SimpleDateFormat("yyyy-MM-dd'_at_'HHmmSS_a");
        return stf.format(date);
    }

    public static LocalDate getLocalDateFromStringDate(String date){
        DateTimeFormatter stf= DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, stf);
    }

    public static String getFormattedDateAsFunctionForCSVExport(LocalDateTime date, String dateFormat){
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.set(Calendar.YEAR, date.getYear());
        calendarDate.set(Calendar.MONTH, date.getMonthValue()-1);
        calendarDate.set(Calendar.DATE, date.getDayOfMonth());
        calendarDate.set(Calendar.HOUR_OF_DAY, date.getHour());
        calendarDate.set(Calendar.MINUTE, date.getMinute());
        calendarDate.set(Calendar.SECOND, date.getSecond());
        return getFormattedDateAsFunctionForCSVExport(calendarDate.getTime(), dateFormat);
    }

    public static String getFormattedDateAsFunctionForCSVExport(Date date, String dateFormat){
        log.debug("Request to DateUtil for converting date format");
        try{
            String newDateFormat = new SimpleDateFormat(dateFormat+" HH:mm").format(date);
            return new StringBuffer("=\"").append(newDateFormat).append("\"").toString();
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return "";
    }

/*    public static void main(String[] args) {
        System.out.println(getFormattedDateAsFunctionForCSVExport(LocalDateTime.now(),"dd/MM/yy"));
    }*/
}
