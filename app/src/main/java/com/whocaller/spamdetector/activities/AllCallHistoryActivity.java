/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.adapter.CallLogAdapter;
import com.whocaller.spamdetector.databinding.ActivityAllCallHistoryBinding;
import com.whocaller.spamdetector.helpers.ContactsHelper;
import com.whocaller.spamdetector.modal.CallLogItem;

import java.util.ArrayList;
import java.util.List;

public class AllCallHistoryActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_CALL_LOG = 1;

    private ActivityAllCallHistoryBinding binding;
    private List<CallLogItem> allCallLogs = new ArrayList<>();
    private CallLogAdapter adapter;

    String callText = "Call History";
    OnBackPressedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllCallHistoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        binding.more.setOnClickListener(this::showPopupMenu);
        binding.closeBtn.setOnClickListener(v -> callback.handleOnBackPressed());
        binding.showAllCalls.setOnClickListener(v ->callback.handleOnBackPressed());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    REQUEST_CODE_READ_CALL_LOG);
        } else {
            loadCallLogs();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_CALL_LOG && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadCallLogs();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCallLogs() {

        new ContactsHelper.LoadCallLogsTask(this , binding.progressBar, (callLogItems, incomingCallsCount, outgoingCallsCount) -> {
            allCallLogs = callLogItems;
            adapter = new CallLogAdapter(allCallLogs, AllCallHistoryActivity.this, false, true);
            adapter.setShowHeader(true);
            binding.recyclerview.setAdapter(adapter);
            binding.recyclerview.setLayoutManager(new LinearLayoutManager(AllCallHistoryActivity.this));
            binding.recyclerview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            loadCallUi(allCallLogs);
        }).execute();
    }

    @SuppressLint("SetTextI18n")
    private void loadCallUi(List callLogItems) {
        binding.name.setText(callText);
        if (callLogItems.isEmpty()) {
            binding.txtMessage.setText("No" + " " + callText);
            binding.noCallsLay.setVisibility(View.VISIBLE);
            binding.recyclerview.setVisibility(View.GONE);
        } else {
            binding.noCallsLay.setVisibility(View.GONE);
            binding.recyclerview.setVisibility(View.VISIBLE);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.call_history, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.outgoing_call) {
                callText = getString(R.string.outgoing_calls);
                filterCallLogs(CallLog.Calls.OUTGOING_TYPE);
                return true;
            } else if (id == R.id.incoming_call) {
                callText = getString(R.string.incoming_calls);
                filterCallLogs(CallLog.Calls.INCOMING_TYPE);
                return true;
            } else if (id == R.id.missed_call) {
                callText = getString(R.string.missed_calls);
                filterCallLogs(CallLog.Calls.MISSED_TYPE);
                return true;
            } else if (id == R.id.blocked_call) {
                callText = getString(R.string.blocked_calls);
                filterCallLogs(CallLog.Calls.BLOCKED_TYPE);
                return true;
            } else if (id == R.id.rejected_call) {
                callText = getString(R.string.rejected_calls);
                filterCallLogs(CallLog.Calls.REJECTED_TYPE);
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCallLogs(int callType) {
        List<CallLogItem> filteredCallLogs = new ArrayList<>();
        for (CallLogItem item : allCallLogs) {
            if (item.getCallType() == callType) {
                filteredCallLogs.add(item);
            }
        }

        loadCallUi(filteredCallLogs);
        adapter.updateCallLogs(filteredCallLogs);
        adapter.notifyDataSetChanged();
    }
}
