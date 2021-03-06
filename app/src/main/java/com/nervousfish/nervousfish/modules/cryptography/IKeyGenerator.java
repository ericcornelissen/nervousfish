package com.nervousfish.nervousfish.modules.cryptography;

import com.nervousfish.nervousfish.data_objects.KeyPair;
import com.nervousfish.nervousfish.modules.IModule;

/**
 * Defines a module for generating secure keys that can be used by a service locator.
 */

public interface IKeyGenerator extends IModule {

    /**
     * Generates a random {@link KeyPair} with the RSA algorithm.
     *
     * @param name The name of the newly generated key
     * @return a randomly generated KeyPair
     */
    KeyPair generateRSAKeyPair(String name) throws KeyGenerationException;

    /**
     * Generates a random {@link KeyPair} with the Ed25519 algorithm. For more
     * details about Ed25519: https://ed25519.cr.yp.to/
     *
     * @param name The name of the newly generated key
     * @return a randomly generated KeyPair
     */
    KeyPair generateEd25519KeyPair(String name) throws KeyGenerationException;

}
