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
        characteristicNames.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        characteristicNames.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        characteristicNames.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        characteristicNames.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        characteristicNames.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        characteristicNames.put("00002a9c-0000-1000-8000-00805f9b34fb", "Body Composition Feature");
        characteristicNames.put("00002a9d-0000-1000-8000-00805f9b34fb", "Body Composition Measurement");
        characteristicNames.put("00002a9e-0000-1000-8000-00805f9b34fb", "Weight Measurement");
        characteristicNames.put("00002a9f-0000-1000-8000-00805f9b34fb", "Weight Scale Feature");
        characteristicNames.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        characteristicNames.put("00002a52-0000-1000-8000-00805f9b34fb", "Glucose Control Point");
        characteristicNames.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        characteristicNames.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        characteristicNames.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        characteristicNames.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
    }

    public static String getCharacteristicName(String uuid) {
        String characteristicName = characteristicNames.get(uuid.toLowerCase());
        return (characteristicName != null) ? characteristicName : "Unknown Characteristic";
    }
}
