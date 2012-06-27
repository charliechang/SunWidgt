package com.bk.sunwidgt.adapter;

import java.util.Calendar;

import com.bk.sunwidgt.fragment.SunFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

public class SunFragmentAdapter extends FragmentStatePagerAdapter {
    
    public final static int MONTH_RANGE = 100;
    private final Calendar INIT_TIME;
    public SunFragmentAdapter(FragmentManager fm) {
        super(fm);
        
        INIT_TIME = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int postion) {
        final Calendar movedCalendar = (Calendar) INIT_TIME.clone();
        movedCalendar.add(Calendar.MONTH, postion - MONTH_RANGE);
        return SunFragment.newInstance(movedCalendar.getTimeInMillis());
    }

    @Override
    public int getCount() {
        return MONTH_RANGE * 2;
    }

}
