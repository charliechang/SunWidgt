package com.bk.sunwidgt.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.bk.sunwidgt.task.RainInformation;
import com.bk.sunwidgt.task.SearchLocationTask;

public class RainStoreUtil {
    
    private final static String TAG = "Sun" + RainStoreUtil.class.getSimpleName();
    private final static String SEPERATOR = "\t";
    private final static String RAIN_UPDATE_TIME = RainStoreUtil.class.getName() + "update_time";
    private final static String RAIN_LOCATION_NAME = RainStoreUtil.class.getName() + "rain_data";
    
    private final static String KEY_LOCATION_NAME = RainStoreUtil.class.getName() + ".location_names";
    private final static String KEY_OBSERVER_NAME = RainStoreUtil.class.getName() + ".observer_names";
    
    private final static String KEY_LOCATION_YEAR = RainStoreUtil.class.getName() + ".year";
    private final static String KEY_LOCATION_MONTH = RainStoreUtil.class.getName() + ".month";
    private final static String KEY_LOCATION_DAY = RainStoreUtil.class.getName() + ".day";
    private final static String KEY_LOCATION_START_HOUR = RainStoreUtil.class.getName() + ".hour";
    
    private final static String RAIN_LOCATION_DATA = ".rain_location_data";
    private final static String RAIN_LOCATION_LAT = ".lat";
    private final static String RAIN_LOCATION_LNG = ".lng";
    
    private final static ExecutorService GEOCODER_THREADPOOL = Executors.newFixedThreadPool(5);
    
    public final static SimpleDateFormat fmtDateTime = new SimpleDateFormat("yyyy MM/dd HH:mm");
    
    public static long getLastUpdateTimeInMS(Context context) {
        final SharedPreferences perf = context.getSharedPreferences(RAIN_LOCATION_NAME,Context.MODE_PRIVATE);
        return perf.getLong(RAIN_UPDATE_TIME, 0L);
    }
    
    public static boolean isPerfExpire(Context context) {
        final SharedPreferences perf = context.getSharedPreferences(RAIN_LOCATION_NAME,Context.MODE_PRIVATE);
        final int year = perf.getInt(KEY_LOCATION_YEAR, 1900);
        final int month = perf.getInt(KEY_LOCATION_MONTH, 0);
        final int day = perf.getInt(KEY_LOCATION_DAY, 0);
        final int start_hour = perf.getInt(KEY_LOCATION_START_HOUR, 0);
        final long updateTime = getLastUpdateTimeInMS(context);
        
        final Calendar perfTime = Calendar.getInstance();
        final long currentTimeInMS = perfTime.getTimeInMillis();
        
        perfTime.set(year, month, day, start_hour, 0, 0);
        Log.d(TAG, "cache=" + fmtDateTime.format(perfTime.getTime()));
        return currentTimeInMS - perfTime.getTimeInMillis() > 2L * 60L * 60L * 1000L && currentTimeInMS - updateTime > 30L * 60L * 1000L;
    }
    
    
    public static RainAdapterData[] loadRains(Context context) {
        
        final SharedPreferences perf = context.getSharedPreferences(RAIN_LOCATION_NAME,Context.MODE_PRIVATE);
        final int year = perf.getInt(KEY_LOCATION_YEAR, RainInformation.NOSET_INT);
        final int month = perf.getInt(KEY_LOCATION_MONTH, RainInformation.NOSET_INT);
        final int day = perf.getInt(KEY_LOCATION_DAY, RainInformation.NOSET_INT);
        final int start_hour = perf.getInt(KEY_LOCATION_START_HOUR, RainInformation.NOSET_INT);
        final String[] locationNames = perf.getString(KEY_LOCATION_NAME, "").split(SEPERATOR);
        final String[] observerNames = perf.getString(KEY_OBSERVER_NAME, "").split(SEPERATOR);
        final List<RainAdapterData> rainList = new ArrayList<RainAdapterData>(locationNames.length);
        
        Log.d(TAG, "header found month=" + (1+month) + " day=" + day + " start_hour=" + start_hour);

        for(int i = 0;i < locationNames.length;i++) {
            final String latKey = getRainKey(locationNames[i],"",RAIN_LOCATION_LAT);
            final String lngKey = getRainKey(locationNames[i],"",RAIN_LOCATION_LNG);
            final String rainKey = getRainKey(locationNames[i],observerNames[i],RAIN_LOCATION_DATA);
            
            final String[] rainDatas = perf.getString(rainKey, "").split(SEPERATOR);
            final float lat = perf.getFloat(latKey, 0.0f);
            final float lng = perf.getFloat(lngKey, 0.0f);
            
            if(0 == rainDatas.length || 0.0f == lat || 0.0f == lng) {
                Log.w(TAG, "Skip " + locationNames[i] + " " + observerNames[i] + " " + perf.getString(rainKey,null) + " lat=" + lat + " lng=" + lng);
            }
            else {
                final Location loc = new Location(RAIN_LOCATION_NAME);
                loc.setLatitude(lat);
                loc.setLongitude(lng);
                final RainAdapterData locationRainData = new RainAdapterData(locationNames[i],observerNames[i],loc,year,month,day,start_hour);
                final StringBuffer debugLineSB = new StringBuffer();
                
                Log.i(TAG, "locationName=" + locationNames[i] + " observerName=" + observerNames[i] + " lat=" + lat + " lng=" + lng);
                
                int hour = start_hour;
                for(int r = 0;
                        r < Math.max(RainAdapterData.MAX_HOURS, rainDatas.length);
                        r++,hour = (hour + RainAdapterData.MAX_HOURS - 1) % RainAdapterData.MAX_HOURS) {
                    
                    debugLineSB.append(hour).append(" ");
                    
                    try {
                        float measure = Float.parseFloat(rainDatas[r]);
                        locationRainData.setHour(hour, measure);
                        debugLineSB.append(measure);
                    }
                    catch(Exception e) {
                        locationRainData.setHour(hour, 0.0f);
                        debugLineSB.append("0.0");
                    }
                    
                    //debug print
                    debugLineSB.append(SEPERATOR);
                }

                debugLineSB.append("sum3=" + locationRainData.sumRainHour(3));
                Log.d(TAG, debugLineSB.toString());
                
                rainList.add(locationRainData);
            }
            
            
        }
        
        return rainList.toArray(new RainAdapterData[0]);
    }  

