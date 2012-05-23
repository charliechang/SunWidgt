
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

import com.bk.sunwidgt.adapter.BookmarkStoreUtil;
import com.bk.sunwidgt.adapter.LocationAdapter;
import com.bk.sunwidgt.adapter.LocationAdapterData;

public class BookmarkListActivity extends Activity {
    public final static int REQUEST_FROM_BOOKMARK = 1000;
    public final static String SAVE_BOOKMARK_INDEX = SunMapActivity.class.getName() + ".bookmark_index";
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
        
        if (RESULT_OK == resultCode && REQUEST_FROM_BOOKMARK == requestCode) {
            loadBookmarks();
        }
        
        /*
        if (RESULT_OK == resultCode && requestCode >= REQUEST_CODEBASE) {
            int position = requestCode - REQUEST_CODEBASE;
            if (position < 0) {
                Log.w(TAG, "invaild position=" + position);
            }
            else {

                final String address = data.getStringExtra(SunMapActivity.LOCATION_ADDRESS);
                final Location loc = data.getParcelableExtra(SunMapActivity.START_LOCATION);

                Log.i(TAG, "Add position=" + position + " address=" + address + " loc=" + loc);
                
                final LocationAdapterData oldData = m_bookmakrAdapter.getItem(position);
                m_bookmakrAdapter.remove(oldData);
                m_bookmakrAdapter.insert(new LocationAdapterData(loc, address), position);
                if (position == m_bookmakrAdapter.getCount() - 1) {
                    // Add one slot if this if the last one
                    final String emptyString = getResources().getString(
                            com.bk.sunwidgt.R.string.map_no_bookmark);
                    final Location empttyLocation = new Location(
                            BookmarkListActivity.class.getName());
                    m_bookmakrAdapter.add(new LocationAdapterData(empttyLocation, emptyString));
                }
                
                saveBookmarks();
                loadBookmarks();
            }
        }*/
    }

    private void saveBookmarks() {
        Log.i(TAG, "Save bookmarks to list");
        final LocationAdapterData[] locData = new LocationAdapterData[m_bookmakrAdapter.getCount()];
        for(int i = 0;i < locData.length;i++) {
            locData[i] = m_bookmakrAdapter.getItem(i);
        }
        BookmarkStoreUtil.saveBookmarks(this, locData);
    }

    private void loadBookmarks() {
        Log.i(TAG, "Load bookmarks to list");

        m_bookmakrAdapter.clear();

        final LocationAdapterData[] locData = BookmarkStoreUtil.loadBookmarks(this);
        
        for(LocationAdapterData loc : locData) {
            m_bookmakrAdapter.add(loc);
        }
        
        // add empty record
        final String emptyString = getResources().getString(com.bk.sunwidgt.R.string.map_no_bookmark);
        final Location empttyLocation = new Location(BookmarkListActivity.class.getName());
        m_bookmakrAdapter.add(new LocationAdapterData(empttyLocation, emptyString));
        
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
        Log.d(TAG, "onResume");      
        super.onResume();
        
        loadBookmarks();
    }

}
