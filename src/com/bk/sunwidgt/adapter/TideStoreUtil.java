package com.bk.sunwidgt.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bk.sunwidgt.task.SearchLocationTask;
import com.bk.sunwidgt.task.TideInformation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class TideStoreUtil {
    public final static SimpleDateFormat fmtDate = new SimpleDateFormat("yyyyMMdd");
    public final static SimpleDateFormat fmtTime = new SimpleDateFormat("HHmm");
    private final static String TAG = "Sun" + TideStoreUtil.class.getSimpleName();
    private final static String SEPERATOR = "\t";
    private final static String TIDE_LOCATION_PREFIX = TideStoreUtil.class.getName() + ".location.";
    private final static String TIDE_DATES = "tide_dates";
    private final static String TIDE_TIMES_PREFIX = "tide_times";
    private final static String TIDE_UPDATE_TIME = "tide_lastupdate";

    private final static String TIDE_LOCATION_LAT = ".lat";
    private final static String TIDE_LOCATION_LNG = ".lng";

    public final static SimpleDateFormat fmtDateTime = new SimpleDateFormat("yyyy MM/dd HH:mm");
    
    public static long getLastUpdateTimeInMS(Context context,String tideLocation) {
        final String locationFileName = TIDE_LOCATION_PREFIX + tideLocation;
        final SharedPreferences perf = context.getSharedPreferences(locationFileName,Context.MODE_PRIVATE);
        return perf.getLong(TIDE_UPDATE_TIME, 0L);
    }
    
    public static boolean isPerfExpire(Context context,String tideLocation) {
        //final String locationFileName = TIDE_LOCATION_PREFIX + tideLocation;
        //final SharedPreferences perf = context.getSharedPreferences(locationFileName,Context.MODE_PRIVATE);
        //final String[] dateKeys = perf.getString(TIDE_DATES,"").split(SEPERATOR);
        //if(0 == dateKeys.length) {
        //    return true;
        //}
        //else {
            /*
            String maxDate = dateKeys[0];
            for(int i = 1;i < dateKeys.length;i++) {
                if(String.CASE_INSENSITIVE_ORDER.compare(maxDate, dateKeys[i]) < 0) {
                    maxDate = dateKeys[i];
                }
            }
            
            final String maxtimeKey = TIDE_TIMES_PREFIX + maxDate;
            final String[] times = perf.getString(maxtimeKey,"").split(SEPERATOR);
            long maxTime = 0L;
            for(String timeString : times) {
                try {
                    final long time = Long.parseLong(timeString);
                    maxTime = Math.max(maxTime, time);
                }
                catch(Exception e) {}
                
            }
            */
            //Show debug message
            final Calendar cal = Calendar.getInstance();
            final long lastUpdateTimeInMS = getLastUpdateTimeInMS(context,tideLocation);
            cal.setTimeInMillis(lastUpdateTimeInMS);
            Log.i(TAG, "lastUpdateTimeInMS=" + fmtDateTime.format(cal.getTime()));

            return Calendar.getInstance().getTimeInMillis() - lastUpdateTimeInMS > 24L * 60L * 60L * 1000L; //Fetch tide 1 day
        //}
        
    }

    public static void saveTides(Context context,String tideLocationName,Location tideLocation,TideInformation[] tideInformationArray) {
        Log.d(TAG, "processing " + tideLocationName);
        final String locationFileName = TIDE_LOCATION_PREFIX + tideLocationName;
        final SharedPreferences.Editor editor = context.getSharedPreferences(locationFileName,Context.MODE_PRIVATE).edit();
        
        final Calendar cal = Calendar.getInstance();
        
        editor.putLong(TIDE_UPDATE_TIME, cal.getTimeInMillis());
        
        final Map<String,Map<Long,Integer>> dateToHeightMap = new HashMap<String,Map<Long,Integer>>();
        
        for(int r = 0;r < tideInformationArray.length;r++) {
                if(tideInformationArray[r].day_of_month != TideInformation.NOSET_INT) {
                    cal.set(Calendar.DAY_OF_MONTH, tideInformationArray[r].day_of_month);
                }
                if(tideInformationArray[r].month != TideInformation.NOSET_INT) {
                    cal.set(Calendar.MONTH, tideInformationArray[r].month);
                }
                if(tideInformationArray[r].hours != TideInformation.NOSET_INT) {
                    cal.set(Calendar.HOUR_OF_DAY, tideInformationArray[r].hours);
                }
                if(tideInformationArray[r].mins != TideInformation.NOSET_INT) {
                    cal.set(Calendar.MINUTE, tideInformationArray[r].mins);
                }
                if(tideInformationArray[r].tide_height != TideInformation.NOSET_INT) {
                    final String dateKey = fmtDate.format(cal.getTime());
                    Map<Long,Integer> timeToHeightMap = null;
                    if(!dateToHeightMap.containsKey(dateKey)) {
                        timeToHeightMap = new HashMap<Long,Integer>();
                        dateToHeightMap.put(dateKey, timeToHeightMap);
                    }
                    else {
                        timeToHeightMap = dateToHeightMap.get(dateKey);
                    }
                    
                    timeToHeightMap.put(cal.getTimeInMillis(), tideInformationArray[r].tide_height);
                    Log.d(TAG, "tideLocationName=" + tideLocationName + " date=" + dateKey + " time-" + fmtTime.format(cal.getTime()) + " height=" + tideInformationArray[r].tide_height);
                }
                
            
        }
        
        //Put all dates
       
        editor.putString(TIDE_DATES, longSetToString(new ArrayList<String>(dateToHeightMap.keySet())));
        
        for(Entry<String,Map<Long,Integer>> entry : dateToHeightMap.entrySet()) {
            final String dateKey = entry.getKey();
            final String timeKey = TIDE_TIMES_PREFIX + dateKey;
            
            //put all times
            editor.putString(timeKey, longSetToString(new ArrayList<Long>(entry.getValue().keySet())));
            
            for(Entry<Long,Integer> timeEntry : entry.getValue().entrySet()){
                editor.putInt(String.valueOf(timeEntry.getKey()), timeEntry.getValue());
            }

        }        
        
        //save location if not exist
        final SharedPreferences perf = context.getSharedPreferences(locationFileName,Context.MODE_PRIVATE);
        final String latKey = tideLocationName + TIDE_LOCATION_LAT;
        final String lngKey = tideLocationName + TIDE_LOCATION_LNG;
        if(!perf.contains(latKey) || !perf.contains(lngKey)) {
            editor.putFloat(latKey, (float) tideLocation.getLatitude());
            editor.putFloat(lngKey, (float) tideLocation.getLongitude());
        }
        
        editor.commit();
    }
    
    public static TideAdapterData loadTide(Context context,String tideLocation) {
        Log.d(TAG, "loading " + tideLocation);
        final String locationFileName = TIDE_LOCATION_PREFIX + tideLocation;
        final SharedPreferences perf = context.getSharedPreferences(locationFileName,Context.MODE_PRIVATE);
        final String latKey = tideLocation + TIDE_LOCATION_LAT;
        final String lngKey = tideLocation + TIDE_LOCATION_LNG;
        
        final float lat = perf.getFloat(latKey, 0.0f);
        final float lng = perf.getFloat(lngKey, 0.0f);
        final String[] dateKeys = perf.getString(TIDE_DATES,"").split(SEPERATOR);

        if(0 == dateKeys.length || 0.0f == lat || 0.0f == lng) {
            Log.w(TAG, "lat=" + lat + " lng=" + lng + " dateKeys.length=" + dateKeys.length);
            return null;
        }
        else {
            final Calendar cal = Calendar.getInstance();
            final Location loc = new Location(locationFileName);
            loc.setLatitude(lat);
            loc.setLongitude(lng);
            
            final TideAdapterData tideData = new TideAdapterData(tideLocation,loc);

            for(String dateString : dateKeys) {
                final String timeKey = TIDE_TIMES_PREFIX + dateString;
                final String[] times = perf.getString(timeKey,"").split(SEPERATOR);
                for(String timeString : times) {
                    try {
                        final int height = perf.getInt(timeString, Integer.MIN_VALUE);
                        final long time = Long.parseLong(timeString);
                        
                        tideData.addHeight(dateString, time, height);
                        
                        cal.setTimeInMillis(time);
                        Log.d(TAG, "Add height tideLocation=" + tideLocation + " time=" + fmtDate.format(cal.getTime()) + fmtTime.format(cal.getTime()) + " height=" + height);
                        
                    }
                    catch(Exception e) {
                        Log.e(TAG, "",e);
                    }
                    
                }
                
            }
            return tideData;
        }
        
    }
    
    
    private static String longSetToString(List<?> list) {
        final StringBuffer dataSB = new StringBuffer();
        
        for(int i = 0;i < list.size();i++) {
            dataSB.append(list.get(i));
            if(i < list.size() - 1) {
                dataSB.append(SEPERATOR);
            }
        }
        return dataSB.toString();
    }

    
}
