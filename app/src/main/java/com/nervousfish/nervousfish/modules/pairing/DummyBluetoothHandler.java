package com.nervousfish.nervousfish.modules.pairing;

import com.nervousfish.nervousfish.events.SLReadyEvent;
import com.nervousfish.nervousfish.service_locator.IServiceLocator;
import com.nervousfish.nervousfish.service_locator.IServiceLocatorCreator;
import com.nervousfish.nervousfish.service_locator.ModuleWrapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * An handler doing nothing.
 */
public final class DummyBluetoothHandler extends APairingHandler implements IBluetoothHandler {
    final IServiceLocatorCreator serviceLocatorCreator;
    /**
     * Prevents construction from outside the class.
     *
     * @param serviceLocatorCreator The object responsible for creating the service locator
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private DummyBluetoothHandler(final IServiceLocatorCreator serviceLocatorCreator) {
        super();
        this.serviceLocatorCreator = serviceLocatorCreator;
        EventBus.getDefault().register(this);
    }

    /**
     * Creates a new instance of itself and wraps it in a {@link ModuleWrapper} so that only an {@link IServiceLocatorCreator}
     * can access the new module to create the new {@link IServiceLocator}.
     *
     * @param serviceLocatorCreator The service locator bridge that creates the new service locator
     * @return A wrapper around a newly created instance of this class
     */
    public static ModuleWrapper<DummyBluetoothHandler> newInstance(final IServiceLocatorCreator serviceLocatorCreator) {
        return new ModuleWrapper<>(new DummyBluetoothHandler(serviceLocatorCreator));
    }

    /**
     * {@inheritDoc}
     * @param event Indicates that the {@link SLReadyEvent} happened
     */
    @Subscribe
    @Override
    public void onSLReadyEvent(final SLReadyEvent event) {

    }
}
