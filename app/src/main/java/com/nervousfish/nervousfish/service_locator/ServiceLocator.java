package com.nervousfish.nervousfish.service_locator;

import com.nervousfish.nervousfish.annotations.DesignedForExtension;
import com.nervousfish.nervousfish.modules.constants.Constants;
import com.nervousfish.nervousfish.modules.constants.IConstants;
import com.nervousfish.nervousfish.modules.cryptography.EncryptorAdapter;
import com.nervousfish.nervousfish.modules.cryptography.IEncryptor;
import com.nervousfish.nervousfish.modules.cryptography.IKeyGenerator;
import com.nervousfish.nervousfish.modules.cryptography.KeyGeneratorAdapter;
import com.nervousfish.nervousfish.modules.database.GsonDatabaseAdapter;
import com.nervousfish.nervousfish.modules.database.IDatabase;
import com.nervousfish.nervousfish.modules.filesystem.AndroidFileSystemAdapter;
import com.nervousfish.nervousfish.modules.filesystem.IFileSystem;
import com.nervousfish.nervousfish.modules.pairing.AndroidBluetoothHandler;
import com.nervousfish.nervousfish.modules.pairing.IBluetoothHandler;
import com.nervousfish.nervousfish.modules.pairing.INfcHandler;
import com.nervousfish.nervousfish.modules.pairing.NFCHandler;

import org.apache.commons.lang3.Validate;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all modules and provides access to them. This class should be dependency injected into all
 * activities that can use it to get access to any service the service locator provides.
 */
