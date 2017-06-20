package com.nervousfish.nervousfish.data_objects;

import com.nervousfish.nervousfish.ConstantKeywords;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import nl.tudelft.ewi.ds.bankver.IBAN;

/**
 * A Profile POJO to store a contact representing the user and the user's keypairs.
 */
@SuppressWarnings({"PMD.ImmutableField", "PMD.UselessParentheses"})
//1. We don't want the field iban to be final.
//2. The parentheses on line 104 are not useless.
public final class Profile implements Serializable {

    private static final long serialVersionUID = 8191245914949893284L;
    private final String name;
    private final List<KeyPair> keyPairs;
    private final IBAN iban;


    /**
     * The constructor for the {@link Profile} class.
     *
     * @param name     The contact belonging to the user.
     * @param keyPairs the public/private key-pairs of the user.
     */
    public Profile(final String name, final List<KeyPair> keyPairs) {
        this.keyPairs = keyPairs;
        this.name = name;
        this.iban = null;
    }

    /**
     * The constructor for the {@link Profile} class. This one
     * includes the IBAN.
     *
     * @param name     The contact belonging to the user.
     * @param keyPairs The public/private key-pairs of the user.
     * @param iban     The iban of the user.
     */
    public Profile(final String name, final List<KeyPair> keyPairs, final IBAN iban) {
        this.keyPairs = keyPairs;
        this.name = name;
        this.iban = iban;
    }

    /**
     * Adds a new keyPair to the profile
     *
     * @param keyPair the keyPair to add.
     */
    public void addKeyPair(final KeyPair keyPair) {
        keyPairs.add(keyPair);
    }

    /**
     * Returns the contact Object of the user
     *
     * @return - The Contact POJO.
     */
    public Contact getContact() {
        final List<IKey> publicKeys = new ArrayList<>();
        for (final KeyPair pair : keyPairs) {
            publicKeys.add(pair.getPublicKey());
        }
        return new Contact(name, publicKeys);
    }

    /**
     * Returns the Keypairs of the user.
     *
     * @return The list of keypairs of the user.
     */
    public List<KeyPair> getKeyPairs() {
        return keyPairs;
    }

    public String getName() {
        return this.name;
    }

    public IBAN getIban() {
        return this.iban;
    }

    /**
     * Retuns the string of the IBAN, and an empty string if the object is null.
     *
     * @return String of the IBAN, and an empty string if the object is null;
     */
    public String getIbanAsString() {
        if (this.getIban() != null) {
            return this.getIban().toString();
        }
        return "";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final Profile that = (Profile) o;

        return this.name.equals(that.name) && this.keyPairs.equals(that.keyPairs)
                && (this.iban == null ? that.getIban() == null : this.iban.equals(that.getIban()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name.hashCode() + this.keyPairs.hashCode() + Objects.hashCode(this.iban);
    }

    /**
     * Serialize the created proxy instead of this instance.
     */
    private Object writeReplace() {
        return new Profile.SerializationProxy(this);
    }

    /**
     * Ensure that no instance of this class is created because it was present in the stream. A correct
     * stream should only contain instances of the proxy.
     */
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(ConstantKeywords.PROXY_REQUIRED);
    }

    /**
     * Represents the logical state of this class and copies the data from that class without
     * any consistency checking or defensive copying.
     */
    private static final class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 8191245914949893284L;
        private final String name;
        private final KeyPair[] keyPairs;
        private IBAN iban;

        /**
         * Constructs a new SerializationProxy
         *
         * @param profile The current instance of the proxy
         */
        SerializationProxy(final Profile profile) {
            this.name = profile.name;
            this.keyPairs = profile.keyPairs.toArray(new KeyPair[profile.keyPairs.size()]);
            this.iban = profile.getIban();
        }

        /**
         * Not to be called by the user - resolves a new object of this proxy
         *
         * @return The object resolved by this proxy
         */
        private Object readResolve() {
            return new Profile(this.name, Arrays.asList(this.keyPairs), this.iban);
        }
    }

}
