package vn.ss.ble.acitvity;

import android.Manifest;
import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import vn.ss.ble.R;
import vn.ss.ble.adapter.BleDeviceAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE_MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;
    private static final long SCAN_PERIOD = 10000;      // 10 giây
    private final Handler handler = new Handler();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private HashSet<String> addressMap;
    private List<ScanResult> scanResults;
    private BleDeviceAdapter bleDeviceAdapter;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String deviceAddress = result.getDevice().getAddress();

            @SuppressLint("MissingPermission") String deviceName = result.getDevice().getName();

            if (!addressMap.contains(deviceAddress) && deviceName != null) {
                addressMap.add(deviceAddress);
                scanResults.add(result);
                bleDeviceAdapter.notifyDataSetChanged();
            }
        }
    };
    private Button btnScan;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addressMap = new HashSet<>();
        scanResults = new ArrayList<>();
        bleDeviceAdapter = new BleDeviceAdapter(scanResults, this, position -> {
            Log.d(TAG, "onCreate: Clicked: " + position);
            ScanResult device = scanResults.get(position);

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.KEY_SCAN_RESULT, device);
            startActivity(intent);
        });
        recyclerView.setAdapter(bleDeviceAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
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


    private void startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "startBleScan: Yêu cầu quyền CONNECT và SCAN.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_PERMISSION);
                return;
            }
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        addressMap.clear();
        scanResults.clear();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "stopBleScan: Yêu cầu quyền CONNECT và SCAN.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_PERMISSION);
            }
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
                    Log.w(TAG, "onRequestPermissionsResult: Yêu cầu quyền bị từ chối");
                    Toast.makeText(this, "Yêu cầu quyền bị từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}