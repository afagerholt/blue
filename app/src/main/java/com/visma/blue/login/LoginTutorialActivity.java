package com.visma.blue.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.visma.blue.R;

import java.lang.reflect.Field;

public class LoginTutorialActivity extends BaseLoginActivity {
    private Handler mHandler;
    private Runnable mPageTurnRunnable;
    private final int mPageTurnPeriod = 3000;

    private TutorialFragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_login_tutorial);

        mHandler = new Handler();
        mPageTurnRunnable = new Runnable() {
            public void run() {
                final int currentPage = mPager.getCurrentItem();
                final int newPage = (currentPage + 1) % mAdapter.getCount();

                if (newPage == 0) {
                    mPager.setCurrentItem(newPage);
                } else {
                    mPager.setCurrentItem(newPage, true);
                }
            }
        };

        mAdapter = new TutorialFragmentAdapter(getFragmentManager());

        mPager = (ViewPager) findViewById(R.id.blue_activity_tutorial_pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) findViewById(R.id.blue_activity_tutorial_indicator);
        mIndicator.setViewPager(mPager);

        Button buttonLogin = (Button) findViewById(R.id.blue_activity_tutorial_button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        // Replace the default scroller with one that scrolls a little slower.
        try {
            Field scroller;
            scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            FixedSpeedScroller fixedSpeedScroller = new FixedSpeedScroller(mPager.getContext());
            scroller.set(mPager, fixedSpeedScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mHandler.postDelayed(mPageTurnRunnable, mPageTurnPeriod);
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    if (mHandler != null) {
                        mHandler.removeCallbacks(mPageTurnRunnable);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mHandler != null) {
            mHandler.removeCallbacks(mPageTurnRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.postDelayed(mPageTurnRunnable, mPageTurnPeriod);
    }
}
