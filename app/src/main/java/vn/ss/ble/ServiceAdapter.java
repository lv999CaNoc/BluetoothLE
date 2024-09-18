package vn.ss.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private static final String TAG = "BLE_ServiceAdapter";

    private List<BluetoothGattService> serviceList;
    private Context context;

    public ServiceAdapter(List<BluetoothGattService> serviceList, Context context) {
        this.serviceList = serviceList;
        this.context = context;
    }


    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.service_list_item, parent, false);
        return new ServiceViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        BluetoothGattService service = serviceList.get(position);
        UUID serviceUUID = service.getUuid();

        holder.serviceName.setText("Service: " + serviceUUID.toString());

        // Khi nhấn vào dịch vụ, sẽ hiển thị/ẩn các characteristic của dịch vụ đó
        holder.itemView.setOnClickListener(view -> {
            Log.d(TAG, "onBindViewHolder: touch " + serviceUUID);
            if (holder.characteristicContainer.getVisibility() == View.GONE) {
                holder.characteristicContainer.setVisibility(View.VISIBLE);
                displayCharacteristics(service, holder.characteristicContainer);
            } else {
                holder.characteristicContainer.setVisibility(View.GONE);
            }
        });

    }

    // Hiển thị các characteristic bên dưới khi nhấn vào dịch vụ
    private void displayCharacteristics(BluetoothGattService service, LinearLayout container) {
        container.removeAllViews(); // Xóa tất cả các view cũ trước khi hiển thị mới

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();

        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            TextView characteristicView = new TextView(context);

            UUID characteristicUuid = characteristic.getUuid();

            characteristicView.setText("Characteristic: " + characteristicUuid.toString());
            characteristicView.setPadding(16, 8, 16, 8);
            characteristicView.setTextSize(16);

            // Thêm đặc tính vào container
            container.addView(characteristicView);

            // Nếu muốn thêm chức năng nhấn vào characteristic
            characteristicView.setOnClickListener(v -> {
                // Thực hiện các thao tác khi người dùng nhấn vào characteristic
                Log.d(TAG, "displayCharacteristics: touch " + characteristicUuid.toString());
            });
        }
    }


    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        LinearLayout characteristicContainer;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            characteristicContainer = itemView.findViewById(R.id.characteristic_container);
        }
    }

}
