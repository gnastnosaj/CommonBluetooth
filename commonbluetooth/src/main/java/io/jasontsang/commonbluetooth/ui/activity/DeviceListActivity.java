package io.jasontsang.commonbluetooth.ui.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import io.jasontsang.commonbluetooth.CommonBluetooth;
import io.jasontsang.commonbluetooth.Device;
import io.jasontsang.commonbluetooth.OnDeviceItemClickListener;
import io.jasontsang.commonbluetooth.R;
import io.jasontsang.commonbluetooth.adapter.DeviceListAdapter;
import io.jasontsang.commonbluetooth.event.DeviceEvent;

import com.github.gnastnosaj.boilerplate.ui.activity.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by jason on 12/3/2015.
 */
public class DeviceListActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonBluetooth.observable
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(event -> {
                    if (event instanceof DeviceEvent) {
                        DeviceEvent deviceEvent = (DeviceEvent) event;
                        if (deviceEvent.getType() == DeviceEvent.SCAN_FOUND) {
                            Bundle bundle = deviceEvent.getBundle();
                            Device device = (Device) bundle.get(DeviceEvent.EXTRA_DEVICE);
                            deviceListAdapter.notifyDataChanged(device);
                        }
                    }
                }, throwable -> {
                    Timber.e(throwable);
                });
    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            deviceListAdapter.refreshData();
            CommonBluetooth.scanDevices();
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> swipeRefreshLayout.setRefreshing(false));
        });
        createDynamicBox(swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceListAdapter = new DeviceListAdapter();
        recyclerView.setAdapter(deviceListAdapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());

        new RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_PRIVILEGED)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(grant -> {
            CommonBluetooth.scanDevices();
        });
    }

    public void setOnDeviceItemClickListener(OnDeviceItemClickListener onDeviceItemClickListener) {
        deviceListAdapter.setOnDeviceItemClickListener(onDeviceItemClickListener);
    }
}
