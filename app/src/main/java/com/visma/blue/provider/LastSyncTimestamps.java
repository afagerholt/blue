package com.visma.blue.provider;

public class LastSyncTimestamps implements BaseColumns {

    public static final String TYPE = "typeOfSync";
    // The timestamp to use in the next get operation
    public static final String TIMESTAMP = "timestamp";
    // The page number to use in the next get operation
    public static final String PAGE = "page";

    public interface Type {
        int EXPENSE_CUSTOM_DATA = 1;
        int SEVERA_CUSTOM_DATA = 2;
        int NETVISOR_CUSTOM_DATA = 3;
    }
}
