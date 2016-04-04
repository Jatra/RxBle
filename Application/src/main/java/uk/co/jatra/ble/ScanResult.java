package uk.co.jatra.ble;


/**
 * Created by tim on 03/04/2016.
 */
public class ScanResult {
    final private int callbackType;
    final private android.bluetooth.le.ScanResult scanResult;

    public ScanResult(int callbackType, android.bluetooth.le.ScanResult scanResult) {
        this.callbackType = callbackType;
        this.scanResult = scanResult;
    }

    public int getCallbackType() {
        return callbackType;
    }

    public android.bluetooth.le.ScanResult getScanResult() {
        return scanResult;
    }
}
