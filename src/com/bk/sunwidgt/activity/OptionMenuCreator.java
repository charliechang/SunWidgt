package com.bk.sunwidgt.activity;

import com.bk.sunwidgt.adapter.BookmarkStoreUtil;
import com.bk.sunwidgt.adapter.LocationAdapterData;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class OptionMenuCreator {
    private final static String TAG = "Sun" + OptionMenuCreator.class.getSimpleName();
    private final static int MENUITEM_CALENDAR_ID = 0;
    private final static int MENUITEM_BOOKMARKLIST_ID = 1;
    private final static int MENUITEM_MAP_ID = 2;
    private final static int MENUITEM_RAIN_LOCATION_ID = 3;
    private final static int MENUITEM_TIDE_ID = 4;
    
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENUITEM_CALENDAR_ID, MENUITEM_CALENDAR_ID, com.bk.sunwidgt.R.string.menuitem_title_calendar);
        menu.add(0, MENUITEM_BOOKMARKLIST_ID, MENUITEM_BOOKMARKLIST_ID, com.bk.sunwidgt.R.string.menuitem_title_bookmarks);
        menu.add(0, MENUITEM_MAP_ID, MENUITEM_MAP_ID, com.bk.sunwidgt.R.string.menuitem_title_location);
        menu.add(0, MENUITEM_RAIN_LOCATION_ID, MENUITEM_RAIN_LOCATION_ID, com.bk.sunwidgt.R.string.menuitem_title_rain);
        menu.add(0, MENUITEM_TIDE_ID, MENUITEM_TIDE_ID, com.bk.sunwidgt.R.string.menuitem_title_tide);
        return true;
    }
    
    public boolean onOptionsItemSelected (Context context,MenuItem item) {
        boolean ret = true;
        Intent activityIntent = null;
        switch(item.getItemId()) {
            
            case MENUITEM_CALENDAR_ID:
                activityIntent = new Intent(context,SunActivity.class);
                break;
            case MENUITEM_BOOKMARKLIST_ID:
                activityIntent = new Intent(context,BookmarkListActivity.class);
                break;
            case MENUITEM_MAP_ID:
                activityIntent = new Intent(context,SunMapActivity.class);
                final LocationAdapterData[] locData = BookmarkStoreUtil.loadBookmarks(context);

                if(locData.length > 0) {
                    Log.i(TAG, "put START_LOCATION_EXTRAS extraLocationList.size()=" + locData.length);
                    activityIntent.putExtra(SunMapActivity.START_LOCATION_BOOKMARKS, BookmarkStoreUtil.tolocationParcelableArray(locData));
                }
                
                break;
            case MENUITEM_RAIN_LOCATION_ID:
                activityIntent = new Intent(context,RainMapActivity.class);
                break;
            case MENUITEM_TIDE_ID:    
                activityIntent = new Intent(context,TideMapActivity.class);
                break;
            default:
                Log.w(TAG, "Unable to handle item id=" + item.getItemId());
                ret = false;
                break;
        }
        
        if(ret && !activityIntent.equals(context)) {
            context.startActivity(activityIntent);
        }

        return ret;
    }

}
