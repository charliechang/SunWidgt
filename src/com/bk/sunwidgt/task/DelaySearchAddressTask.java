
package com.bk.sunwidgt.task;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bk.sunwidgt.activity.SunMapActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.util.Log;

@Deprecated
public class DelaySearchAddressTask extends TimerTask {
    private final static String TAG = "Sun" + DelaySearchAddressTask.class.getSimpleName();
    private final static Executor SINGLE_THEEAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private final SearchAddressTask mSearchTask;
    private final long m_timeoutInMS;
    private final Handler m_handler;

    public DelaySearchAddressTask(Handler handler, long timeoutInMS, Geocoder gencorder,
            double lat, double lng) {
        super();

        mSearchTask = new SearchAddressTask(handler,gencorder, lat, lng);
        m_timeoutInMS = timeoutInMS;
        m_handler = handler;
    }

    public void run() {
        Log.d(TAG, "Running " + this);
        mSearchTask.executeOnExecutor(SINGLE_THEEAD_EXECUTOR);
        
        //m_handler.obtainMessage(SunMapActivity.MESSAGE_QUERY_ADDRESS).sendToTarget();
        
        Address address = null;
        try {
            address = mSearchTask.get(m_timeoutInMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "", e);
        } catch (TimeoutException e) {
            Log.e(TAG, "", e);
        }

        m_handler.obtainMessage(SunMapActivity.MESSAGE_SET_ADDRESS, address).sendToTarget();

        Log.d(TAG, "Done " + this);
    }

}
