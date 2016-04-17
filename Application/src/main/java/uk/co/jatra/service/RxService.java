package uk.co.jatra.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import rx.Observable;
import rx.Subscriber;

import static rx.android.MainThreadSubscription.verifyMainThread;
import static uk.co.jatra.service.RxService.ServiceConnectionResult.Reason.CONNECTED;
import static uk.co.jatra.service.RxService.ServiceConnectionResult.Reason.DISCONNECTED;

public class RxService {

    static ObserverCallback serviceConnectionCallback;

    public static Observable<ServiceConnectionResult> bind(Context context, Intent intent, int flags) {
        return Observable.create(new ConnectionCallBackOnSubscribe(context, intent, flags));
    }

    public static void unbind(Context context, ServiceConnection connection) {
        context.unbindService(connection);
    }

    public static class ConnectionCallBackOnSubscribe implements Observable.OnSubscribe<ServiceConnectionResult> {

        private final Context context;
        private final Intent intent;
        private final int flags;

        public ConnectionCallBackOnSubscribe(Context context, Intent intent, int flags) {
            this.context = context;
            this.intent = intent;
            this.flags = flags;
            serviceConnectionCallback = new ObserverCallback();
        }

        @Override
        public void call(Subscriber<? super ServiceConnectionResult> subscriber) {
            verifyMainThread();
            ObserverCallback callback = serviceConnectionCallback;
            callback.setSubscriber(subscriber);
            context.bindService(intent, callback, flags);
        }
    }

    private static class ObserverCallback implements ServiceConnection {
        private Subscriber<? super ServiceConnectionResult> subscriber;

        public void setSubscriber(Subscriber<? super ServiceConnectionResult> subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * Called when a connection to the Service has been established, with
         * the {@link IBinder} of the communication channel to the
         * Service.
         *
         * @param name    The concrete component name of the service that has
         *                been connected.
         * @param service The IBinder of the Service's communication channel,
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            subscriber.onNext(new ServiceConnected(this, name, service));
        }

        /**
         * Called when a connection to the Service has been lost.  This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does <em>not</em> remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to {@link #onServiceConnected} when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         *             connection has been lost.
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            subscriber.onNext(new ServiceDisconnected(name));
        }
    }

    public abstract static class ServiceConnectionResult {
        public enum Reason {
            CONNECTED,
            DISCONNECTED
        }
        private final Reason reason;
        private final ComponentName componentName;


        public ServiceConnectionResult(Reason reason, ComponentName componentName) {
            this.reason = reason;
            this.componentName = componentName;
        }

        public ComponentName getComponentName() {
            return componentName;
        }

        public Reason getReason() {
            return reason;
        }
    }

    public static class ServiceConnected extends ServiceConnectionResult {
        private ServiceConnection serviceConnection;
        private final IBinder service;

        public ServiceConnected(ServiceConnection serviceConnection, ComponentName componentName, IBinder service) {
            super(CONNECTED, componentName);
            this.serviceConnection = serviceConnection;
            this.service = service;
        }

        public ServiceConnection getServiceConnection() {
            return serviceConnection;
        }

        public IBinder getService() {
            return service;
        }

    }
    public static class ServiceDisconnected extends ServiceConnectionResult {
        public ServiceDisconnected(ComponentName componentName) {
            super(DISCONNECTED, componentName);
        }
    }

}
