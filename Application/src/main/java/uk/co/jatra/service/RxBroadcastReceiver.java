package uk.co.jatra.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

import static rx.android.MainThreadSubscription.verifyMainThread;

/**
 * Created by tim on 06/04/2016.
 */
public class RxBroadcastReceiver {

    private static  Map<IntentFilter, BroadcastReceiver> registered = new HashMap<>();

    public static Observable<Intent> register(Context context, IntentFilter intentFilter) {
        return Observable.create(new RegisterCallbackOnSubscribe(context, intentFilter));
    }

    public static void unregister(Context context, IntentFilter intentFilter) {
        BroadcastReceiver broadcastReceiver = registered.remove(intentFilter);
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
        }
    }

    private static class RegisterCallbackOnSubscribe implements Observable.OnSubscribe<Intent> {
        Context context;
        IntentFilter intentFilter;

        public RegisterCallbackOnSubscribe(Context context, IntentFilter intentFilter) {
            this.context = context;
            this.intentFilter = intentFilter;
        }

        @Override
        public void call(Subscriber<? super Intent> subscriber) {
            verifyMainThread();
            RxBroadcastReceiverCallback rxBroadcastReceiverCallback = new RxBroadcastReceiverCallback();
            registered.put(intentFilter, rxBroadcastReceiverCallback);
            rxBroadcastReceiverCallback.setSubscriber(subscriber);
            context.registerReceiver(rxBroadcastReceiverCallback, intentFilter);
        }
    }


    private static class RxBroadcastReceiverCallback extends BroadcastReceiver {

        private Subscriber subscriber;

        public void setSubscriber(Subscriber subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * {@link Context#registerReceiver(BroadcastReceiver,
         * IntentFilter, String, Handler)}. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         * <p>
         * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.</b>  This means you should not perform any operations that
         * return a result to you asynchronously -- in particular, for interacting
         * with services, you should use
         * {@link Context#startService(Intent)} instead of
         * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
         * to interact with a service that is already running, you can use
         * {@link #peekService}.
         * <p>
         * <p>The Intent filters used in {@link Context#registerReceiver}
         * and in application manifests are <em>not</em> guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            subscriber.onNext(intent);
        }
    }
}


/*

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

 */