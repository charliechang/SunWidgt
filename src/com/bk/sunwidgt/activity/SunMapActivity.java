
package com.bk.sunwidgt.activity;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bk.sunwidgt.SunWidget;
import com.bk.sunwidgt.lib.MoonCalculator;
import com.bk.sunwidgt.lib.SunCalculator;
import com.bk.sunwidgt.task.DelaySearchAddressTask;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class SunMapActivity extends MapActivity {
    public final static String START_LOCATION = SunMapActivity.class.getName() + ".start_location";
    public final static String SHOW_TIME = SunMapActivity.class.getName() + ".show_time";
    public final static int DEFAULT_ZOOM_LEVEL = 10;
    public final static double DOUBLE_1E6 = 1E6;
    public final static int MESSAGE_SET_ADDRESS = 1;
    public final static int MESSAGE_QUERY_ADDRESS = 2;

    private final static String TAG = SunMapActivity.class.getSimpleName();

    private final static int CIRCLE_RADIOUS = 8;

    private final static DecimalFormat LATLNG_FORMATTER = new DecimalFormat("#.#####");
    private final static Paint REDPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint TEXTPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static int TEXT_PADDING = 2;
    static {
        REDPAINT.setColor(Color.RED);
        TEXTPAINT.setColor(Color.BLACK);
        TEXTPAINT.setTextSize(14);
    }

    private Geocoder m_geocoder;
    private MapController m_mapController;
    private MapView m_mapView;
    private TextView m_addressView;
    private Button m_bookmarkButton;
    private long m_showTime;
    private GeoPoint m_geopoint;

    private Handler m_Handler = new Handler() {

        public void handleMessage(Message msg) {

            if (MESSAGE_SET_ADDRESS == msg.what) {

                final Address address = (Address) msg.obj;
                final StringBuffer addressStringSB = new StringBuffer();

                for (int i = 0; address != null && i < address.getMaxAddressLineIndex() + 1; i++) {
                    addressStringSB.append(address.getAddressLine(i));
                    if(i < address.getMaxAddressLineIndex()) {
                        addressStringSB.append(" ");
                    }
                }

                m_addressView.setText(addressStringSB.toString());

                m_bookmarkButton.setText(com.bk.sunwidgt.R.string.map_bookmark);
                m_bookmarkButton.setEnabled(true);

            }
            else if (MESSAGE_QUERY_ADDRESS == msg.what) {

                m_bookmarkButton.setText(com.bk.sunwidgt.R.string.map_locating);
                m_bookmarkButton.setEnabled(false);
            }

        }

    };

    class ShowTimeView extends Overlay {

        private final Point screenPoint = new Point();
        private final Calendar cal = Calendar.getInstance();

        private Timer searchAddressTimer = new Timer();

        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            // draw red circle
            mapView.getProjection().toPixels(m_geopoint, screenPoint);
            canvas.drawCircle(screenPoint.x, screenPoint.y, CIRCLE_RADIOUS, REDPAINT);

            cal.setTimeInMillis(m_showTime);

            final SunCalculator.SunriseSunset sunTime = SunCalculator.getSunriseSunset(cal,
                    (double) m_geopoint.getLatitudeE6() / DOUBLE_1E6,
                    (double) m_geopoint.getLongitudeE6() / DOUBLE_1E6, false);

            final MoonCalculator.MoonriseMoonset moonTime = MoonCalculator.getMoonriseMoonset(cal,
                    (double) m_geopoint.getLatitudeE6() / 1E6,
                    (double) m_geopoint.getLongitudeE6() / 1E6);

            final StringBuffer timeSB = new StringBuffer();

            // Show date
            timeSB.append(SunWidget.fmtDate.format(cal.getTime()));
            canvas.drawText(timeSB.toString(), screenPoint.x + CIRCLE_RADIOUS * 2, screenPoint.y
                    + TEXTPAINT.getTextSize() / 2, TEXTPAINT);

            float y = screenPoint.y + CIRCLE_RADIOUS + TEXT_PADDING + TEXTPAINT.getTextSize();
            float x = screenPoint.x - CIRCLE_RADIOUS / 2;

            // Show Sunrise
            timeSB.delete(0, timeSB.length());
            timeSB.append(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_sunrise));
            timeSB.append(SunWidget.fmtTime.format(sunTime.sunrise));
            canvas.drawText(timeSB.toString(), x, y, TEXTPAINT);
            y += TEXTPAINT.getTextSize() + TEXT_PADDING;

            // Show Sunset
            timeSB.delete(0, timeSB.length());
            timeSB.append(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_sunset));
            timeSB.append(SunWidget.fmtTime.format(sunTime.sunset));
            canvas.drawText(timeSB.toString(), x, y, TEXTPAINT);
            y += TEXTPAINT.getTextSize() + TEXT_PADDING;

            // Show Moonrise
            y += TEXT_PADDING;
            timeSB.delete(0, timeSB.length());
            timeSB.append(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_moonrise));
            if (null == moonTime.moonrise) {
                timeSB.append(SunWidget.notimeString);
            }
            else {
                timeSB.append(SunWidget.fmtTime.format(moonTime.moonrise));
            }
            canvas.drawText(timeSB.toString(), x, y, TEXTPAINT);
            y += TEXTPAINT.getTextSize() + TEXT_PADDING;

            // Show Moonset
            timeSB.delete(0, timeSB.length());
            timeSB.append(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_moonset));
            if (null == moonTime.moonset) {
                timeSB.append(SunWidget.notimeString);
            }
            else {
                timeSB.append(SunWidget.fmtTime.format(moonTime.moonset));
            }
            canvas.drawText(timeSB.toString(), x, y, TEXTPAINT);
            y += TEXTPAINT.getTextSize() + TEXT_PADDING;
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {

            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                m_geopoint = mapView.getProjection().fromPixels((int) event.getX(),
                        (int) event.getY());
                final StringBuffer latlngSB = new StringBuffer();
                final double lat = (double) m_geopoint.getLatitudeE6() / DOUBLE_1E6;
                final double lng = (double) m_geopoint.getLongitudeE6() / DOUBLE_1E6;
                latlngSB.append(LATLNG_FORMATTER.format(lat));
                latlngSB.append(",");
                latlngSB.append(LATLNG_FORMATTER.format(lng));

                m_addressView.setText(latlngSB.toString());

                if (searchAddressTimer != null) {
                    searchAddressTimer.cancel();
                }
                searchAddressTimer = new Timer();

                searchAddressTimer.schedule(new DelaySearchAddressTask(m_Handler, 8000L,
                        m_geocoder, lat, lng), 1000L);

            }

            return false;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.bk.sunwidgt.R.layout.map_activity);

        m_geocoder = new Geocoder(this);
        m_mapView = (MapView) findViewById(com.bk.sunwidgt.R.id.mapview);
        m_addressView = (TextView) findViewById(com.bk.sunwidgt.R.id.map_address);
        m_bookmarkButton = (Button) findViewById(com.bk.sunwidgt.R.id.map_bookmark);

        m_mapController = m_mapView.getController();

        Location startLocation = null;
        if (getIntent().hasExtra(START_LOCATION)) {
            startLocation = getIntent().getParcelableExtra(START_LOCATION);
            Log.d(TAG, "Get Location from intent");
        }
        else if (savedInstanceState != null && savedInstanceState.containsKey(START_LOCATION)) {
            startLocation = savedInstanceState.getParcelable(START_LOCATION);
            Log.d(TAG, "Get Location from bundle");
        }
        else {
            final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location coarseLocation = locManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (null == coarseLocation) {
                coarseLocation = new Location(LocationManager.NETWORK_PROVIDER);
                coarseLocation.setLatitude(25.045792);
                coarseLocation.setLongitude(121.453857);
                Log.d(TAG, "Use default location");
            }
            else {
                Log.d(TAG, "Get Location from LocationManager");
            }

            startLocation = coarseLocation;
        }

        Log.i(TAG, "startLocation=" + startLocation);
        m_mapController.animateTo(new GeoPoint((int) (startLocation.getLatitude() * 1E6),
                (int) (startLocation.getLongitude() * 1E6)));
        m_mapController.setZoom(DEFAULT_ZOOM_LEVEL);

        m_geopoint = m_mapView.getMapCenter();

        ShowTimeView mapOverlay = new ShowTimeView();
        List<Overlay> listOfOverlays = m_mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);

        // TODO: Check intent or onSaveInstance before use current time
        m_showTime = Calendar.getInstance().getTimeInMillis();

        m_mapView.invalidate();
    }

    // Call when bookmark is clicked
    public void doBookmark(View view) {
        Log.d(TAG, "doBookmark");
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
