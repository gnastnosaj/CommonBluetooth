package com.github.gnastnosaj.commonbluetooth.app;

import android.app.Application;

import com.github.gnastnosaj.commonbluetooth.CommonBluetooth;

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