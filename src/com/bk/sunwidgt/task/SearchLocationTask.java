
package com.bk.sunwidgt.task;

import java.io.IOException;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

public class SearchLocationTask extends AsyncTask<Void, Void, Address> {
    private final static String TAG = "Sun" + SearchLocationTask.class.getSimpleName();
    final Geocoder m_gencorder;
    final String m_locationName;

    public SearchLocationTask(String locationName, Geocoder gencorder) {
        m_gencorder = gencorder;
        m_locationName = locationName;
    }

    @Override
    protected Address doInBackground(Void... names) {

            try {
                List<Address> listAddress = m_gencorder.getFromLocationName(m_locationName, 1);
                if (listAddress != null && listAddress.size() > 0) {
                    final Address address = listAddress.get(0);
                    Log.i(TAG, "address=" + address);
                    
                    return address;
                }
            } catch (IOException e) {
                Log.e(TAG, "getFromLocation error", e);
            }
            finally {
    
   
        }
        
        return null;
    }
}
