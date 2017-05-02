package com.github.gnastnosaj.commonbluetooth;

import android.app.Application;

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