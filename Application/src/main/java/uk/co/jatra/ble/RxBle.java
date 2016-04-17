package uk.co.jatra.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static rx.android.MainThreadSubscription.verifyMainThread;

public class RxBle {

    static WeakReference<ScanCallBackOnSubscribe.ObserverCallback> scanCallback;
    static WeakReference<ConnectGattCallbackOnSubscribe.ObserverCallback> gattCallback;

    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Observable<ScanResult> scan(BluetoothAdapter bluetoothAdapter, List<ScanFilter> filters, ScanSettings settings) {
        return Observable.create(new ScanCallBackOnSubscribe(bluetoothAdapter.getBluetoothLeScanner(), filters, settings));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void stopScan(BluetoothAdapter bluetoothAdapter) {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback.get());
    }

    private static class ScanCallBackOnSubscribe implements Observable.OnSubscribe<ScanResult> {
        final private BluetoothLeScanner scanner;
        private final List<ScanFilter> filters;
        private final ScanSettings settings;
//        private ObserverCallback scanCallback;

        ScanCallBackOnSubscribe(BluetoothLeScanner scanner, List<ScanFilter> filters, ScanSettings settings) {
            this.scanner = scanner;
            this.filters = filters;
            this.settings = settings;
            scanCallback = new WeakReference<>(new ObserverCallback(settings));
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void call(Subscriber<? super ScanResult> subscriber) {
            verifyMainThread();
            scanCallback.get().setSubscriber(subscriber);
            scanner.startScan(filters, settings, scanCallback.get());

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    scanner.stopScan(scanCallback.get());
                }
            });
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private static class ObserverCallback extends ScanCallback {
            private Subscriber subscriber;
            private ScanSettings settings;

            public ObserverCallback(ScanSettings settings) {
                this.settings = settings;
            }

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
                int callbackType = settings.getCallbackType();
                //want to call subscriber.onNext for each of the results.
                for (android.bluetooth.le.ScanResult result : results) {
                    subscriber.onNext(new ScanResult(callbackType, result));
                }
            }
        }
    }


    public static Observable<GattStateResult> connectGatt(Context context, BluetoothDevice bluetoothDevice, boolean autoConnect) {
        return Observable.create(new ConnectGattCallbackOnSubscribe());
    }

    private static class ConnectGattCallbackOnSubscribe implements Observable.OnSubscribe<GattStateResult> {

        public ConnectGattCallbackOnSubscribe() {
        }

        @Override
        public void call(Subscriber<? super GattStateResult> subscriber) {
            verifyMainThread();

        }

        private static class ObserverCallback extends BluetoothGattCallback {
            private Subscriber subscriber;

            public void setSubscriber(Subscriber subscriber) {
                this.subscriber = subscriber;
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                subscriber.onNext(new GattStateResult());
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                subscriber.onNext(new GattStateResult());
            }
        }
    }

}
