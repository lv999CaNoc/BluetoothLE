package vn.ss.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE_MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;
    private static final long SCAN_PERIOD = 30000;      // 30 giây
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private HashSet<String> addressMap;
    private List<ScanResult> scanResults;
    private BleDeviceAdapter bleDeviceAdapter;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String deviceAddress = result.getDevice().getAddress();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String deviceName = result.getDevice().getName();

            if (!addressMap.contains(deviceAddress) && deviceName != null) {
                addressMap.add(deviceAddress);
                scanResults.add(result);
                bleDeviceAdapter.notifyDataSetChanged();
            }
        }
    };
    private final Handler handler = new Handler();
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addressMap = new HashSet<>();
        scanResults = new ArrayList<>();
        bleDeviceAdapter = new BleDeviceAdapter(scanResults, this, position -> {

            Toast.makeText(this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(bleDeviceAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Kiểm tra xem thiết bị có hỗ trợ Bluetooth không
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onCreate: Bluetooth không được hỗ trợ!");
            finish(); // Đóng ứng dụng
        }

        // Kiểm tra nếu thiết bị có hỗ trợ BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE không được hỗ trợ!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onCreate: BLE không được hỗ trợ!");
            finish();
        }

        Log.i(TAG, "onCreate: BluetoothLE được hỗ trợ.");

        // Yêu cầu quyền truy cập BLE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate: Yêu cầu quyền CONNECT và SCAN.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_PERMISSION);
        }

        // Kiểm tra xem Bluetooth có được bật không
        if (!bluetoothAdapter.isEnabled()) {
            // Hiển thị thông báo yêu cầu người dùng bật Bluetooth
            Log.i(TAG, "onCreate: yêu cầu bật Bluetooth.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // bắt đầu quét
                Log.i(TAG, "onCreate: BluetoothLE đã được bật.");
                startBleScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                // Người dùng từ chối bật Bluetooth, thông báo và thoát ứng dụng
                Log.w(TAG, "onActivityResult: Bluetooth cần được bật để quét BLE!");
                Toast.makeText(this, "Bluetooth cần được bật để quét BLE", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startBleScan() {
        // Yêu cầu quyền truy cập BLE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "startBleScan: Yêu cầu quyền CONNECT và SCAN.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_PERMISSION);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        btnScan.setText("SCANNING...");
        btnScan.setEnabled(false);
        Toast.makeText(this, "Bắt đầu quét", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "startBleScan: Bắt đầu quét!");
        bluetoothLeScanner.startScan(scanCallback);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopBleScan();
            }
        }, SCAN_PERIOD);
    }

    private void stopBleScan() {
        // Yêu cầu quyền truy cập BLE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "stopBleScan: Yêu cầu quyền CONNECT và SCAN.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_PERMISSION);
        }

        btnScan.setText("SCAN");
        btnScan.setEnabled(true);
        Log.i(TAG, "stopBleScan: Dừng quét!");
        Toast.makeText(this, "Dừng quét", Toast.LENGTH_SHORT).show();
        bluetoothLeScanner.stopScan(scanCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "onRequestPermissionsResult: Quyền BLE bị từ chối");
                    Toast.makeText(this, "Quyền BLE bị từ chối", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

}