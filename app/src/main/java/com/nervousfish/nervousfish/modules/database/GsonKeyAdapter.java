package com.nervousfish.nervousfish.modules.database;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.nervousfish.nervousfish.ConstantKeywords;
import com.nervousfish.nervousfish.data_objects.IKey;
import com.nervousfish.nervousfish.data_objects.RSAKey;
import com.nervousfish.nervousfish.data_objects.SimpleKey;

import java.io.IOException;

/**
 * Adaptor for the {@link IKey} interface for the GSON library.
 */
final class GsonKeyAdapter extends TypeAdapter<IKey> {

    /**
     * Constructor for the {@code GsonKeyAdapter} class.
     */
    GsonKeyAdapter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final JsonWriter writer, final IKey key) throws IOException {
        writer.beginArray();

        // First write the key type
        final String keyType = key.getType();
        writer.value(keyType);

        // Then write the rest of the key
        writer.beginObject();
        key.toJSON(writer);
        writer.endObject();

        writer.endArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidFinalLocalVariable") // final IKey key is actually useful here
    public IKey read(final JsonReader reader) throws IOException {
        reader.beginArray();
        final String type = reader.nextString();
        reader.beginObject();

        final IKey key;
        switch (type) {
            case ConstantKeywords.RSA_KEY:
                key = RSAKey.fromJSON(reader);
                break;
            case "simple":
                key = SimpleKey.fromJSON(reader);
                break;
            default:
                throw new IOException("Could not read key");
        }

        reader.endObject();
        reader.endArray();
        return key;
    }

}
