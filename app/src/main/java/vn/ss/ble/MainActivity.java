package vn.ss.ble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE_MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<ScanResult> scanResults;
    private BleDeviceAdapter bleDeviceAdapter;

    private Handler handler = new Handler();

    private static final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;      // 10 giây

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (scanResults == null || bleDeviceAdapter == null) {
                return;
            }

            if (!scanResults.contains(result)) {
                scanResults.add(result);
                bleDeviceAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scanResults = new ArrayList<>();
        bleDeviceAdapter = new BleDeviceAdapter(scanResults, this);
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
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        // Kiểm tra xem Bluetooth có được bật không
        if (!bluetoothAdapter.isEnabled()) {
            // Hiển thị thông báo yêu cầu người dùng bật Bluetooth
            Log.i(TAG, "onCreate: yêu cầu bật Bluetooth.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Bluetooth đã bật, bắt đầu quét
        Log.i(TAG, "onCreate: BluetoothLE đã được bật.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Người dùng đã bật Bluetooth, bắt đầu quét BLE
                startBleScan();
            } else {
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
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        Toast.makeText(this, "Bắt đầu quét", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "startBleScan: Bắt đầu quét!");
        bluetoothLeScanner.startScan(scanCallback);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopBleScan(); // Dừng quét sau 10 giây
            }
        }, SCAN_PERIOD);
    }

    private void stopBleScan() {
        // Yêu cầu quyền truy cập BLE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "stopBleScan: Yêu cầu quyền CONNECT và SCAN.");
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        Log.i(TAG, "stopBleScan: Dừng quét!");
        Toast.makeText(this, "Dừng quét", Toast.LENGTH_SHORT).show();
        bluetoothLeScanner.stopScan(scanCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBleScan(); // Gọi lại quét BLE nếu quyền được cấp
            } else {
                Log.w(TAG, "onRequestPermissionsResult: Quyền BLE bị từ chối");
                Toast.makeText(this, "Quyền BLE bị từ chối", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}