package com.github.gnastnosaj.commonbluetooth.event;

import android.os.Bundle;

/**
 * Created by jason on 12/4/2015.
 */
public class DataEvent extends CommonBluetoothEvent {

    public final static String EXTRA_ERROR_MESSAGE = "errorMessage";
    public final static String EXTRA_DATA = "data";
    public final static String EXTRA_MESSAGE = "message";

    public final static int SEND_SUCCESS = 0;
    public final static int SEND_FAILED = 1;
    public final static int DATA_RECEIVED = 2;
    public final static int DATA_READ_SUCCESS = 3;
    public final static int DATA_READ_FAILED = 4;

    private int type;
    private Bundle bundle;

    public DataEvent(int type) {
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
