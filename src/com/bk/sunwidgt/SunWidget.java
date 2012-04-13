package com.bk.sunwidgt;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.bk.sunwidgt.lib.SunCalculator;
import com.bk.sunwidgt.lib.SunCalculator.SunriseSunset;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;


public class SunWidget extends AppWidgetProvider {
    private final static String TAG = SunWidget.class.getSimpleName();
    private final static SimpleDateFormat fmtDate = new SimpleDateFormat("MM/dd");
    private final static SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm");
    
    @Override
    public void onUpdate(Context context,AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "+onUpdate");
        RemoteViews updateViews = new RemoteViews( context.getPackageName(), com.bk.sunwidgt.R.layout.main);
        Calendar cal = Calendar.getInstance();
        
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location coarseLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        
        double lat = null == coarseLocation ? 25.045792 : coarseLocation.getLatitude();
        double lng = null == coarseLocation ? 121.453857 : coarseLocation.getLongitude();
        Log.d(TAG, "lat=" + lat + " lng=" + lng);
        
        SunriseSunset answer = null;
        
        
        answer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
        fillTable(updateViews,answer,com.bk.sunwidgt.R.id.day1_title,com.bk.sunwidgt.R.id.day1_sunrise,com.bk.sunwidgt.R.id.day1_sunset);
        Log.d(TAG, answer.toString());
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        answer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
        fillTable(updateViews,answer,com.bk.sunwidgt.R.id.day2_title,com.bk.sunwidgt.R.id.day2_sunrise,com.bk.sunwidgt.R.id.day2_sunset);
        Log.d(TAG, answer.toString());
        
        //cal.add(Calendar.DAY_OF_MONTH, 1);
        //answer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
        //fillTable(updateViews,answer,com.bk.sunwidgt.R.id.day3_title,com.bk.sunwidgt.R.id.day3_sunrise,com.bk.sunwidgt.R.id.day3_sunset);
        //Log.d(TAG, answer.toString());
        //System.out.println("sunrise=" + fmt.format(answer.sunrise));
        //System.out.println("sunset=" + fmt.format(answer.sunset));
        
        //((TextView)(findViewById(com.bk.sunwidgt.R.id.rise))).setText(fmt.format(answer.sunrise));
        //((TextView)(findViewById(com.bk.sunwidgt.R.id.set))).setText(fmt.format(answer.sunset));
        

        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
        Log.d(TAG, "-onUpdate");
    }
    
    private void fillTable(RemoteViews view,SunriseSunset answer,int res_title,int res_sunrise,int res_sunset) {
        view.setTextViewText(res_title,  fmtDate.format(answer.sunrise));
        view.setTextViewText(res_sunrise,  fmtTime.format(answer.sunrise )  + " " + (int) answer.sunrise_azel);
        view.setTextViewText(res_sunset,  fmtTime.format(answer.sunset ) + " " + (int) answer.sunset_azel);
    }
    
}
