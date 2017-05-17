package com.nervousfish.nervousfish.modules.pairing;

import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.modules.database.IDatabase;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Contains common methods shared by all pairing modules.
 */
abstract class APairingHandler implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger("APairingHandler");

    private final IDatabase database;

    /**
     * Prevent instantiation by other classes outside it's package
     *
     * @param serviceLocator the serviceLocator which will provide means to access other modules
     */
    // This servicelocator will be used later on probably
    APairingHandler(final IServiceLocator serviceLocator) {
        database = serviceLocator.getDatabase();
    }

    /**
     * Luxury method that calls writeContact() for each contact of the database.
     *
     * @throws IOException When deserialization doesn't go well.
     */
    public void writeAllContacts() throws IOException {
        final List<Contact> list = database.getAllContacts();
        for (final Contact e : list) {
            writeContact(e);
        }
    }

    /**
     * Serializes a contact object and writes it, which is implemented in the specific subclass.
     *
     * @param contact contact to serialize
     * @throws IOException When deserialization doesn't go well.
     */
    void writeContact(final Contact contact) throws IOException {
        if (!checkExists(contact)) {
            byte[] bytes = null;
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(contact);
                oos.flush();
                bytes = bos.toByteArray();
            } finally {
                if (oos != null) {
                    oos.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }
            write(bytes);
        }
    }

    /**
     * Checks if a name of a given contact exists in the database.
     *
     * @param contact A contact object
     * @return true when a contact with the same exists in the database
     * @throws IOException When database fails to respond
     */
    boolean checkExists(final Contact contact) throws IOException {
        final String name = contact.getName();
        final List<Contact> list = database.getAllContacts();
        for (final Contact e : list) {
            if (e.getName().equals(name)) {
                return true;
            }
        }
        showWarning();
        return false;
    }

    /**
     * Deserializes a contact through a byte array and sends it to the database.
     *
     * @param bytes byte array representing a contact
     * @return Whether or not the process finished successfully
     */
    boolean saveContact(final byte[] bytes) {
        Contact contact = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            contact = (Contact) ois.readObject();
        } catch (final ClassNotFoundException | IOException e) {
            LOGGER.error(" Couldn't start deserialization!");
            return false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (final IOException e) {
                    LOGGER.warn("Couldn't close the ByteArrayInputStream");
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (final IOException e) {
                    LOGGER.warn("Couldn't close the ObjectInputStream");
                }
            }
        }
        try {
            database.addContact(contact);
        } catch (final IOException e) {
            LOGGER.warn("DB issued an error while saving contact");
            return false;
        }
        return true;
    }

    /**
     * Abstract method in order to write to the connected Device via outputstream.
     *
     * @param buffer The bytes to write
     */
    abstract void write(final byte[] buffer);

    /**
     * Abstract method that handles warnings/errors and shows them to the user.
     */
    abstract void showWarning();
}
