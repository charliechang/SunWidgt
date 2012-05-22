
package com.bk.sunwidgt.activity;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bk.sunwidgt.SunWidget;
import com.bk.sunwidgt.fragment.BookmarkFragment;
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
    public final static String START_LOCATION_EXTRAS = SunMapActivity.class.getName()
            + ".start_location_extras";
    public final static String SHOW_TIME = SunMapActivity.class.getName() + ".show_time";
    public final static String LOCATION_ADDRESS = SunMapActivity.class.getName()
            + ".location_address";
    public final static int DEFAULT_ZOOM_LEVEL = 10;
    public final static double DOUBLE_1E6 = 1E6;
    public final static int MESSAGE_SET_ADDRESS = 1;
    public final static int MESSAGE_QUERY_ADDRESS = 2;
    public final static int MESSAGE_UPDTAE_MAPVIEW = 3;

    private final static String TAG = SunMapActivity.class.getSimpleName();
    private final static String PERF_DATEPICKER_ENABLE = SunMapActivity.class.getName()
            + ".datepickerenable";
    private final static int CIRCLE_RADIOUS = 8;

    private final static DecimalFormat LATLNG_FORMATTER = new DecimalFormat("#.#####");
    private final static Paint REDPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint BLUEPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint TEXTPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    static {
        REDPAINT.setColor(Color.RED);
        BLUEPAINT.setColor(Color.BLUE);
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
    private OptionMenuCreator m_menuCreator;

    private DatePicker m_datePicker;
    private CheckBox m_dataPickerEnableCheckbox;

    private RelativeLayout m_mainLayout;

    private Handler m_Handler = new Handler() {

        public void handleMessage(Message msg) {

            if (MESSAGE_SET_ADDRESS == msg.what) {

                final Address address = (Address) msg.obj;
                final StringBuffer addressStringSB = new StringBuffer();

                for (int i = 0; address != null && i < address.getMaxAddressLineIndex() + 1; i++) {
                    addressStringSB.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
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
            else if (MESSAGE_UPDTAE_MAPVIEW == msg.what) {
                Log.d(TAG, "mapview invalidate");
                m_mapView.invalidate();
            }

        }

    };

    class UserTouchedOverlayView extends Overlay {

        private final Point screenPoint = new Point();
        private final Calendar cal = Calendar.getInstance();

        final private View m_timeView;
        final private TextView m_mapDate;
        final private TextView m_sunriseTextview;
        final private TextView m_sunsetTextview;
        final private TextView m_moonriseTextview;
        final private TextView m_moonsetTextview;
        private RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        private Timer searchAddressTimer = new Timer();
        private GeoPoint geoPoint;

        public UserTouchedOverlayView() {
            this(null);
        }

        public UserTouchedOverlayView(GeoPoint geoPoint) {
            super();
            final LayoutInflater inflater = (LayoutInflater) getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            this.geoPoint = geoPoint;

            m_timeView = inflater.inflate(com.bk.sunwidgt.R.layout.map_showtime, null);
            m_mapDate = (TextView) m_timeView.findViewById(com.bk.sunwidgt.R.id.map_date);
            m_sunriseTextview = (TextView) m_timeView
                    .findViewById(com.bk.sunwidgt.R.id.map_sunrise);
            m_sunsetTextview = (TextView) m_timeView.findViewById(com.bk.sunwidgt.R.id.map_sunset);
            m_moonriseTextview = (TextView) m_timeView
                    .findViewById(com.bk.sunwidgt.R.id.map_moonrise);
            m_moonsetTextview = (TextView) m_timeView
                    .findViewById(com.bk.sunwidgt.R.id.map_moonset);
        }

        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            // draw red circle
            if (null == geoPoint) {
                mapView.getProjection().toPixels(m_geopoint, screenPoint);
            }
            else {
                mapView.getProjection().toPixels(geoPoint, screenPoint);
            }

            if (null == geoPoint) {
                canvas.drawCircle(screenPoint.x, screenPoint.y, CIRCLE_RADIOUS, REDPAINT);
            }
            else {
                canvas.drawCircle(screenPoint.x, screenPoint.y, CIRCLE_RADIOUS, BLUEPAINT);
            }
            cal.setTimeInMillis(m_showTime);

            final SunCalculator.SunriseSunset sunTime = SunCalculator.getSunriseSunset(cal,
                    (double) m_geopoint.getLatitudeE6() / DOUBLE_1E6,
                    (double) m_geopoint.getLongitudeE6() / DOUBLE_1E6, false);

            final MoonCalculator.MoonriseMoonset moonTime = MoonCalculator.getMoonriseMoonset(cal,
                    (double) m_geopoint.getLatitudeE6() / 1E6,
                    (double) m_geopoint.getLongitudeE6() / 1E6);

            m_mapDate.setText(SunWidget.fmtDate.format(cal.getTime()));
            m_sunriseTextview.setText(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_sunrise)
                    + SunWidget.fmtTime.format(sunTime.sunrise));
            m_sunsetTextview.setText(SunMapActivity.this.getResources().getString(
                    com.bk.sunwidgt.R.string.map_sunset)
                    + SunWidget.fmtTime.format(sunTime.sunset));
            if (null == moonTime.moonrise) {
                m_moonriseTextview.setText(SunMapActivity.this.getResources().getString(
                        com.bk.sunwidgt.R.string.map_moonrise)
                        + SunWidget.notimeString);
            }
            else {
                m_moonriseTextview.setText(SunMapActivity.this.getResources().getString(
                        com.bk.sunwidgt.R.string.map_moonrise)
                        + SunWidget.fmtTime.format(moonTime.moonrise));
            }

            if (null == moonTime.moonset) {
                m_moonsetTextview.setText(SunMapActivity.this.getResources().getString(
                        com.bk.sunwidgt.R.string.map_moonset)
                        + SunWidget.notimeString);
            }
            else {
                m_moonsetTextview.setText(SunMapActivity.this.getResources().getString(
                        com.bk.sunwidgt.R.string.map_moonset)
                        + SunWidget.fmtTime.format(moonTime.moonset));
            }

            // Bitmap timeTextViewBitmap = m_timeView.getDrawingCache();
            float y = screenPoint.y + CIRCLE_RADIOUS / 2;
            float x = screenPoint.x - CIRCLE_RADIOUS / 2;

            params.leftMargin = (int) x;
            params.topMargin = (int) y;
            m_mainLayout.removeView(m_timeView);
            m_mainLayout.addView(m_timeView, params);

            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {

            if (null == geoPoint && MotionEvent.ACTION_DOWN == event.getAction()) {
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
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(com.bk.sunwidgt.R.layout.map_activity);

        // Initialize Map objects
        m_geocoder = new Geocoder(this);
        m_mapView = (MapView) findViewById(com.bk.sunwidgt.R.id.mapview);
        m_mapController = m_mapView.getController();
        m_addressView = (TextView) findViewById(com.bk.sunwidgt.R.id.map_address);
        m_bookmarkButton = (Button) findViewById(com.bk.sunwidgt.R.id.map_bookmark);

        m_dataPickerEnableCheckbox = (CheckBox) findViewById(com.bk.sunwidgt.R.id.map_datepicker_checkbox);
        m_datePicker = (DatePicker) findViewById(com.bk.sunwidgt.R.id.map_datepicker);

        m_mainLayout = (RelativeLayout) findViewById(com.bk.sunwidgt.R.id.map_relative_layout);

        // Retrieve start location
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
        String startAddress = null;
        if (getIntent().hasExtra(LOCATION_ADDRESS)) {
            startAddress = getIntent().getStringExtra(LOCATION_ADDRESS);
            Log.d(TAG, "Get address from intent");
        }
        else if (savedInstanceState != null && savedInstanceState.containsKey(LOCATION_ADDRESS)) {
            startAddress = savedInstanceState.getString(LOCATION_ADDRESS);
            Log.d(TAG, "Get address from bundle");
        }
        else {
            startAddress = "";
        }

        Log.i(TAG, "startLocation=" + startLocation);
        Log.i(TAG, "startAddress=" + startAddress);

        m_addressView.setText(startAddress);

        // Center and zoom to start location
        m_geopoint = new GeoPoint((int) (startLocation.getLatitude() * 1E6),
                (int) (startLocation.getLongitude() * 1E6));
        m_mapController.animateTo(m_geopoint);
        m_mapController.setZoom(DEFAULT_ZOOM_LEVEL);

        // Add time layer
        UserTouchedOverlayView mapOverlay = new UserTouchedOverlayView();
        List<Overlay> listOfOverlays = m_mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);

        if (getIntent().hasExtra(START_LOCATION_EXTRAS)) {
            Log.i(TAG, "Found " + START_LOCATION_EXTRAS);
            Parcelable[] extraLocations = getIntent()
                    .getParcelableArrayExtra(START_LOCATION_EXTRAS);
            for (Parcelable par : extraLocations) {
                final Location loc = (Location) par;
                final GeoPoint geopoint = new GeoPoint((int) (loc.getLatitude() * 1E6),
                        (int) (loc.getLongitude() * 1E6));
                listOfOverlays.add(new UserTouchedOverlayView(geopoint));

            }
        }

        m_dataPickerEnableCheckbox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        m_datePicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    }
                });

        m_datePicker.getCalendarView().setOnDateChangeListener(
                new CalendarView.OnDateChangeListener() {

                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month,
                            int dayOfMonth) {
                        final Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        m_showTime = cal.getTimeInMillis();
                        m_mapView.invalidate();
                        Log.d(TAG, "onSelectedDayChange cal=" + cal);
                    }
                });

        m_showTime = Calendar.getInstance().getTimeInMillis();
    }

    // Call when bookmark is clicked
    public void doBookmark(View view) {
        Log.d(TAG, "doBookmark");
        if (m_geopoint != null) {
            final Location loc = new Location(getClass().getName());
            loc.setLatitude((double) m_geopoint.getLatitudeE6() / DOUBLE_1E6);
            loc.setLongitude((double) m_geopoint.getLongitudeE6() / DOUBLE_1E6);

            BookmarkFragment dialog = BookmarkFragment.newInstance(loc, m_addressView.getText()
                    .toString());
            dialog.show(getFragmentManager(), null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        if (null == m_menuCreator) {
            m_menuCreator = new OptionMenuCreator();
        }
        return m_menuCreator.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null == m_menuCreator) {
            m_menuCreator = new OptionMenuCreator();
        }
        return m_menuCreator.onOptionsItemSelected(this, item);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        final SharedPreferences.Editor perf = getSharedPreferences(getClass().getName(),
                Context.MODE_PRIVATE).edit();

        try {
            perf.putBoolean(PERF_DATEPICKER_ENABLE, m_dataPickerEnableCheckbox.isChecked());
        } finally {
            perf.commit();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        final SharedPreferences perf = getSharedPreferences(getClass().getName(),
                Context.MODE_PRIVATE);
        m_dataPickerEnableCheckbox.setChecked(perf.getBoolean(PERF_DATEPICKER_ENABLE, false));

        m_mapView.invalidate();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
