package com.whocaller.spamdetector.activities;

import static com.whocaller.spamdetector.utils.Utils.generateAvatar;
import static com.whocaller.spamdetector.utils.Utils.getContactImage;
import static com.whocaller.spamdetector.utils.Utils.isValidName;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.whocaller.spamdetector.adapter.CallLogAdapter;
import com.whocaller.spamdetector.databinding.ActivityCallHistoryBinding;
import com.whocaller.spamdetector.helpers.CallLogHelper;
import com.whocaller.spamdetector.modal.CallLogItem;

import java.util.List;
import java.util.Objects;

public class CallHistoryActivity extends AppCompatActivity {

    private ActivityCallHistoryBinding binding;

    // LiveData değişkenleri
    private MutableLiveData<String> phoneNumberLiveData = new MutableLiveData<>();
    private MutableLiveData<String> contactNameLiveData = new MutableLiveData<>();
    private MutableLiveData<List<CallLogItem>> callLogListLiveData = new MutableLiveData<>();
    private MutableLiveData<Bitmap> profilePicLiveData = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallHistoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Intent'ten verileri çek ve LiveData'ya ata
        Intent intent = getIntent();
        if (intent != null) {
            phoneNumberLiveData.setValue(intent.getStringExtra("phoneNumber"));
            contactNameLiveData.setValue(intent.getStringExtra("name"));
        }

        // **LiveData Observer'ları**

        // **İsim ve Numara LiveData Güncellemesi**
        contactNameLiveData.observe(this, contactName -> {
            if (contactName != null) {
                binding.name.setText(contactName);
                updateProfileImage(contactName);
            }
        });

        phoneNumberLiveData.observe(this, phoneNumber -> {
            if (phoneNumber != null) {
                binding.number.setText(phoneNumber);
                loadCallLogs(phoneNumber);
            }
        });

        // **Profil Resmi LiveData Güncellemesi**
        profilePicLiveData.observe(this, bitmap -> {
            if (bitmap != null) {
                binding.profilePic.setImageBitmap(bitmap);
            }
        });

        // **Çağrı Geçmişi Listesi LiveData Güncellemesi**
        callLogListLiveData.observe(this, callLogItems -> {
            if (callLogItems != null) {
                CallLogAdapter callLogAdapter = new CallLogAdapter(callLogItems, this, false, false);
                callLogAdapter.setShowHeader(true);
                binding.recyclerview.setAdapter(callLogAdapter);
            }
        });

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // Geri tuşu yönetimi
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        binding.backBtn.setOnClickListener(v -> callback.handleOnBackPressed());
    }

    // **Profil resmini güncelle**
    private void updateProfileImage(String contactName) {
        String phoneNumber = phoneNumberLiveData.getValue();
        if (phoneNumber == null) return;

        Bitmap contactImage = getContactImage(this, phoneNumber);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            profilePicLiveData.setValue(Objects.requireNonNullElseGet(contactImage, () -> generateAvatar(contactName)));
        } else {
            profilePicLiveData.setValue(generateAvatar(contactName));
        }
    }

    // **Çağrı geçmişini yükle**
    private void loadCallLogs(String phoneNumber) {
        List<CallLogItem> callLogItems = CallLogHelper.getCallLogsForNumber(this, phoneNumber);
        callLogListLiveData.setValue(callLogItems);
    }
}
