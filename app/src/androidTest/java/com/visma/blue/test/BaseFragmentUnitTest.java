package com.visma.blue.test;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.test.suitebuilder.annotation.LargeTest;

import com.visma.blue.R;
import com.visma.blue.TestFragmentActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;

/**
 * This test is not finished, it is a proof of concept/learning test at the moment.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class BaseFragmentUnitTest<T extends Fragment> extends BlueActivityUnitTestCase<TestFragmentActivity> {

    private final Class<T> mTestFragmentClass;

    public BaseFragmentUnitTest(Class<T> clazz) {
        super(TestFragmentActivity.class);

        mTestFragmentClass = clazz;
    }

    @Before
    public void setUp() throws Exception {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected Context getThemedContext() {
        Context context = getInstrumentation().getTargetContext();
        context.setTheme(R.style.NordicCoolMaterialTheme);

        return context;
    }

    @Test
    public void testFragment() throws Exception {
        //It looks as if we need to create fragments on the main thread when using junit4
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getThemedContext(), TestFragmentActivity.class), null, null);
                FragmentActivity activity = getActivity();
                Fragment fragment = null;

                try {
                    fragment = mTestFragmentClass.getConstructor().newInstance();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                assertNotNull("Could not instantiate fragment of type: " + mTestFragmentClass.getSimpleName(), fragment);

                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();
                fragmentManager.executePendingTransactions();

                Fragment foundFragment = fragmentManager.findFragmentById(android.R.id.content);
                assertNotNull("Could not find the fragment: " + mTestFragmentClass.getSimpleName(),
                        foundFragment);
                assertTrue("Found fragment is not of correct type. Found "
                                + foundFragment.getClass().getSimpleName()
                                + " expected: " + mTestFragmentClass.getSimpleName() + ".",
                        foundFragment.getClass().isAssignableFrom(mTestFragmentClass));

                TestUtils.runThroughLongLifecycle(getInstrumentation(), activity);
                TestUtils.runThroughShortLifecycle(getInstrumentation(), activity);
            }
        });
    }
}
