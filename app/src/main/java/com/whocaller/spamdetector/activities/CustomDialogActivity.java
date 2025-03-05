package com.whocaller.spamdetector.activities;

import static com.whocaller.spamdetector.utils.Utils.generateAvatar;
import static com.whocaller.spamdetector.utils.Utils.getLookupKeyFromPhoneNumber;
import static com.whocaller.spamdetector.utils.Utils.isPhoneNumberSaved;
import static com.whocaller.spamdetector.utils.Utils.isValidName;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.ads.Ads;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.databinding.ActivityCustomDialogBinding;
import com.whocaller.spamdetector.helpers.NotificationHelper;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.Utils;

import java.util.List;

public class CustomDialogActivity extends AppCompatActivity {
    private static final String TAG = "CustomDialogActivity";

    Contact contactModal;
    Intent intent;
    boolean isBlock = false;
    BlockCallerDbHelper blockCallerDbHelper;
    ActivityCustomDialogBinding binding;

    private MutableLiveData<Contact> contactLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> blockStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> profileImageUrlLiveData = new MutableLiveData<>();

    private boolean isMissedCall = false;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Dialog tarzı ayarları
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // View binding
            binding = ActivityCustomDialogBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            Log.d(TAG, "CustomDialogActivity onCreate started");

            // CustomDialog'un dokunulduğunda kapanmamasını sağla
            setFinishOnTouchOutside(false);

            // Window ayarları
            Window window = getWindow();
            if (window != null) {
                // Bayraklar ekle - kilit ekranı üzerinde göster
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Pozisyonu ayarla
                SettingsPrefHelper settingsPrefHelper = new SettingsPrefHelper(this);
                WindowManager.LayoutParams params = window.getAttributes();

                switch (settingsPrefHelper.getCallerIdPosition()) {
                    case "Top":
                        params.gravity = Gravity.TOP;
                        break;
                    case "Center":
                        params.gravity = Gravity.CENTER;
                        break;
                    case "Bottom":
                        params.gravity = Gravity.BOTTOM;
                        break;
                    default:
                        params.gravity = Gravity.CENTER;
                        break;
                }

                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.y = 0;
                window.setAttributes(params);
            }

            // Gerekli bileşenleri başlat
            blockCallerDbHelper = new BlockCallerDbHelper(this);

            // Reklamları yükle
            if (Utils.isNetworkAvailable(this)) {
                Ads ads = new Ads(this);
                ads.loadingNativeAd(this);
            }

            // LiveData observer'ları kur
            setupLiveDataObservers();

            // Kapat butonunu ayarla
            binding.closeIcon.setOnClickListener(v -> {
                // Cevapsız çağrı bildirimi temizle (isteğe bağlı)
                if (isMissedCall) {
                    NotificationHelper.clearMissedCallNotification(this);
                }
                finish();
            });

            // Intent verilerini işle
            processIntent();

