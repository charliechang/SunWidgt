
package com.bk.sunwidgt.adapter;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LocationAdapterData implements Parcelable {
    public final Location location;
    public final String address;

    public LocationAdapterData(Location location, String address) {
        this.location = new Location(location);
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        location.writeToParcel(out, flags);
        out.writeString(address);

    }

    public static final Parcelable.Creator<LocationAdapterData> CREATOR = new Parcelable.Creator<LocationAdapterData>() {

        @Override
        public LocationAdapterData createFromParcel(Parcel p) {
            Location loc = Location.CREATOR.createFromParcel(p);
            String address = p.readString();
            return new LocationAdapterData(loc,address);
        }

        @Override
        public LocationAdapterData[] newArray(int size) {
            return new LocationAdapterData[size];
        }

    };
}
