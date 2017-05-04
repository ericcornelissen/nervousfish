package com.nervousfish.nervousfish.events;

/**
 * Greenrobot's EventBus message event
 * <p>
 * Sent when the {@link com.nervousfish.nervousfish.service_locator.IServiceLocator} inside the
 * {@link com.nervousfish.nervousfish.service_locator.IServiceLocatorCreator} is ready to be used.
 */
public class SLReadyEvent {

    /**
     * Constructs a new SLReadyEvent
     */
    public SLReadyEvent() {
        // No implementation needed
    }
}
