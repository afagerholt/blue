package com.visma.blue.login;

public class LoginData {
    protected static int getAppIdFromServiceId(int serviceId) {
        switch (serviceId) {
            case 0: // Mobil Scanner
                return 1110;
            case 1: //Mamut
                return 1310;
            case 2: //Expense
                return 1410;
            case 3: //AccountView
                return 1510;
            case 4: //Netvisor
                return 1710;
            case 5: //Severa
                return 1810;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }
}
