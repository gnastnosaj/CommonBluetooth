package io.jasontsang.commonbluetooth.event;

/**
 * Created by jason on 12/11/2015.
 */
public interface CommonBluetoothEventObserver {
    <T extends CommonBluetoothEvent> Class<T> observableClass();
    void observe(CommonBluetoothEvent commonBluetoothEvent);
}
