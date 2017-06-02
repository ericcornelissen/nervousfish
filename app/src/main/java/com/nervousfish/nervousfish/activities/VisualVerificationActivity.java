package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.data_objects.Profile;
import com.nervousfish.nervousfish.data_objects.SimpleKey;
import com.nervousfish.nervousfish.modules.pairing.events.NewDataReceivedEvent;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Example activity to verify identity in Bluetooth connections.
 */
public class VisualVerificationActivity extends Activity {

    private static final Logger LOGGER = LoggerFactory.getLogger("VisualVerificationActivity");
    private static final int SECURITY_CODE_LENGTH = 5;

    private IServiceLocator serviceLocator;
    private String securityCode = "";
    private Contact dataReceived;

    /**
     * Stuff that needs to be done when the new activity being created.
     *
     * @param savedInstanceState The saved state
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.visual_verification);

        final Intent intent = this.getIntent();
        this.serviceLocator = (IServiceLocator) intent.getSerializableExtra(ConstantKeywords.SERVICE_LOCATOR);

        LOGGER.info("VisualVerificationActivity created");
    }


    /**
     * Gets triggered when the done button is clicked.
     *
     * @param v - the {@link View} clicked
     */
    public void onDoneCreatingRhythmClick(final View v) {


    }
    /**
     * Go to the next activity and provide it with the generated pattern.
     */
    private void nextActivity() {
        LOGGER.info("Done tapping the VisualVerification");
        try {
            final Profile myProfile = this.serviceLocator.getDatabase().getProfiles().get(0);

            LOGGER.info("Sending my profile with name: " + myProfile.getName() + ", public key: "
                    + myProfile.getPublicKey().toString());
            final Contact myProfileAsContact = new Contact(myProfile.getName(), new SimpleKey("simplekey", "73890ien"));
            this.serviceLocator.getBluetoothHandler().send(myProfileAsContact);
        } catch (IOException e) {
            LOGGER.error("Could not send my contact to other device " + e.getMessage());
        }

        final Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, serviceLocator);
        intent.putExtra(ConstantKeywords.WAIT_MESSAGE, this.getString(R.string.wait_message_partner_rhythm_tapping));
        intent.putExtra(ConstantKeywords.DATA_RECEIVED, dataReceived);
        intent.putExtra(ConstantKeywords.TAP_DATA, securityCode);
        this.startActivityForResult(intent, ConstantKeywords.START_RHYTHM_REQUEST_CODE);
    }

    /**
     * Action to perform when clicking on a button in the activity.
     *
     * @param v The view of the button being clicked.
     */
    public void buttonAction(final View v) {
        final String button = v.getContentDescription().toString();
        LOGGER.info("button '%s' clicked", button);

        if (this.securityCode.length() > VisualVerificationActivity.SECURITY_CODE_LENGTH) {
            LOGGER.warn("Security code already long enough");
        } else if (this.securityCode.length() + 1 == VisualVerificationActivity.SECURITY_CODE_LENGTH) {
            this.securityCode += button;
            LOGGER.info("final code is: %s", this.securityCode);
            this.nextActivity();
        } else {
            this.securityCode += button;
            LOGGER.info("code so far: %s", this.securityCode);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.serviceLocator.registerToEventBus(this);
    }

    @Override
    protected void onStop() {
        this.serviceLocator.unregisterFromEventBus(this);
        super.onStop();
    }

    /**
     * Called when a new data is received.
     *
     * @param event Contains additional data about the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewDataReceivedEvent(final NewDataReceivedEvent event) {
        LOGGER.info("onNewDataReceivedEvent called");
        if (event.getClazz().equals(Contact.class)) {
            final Contact contact = (Contact) event.getData();
            try {
                LOGGER.info("Adding contact to database...");
                this.serviceLocator.getDatabase().addContact(contact);
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.error("Couldn't get contacts from database", e);
            }

            //This needs to be outside of the try catch block
            this.dataReceived = contact;
        }
    }

}
