package com.nervousfish.nervousfish.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.data_objects.KeyPair;
import com.nervousfish.nervousfish.data_objects.Profile;
import com.nervousfish.nervousfish.modules.cryptography.IKeyGenerator;
import com.nervousfish.nervousfish.modules.database.IDatabase;
import com.nervousfish.nervousfish.modules.qr.QRGenerator;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * An {@link Activity} that is used for pairing using QR codes
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.AccessorClassGeneration"})
//  1)  This warning is because the class relies on too many external classes, which can't really be avoided
//  2)  This warning doesn't make sense since I can't instantiate the object in the constructor as I
//      need the qr message to create the editnameclicklistener in the addnewcontact method
public class QRExchangeKeyActivity extends AppCompatActivity {

    private static final String SPACE = " ";
    private static final String SEMICOLON = ";";
    private static final Logger LOGGER = LoggerFactory.getLogger("QRExchangeKeyActivity");

    private IServiceLocator serviceLocator;
    private AlertDialog lastDialog;
    private Profile profile;

    /**
     * Creates the new activity, should only be called by Android
     *
     * @param savedInstanceState Don't touch this
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_qrexchange);

        final Intent intent = getIntent();
        this.serviceLocator = (IServiceLocator) intent.getSerializableExtra(ConstantKeywords.SERVICE_LOCATOR);

        try {
            profile = this.serviceLocator.getDatabase().getProfiles().get(0);
        } catch (IOException e) {
            LOGGER.error("Loading the public key went wrong", e);
        }

        showQRCode();
    }

    /**
     * Returns to the previous activity.
     * @param view - the imagebutton
     */
    public void onBackButtonClick(final View view) {
        LOGGER.info("Return to previous screen");
        finish();
    }

    /**
     * Starts the scan qr feature.
     * @param view - the imagebutton
     */
    public void onScanButtonClick(final View view) {
        LOGGER.info("Started scanning QR code");
        final IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        LOGGER.info("Activity resulted");
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult == null) {
            LOGGER.error("No scan result in QR Scanner");
        } else {
            final String result = scanResult.getContents();
            addNewContact(result);
        }


    }


    /**
     * Adds new contact with the scanned key and opens change
     *
     * @param qrMessage The information we got from the QR code.
     */
    private void addNewContact(final String qrMessage) {
        try {
            LOGGER.info("Adding new contact to database");
            //Name is the first part
            final String name = qrMessage.split(SEMICOLON)[0];
            //Key is the second part
            final IKey key = QRGenerator.deconstructToKey(qrMessage.split(SEMICOLON)[1]);

            final Contact contact = new Contact(name, key);
            this.serviceLocator.getDatabase().addContact(contact);

            final Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(ConstantKeywords.SERVICE_LOCATOR, serviceLocator);
            intent.putExtra(ConstantKeywords.SUCCESSFUL_EXCHANGE, true);
            this.startActivity(intent);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument exception in addNewContact", e);
        } catch (IOException e) {
            LOGGER.error("IOException in addNewContact", e);
        }
    }

    /**
     * Shows the QR Code in the activity.
     */
    @SuppressLint("InflateParams")
    private void showQRCode() {
        final IKey publicKey = profile.getPublicKey();
        final Bitmap qrCode = QRGenerator.encode(profile.getName() + SEMICOLON + publicKey.getType()
                + SPACE + publicKey.getName() + SPACE + publicKey.getKey());

        final ImageView imageView = (ImageView) this.findViewById(R.id.QR_code_image);
        imageView.setImageBitmap(qrCode);
        imageView.setMaxWidth(30);
    }
}
