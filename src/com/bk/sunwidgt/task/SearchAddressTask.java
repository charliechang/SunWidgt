package com.bk.sunwidgt.task;

import java.io.IOException;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

public class SearchAddressTask extends AsyncTask<Void, Void, Address>{
    private final static String TAG = "Sun" + SearchAddressTask.class.getSimpleName();
    final Geocoder m_gencorder;
    final double m_lat;
    final double m_lng;
    public SearchAddressTask(Geocoder gencorder,double lat,double lng) {
        m_gencorder = gencorder;
        m_lat = lat;
        m_lng = lng;
    }
    @Override
    protected Address doInBackground(Void... arg0) {
        try {
            List<Address> listAddress = m_gencorder.getFromLocation(m_lat, m_lng, 1);
            if (listAddress != null && listAddress.size() > 0) {
                final Address address = listAddress.get(0);
                Log.i(TAG, "address=" + address);
                return address;
            }
        } catch (IOException e) {
            Log.e(TAG, "getFromLocation error", e);
        }

        return null;
    }
}
