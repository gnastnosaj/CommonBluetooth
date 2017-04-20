package io.jasontsang.commonbluetooth.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import io.jasontsang.commonbluetooth.CommonBluetooth;
import io.jasontsang.commonbluetooth.Device;
import io.jasontsang.commonbluetooth.OnDeviceItemClickListener;
import io.jasontsang.commonbluetooth.R;
import io.jasontsang.commonbluetooth.adapter.DeviceListAdapter;
import io.jasontsang.commonbluetooth.event.DeviceEvent;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by jason on 12/3/2015.
 */
public class DeviceListActivity extends AppCompatActivity{

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DeviceListAdapter deviceListAdapter;
    private Subscription deviceSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        deviceSubscription = CommonBluetooth.rxBus.toObserverable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof DeviceEvent) {
                        DeviceEvent deviceEvent = (DeviceEvent) event;
                        if(deviceEvent.getType() == DeviceEvent.SCAN_FOUND) {
                            Bundle bundle = deviceEvent.getBundle();
                            Device device = (Device) bundle.get(DeviceEvent.EXTRA_DEVICE);
                            deviceListAdapter.notifyDataChanged(device);
                        }
                    }
                }, error -> {
                    Log.e("DeviceList", error.toString());
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceSubscription.unsubscribe();
    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            deviceListAdapter.refreshData();
            CommonBluetooth.scanDevices();
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) ->
                            swipeRefreshLayout.setRefreshing(false));
        });
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceListAdapter = new DeviceListAdapter(this);
        recyclerView.setAdapter(deviceListAdapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        CommonBluetooth.scanDevices();
    }

    public void setOnDeviceItemClickListener(OnDeviceItemClickListener onDeviceItemClickListener) {
        deviceListAdapter.setOnDeviceItemClickListener(onDeviceItemClickListener);
    }

}
