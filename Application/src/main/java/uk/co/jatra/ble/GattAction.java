package uk.co.jatra.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * Created by tim on 16/04/2016.
 */
@AutoValue
public abstract class GattAction {

    public enum Action {
        READ,
        WRITE,
        SUBSCRIBE
    }
    public static GattAction create(BluetoothGatt gatt, int status, @Nullable BluetoothGattCharacteristic characteristic, @Nullable BluetoothGattDescriptor descriptor) {
        return new AutoValue_GattAction();
    }

    public abstract BluetoothGatt getGatt();

    public abstract int getStatus();
    @Nullable
    public abstract BluetoothGattCharacteristic getCharacteristic();
    @Nullable
    public abstract BluetoothGattDescriptor getDescriptor();
}
