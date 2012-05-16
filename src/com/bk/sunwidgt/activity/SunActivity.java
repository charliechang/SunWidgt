
package com.bk.sunwidgt.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class SunActivity extends Activity {
    private final static String TAG = SunActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(com.bk.sunwidgt.R.layout.sun_activity);
        /*
         * new Handler().post(new Runnable() {
         * @Override public void run() { FragmentManager fm =
         * getFragmentManager(); fm.beginTransaction().add(new
         * CalendarSlideFragment(), null).commitAllowingStateLoss();
         * fm.executePendingTransactions(); }});
         */
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
