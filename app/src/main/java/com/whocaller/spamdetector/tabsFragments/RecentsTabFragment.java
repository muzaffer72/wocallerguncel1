/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.tabsFragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ROLE_SERVICE;
import static android.content.Context.TELECOM_SERVICE;
import static com.whocaller.spamdetector.utils.Permission.executorService;
import static com.whocaller.spamdetector.utils.Permission.mainHandler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.whocaller.spamdetector.activities.MainActivity;
import com.whocaller.spamdetector.adapter.CallLogAdapter;
import com.whocaller.spamdetector.databinding.FragmentRecentsTabBinding;
import com.whocaller.spamdetector.helpers.ContactsHelper;

public class RecentsTabFragment extends Fragment implements CallLogAdapter.SelectionListener, CallLogAdapter.SetDefault {

    @SuppressLint("StaticFieldLeak")
    private FragmentRecentsTabBinding binding;
    @SuppressLint("StaticFieldLeak")
    public static CallLogAdapter adapter;

    private ActivityResultLauncher<Intent> dialerRoleLauncher;
    private boolean isDefaultDialer;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean readCallLogGranted = permissions.getOrDefault(Manifest.permission.READ_CALL_LOG, false);
                Boolean readContactsGranted = permissions.getOrDefault(Manifest.permission.READ_CONTACTS, false);
                Boolean writeContactsGranted = permissions.getOrDefault(Manifest.permission.WRITE_CONTACTS, false);

                if (Boolean.TRUE.equals(readCallLogGranted) && Boolean.TRUE.equals(readContactsGranted) && Boolean.TRUE.equals(writeContactsGranted)) {
                    loadCallLogs();
                } else {
                    binding.noContactsFound.setVisibility(View.VISIBLE);
                    binding.recyclerview.setVisibility(View.GONE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecentsTabBinding.inflate(inflater, container, false);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));


        dialerRoleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(getContext(), "Default dialer role granted", Toast.LENGTH_SHORT).show();
                        isDefaultDialer = true;
                        loadCallLogs();
                    } else {
                        isDefaultDialer = false;
                        Toast.makeText(getContext(), "Default dialer role not granted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
                    }
                }
        );


        requestDefaultDialerRole(false);


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            });
        } else {
            loadCallLogs();
        }

        return binding.getRoot();
    }

    private void requestDefaultDialerRole(boolean clicked) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) requireContext().getSystemService(ROLE_SERVICE);
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                if (clicked) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    dialerRoleLauncher.launch(intent);
                    isDefaultDialer = false;
                }
            } else {
                isDefaultDialer = true;
                loadCallLogs();
            }
        } else {
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TELECOM_SERVICE);
            if (telecomManager != null && !telecomManager.getDefaultDialerPackage().equals(getContext().getPackageName())) {
                if (clicked) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getContext().getPackageName());
                    dialerRoleLauncher.launch(intent);
                    isDefaultDialer = false;
                }
            } else {
                isDefaultDialer = true;
                loadCallLogs();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCallLogs() {
        Context context = requireContext();
        executorService.execute(() -> {
            mainHandler.post(() -> {
                new ContactsHelper.LoadCallLogsTask(getContext(), binding.progressBar, (callLogItems, incomingCallsCount, outgoingCallsCount) -> {
                    ProfilePrefHelper profilePrefHelper = new ProfilePrefHelper(context);
                    profilePrefHelper.saveCallsCount(incomingCallsCount, outgoingCallsCount);
                    adapter = new CallLogAdapter(callLogItems, getContext(), false, true);
                    adapter.setSelectionListener(RecentsTabFragment.this);
                    adapter.setSetDefault(RecentsTabFragment.this);
                    adapter.setShowHeader(isDefaultDialer);
                    binding.recyclerview.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    if (callLogItems.isEmpty()) {
                        binding.noContactsFound.setVisibility(View.VISIBLE);
                        binding.recyclerview.setVisibility(View.GONE);
                    } else {
                        binding.noContactsFound.setVisibility(View.GONE);
                        binding.recyclerview.setVisibility(View.VISIBLE);
                    }
                }).execute();
            });
        });


    }

    @Override
    public void onSelectionModeChange(boolean enabled) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).onSelectionModeChange(enabled);
        }
    }

    @Override
    public void onItemSelectionChange(int count) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).onItemSelectionChange(count);
        }
    }

    @Override
    public void onItemDeleteChange(boolean deleted) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).onItemDeleteChange(deleted);
        }
    }

    @Override
    public void onItemClick(boolean click) {
        requestDefaultDialerRole(true);
    }
}
