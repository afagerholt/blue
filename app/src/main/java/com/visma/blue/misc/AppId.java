package com.visma.blue.misc;

public enum AppId {
    UNKNOWN(0, "Unknown"),
    VISMA_ONLINE(1110, "Online"),
    EACCOUNTING(1210, "eAccounting"),
    MAMUT(1310, "Mamut"),
    EXPENSE_MANAGER(1410, "Expense"),
    ACCOUNTVIEW(1510, "AccountView"),
    NETVISOR(1710, "Netvisor"),
    SEVERA(1810, "Severa");

    private final int id;
    private final String name;

    AppId(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getValue() {
        return id;
    }

    public String getName() {
        return name;
    }
}