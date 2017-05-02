package com.github.gnastnosaj.commonbluetooth.app;

import android.content.Intent;
import android.os.Bundle;

import com.github.gnastnosaj.commonbluetooth.app.R;
import com.github.gnastnosaj.commonbluetooth.CommonBluetooth;
import com.github.gnastnosaj.commonbluetooth.Data;
import com.github.gnastnosaj.commonbluetooth.Device;
import com.github.gnastnosaj.commonbluetooth.event.BLEEvent;
import com.github.gnastnosaj.commonbluetooth.event.DeviceEvent;
import com.github.gnastnosaj.commonbluetooth.ui.activity.DeviceListActivity;

/**
 * Created by jason on 12/4/2015.
 */
public class CustomDeviceListActivity extends DeviceListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonBluetooth.observable.compose(bindToLifecycle()).subscribe(event -> {
            if (event instanceof BLEEvent) {
                BLEEvent bleEvent = (BLEEvent) event;
                Bundle bundle = bleEvent.getBundle();
                Device device = (Device) bundle.get(BLEEvent.EXTRA_DEVICE);
                Intent intent = new Intent();
                intent.setClass(CustomDeviceListActivity.this, DemoActivity.class);
                intent.putExtra(DemoActivity.EXTRA_DEVICE, device);
                intent.putExtra(DemoActivity.EXTRA_DATATYPE, Data.CHAR_DATA);
                if (device.getName().equals("BLE-BOLUTEK")) {
                    intent.putExtra(DemoActivity.EXTRA_SERVICE_UUID, "0000fee0-0000-1000-8000-00805f9b34fb");
                    intent.putExtra(DemoActivity.EXTRA_CHAR_UUID_WRITE, "0000fee2-0000-1000-8000-00805f9b34fb");
                    intent.putExtra(DemoActivity.EXTRA_CHAR_UUID_READ, "0000fee1-0000-1000-8000-00805f9b34fb");

                    Data data = new Data();
                    data.setDevice(device);
                    Bundle b = new Bundle();
                    b.putInt(Data.EXTRA_DATATYPE, Data.CHAR_DATA);
                    b.putString(Data.EXTRA_SERVICE_UUID, "0000fee0-0000-1000-8000-00805f9b34fb");
                    b.putString(Data.EXTRA_CHAR_UUID, "0000fee1-0000-1000-8000-00805f9b34fb");
                    data.setBundle(b);
                    CommonBluetooth.enableNotification(data);

                    data = new Data();
                    data.setDevice(device);
                    b = new Bundle();
                    b.putInt(Data.EXTRA_DATATYPE, Data.DES_DATA);
                    b.putString(Data.EXTRA_SERVICE_UUID, "0000fee0-0000-1000-8000-00805f9b34fb");
                    b.putString(Data.EXTRA_CHAR_UUID, "0000fee1-0000-1000-8000-00805f9b34fb");
                    b.putString(Data.EXTRA_DES_UUID, "00002902-0000-1000-8000-00805f9b34fb");
                    data.setBundle(b);
                    CommonBluetooth.enableNotification(data);
                }
                startActivity(intent);
            }
        });

        CommonBluetooth.observable.compose(bindToLifecycle()).subscribe(event -> {
            if (event instanceof DeviceEvent) {
                DeviceEvent deviceEvent = (DeviceEvent) event;
                if (deviceEvent.getType() == DeviceEvent.DEVICE_CONNECTED) {
                    Bundle bundle = deviceEvent.getBundle();
                    Device device = (Device) bundle.get(DeviceEvent.EXTRA_DEVICE);
                    if (device.getType() == Device.DEVICE_TYPE_SPP) {
                        Intent intent = new Intent();
                        intent.setClass(CustomDeviceListActivity.this, DemoActivity.class);
                        intent.putExtra(DemoActivity.EXTRA_DEVICE, device);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
