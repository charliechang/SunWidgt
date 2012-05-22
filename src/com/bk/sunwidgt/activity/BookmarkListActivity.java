
package com.bk.sunwidgt.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import com.bk.sunwidgt.adapter.LocationAdapter;
import com.bk.sunwidgt.adapter.LocationAdapterData;

public class BookmarkListActivity extends Activity {
    public final static int REQUEST_CODEBASE = 1000;
    public final static int DEFAULT_BOOKMAKR_SIZE = 0;
    public final static String BOOKMAKR_PREFIX = BookmarkListActivity.class.getName()
            + ".bookmark_address";
    public final static String BOOKMARK_LOCATION_LAT = BookmarkListActivity.class.getName()
            + ".bookmark_lat";
    public final static String BOOKMARK_LOCATION_LNG = BookmarkListActivity.class.getName()
            + ".bookmark_lng";
    public final static String BOOKMARK_SIZE = BookmarkListActivity.class.getName()
            + ".bookmark_size";

    private final static String TAG = "Sun" + BookmarkListActivity.class.getSimpleName();
    private OptionMenuCreator m_menuCreator;

    private ListView m_bookmarkList;
    private LocationAdapter m_bookmakrAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(com.bk.sunwidgt.R.layout.bookmark_list_activity);

        m_bookmarkList = (ListView) findViewById(com.bk.sunwidgt.R.id.bookmark_list);
        m_bookmakrAdapter = new LocationAdapter(this);

        m_bookmarkList.setAdapter(m_bookmakrAdapter);

        loadBookmarks();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode);
        if (RESULT_OK == resultCode && requestCode >= REQUEST_CODEBASE) {
            int position = requestCode - REQUEST_CODEBASE;
            if (position < 0) {
                Log.w(TAG, "invaild position=" + position);
            }
            else {

                final String address = data.getStringExtra(SunMapActivity.LOCATION_ADDRESS);
                final Location loc = data.getParcelableExtra(SunMapActivity.START_LOCATION);

                Log.i(TAG, "Add position=" + position + " address=" + address + " loc=" + loc);
                //

                final LocationAdapterData oldData = m_bookmakrAdapter.getItem(position);
                m_bookmakrAdapter.remove(oldData);
                m_bookmakrAdapter.insert(new LocationAdapterData(loc, address), position);
                if (position == m_bookmakrAdapter.getCount() - 1) {
                    // Add one slot if this is the last one
                    final String emptyString = getResources().getString(
                            com.bk.sunwidgt.R.string.map_no_bookmark);
                    final Location empttyLocation = new Location(
                            BookmarkListActivity.class.getName());
                    m_bookmakrAdapter.add(new LocationAdapterData(empttyLocation, emptyString));
                }
                m_bookmakrAdapter.notifyDataSetChanged();
                saveBookmarks();
                loadBookmarks();
            }
        }
    }

    private void saveBookmarks() {
        Log.i(TAG, "Save bookmarks to list");

        final SharedPreferences.Editor perf = getSharedPreferences(getClass().getName(),
                Context.MODE_PRIVATE).edit();

        try {
            for (int i = 0; i < m_bookmakrAdapter.getCount() - 1; i++) {
                final LocationAdapterData data = m_bookmakrAdapter.getItem(i);
                perf.putString(BOOKMAKR_PREFIX + String.valueOf(i), data.address);
                perf.putFloat(BOOKMARK_LOCATION_LAT + String.valueOf(i),
                        (float) data.location.getLatitude());
                perf.putFloat(BOOKMARK_LOCATION_LNG + String.valueOf(i),
                        (float) data.location.getLongitude());

                Log.i(TAG, "Saving position=" + i + " address=" + data.address + " loc="
                        + data.location);
            }
            perf.putInt(BOOKMARK_SIZE, m_bookmakrAdapter.getCount() - 1);
            Log.i(TAG, BOOKMARK_SIZE + " =" + (m_bookmakrAdapter.getCount() - 1));

        } finally {
            perf.commit();
        }
    }

    private void loadBookmarks() {
        Log.i(TAG, "Load bookmarks to list");

        m_bookmakrAdapter.clear();

        final SharedPreferences perf = getSharedPreferences(getClass().getName(),
                Context.MODE_PRIVATE);

        final int bookmarkSize = perf.getInt(BOOKMARK_SIZE, DEFAULT_BOOKMAKR_SIZE);
        boolean isLastEntryEmpty = false;
        for (int i = 0; i < bookmarkSize; i++) {
            final String address = perf.getString(BOOKMAKR_PREFIX + String.valueOf(i),
                    getResources().getString(com.bk.sunwidgt.R.string.map_no_bookmark));
            final double lat = perf.getFloat(BOOKMARK_LOCATION_LAT + String.valueOf(i), 0.0f);
            final double lng = perf.getFloat(BOOKMARK_LOCATION_LNG + String.valueOf(i), 0.0f);
            final Location loc = new Location(BookmarkListActivity.class.getName());

            if (0.0 == lat && 0.0 == lng) {
                isLastEntryEmpty = true;
            }
            else {
                isLastEntryEmpty = false;
            }

            loc.setLatitude(lat);
            loc.setLongitude(lng);

            m_bookmakrAdapter.add(new LocationAdapterData(loc, address));
            Log.i(TAG, "Loading position=" + i + " address=" + address + " loc=" + loc);
        }

        // add empty record
        if (!isLastEntryEmpty) {
            final String emptyString = getResources().getString(
                    com.bk.sunwidgt.R.string.map_no_bookmark);
            final Location empttyLocation = new Location(BookmarkListActivity.class.getName());
            m_bookmakrAdapter.add(new LocationAdapterData(empttyLocation, emptyString));
        }
        m_bookmakrAdapter.notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        saveBookmarks();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}
