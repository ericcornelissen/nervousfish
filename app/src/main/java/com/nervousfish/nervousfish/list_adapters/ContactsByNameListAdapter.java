package com.nervousfish.nervousfish.list_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Contact;

import java.util.List;

/**
 * An Adapter which converts a list with contacts into List entries.
 *
 */
public final class ContactsByNameListAdapter extends ArrayAdapter<Contact> {

    /**
     * Create and initialize a {@link ContactsByNameListAdapter}.
     *
     * @param context  the Context where the ListView is created
     * @param contacts the list with contacts
     */
    public ContactsByNameListAdapter(final Context context, final List<Contact> contacts) {
        super(context, 0, contacts);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        final View v;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(this.getContext());
            v = inflater.inflate(R.layout.contact_list_entry, null);
        } else {
            v = convertView;
        }

        final Contact contact = this.getItem(position);

        if (contact != null) {
            final TextView name = (TextView) v.findViewById(R.id.contact_name_list_entry);

            if (name != null) {
                name.setText(contact.getName());
            }
        }

        return v;
    }

}
