/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.tabsFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.whocaller.spamdetector.adapter.OnlineContactAdapter;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.databinding.FragmentIdentifiedBinding;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.Utils;

import java.util.List;

public class IdentifiedTabFragment extends Fragment implements OnlineContactAdapter.SelectionListener {


    FragmentIdentifiedBinding binding;

    ContactsDataDb contactsDataDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIdentifiedBinding.inflate(inflater, container, false);


        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));


        loadContacts();
        contactsDataDb = new ContactsDataDb(requireContext());


        return binding.getRoot();
    }


    @Override
    public void onResume() {
        loadContacts();
        super.onResume();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadContacts() {
        ContactsDataDb databaseHelper = new ContactsDataDb(requireContext());
        List<Contact> contactList = databaseHelper.getAllContacts();

        OnlineContactAdapter contactAdapter = new OnlineContactAdapter(contactList, requireContext());
        contactAdapter.setSelectionListener(IdentifiedTabFragment.this);
        binding.recyclerview.setAdapter(contactAdapter);
        contactAdapter.notifyDataSetChanged();

        if (contactList.isEmpty()) {
            binding.noContactsFound.setVisibility(View.VISIBLE);
            binding.recyclerview.setVisibility(View.GONE);
        } else {
            binding.noContactsFound.setVisibility(View.GONE);
            binding.recyclerview.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onSelectionModeChange(String number) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Contact")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Are you sure you want to Delete this contact?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (contactsDataDb.deleteContactByPhoneNumber(number)) {
                        loadContacts();
                        Utils.showToast(requireContext(), "Sucseed to delete contact.");
                    } else {
                        Utils.showToast(requireContext(), "Can't delete this contact.");
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
