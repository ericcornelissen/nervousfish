package com.nervousfish.nervousfish.activities;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.data_objects.IKey;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The ListviewActivityHelper has some static methods which are
 * used in the ContactActivity and the ChangeContactActivity.
 */

final class ListviewActivityHelper {

    /**
     * Methods are static so this is not called.
     */
    private ListviewActivityHelper() {
        // Prevent instantiation
    }

    /**
     * Set the name of the {@link Contact} to the {@link ContactActivity}.
     *
     * @param activity The {@link Activity} where the name has to be set
     * @param name The name, cannot be blank
     * @param id the {@code R.id} of the target {@link TextView}.
     */
    static void setName(final Activity activity, final String name, final int id) {
        Validate.notNull(activity);
        Validate.notBlank(name);
        Validate.isTrue(id > 0);
        final TextView tv = (TextView) activity.findViewById(id);
        tv.setText(name);
    }

    /**
     * Set the keys of the {@link Contact} to the {@link ContactActivity} or {@link KeyManagementActivity}.
     *
     * @param activity The where the name has to be set.
     * @param keys A {@link Collection} of {@link IKey}s.
     * @param id the {@code R.id} of the target {@link ListView}.
     */
    static void setKeys(final Activity activity, final Collection<IKey> keys, final int id) {
        Validate.notNull(activity);
        Validate.notNull(keys);
        Validate.isTrue(id > 0);
        final List<String> keyNames = new ArrayList<>();
        for (final IKey key : keys) {
            keyNames.add(key.getName());
        }

        final ListView lv = (ListView) activity.findViewById(id);
        lv.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, keyNames));
    }

}
