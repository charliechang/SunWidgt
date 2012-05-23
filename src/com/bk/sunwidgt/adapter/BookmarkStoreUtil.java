
package com.bk.sunwidgt.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcelable;
import android.util.Log;

public class BookmarkStoreUtil {
    public final static int NEW_BOOKMAKRS = -1;
    private final static String TAG = "Sun" + BookmarkStoreUtil.class.getSimpleName();
    private final static String STORAGE_NAME = BookmarkStoreUtil.class.getName() + ".storagename";
    private final static int DEFAULT_BOOKMAKR_SIZE = 0;
    private final static String BOOKMAKR_ADDRESS = BookmarkStoreUtil.class.getName()
            + ".bookmark_address";
    private final static String BOOKMARK_LOCATION_LAT = BookmarkStoreUtil.class.getName()
            + ".bookmark_lat";
    private final static String BOOKMARK_LOCATION_LNG = BookmarkStoreUtil.class.getName()
            + ".bookmark_lng";
    private final static String BOOKMARK_SIZE = BookmarkStoreUtil.class.getName()
            + ".bookmark_size";

    public static Parcelable[] tolocationParcelableArray(LocationAdapterData[] locData) {

        final Parcelable[] extraLocation = new Parcelable[locData.length];
        for (int k = 0; k < locData.length; k++) {
            extraLocation[k] = locData[k].location;
        }
        return extraLocation;
    }

    public static void saveBookmark(Context context, LocationAdapterData loc, int pos) {
        final SharedPreferences perf = context.getSharedPreferences(STORAGE_NAME,
                Context.MODE_PRIVATE);
        final int bookmarkSize = perf.getInt(BOOKMARK_SIZE, DEFAULT_BOOKMAKR_SIZE);

        if (NEW_BOOKMAKRS == pos || pos < 0 || pos > bookmarkSize) {
            pos = bookmarkSize; // Insert in the last one entry
            Log.i(TAG, "new bookmrk");
        }

        final SharedPreferences.Editor editperf = context.getSharedPreferences(STORAGE_NAME,
                Context.MODE_PRIVATE).edit();

        try {

            editperf.putString(BOOKMAKR_ADDRESS + String.valueOf(pos), loc.address);
            editperf.putFloat(BOOKMARK_LOCATION_LAT + String.valueOf(pos),
                    (float) loc.location.getLatitude());
            editperf.putFloat(BOOKMARK_LOCATION_LNG + String.valueOf(pos),
                    (float) loc.location.getLongitude());

            if (pos == bookmarkSize) {
                pos++;
                editperf.putInt(BOOKMARK_SIZE, pos);
                Log.i(TAG, BOOKMARK_SIZE + " =" + pos);
            }

        } finally {
            editperf.commit();
        }

    }

    public static void saveBookmarks(Context context, LocationAdapterData[] locData) {
        final SharedPreferences.Editor perf = context.getSharedPreferences(STORAGE_NAME,
                Context.MODE_PRIVATE).edit();
        int numRecords = 0;
        try {
            for (int i = 0; i < locData.length; i++) {
                if ("".equals(locData[i].address) || 0.0 == locData[i].location.getLatitude()
                        || 0.0 == locData[i].location.getLongitude()) {
                    Log.i(TAG, "Skip position=" + i);
                }
                else {
                    perf.putString(BOOKMAKR_ADDRESS + String.valueOf(i), locData[i].address);
                    perf.putFloat(BOOKMARK_LOCATION_LAT + String.valueOf(i),
                            (float) locData[i].location.getLatitude());
                    perf.putFloat(BOOKMARK_LOCATION_LNG + String.valueOf(i),
                            (float) locData[i].location.getLongitude());
                    numRecords++;
                }
                Log.i(TAG, "Saving position=" + i + " address=" + locData[i].address + " loc="
                        + locData[i].location);
            }
            perf.putInt(BOOKMARK_SIZE, numRecords);
            Log.i(TAG, BOOKMARK_SIZE + " =" + locData.length);

        } finally {
            perf.commit();
        }
    }

    public static LocationAdapterData[] loadBookmarks(Context context) {
        final SharedPreferences perf = context.getSharedPreferences(STORAGE_NAME,
                Context.MODE_PRIVATE);
        final int bookmarkSize = perf.getInt(BOOKMARK_SIZE, DEFAULT_BOOKMAKR_SIZE);
        final LocationAdapterData[] locData = new LocationAdapterData[bookmarkSize];
        for (int i = 0; i < bookmarkSize; i++) {
            final String address = perf.getString(BOOKMAKR_ADDRESS + String.valueOf(i), context
                    .getResources().getString(com.bk.sunwidgt.R.string.map_no_bookmark));
            final double lat = perf.getFloat(BOOKMARK_LOCATION_LAT + String.valueOf(i), 0.0f);
            final double lng = perf.getFloat(BOOKMARK_LOCATION_LNG + String.valueOf(i), 0.0f);
            final Location loc = new Location(STORAGE_NAME);

            loc.setLatitude(lat);
            loc.setLongitude(lng);

            locData[i] = new LocationAdapterData(loc, address);

            Log.i(TAG, "Loading position=" + i + " address=" + address + " loc=" + loc);
        }

        return locData;
    }

}
