package com.nervousfish.nervousfish.activities;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.nervousfish.nervousfish.R;
import com.nervousfish.nervousfish.data_objects.Contact;
import com.nervousfish.nervousfish.list_adapters.ContactsByKeyTypeListAdapter;
import com.nervousfish.nervousfish.list_adapters.ContactsByNameListAdapter;

import java.util.Collections;
import java.util.Comparator;

/**
 * Is used to sort the list in the MainActivity
 */
final class MainActivitySorter {

    private static final int SORT_BY_NAME = 0;
    private static final int SORT_BY_KEY_TYPE = 1;
    private static final int NUMBER_OF_SORTING_MODES = 2;
    private static final Comparator<Contact> NAME_SORTER = (o1, o2) -> o1.getName().compareTo(o2.getName());

    private final MainActivity mainActivity;
    private int currentSorting;

    /**
     * Create and initialize the class.
     *
     * @param mainActivity The MainActivity where sorting is needed
     */
    MainActivitySorter(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Switches the sorting mode.
     *
     * @param view The sort floating action button that was clicked
     */
    void onSortButtonClicked(final View view) {
        this.currentSorting++;
        if (this.currentSorting >= NUMBER_OF_SORTING_MODES) {
            this.currentSorting = 0;
        }
        final ViewFlipper flipper = (ViewFlipper) this.mainActivity.findViewById(R.id.view_flipper_sorter_main);
        flipper.showNext();
        switch (this.currentSorting) {
            case SORT_BY_NAME:
                this.sortOnName();
                break;
            case SORT_BY_KEY_TYPE:
                this.sortOnKeyType();
                break;
            default:
                break;
        }
    }


    /**
     * Sorts contacts by name
     */
    void sortOnName() {
        final ListView lv = (ListView) this.mainActivity.findViewById(R.id.list_view_main);
        final ContactsByNameListAdapter contactsByNameListAdapter =
                new ContactsByNameListAdapter(this.mainActivity, this.mainActivity.getContacts());
        contactsByNameListAdapter.sort(NAME_SORTER);
        lv.setAdapter(contactsByNameListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                MainActivitySorter.this.mainActivity.openContact(position);
            }
        });

        Collections.sort(this.mainActivity.getContacts(), NAME_SORTER);

        if (contactsByNameListAdapter.isEmpty()) {
            this.showNoContactsContent();
        } else {
            this.hideNoContactsContent();
        }
    }

    /**
     * Shows the message that the user has no contacts yet.
     */
    private void showNoContactsContent() {
        this.mainActivity.findViewById(R.id.view_flipper_sorter_main).setVisibility(View.GONE);
        this.mainActivity.findViewById(R.id.no_users_screen).setVisibility(View.VISIBLE);
    }

    /**
     * Hides the message that the user has no contacts yet.
     */
    private void hideNoContactsContent() {
        this.mainActivity.findViewById(R.id.view_flipper_sorter_main).setVisibility(View.VISIBLE);
        this.mainActivity.findViewById(R.id.no_users_screen).setVisibility(View.GONE);
    }

    /**
     * Sorts contacts by key type
     */
    private void sortOnKeyType() {
        final ExpandableListView expandabelView = (ExpandableListView) this.mainActivity.findViewById(R.id.expandable_contact_list_by_key_type);
        final ContactsByKeyTypeListAdapter contactsByKeyTypeListAdapter =
                new ContactsByKeyTypeListAdapter(this.mainActivity, this.mainActivity.getContacts());
        expandabelView.setAdapter(contactsByKeyTypeListAdapter);
        expandabelView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            this.mainActivity.openContact(childPosition);
            return false;
        });

        if (contactsByKeyTypeListAdapter.isEmpty()) {
            this.showNoContactsContent();
        } else {
            this.hideNoContactsContent();
        }
    }

}
