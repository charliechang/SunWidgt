
package com.bk.sunwidgt.activity;

import android.os.Bundle;

import com.google.android.maps.MapActivity;

public class SunMapActivity extends MapActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.bk.sunwidgt.R.layout.map_activity);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
