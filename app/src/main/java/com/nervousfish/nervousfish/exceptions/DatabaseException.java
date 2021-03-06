package com.nervousfish.nervousfish.exceptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Thrown when there is an issue with the database
 */
public final class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = -5464890114687852303L;

    /**
     * Constructs a new Database Exception, thrown when there is an issue with the database
     *
     * @param msg A message describing the event that happened
     */
    public DatabaseException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new Datbase Exception, thrown when there is an issue with the database
     *
     * @param throwable The root of th exception
     */
    public DatabaseException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Deserialize the instance using readObject to ensure invariants and security.
     *
     * @param stream The serialized object to be deserialized
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.ensureClassInvariant();
    }

    /**
     * Used to improve performance / efficiency
     *
     * @param stream The stream to which this object should be serialized to
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    /**
     * Ensure that the instance meets its class invariant
     */
    private void ensureClassInvariant() {
        // No checks to perform
    }

}
