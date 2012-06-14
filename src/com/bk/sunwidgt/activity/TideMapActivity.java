package com.bk.sunwidgt.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bk.sunwidgt.activity.RainMapActivity.UserTouchedOverlayView;
import com.bk.sunwidgt.adapter.TideAdapterData;
import com.bk.sunwidgt.adapter.TideStoreUtil;
import com.bk.sunwidgt.task.TideParserTask;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TideMapActivity extends MapActivity{
    public final static int MESSAGE_LOAD_TIDEATA = 5;
    public final static int MESSAGE_SET_CENTER = 6;
    public final static int MESSAGE_SET_UPDATE_STATUS = 7;

    public final static double DOUBLE_1E6 = 1E6;
    
    public final static int DEFAULT_ZOOM_LEVEL = 8;

    private final static ExecutorService FIX_THREAD_EXECUTOR = Executors.newFixedThreadPool(5);
    private final static int CIRCLE_RADIOUS = 6;
    private final static SimpleDateFormat fmtTideDate = new SimpleDateFormat("MM/dd");
    private final static SimpleDateFormat fmtTideTime = new SimpleDateFormat("HH:mm");
    private final Map<String,TideAdapterData> m_TideMap = new HashMap<String,TideAdapterData>();
    private final static String TAG = "Sun" + TideMapActivity.class.getSimpleName();
    private final static String TIDES_RESOURCE_NAME = "tide_urls";
    private final static Paint BLACKPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint REDPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private MapController m_mapController;
    private MapView m_mapView;
    private DatePicker m_datePicker;
    private CheckBox m_dataPickerEnableCheckbox;
    private RelativeLayout m_mainLayout;
    private TextView m_updateStatusView;
    private long m_showTime;
    
    private OptionMenuCreator m_menuCreator;
    
    static {
        BLACKPAINT.setColor(Color.BLACK);
        BLACKPAINT.setAlpha(128);
        REDPAINT.setColor(Color.RED);
        REDPAINT.setAlpha(128);
    }
    
    private final Handler m_handler = new Handler() {

        public void handleMessage(Message msg) {
            
            if(MESSAGE_LOAD_TIDEATA == msg.what) {
                
                final String locationName = (String) msg.obj;
                final TideAdapterData tideData = TideStoreUtil.loadTide(TideMapActivity.this,locationName);
                if(null == tideData) {
                    Log.w(TAG, "unable to load locationName=" + locationName);
                }
                else {
                    m_TideMap.remove(locationName);
                    m_TideMap.put(locationName, tideData);
                }
            }
            else if(MESSAGE_SET_UPDATE_STATUS == msg.what) {
                m_updateStatusView.setText((String) msg.obj);
            }
            
        }
    };
    
    class UserTouchedOverlayView extends Overlay {
        private final Point screenPoint = new Point();
        private final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 120);
        private final View tideView;
        private final TextView tideDateView;
        private final ListView tideTimeListView;
        private final ArrayAdapter<String> m_tideTimeAdapter = new ArrayAdapter<String>(TideMapActivity.this,com.bk.sunwidgt.R.layout.map_tidetime_item);
        private String selectedLocationName;
        private TideAdapterData selectedData;
        public UserTouchedOverlayView() {
            super();
            
            final LayoutInflater inflater = (LayoutInflater) getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            
            tideView = inflater.inflate(com.bk.sunwidgt.R.layout.map_tidelocation_view, null);
            tideDateView = (TextView) tideView.findViewById(com.bk.sunwidgt.R.id.location_date);
            tideTimeListView = (ListView) tideView.findViewById(com.bk.sunwidgt.R.id.tide_time_list);
            
            tideTimeListView.setAdapter(m_tideTimeAdapter);
        }
        
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);
            
            for(Entry<String,TideAdapterData> entry : m_TideMap.entrySet()) {
                final String locationName = entry.getKey();
                final TideAdapterData tideData = entry.getValue();
                final GeoPoint geoPoint = new GeoPoint((int)(tideData.m_location.getLatitude() * DOUBLE_1E6),(int) (tideData.m_location.getLongitude() * DOUBLE_1E6));

                mapView.getProjection().toPixels(geoPoint, screenPoint);
                BLACKPAINT.setStyle(Paint.Style.FILL);
                canvas.drawCircle(screenPoint.x, screenPoint.y, CIRCLE_RADIOUS, locationName.equals(selectedLocationName) ? REDPAINT : BLACKPAINT);
            
                if(locationName.equals(selectedLocationName)) {
                    m_tideTimeAdapter.clear();
                    if(selectedData != null) {
                        final Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(m_showTime);
                        
                        tideDateView.setText(fmtTideDate.format(cal.getTime()));

                        final Map<Long,Integer> heightMap = selectedData.getHeights(cal.getTime());
            
                        if(heightMap != null) {
                            for(Entry<Long,Integer> tideTimeEntry : heightMap.entrySet()) {
                                cal.setTimeInMillis(tideTimeEntry.getKey());
                                m_tideTimeAdapter.add(fmtTideTime.format(cal.getTime()) + " " + tideTimeEntry.getValue());
                            }
                        }
                    }
                    m_tideTimeAdapter.notifyDataSetChanged();
                    
                    //Show tideView
                    float y = screenPoint.y + CIRCLE_RADIOUS / 2;
                    float x = screenPoint.x - CIRCLE_RADIOUS / 2;

                    params.leftMargin = (int) x;
                    params.topMargin = (int) y;
                    m_mainLayout.removeView(tideView);
                    m_mainLayout.addView(tideView, params);
                }
            }
            return true;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            final GeoPoint geopoint =  mapView.getProjection().fromPixels((int) event.getX(),
                    (int) event.getY());
            
            final double lat = (double) geopoint.getLatitudeE6() / DOUBLE_1E6;
            final double lng = (double) geopoint.getLongitudeE6() / DOUBLE_1E6;

            double minDist = Double.MAX_VALUE;
            String minlocationName = null;
            TideAdapterData minDistData = null;
            for(Entry<String,TideAdapterData> entry : m_TideMap.entrySet()) {

                final TideAdapterData tideData = entry.getValue();
                final Location location = tideData.m_location;
                final double latDiff = location.getLatitude() - lat;
                final double lngDiff = location.getLongitude() - lng;
                
                final double dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
                if(dist < minDist) {
                    minDist = dist;
                    minDistData = tideData;
                    minlocationName = entry.getKey();
                }
            }
            
            selectedLocationName = minlocationName;
            selectedData = minDistData;
            
            return false;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(com.bk.sunwidgt.R.layout.map_tideactivity);
        
        m_updateStatusView = (TextView) findViewById(com.bk.sunwidgt.R.id.tide_update_status);
        m_mapView = (MapView) findViewById(com.bk.sunwidgt.R.id.mapview);
        m_mapController = m_mapView.getController();
        m_mainLayout = (RelativeLayout) findViewById(com.bk.sunwidgt.R.id.map_relative_layout);
        m_dataPickerEnableCheckbox = (CheckBox) findViewById(com.bk.sunwidgt.R.id.map_datepicker_checkbox);
        m_datePicker = (DatePicker) findViewById(com.bk.sunwidgt.R.id.map_datepicker);
        m_dataPickerEnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
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
        
        final UserTouchedOverlayView userTouchMapOverlay = new UserTouchedOverlayView();

        List<Overlay> listOfOverlays = m_mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(userTouchMapOverlay);
        
        try {
            
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(TIDES_RESOURCE_NAME)));
            
            while(br.ready()) {
                String line = br.readLine();
                if(null == line) {
                    break;
                }
                
                final String[] words = line.split("\t");
                final double lat = Double.parseDouble(words[0]);
                final double lng  = Double.parseDouble(words[1]);
                final String url = words[2];
                final Location location = new Location(TIDES_RESOURCE_NAME);
                location.setLatitude(lat);
                location.setLongitude(lng);
                Log.d(TAG, "line="+ line);
                
                new TideParserTask(this,m_handler,location,new URL(url)).executeOnExecutor(FIX_THREAD_EXECUTOR);
            }
        } catch (Exception e) {
            Log.e(TAG, "",e);
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
    protected boolean isRouteDisplayed() {
        return false;
    }
    
}
