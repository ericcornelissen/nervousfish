package com.nervousfish.nervousfish.modules.cryptography;

import com.nervousfish.nervousfish.exceptions.EncryptionException;
import com.nervousfish.nervousfish.modules.IModule;

import java.security.GeneralSecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

/**
 * Defines a module used for encrypting / decrypting messages that can be used by a service locator.
 */
public interface IEncryptor extends IModule {

    /**
     * Hashes a given pass to an encrypted string that can't be decrypted.
     *
     * @param string The string to encrypt
     * @return       The encrypted string.
     */
    String hashString(String string) throws EncryptionException;

    /**
     * Make a key for encryption based on a password.
     *
     * @param password The password to make the key with
     * @return         The SecretKey based on the password.
     */
    SecretKey makeKeyFromPassword(String password);

    /**
     * Encrypts a string with a secret key in byte array from a password
     *
     * @param toEncrypt The string to be encrypted
     * @param key       The secret key made from the password.
     * @return          The encrypted string.
     */
    String encryptWithPassword(String toEncrypt, SecretKey key)
            throws IllegalBlockSizeException, BadPaddingException, EncryptionException;

    /**
     * Decrypts a string with a secret key in byte array from a password
     *
     * @param toDecrypt The byte array to be decrypted
     * @param key       The secret key made from the password.
     * @return          The decrypted byte array.
     */
    byte[] decryptWithPassword(byte[] toDecrypt, long key) throws GeneralSecurityException;

    /**
     * Decrypts a string with a secretkey in bytearray from a password
     *
     * @param toDecrypt The string to be decrypted
     * @param key       The secret key made from the password.
     * @return          The decrypted byte array.
     */
    String decryptWithPassword(String toDecrypt, SecretKey key) throws EncryptionException, IllegalBlockSizeException, BadPaddingException;


}
