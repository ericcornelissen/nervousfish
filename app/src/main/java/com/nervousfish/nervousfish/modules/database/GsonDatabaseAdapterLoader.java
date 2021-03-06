package com.nervousfish.nervousfish.modules.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nervousfish.nervousfish.data_objects.Database;
import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.exceptions.DatabaseException;
import com.nervousfish.nervousfish.modules.cryptography.IEncryptor;
import com.nervousfish.nervousfish.modules.filesystem.IFileSystem;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

/**
 * Helper method for the logical functionality of the {@link GsonDatabaseAdapter}.
 */
class GsonDatabaseAdapterLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("GsonDatabaseAdapter");
    private static final Type TYPE_DATABASE = new TypeToken<Database>() {
    }.getType();
    private static final String BAD_PADDING = "Database has bad padding";
    private static final String DATABASE_WRONG_SIZE = "Database has the wrong size";

    private final IFileSystem fileSystem;
    private final IEncryptor encryptor;
    private final String databasePath;
    private final String passwordPath;


    /**
     * Helper class for GsonDatabaseAdapter.
     *
     * @param serviceLocator Can be used to get access to other modules
     */
    GsonDatabaseAdapterLoader(final IServiceLocator serviceLocator) {
        Validate.notNull(serviceLocator);
        this.fileSystem = serviceLocator.getFileSystem();
        this.encryptor = serviceLocator.getEncryptor();
        this.databasePath = serviceLocator.getConstants().getDatabasePath();
        this.passwordPath = serviceLocator.getConstants().getPasswordPath();
    }

    /**
     * Reads the database to a string.
     *
     * @return A String containing the json database
     */
    private String readDatabaseToString() throws IOException {
        final StringBuilder databaseFileStringBuilder = new StringBuilder();

        // Get the database from the database file
        try (BufferedReader databaseReader = (BufferedReader) this.fileSystem.getReader(this.databasePath)) {

            while (true) {
                final String line = databaseReader.readLine();
                if (line == null) {
                    break;
                }
                databaseFileStringBuilder.append(line);
            }
        }
        return databaseFileStringBuilder.toString();
    }

    /**
     * Checks the password, throws IOException if it's wrong.
     *
     * @param password -   The password to check.
     * @return Whether the password is correct.
     * @throws IOException Thrown when password is wrong.
     */
    private boolean checkPassword(final String password) throws IOException {
        Validate.notBlank(password);
        final BufferedReader passReader = (BufferedReader) this.fileSystem.getReader(this.passwordPath);
        final StringBuffer passwordFileStringBuffer = new StringBuffer();
        while (true) {
            final String line = passReader.readLine();
            if (line == null) {
                break;
            }
            passwordFileStringBuffer.append(line);
        }
        passReader.close();
        final String encryptedPassword = passwordFileStringBuffer.toString();
        return encryptedPassword.equals(this.encryptor.hashString(password));
    }

    /**
     * Loads the Secretkey from a given password.
     *
     * @param password The password to load a key from
     * @return The secretKey made from the password\
     */
    public SecretKey loadKey(final String password) throws IOException, InvalidKeySpecException {
        Validate.notBlank(password);
        this.checkPassword(password);
        if (this.checkPassword(password)) {
            return this.encryptor.makeKeyFromPassword(password);
        } else {
            throw new IOException("Password is wrong");
        }
    }

    /**
     * Loads the database from file.
     *
     * @param key The key to decrypt the database
     * @return The loaded database POJO.
     */
    public Database loadDatabase(final SecretKey key) throws IOException {
        LOGGER.info("Started loading database");
        Validate.notNull(key);
        final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();
        try {
            final String databaseFileString = this.readDatabaseToString();
            final String databaseJson = this.encryptor.decryptWithPassword(databaseFileString, key);
            return gsonParser.fromJson(databaseJson, TYPE_DATABASE);
        } catch (final IllegalBlockSizeException e) {
            throw new IOException(DATABASE_WRONG_SIZE, e);
        } catch (final BadPaddingException e) {
            LOGGER.error(BAD_PADDING, e);
            throw new DatabaseException(e);
        }
    }
}
