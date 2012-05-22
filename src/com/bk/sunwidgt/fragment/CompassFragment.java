
package com.bk.sunwidgt.fragment;

import java.text.NumberFormat;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private final static String TAG = "Sun" + CompassFragment.class.getSimpleName();
    private SensorManager mSensorManager;
    // private Sensor mSensor;
    private CompassView mView;
    // private float[] mValues = new float[]
    // {Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE};
    private float[] m_rotationMatrix = new float[16];
    // private float[] m_remappedR = new float[16];
    private float[] m_orientation = new float[4];

    private float[] m_lastMagFields;
    private float[] m_lastAccels;

    private float mYaw = 0.0f;
    private float mPitch = 0.0f;
    private float mRoll = 0.0f;

    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                if (null == m_lastMagFields) {
                    m_lastMagFields = new float[3];
                }

                System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);

                if (null == m_lastAccels) {
                    Log.d(TAG, "m_lastAccels=null");
                    return;
                }
                if (!SensorManager.getRotationMatrix(m_rotationMatrix, null, m_lastMagFields,
                        m_lastAccels)) {
                    Log.w(TAG, "SensorManager.getRotationMatrix return false");
                    return;
                }

                SensorManager.getOrientation(m_rotationMatrix, m_orientation);

                mYaw = m_orientation[0] * 57.2957795f;
                mPitch = m_orientation[1] * 57.2957795f;
                mRoll = m_orientation[2] * 57.2957795f;

                //Log.d(TAG, "mYaw=" + mYaw + ", mPitch=" + mPitch + " ,mRoll=" + mRoll);

                // SensorManager.getOrientation(null, mValues);
                if (mView != null) {
                    mView.invalidate();
                }

            }
            else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (null == m_lastAccels) {
                    m_lastAccels = new float[3];
                }
                System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
                
                //Log.d(TAG, "m_lastAccels=" + m_lastAccels[0] + "," + m_lastAccels[1] + "," + m_lastAccels[2]);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ALL);

        // if(null == mSensor) {
        // Log.d(TAG, "mSensor=null");
        // }
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

    private class CompassView extends View {

        private Paint mPaint = new Paint();
        private Path mPath = new Path();
        private boolean mAnimate;

        private final Paint mDebugTextPaint;
        private final StringBuffer mDebugString = new StringBuffer();
        private NumberFormat mdebugValueFormatter = NumberFormat.getInstance();
        
        public CompassView(Context context) {
            super(context);

            // Construct a wedge-shaped path
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();

            mDebugTextPaint = new Paint();
            mDebugTextPaint.setColor(Color.RED);
            mDebugTextPaint.setTextSize(15.0f);
            mdebugValueFormatter.setMaximumFractionDigits( 2 );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Log.d(TAG, "onDraw");

            canvas.drawColor(Color.BLACK);

            mDebugString.delete(0, mDebugString.length());
            mDebugString.append(mdebugValueFormatter.format(mYaw));
            mDebugString.append(" ");
            mDebugString.append(mdebugValueFormatter.format(mPitch));
            mDebugString.append(" ");
            mDebugString.append(mdebugValueFormatter.format(mRoll));
            mDebugString.append(" ");

            canvas.drawText(mDebugString.toString(), 0.0f, 20.0f, mDebugTextPaint);
            
            if(m_lastAccels != null) {
                mDebugString.delete(0, mDebugString.length());
                mDebugString.append(mdebugValueFormatter.format(m_lastAccels[0]));
                mDebugString.append(" ");
                mDebugString.append(mdebugValueFormatter.format(m_lastAccels[1]));
                mDebugString.append(" ");
                mDebugString.append(mdebugValueFormatter.format(m_lastAccels[2]));
                mDebugString.append(" ");
                canvas.drawText(mDebugString.toString(), 0.0f, 40.0f, mDebugTextPaint);
            }
            /*
             * Paint paint = mPaint; 
             * 
             * paint.setAntiAlias(true);
             * paint.setColor(Color.BLACK);
             *  paint.setStyle(Paint.Style.FILL);
             * int w = canvas.getWidth(); 
             * int h = canvas.getHeight(); 
             * int cx = w
             * / 2; int cy = h / 2; canvas.translate(cx, cy); if (mValues !=
             * null) { canvas.rotate(-mValues[0]); } canvas.drawPath(mPath,
             * mPaint);
             */
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
