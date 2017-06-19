package com.nervousfish.nervousfish.modules.constants;

import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.ModuleWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Class containing global constants for the application. These constants are definied in this file, because
 * they're used by multiple files and don't logically belong to any one of them.
 */
public final class Constants implements IConstants {

    private static final long serialVersionUID = -2465684567600871939L;
    private static final Logger LOGGER = LoggerFactory.getLogger("Constants");
    private static final String DB_USERDATA_PATH = "accountInformation.json";
    private static final String DB_CONTACTS_PATH = "contacts.json";
    private static final String DB_DATABASE_PATH = "database.txt";
    private static final String DB_PASSWORD_PATH = "databaseKey.txt";
    // Unique secure uuid for this application
    private static final UUID MY_UUID = UUID.fromString("2d7c6682-3b84-4d00-9e61-717bac0b2643");
    // Name for the SDP record when creating server socket
    private static final String NAME_SDP_RECORD = "BluetoothChatSecure";
    private transient String androidFilesDir;
    private transient String databaseContactsPath;
    private transient String databaseUserPath;

    /**
     * Prevents construction from outside the class.
     *
     * @param serviceLocator Can be used to get access to other modules
     */
    private Constants(final IServiceLocator serviceLocator) {
        this.androidFilesDir = serviceLocator.getAndroidFilesDir();
        this.initializePaths();
        LOGGER.info("Initialized");
    }

    /**
     * Creates a new instance of itself and wraps it in a {@link ModuleWrapper} so that only an
     * {@link IServiceLocator}
     *
     * @param serviceLocator The new service locator
     * @return A wrapper around a newly created instance of this class
     */
    public static ModuleWrapper<Constants> newInstance(final IServiceLocator serviceLocator) {
        return new ModuleWrapper<>(new Constants(serviceLocator));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getPasswordPath() {
        return this.androidFilesDir + Constants.DB_PASSWORD_PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabasePath() {
        return this.androidFilesDir + Constants.DB_DATABASE_PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseContactsPath() {
        return this.databaseContactsPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseUserdataPath() {
        return this.databaseUserPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUuid() {
        return MY_UUID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSDPRecord() {
        return NAME_SDP_RECORD;
    }

    /**
     * Deserialize the instance using readObject to ensure invariants and security.
     *
     * @param stream The serialized object to be deserialized
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.androidFilesDir = stream.readUTF();
        this.initializePaths();
        this.ensureClassInvariant();
    }

    /**
     * Used to improve performance / efficiency
     *
     * @param stream The stream to which this object should be serialized to
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeUTF(this.androidFilesDir);
    }

    /**
     * Ensure that the instance meets its class invariant
     */
    private void ensureClassInvariant() {
        assertThat(this.androidFilesDir, notNullValue());
        assertThat(this.databaseContactsPath, notNullValue());
        assertThat(this.databaseUserPath, notNullValue());
    }

    private void initializePaths() {
        //noinspection StringConcatenationMissingWhitespace because this is a file path
        this.databaseContactsPath = this.androidFilesDir + Constants.DB_CONTACTS_PATH;
        //noinspection StringConcatenationMissingWhitespace because this is a file path
        this.databaseUserPath = this.androidFilesDir + Constants.DB_USERDATA_PATH;
    }

    /**
     * The general result codes for inputfield checks.
     */
    public enum InputFieldResultCodes {
        CORRECT_FIELD, EMPTY_FIELD, TOO_SHORT_FIELD, INVALID_IBAN
    }

    /**
     * The more explicit result codes for inputfield checks.
     */
    public enum ExplicitFieldResultCodes {
        INPUT_CORRECT, PASSWORD_TOO_SHORT, PASSWORD_EMPTY, NAME_EMPTY,
        PASSWORDS_NOT_EQUAL, ALl_FIELDS_EMPTY, INVALID_IBAN
    }
}
