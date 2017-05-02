package io.jasontsang.commonbluetooth.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.jasontsang.commonbluetooth.Device;
import io.jasontsang.commonbluetooth.OnDeviceItemClickListener;
import io.jasontsang.commonbluetooth.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jason on 11/3/2015.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private List<Device> deviceList;
    private OnDeviceItemClickListener onDeviceItemClickListener;

    public DeviceListAdapter() {
        deviceList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.deviceType.setText(device.getType() == Device.DEVICE_TYPE_BLE ? "BLE" : "SPP");
        holder.deviceScanDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.deviceScanTime.setText(new SimpleDateFormat("hh:mm:ss").format(new Date()));
        holder.itemView.setOnClickListener(v -> {
            if (onDeviceItemClickListener != null) {
                onDeviceItemClickListener.onClick(holder.itemView, device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void refreshData() {
        this.deviceList.clear();
        notifyDataSetChanged();
    }

    public void notifyDataChanged(Device device) {
        if (!contain(device)) {
            this.deviceList.add(device);
            notifyDataSetChanged();
        }
    }

    private boolean contain(Device device) {
        for (Device d : deviceList) {
            if (d.getType() == device.getType() && d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    public void setOnDeviceItemClickListener(OnDeviceItemClickListener onDeviceItemClickListener) {
        this.onDeviceItemClickListener = onDeviceItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceType;
        TextView deviceScanDate;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceScanTime;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceType = (TextView) itemView.findViewById(R.id.tv_device_type);
            deviceScanDate = (TextView) itemView.findViewById(R.id.tv_device_scan_date);
            deviceName = (TextView) itemView.findViewById(R.id.tv_device_name);
            deviceAddress = (TextView) itemView.findViewById(R.id.tv_device_address);
            deviceScanTime = (TextView) itemView.findViewById(R.id.tv_device_scan_time);
        }
    }
}
