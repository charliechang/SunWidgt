
package com.bk.sunwidgt.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BookmarkListFragment extends Fragment {
    private final static String TAG = "Sun" + BookmarkListFragment.class.getSimpleName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(com.bk.sunwidgt.R.layout.bookmark_list_fragment,
                container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        // mLogger.logMethodName();
        super.onPause();
    }

}
