package com.bk.sunwidgt;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.bk.sunwidgt.lib.SunCalculator;
import com.bk.sunwidgt.lib.SunCalculator.SunriseSunset;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.TextView;


public class SunWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context,AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews updateViews = new RemoteViews( context.getPackageName(), R.layout.main);
        final SunriseSunset answer = SunCalculator.getSunriseSunset(Calendar.getInstance(),25.045792,121.453857,false);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        //System.out.println("sunrise=" + fmt.format(answer.sunrise));
        //System.out.println("sunset=" + fmt.format(answer.sunset));
        
        //((TextView)(findViewById(com.bk.sunwidgt.R.id.rise))).setText(fmt.format(answer.sunrise));
        //((TextView)(findViewById(com.bk.sunwidgt.R.id.set))).setText(fmt.format(answer.sunset));
        updateViews.setTextViewText(com.bk.sunwidgt.R.id.rise,  new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(answer.sunrise ) );
        updateViews.setTextViewText(com.bk.sunwidgt.R.id.set,  new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(answer.sunset ) );
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
}
/*
public class SunWidget extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
       //final TimeZone timeZone = cal.getTimeZone();
        
        //cal.set(2012, 3-1, 27, 02, 34, 44);
        
        final SunriseSunset answer = SunCalculator.getSunriseSunset(Calendar.getInstance(),25.045792,121.453857,false);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        //System.out.println("sunrise=" + fmt.format(answer.sunrise));
        //System.out.println("sunset=" + fmt.format(answer.sunset));
        
        ((TextView)(findViewById(com.bk.sunwidgt.R.id.rise))).setText(fmt.format(answer.sunrise));
        ((TextView)(findViewById(com.bk.sunwidgt.R.id.set))).setText(fmt.format(answer.sunset));
        
    }
}*/