// TooManyMethods Suppressed because we cannot reduce the number of services or refactor the servicelocator without reducing readability
// ConstructorCallsOverridableMethod suppressed because we actually do want the subclasses to change the behaviour of the constructor!
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ConstructorCallsOverridableMethod"})
public class ServiceLocator implements IServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServiceLocator");

    private final String androidFilesDir;
    private final IDatabase database;
    private final IKeyGenerator keyGenerator;
    private final IEncryptor encryptor;
    private final IFileSystem fileSystem;
    private final IConstants constants;
    private final IBluetoothHandler bluetoothHandler;
    private final INfcHandler nfcHandler;

    /**
     * Package-private constructor of the service locator
     *
     * @param androidFilesDir The directory of the Android-specific files
     */
    ServiceLocator(final String androidFilesDir) {
        this.androidFilesDir = androidFilesDir;
        this.constants = this.initConstants();
        this.fileSystem = this.initFileSystem();
        this.keyGenerator = this.initKeyGenerator();
        this.encryptor = this.initEncryptor();
        this.database = this.initDatabase();
        this.bluetoothHandler = this.initBluetoothHandler();
        this.nfcHandler = this.initNfcHandler();
    }

    /**
     * Private constructor for deserializing the service locator
     *
     * @param androidFilesDir The directory of the Android-specific files
     */
    // We suppress parameternumber and javadocmethod, because this method isn't meant to be used outside
    // this class and it's needed for the serialization proxy
    @SuppressWarnings({"checkstyle:parameternumber", "checkstyle:javadocmethod", "ConstructorWithTooManyParameters"})
    ServiceLocator(final String androidFilesDir,
                   final IDatabase database,
                   final IKeyGenerator keyGenerator,
                   final IEncryptor encryptor,
                   final IFileSystem fileSystem,
                   final IConstants constants,
                   final IBluetoothHandler bluetoothHandler,
                   final INfcHandler nfcHandler) {
        Validate.notBlank(androidFilesDir);
        Validate.notNull(database);
        Validate.notNull(keyGenerator);
        Validate.notNull(encryptor);
        Validate.notNull(fileSystem);
        Validate.notNull(constants);
        Validate.notNull(bluetoothHandler);
        Validate.notNull(nfcHandler);

        this.androidFilesDir = androidFilesDir;
        this.database = database;
        this.keyGenerator = keyGenerator;
        this.encryptor = encryptor;
        this.fileSystem = fileSystem;
        this.constants = constants;
        this.bluetoothHandler = bluetoothHandler;
        this.nfcHandler = nfcHandler;
    }

    /**
     * @return The constants module used for this servicelocator
     */
    @DesignedForExtension
    IConstants initConstants() {
        return Constants.newInstance(this).getModule();
    }

    /**
     * @return The filesystem module used for this servicelocator
     */
    @DesignedForExtension
    IFileSystem initFileSystem() {
        return AndroidFileSystemAdapter.newInstance(this).getModule();
    }

    /**
     * @return The database module used for this servicelocator
     */
    @DesignedForExtension
    IDatabase initDatabase() {
        return GsonDatabaseAdapter.newInstance(this).getModule();
    }

    /**
     * @return The keygenerator module used for this servicelocator
     */
    @DesignedForExtension
    IKeyGenerator initKeyGenerator() {
        return KeyGeneratorAdapter.newInstance(this).getModule();
    }

    /**
     * @return The encryptor module used for this servicelocator
     */
    @DesignedForExtension
    IEncryptor initEncryptor() {
        return EncryptorAdapter.newInstance(this).getModule();
    }

    /**
     * @return The Bluetooth module used for this servicelocator
     */
    @DesignedForExtension
    IBluetoothHandler initBluetoothHandler() {
        return AndroidBluetoothHandler.newInstance(this).getModule();
    }

    /**
     * @return The NFC module used for this servicelocator
     */
    @DesignedForExtension
    INfcHandler initNfcHandler() {
        return NFCHandler.newInstance(this).getModule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getAndroidFilesDir() {
        ServiceLocator.assertExists(this.androidFilesDir, "androidFilesDir");
        return this.androidFilesDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IDatabase getDatabase() {
        ServiceLocator.assertExists(this.database, "database");
        return this.database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IKeyGenerator getKeyGenerator() {
        ServiceLocator.assertExists(this.keyGenerator, "keyGenerator");
        return this.keyGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IEncryptor getEncryptor() {
        ServiceLocator.assertExists(this.encryptor, "encryptor");
        return this.encryptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IFileSystem getFileSystem() {
        ServiceLocator.assertExists(this.fileSystem, "fileSystem");
        return this.fileSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IConstants getConstants() {
        ServiceLocator.assertExists(this.constants, "constants");
        return this.constants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IBluetoothHandler getBluetoothHandler() {
        ServiceLocator.assertExists(this.bluetoothHandler, "bluetoothHandler");
        return this.bluetoothHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final INfcHandler getNFCHandler() {
        ServiceLocator.assertExists(this.nfcHandler, "nfcHandler");
        return this.nfcHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerToEventBus(final Object object) {
        Validate.notNull(object);
        EventBus.getDefault().register(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterFromEventBus(final Object object) {
        Validate.notNull(object);
        EventBus.getDefault().unregister(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DesignedForExtension
    public void postOnEventBus(final Object message) {
        Validate.notNull(message);
        EventBus.getDefault().post(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DesignedForExtension
    public boolean isRegisteredToEventBus(final Object object) {
        Validate.notNull(object);
        return EventBus.getDefault().isRegistered(object);
    }

    /**
     * Checks if the module is initialized and throws an error otherwise.
     *
     * @param object The object to check
     * @param name   The name of the object
     */
    private static void assertExists(final Object object, final String name) {
        if (object == null) {
            LOGGER.error("The module \"{}\" is used before it is defined", name);
            throw new ServiceLocator.ModuleNotFoundException("The module \"" + name + "\" is used before it is defined");
        }
    }

    /**
     * Thrown when a module was called before it was initialized.
     */
    static final class ModuleNotFoundException extends RuntimeException {

        private static final long serialVersionUID = -2889621076876351934L;

        /**
         * Constructs a new exception to make clear that a module was requested before it was initialized.
         *
         * @param message A message describing in more detail what happened
         */
        ModuleNotFoundException(final String message) {
            super(message);
        }

    }

}
