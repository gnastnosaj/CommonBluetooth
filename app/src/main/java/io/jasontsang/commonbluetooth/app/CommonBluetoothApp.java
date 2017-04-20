package io.jasontsang.commonbluetooth.app;

import android.app.Application;

import io.jasontsang.commonbluetooth.CommonBluetooth;

/**
 * Created by jason on 12/4/2015.
 */
public class CommonBluetoothApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommonBluetooth.init(this);
    }
}