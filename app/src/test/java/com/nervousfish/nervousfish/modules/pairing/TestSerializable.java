package com.nervousfish.nervousfish.modules.pairing;

import com.nervousfish.nervousfish.ConstantKeywords;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * This class is used to be serialized whenever some test wants to serialize / deserialize something and doesn't
 * care what the class it serializes / deserializes is.
 * The reason we don't take a class from the app itself, is because of none of those classes we can be sure they
 * keep existing.
 */
final class TestSerializable implements Serializable {

    private static final long serialVersionUID = -4715364587956219157L;
    private final String name;

    /**
     * Constructs a new Serializable object for test purposes with a random string as name.
     *
     * @param name a random String
     */
    TestSerializable(final String name) {
        this.name = name;
    }

    /**
     * Getter for the name of this class.
     *
     * @return The random name of this class, that was injected in the constructor
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        final TestSerializable other = (TestSerializable) obj;
        return this.name.equals(other.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Serialize the created proxy instead of this instance.
     */
    private Object writeReplace() {
        return new TestSerializable.SerializationProxy(this);
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
        private static final long serialVersionUID = -4715364587956219157L;
        private final String name;

        /**
         * Constructs a new SerializationProxy
         *
         * @param test The current instance of the proxy
         */
        SerializationProxy(final TestSerializable test) {
            this.name = test.name;
        }

        /**
         * Not to be called by the user - resolves a new object of this proxy
         *
         * @return The object resolved by this proxy
         */
        private Object readResolve() {
            return new TestSerializable(this.name);
        }
    }
}
