package com.nervousfish.nervousfish.modules.filesystem;

import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.ModuleWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;


/**
 * An adapter to the default Android file system
 */
// We suppress this warning because this class is by nature tightly coupled to the Java filesystem, which has a lot of separate classes unfortunately
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public final class AndroidFileSystemAdapter implements IFileSystem {
    private static final long serialVersionUID = 1937542180968231197L;
    private static final Logger LOGGER = LoggerFactory.getLogger("AndroidFileSystemAdapter");

    /**
     * Prevents construction from outside the class.
     *
     * @param serviceLocator Can be used to get access to other modules
     */
    // We suppress UnusedFormalParameter because the chance is big that a service locator will be used in the future
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private AndroidFileSystemAdapter(final IServiceLocator serviceLocator) {
        LOGGER.info("Initialized");
    }

    /**
     * Creates a new instance of itself and wraps it in a {@link ModuleWrapper} so that only an
     * {@link IServiceLocator}
     *
     * @param serviceLocator The new service locator
     * @return A wrapper around a newly created instance of this class
     */
    public static ModuleWrapper<AndroidFileSystemAdapter> newInstance(final IServiceLocator serviceLocator) {
        return new ModuleWrapper<>(new AndroidFileSystemAdapter(serviceLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer getWriter(final String path) throws IOException {
        final OutputStream outputStream = new FileOutputStream(path);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamWriter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getReader(final String path) throws IOException {
        final InputStream inputStream = new FileInputStream(path);
        final InputStreamReader outputStreamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
        return new BufferedReader(outputStreamReader);
    }

    /**
     * Deserialize the instance using readObject to ensure invariants and security.
     *
     * @param stream The serialized object to be deserialized
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        ensureClassInvariant();
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
     *
     * @throws InvalidObjectException Thrown when the state of the class is unstbale
     */
    private void ensureClassInvariant() throws InvalidObjectException {
        // No checks to perform
    }
}
