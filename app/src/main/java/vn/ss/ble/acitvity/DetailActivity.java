package vn.ss.ble.acitvity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.ss.ble.R;
import vn.ss.ble.adapter.ServiceAdapter;
import vn.ss.ble.map.BluetoothGattCharacteristicNames;
import vn.ss.ble.map.BluetoothGattServiceNames;

public class DetailActivity extends AppCompatActivity {

    public static final String KEY_SCAN_RESULT = "scan_result";
    private static final String TAG = "BLE_DetailActivity";
    private String deviceAddress;

    private TextView tvNameDevice;
    private Button btnConnectDevice;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private ServiceAdapter serviceAdapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;


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
                displayGattServices(bluetoothGatt.getServices());
            }
        }
    };

    // Hiển thị dịch vụ và đặc tính trong ExpandableListView
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            String serviceUUID = gattService.getUuid().toString();
            String serviceName = BluetoothGattServiceNames.getServiceName(serviceUUID);
            listDataHeader.add("Service: " + serviceName); // Hiển thị tên của dịch vụ

            List<String> characteristics = new ArrayList<>();
            for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                String characteristicUUID = characteristic.getUuid().toString();
                String characteristicName = BluetoothGattCharacteristicNames.getCharacteristicName(characteristicUUID);
                int properties = characteristic.getProperties();
                byte[] value = characteristic.getValue();

                // Đọc các thuộc tính (properties) của characteristic
                String propertiesString = getPropertiesString(properties);

                // Giá trị đặc tính (value), có thể hiển thị dưới dạng hex
                String valueString = (value != null) ? bytesToHex(value) : "N/A";

                // Hiển thị UUID, thuộc tính và giá trị
                characteristics.add("Characteristic: " + characteristicName +
                        "\nProperties: " + propertiesString +
                        "\nValue: " + valueString);
            }

            listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), characteristics);
        }

        // Cập nhật adapter khi có thay đổi dữ liệu
        runOnUiThread(() -> serviceAdapter.notifyDataSetChanged());
    }

    // Chuyển đổi byte array sang hex string để hiển thị giá trị
    private String bytesToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    // Phương thức để lấy thuộc tính của characteristic
    private String getPropertiesString(int properties) {
        StringBuilder propertiesBuilder = new StringBuilder();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            propertiesBuilder.append("READ ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            propertiesBuilder.append("WRITE ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            propertiesBuilder.append("NOTIFY ");
        }
        return propertiesBuilder.toString();
    }

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

        expandableListView = findViewById(R.id.expandable_lv_services);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        serviceAdapter = new ServiceAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(serviceAdapter);

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