            isInitialized = true;

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Bir hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Gecikmeli kapanma
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
        }
    }

    private void setupLiveDataObservers() {
        // Contact verilerini izle
        contactLiveData.observe(this, contact -> {
            if (contact == null) return;

            // Kişi adı ve numarasını ayarla
            binding.callerName.setText(Utils.toTextCase(contact.getName()));
            binding.callerNumber.setText(contact.getPhoneNumber());

            // Profil resmini ayarla
            if (contact.isSpam()) {
                binding.profilePic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.spam_circle));
                profileImageUrlLiveData.setValue(null);
            } else {
                if (contact.getImageUrl() != null && !contact.getImageUrl().isEmpty()) {
                    profileImageUrlLiveData.setValue(contact.getImageUrl());
                } else {
                    profileImageUrlLiveData.setValue("");
                }
            }
        });

        // Engelleme durumunu izle
        blockStatusLiveData.observe(this, blocked -> {
            isBlock = (blocked != null && blocked);
            if (isBlock) {
                binding.blockTxt.setText(getString(R.string.unblock));
                binding.profilePic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.block_circle));
                profileImageUrlLiveData.setValue(null);
            } else {
                binding.blockTxt.setText(getString(R.string.block));
            }
        });

        // Profil resmini izle
        profileImageUrlLiveData.observe(this, url -> {
            if (url == null) return;
            if (url.isEmpty()) {
                Contact c = contactLiveData.getValue();
                if (c != null && isValidName(c.getName())) {
                    binding.profilePic.setImageBitmap(generateAvatar(c.getName()));
                } else {
                    binding.profilePic.setImageBitmap(generateAvatar("U"));
                }
            } else {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .into(binding.profilePic);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        Log.d(TAG, "onNewIntent called");
        setIntent(newIntent);
        processIntent();
    }

    private void processIntent() {
        try {
            intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "Intent is null, finishing activity");
                finish();
                return;
            }

            Log.d(TAG, "Processing intent: " + intent.toString());
            // Intent içeriğini logla
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.d(TAG, "Extra key: " + key + ", value: " + extras.get(key));
                }
            }

            // Cevapsız çağrı kontrolü
            isMissedCall = intent.getBooleanExtra("isMissedCall", false);
            if (isMissedCall) {
                Log.d(TAG, "This is a missed call dialog");
                // Cevapsız çağrı için UI düzenlemeleri yapabilirsiniz
            }

            // 'modal' ekstrası var mı kontrol et
            if (intent.hasExtra("modal") && intent.getParcelableExtra("modal") != null) {
                // Modal aracılığıyla veriler al
                final Contact contactModal = intent.getParcelableExtra("modal");
                contactLiveData.setValue(contactModal);

                final String phoneNumber = contactModal.getPhoneNumber();
                final String name = contactModal.getName();

                Log.d(TAG, "Processing intent with modal. Name: " + name + ", number: " + phoneNumber);

                isBlock = blockCallerDbHelper.isPhoneNumberBlocked(phoneNumber);
                blockStatusLiveData.setValue(isBlock);

                dialogAction(phoneNumber, name);

                binding.btnBlock.setOnClickListener(v ->
                        showConfirmBlockDialog(phoneNumber, name));
                binding.viewProfile.setOnClickListener(v -> {
                    Intent contactDetailIntent = new Intent(CustomDialogActivity.this, ContactDetailsActivity.class);
                    contactDetailIntent.putExtra("modal", contactModal);
                    contactDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(contactDetailIntent);
                    finish();
                });

                setupWhatsAppAndSmsButtons(phoneNumber);
            }
            // 'callerNumber' ekstrası var mı kontrol et
            else if (intent.hasExtra("callerNumber")) {
                // Intent'ten doğrudan verileri al
                final String callerNumber = intent.getStringExtra("callerNumber");
                final String callerName = intent.hasExtra("callerName") ?
                        intent.getStringExtra("callerName") : "";

                if (callerNumber == null || callerNumber.isEmpty()) {
                    Log.e(TAG, "Caller number is null or empty");
                    finish();
                    return;
                }

                Log.d(TAG, "Processing intent with callerNumber: " + callerNumber + ", callerName: " + callerName);

                // Yeni Contact nesnesi oluştur
                Contact temp = new Contact();
                temp.setName(callerName);
                temp.setPhoneNumber(callerNumber);

                contactModal = temp;
                contactLiveData.setValue(temp);

                isBlock = blockCallerDbHelper.isPhoneNumberBlocked(callerNumber);
                blockStatusLiveData.setValue(isBlock);

                dialogAction(callerNumber, callerName);

                binding.btnBlock.setOnClickListener(v ->
                        showConfirmBlockDialog(callerNumber, callerName));
                binding.viewProfile.setOnClickListener(v -> {
                    Intent contactDetailIntent = new Intent(CustomDialogActivity.this, ContactDetailsActivity.class);
                    contactDetailIntent.putExtra("name", callerName);
                    contactDetailIntent.putExtra("phoneNumber", callerNumber);
                    contactDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(contactDetailIntent);
                    finish();
                });
                setupWhatsAppAndSmsButtons(callerNumber);
            } else {
                Log.e(TAG, "Required data not found in intent");
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing intent: " + e.getMessage(), e);
            Toast.makeText(this, "Arama bilgileri görüntülenirken hata oluştu", Toast.LENGTH_SHORT).show();

            // Gecikmeli kapanma
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
        }
    }

    private void setupWhatsAppAndSmsButtons(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Invalid phone number for WhatsApp/SMS setup");
            binding.btnWhatsapp.setVisibility(View.GONE);
            binding.btnSms.setVisibility(View.GONE);
            Toast.makeText(this, "Geçerli bir numara yok.", Toast.LENGTH_SHORT).show();
            return;
        }

        // WhatsApp butonu
        binding.btnWhatsapp.setOnClickListener(v -> {
            try {
                String message = " ";
                String formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
                String url = "https://wa.me/" + formattedPhoneNumber + "?text=" + Uri.encode(message);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse(url));

                if (isSamsungDevice()) {
                    try {
                        Intent samsungIntent = new Intent(sendIntent);
                        samsungIntent.setPackage("com.whatsapp");
                        startActivity(samsungIntent);
                        return;
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                PackageManager pm = getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!activities.isEmpty()) {
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(this, "WhatsApp yok!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening WhatsApp: " + e.getMessage());
                Toast.makeText(this, "WhatsApp açılırken hata oluştu", Toast.LENGTH_SHORT).show();
            }
        });

        // SMS butonu
        binding.btnSms.setOnClickListener(v -> {
            try {
                String message = " ";
                String formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + formattedPhoneNumber));
                sendIntent.putExtra("sms_body", message);

                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(this, "SMS uygulaması yok!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening SMS: " + e.getMessage());
                Toast.makeText(this, "SMS açılırken hata oluştu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isSamsungDevice() {
        return android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung");
    }

    public void dialogAction(String number, String name) {
        if (number == null || number.isEmpty()) {
            Log.e(TAG, "Invalid phone number in dialogAction");
            binding.btnSave.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.GONE);
            return;
        }

        // Rehberde kayıtlı olup olmadığını kontrol et
        boolean isInContacts = false;
        try {
            isInContacts = isPhoneNumberSaved(number, this);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if number is saved: " + e.getMessage());
        }

        if (isInContacts) {
            // Rehberde kayıtlıysa düzenleme butonu göster
            binding.btnSave.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.VISIBLE);
            binding.btnEdit.setOnClickListener(v -> {
                try {
                    String lKey = getLookupKeyFromPhoneNumber(number, this);
                    if (lKey != null) {
                        Utils.openContactEditPage(this, lKey);
                    } else {
                        Toast.makeText(this, "Kişi bilgisi bulunamadı", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error opening contact edit page: " + e.getMessage());
                    Toast.makeText(this, "Kişi düzenlenemedi", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Rehberde kayıtlı değilse kaydetme butonu göster
            binding.btnEdit.setVisibility(View.GONE);
            binding.btnSave.setVisibility(View.VISIBLE);
            binding.btnSave.setOnClickListener(v -> {
                try {
                    Utils.openContactCreatePage(this, number, name);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening contact create page: " + e.getMessage());
                    Toast.makeText(this, "Kişi kaydedilemedi", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Çağrı butonu
        binding.btnCall.setOnClickListener(v -> {
            try {
                Utils.makeCall(number, this);
            } catch (Exception e) {
                Log.e(TAG, "Error making call: " + e.getMessage());
                Toast.makeText(this, "Arama yapılamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmBlockDialog(String number, String name) {
        String message, title;
        if (isBlock) {
            message = getString(R.string.do_you_want_to_unblock_this_contact);
            title = getString(R.string.confirm_unblock);
        } else {
            message = getString(R.string.do_you_want_to_block_this_contact);
            title = getString(R.string.confirm_block);
        }

        try {
            new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setTitle(title)
                    .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                        if (isBlock) {
                            if (blockCallerDbHelper.deleteBlockCallerByPhoneNumber(number)) {
                                blockStatusLiveData.setValue(false);
                                Toast.makeText(this, "Engel kaldırıldı", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Engel kaldırılamadı", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (blockCallerDbHelper.addBlockCaller(name, number)) {
                                blockStatusLiveData.setValue(true);
                                Toast.makeText(this, "Kişi engellendi", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Kişi engellenemedi", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.dismiss())
                    .create()
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing block dialog: " + e.getMessage());
            Toast.makeText(this, "İletişim kutusu gösterilemedi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Cevapsız çağrı bildirimi temizle (isteğe bağlı)
        if (isMissedCall) {
            NotificationHelper.clearMissedCallNotification(this);
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "CustomDialogActivity onResume");

        // Eğer initialize edilmediyle veya intent değiştiyse yeniden işle
        if (!isInitialized) {
            processIntent();
            isInitialized = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CustomDialogActivity onDestroy");
    }
}