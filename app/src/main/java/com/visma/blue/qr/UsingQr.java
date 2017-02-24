package com.visma.blue.qr;

import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.OnlineMetaData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsingQr {

    public UsingQr() {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private static final Pattern mPattern = Pattern.compile("[\\d]{8}");

    @SuppressWarnings("unused")
    private int uqr;
    public int tp;
    private String nme;
    @SuppressWarnings("unused")
    private String cc;
    private String cid;
    private String iref;
    @SuppressWarnings("unused")
    private String cr;
    public String idt;
    private String ddt;
    private Double due;
    private String cur;
    private Double vat;
    private Double vh;
    private Double vm;
    private Double vl;
    @SuppressWarnings("unused")
    private String pt;
    @SuppressWarnings("unused")
    private String acc;
    @SuppressWarnings("unused")
    private String bc;
    @SuppressWarnings("unused")
    private String adr;

    public Date parseAndGetDateLong(String input) {
        if (input == null) {
            return null;
        }

        Matcher matcher = mPattern.matcher(input);
        if (matcher.find()) {
            try {
                return mDateFormat.parse(input);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void updateFieldsIn(OnlineMetaData onlineMetaData) {
        onlineMetaData.name = this.nme;
        onlineMetaData.organisationNumber = this.cid;
        onlineMetaData.referenceNumber = this.iref;
        onlineMetaData.dueAmount = this.due;
        onlineMetaData.highVatAmount = this.vh;
        onlineMetaData.middleVatAmount = this.vm;
        onlineMetaData.lowVatAmount = this.vl;
        onlineMetaData.totalVatAmount = this.vat;
        onlineMetaData.currency = this.cur;
        onlineMetaData.invoiceDate = this.parseAndGetDateLong(this.idt);
        onlineMetaData.dueDate = this.parseAndGetDateLong(this.ddt);

        switch (this.tp) {
            case 1:
                onlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
                onlineMetaData.isPaid = false;
                onlineMetaData.paymentDate = null;
                break;
            case 2:
                onlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
                onlineMetaData.isPaid = false;
                onlineMetaData.paymentDate = null;
                break;
            case 3:
                onlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
                onlineMetaData.isPaid = true;
                onlineMetaData.paymentDate = this.parseAndGetDateLong(this.idt);
                break;
            default:
                break;
        }
    }
}

