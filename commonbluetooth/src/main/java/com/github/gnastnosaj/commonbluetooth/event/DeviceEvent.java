package com.github.gnastnosaj.commonbluetooth.event;

import android.os.Bundle;

/**
 * Created by jason on 12/3/2015.
 */
public class DeviceEvent extends CommonBluetoothEvent {

    public final static int SCAN_FOUND = 0;
    public final static int SCAN_FINISHED = 1;
    public final static int DEVICE_CONNECTED = 2;
    public final static int DEVICE_CONNECT_FAILED = 3;
    public final static int DEVICE_DISCONNECTED = 4;

    public final static String EXTRA_DEVICE = "device";
    public final static String EXTRA_ERROR_MESSAGE = "errorMessage";

    private int type;
    private Bundle bundle;

    public DeviceEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

}
