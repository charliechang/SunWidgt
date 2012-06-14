package com.bk.sunwidgt.adapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.location.Location;

public class TideAdapterData {
    
    public final Location m_location;
    public final String m_locationName;

    private final Map<String,Map<Long,Integer>> m_dateToHeightMap = new HashMap<String,Map<Long,Integer>>();
    
    public TideAdapterData(String locationName,Location location) {
        m_location = new Location(location);
        m_locationName = locationName;
    }
    
    public void addHeight(String dateString,long time,int height) {

        Map<Long,Integer> timeToHeightMap = null;
        if(!m_dateToHeightMap.containsKey(dateString)) {
            timeToHeightMap = new HashMap<Long,Integer>();
            m_dateToHeightMap.put(dateString, timeToHeightMap);
        }
        else {
            timeToHeightMap = m_dateToHeightMap.get(dateString);
        }
        
        timeToHeightMap.put(time, height);
    }
    
    public Map<Long,Integer> getHeights(Date date) {
        final String key = TideStoreUtil.fmtDate.format(date);
        return m_dateToHeightMap.get(key);
    }
    
    
}
