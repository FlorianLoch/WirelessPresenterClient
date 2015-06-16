package net.fdloch.wifiPresenter.android.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by florian on 14.03.15.
 */
public class ServerAddress implements Parcelable {
    private String host;
    private String passcode;

    public ServerAddress(String host, String passcode) {
        this.host = host;
        this.passcode = passcode;
    }

    public String getHost() {
        return host;
    }

    public String getPasscode() {
        return passcode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.host);
        parcel.writeString(this.passcode);
    }

    public static final Parcelable.Creator<ServerAddress> CREATOR = new Parcelable.Creator<ServerAddress>() {
        @Override
        public ServerAddress createFromParcel(Parcel parcel) {
            return new ServerAddress(parcel);
        }

        @Override
        public ServerAddress[] newArray(int i) {
            return new ServerAddress[0];
        }
    };

    private ServerAddress(Parcel parcel) {
        this(parcel.readString(), parcel.readString());
    }
}
