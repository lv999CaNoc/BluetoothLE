package vn.ss.ble.map;

import java.util.HashMap;

public class BluetoothGattServiceNames {

    // HashMap chứa UUID và tên dịch vụ tương ứng
    private static final HashMap<String, String> serviceNames = new HashMap<>();

    static {
        serviceNames.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        serviceNames.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        serviceNames.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        serviceNames.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        serviceNames.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        serviceNames.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
    }

    public static String getServiceName(String uuid) {
        String serviceName = serviceNames.get(uuid.toLowerCase());
        return (serviceName != null) ? serviceName : "Unknown Service";
    }
}
