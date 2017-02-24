package com.visma.blue.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.visma.blue.R;
import com.visma.blue.BlueMainActivity;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetCompaniesAnswer;
import com.visma.blue.network.test.MobileScannerCommunicatorTest;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SimpleLoginTest {

    private static final String mPreferenceFile = "MobileScanner";
    private static final VolleySingleton.ServerType serverType = VolleySingleton.ServerType.ALPHA;

    @Rule
    public ActivityTestRule<BlueMainActivity> mActivityRule =
            new ActivityTestRule<>(BlueMainActivity.class);


    @BeforeClass
    public static void ClassSetUp() {
        // Clear the settings so that we get to the login form the next time we run the test

        Context targetContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences settings = targetContext
                .getSharedPreferences(mPreferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        VismaUtils.init(targetContext);

        // Setup volley so that it is configured and ready when we want to use it later on.
        VolleySingleton.getInstance().setAndSaveServer(targetContext, VolleySingleton.getInstance
                ().getServerData(VolleySingleton.ServerType.ALPHA));
    }

    @Before
    @After
    public void setUp() {
        // Clear the settings so that we get to the login form the next time we run the test

        Context targetContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences settings = targetContext
                .getSharedPreferences(mPreferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        // Setup volley so that it is configured and ready when we want to use it later on.
        VolleySingleton.getInstance().setAndSaveServer(targetContext, VolleySingleton.getInstance
                ().getServerData(VolleySingleton.ServerType.ALPHA));
    }

    @Test
    public void verifyThatSomethingWorks() {
        onView(withId(R.id.blue_activity_tutorial_button_login)).check(matches(isDisplayed()));
        onView(withId(R.id.blue_activity_tutorial_button_login)).perform(click());

        String testIntegrationName = InstrumentationRegistry.getTargetContext().getString(R
                .string.visma_blue_integration_online_title);

        onData(withIntegrationName(testIntegrationName))
                .inAdapterView(withId(R.id.blue_integrations_list)).atPosition(0)
                .perform(click());

        onView(withId(R.id.blue_fragment_login_user)).perform(typeText
                (MobileScannerCommunicatorTest.getUser()));
        onView(withId(R.id.blue_fragment_login_password)).perform(typeText
                (MobileScannerCommunicatorTest.getPassword()));
        onView(withId(R.id.blue_fragment_login_button_login)).perform(click());

        //TODO: This is not the way to do it, test should not have conditions inside them like this
        //Some users have 1 company and some users have multiple companies and the logic is different for them
        try {
            onData(is(instanceOf(GetCompaniesAnswer.OnlineCustomer.class)))
                    .atPosition(0)
                    .perform(click());
        }catch (NoMatchingViewException e){
            //View not displayed
        }
    }

    public static Matcher<Object> withIntegrationName(final String integrationName){
        return new BoundedMatcher<Object, IntegrationData>(IntegrationData.class) {
            @Override
            public boolean matchesSafely(IntegrationData info) {
                return info.getName().matches(integrationName);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with name: ");
            }
        };
    }

    private void waitTimeout(int timeout) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
