/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.tabsFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.whocaller.spamdetector.adapter.OnlineContactAdapter;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.databinding.FragmentBlockTabBinding;
import com.whocaller.spamdetector.modal.Contact;

import java.util.List;

public class BlockTabFragment extends Fragment {

    FragmentBlockTabBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBlockTabBinding.inflate(inflater, container, false);

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadContacts();

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadContacts() {
        BlockCallerDbHelper blockCallerDbHelper = new BlockCallerDbHelper(requireContext());
        List<Contact> contactList = blockCallerDbHelper.getAllBlockCallers();

        OnlineContactAdapter contactAdapter = new OnlineContactAdapter(contactList, requireContext());
        binding.recyclerview.setAdapter(contactAdapter);
        contactAdapter.notifyDataSetChanged();

        if (contactList.isEmpty()) {
            binding.demoItem.setVisibility(View.VISIBLE);
            binding.recyclerview.setVisibility(View.GONE);
        } else {
            binding.demoItem.setVisibility(View.GONE);
            binding.recyclerview.setVisibility(View.VISIBLE);
        }
    }
}
