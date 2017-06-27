package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Profile;
import com.nervousfish.nervousfish.exceptions.DatabaseException;
import com.nervousfish.nervousfish.modules.pairing.events.BluetoothConnectedEvent;
import com.nervousfish.nervousfish.service_locator.BlockchainWrapper;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.NervousFish;

import org.apache.commons.lang3.Validate;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nl.tudelft.ewi.ds.bankver.BankVer;
import nl.tudelft.ewi.ds.bankver.IBAN;
import nl.tudelft.ewi.ds.bankver.bank.IBANVerifier;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

/**
 * The {@link Activity} that makes it possible for a user to encrypt his received
 * challenge from his bank through our app.
 */
public final class IbanSignActivity extends AppCompatActivity {

    static final Logger LOGGER = LoggerFactory.getLogger("IbanSignActivity");

    private IServiceLocator serviceLocator;
    private EditText ibanInput;
    private EditText challengeInput;
    private TextView challengeOutput;
    private BankVer bankVer;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_iban_sign);

        this.serviceLocator = NervousFish.getServiceLocator();

        final Profile profile;
        try {
            profile = serviceLocator.getDatabase().getProfile();
        } catch (final IOException e) {
            throw new DatabaseException(e);
        }

        final BlockchainWrapper blockchainWrapper = new BlockchainWrapper(profile);
        this.bankVer = new BankVer(this, blockchainWrapper);

        this.ibanInput = (EditText) this.findViewById(R.id.icon_iban_verify_challenge);
        this.challengeInput = (EditText) this.findViewById(R.id.iban_challenge);
        this.challengeOutput = (TextView) this.findViewById(R.id.iban_generate_key);

        LOGGER.info("Activity created");
    }

    /**
     * Gets triggered when the Submit button is clicked.
     *
     * @param view The {@link View} clicked
     */
    public void onSubmitClick(final View view) {
        Validate.notNull(view);
        final String challenge = challengeInput.getText().toString();
        IBAN iban = null;
        if (IBANVerifier.isValidIBAN(ibanInput.getText().toString())) {
            iban = new IBAN(ibanInput.getText().toString());
        }

        this.challengeOutput.setText(this.bankVer.handleManualMessage(iban, challenge));
        final ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(MIMETYPE_TEXT_PLAIN, this.challengeOutput.getText());
        clipboard.setPrimaryClip(clip);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        this.serviceLocator.registerToEventBus(this);

        LOGGER.info("Activity started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        LOGGER.info("Activity resumed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        this.serviceLocator.unregisterFromEventBus(this);

        LOGGER.info("Activity stopped");
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
        intent.putExtra(ConstantKeywords.WAIT_MESSAGE, this.getString(R.string.wait_message_slave_verification_method));
        this.startActivityForResult(intent, ConstantKeywords.START_RHYTHM_REQUEST_CODE);
    }

}
