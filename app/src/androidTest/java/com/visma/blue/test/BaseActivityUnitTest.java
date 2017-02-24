package com.visma.blue.test;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;

import com.visma.blue.R;

public abstract class BaseActivityUnitTest<T extends Activity> extends ActivityUnitTestCase<T> {

    private final Class<T> mTestActivityClass;
    private Activity mActivity;

    public BaseActivityUnitTest(Class<T> clazz) {
        super(clazz);

        mTestActivityClass = clazz;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // The test runs in an isolated context, and seems to not get its theme from the Manifest.
        //ContextThemeWrapper context = new ContextThemeWrapper(getInstrumentation().getTargetContext(), R.style.NordicCoolMaterialTheme);
        //setActivityContext(context);
        //mLaunchIntent = new Intent(getInstrumentation().getTargetContext(), LaunchActivity.class);
        //startActivity(mLaunchIntent, null, null);

        // This is not how it is supposed to be done, but the correct way does not seem to work with
        // the AppCompat library at the moment.

        mActivity = launchActivity(getThemedContext().getPackageName(), mTestActivityClass, null);
        getInstrumentation().waitForIdleSync();
    }


    protected Context getThemedContext() {
        Context context = getInstrumentation().getTargetContext();
        context.setTheme(R.style.NordicCoolMaterialTheme);

        return context;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (mActivity != null)
            mActivity.finish();
    }

    @UiThreadTest
    public void testLifecycle() throws Exception {
        // The documentation says:
        // Do not call from your setUp() method. You must call this method from each of your test methods.
        /*
        This is how it should be done, but for some reason it does not work with the AppCompat library
        startActivity(new Intent(getInstrumentation().getTargetContext(), mTestActivityClass), null, null);
        ActionBarActivity activity = getActivity();
        */

        assertNotNull(mActivity);

        // It looks as if we need to run these on the main thread because the fragment manager
        // needs to be run that way.
        getInstrumentation().callActivityOnStart(mActivity);
        getInstrumentation().callActivityOnResume(mActivity);

        TestUtils.runThroughLongLifecycle(getInstrumentation(), mActivity);
        TestUtils.runThroughShortLifecycle(getInstrumentation(), mActivity);
    }
}

