package vn.ss.ble;

import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private List<ScanResult> devices;
    private Context context;

    public BleDeviceAdapter(List<ScanResult> devices, Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.devices = devices;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult device = devices.get(position);

        holder.deviceName.setText("Permission required");
        holder.deviceAddress.setText("Unknown Address");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String deviceName = device.getDevice().getName();
        String deviceAddress = device.getDevice().getAddress();

        holder.deviceName.setText(deviceName != null ? deviceName : "Unknown Device");
        holder.deviceAddress.setText(deviceAddress);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }
}
