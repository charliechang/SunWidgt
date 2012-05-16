
package com.bk.sunwidgt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bk.sunwidgt.activity.SunActivity;
import com.bk.sunwidgt.lib.MoonCalculator.MoonriseMoonset;
import com.bk.sunwidgt.lib.MoonCalculator;
import com.bk.sunwidgt.lib.SunCalculator;
import com.bk.sunwidgt.lib.SunCalculator.SunriseSunset;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class SunWidget extends AppWidgetProvider {
    private final static String TAG = SunWidget.class.getSimpleName();
    public final static SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm");
    private final static SimpleDateFormat fmtDate = new SimpleDateFormat("MM/dd");
    public final static String notimeString = "--:--";
    private final static String REFRESH_ACTION = SunWidget.class.getName() + ".refresh";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "+onReceive");
        final String action = intent.getAction();
        if (action.equals(REFRESH_ACTION)) {
            Log.d(TAG, "action=" + REFRESH_ACTION);

            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, SunWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
                    com.bk.sunwidgt.R.id.mainlayout);
        }

        super.onReceive(context, intent);

        Log.d(TAG, "-onReceive");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "+onUpdate");
        final Geocoder gencorder = new Geocoder(context);
        final RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                com.bk.sunwidgt.R.layout.main);
        Calendar cal = Calendar.getInstance();

        LocationManager locManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        Location coarseLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        final double lat = null == coarseLocation ? 25.045792 : coarseLocation.getLatitude();
        final double lng = null == coarseLocation ? 121.453857 : coarseLocation.getLongitude();
        Log.d(TAG, "lat=" + lat + " lng=" + lng);

        final AsyncTask<Void, Void, Address> getlocationTask = new AsyncTask<Void, Void, Address>() {

            @Override
            protected Address doInBackground(Void... arg0) {
                try {
                    List<Address> listAddress = gencorder.getFromLocation(lat, lng, 1);
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

        };

        getlocationTask.execute();

        SunriseSunset sunAnswer = null;
        MoonriseMoonset moonAnswer = null;

        sunAnswer = SunCalculator.getSunriseSunset(cal, lat, lng, false);
        moonAnswer = MoonCalculator.getMoonriseMoonset(cal, lat, lng);
        fillSunTable(updateViews, sunAnswer, com.bk.sunwidgt.R.id.day1_title,
                com.bk.sunwidgt.R.id.day1_sunrise, com.bk.sunwidgt.R.id.day1_sunset);
        fillMoonTable(updateViews, moonAnswer, com.bk.sunwidgt.R.id.day1_title,
                com.bk.sunwidgt.R.id.day1_moonrise, com.bk.sunwidgt.R.id.day1_moonset);
        Log.d(TAG, sunAnswer.toString());
        Log.d(TAG, moonAnswer.toString());

        try {
            final Address address = getlocationTask.get(9L, TimeUnit.SECONDS);

            if (address != null) {
                if (address.getAdminArea() != null) {
                    updateViews.setTextViewText(com.bk.sunwidgt.R.id.widget_city,
                            address.getAdminArea());
                }
                else if (address.getCountryName() != null) {
                    updateViews.setTextViewText(com.bk.sunwidgt.R.id.widget_city,
                            address.getCountryName());
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "", e);
        } catch (TimeoutException e) {
            Log.e(TAG, "", e);
        }
        final Intent refreshIntent = new Intent(context, SunWidget.class);
        refreshIntent.setAction(REFRESH_ACTION);
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
                refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        updateViews.setOnClickPendingIntent(com.bk.sunwidgt.R.id.widget_city, refreshPendingIntent);

        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(
                context, SunActivity.class), 0);
        updateViews.setOnClickPendingIntent(com.bk.sunwidgt.R.id.mainlayout, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);

        Log.d(TAG, "-onUpdate");
    }

    private void fillSunTable(RemoteViews view, SunriseSunset answer, int res_title,
            int res_sunrise, int res_sunset) {
        view.setTextViewText(res_title, fmtDate.format(answer.sunrise));
        view.setTextViewText(res_sunrise, fmtTime.format(answer.sunrise) + " "
                + (int) answer.sunrise_azel);
        view.setTextViewText(res_sunset, fmtTime.format(answer.sunset) + " "
                + (int) answer.sunset_azel);
    }

    private void fillMoonTable(RemoteViews view, MoonriseMoonset answer, int res_title,
            int res_sunrise, int res_sunset) {
        if (answer.moonrise != null) {
            view.setTextViewText(res_sunrise, fmtTime.format(answer.moonrise) + " "
                    + (int) answer.rise_az);
        }
        else {
            view.setTextViewText(res_sunrise, notimeString);
        }

        if (answer.moonset != null) {
            view.setTextViewText(res_sunset, fmtTime.format(answer.moonset) + " "
                    + (int) answer.set_sz);
        }
        else {
            view.setTextViewText(res_sunset, notimeString);
        }
    }

}
