
package com.bk.sunwidgt.activity;

import com.bk.sunwidgt.adapter.SunFragmentAdapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class SunActivity extends Activity {
    private final static String TAG = SunActivity.class.getSimpleName();
    private OptionMenuCreator m_menuCreator;
    private ViewPager m_viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(com.bk.sunwidgt.R.layout.sun_activity);
        
        m_viewPager = (ViewPager) findViewById(com.bk.sunwidgt.R.id.sun_time_pager);
        m_viewPager.setAdapter(new SunFragmentAdapter(getFragmentManager()));
        m_viewPager.setCurrentItem(SunFragmentAdapter.MONTH_RANGE);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        if (null == m_menuCreator) {
            m_menuCreator = new OptionMenuCreator();
        }
        return m_menuCreator.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null == m_menuCreator) {
            m_menuCreator = new OptionMenuCreator();
        }
        return m_menuCreator.onOptionsItemSelected(this, item);
    }

    @Override
    protected void onStart() {
        // mLogger.logMethodName();
        super.onStart();
    }

    @Override
    protected void onResume() {
        // mLogger.logMethodName();
        super.onResume();
        // setResult(RESULT_OK);
    }

    @Override
    protected void onPause() {
        // mLogger.logMethodName();
        super.onPause();
    }

    @Override
    protected void onStop() {
        // mLogger.logMethodName();
        super.onStop();
        // setResult(RESULT_OK);
        // finish();
        // mLogger.info("Finish activity by onStop");
    }

    @Override
    protected void onDestroy() {
        // mLogger.logMethodName();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
