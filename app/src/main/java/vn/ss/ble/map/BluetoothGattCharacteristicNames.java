package vn.ss.ble.map;

import java.util.HashMap;

public class BluetoothGattCharacteristicNames {

    // HashMap chứa UUID và tên characteristic tương ứng
    private static final HashMap<String, String> characteristicNames = new HashMap<>();

    static {
        characteristicNames.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        characteristicNames.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        characteristicNames.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        characteristicNames.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        characteristicNames.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        characteristicNames.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
    }

    public static String getCharacteristicName(String uuid) {
        String characteristicName = characteristicNames.get(uuid.toLowerCase());
        return (characteristicName != null) ? characteristicName : "Unknown Characteristic";
    }
}
