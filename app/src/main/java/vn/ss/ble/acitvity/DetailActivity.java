package vn.ss.ble.acitvity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ss.ble.R;
import vn.ss.ble.adapter.ServiceAdapter;

public class DetailActivity extends AppCompatActivity {

    public static final String KEY_SCAN_RESULT = "scan_result";
    private static final String TAG = "BLE_DetailActivity";
    private String deviceAddress;

    private TextView tvNameDevice;
    private Button btnConnectDevice;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private ServiceAdapter serviceAdapter;
    private RecyclerView recyclerViewServices;

    // GATT callback để xử lý kết nối và nhận dữ liệu
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Đã kết nối tới thiết bị GATT");
                // Bắt đầu khám phá các dịch vụ
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Đã ngắt kết nối từ thiết bị GATT");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO: Nhận danh sách dịch vụ và cập nhật Adapter
                List<BluetoothGattService> services = bluetoothGatt.getServices();
                runOnUiThread(() -> {
                    serviceAdapter = new ServiceAdapter(services, DetailActivity.this);
                    recyclerViewServices.setAdapter(serviceAdapter);
                });

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvNameDevice = findViewById(R.id.tv_name_device);
        btnConnectDevice = findViewById(R.id.btn_connect_device);

        Intent intent = getIntent();
        if (intent != null) {
            ScanResult device = intent.getParcelableExtra(KEY_SCAN_RESULT);
            if (device != null) {
                deviceAddress = device.getDevice().getAddress();
                tvNameDevice.setText(deviceAddress);
            }
        }

        recyclerViewServices = findViewById(R.id.recycler_view_services);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        btnConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kết nối đến thiết bị BLE
                connectToDevice(deviceAddress);

                Toast.makeText(DetailActivity.this, "Connect device", Toast.LENGTH_SHORT).show();
            }
        });

        if (deviceAddress == null) {
            btnConnectDevice.setEnabled(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.e(TAG, "Thiết bị không tìm thấy!");
            return;
        }

        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Đang kết nối tới thiết bị: " + address);
    }
}