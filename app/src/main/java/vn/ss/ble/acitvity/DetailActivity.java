package vn.ss.ble.acitvity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import vn.ss.ble.R;
import vn.ss.ble.adapter.ServiceAdapter;
import vn.ss.ble.map.BluetoothGattCharacteristicNames;

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

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Đọc characteristic thành công: " + characteristic.getUuid().toString());
                String value = bytesToHex(characteristic.getValue());

                // Tìm dịch vụ chứa characteristic này
                String serviceUUID = characteristic.getService().getUuid().toString();
                String characteristicUUID = characteristic.getUuid().toString();

                runOnUiThread(() -> {
                    // Cập nhật danh sách characteristic với giá trị mới
                    for (int i = 0; i < listDataHeader.size(); i++) {
                        String header = listDataHeader.get(i);
                        if (header.equals(serviceUUID)) {
                            List<String> characteristics = listDataChild.get(header);
                            if (characteristics != null) {
                                for (int j = 0; j < characteristics.size(); j++) {
                                    String child = characteristics.get(j);
                                    if (child.contains(BluetoothGattCharacteristicNames.getCharacteristicName(characteristicUUID))) {
                                        // Cập nhật giá trị trong danh sách characteristic
                                        characteristics.set(j,
                                                "Characteristic: " + BluetoothGattCharacteristicNames.getCharacteristicName(characteristicUUID) +
                                                        "\nUUID: " + characteristicUUID +
                                                        "\nProperties: " + getPropertiesString(characteristic.getProperties()) +
                                                        "\nValue: " + value);
                                        listDataChild.put(header, characteristics);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    // Thông báo cho adapter rằng dữ liệu đã thay đổi
                    serviceAdapter.notifyDataSetChanged();
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] valueBytes = characteristic.getValue();

            int value = 0;
            // Lấy giá trị heart rate
            if (valueBytes.length > 0) {
                // Kiểm tra loại dữ liệu (chỉ số nhị phân đầu tiên)
                if ((valueBytes[0] & 0x01) == 0) {
                    // Heart Rate Value Format = 0 (đơn vị là bpm)
                    value = valueBytes[1] & 0xFF; // Lấy byte thứ 2
                } else {
                    // Heart Rate Value Format = 1 (đơn vị là 10 bpm)
                    value = (valueBytes[1] & 0xFF) | ((valueBytes[2] & 0xFF) << 8); // Kết hợp byte 2 và 3
                }
            }

            String valueString = String.valueOf(value);
            Log.d(TAG, "Characteristic đã thay đổi: " + valueString);

            // Tìm dịch vụ chứa characteristic này
            String serviceUUID = characteristic.getService().getUuid().toString();
            String characteristicUUID = characteristic.getUuid().toString();

            runOnUiThread(() -> {
                // Cập nhật danh sách characteristic với giá trị mới
                for (int i = 0; i < listDataHeader.size(); i++) {
                    String header = listDataHeader.get(i);
                    if (header.equals(serviceUUID)) {
                        List<String> characteristics = listDataChild.get(header);
                        if (characteristics != null) {
                            for (int j = 0; j < characteristics.size(); j++) {
                                String child = characteristics.get(j);
                                if (child.contains(BluetoothGattCharacteristicNames.getCharacteristicName(characteristicUUID))) {
                                    // Cập nhật giá trị trong danh sách characteristic
                                    characteristics.set(j,
                                            "Characteristic: " + BluetoothGattCharacteristicNames.getCharacteristicName(characteristicUUID) +
                                                    "\nUUID: " + characteristicUUID +
                                                    "\nProperties: " + getPropertiesString(characteristic.getProperties()) +
                                                    "\nValue: " + valueString +" bpm");
                                    listDataChild.put(header, characteristics);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                // Thông báo cho adapter rằng dữ liệu đã thay đổi
                serviceAdapter.notifyDataSetChanged();
            });
        }
    };

    // Hiển thị dịch vụ và đặc tính trong ExpandableListView
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            String serviceUUID = gattService.getUuid().toString();
            listDataHeader.add(serviceUUID);

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
                        "\nUUID: " + characteristicUUID +
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

    @SuppressLint("MissingPermission")
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


        // Xử lý sự kiện nhấn vào characteristic
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String characteristicUUID = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).split("\n")[1].split(": ")[1];
            BluetoothGattService service = bluetoothGatt.getServices().get(groupPosition);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));

            boolean isNotify = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0;
            boolean isRead = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0;

            if (characteristic != null) {
                if (isNotify) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                    if (descriptor != null) {
                        byte[] currentValue = descriptor.getValue();

                        if (currentValue != null && Arrays.equals(currentValue, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                            // Nếu thông báo đang bật, tắt thông báo
                            bluetoothGatt.setCharacteristicNotification(characteristic, false);
                            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);

                            // Đặt lại màu TextView thành bình thường
                            v.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.white));
                        } else {
                            // Nếu thông báo đang tắt, bật thông báo
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);

                            // Đổi màu TextView thành xanh
                            v.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.teal_200));
                        }
                    }
                } else if (isRead) {
                    // Yêu cầu đọc giá trị của characteristic
                    bluetoothGatt.readCharacteristic(characteristic);
                }
            }

            return true;
        });

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