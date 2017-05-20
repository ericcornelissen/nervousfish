package com.nervousfish.nervousfish.events;

import android.bluetooth.BluetoothSocket;

/**
 * Greenrobot's EventBus message event
 *
 * Sent when the device successfully paired with a Bluetooth device
 */
public final class BluetoothConnectedEvent {
    private final BluetoothSocket socket;

    /**
     * Constructs a new BluetoothConnectedEvent
     * @param socket The socket to the server
     */
    public BluetoothConnectedEvent(final BluetoothSocket socket) {
        this.socket = socket;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }
}
