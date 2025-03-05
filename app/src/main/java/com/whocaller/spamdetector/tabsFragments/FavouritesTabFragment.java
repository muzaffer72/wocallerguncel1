/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.tabsFragments;

import static com.whocaller.spamdetector.utils.Permission.executorService;
import static com.whocaller.spamdetector.utils.Permission.mainHandler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.whocaller.spamdetector.adapter.ContactAdapter;
import com.whocaller.spamdetector.databinding.FragmentFavouritesTabBinding;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FavouritesTabFragment extends Fragment {

    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();
    private FragmentFavouritesTabBinding binding;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavouritesTabBinding.inflate(inflater, container, false);


        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
        binding.recyclerview.setLayoutManager(layoutManager);


        adapter = new ContactAdapter(contactList, getContext(), true, false);
        binding.recyclerview.setAdapter(adapter);

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadContacts();
            } else {
                binding.noContactsFound.setVisibility(View.VISIBLE);
                binding.recyclerview.setVisibility(View.GONE);
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

    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadContacts() {
        contactList.clear();
        executorService.execute(() -> {
            contactList.addAll(Utils.loadFavoriteContacts(requireContext()));
            mainHandler.post(() -> {
                adapter.notifyDataSetChanged();
                if (contactList.isEmpty()) {
                    binding.noContactsFound.setVisibility(View.VISIBLE);
                    binding.recyclerview.setVisibility(View.GONE);
                } else {
                    binding.noContactsFound.setVisibility(View.GONE);
                    binding.recyclerview.setVisibility(View.VISIBLE);
                }
            });
        });

    }
}


