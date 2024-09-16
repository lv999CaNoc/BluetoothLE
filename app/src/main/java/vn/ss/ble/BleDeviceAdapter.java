package vn.ss.ble;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {

    private List<ScanResult> devices;
    private Context context;

    public BleDeviceAdapter(List<ScanResult> devices, Context context) {
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

        // Kiểm tra quyền BLUETOOTH_CONNECT trước khi truy cập thông tin thiết bị
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            String deviceName = device.getDevice().getName();
            String deviceAddress = device.getDevice().getAddress();

            holder.deviceName.setText(deviceName != null ? deviceName : "Unknown Device");
            holder.deviceAddress.setText(deviceAddress);
        } else {
            // Nếu không có quyền, hiển thị thông báo mặc định
            holder.deviceName.setText("Permission required");
            holder.deviceAddress.setText("Unknown Address");
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
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
