package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.modules.pairing.IBluetoothHandler;
import com.nervousfish.nervousfish.modules.pairing.events.BluetoothConnectedEvent;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.NervousFish;

import org.apache.commons.lang3.Validate;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * An {@link Activity} that establishes and manages a bluetooth connection.
 * It shows a screen with the Bluetooth devices with which the user is paired and other Bluetooth devices
 * that are detected near the device of the user.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
// 1. Uses many Android and utility classes
// 2. The amount of methods is not too much at this moment
public final class BluetoothConnectActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger("BluetoothConnectActivity");
    private static final int DISCOVERABLE_DURATION = 300; // Device discoverable for 300 seconds
    private static final int MINIMUM_MAC_LENGTH = 48; // The minimum length of a MAC address

    private final Set<BluetoothDevice> newDevices = new HashSet<>();
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver broadcastReceiver = new BluetoothDiscoverBroadcastReceiver();
    private IServiceLocator serviceLocator;
    private BluetoothAdapter bluetoothAdapter;
    private IBluetoothHandler bluetoothHandler;
    private Set<BluetoothDevice> pairedDevices;
    private boolean isMaster;
    /*
     * Used to fill the listview of newly discovered Bluetooth devices
     */
    private ArrayAdapter<String> newDevicesArrayAdapter;
    /**
     * Used to fill the listview of paired Bluetooth devices
     */
    private ArrayAdapter<String> pairedDevicesArrayAdapter;


    private static String getDeviceDescription(final BluetoothDevice device) {
        return String.format("%s%n%s", device.getName(), device.getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bluetooth_connection);
        this.serviceLocator = NervousFish.getServiceLocator();

        // Register for broadcasts when a device is discovered.
        final IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(this.broadcastReceiver, foundFilter);

        // Set result CANCELED in case the user backs out
        this.setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        this.pairedDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        this.newDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Find and set up the ListView for paired devices
        final ListView pairedListView = (ListView) this.findViewById(R.id.paired_list);
        pairedListView.setAdapter(this.pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(new DeviceClickListener());

        // Find and set up the ListView for newly discovered devices
        final ListView newDevicesListView = (ListView) this.findViewById(R.id.discovered_list);
        newDevicesListView.setAdapter(this.newDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(new DeviceClickListener());

        // Register for broadcasts when discovery has finished
        final IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(this.broadcastReceiver, filter);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get the AndroidBluetoothHandler.
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothHandler = this.serviceLocator.getBluetoothHandler();

        final String deviceName = String.format(this.getString(R.string.your_device_name_is), this.bluetoothAdapter.getName());
        ((TextView) this.findViewById(R.id.tv_device_name)).setText(deviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        this.serviceLocator.registerToEventBus(this);
        this.queryPairedDevices();
        this.discoverDevices();

        LOGGER.info("Activity started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        this.serviceLocator.unregisterFromEventBus(this);

        LOGGER.info("Activity stopped");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        LOGGER.info("Back button was pressed");
        this.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ConstantKeywords.DONE_PAIRING_RESULT_CODE) {
            this.setResult(ConstantKeywords.DONE_PAIRING_RESULT_CODE);
            this.finish();
        } else if (resultCode == ConstantKeywords.CANCEL_PAIRING_RESULT_CODE) {
            this.setResult(ConstantKeywords.CANCEL_PAIRING_RESULT_CODE);
            this.finish();
        }
    }

    /**
     * Lines up all paired devices.
     */
    public void queryPairedDevices() {
        this.findViewById(R.id.paired_list).setVisibility(View.VISIBLE);

        this.pairedDevices = this.bluetoothAdapter.getBondedDevices();
        for (final BluetoothDevice device : this.pairedDevices) {
            if (this.pairedDevicesArrayAdapter.getPosition(device.getName() + "\n" + device.getAddress()) == -1) {
                this.pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        LOGGER.info("Pairing query done");
    }

    /**
     * Starts Discovering bluetooth devices
     */
    public void discoverDevices() {
        this.setTitle(R.string.scanning);
        this.stopDiscovering();

        final Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        this.startActivity(discoverableIntent);

        this.bluetoothAdapter.startDiscovery();
        LOGGER.info("Bluetooth service started discovering");
    }

    /**
     * Stops discovering bluetooth devices.
     */
    public void stopDiscovering() {
        if (this.bluetoothAdapter.isDiscovering()) {
            this.bluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * Called when the device is connected over Bluetooth
     *
     * @param event Describes the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothConnectedEvent(final BluetoothConnectedEvent event) {
        LOGGER.info("onBluetoothConnectedEvent called");
        Validate.notNull(event);
        if (this.isMaster) {
            final Intent intent = new Intent(this, SelectVerificationMethodActivity.class);
            this.startActivityForResult(intent, ConstantKeywords.START_RHYTHM_REQUEST_CODE);
            this.isMaster = false;
        } else {
            final Intent intent = new Intent(this, WaitActivity.class);
            intent.putExtra(ConstantKeywords.WAIT_MESSAGE, this.getString(R.string.wait_message_slave_verification_method));
            this.startActivityForResult(intent, ConstantKeywords.START_RHYTHM_REQUEST_CODE);
        }
    }

    /**
     * Add new device to the list of Bluetooth devices.
     *
     * @param intent The {@link BroadcastReceiver} {@code intent}.
     */
    private void addNewDevice(final Intent intent) {
        Validate.notNull(intent);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // Skip paired devices and devices without a valid name.
        if (this.isValidDevice(device) && device.getBondState() != BluetoothDevice.BOND_BONDED
                && this.newDevicesArrayAdapter.getPosition(device.getName() + "\n" + device.getAddress()) == -1) {
            this.newDevices.add(device);
            this.newDevicesArrayAdapter.add(getDeviceDescription(device));
        }
    }

    /**
     * Checks if the {@link BluetoothDevice} is not null, has a name and that the
     * name does not equal the string 'null'
     *
     * @param device The {@link BluetoothDevice} to check
     * @return If the device is valid
     */
    private boolean isValidDevice(final BluetoothDevice device) {
        return device != null && device.getName() != null && !device.getName().equals("null");
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private final class DeviceClickListener implements AdapterView.OnItemClickListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            // Cancel discovery because it's costly and we're about to connect
            BluetoothConnectActivity.this.stopDiscovering();

            // Get the device MAC address, which is the last 17 chars in the View
            final String info = ((TextView) view).getText().toString();
            if (info.equals(BluetoothConnectActivity.this.getString(R.string.no_devices_found))) {
                return;
            }

            BluetoothConnectActivity.this.isMaster = true;
            final String address = info.substring(info.length() - 17);
            final BluetoothDevice device = this.getDevice(address);
            BluetoothConnectActivity.this.bluetoothHandler.connect(device);
        }

        /**
         * Gets the device corresponding to the address from either the paired or the new devices.
         *
         * @param address The MAC address corresponding to the device to be found
         * @return The BLuetoothDevice corresponding to the mac address.
         */
        private BluetoothDevice getDevice(final String address) {
            assert address != null;
            assert address.length() >= MINIMUM_MAC_LENGTH;
            for (final BluetoothDevice device : BluetoothConnectActivity.this.pairedDevices) {
                if (device.getAddress().equals(address)) {
                    return device;
                }
            }
            for (final BluetoothDevice device : BluetoothConnectActivity.this.newDevices) {
                if (device.getAddress().equals(address)) {
                    return device;
                }
            }
            return null;
        }
    }

    private final class BluetoothDiscoverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Validate.notNull(context);
            Validate.notNull(intent);
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothConnectActivity.this.addNewDevice(intent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                this.setNoDevicesFound();
            }
        }

        /**
         * Set message when no devices
         */
        private void setNoDevicesFound() {
            BluetoothConnectActivity.this.setTitle(R.string.select_device);
            if (BluetoothConnectActivity.this.newDevicesArrayAdapter.getCount() == 0) {
                BluetoothConnectActivity.this.newDevicesArrayAdapter.add(BluetoothConnectActivity.this.getString(R.string.no_devices_found));
            }
        }
    }

}