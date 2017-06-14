package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.modules.database.IDatabase;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.NervousFish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An {@link Activity} that draws the screen that is used to login by entering a password.
 */
public final class LoginActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger("LoginActivity");

    private String actualPassword;
    private KeyboardView keyboardView;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login);

        final IServiceLocator serviceLocator = NervousFish.getServiceLocator();
        final IDatabase database = serviceLocator.getDatabase();
        try {
            this.actualPassword = database.getUserPassword();
        } catch (final IOException e) {
            LOGGER.error("Failed to retrieve password from database", e);
        }

        final Keyboard keyboard = new Keyboard(this, R.xml.qwerty);
        this.keyboardView = (KeyboardView) this.findViewById(R.id.keyboardview);
        this.keyboardView.setKeyboard(keyboard);
        this.keyboardView.setPreviewEnabled(false);
        this.keyboardView.setOnKeyboardActionListener(new MyOnKeyboardActionListener());

        this.findViewById(R.id.login_password_input).setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                this.showCustomKeyboard(v);
            } else {
                this.hideCustomKeyboard();
            }
        });
        this.findViewById(R.id.login_password_input).setOnClickListener(this::showCustomKeyboard);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        LOGGER.info("Activity created");
    }

    public void hideCustomKeyboard() {
        this.keyboardView.setVisibility(View.GONE);
        this.keyboardView.setEnabled(false);
    }

    public void showCustomKeyboard(final View view) {
        this.keyboardView.setVisibility(View.VISIBLE);
        this.keyboardView.setEnabled(true);
        ((InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean isCustomKeyboardVisible() {
        return this.keyboardView.getVisibility() == View.VISIBLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if (this.isCustomKeyboardVisible()) {
            this.hideCustomKeyboard();
        } else {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
    }

    /**
     * Validate a login attempt.
     *
     * @param view The submit button that was clicked
     */
    public void validateLoginAttempt(final View view) {
        LOGGER.info("Submit button clicked");

        final View mError = this.findViewById(R.id.error_message_login);
        final EditText passwordInput = (EditText) this.findViewById(R.id.login_password_input);

        final boolean skipPassword = passwordInput.getText().toString().isEmpty();
        if (skipPassword) {
            LOGGER.warn("Password skipped!");
            mError.setVisibility(View.GONE);
            this.toMainActivity();
        } else {
            final String providedPassword = passwordInput.getText().toString();
            final boolean wrongPassword = !providedPassword.equals(this.actualPassword);
            if (wrongPassword) {
                LOGGER.warn("Password incorrect!");
                mError.setVisibility(View.VISIBLE);
            } else {
                LOGGER.info("Password correct");
                mError.setVisibility(View.GONE);
                this.toMainActivity();
            }
        }
    }

    /**
     * Go to the next activity from the {@link LoginActivity}.
     */
    private void toMainActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    private class MyOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {
        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            final View focusCurrent = LoginActivity.this.getWindow().getCurrentFocus();
            if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            editable.insert(start, Character.toString((char) primaryCode));
        }

        @Override
        public void onText(final CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }
}
