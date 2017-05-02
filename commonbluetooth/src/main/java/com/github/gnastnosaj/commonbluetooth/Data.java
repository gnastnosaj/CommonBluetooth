package com.github.gnastnosaj.commonbluetooth;

import android.os.Bundle;

/**
 * Created by jason on 12/4/2015.
 */
public class Data {
    public final static String EXTRA_DATATYPE = "dataType";
    public final static String EXTRA_SERVICE_UUID = "serviceUUID";
    public final static String EXTRA_CHAR_UUID = "charUUID";
    public final static String EXTRA_DES_UUID = "desUUID";

    public final static int CHAR_DATA = 0;
    public final static int DES_DATA = 1;

    private Device device;
    private byte[] data;
    private Bundle bundle;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

}
