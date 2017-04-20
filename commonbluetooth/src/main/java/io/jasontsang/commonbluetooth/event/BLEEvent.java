package io.jasontsang.commonbluetooth.event;

import android.os.Bundle;

/**
 * Created by jason on 12/4/2015.
 */
public class BLEEvent extends CommonBluetoothEvent{

    public final static String EXTRA_DEVICE = "device";

    public final static int BLE_SERVICE_DISCOVERD = 0;

    private int type;
    private Bundle bundle;
    private Object data;

    public BLEEvent(int type) {
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
