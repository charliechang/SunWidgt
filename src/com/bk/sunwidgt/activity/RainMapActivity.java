package com.bk.sunwidgt.activity;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bk.sunwidgt.SunWidget;
import com.bk.sunwidgt.activity.SunMapActivity.UserTouchedOverlayView;
import com.bk.sunwidgt.adapter.RainAdapter;
import com.bk.sunwidgt.adapter.RainAdapterData;
import com.bk.sunwidgt.adapter.RainStoreUtil;
import com.bk.sunwidgt.fragment.ProgressFragment;
import com.bk.sunwidgt.lib.MoonCalculator;
import com.bk.sunwidgt.lib.SunCalculator;
import com.bk.sunwidgt.task.RainParserTask;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RainMapActivity extends MapActivity{
    private final static String TAG = "Sun" + RainMapActivity.class.getSimpleName();
    
    public final static int MESSAGE_SHOW_PROGRESS = 4;
    public final static int MESSAGE_CLOSE_PROGRESS = 8;
    public final static int MESSAGE_LOAD_RAINDATA = 5;
    public final static int MESSAGE_SET_CENTER = 6;
    public final static int MESSAGE_SET_PROGRESS_MESSAGE = 7;
    
    public final static double DOUBLE_1E6 = 1E6;
    private final static int CIRCLE_RADIOUS = 2;
    public final static int DEFAULT_ZOOM_LEVEL = 8;
    public final static int MIN_ALPHA = 8;
    
    private final static Paint REDPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint BLUEPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final static Paint TEXTPAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    static {
        REDPAINT.setColor(Color.RED);
        BLUEPAINT.setColor(Color.BLUE);
        TEXTPAINT.setColor(Color.BLACK);
        TEXTPAINT.setTextSize(14);
    }
    private final static ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private final AtomicReference<RainAdapterData[]> m_rainDataList = new AtomicReference<RainAdapterData[]>(null);
    private ProgressFragment m_progressDialog;
    private MapController m_mapController;
    private MapView m_mapView;
    private RelativeLayout m_mainLayout;
    private OptionMenuCreator m_menuCreator;
    
    private Handler m_Handler = new Handler() {

        public void handleMessage(Message msg) {
            
                if(MESSAGE_SHOW_PROGRESS == msg.what) {
                    if (null == m_progressDialog) {
                        m_progressDialog = ProgressFragment.newInstance(
                                com.bk.sunwidgt.R.string.progress_message);
                    }
                    m_progressDialog.show(getFragmentManager(), null);
                }
                else if(MESSAGE_LOAD_RAINDATA == msg.what) {
                    m_rainDataList.set(RainStoreUtil.loadRains(RainMapActivity.this));
                    m_Handler.obtainMessage(MESSAGE_SET_CENTER).sendToTarget();
                    
                    m_mapView.invalidate();
                }
                else if(MESSAGE_SET_CENTER == msg.what) {
                    final RainAdapterData[] rainDataList = m_rainDataList.get();
                    double sumLat = 0.0;
                    double sumLng = 0.0;
                    if(rainDataList != null && rainDataList.length > 0) {
                        for(RainAdapterData rainData : rainDataList) {
                            final Location location = rainData.getLocation();
                            sumLat += location.getLatitude();
                            sumLng += location.getLongitude();
                        }
                        
                        final GeoPoint geoPoint = new GeoPoint((int)(sumLat * DOUBLE_1E6 / (double) rainDataList.length),(int) (sumLng * DOUBLE_1E6 / (double)rainDataList.length ));
                        m_mapController.animateTo(geoPoint);
                        m_mapController.setZoom(DEFAULT_ZOOM_LEVEL);
                    
                    }
                    
                }
                else if(MESSAGE_SET_PROGRESS_MESSAGE == msg.what) {
                    if (null == m_progressDialog) {
                        m_progressDialog = ProgressFragment.newInstance(
                                com.bk.sunwidgt.R.string.progress_message);
                    }
                    m_progressDialog.setMessage(msg.obj.toString());
                    
                }
                else if(MESSAGE_CLOSE_PROGRESS == msg.what) {
                    if(m_progressDialog != null) {
                        m_progressDialog.dismissAllowingStateLoss();
                    }
                }
        
            }
            
            
        };
        
        class UserTouchedOverlayView extends Overlay {

            private final Point screenPoint = new Point();
            private final ListView listView;
            private final RainAdapter rainAdapter = new RainAdapter(RainMapActivity.this);
            private RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, 300);

            public UserTouchedOverlayView() {
                super();
                listView = (ListView) RainMapActivity.this.findViewById(com.bk.sunwidgt.R.id.rain_location_list);
                listView.setAdapter(rainAdapter);
            }

            public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
                super.draw(canvas, mapView, shadow);
                
                final RainAdapterData[] rainDataList = m_rainDataList.get();
                if(null == rainDataList) {
                    return true;
                }
                else {
                    
                    for(RainAdapterData rainData : rainDataList) {
                        final Location location = rainData.getLocation();
                        final GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude() * DOUBLE_1E6),(int) (location.getLongitude() * DOUBLE_1E6));
                        
                        //First, draw a circle according to rain
                        final float sumRainHour = rainData.sumRainHour(12);
                        if(sumRainHour > 0.0f) {
                            final int alphaByrainHours = Math.max(0, Math.min(255,(int) Math.pow((double) Math.ceil(sumRainHour), 1.3)));
                            
                            mapView.getProjection().toPixels(geoPoint, screenPoint);
                            BLUEPAINT.setAlpha(alphaByrainHours);
                            BLUEPAINT.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(screenPoint.x, screenPoint.y, Math.max(CIRCLE_RADIOUS, Math.min(DEFAULT_ZOOM_LEVEL, m_mapView.getZoomLevel() - 3)), BLUEPAINT);
                            BLUEPAINT.setAlpha(255);
                            BLUEPAINT.setStyle(Paint.Style.STROKE);
                            canvas.drawCircle(screenPoint.x, screenPoint.y, Math.max(CIRCLE_RADIOUS, Math.min(DEFAULT_ZOOM_LEVEL, m_mapView.getZoomLevel() - 3)), BLUEPAINT);
                            
                        }
                    }
                }

                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event, MapView mapView) {
                
                final RainAdapterData[] rainDataList = m_rainDataList.get();
                if(null == rainDataList) {
                    return false;
                }
                
                
                final GeoPoint geopoint =  mapView.getProjection().fromPixels((int) event.getX(),
                        (int) event.getY());
                
                final double lat = (double) geopoint.getLatitudeE6() / DOUBLE_1E6;
                final double lng = (double) geopoint.getLongitudeE6() / DOUBLE_1E6;

                double minDist = Double.MAX_VALUE;
                final TreeMap<Float,RainAdapterData> minDistMap = new TreeMap<Float,RainAdapterData>();
                //final List<RainAdapterData> minDistList = new ArrayList<RainAdapterData>(5);
                
                for(RainAdapterData rainData : rainDataList) {
                    final Location location = rainData.getLocation();
                    final double latDiff = location.getLatitude() - lat;
                    final double lngDiff = location.getLongitude() - lng;
                    
                    final double dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
                    
                    if(dist < minDist) {
                        minDistMap.clear();
                        minDistMap.put(rainData.sumRainHour(12),rainData);
                        minDist = dist;
                    }
                    else if(dist == minDist) {
                        minDistMap.put(rainData.sumRainHour(12),rainData);
                    }
                    
                }
                
                if(!minDistMap.isEmpty()) {
                    rainAdapter.clear();
                    for(Entry<Float,RainAdapterData> entry : minDistMap.descendingMap().entrySet()) {
                        rainAdapter.add(entry.getValue());
                    }
                }


                return false;
            }

        }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(com.bk.sunwidgt.R.layout.map_rainactivity);
        
        m_mapView = (MapView) findViewById(com.bk.sunwidgt.R.id.mapview);
        m_mapController = m_mapView.getController();
        m_mainLayout = (RelativeLayout) findViewById(com.bk.sunwidgt.R.id.map_relative_layout);
        
        // Add time layer
        final UserTouchedOverlayView userTouchMapOverlay = new UserTouchedOverlayView();

        List<Overlay> listOfOverlays = m_mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(userTouchMapOverlay);
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
    
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
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
        
        
        try {

            new RainParserTask(this,new URL("http://www.cwb.gov.tw/V7/observe/rainfall/Rain_Hr/22.htm"),m_Handler).executeOnExecutor(SINGLE_THREAD_EXECUTOR);
        }
        catch(Exception e) {
            Log.e(TAG, "failed", e);
        }
        
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


}
