package org.nh.pharmacy.config;

/**
 * Application constants.
 */
public final class Constants {

    //Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String ORDERS = "orders";
    public static final String ORDERS_QUANTITY = "orders_quantity";
    public static final String PHR_KAFKA_TASK_THREAD_POOL = "phr-kafka-pool";
    public static final String USER_CACHE_NAME ="users";
    public static final String USER_ID = "user-id:";
    public static final String USER_LOGIN = "user-login.raw:";
    public static final String PHR_ENTITY ="PHR-Entity-";
    public static final String APPROVED ="Approved";
    public static final String REJECTED ="Rejected";
    public static final String SEND_FOR_PA_APPROVAL ="Send For PA Approval";
    public static final String SEND_FOR_APPROVAL ="Send For Approval";

    private Constants() {
    }
}
