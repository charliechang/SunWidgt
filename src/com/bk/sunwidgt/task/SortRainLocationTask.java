package com.bk.sunwidgt.task;

import java.util.ArrayList;
import java.util.List;

import com.bk.sunwidgt.adapter.RainAdapterData;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;

public class SortRainLocationTask extends AsyncTask<Void, Void, RainAdapterData[]>{
    private final static String TAG = "Sun" + SortRainLocationTask.class.getSimpleName();
    
    private final RainAdapterData[] m_rainDataList;
    private final Handler m_handler;
    private final double m_lat;
    private final double m_lng;
    
    public SortRainLocationTask(RainAdapterData[] rainDataList,Handler handler,double lat,double lng) {
        super();
        
        m_rainDataList = rainDataList;
        m_handler = handler;
        m_lat = lat;
        m_lng = lng;
    }
    
    @Override
    protected RainAdapterData[] doInBackground(Void... arg0) {
        double minDist = Double.MAX_VALUE;
        final List<RainAdapterData> minDistList = new ArrayList<RainAdapterData>(5);
        
        for(RainAdapterData rainData : m_rainDataList) {
            final Location location = rainData.getLocation();
            final double latDiff = location.getLatitude() - m_lat;
            final double lngDiff = location.getLongitude() - m_lng;
            
            final double dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
            
            if(dist < minDist) {
                minDistList.clear();
                minDistList.add(rainData);
                minDist = dist;
            }
            else if(dist == minDist) {
                minDistList.add(rainData);
            }
            
        }
        
        
        
        return minDistList.toArray(new RainAdapterData[0]);
    }

    

}
