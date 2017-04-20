package io.jasontsang.commonbluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jason on 12/2/2015.
 */
public class Device implements Parcelable {

    public final static int DEVICE_TYPE_BLE = 0;
    public final static int DEVICE_TYPE_SPP = 1;

    private int type;
    private String name;
    private String address;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.name);
        dest.writeString(this.address);
    }

    public Device() {
    }

    protected Device(Parcel in) {
        this.type = in.readInt();
        this.name = in.readString();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
