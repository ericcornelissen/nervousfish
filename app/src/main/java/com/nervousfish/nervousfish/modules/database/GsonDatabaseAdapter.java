package com.nervousfish.nervousfish.modules.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.data_objects.Database;
import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.data_objects.Profile;
import com.nervousfish.nervousfish.exceptions.DatabaseException;
import com.nervousfish.nervousfish.modules.cryptography.IEncryptor;
import com.nervousfish.nervousfish.modules.filesystem.IFileSystem;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.ModuleWrapper;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

/**
 * An adapter class that adapter our {@link IDatabase} to the GSON serialization library, use for data storage
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity", "PMD.TooManyMethods"})
//  1 / 3) Suppressed because a DatabaseHandler has a lot of methods by nature and refactoring it to multiple
// classes or single methods with more logic would make the class only less understandable
//  2) Suppressed because of the by the pojo's created to write safely, we have to import this much
public final class GsonDatabaseAdapter implements IDatabase {

    private static final String CONTACT_NOT_FOUND = "Contact not found in database";
    private static final String CONTACT_DUPLICATE = "Contact is already in the database";
    private static final String DATABASE_NOT_CREATED = "Database is not created";
    private static final String DATABASE_WRONG_SIZE = "Database has the wrong size";
    private static final String BAD_KEY_SPEC = "Password leads to bad key spec";
    private static final String BAD_PADDING = "Database has bad padding";

    //  Strings for saving database related objects in the map
    private static final String DATABASE = "database";
    private static final String ENCRYPTED_PASSWORD = "encrypted password";
    private static final String ENCRYPTION_KEY = "encryption key";
    private static final Logger LOGGER = LoggerFactory.getLogger("GsonDatabaseAdapter");

    private final String databasePath;
    private final String passwordPath;

    private final Map<String, Object> databaseMap;

    private final IFileSystem fileSystem;
    private final GsonDatabaseAdapterLoader helper;
    private final IEncryptor encryptor;

    /**
     * Prevents construction from outside the class.
     *
     * @param serviceLocator Can be used to get access to other modules
     */
    private GsonDatabaseAdapter(final IServiceLocator serviceLocator) {
        assert serviceLocator != null;
        this.fileSystem = serviceLocator.getFileSystem();
        this.encryptor = serviceLocator.getEncryptor();
        this.databaseMap = new HashMap<>();
        this.databasePath = serviceLocator.getConstants().getDatabasePath();
        this.passwordPath = serviceLocator.getConstants().getPasswordPath();
        this.helper = new GsonDatabaseAdapterLoader(serviceLocator);
        LOGGER.info("Initialized");
    }

    /**
     * Creates a new instance of itself and wraps it in a {@link ModuleWrapper} so that only an
     * {@link IServiceLocator}
     *
     * @param serviceLocator The new service locator
     * @return A wrapper around a newly created instance of this class
     */
    public static ModuleWrapper<GsonDatabaseAdapter> newInstance(final IServiceLocator serviceLocator) {
        Validate.notNull(serviceLocator);
        return new ModuleWrapper<>(new GsonDatabaseAdapter(serviceLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContact(final Contact contact) throws IOException {
        Validate.notNull(contact);

        LOGGER.info("Adding new contact with name: {}", contact.getName());
        if (this.contactExists(contact.getName())) {
            throw new IllegalArgumentException(CONTACT_DUPLICATE);
        }
        LOGGER.info("Contact added");

        final List<Contact> contacts = this.getAllContacts();
        contacts.add(contact);

        this.updateDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContact(final String contactName) throws IOException {
        Validate.notBlank(contactName);
        final List<Contact> contacts = this.getAllContacts();
        final int lengthBefore = contacts.size();
        for (final Contact contact : contacts) {
            if (contactName.equals(contact.getName())) {
                contacts.remove(contact);
                break;
            }
        }

        // Throw if the contact to remove is not found
        if (contacts.size() == lengthBefore) {
            throw new IllegalArgumentException(CONTACT_NOT_FOUND);
        }

        this.updateDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContact(final Contact oldContact, final Contact newContact) throws IllegalArgumentException, IOException {
        Validate.notNull(oldContact);
        Validate.notNull(newContact);
        // Get the list of contacts
        final List<Contact> contacts = this.getAllContacts();
        final int lengthBefore = contacts.size();
        contacts.remove(oldContact);
        // Throw if the contact to update is not found
        if (contacts.size() == lengthBefore) {
            throw new IllegalArgumentException(CONTACT_NOT_FOUND);
        }

        contacts.add(newContact);
        this.updateDatabase();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> getAllContacts() {
        return this.getDatabase().getContacts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact getContactWithName(final String contactName) throws IOException {
        Validate.notBlank(contactName);
        final List<Contact> contacts = this.getAllContacts();
        for (final Contact contact : contacts) {
            if (contact.getName().equals(contactName)) {
                return contact;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contactExists(final String name) throws IOException {
        Validate.notBlank(name);
        return this.getContactWithName(name) != null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Profile getProfile() {
        return this.getDatabase().getProfile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProfile(final Profile newProfile) throws IOException {
        Validate.notNull(newProfile);
        final Database newDatabase = new Database(this.getDatabase().getContacts(), newProfile);
        this.databaseMap.put(DATABASE, newDatabase);
        this.updateDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDatabase() throws IOException {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
            final Gson gsonParser = gsonBuilder.create();

            final String databaseJson = gsonParser.toJson(this.getDatabase());
            final String encryptedDatabase = this.encryptor.encryptWithPassword(databaseJson, this.getEncryptionKey());
            try (Writer writer = this.fileSystem.getWriter(this.databasePath)) {
                writer.write(encryptedDatabase);
            }
        } catch (final IllegalBlockSizeException e) {
            LOGGER.error("Wrong size of password", e);
            throw new IOException("Password is wrong size", e);
        } catch (final BadPaddingException e) {
            LOGGER.error(BAD_PADDING, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDatabase(final String password) throws IOException {
        LOGGER.info("Started loading database");
        try {
            final SecretKey key = this.helper.loadKey(password);
            this.databaseMap.put(ENCRYPTION_KEY, key);
            final Database database = this.helper.loadDatabase(key);
            this.databaseMap.put(DATABASE, database);
        } catch (final InvalidKeySpecException e) {
            throw new IOException(BAD_KEY_SPEC, e);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createDatabase(final Profile profile, final String password) throws IOException {
        LOGGER.info("Started creating database for profile");
        this.initializePassword(password);
        this.initializeDatabase(profile, password);
        LOGGER.info("Database successfully created");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkFirstUse() {
        LOGGER.info("Checking first use of the database");
        return this.fileSystem.checkFileExists(this.passwordPath) && this.fileSystem.checkFileExists(this.databasePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDatabase() {
        LOGGER.info("Deleting database");
        return !this.checkFirstUse()
                || this.fileSystem.deleteFile(this.passwordPath) && this.fileSystem.deleteFile(this.databasePath);
    }


    /**
     * Initialize the main database. Does nothing when file is present.
     *
     * @param profile  The profile to initialize the database for.
     * @param password The password to initialize the database with.
     */
    private void initializeDatabase(final Profile profile, final String password) throws IOException {
        Validate.notNull(profile);
        Validate.notBlank(password);
        LOGGER.info("Database path created: {}", this.databasePath);

        this.databaseMap.put(DATABASE, new Database(new ArrayList<Contact>(), profile));

        final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();

        final String databaseJson = gsonParser.toJson(this.getDatabase());
        LOGGER.info("Database translated to json: {}", databaseJson);

        try {
            final SecretKey key = this.encryptor.makeKeyFromPassword(password);
            final String databaseEncrypted = this.encryptor.encryptWithPassword(databaseJson, key);

            try (Writer writer = this.fileSystem.getWriter(this.databasePath)) {
                writer.write(databaseEncrypted);
            }
            LOGGER.info("Created the database: {}", this.databasePath);
        } catch (final IllegalBlockSizeException e) {
            throw new IOException(DATABASE_WRONG_SIZE, e);
        } catch (final BadPaddingException e) {
            LOGGER.error(BAD_PADDING, e);
        }
    }

    /**
     * Initialize the password file with the keys to encrypt the database. When there's no file yet
     * it creates a new file and a new keypair. Does nothing when file is present.
     *
     * @param password The password to initialize the database with.
     */
    private void initializePassword(final String password) throws IOException {
        assert password != null;
        assert !password.isEmpty();
        LOGGER.info("Initializing password");
        this.databaseMap.put(ENCRYPTED_PASSWORD, this.encryptor.hashString(password));

        final String encryptedPassword = this.getEncryptedPassword();
        LOGGER.info("Encrypted password: {}", encryptedPassword);

        try (Writer writer = this.fileSystem.getWriter(this.passwordPath)) {
            writer.write(encryptedPassword);
        }
        LOGGER.info("Created the password file: {}", this.passwordPath);

    }


    /**
     * Gets the encrypted password from the databaseMap
     */
    private String getEncryptedPassword() throws DatabaseException {
        final Object object = this.databaseMap.get(ENCRYPTED_PASSWORD);
        if (object instanceof String) {
            return (String) object;
        } else {
            throw new DatabaseException(DATABASE_NOT_CREATED);
        }
    }

    /**
     * Gets the database from the databaseMap
     */
    private Database getDatabase() throws DatabaseException {
        final Object object = this.databaseMap.get(DATABASE);
        if (object instanceof Database) {
            return (Database) object;
        } else {
            throw new DatabaseException(DATABASE_NOT_CREATED);
        }
    }

    /**
     * Gets the encryption key from the databaseMap
     */
    private SecretKey getEncryptionKey() throws DatabaseException {
        final Object object = this.databaseMap.get(ENCRYPTION_KEY);
        if (object instanceof SecretKey) {
            return (SecretKey) object;
        } else {
            throw new DatabaseException(DATABASE_NOT_CREATED);
        }
    }
}
