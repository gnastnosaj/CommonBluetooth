package io.jasontsang.commonbluetooth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import io.jasontsang.commonbluetooth.event.BLEEvent;
import io.jasontsang.commonbluetooth.event.CommonBluetoothEvent;
import io.jasontsang.commonbluetooth.event.DataEvent;
import io.jasontsang.commonbluetooth.event.DeviceEvent;

import com.github.gnastnosaj.boilerplate.Boilerplate;
import com.github.gnastnosaj.boilerplate.rxbus.RxBus;
import com.litesuits.bluetooth.LiteBleGattCallback;
import com.litesuits.bluetooth.LiteBluetooth;
import com.litesuits.bluetooth.conn.BleCharactCallback;
import com.litesuits.bluetooth.conn.BleDescriptorCallback;
import com.litesuits.bluetooth.conn.LiteBleConnector;
import com.litesuits.bluetooth.exception.BleException;
import com.litesuits.bluetooth.scan.PeriodScanCallback;

import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import io.reactivex.Observable;

/**
 * Created by jason on 12/2/2015.
 */
public class CommonBluetooth {

    public final static int TIME_OUT_SCAN = 5000;
    public final static Observable<CommonBluetoothEvent> observable = RxBus.getInstance().register(CommonBluetoothEvent.class, CommonBluetoothEvent.class);

    private static Application application;
    private static LiteBluetooth liteBluetooth;
    private static BluetoothSPP bluetoothSPP;
    private static BluetoothAdapter bluetoothAdapter;

    public static void init(Application application) {
        init(application, true);
    }

