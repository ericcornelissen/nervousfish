package com.nervousfish.nervousfish.modules.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.modules.constants.IConstants;
import com.nervousfish.nervousfish.events.SLReadyEvent;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.IServiceLocatorCreator;
import com.nervousfish.nervousfish.service_locator.ModuleWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.Subscribe;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An adapter to the GSON database library
 */
public final class GsonDatabaseAdapter implements IDatabase {

    private final static Type TYPE_CONTACT_LIST = new TypeToken<ArrayList<Contact>>(){}.getType();

    @SuppressWarnings("PMD.SingularField")
    private final IServiceLocatorCreator serviceLocatorCreator;
    private IConstants constants; // Can't be final because it cannot be set in the constructor atm...

    /**
     * Prevents construction from outside the class.
     *
     * @param serviceLocatorCreator The object responsible for creating the service locator
     */
    private GsonDatabaseAdapter(final IServiceLocatorCreator serviceLocatorCreator) {
        this.serviceLocatorCreator = serviceLocatorCreator;
        this.serviceLocatorCreator.registerToEventBus(this);
    }

    /**
     * Creates a new instance of itself and wraps it in a {@link ModuleWrapper} so that only an
     * {@link IServiceLocatorCreator} can access the new module to create the new
     * {@link IServiceLocator}.
     *
     * @param serviceLocatorCreator The service locator bridge that creates the new service locator
     * @return A wrapper around a newly created instance of this class
     */
    public static ModuleWrapper<GsonDatabaseAdapter> newInstance(final IServiceLocatorCreator serviceLocatorCreator) {
        return new ModuleWrapper<>(new GsonDatabaseAdapter(serviceLocatorCreator));
    }

    /**
     * {@inheritDoc}
     */
    @Subscribe
    @Override
    public void onSLReadyEvent(final SLReadyEvent event) {
        final IServiceLocator serviceLocator = serviceLocatorCreator.getServiceLocator();
        this.constants = serviceLocator.getConstants();

        try {
            this.initializeDatabase();
        } catch (final IOException e) {
            // TODO: Handle failure
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContact(final Contact contact) throws IOException {
        // Get the list of contacts and add the new contact
        final List<Contact> contacts = this.getAllContacts();
        contacts.add(contact);

        // Update the database
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();

        final String contactsPath = this.constants.getDatabaseContactsPath();
        final Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(contactsPath), "UTF-8"));

        gsonParser.toJson(contacts, writer);
        writer.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContact(final Contact contact) throws IllegalArgumentException, IOException {
        // Get the list of contacts
        final List<Contact> contacts = this.getAllContacts();
        final int lengthBefore = contacts.size();
        contacts.remove(contact);

        // Throw if the contact to remove is not found
        if (contacts.size() == lengthBefore) {
            throw new IllegalArgumentException("Contact not found in database");
        }

        // Update the database
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();

        final String contactsPath = this.constants.getDatabaseContactsPath();
        final Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(contactsPath), UTF_8));

        gsonParser.toJson(contacts, writer);
        writer.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> getAllContacts() throws IOException {
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();

        final String contactsPath = this.constants.getDatabaseContactsPath();
        final Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(contactsPath), UTF_8));

        final List<Contact> contacts = gsonParser.fromJson(reader, TYPE_CONTACT_LIST);
        reader.close();

        return contacts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContact(final Contact oldContact, final Contact newContact) throws IllegalArgumentException, IOException {
        // Get the list of contacts
        final List<Contact> contacts = this.getAllContacts();
        final int lengthBefore = contacts.size();
        contacts.remove(oldContact);

        // Throw if the contact to update is not found
        if (contacts.size() == lengthBefore) {
            throw new IllegalArgumentException("Contact not found in database");
        }

        contacts.add(newContact);

        // Update the database
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(IKey.class, new GsonKeyAdapter());
        final Gson gsonParser = gsonBuilder.create();

        final String contactsPath = this.constants.getDatabaseContactsPath();
        final Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(contactsPath), UTF_8));

        gsonParser.toJson(contacts, writer);
        writer.close();
    }

    /**
     * Initialize the database.
     */
    private void initializeDatabase() throws IOException {
        this.initializeContacts();
        this.initializeUserdata();
    }

    /**
     * Initialize the contacts in the database. This does nothing
     * if the contacts section of the database already exists.
     */
    private void initializeContacts() throws IOException {
        final String contactsPath = constants.getDatabaseContactsPath();
        final File file = new File(contactsPath);
        if (!file.exists()) {
            final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(contactsPath), UTF_8));
            writer.write("[]");
            writer.close();
        }
    }

    /**
     * Initialize the userdata in the database. This does nothing
     * if the userdata section of the database already exists.
     */
    private void initializeUserdata() throws IOException {
        final String userdataPath = constants.getDatabaseUserdataPath();
        final File file = new File(userdataPath);
        if (!file.exists()) {
            final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(userdataPath), UTF_8));
            writer.write("[]");
            writer.close();
        }
    }

}
