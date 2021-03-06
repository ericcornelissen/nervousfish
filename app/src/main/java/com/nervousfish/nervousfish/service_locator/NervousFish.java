package com.nervousfish.nervousfish.service_locator;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nervousfish.nervousfish.modules.pairing.AndroidBluetoothService;
import com.nervousfish.nervousfish.modules.pairing.PairingWrapper;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller of the NervousFish application. It provides an access point for the
 * {@link com.nervousfish.nervousfish.modules.pairing.IBluetoothHandlerService} and holds the
 * current global state of the application.
 */
public final class NervousFish extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger("NervousFish");

    private static NervousFish instance;
    private static IServiceLocator serviceLocator;

    private final ServiceConnection connection = new BluetoothServiceConnection();
    private AndroidBluetoothService bluetoothService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        final Intent serviceIntent = new Intent(this, AndroidBluetoothService.class);
        this.bindService(serviceIntent, this.connection, Context.BIND_AUTO_CREATE);

        final String androidFileDir = this.getFilesDir().getPath();
        NervousFish.serviceLocator = new ServiceLocator(androidFileDir);
        NervousFish.instance = this;

        LOGGER.info("Application created");
    }

    /**
     * @return The NervousFish {@link Application} instance,
     */
    public static Context getInstance() {
        Validate.notNull(NervousFish.instance);
        return NervousFish.instance;
    }

    /**
     * @return The ServiceLocator of the {@link Application} instance.
     */
    public static IServiceLocator getServiceLocator() {
        Validate.notNull(NervousFish.serviceLocator);
        return NervousFish.serviceLocator;
    }

    /**
     * @return The global bluetooth service used for bluetooth connections
     */
    public PairingWrapper<AndroidBluetoothService> getBluetoothService() {
        return new PairingWrapper<>(this.bluetoothService);
    }

    private class BluetoothServiceConnection implements ServiceConnection {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final AndroidBluetoothService.LocalBinder binder = (AndroidBluetoothService.LocalBinder) service;
            NervousFish.this.bluetoothService = binder.getService();
            NervousFish.this.bluetoothService.setServiceLocator(NervousFish.serviceLocator);

            LOGGER.info("Bluetooth service connected");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            LOGGER.info("Bluetooth service disconnected");
        }

    }
}