    public static void saveRains(Context context,RainInformation[][] rainInformatinoArray) {
        
        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        int month = RainInformation.NOSET_INT;
        int day = RainInformation.NOSET_INT;
        int hour = RainInformation.NOSET_INT;
        int row = 0;
        int maxcol = 0;

        final SharedPreferences.Editor editor = context.getSharedPreferences(RAIN_LOCATION_NAME,Context.MODE_PRIVATE).edit();
        
        //put update time
        editor.putLong(RAIN_UPDATE_TIME, cal.getTimeInMillis());
        
        //parse header
        boolean headerFound = false;
        for(;row < rainInformatinoArray.length && !headerFound;row++) {
            maxcol = Math.max(maxcol,rainInformatinoArray[row].length);
            for(int col = 0;col < rainInformatinoArray[row].length && !headerFound ;col++) {
                
                if(rainInformatinoArray[row][col].day_of_month != RainInformation.NOSET_INT &&
                        rainInformatinoArray[row][col].month != RainInformation.NOSET_INT &&
                        rainInformatinoArray[row][col].fromHours != RainInformation.NOSET_INT) {
                    
                    month = rainInformatinoArray[row][col].month;
                    day = rainInformatinoArray[row][col].day_of_month;
                    hour = rainInformatinoArray[row][col].fromHours;
                    headerFound = true;
                }
                
            }
            
        }
        
        if(!headerFound) {
            Log.d(TAG, "No header found");
        }
        else {
            
            editor.putInt(KEY_LOCATION_YEAR, year);
            editor.putInt(KEY_LOCATION_MONTH,month);
            editor.putInt(KEY_LOCATION_DAY,day);
            editor.putInt(KEY_LOCATION_START_HOUR,hour);

            Log.d(TAG, "header found month=" + (1+month) + " day=" + day + " hour=" + hour + " in row=" + row + " maxcol=" + maxcol);
            
            final List<String> locationList = new ArrayList<String>(rainInformatinoArray.length);
            final List<String> observerList = new ArrayList<String>(rainInformatinoArray.length);
            
            for(;row < rainInformatinoArray.length;row++) {
                
                //Get LocationName
                String locationName = null;
                if(rainInformatinoArray[row].length > 0 && 
                        rainInformatinoArray[row][0].location != RainInformation.NOSET_STRING) {
                    
                    locationName = rainInformatinoArray[row][0].location;
                }
                String observerName = null;
                if(rainInformatinoArray[row].length ==  maxcol && 
                        rainInformatinoArray[row][maxcol - 1].location != RainInformation.NOSET_STRING) {
                    
                    observerName = rainInformatinoArray[row][maxcol - 1].location;
                }
                
                if(null == locationName || null == observerName) {
                    Log.d(TAG, "Skip row=" + row);
                }
                else {
                    
                    locationList.add(locationName);
                    observerList.add(observerName);
                    
                    Log.i(TAG, "locationName=" + locationName + " observerName=" + observerName);
                    int colhour = hour;

                    final StringBuffer debugLineSB = new StringBuffer();
                    final List<String> rainList = new ArrayList<String>(rainInformatinoArray.length);
                    
                    for(int col = 1;
                            col < Math.min(rainInformatinoArray[row].length, RainAdapterData.MAX_HOURS) ;
                            col++,colhour=(colhour + RainAdapterData.MAX_HOURS - 1) % RainAdapterData.MAX_HOURS) {
                        
                        
                        rainList.add(String.valueOf(rainInformatinoArray[row][col].mesure != RainInformation.NOSET_FLOAT ? rainInformatinoArray[row][col].mesure : 0.0f));
                        
                        //debug print
                        debugLineSB.append(colhour).append(" ");
                        if(rainInformatinoArray[row][col].mesure != RainInformation.NOSET_FLOAT) {
                            debugLineSB.append(rainInformatinoArray[row][col].mesure);
                        }
                        else {
                            debugLineSB.append("0.0");
                        }
                        debugLineSB.append(SEPERATOR);
                        
                    }
                    
                    
                    editor.putString(getRainKey(locationName,observerName,RAIN_LOCATION_DATA), intSetToString(rainList));
                    
                    Log.d(TAG, debugLineSB.toString());
                }
                
            }

            editor.putString(KEY_LOCATION_NAME, intSetToString(locationList));
            editor.putString(KEY_OBSERVER_NAME, intSetToString(observerList));
            
            //Put lat and lng
            Geocoder geocoder = null;
            
            final SharedPreferences perf = context.getSharedPreferences(RAIN_LOCATION_NAME,Context.MODE_PRIVATE);
            final Hashtable<Integer,SearchLocationTask> pendingSearchSet = new Hashtable<Integer,SearchLocationTask>();
            
            for(int i = 0;i < locationList.size();i++) {
                final String locationName = locationList.get(i);
                final String latKey = getRainKey(locationName,"",RAIN_LOCATION_LAT);
                final String lngKey = getRainKey(locationName,"",RAIN_LOCATION_LNG);
                if(!perf.contains(latKey) || !perf.contains(lngKey)) {
                    
                    if(null == geocoder) {
                        geocoder = new Geocoder(context);
                    }
                    
                    final SearchLocationTask task = new SearchLocationTask(locationName,geocoder);
                    task.executeOnExecutor(GEOCODER_THREADPOOL);
                    
                    Log.d(TAG, "start search " + locationName);
                    pendingSearchSet.put(i,task);
                }
            }
            
            for(Entry<Integer,SearchLocationTask> entry : pendingSearchSet.entrySet()) {
                final String locationName = locationList.get(entry.getKey());
                
                try {
                    final Address address = entry.getValue().get(10L, TimeUnit.SECONDS);
                    final String latKey = getRainKey(locationName,"",RAIN_LOCATION_LAT);
                    final String lngKey = getRainKey(locationName,"",RAIN_LOCATION_LNG);
                    if(address != null) {
                        editor.putFloat(latKey, (float) address.getLatitude());
                        editor.putFloat(lngKey, (float) address.getLongitude());
                    }
                    
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to get Address=" + locationName,e);
                } catch (ExecutionException e) {
                    Log.e(TAG, "Unable to get Address=" + locationName,e);
                } catch (TimeoutException e) {
                    Log.e(TAG, "Unable to get Address=" + locationName,e);
                }
                
                
            }
        }
        
        Log.d(TAG, "Commit to perference");
        editor.commit();

    }
    
    
    private static String getRainKey(String locationName,String observerName,String POSFIX) {
        return locationName + "." + observerName + POSFIX;
    }
    
    private static String intSetToString(List<?> list) {
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
