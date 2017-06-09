package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.Label;
import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.exceptions.NoBluetoothException;
import com.nervousfish.nervousfish.modules.pairing.events.BluetoothConnectedEvent;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * The main {@link Activity} that shows a list of all contacts and a button that lets you obtain new
 * public keys from other people
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling",
        "PMD.ExcessiveImports", "PMD.TooFewBranchesForASwitchStatement", "PMD.TooManyMethods"})
//  1)  This warning is because it relies on too many other classes, yet there's still methods like fill databasewithdemodata
//      which will be deleted later on
//  2)  This warning means there are too many instantiations of other classes within this class,
//      which basically comes down to the same problem as the last
//  3)  Another suppression based on the same problem as the previous 2
//  4)  The switch statement for switching sorting Types does not have enough branches, because it is designed
//      to be extended when necessairy to more sorting Types.
//  5)  Suppressed because this rule is not meant for Android classes like this, that have no other choice
//      than to add methods for overriding the activity state machine and providing View click listeners
public final class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger("MainActivity");
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH_ON_START = 100;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH_ON_BUTTON_CLICK = 200;

    private List<Contact> contacts;

    private IServiceLocator serviceLocator;
    private MainActivitySorter sorter;

    /**
     * Creates the new activity, should only be called by Android
     *
     * @param savedInstanceState Don't touch this
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        this.serviceLocator = (IServiceLocator) intent.getSerializableExtra(ConstantKeywords.SERVICE_LOCATOR);
        this.setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        this.setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        try {
            this.contacts = this.serviceLocator.getDatabase().getAllContacts();
        } catch (final IOException e) {
            LOGGER.error("Failed to retrieve contacts from database", e);
        }

        sorter = new MainActivitySorter(this);
        sorter.sortOnName();

        try {
            this.serviceLocator.getBluetoothHandler().start();
        } catch (NoBluetoothException e) {
            LOGGER.info("Bluetooth not available on device, disabling button");
            final FloatingActionButton button = (FloatingActionButton) this.findViewById(R.id.pairing_menu_bluetooth);
            button.setEnabled(false);
        } catch (IOException e) {
            LOGGER.info("Bluetooth handler not started, most likely Bluetooth is not enabled");
            this.enableBluetooth(false);
        }

        final Object successfulBluetooth = intent.getSerializableExtra(ConstantKeywords.SUCCESSFUL_BLUETOOTH);
        this.showSuccessfulBluetoothPopup(successfulBluetooth);

        LOGGER.info("MainActivity created");
    }

    /**
     * Shows a popup that adding a contact went fine if the boolean
     * added in the intent is true.
     *
     * @param successfulBluetooth The intents value for {@code SUCCESSFUL_BLUETOOTH}.
     */
    private void showSuccessfulBluetoothPopup(final Object successfulBluetooth) {
        if (successfulBluetooth != null) {
            final boolean success = (boolean) successfulBluetooth;
            if (success) {
                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(this.getString(R.string.contact_added_popup_title))
                        .setContentText(this.getString(R.string.contact_added_popup_explanation))
                        .setConfirmText(this.getString(R.string.dialog_ok))
                        .show();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            this.contacts = serviceLocator.getDatabase().getAllContacts();
            sorter.sortOnName();
        } catch (final IOException e) {
            LOGGER.error("onResume in MainActivity threw an IOException", e);
        }
    }

    /**
     * Switches the sorting mode.
     *
     * @param view The sort floating action button that was clicked
     */
    public void onSortButtonClicked(final View view) {
        sorter.onSortButtonClicked(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        this.serviceLocator.registerToEventBus(this);
        try {
            this.contacts = this.serviceLocator.getDatabase().getAllContacts();
        } catch (final IOException e) {
            LOGGER.error("onStart in MainActivity threw an IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        this.serviceLocator.unregisterFromEventBus(this);
        super.onStop();
    }

    /**
     * Goes to the activity associated with the view after clicking a pairing button
     *
     * @param view The view that was clicked
     */
    public void onPairingButtonClicked(final View view) {
        // Close the FAB
        ((FloatingActionMenu) this.findViewById(R.id.pairing_button)).close(true);

        // Open the correct pairing activity
        final Intent intent = new Intent();
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);

        String textOnLabel = "";
        if (view instanceof Label) {
            textOnLabel = ((Label) view).getText().toString();
        }

        if (view.getId() == R.id.pairing_menu_bluetooth || textOnLabel.equals(getResources().getString(R.string.bluetooth))) {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                intent.setComponent(new ComponentName(this, BluetoothConnectionActivity.class));
                this.startActivity(intent);
            } else {
                this.enableBluetooth(true);
                return; // Prevent `this.startActivity()`
            }
        } else if (view.getId() == R.id.pairing_menu_nfc || textOnLabel.equals(getResources().getString(R.string.nfc))) {
            intent.setComponent(new ComponentName(this, NFCActivity.class));
        } else if (view.getId() == R.id.pairing_menu_qr || textOnLabel.equals(getResources().getString(R.string.qr))) {
            intent.setComponent(new ComponentName(this, QRExchangeKeyActivity.class));
        } else {
            LOGGER.error("Unknown pairing button clicked: " + view.getId());
            throw new IllegalArgumentException("Only existing buttons can be clicked");
        }

        this.startActivity(intent);
    }

    /**
     * Gets triggered when the 3 dots button in the toolbar is clicked.
     *
     * @param view The view that was clicked
     */
    public void onClickDotsButton(final View view) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);
        this.startActivity(intent);
    }

    /**
     * Temporary method to open the {@link ContactActivity} for a contact.
     *
     * @param index The index of the contact in {@code this.contacts}.
     */
    void openContact(final int index) {
        final Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);
        intent.putExtra(ConstantKeywords.CONTACT, this.contacts.get(index));
        this.startActivity(intent);
    }

    /**
     * Called when a Bluetooth connection is established.
     *
     * @param event Contains additional data about the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewBluetoothConnectedEvent(final BluetoothConnectedEvent event) {
        LOGGER.info("onNewBluetoothConnectedEvent called");

        final Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);
        intent.putExtra(ConstantKeywords.WAIT_MESSAGE, getString(R.string.wait_message_slave_verification_method));
        this.startActivityForResult(intent, ConstantKeywords.START_RHYTHM_REQUEST_CODE);
    }

    /**
     * Exit the application when the user taps the back button twice
     */
    @Override
    public void onBackPressed() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(this.getString(R.string.popup_log_out_title))
                .setContentText(this.getString(R.string.popup_log_out_description))
                .setCancelText(this.getString(R.string.no))
                .setConfirmText(this.getString(R.string.yes))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, serviceLocator);
                        startActivity(intent);
                    }
                })
                .show();
    }

    /**
     * Prompt user to enable Bluetooth if it is disabled.
     */
    private void enableBluetooth(final boolean buttonClicked) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (buttonClicked && bluetoothAdapter.isEnabled()) {
            final Intent intent = new Intent(this, BluetoothConnectionActivity.class);
            intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);
            this.startActivity(intent);
        } else if (!bluetoothAdapter.isEnabled()) {
            final String description;
            if (buttonClicked) {
                description = this.getString(R.string.popup_enable_bluetooth_exchange);
            } else {
                description = this.getString(R.string.popup_enable_bluetooth_findable);
            }

            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(this.getString(R.string.popup_enable_bluetooth_title))
                    .setContentText(description)
                    .setCancelText(this.getString(R.string.no))
                    .setConfirmText(this.getString(R.string.yes))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog dialog) {
                            dialog.dismiss();

                            LOGGER.info("Requesting to enable Bluetooth");
                            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            if (buttonClicked) {
                                startActivityForResult(intent, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH_ON_BUTTON_CLICK);
                            } else {
                                startActivityForResult(intent, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH_ON_START);
                            }
                            LOGGER.info("Request to enable Bluetooth sent");
                        }
                    })
                    .show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH_ON_BUTTON_CLICK) {
            final Intent intent = new Intent(this, BluetoothConnectionActivity.class);
            intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, this.serviceLocator);
            this.startActivity(intent);
        }
    }

    List<Contact> getContacts() {
        return contacts;
    }

}