package com.visma.blue.network;

public enum OnlinePhotoType {

    UNKNOWN(-1),
    INVOICE(0),
    RECEIPT(1),
    DOCUMENT(2);

    private final int type;

    OnlinePhotoType(int type) {
        this.type = type;
    }

    public int getValue() {
        return type;
    }
}