    public static void init(Application application, boolean otherDevice) {
        Boilerplate.initialize(application);
        
        CommonBluetooth.application = application;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            liteBluetooth = new LiteBluetooth(application);
            liteBluetooth.enableBluetooth();
        }
        bluetoothSPP = new BluetoothSPP(application);
        bluetoothSPP.setupService();
        if (otherDevice) {
            bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
        } else {
            bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_CONNECTED);
                Bundle bundle = new Bundle();
                Device device = new Device();
                device.setType(Device.DEVICE_TYPE_SPP);
                device.setName(name);
                device.setAddress(address);
                bundle.putParcelable(DeviceEvent.EXTRA_DEVICE, device);
                deviceEvent.setBundle(bundle);
                RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
            }

            @Override
            public void onDeviceDisconnected() {
                DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_DISCONNECTED);
                RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
            }

            @Override
            public void onDeviceConnectionFailed() {
                DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_CONNECT_FAILED);
                RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
            }
        });
        bluetoothSPP.setOnDataReceivedListener(((data, message) -> {
            DataEvent dataEvent = new DataEvent(DataEvent.DATA_RECEIVED);
            Bundle bundle = new Bundle();
            bundle.putByteArray(DataEvent.EXTRA_DATA, data);
            bundle.putString(DataEvent.EXTRA_MESSAGE, message);
            dataEvent.setBundle(bundle);
            RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
        }));
        bluetoothAdapter = bluetoothSPP.getBluetoothAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        application.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        application.registerReceiver(receiver, filter);
    }

    public static void scanDevices() {
        bluetoothAdapter.startDiscovery();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            liteBluetooth.startLeScan(new PeriodScanCallback(TIME_OUT_SCAN) {
                @Override
                public void onScanTimeout() {

                }

                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Bundle bundle = new Bundle();
                    Device d = new Device();
                    d.setType(Device.DEVICE_TYPE_BLE);
                    d.setName(device.getName());
                    d.setAddress(device.getAddress());
                    bundle.putParcelable(DeviceEvent.EXTRA_DEVICE, d);
                    DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.SCAN_FOUND);
                    deviceEvent.setBundle(bundle);
                    RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
                }
            });
        }
    }

    public static void connectDevice(Device device) {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (liteBluetooth.isInScanning()) {
                liteBluetooth.stopScan((d, rssi, scanRecord) -> {
                });
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getType() == Device.DEVICE_TYPE_BLE) {
            liteBluetooth.scanAndConnect(device.getAddress(), false, new LiteBleGattCallback() {
                @Override
                public void onConnectSuccess(BluetoothGatt gatt, int status) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_CONNECTED);
                        Bundle bundle = new Bundle();
                        Device d = new Device();
                        d.setType(Device.DEVICE_TYPE_BLE);
                        d.setName(gatt.getDevice().getName());
                        d.setAddress(gatt.getDevice().getAddress());
                        gatt.discoverServices();
                        bundle.putParcelable(DeviceEvent.EXTRA_DEVICE, d);
                        deviceEvent.setBundle(bundle);
                        RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        List<BluetoothGattService> bluetoothGattServices = gatt.getServices();
                        BLEEvent bleEvent = new BLEEvent(BLEEvent.BLE_SERVICE_DISCOVERD);
                        bleEvent.setData(bluetoothGattServices);
                        Bundle bundle = new Bundle();
                        Device d = new Device();
                        d.setType(Device.DEVICE_TYPE_BLE);
                        d.setName(gatt.getDevice().getName());
                        d.setAddress(gatt.getDevice().getAddress());
                        bundle.putParcelable(BLEEvent.EXTRA_DEVICE, d);
                        bleEvent.setBundle(bundle);
                        RxBus.getInstance().post(CommonBluetoothEvent.class, bleEvent);
                    }
                }

                @Override
                public void onConnectFailure(BleException exception) {
                    Bundle b = new Bundle();
                    b.putString(DataEvent.EXTRA_ERROR_MESSAGE, exception.toString());
                    DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_CONNECT_FAILED);
                    deviceEvent.setBundle(b);
                    RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        DataEvent dataEvent = new DataEvent(DataEvent.DATA_RECEIVED);
                        Bundle b = new Bundle();
                        b.putByteArray(DataEvent.EXTRA_DATA, characteristic.getValue());
                        dataEvent.setBundle(b);
                        RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                    }
                }
            });
        } else {
            bluetoothSPP.connect(device.getAddress());
        }
    }

    public static void send(Data data) {
        Device device = data.getDevice();
        if (device.getType() == Device.DEVICE_TYPE_SPP) {
            if (bluetoothSPP.getConnectedDeviceAddress().equals(device.getAddress())) {
                bluetoothSPP.send(data.getData(), false);
            } else {
                DataEvent dataEvent = new DataEvent(DataEvent.SEND_FAILED);
                Bundle bundle = new Bundle();
                bundle.putString(DataEvent.EXTRA_ERROR_MESSAGE, "target device is not connected");
                dataEvent.setBundle(bundle);
                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
            }
        } else {
            LiteBleConnector connector = liteBluetooth.newBleConnector();
            Bundle bundle = data.getBundle();
            if (bundle.getInt(Data.EXTRA_DATATYPE) == Data.CHAR_DATA) {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                connector.withUUIDString(serviceUUID, charUUID, null)
                        .writeCharacteristic(data.getData(), new BleCharactCallback() {
                            @Override
                            public void onSuccess(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                                DataEvent dataEvent = new DataEvent(DataEvent.SEND_SUCCESS);
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }

                            @Override
                            public void onFailure(BleException e) {
                                DataEvent dataEvent = new DataEvent(DataEvent.SEND_FAILED);
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                Bundle b = new Bundle();
                                b.putString(DataEvent.EXTRA_ERROR_MESSAGE, e.toString());
                                dataEvent.setBundle(bundle);
                            }
                        });
            } else {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                String desUUID = bundle.getString(Data.EXTRA_DES_UUID);
                connector.withUUIDString(serviceUUID, charUUID, desUUID)
                        .writeDescriptor(data.getData(), new BleDescriptorCallback() {
                            @Override
                            public void onSuccess(BluetoothGattDescriptor bluetoothGattDescriptor) {
                                DataEvent dataEvent = new DataEvent(DataEvent.SEND_SUCCESS);
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }

                            @Override
                            public void onFailure(BleException e) {
                                DataEvent dataEvent = new DataEvent(DataEvent.SEND_FAILED);
                                Bundle b = new Bundle();
                                b.putString(DataEvent.EXTRA_ERROR_MESSAGE, e.toString());
                                dataEvent.setBundle(bundle);
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }
                        });
            }
        }
    }

    public static void read(Data data) {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        Device device = data.getDevice();
        Bundle bundle = data.getBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getType() == Device.DEVICE_TYPE_BLE) {
            if (bundle.getInt(Data.EXTRA_DATATYPE) == Data.CHAR_DATA) {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                connector.withUUIDString(serviceUUID, charUUID, null)
                        .readCharacteristic(new BleCharactCallback() {
                            @Override
                            public void onSuccess(BluetoothGattCharacteristic characteristic) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    data.setData(characteristic.getValue());
                                    DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_SUCCESS);
                                    Bundle b = new Bundle();
                                    b.putByteArray(DataEvent.EXTRA_DATA, data.getData());
                                    dataEvent.setBundle(b);
                                    RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                }
                            }

                            @Override
                            public void onFailure(BleException exception) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_FAILED);
                                    Bundle b = new Bundle();
                                    b.putString(DataEvent.EXTRA_ERROR_MESSAGE, exception.toString());
                                    RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                }
                            }
                        });
            } else {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                String desUUID = bundle.getString(Data.EXTRA_DES_UUID);
                connector.withUUIDString(serviceUUID, charUUID, desUUID)
                        .readDescriptor(new BleDescriptorCallback() {
                            @Override
                            public void onSuccess(BluetoothGattDescriptor bluetoothGattDescriptor) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    data.setData(bluetoothGattDescriptor.getValue());
                                    DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_SUCCESS);
                                    Bundle b = new Bundle();
                                    b.putByteArray(DataEvent.EXTRA_DATA, data.getData());
                                    dataEvent.setBundle(b);
                                    RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                }
                            }

                            @Override
                            public void onFailure(BleException e) {
                                DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_FAILED);
                                Bundle b = new Bundle();
                                b.putString(DataEvent.EXTRA_ERROR_MESSAGE, e.toString());
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }
                        });
            }
        }
    }

    public static void enableNotification(Data data) {
        Device device = data.getDevice();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getType() == Device.DEVICE_TYPE_BLE) {
            LiteBleConnector connector = liteBluetooth.newBleConnector();
            Bundle bundle = data.getBundle();
            if (bundle.getInt(Data.EXTRA_DATATYPE) == Data.CHAR_DATA) {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                connector.withUUIDString(serviceUUID, charUUID, null)
                        .enableCharacteristicNotification(new BleCharactCallback() {
                            @Override
                            public void onSuccess(BluetoothGattCharacteristic characteristic) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    DataEvent dataEvent = new DataEvent(DataEvent.DATA_RECEIVED);
                                    Bundle b = new Bundle();
                                    b.putByteArray(DataEvent.EXTRA_DATA, characteristic.getValue());
                                    dataEvent.setBundle(b);
                                    RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                }
                            }

                            @Override
                            public void onFailure(BleException exception) {
                                DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_FAILED);
                                Bundle b = new Bundle();
                                b.putString(DataEvent.EXTRA_ERROR_MESSAGE, exception.toString());
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }
                        });
            } else {
                String serviceUUID = bundle.getString(Data.EXTRA_SERVICE_UUID);
                String charUUID = bundle.getString(Data.EXTRA_CHAR_UUID);
                String desUUID = bundle.getString(Data.EXTRA_DES_UUID);
                connector.withUUIDString(serviceUUID, charUUID, desUUID)
                        .enableDescriptorNotification(new BleDescriptorCallback() {
                            @Override
                            public void onSuccess(BluetoothGattDescriptor bluetoothGattDescriptor) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    DataEvent dataEvent = new DataEvent(DataEvent.DATA_RECEIVED);
                                    Bundle b = new Bundle();
                                    b.putByteArray(DataEvent.EXTRA_DATA, bluetoothGattDescriptor.getValue());
                                    dataEvent.setBundle(b);
                                    RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                                }
                            }

                            @Override
                            public void onFailure(BleException exception) {
                                DataEvent dataEvent = new DataEvent(DataEvent.DATA_READ_FAILED);
                                Bundle b = new Bundle();
                                b.putString(DataEvent.EXTRA_ERROR_MESSAGE, exception.toString());
                                RxBus.getInstance().post(CommonBluetoothEvent.class, dataEvent);
                            }
                        });

            }
        }
    }

    public static void disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (liteBluetooth.isConnected()) {
                liteBluetooth.closeBluetoothGatt();
                liteBluetooth = new LiteBluetooth(application);
            }
        }
        bluetoothSPP.disconnect();
        DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.DEVICE_DISCONNECTED);
        RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
    }

    private final static BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Bundle bundle = new Bundle();
                Device d = new Device();
                d.setType(Device.DEVICE_TYPE_SPP);
                d.setName(device.getName());
                d.setAddress(device.getAddress());
                bundle.putParcelable(DeviceEvent.EXTRA_DEVICE, d);
                DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.SCAN_FOUND);
                deviceEvent.setBundle(bundle);
                RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                DeviceEvent deviceEvent = new DeviceEvent(DeviceEvent.SCAN_FINISHED);
                RxBus.getInstance().post(CommonBluetoothEvent.class, deviceEvent);
            }
        }
    };
}