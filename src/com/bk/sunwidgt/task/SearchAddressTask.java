package com.bk.sunwidgt.task;

import java.io.IOException;
import java.util.List;

import com.bk.sunwidgt.activity.SunMapActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class SearchAddressTask extends AsyncTask<Void, Void, Address>{
    private final static String TAG = "Sun" + SearchAddressTask.class.getSimpleName();
    final Handler m_handler;
    final Geocoder m_gencorder;
    final double m_lat;
    final double m_lng;
    public SearchAddressTask(Handler handler,Geocoder gencorder,double lat,double lng) {
        m_handler = handler;
        m_gencorder = gencorder;
        m_lat = lat;
        m_lng = lng;
    }
    @Override
    protected Address doInBackground(Void... arg0) {
        
        //Show progress dialog
        if(m_handler != null) {
            m_handler.obtainMessage(SunMapActivity.MESSAGE_SHOW_PROGRESS).sendToTarget();
        }
        try {
            List<Address> listAddress = m_gencorder.getFromLocation(m_lat, m_lng, 1);
            if (listAddress != null && listAddress.size() > 0) {
                final Address address = listAddress.get(0);
                Log.i(TAG, "address=" + address);
                
                //Show address
                if(m_handler != null) {
                    m_handler.obtainMessage(SunMapActivity.MESSAGE_SET_ADDRESS,address).sendToTarget();
                }
                return address;
            }
        } catch (IOException e) {
            Log.e(TAG, "getFromLocation error", e);
        }
        finally {
            //Show bookmark dialog
            if(m_handler != null) {
                m_handler.obtainMessage(SunMapActivity.MESSAGE_SHOW_BOOKMARK).sendToTarget();
            }
        }
        
        
        return null;
    }

}
