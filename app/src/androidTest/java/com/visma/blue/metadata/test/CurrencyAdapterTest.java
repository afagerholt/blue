package com.visma.blue.metadata.test;

import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.visma.blue.metadata.CurrencyAdapter;
import com.visma.blue.misc.VismaUtils;

public class CurrencyAdapterTest extends AndroidTestCase {
    private static final String[] mCountryCodes = {"se", "no", "dk", "other"};
    private static final String[] mCurrencies = {"SEK", "NOK", "DKK", "EUR"};

    public void testCurrencyOrder() {
        for (int i=0; i<mCountryCodes.length; i++) {
            VismaUtils.setCurrentCompanyCountryCodeAlpha2(getContext(), mCountryCodes[i]);
            CurrencyAdapter adapter = new CurrencyAdapter(getContext());
            CharSequence firstItem = adapter.getItem(0);
            CharSequence secondItem = adapter.getItem(1);
            assertTrue("The first currency should be empty.", TextUtils.isEmpty(firstItem));
            assertTrue("The second currency is not correct.", mCurrencies[i].equals(secondItem.toString()));
        }
    }

    public void testNumberOfCurrencies() {
        for (String countryCode : mCountryCodes) {
            VismaUtils.setCurrentCompanyCountryCodeAlpha2(getContext(), countryCode);
            CurrencyAdapter adapter = new CurrencyAdapter(getContext());
            assertEquals("The number of currency items are not correct.", 170, adapter.getCount());
        }
    }
}
