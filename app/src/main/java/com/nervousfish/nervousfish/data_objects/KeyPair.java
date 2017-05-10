package com.nervousfish.nervousfish.data_objects;

/**
 * KeyPair POJO which keeps a public and private key.
 */
public final class KeyPair {

    private final IKey publicKey;
    private final IKey privateKey;

    /**
     * The constructor for the {@link KeyPair}.
     *
     * @param publicKey  The publicKey of the KeyPair.
     * @param privateKey The privateKey of the KeyPair.
     */
    public KeyPair(final IKey publicKey, final IKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Returns the public key of the {@link KeyPair}.
     *
     * @return the public {@link IKey}.
     */
    public IKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Returns the private key of the {@link KeyPair}.
     *
     * @return the private {@link IKey}.
     */
    public IKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final KeyPair that = (KeyPair) o;
        return this.publicKey.equals(that.publicKey)
                && this.privateKey.equals(that.privateKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.publicKey.hashCode() + this.privateKey.hashCode();
    }

}
