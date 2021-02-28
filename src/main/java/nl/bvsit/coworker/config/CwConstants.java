package nl.bvsit.coworker.config;

/**
 * Application constants.
 */
public final class CwConstants {
    private CwConstants() {}

    public static final int NUM_ORDERS_PER_SESSION = 3;
    public static final int ORDER_INTERVAL_IN_MINUTES = 60;

    //debug
    public static final boolean LOAD_MINIMAL_TEST_DATA=true;
    public static final boolean CREATE_TEST_USERS=true;
    public static final boolean AUTHENTICATION_PERMIT_ALL=false;

    //Exception messages
    public static final String RESOURCE_NOT_FOUND="Resource not found";
    public static final String RESOURCE_NOT_FOUND_WITH_ID = RESOURCE_NOT_FOUND+" with id: %d";
    public static final String MENU_ITEM_NOT_FOUND = "Menu item not found" ;
    public static final String MENU_ITEM_NOT_FOUND_WITH_ID = "Menu item not found with id: %d" ;

}
