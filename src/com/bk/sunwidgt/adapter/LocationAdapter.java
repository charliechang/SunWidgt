
package com.bk.sunwidgt.adapter;

import java.util.ArrayList;
import java.util.List;

import com.bk.sunwidgt.activity.BookmarkListActivity;
import com.bk.sunwidgt.activity.SunMapActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.location.Location;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class LocationAdapter extends ArrayAdapter<LocationAdapterData> {
    private final static String TAG = "Sun" + LocationAdapter.class.getSimpleName();
    private GestureLibrary m_gestureLib;
    
    class ItemViewGestureListener implements OnGesturePerformedListener {
        private View lastClickedView;
        @Override
        public void onGesturePerformed(GestureOverlayView view, Gesture gesture) {
            ArrayList<Prediction> predictions =
                    m_gestureLib.recognize(gesture); // We want at least
                                                     // one prediction
            if (predictions.size() > 0) {
                Prediction prediction = predictions.get(0); // We want
                                                            // at least
                                                            // some
                                                            // confidence
                                                            // in the
                                                            // result
                if (prediction.score > 1.0) {
                    final View currentView = view.getChildAt(0);
                    if(lastClickedView != null) {
                        final View bookmarkFunctinos = lastClickedView.findViewById(com.bk.sunwidgt.R.id.bookmark_function_layout);
                        bookmarkFunctinos.setVisibility(View.GONE);                    }
                    
                    if(prediction.name.startsWith("bknext")) {
                        final View bookmarkFunctinos = currentView.findViewById(com.bk.sunwidgt.R.id.bookmark_function_layout);
                        bookmarkFunctinos.setVisibility(View.VISIBLE);
                    }
                    else if(prediction.name.startsWith("bkprev")) {
                        final View bookmarkFunctinos = currentView.findViewById(com.bk.sunwidgt.R.id.bookmark_function_layout);
                        bookmarkFunctinos.setVisibility(View.GONE);
                    }
                    
                    lastClickedView = currentView;
                }
            }
        }
        
    }
    
    private ItemViewGestureListener m_gestureListener = new ItemViewGestureListener();

    public LocationAdapter(Context context) {
        super(context, com.bk.sunwidgt.R.layout.bookmark_list_view,
                com.bk.sunwidgt.R.id.bookmark_address);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final LocationAdapterData locAddressData = getItem(position);
        final TextView addressEditView = (TextView) view
                .findViewById(com.bk.sunwidgt.R.id.bookmark_address);

        addressEditView.setText(locAddressData.address);
        ((TextView) view.findViewById(com.bk.sunwidgt.R.id.bookmark_latlng)).setText(String.valueOf((float) locAddressData.location.getLatitude())+ "," + String.valueOf((float)  locAddressData.location.getLongitude()));

        //final Button setdefaultButton = (Button) view.findViewById(com.bk.sunwidgt.R.id.bookmark_setdefault);
        //final Button editButton = (Button)view.findViewById(com.bk.sunwidgt.R.id.bookmark_edit);
        final Button removeButton = (Button) view.findViewById(com.bk.sunwidgt.R.id.bookmark_remove);
        
        Log.d(TAG, "getView position=" + position + " address=" + locAddressData.address + " lat=" + locAddressData.location.getLatitude() + " lng=" + locAddressData.location.getLongitude());
        
        //editButton.setOnClickListener(new View.OnClickListener() {
        view.setOnClickListener(new View.OnClickListener() {
                    
            @Override
            public void onClick(View v) {
                if(Activity.class.isInstance(getContext())) {
                    final Activity activity = (BookmarkListActivity) getContext();
                    final LocationAdapterData selectedLocAddressData = getItem(position);
                    Intent intent = new Intent(getContext(),SunMapActivity.class);
                    if(selectedLocAddressData.location.getLongitude() != 0.0 && selectedLocAddressData.location.getLatitude() != 0.0) {
                        intent.putExtra(SunMapActivity.START_LOCATION, selectedLocAddressData.location);
                        intent.putExtra(SunMapActivity.LOCATION_ADDRESS, selectedLocAddressData.address);
                        intent.putExtra(SunMapActivity.SHOW_BOOKMARK_LOCATION, false);
                    }
                    
                    final LocationAdapterData[] locData = BookmarkStoreUtil.loadBookmarks(getContext());

                    if(locData.length > 0) {
                        Log.i(TAG, "put START_LOCATION_EXTRAS extraLocationList.size()=" + locData.length);
                        intent.putExtra(SunMapActivity.START_LOCATION_BOOKMARKS, BookmarkStoreUtil.tolocationParcelableArray(locData));
                    }
                    
                    intent.putExtra(BookmarkListActivity.SAVE_BOOKMARK_INDEX, position);                    
                    activity.startActivityForResult(intent, BookmarkListActivity.REQUEST_FROM_BOOKMARK );
                }

            }
        });
        
        removeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(position < LocationAdapter.this.getCount() - 1) {
                    final LocationAdapterData selectedLocAddressData = getItem(position);
                    LocationAdapter.this.remove(selectedLocAddressData);
                    LocationAdapter.this.notifyDataSetChanged();
                }
                else {
                    Log.i(TAG, "Unable to remove the last one entry");
                }
            }});
        
        if(view instanceof GestureOverlayView) {
            return view;
        }
        else {
            final GestureOverlayView gestureOverlayView = new GestureOverlayView(getContext());
    
            gestureOverlayView.addView(view);
            gestureOverlayView.setGestureVisible(false);
    
            if (null == m_gestureLib) {
                m_gestureLib = GestureLibraries.fromRawResource(getContext(),
                        com.bk.sunwidgt.R.raw.gestures);
            }
            if (!m_gestureLib.load()) {
                Log.w(TAG, "Unable to load Gestures");
            }
            else {
                gestureOverlayView.addOnGesturePerformedListener(m_gestureListener);
            }
            
            return gestureOverlayView;
        }
        
    }

}
