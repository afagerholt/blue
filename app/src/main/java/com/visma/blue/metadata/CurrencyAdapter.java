package com.visma.blue.metadata;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;

import java.util.Locale;

public class CurrencyAdapter extends ArrayAdapter<CharSequence> {

    private static final String[] mSweden = {"SEK", "NOK", "DKK", "EUR", "USD",};
    private static final String[] mNorway = {"NOK", "SEK", "DKK", "EUR", "USD",};
    private static final String[] mDenmark = {"DKK", "SEK", "NOK", "EUR", "USD",};
    private static final String[] mOther = {"EUR", "SEK", "NOK", "DKK", "USD",};

    public CurrencyAdapter(Context context) {
        super(context, R.layout.blue_spinner_item);

        // Add the empty element
        this.add("");

        // Add country specific order of currencies to the beginning of the array
        String countryCodeAlpha2 = VismaUtils.getCurrentCompanyCountryCodeAlpha2();
        if (TextUtils.isEmpty(countryCodeAlpha2)) {
            countryCodeAlpha2 = Locale.getDefault().getCountry();
        }

        if (countryCodeAlpha2.equalsIgnoreCase("se")) {
            this.addAll(mSweden);
        } else if (countryCodeAlpha2.equalsIgnoreCase("no")) {
            this.addAll(mNorway);
        } else if (countryCodeAlpha2.equalsIgnoreCase("dk")) {
            this.addAll(mDenmark);
        } else {
            this.addAll(mOther);
        }

        // Add all other currencies
        this.addAll(
                "AED", "AFN",
                "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM",
                "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL",
                "BSD", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF", "CHF", "CLP",
                "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK",
                "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP",
                "GEL", "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD",
                "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "IMP", "INR", "IQD",
                "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR",
                "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR",
                "LRD", "LSL", "LTL", "LVL", "LYD", "MAD", "MDL", "MGA", "MKD",
                "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN", "MYR",
                "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB",
                "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD",
                "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP",
                "SLL", "SOS", "SPL", "SRD", "STD", "SVC", "SYP", "SZL", "THB",
                "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TVD", "TWD", "TZS",
                "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST",
                "XAF", "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMW", "ZWD");

        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}