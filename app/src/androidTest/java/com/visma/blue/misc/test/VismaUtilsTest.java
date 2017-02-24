package com.visma.blue.misc.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.visma.blue.misc.VismaUtils;

public class VismaUtilsTest extends AndroidTestCase {

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final String filenamePrefix = "test.";
        MockContentResolver resolver = new MockContentResolver();

        RenamingDelegatingContext targetContextWrapper =
                new RenamingDelegatingContext(new DelegatedMockContext(getContext()), filenamePrefix);
        Context context = new IsolatedContext(resolver, targetContextWrapper);
        mContext = context;
    }

    @SmallTest
    public void testSetAndGet_token() {
        final String testToken = "token" + System.currentTimeMillis();
        VismaUtils.setToken(mContext, testToken);
        assertTrue("The token was not saved/restored properly.",
                VismaUtils.getToken().equals(testToken));
    }

    @SmallTest
    public void testSetAndGet_demoMode() {
        VismaUtils.setDemoMode(mContext, true);
        assertTrue("The demo mode flag was not saved/restored properly.",
                VismaUtils.isDemoMode(mContext));

        VismaUtils.setDemoMode(mContext, false);
        assertTrue("The demo mode flag was not saved/restored properly.",
                !VismaUtils.isDemoMode(mContext));
    }

    @SmallTest
    public void testSetAndGet_syncMode() {
        VismaUtils.setSyncMode(mContext, true);
        assertTrue("The sync mode was not saved/restored properly.",
                VismaUtils.isSyncMode(mContext));

        VismaUtils.setSyncMode(mContext, false);
        assertTrue("The sync mode was not saved/restored properly.",
                !VismaUtils.isSyncMode(mContext));
    }

    @SmallTest
    public void testSetAndGet_email() {
        final String email = "test" + System.currentTimeMillis() +  "@test.com";
        VismaUtils.setCurrentEmail(mContext, email);
        assertTrue("The email was not saved/restored properly.",
                VismaUtils.getCurrentEmail(mContext).equals(email));
    }

    @SmallTest
    public void testSetAndGet_company() {
        final String value = "Company" + System.currentTimeMillis();
        VismaUtils.setCurrentCompany(mContext, value);
        assertTrue("The company name was not saved/restored properly.",
                VismaUtils.getCurrentCompany(mContext).equals(value));
    }

    @SmallTest
    public void testSetAndGet_countryCode() {
        final String value = "se";
        VismaUtils.setCurrentCompanyCountryCodeAlpha2(mContext, value);
        assertTrue("The user id was not saved/restored properly.",
                VismaUtils.getCurrentCompanyCountryCodeAlpha2().equals(value));

        final String value2 = "no";
        VismaUtils.setCurrentCompanyCountryCodeAlpha2(mContext, value2);
        assertTrue("The user id was not saved/restored properly.",
                VismaUtils.getCurrentCompanyCountryCodeAlpha2().equals(value2));
    }

    private static class DelegatedMockContext extends MockContext {

        private Context mDelegatedContext;

        public DelegatedMockContext(Context context) {
            mDelegatedContext = context;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return mDelegatedContext.getSharedPreferences("test." + name, mode);
        }
    }
}
