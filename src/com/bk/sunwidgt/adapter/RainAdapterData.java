package com.bk.sunwidgt.adapter;

import java.util.logging.Logger;

import android.location.Location;
import android.util.Log;

public class RainAdapterData {
    protected final static int MAX_HOURS = 24;
    protected final String m_locationName;
    protected final String m_observerName;
    protected final int m_year;
    protected final int m_month;
    protected final int m_day;
    protected final int m_start_hour;
    protected final Location m_location;
    
    protected final float[] m_dayRain = new float[MAX_HOURS];
    
    public RainAdapterData(String locationName,String observerName,Location location,int year,int month,int day,int start_hour) {
        m_locationName = locationName;
        m_observerName = observerName;
        m_year = year;
        m_month = month;
        m_day = day;
        m_start_hour = start_hour % MAX_HOURS;
        m_location = new Location(location);
    }
    
    public Location getLocation() {
        return m_location;
    }
    
    public String getLocationName() {
        return m_locationName;
    }
    
    public String getObserverName() {
        return m_observerName;
    }
    
    public void setHour(int hour,float measure) {
        m_dayRain[hour % MAX_HOURS] = measure;
    }
    
    public float sumRainHour(int hours) {
        int offset = m_start_hour; 
        float sum = 0.0f;
      
        hours %= MAX_HOURS;

        while(hours-- > 0) {
            sum += m_dayRain[offset];

            offset = (offset + MAX_HOURS - 1) % MAX_HOURS;
        }

        return sum;
    }
    
    
}
