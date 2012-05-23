
package com.bk.sunwidgt.fragment;

import com.bk.sunwidgt.activity.BookmarkListActivity;
import com.bk.sunwidgt.activity.SunMapActivity;
import com.bk.sunwidgt.adapter.BookmarkStoreUtil;
import com.bk.sunwidgt.adapter.LocationAdapterData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkFragment extends DialogFragment {
    private final static String TAG = "Sun" + BookmarkFragment.class.getSimpleName();
    private final static String LOCATION = BookmarkFragment.class.getName() + ".location";
    private final static String ADDRESS = BookmarkFragment.class.getName() + ".address";
    private ComponentName m_bookmark_activity;
    
    public static BookmarkFragment newInstance(Location loc, String address) {
        BookmarkFragment dialog = new BookmarkFragment();
        Bundle b = new Bundle();

        b.putParcelable(LOCATION, loc);
        b.putString(ADDRESS, address);

        dialog.setArguments(b);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        final View locationView = inflater.inflate(com.bk.sunwidgt.R.layout.user_location_view,
                null);

        final Location loc = getArguments().getParcelable(LOCATION);
        final String address = getArguments().getString(ADDRESS, "");
        final EditText addressEditText = (EditText) locationView
                .findViewById(com.bk.sunwidgt.R.id.edit_address);

        addressEditText.setText(address);

        setCancelable(true);
        
        m_bookmark_activity = new ComponentName(getActivity(),BookmarkListActivity.class);

        ((TextView) locationView.findViewById(com.bk.sunwidgt.R.id.latlng)).setText(String
                .valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude()));

        Log.i(TAG, "Return address=" + address + " loc=" + loc);

        DialogInterface.OnClickListener saveBookmarkListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DialogInterface.BUTTON_POSITIVE == which) {
                    final String address = addressEditText.getText().toString();
                    if ("".equals(address)) {
                        Toast.makeText(getActivity(),
                                com.bk.sunwidgt.R.string.map_bookmark_empty_address,
                                Toast.LENGTH_LONG);
                        Log.i(TAG, "Empty address");
                    }
                    else {
                        // Intent data = new Intent();
                        // data.putExtra(SunMapActivity.LOCATION_ADDRESS,
                        // address);
                        // data.putExtra(SunMapActivity.START_LOCATION, loc);

                        // Save to bookmark according to index
                        Log.i(TAG, "Saving address=" + address + " loc=" + loc);
                        
                        final int bookmark_index = getActivity().getIntent().getIntExtra(
                                BookmarkListActivity.SAVE_BOOKMARK_INDEX,
                                BookmarkStoreUtil.NEW_BOOKMAKRS);
                        BookmarkStoreUtil.saveBookmark(getActivity(), new LocationAdapterData(loc,
                                address), bookmark_index);
                        
                        if(!m_bookmark_activity.equals(getActivity().getCallingActivity())) {
                            Log.i(TAG, "Not Call by " + BookmarkListActivity.class.getName());
                            
                            Intent activityIntent = new Intent(getActivity(),BookmarkListActivity.class);
                            getActivity().startActivity(activityIntent);
                        }

                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                 
                    }
                }
                BookmarkFragment.this.dismissAllowingStateLoss();
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle(com.bk.sunwidgt.R.string.map_bookmark)
                .setPositiveButton(android.R.string.ok, saveBookmarkListener)
                .setNegativeButton(android.R.string.cancel, saveBookmarkListener)
                .setView(locationView).create();
    }
}
