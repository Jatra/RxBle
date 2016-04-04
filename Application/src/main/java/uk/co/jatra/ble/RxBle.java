package uk.co.jatra.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static rx.android.MainThreadSubscription.verifyMainThread;

/**
 * Created by tim on 03/04/2016.
 */
public class RxBle {

    static ScanCallback scanCallback;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Observable<ScanResult> scan(BluetoothAdapter bluetoothAdapter, List<ScanFilter> filters, ScanSettings settings) {
        return Observable.create(new ScanCallBackOnSubscribe(bluetoothAdapter.getBluetoothLeScanner(), filters, settings));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void stopScan(BluetoothAdapter bluetoothAdapter) {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    public static class ScanCallBackOnSubscribe implements Observable.OnSubscribe<ScanResult> {
        final private BluetoothLeScanner scanner;
        private final List<ScanFilter> filters;
        private final ScanSettings settings;
        private ObserverCallback scanCallback;

        ScanCallBackOnSubscribe(BluetoothLeScanner scanner, List<ScanFilter> filters, ScanSettings settings) {
            this.scanner = scanner;
            this.filters = filters;
            this.settings = settings;
            scanCallback = new ObserverCallback();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void call(Subscriber<? super ScanResult> subscriber) {
            verifyMainThread();
            scanCallback.setSubscriber(subscriber);
            scanner.startScan(filters, settings, scanCallback);

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    scanner.stopScan(scanCallback);
                }
            });
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private class ObserverCallback extends ScanCallback {
            private Subscriber subscriber;

            public void setSubscriber(Subscriber subscriber) {
                this.subscriber = subscriber;
            }

            @Override
            public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                subscriber.onNext(new ScanResult(callbackType, result));
            }

            @Override
            public void onScanFailed(int errorCode) {
                subscriber.onError(new RuntimeException(Integer.toString(errorCode)));
            }

            @Override
            public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
                //want to call subscriber.onNext for each of the results.
                for (android.bluetooth.le.ScanResult result : results) {
                    subscriber.onNext(new ScanResult(0, result));
                }
            }
        }
    }
}
