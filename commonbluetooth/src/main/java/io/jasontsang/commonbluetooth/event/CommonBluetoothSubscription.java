package io.jasontsang.commonbluetooth.event;

import rx.Subscription;

/**
 * Created by jason on 12/11/2015.
 */
public class CommonBluetoothSubscription {
    private Subscription subscription;

    public CommonBluetoothSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void unsubscribe() {
        subscription.unsubscribe();
    }
}
