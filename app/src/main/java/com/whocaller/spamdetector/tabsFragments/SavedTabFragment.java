/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.tabsFragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.adapter.ContactAdapter;
import com.whocaller.spamdetector.databinding.FragmentSavedBinding;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SavedTabFragment extends Fragment {

    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> filteredContactList = new ArrayList<>();
    public static String phonenumber = null;
    private FragmentSavedBinding binding;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactAdapter(filteredContactList, getContext(), false, true);
        binding.recyclerView.setAdapter(contactAdapter);


        ProfilePrefHelper sharedPrefHelper = new ProfilePrefHelper(requireContext());
        phonenumber = sharedPrefHelper.getPhone();


        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadContacts();
            } else {
                Log.d("SavedTabFragment", "Permission denied");
            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        } else {
            loadContacts();
        }

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadContacts() {
        new Utils.ContactLoaderTask(requireContext(), binding.progressBar, contacts -> {
            Log.d("SavedTabFragment", "onContactsLoaded");
            contactList.clear();
            contactList.addAll(contacts);
            filteredContactList.clear();
            filteredContactList.addAll(contactList);
            contactAdapter.notifyDataSetChanged();

            if (contactList.isEmpty()) {
                binding.noContactsFound.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            } else {
                binding.noContactsFound.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }

            if (phonenumber != null) {
                if (Config.CONTACTS_SYNC) {
                    Utils.postContacts(contactList, phonenumber);
                }

            }
        }).execute();
    }
}
