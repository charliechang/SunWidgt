package com.bk.sunwidgt.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RainAdapter extends ArrayAdapter<RainAdapterData>{
    public RainAdapter(Context context) {
        super(context, com.bk.sunwidgt.R.layout.map_rainlocation_view,
                com.bk.sunwidgt.R.id.location_name);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final RainAdapterData rainData = getItem(position);
        
        final TextView locationView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_name);
        final TextView observerView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_observer);
        final TextView timeView= (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_time);
        
        final TextView rain1hrView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_1hr);
        final TextView rain3hrView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_3hr);
        final TextView rain6hrView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_6hr);
        final TextView rain12hrView = (TextView) view.findViewById(com.bk.sunwidgt.R.id.location_rain_12hr);
        
        locationView.setText(rainData.getLocationName());
        observerView.setText(rainData.getObserverName());
        timeView.setText(String.valueOf(rainData.m_month + 1) + "/" + rainData.m_day + " " + rainData.m_start_hour + ":00");
        
        rain1hrView.setText(String.valueOf(rainData.sumRainHour(1)));
        rain3hrView.setText(String.valueOf(rainData.sumRainHour(3)));
        rain6hrView.setText(String.valueOf(rainData.sumRainHour(6)));
        rain12hrView.setText(String.valueOf(rainData.sumRainHour(12)));
        
        return view;
    }

}
