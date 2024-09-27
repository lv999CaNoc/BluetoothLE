package vn.ss.ble.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.ss.ble.R;
import vn.ss.ble.map.BluetoothGattCharacteristicNames;
import vn.ss.ble.map.BluetoothGattServiceNames;
import vn.ss.ble.service.BluetoothLeService;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String TAG = "BLE_DeviceControlActivity";

    public static final String KEY_DEVICE_ADDRESS = "device_address";
    private static final String LIST_NAME = "list_name", LIST_UUID = "list_uuid";
    ExpandableListView mGattServicesList;
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothLeService bluetoothLeService;
    private boolean isConnected = false;

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                Log.d(TAG, "onReceive: gatt connected");
                Toast.makeText(context, "gatt connected", Toast.LENGTH_SHORT).show();
//                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                Log.d(TAG, "onReceive: gatt disconnected");
                Toast.makeText(context, "gatt disconnected", Toast.LENGTH_SHORT).show();
//                updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "onReceive: gatt services discovered");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else  if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "onReceive: data read and notify");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                // Cập nhật UI với dữ liệu nhận được
                Log.d(TAG, "onReceive: data: "+data);
                Toast.makeText(context, "onReceive: data: "+data, Toast.LENGTH_SHORT).show();
//                updateUIWithData(data);
            }
        }
    };

    private String deviceAddress;
    private TextView tvDeviceAddress;
    private Button btnConnect;

    // Khởi tạo đối tượng ServiceConnection để theo dõi kết nối và ngắt kết nối với dịch vụ
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            bluetoothLeService = binder.getService();

            if (bluetoothLeService != null) {
                if (!bluetoothLeService.initialize()) {
                    Log.e(TAG, "Không thể khởi tạo Bluetooth");
                    finish();
                }
                // Kết nối tới thiết bị BLE bằng địa chỉ MAC
                isConnected = true;
                bluetoothLeService.connect(deviceAddress);

                Log.d(TAG, "onServiceConnected: kết nối tới address: "+deviceAddress);

                // Chuyển trạng thái button
                btnConnect.setText("DISCONNECT");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService.disconnect();
            bluetoothLeService = null;
            isConnected = false;

            // Chuyển trạng thái button
            btnConnect.setText("CONNECT");

            Log.d(TAG, "onServiceDisconnected: ngắt kết nối tới address: "+deviceAddress);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        tvDeviceAddress = findViewById(R.id.device_control_tv_name_device);
        btnConnect = findViewById(R.id.device_control_btn_connect_device);

        btnConnect.setOnClickListener(view -> {
            if (!isConnected) {
                Intent serviceIntent = new Intent(DeviceControlActivity.this, BluetoothLeService.class);
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            } else {
                unbindService(serviceConnection);
                isConnected = false;
            }
        });

        Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(KEY_DEVICE_ADDRESS);
        tvDeviceAddress.setText(deviceAddress);

        mGattServicesList = (ExpandableListView) findViewById(R.id.device_control_expandable_lv_services);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

        registerReceiver(gattUpdateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) {
            unbindService(serviceConnection);
            isConnected = false;
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        String uuid = null;

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        // Lưu lại các đặc tính (characteristics) của thiết bị.
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Duyệt qua các dịch vụ GATT
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            // Lấy tên dịch vụ nếu có, nếu không có thì sử dụng giá trị mặc định "unknown_service"
            currentServiceData.put(LIST_NAME, BluetoothGattServiceNames.getServiceName(uuid));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // Duyệt qua các đặc tính (characteristics) của dịch vụ
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);

                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Lấy tên của đặc tính nếu có, nếu không có thì sử dụng giá trị mặc định "unknown_characteristic"
                currentCharaData.put(LIST_NAME, BluetoothGattCharacteristicNames.getCharacteristicName(uuid));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            // Lưu các đặc tính của dịch vụ hiện tại
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        // Cập nhật dữ liệu lên giao diện (ExpandableListView)
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        // Gán adapter cho ExpandableListView để hiển thị dữ liệu
        mGattServicesList.setAdapter(gattServiceAdapter);

        mGattServicesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // Lấy đặc tính (Characteristic) từ danh sách
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);

                // Xác định thao tác: Đọc hoặc Nhận thông báo (Notify)
                final int charaProp = characteristic.getProperties();

                // Nếu có thể đọc giá trị
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // Đọc giá trị của Characteristic
                    bluetoothLeService.readCharacteristic(characteristic);
                }

                // Nếu có thể nhận thông báo
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    // Đăng ký để nhận thông báo từ Characteristic hoặc huỷ
                    bluetoothLeService.setCharacteristicNotification(characteristic);
                }
                return true;
            }
        });
    }


//    Tích hợp lên trên rồi!
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Liên kết với dịch vụ
//        Intent serviceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Hủy liên kết với dịch vụ khi hoạt động dừng lại
//        if (isServiceBound) {
//            unbindService(serviceConnection);
//            isServiceBound = false;
//        }
//    }

}