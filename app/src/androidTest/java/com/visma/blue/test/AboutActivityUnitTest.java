package com.visma.blue.test;

import com.visma.blue.about.AboutActivity;

import org.junit.Ignore;

@Ignore("There is a problem with data binding and instrumentation tests on android.")
public class AboutActivityUnitTest extends BaseActivityUnitTest<AboutActivity> {
    public AboutActivityUnitTest() {
        super(AboutActivity.class);
    }
}
