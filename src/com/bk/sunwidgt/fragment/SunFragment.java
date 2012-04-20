package com.bk.sunwidgt.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

public class SunFragment extends Fragment{
    
    private final static String TAG = SunFragment.class.getSimpleName();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //mLogger.logMethodName();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //mLogger.logMethodName();
        final View view = inflater.inflate(com.bk.sunwidgt.R.layout.sun_fragment, container, false);

        return view;
    }

    @Override
    public void onStart() {
        //mLogger.logMethodName();
        super.onStart();
    }

    @Override
    public void onResume() {
        //mLogger.logMethodName();
        super.onResume();
    }

    @Override
    public void onPause() {
        //mLogger.logMethodName();
        super.onPause();
    }

    @Override
    public void onStop() {
        //mLogger.logMethodName();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //mLogger.logMethodName();
        super.onDestroy();
    }
}
