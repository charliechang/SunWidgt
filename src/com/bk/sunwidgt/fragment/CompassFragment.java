
package com.bk.sunwidgt.fragment;

import java.text.NumberFormat;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CompassFragment extends Fragment {
    public final static int NO_AZ = -1;
    public final static String SUNRISE_AZ = CompassFragment.class.getName() + ".sunrise_az";
    public final static String SUNSET_AZ = CompassFragment.class.getName() + ".sunset_az";
    public final static String MOONRISE_AZ = CompassFragment.class.getName() + ".moonrise_az";
    public final static String MOONSET_AZ = CompassFragment.class.getName() + ".moonset_az";
    
    private final static String TAG = "Sun" + CompassFragment.class.getSimpleName();
    private SensorManager mSensorManager;
    // private Sensor mSensor;
    private CompassView mView;
    private int m_sunriseAz = NO_AZ;
    private int m_sunsetAz = NO_AZ;
    private int m_moonriseAz = NO_AZ;
    private int m_moonsetAz = NO_AZ;
    // private float[] mValues = new float[]
    // {Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE};
    private float[] mags = null;
    private float[] accels = null;
    private float[] R = new float[9];
    private float[] outR = new float[9];
    private float[] I = new float[9];
    private float[] values = new float[3];
    private float azimuth = 0.0f;
    private static int bearingIdx = 0;
    private static final int[] bearingArray = new int[10];
    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mags = event.values.clone();

            }
            else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accels = event.values.clone();
            }
            
            if (mags != null && accels != null) {
                SensorManager.getRotationMatrix(R, null, accels, mags);
                // Correct if screen is in Landscape
                //SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,
                //        SensorManager.AXIS_Z, outR);

                SensorManager.getOrientation(R, values);
                //azimuth = (float) Math.round((Math.toDegrees(values[0]))*7)/7;
                //azimuth = ( azimuth + 360)%360; 
                //here is inclination. The problem is just the same with compass
                //inclination=-Math.round((float) (values[1]*(360/(2*Math.PI))));

                //other code to update my view
                //in azimuth i have the degree value. It changes continuously
                //even if i aim still the same direction             
                double rawBearing = -values[0] * (180.0 / Math.PI);
                if(rawBearing < 0.0) {
                    rawBearing += 360.0;
                }
                bearingArray[bearingIdx] = (int) rawBearing;
                bearingIdx = ++bearingIdx % bearingArray.length;
                int total = 0;
                for(int b : bearingArray) {
                    total += b;
                }
                azimuth = (float) total / (float) bearingArray.length;
                mView.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };
    
    public static CompassFragment newInstance(int sunrise_az,int sunset_az,int moonrise_az,int moonset_az) {
        final CompassFragment fragment = new CompassFragment();
        final Bundle b = new Bundle();
        
        b.putInt(SUNRISE_AZ, sunrise_az);
        b.putInt(SUNSET_AZ, sunset_az);
        b.putInt(MOONRISE_AZ, moonrise_az);
        b.putInt(MOONSET_AZ, moonset_az);
        
        fragment.setArguments(b);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        
        Bundle b = getArguments();
        if(b != null && b.containsKey(SUNRISE_AZ) && b.containsKey(SUNSET_AZ)) {
            m_sunriseAz = b.getInt(SUNRISE_AZ,NO_AZ);
            m_sunsetAz = b.getInt(SUNSET_AZ,NO_AZ);
            m_moonriseAz = b.getInt(MOONRISE_AZ,NO_AZ);
            m_moonsetAz = b.getInt(MOONSET_AZ,NO_AZ);
        }
        else {
            final Intent intent = getActivity().getIntent();
            m_sunriseAz = intent.getIntExtra(SUNRISE_AZ,NO_AZ);
            m_sunsetAz = intent.getIntExtra(SUNSET_AZ,NO_AZ);
            m_moonriseAz = intent.getIntExtra(MOONRISE_AZ,NO_AZ);
            m_moonsetAz = intent.getIntExtra(MOONSET_AZ,NO_AZ);            
        }
        
        Log.d(TAG, "m_sunriseAz="+ m_sunriseAz + " m_sunsetAz=" + m_sunsetAz + " m_moonriseAz=" + m_moonriseAz + " m_moonsetAz=" + m_moonsetAz);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new CompassView(getActivity());
        return mView;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        /*
         * final boolean enabled = mSensorManager.registerListener(mListener,
         * mSensor, SensorManager.SENSOR_DELAY_NORMAL);
         */
        final boolean magnSupported = mSensorManager.registerListener(mListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        final boolean acceSupported = mSensorManager.registerListener(mListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        if (!magnSupported) {
            Log.w(TAG, "TYPE_MAGNETIC_FIELD sensor is not enabled");
        }

        if (!acceSupported) {
            Log.w(TAG, "TYPE_ACCELEROMETER sensor is not enabled");
        }

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestory");
        super.onDestroy();
    }
    
    private final static Path constructPole(Path p,int baseHeight) {
        if(null == p) {
            p = new Path();
        }
        
        p.moveTo(0, 0);
        p.lineTo(0, -baseHeight);
        p.lineTo(-baseHeight / 5, -baseHeight * 3 / 4);
        p.lineTo(baseHeight / 5, -baseHeight * 3 / 4);
        p.lineTo(0, -baseHeight);
        return p;
    }
    
    private final static Path constructPath(Path p,int baseHeight) {
        if(null == p) {
            p = new Path();
        }
        
        p.moveTo(0, -baseHeight);
        p.lineTo(-baseHeight / 5, baseHeight);
        p.lineTo(0, baseHeight * 3 / 4);
        p.lineTo(baseHeight / 5, baseHeight);
        p.close();
        
        return p;
    }
    
    private final static void drawPath(Canvas canvas,Path p, int az,Paint paint) {
        final Path path = new Path(p);
        final Matrix roateMatrix = new Matrix();
        roateMatrix.preRotate(az);
        path.transform(roateMatrix);
        canvas.drawPath(path,paint);
    }

    private class CompassView extends View {

        private Paint mPaint = new Paint();
        private Path mNorthPath = new Path();
        private Path mSunrisePath = new Path();
        private Path mSunsetPath = new Path();
        private Path mMoonrisePath = new Path();
        private Path mMoonsetPath = new Path();
        private boolean mAnimate;
        private String m_sunriseString;
        private String m_sunsetString;
        private String m_moonriseString;
        private String m_moonsetString;
        
        private int m_baseHeight;
        private final Paint mDebugTextPaint;
        private final StringBuffer mDebugString = new StringBuffer();
        private NumberFormat mdebugValueFormatter = NumberFormat.getInstance();
        
        public CompassView(Context context) {
            super(context);

            m_sunriseString = CompassFragment.this.getActivity().getResources().getString(com.bk.sunwidgt.R.string.compass_sunrise);
            m_sunsetString = CompassFragment.this.getActivity().getResources().getString(com.bk.sunwidgt.R.string.compass_sunset);
            m_moonriseString = CompassFragment.this.getActivity().getResources().getString(com.bk.sunwidgt.R.string.compass_moonrise);
            m_moonsetString = CompassFragment.this.getActivity().getResources().getString(com.bk.sunwidgt.R.string.compass_moonset);

            mDebugTextPaint = new Paint();
            mDebugTextPaint.setColor(Color.RED);
            mDebugTextPaint.setTextSize(15.0f);
            mdebugValueFormatter.setMaximumFractionDigits( 2 );
                       
            //buildCompassBitmap();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Log.d(TAG, "onDraw");
            canvas.drawColor(Color.BLACK);
            
            if(0 == m_baseHeight) {
                m_baseHeight = Math.min(canvas.getHeight(), canvas.getWidth()) / 6;
                // Construct a wedge-shaped path
                mNorthPath = constructPath(null,m_baseHeight);
                mSunrisePath = constructPole(null,m_baseHeight);
                mSunsetPath = constructPole(null,m_baseHeight);
                mMoonrisePath = constructPole(null,m_baseHeight);
                mMoonsetPath = constructPole(null,m_baseHeight);
            }
            
            Log.d(TAG, "m_baseHeight=" + m_baseHeight);
            

            mDebugString.delete(0, mDebugString.length());
            mDebugString.append(mdebugValueFormatter.format(azimuth));

            canvas.drawText(mDebugString.toString(), 0.0f, 20.0f, mDebugTextPaint);
            
             Paint paint = mPaint; 
              
             paint.setAntiAlias(true);
             paint.setColor(Color.BLUE);
             paint.setStyle(Paint.Style.FILL);
             int w = canvas.getWidth(); 
             int h = canvas.getHeight(); 
             int cx = w / 2 - m_baseHeight; 
             int cy = h / 2 - m_baseHeight; 
             canvas.translate(cx, cy); 
              
             //canvas.rotate(azimuth); 
             //draw azimuth
             mPaint.setColor(Color.WHITE);
             canvas.rotate(azimuth);
             final int STEP_AZ = 30;
             for(int i = 0;i < 360 / STEP_AZ;i++) {
                 final String azString = String.valueOf(i * STEP_AZ);
                 canvas.drawText(azString, 0, -m_baseHeight - mPaint.getTextSize() - ((int) mPaint.measureText(azString) >> 1), mPaint);
                 canvas.rotate(STEP_AZ);
             }
             mPaint.setColor(Color.GRAY);
             
             //draw noth
             drawPath(canvas, mNorthPath, (int) 0, mPaint);
             
             //draw sunrise
             if(m_sunriseAz != NO_AZ) {
                 paint.setStyle(Paint.Style.STROKE);
                 mPaint.setColor(Color.YELLOW);
                 canvas.rotate(m_sunriseAz);
                 canvas.drawText(m_sunriseString, 0, -m_baseHeight - mPaint.getTextSize() * 2 - ((int) mPaint.measureText(m_sunriseString) >> 1) , mPaint);
                 canvas.rotate(-m_sunriseAz);
                 drawPath(canvas, mSunrisePath, m_sunriseAz % 360, mPaint);
                 paint.setStyle(Paint.Style.FILL);
                 drawPath(canvas, mSunrisePath, m_sunriseAz % 360, mPaint);
             }
             //draw sunset
             if(m_sunsetAz != NO_AZ) {
                 paint.setStyle(Paint.Style.STROKE);
                 mPaint.setColor(Color.RED);
                 canvas.rotate(m_sunsetAz);
                 canvas.drawText(m_sunsetString, 0, -m_baseHeight - mPaint.getTextSize() * 2 - ((int) mPaint.measureText(m_sunsetString) >> 1) , mPaint);
                 canvas.rotate(-m_sunsetAz);
                 drawPath(canvas, mSunsetPath, m_sunsetAz % 360, mPaint);
                 paint.setStyle(Paint.Style.FILL);
                 drawPath(canvas, mSunsetPath, m_sunsetAz % 360, mPaint);
             }            
             if(m_moonriseAz != NO_AZ) {
                 paint.setStyle(Paint.Style.STROKE);
                 mPaint.setColor(Color.GREEN);
                 canvas.rotate(m_moonriseAz);
                 canvas.drawText(m_moonriseString, 0, -m_baseHeight - mPaint.getTextSize() * 2 - ((int) mPaint.measureText(m_moonriseString) >> 1) , mPaint);
                 canvas.rotate(-m_moonriseAz);
                 drawPath(canvas, mMoonrisePath, m_moonriseAz % 360, mPaint);
                 paint.setStyle(Paint.Style.FILL);
                 drawPath(canvas, mMoonrisePath, m_moonriseAz % 360, mPaint);
             }
             //draw sunset
             if(m_moonsetAz != NO_AZ) {
                 paint.setStyle(Paint.Style.STROKE);
                 mPaint.setColor(Color.WHITE);
                 canvas.rotate(m_moonsetAz);
                 canvas.drawText(m_moonsetString, 0, -m_baseHeight - mPaint.getTextSize() * 2 - ((int) mPaint.measureText(m_moonsetString) >> 1) , mPaint);
                 canvas.rotate(-m_moonsetAz);
                 drawPath(canvas, mMoonsetPath, m_moonsetAz % 360, mPaint);
                 paint.setStyle(Paint.Style.FILL);
                 drawPath(canvas, mMoonsetPath, m_moonsetAz % 360, mPaint);
             }            

             
        }

        @Override
        protected void onAttachedToWindow() {
            mAnimate = true;
            Log.d(TAG, "onAttachedToWindow. mAnimate=" + mAnimate);
            super.onAttachedToWindow();

        }

        @Override
        protected void onDetachedFromWindow() {
            mAnimate = false;
            Log.d(TAG, "onDetachedFromWindow. mAnimate=" + mAnimate);
            super.onDetachedFromWindow();
        }

    }

}
