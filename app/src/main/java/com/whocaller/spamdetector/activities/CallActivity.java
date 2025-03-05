/*
 * Düzeltilmiş CallActivity.java
 * - Zil sesini kısma sorunu çözüldü
 * - Arama bildirimi UI sorunları giderildi
 * - Arayan bildirimi tutarlı hale getirildi
 */
package com.whocaller.spamdetector.activities;

import static com.whocaller.spamdetector.helpers.CallManager.hangUpCall;
import static com.whocaller.spamdetector.helpers.CallManager.num;
import static com.whocaller.spamdetector.helpers.ContactsHelper.getContactNameFromLocal;
import static com.whocaller.spamdetector.utils.Utils.getContactImage;
import static com.whocaller.spamdetector.utils.Utils.isContactStarred;
import static com.whocaller.spamdetector.utils.Utils.isPhoneNumberSaved;
import static com.whocaller.spamdetector.utils.Utils.isValidName;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.databinding.ActivityCallBinding;
import com.whocaller.spamdetector.helpers.CallManager;
import com.whocaller.spamdetector.helpers.NotificationHelper;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Utils;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CallActivity";

    // LiveData tanımlamaları
    private MutableLiveData<String> callerNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> phoneNumberLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> callStateLiveData = new MutableLiveData<>();

    LinearLayout endCallBtn;

    @SuppressLint("StaticFieldLeak")
    public static Button recordBtn;
    ImageView mergeCallBtn;

    @SuppressLint("StaticFieldLeak")
    public static ImageView muteBtn, speakerBtn, holdBtn, addCallBtn;

    private float dY;
    LinearLayout draggableButton;
    ImageView arrowUp, arrowDown, actionBtn;
    private float topLimit;
    private float bottomLimit;
    private boolean isButtonDragged = false;
    private float initialY;
    ObjectAnimator shinyAnimator;

    Button btn0, btn01, btn02, btn03, btn04, btn05, btn06, btn07, btn08, btn09, btnStar, btnHash;

    BottomSheetDialog keypadDialog;
    String keypadDialogTextViewText = "";

    @SuppressLint("StaticFieldLeak")
    public static TextView callerNameTV, callerPhoneNumberTV, callDurationTV, callingStatusTV;
    @SuppressLint("StaticFieldLeak")
    public static TextView incomingCallerPhoneNumberTV, incomingCallerNameTV, ringingStatusTV;

    RelativeLayout inProgressCallRLView, incomingRLView;

    public static boolean isMuted, isSpeakerOn, isCallOnHold;
    public static String PHONE_NUMBER, CALLER_NAME;
    public static String muteBtnName, speakerBtnName;

    ContactsDataDb contactsDataDb;
    Window window;
    ActivityCallBinding binding;
    SettingsPrefHelper settingsPrefHelper;
    BlockCallerDbHelper blockCallerDbHelper;

    private CallManager callManager;
    private AudioManager audioManager;

    public void callStatus(int i) {
        if (i != 1) {
            if (i == 2) {
                calling();
                return;
            } else if (i == 4) {
                callAnswerd();
                return;
            } else if (i == 7) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancel(NotificationHelper.NOTIFICATION_ID);
                try {
                    unregisterReceiver(this.receiver);
                } catch (Exception e) {
                    Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
                }
                callEnd();
                return;
            } else if (i == 10) {
                return;
            }
        }
        callAnswerd();
    }

    // BroadcastReceiver; çağrı durum ve süre güncellemeleri için
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "CallActivity onReceive: " + intent.getAction());
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(CallManager.ACTION_CALL)) {
                int callState = intent.getIntExtra("data", -1);
                callStateLiveData.setValue(callState);
                return;
            }
            String stringExtra = intent.getStringExtra("time");
            if (stringExtra == null || stringExtra.isEmpty()) {
                stringExtra = "00:00";
            }
            setTime(stringExtra);
        }
    };

    @SuppressLint("SetTextI18n")
    private void callAnswerd() {
        inProgressCallRLView.setVisibility(View.VISIBLE);
        incomingRLView.setVisibility(View.GONE);

        PHONE_NUMBER = num;
        phoneNumberLiveData.setValue(PHONE_NUMBER);

        CALLER_NAME = getContactNameFromLocal(PHONE_NUMBER, CallActivity.this);
        Contact contact = contactsDataDb.getContactByPhoneNumber(PHONE_NUMBER);
        if (contact != null) {
            CALLER_NAME = contact.getName();
        } else {
            if (Utils.isNetworkAvailable(CallActivity.this)) {
                getContactData();
            }
        }
        callerNameLiveData.setValue(CALLER_NAME);
        callerNameTV.setText(CALLER_NAME);

        if (isContactStarred(PHONE_NUMBER, CallActivity.this)) {
            binding.favorite2.setImageResource(R.drawable.favorite_fill);
            binding.favorite2.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(CallActivity.this, R.color.white)));
        }

        if (isValidName(CALLER_NAME)) {
            Bitmap contactImage = getContactImage(CallActivity.this, PHONE_NUMBER);
            if (contactImage != null) {
                binding.profilePic2.setImageBitmap(contactImage);
            } else {
                binding.profilePic2.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
            }
        } else {
            binding.profilePic2.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
        }

        if (contact != null) {
            if (contact.getCountryName() != null) {
                binding.countryName2.setVisibility(View.VISIBLE);
                binding.carriorName2.setVisibility(View.VISIBLE);
                binding.view2.setVisibility(View.VISIBLE);
                binding.countryName2.setText(contact.getCountryName());
                if (contact.getCarrierName() != null) {
                    binding.carriorName2.setText(contact.getCarrierName());
                }
            }
            if (contact.isWho()) {
                binding.whoProfile2.setVisibility(View.VISIBLE);
            }
            if (contact.getContactsBy() != null) {
                if (contact.getContactsBy().equals("whocaller") || !isPhoneNumberSaved(PHONE_NUMBER, CallActivity.this)) {
                    binding.whoLay2.setVisibility(View.VISIBLE);
                }
            }
            if (contact.isSpam()) {
                window.setStatusBarColor(getResources().getColor(R.color.red, null));
                binding.profilePic2.setImageDrawable(getResources().getDrawable(R.drawable.spam_circle, null));
                binding.inProgressCallRLView.setBackground(getDrawable(R.drawable.bg_red));
            }
        }

        if (blockCallerDbHelper.isPhoneNumberBlocked(PHONE_NUMBER)) {
            window.setStatusBarColor(getResources().getColor(R.color.red, null));
            binding.profilePic2.setImageDrawable(getResources().getDrawable(R.drawable.block_circle, null));
            binding.inProgressCallRLView.setBackground(getDrawable(R.drawable.bg_red));
        }
        callerPhoneNumberTV.setText(PHONE_NUMBER);

        // Butonların durumlarını güncelle
        updateButtonStates();

        callingStatusTV.setText(R.string.call_in_progress);

        // NotificationHelper'a bildirimleri devralacağımızı söyle
        NotificationHelper.setRingtoneHandledByActivity(true);
    }

    // Buton durumlarını güncelleyen yeni metot
    private void updateButtonStates() {
        // Ses ayarı butonlarını güncelle
        if (isMuted) {
            muteBtnName = getString(R.string.unmute);
            binding.muteBtn.setEnabled(true);
            binding.muteBtn.setClickable(true);
            binding.muteBtnTxt.setText(R.string.unmute);
            binding.muteBtn.setBackgroundResource(R.drawable.rounded_fill_btn);
            binding.muteBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
        } else {
            muteBtnName = getString(R.string.mute);
            binding.muteBtn.setEnabled(true);
            binding.muteBtn.setClickable(true);
            binding.muteBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
            binding.muteBtn.setBackgroundResource(R.drawable.rounded_padding_btn);
            binding.muteBtnTxt.setText(R.string.mute);
        }

        if (isSpeakerOn) {
            speakerBtnName = getString(R.string.speaker_off);
            binding.speakerBtn.setBackgroundResource(R.drawable.rounded_fill_btn);
            binding.speakerBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
        } else {
            speakerBtnName = getString(R.string.speaker_on);
            binding.speakerBtn.setBackgroundResource(R.drawable.rounded_padding_btn);
            binding.speakerBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
        }

        // Bildirimi güncelle
        if (callManager != null && callManager.getCall() != null) {
            NotificationHelper.createIngoingCallNotification(
                    CallActivity.this,
                    callManager.getCall(),
                    callDurationTV.getText().toString(),
                    speakerBtnName,
                    muteBtnName
            );
        }
    }

    private void callEnd() {
        // Activity'yi kapatmadan önce zil sesini ve bildirimleri durdur
        NotificationHelper.stopRingtoneAndNotification(this);

        finishAndRemoveTask();

        String name = (CALLER_NAME == null) ? getContactNameFromLocal(PHONE_NUMBER, CallActivity.this) : CALLER_NAME;

        if (settingsPrefHelper.getShowCallerId()) {
            Intent contactDetailIntent = new Intent(CallActivity.this, CustomDialogActivity.class);
            contactDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contactDetailIntent.putExtra("callerName", name);
            contactDetailIntent.putExtra("callerNumber", PHONE_NUMBER);

            // Intent bilgilerini logla
            Log.d(TAG, "Starting CustomDialogActivity with name: " + name + ", number: " + PHONE_NUMBER);

            startActivity(contactDetailIntent);
        }
    }

    private void calling() {
        inProgressCallRLView.setVisibility(View.GONE);
        incomingRLView.setVisibility(View.VISIBLE);

        PHONE_NUMBER = num;
        phoneNumberLiveData.setValue(PHONE_NUMBER);

        if (blockCallerDbHelper.isPhoneNumberBlocked(PHONE_NUMBER)) {
            Log.d(TAG, "Blocked number detected, hanging up: " + PHONE_NUMBER);
            callManager.hangup();
            callEnd();
        } else {
            shinyAnimator.start();
            inProgressCallRLView.setVisibility(View.GONE);
            incomingRLView.setVisibility(View.VISIBLE);

            CALLER_NAME = getContactNameFromLocal(PHONE_NUMBER, CallActivity.this);
            Contact contact = contactsDataDb.getContactByPhoneNumber(PHONE_NUMBER);
            if (contact != null) {
                CALLER_NAME = contact.getName();
                binding.whoLay.setVisibility(View.VISIBLE);
            } else {
                if (Utils.isNetworkAvailable(this)) {
                    getContactData();
                }
            }
            callerNameLiveData.setValue(CALLER_NAME);
            incomingCallerNameTV.setText(CALLER_NAME);
            incomingCallerPhoneNumberTV.setText(PHONE_NUMBER);

            if (isContactStarred(PHONE_NUMBER, this)) {
                binding.favorite.setImageResource(R.drawable.favorite_fill);
                binding.favorite.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.white)));
            }

            if (isValidName(CALLER_NAME)) {
                Bitmap contactImage = getContactImage(CallActivity.this, PHONE_NUMBER);
                if (contactImage != null) {
                    binding.profilePic.setImageBitmap(contactImage);
                } else {
                    binding.profilePic.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
                }
            } else {
                binding.profilePic.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
            }

            if (contact != null) {
                if (contact.getCountryName() != null) {
                    binding.countryName.setVisibility(View.VISIBLE);
                    binding.countryName.setText(contact.getCountryName());
                }
                if (contact.getCarrierName() != null) {
                    binding.carriorName.setVisibility(View.VISIBLE);
                    binding.carriorName.setText(contact.getCarrierName());
                }
                if (contact.isWho()) {
                    binding.whoProfile.setVisibility(View.VISIBLE);
                }
                if (contact.isSpam()) {
                    window.setStatusBarColor(getResources().getColor(R.color.red, null));
                    binding.profilePic.setImageDrawable(getResources().getDrawable(R.drawable.spam_circle, null));
                    binding.incomingRLView.setBackground(getDrawable(R.drawable.bg_red));
                }
                if (blockCallerDbHelper.isPhoneNumberBlocked(PHONE_NUMBER)) {
                    window.setStatusBarColor(getResources().getColor(R.color.red, null));
                    binding.profilePic2.setImageDrawable(getResources().getDrawable(R.drawable.block_circle, null));
                    binding.inProgressCallRLView.setBackground(getDrawable(R.drawable.bg_red));
                }
            }

            // Callback'i kaydet - çağrı durumu değişikliklerini almak için
            if (callManager != null && callManager.getCall() != null) {
                callManager.getCall().registerCallback(new Call.Callback() {
                    @Override
                    public void onStateChanged(Call call, int state) {
                        // UI thread'e geçiş yap
                        runOnUiThread(() -> {
                            callStateLiveData.setValue(state);
                        });

                        // NotificationHelper üzerinden durum değişimini yönet
                        NotificationHelper.handleCallStateChange(CallActivity.this, call);
                    }
                });
            }

            // NotificationHelper'a bildirimleri devralacağımızı söyle
            NotificationHelper.setRingtoneHandledByActivity(true);
        }
    }

    private void setTime(String stringExtra) {
        callDurationTV.setVisibility(View.VISIBLE);
        callDurationTV.setText(stringExtra);

        // Bildirimi güncelle
        if (callManager != null && callManager.getCall() != null) {
            NotificationHelper.createIngoingCallNotification(
                    CallActivity.this,
                    callManager.getCall(),
                    stringExtra,
                    speakerBtnName,
                    muteBtnName
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint({"UseCompatTextViewDrawableApis", "SetTextI18n", "ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Ses yönetimi için AudioManager oluştur
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Fiziksel ses tuşları ile RING ses seviyesini kontrol edebilmek için
        setVolumeControlStream(AudioManager.STREAM_RING);

        // LiveData observer'lar
        callerNameLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String callerName) {
                if (callerName == null) return;
                callerNameTV.setText(callerName);
                incomingCallerNameTV.setText(callerName);
            }
        });
        phoneNumberLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String phoneNumber) {
                if (phoneNumber == null) return;
                incomingCallerPhoneNumberTV.setText(phoneNumber);
                callerPhoneNumberTV.setText(phoneNumber);
            }
        });
        callStateLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer state) {
                if (state == null) return;
                callStatus(state);
            }
        });

        Log.d(TAG, "CallActivity onCreate");

        Intent intent = getIntent();
        String getNumber = intent.getStringExtra("num");

        // Varsa, hemen PHONE_NUMBER'ı güncelle
        if (getNumber != null && !getNumber.isEmpty()) {
            PHONE_NUMBER = getNumber;
            phoneNumberLiveData.setValue(PHONE_NUMBER);
        }

        muteBtnName = getString(R.string.mute);
        speakerBtnName = getString(R.string.speaker);
        settingsPrefHelper = new SettingsPrefHelper(this);
        contactsDataDb = new ContactsDataDb(this);
        blockCallerDbHelper = new BlockCallerDbHelper(this);

        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary, null));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CallManager.ACTION_CALL);
        intentFilter.addAction(CallManager.ACTION_TIME);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            registerReceiver(this.receiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(this.receiver, intentFilter);
        }

        callManager = new CallManager(this);

        initializeValues();
        addLockScreenFlags();
        setButtonsEnabled(); // Butonların ENABLED olması daha doğru

        Animation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(300);

        shinyAnimator = ObjectAnimator.ofFloat(arrowUp, "alpha", 0f, 1f);
        shinyAnimator.setDuration(800);
        shinyAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        shinyAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        draggableButton.post(() -> {
            initialY = draggableButton.getY();
            topLimit = 0;
            bottomLimit = initialY + 320;
        });

        callerNameTV.setText(num);

        draggableButton.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dY = view1.getY() - motionEvent.getRawY();
                    isButtonDragged = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float newY = motionEvent.getRawY() + dY;
                    newY = Math.max(topLimit, Math.min(bottomLimit, newY));
                    draggableButton.setY(newY);
                    arrowUp.setY(newY - arrowUp.getHeight() - 70);
                    arrowDown.setY(newY + draggableButton.getHeight() + 70);
                    isButtonDragged = true;

                    if (arrowUp.getY() < topLimit) {
                        arrowUp.startAnimation(AnimationUtils.loadAnimation(CallActivity.this, R.anim.fade_out));
                    } else {
                        arrowUp.clearAnimation();
                        arrowUp.setAlpha(1.0f);
                        if (!shinyAnimator.isRunning()) {
                            shinyAnimator.start();
                        }
                    }
                    if (arrowDown.getY() > bottomLimit) {
                        arrowDown.startAnimation(AnimationUtils.loadAnimation(CallActivity.this, R.anim.fade_out));
                    } else {
                        arrowDown.clearAnimation();
                        arrowDown.setAlpha(1.0f);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isButtonDragged) {
                        if (draggableButton.getY() <= topLimit) {
                            CallManager.answerCall(callManager.getCall());
                            arrowDown.setVisibility(View.VISIBLE);
                            arrowUp.setVisibility(View.GONE);
                        } else if (draggableButton.getY() >= bottomLimit) {
                            hangUpCall(callManager.getCall());
                            callManager.hangup();
                            arrowUp.setVisibility(View.VISIBLE);
                            arrowDown.setVisibility(View.GONE);
                        } else {
                            draggableButton.animate().y(initialY).setDuration(300).start();
                            arrowUp.animate().y(initialY - arrowUp.getHeight() - 70).setDuration(300).start();
                            arrowDown.animate().y(initialY + draggableButton.getHeight() + 70).setDuration(300).start();

                            arrowUp.clearAnimation();
                            arrowUp.setAlpha(1.0f);
                            if (!shinyAnimator.isRunning()) {
                                shinyAnimator.start();
                            }
                            arrowDown.clearAnimation();
                            arrowDown.setAlpha(1.0f);
                        }
                    }
                    if (draggableButton.getY() > 0) {
                        actionBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black, null)));
                    }
                    return true;
                default:
                    return false;
            }
        });

        endCallBtn.setOnClickListener(v -> {
            callManager.hangup();
            finishAffinity();
        });

        binding.holdBtn.setOnClickListener(v -> {
            if (isCallOnHold) {
                CallManager.unholdCall(callManager.getCall());
                binding.holdBtn.setBackgroundResource(R.drawable.rounded_padding_btn);
                binding.holdBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                isCallOnHold = false;
            } else {
                CallManager.holdCall(callManager.getCall());
                binding.holdBtn.setBackgroundResource(R.drawable.rounded_fill_btn);
                binding.holdBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                isCallOnHold = true;
            }
        });

        binding.muteBtn.setOnClickListener(v -> {
            if (isMuted) {
                CallManager.muteCall(false);
                binding.muteBtn.setBackgroundResource(R.drawable.rounded_padding_btn);
                binding.muteBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                binding.muteBtnTxt.setText(R.string.mute);
                isMuted = false;

                muteBtnName = getString(R.string.mute);
                updateButtonStates();
            } else {
                CallManager.muteCall(true);
                binding.muteBtn.setBackgroundResource(R.drawable.rounded_fill_btn);
                binding.muteBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                binding.muteBtnTxt.setText(R.string.unmute);
                isMuted = true;

                muteBtnName = getString(R.string.unmute);
                updateButtonStates();
            }
        });

        binding.speakerBtn.setOnClickListener(v -> {
            if (isSpeakerOn) {
                CallManager.speakerCall(false);
                binding.speakerBtn.setBackgroundResource(R.drawable.rounded_padding_btn);
                binding.speakerBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                isSpeakerOn = false;

                speakerBtnName = getString(R.string.speaker_on);
                updateButtonStates();
            } else {
                CallManager.speakerCall(true);
                binding.speakerBtn.setBackgroundResource(R.drawable.rounded_fill_btn);
                binding.speakerBtn.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                isSpeakerOn = true;

                speakerBtnName = getString(R.string.speaker_off);
                updateButtonStates();
            }
        });

        binding.keyPadBtn.setOnClickListener(v -> {
            keypadDialog = new BottomSheetDialog(CallActivity.this);
            keypadDialog.setContentView(R.layout.in_progress_call_dialpad);
            keypadDialog.setCanceledOnTouchOutside(true);
            keypadDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

            ImageButton keypadCancelBtn = keypadDialog.findViewById(R.id.keypadCancelBtn);
            if (keypadCancelBtn != null) {
                keypadCancelBtn.setOnClickListener(v1 -> keypadDialog.cancel());
            }

            Button endCallBottomSheet = keypadDialog.findViewById(R.id.endCallBtnBottomSheet);
            if (endCallBottomSheet != null) {
                endCallBottomSheet.setOnClickListener(v1 -> hangUpCall(callManager.getCall()));
            }

            initBottomSheetBtnsAndPlayDTMFtones(callManager.getCall(),
                    keypadDialog,
                    keypadDialog.findViewById(R.id.keypadDialogTextView));
            keypadDialog.show();
        });

        binding.addCallBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    // Zil sesini yükseltmek veya alçaltmak için ses tuşlarını dinle
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (audioManager != null) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    // Aktif çağrı durumuna göre ses kısma davranışını ayarla
                    if (callStateLiveData.getValue() != null && callStateLiveData.getValue() == Call.STATE_ACTIVE) {
                        // Aktif çağrı sırasında kulaklık/hoparlör sesi
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,
                                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    } else {
                        // Gelen çağrı sırasında zil sesi
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

                        // Eğer zil sesi minimuma indirildi ise, zili kapat
                        int volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        if (volume == 0) {
                            NotificationHelper.silenceRingtone(this);
                            Toast.makeText(this, R.string.ringtone_silenced, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Ses yükseltme için benzer mantık
                    if (callStateLiveData.getValue() != null && callStateLiveData.getValue() == Call.STATE_ACTIVE) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,
                                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    } else {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    }
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Activity temizliği
        NotificationHelper.setRingtoneHandledByActivity(false);
        NotificationHelper.stopRingtoneAndNotification(this);

        if (keypadDialog != null && keypadDialog.isShowing()) {
            keypadDialog.dismiss();
        }

        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private void initializeValues() {
        endCallBtn = findViewById(R.id.endCallBtn);
        muteBtn = findViewById(R.id.muteBtn);
        speakerBtn = findViewById(R.id.speakerBtn);
        holdBtn = findViewById(R.id.holdBtn);
        recordBtn = findViewById(R.id.recordBtn);
        addCallBtn = findViewById(R.id.addCallBtn);
        mergeCallBtn = findViewById(R.id.mergeCallBtn);
        draggableButton = findViewById(R.id.draggable_button);
        arrowUp = findViewById(R.id.arrow_up);
        arrowDown = findViewById(R.id.arrow_down);
        actionBtn = findViewById(R.id.actionBtn);
        callerNameTV = findViewById(R.id.callerName);
        callerPhoneNumberTV = findViewById(R.id.callerPhoneNumber);
        callDurationTV = findViewById(R.id.callingDuration);
        callingStatusTV = findViewById(R.id.callingStatus);
        inProgressCallRLView = findViewById(R.id.inProgressCallRLView);
        incomingRLView = findViewById(R.id.incomingRLView);
        incomingCallerPhoneNumberTV = findViewById(R.id.incomingCallerPhoneNumberTV);
        incomingCallerNameTV = findViewById(R.id.incomingCallerNameTV);
        ringingStatusTV = findViewById(R.id.ringingStatus);
    }

    @SuppressLint("SetTextI18n")
    private void initBottomSheetBtnsAndPlayDTMFtones(Call call, BottomSheetDialog keypadDialog, TextView keypadDialogTextView) {
        btn0 = keypadDialog.findViewById(R.id.btn0);
        btn01 = keypadDialog.findViewById(R.id.btn01);
        btn02 = keypadDialog.findViewById(R.id.btn02);
        btn03 = keypadDialog.findViewById(R.id.btn03);
        btn04 = keypadDialog.findViewById(R.id.btn04);
        btn05 = keypadDialog.findViewById(R.id.btn05);
        btn06 = keypadDialog.findViewById(R.id.btn06);
        btn07 = keypadDialog.findViewById(R.id.btn07);
        btn08 = keypadDialog.findViewById(R.id.btn08);
        btn09 = keypadDialog.findViewById(R.id.btn09);
        btnStar = keypadDialog.findViewById(R.id.btnStar);
        btnHash = keypadDialog.findViewById(R.id.btnHash);

        btn0.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '0');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "0");
        });
        btn0.setOnLongClickListener(v -> {
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "+");
            return true;
        });
        btn01.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '1');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "1");
        });
        btn02.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '2');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "2");
        });
        btn03.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '3');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "3");
        });
        btn04.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '4');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "4");
        });
        btn05.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '5');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "5");
        });
        btn06.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '6');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "6");
        });
        btn07.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '7');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "7");
        });
        btn08.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '8');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "8");
        });
        btn09.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '9');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "9");
        });
        btnStar.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '*');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "*");
        });
        btnHash.setOnClickListener(v -> {
            CallManager.playDtmfTone(call, '#');
            keypadDialogTextViewText = keypadDialogTextView.getText().toString();
            keypadDialogTextView.setText(keypadDialogTextViewText + "#");
        });
    }

    private void setButtonsEnabled() {
        binding.muteBtn.setEnabled(true);
        binding.muteBtn.setClickable(true);
        binding.holdBtn.setEnabled(true);
        binding.holdBtn.setClickable(true);
        recordBtn.setEnabled(true);
        recordBtn.setClickable(true);
        binding.addCallBtn.setEnabled(true);
        binding.addCallBtn.setClickable(true);
    }

    private void addLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (callManager == null || callManager.getCall() == null) {
            Log.d(TAG, "onResume: No active call, UI will update when ready");
        } else {
            Log.d(TAG, "onResume: Active call found, updating UI");
            callStatus(callManager.getCall().getState());

            // UI'yi son duruma göre ayarla
            updateButtonStates();
        }

        // NotificationHelper'a bildirimleri devralacağımızı söyle
        NotificationHelper.setRingtoneHandledByActivity(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Bildirimleri sisteme geri bırak
        NotificationHelper.setRingtoneHandledByActivity(false);
    }

    private void getContactData() {
        Utils.getContactDataDetails(PHONE_NUMBER, new Utils.ContactDataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data != null) {
                    if (data instanceof UserProfile) {
                        UserProfile profile = (UserProfile) data;
                        CALLER_NAME = profile.getFirstName() + " " + profile.getLastName();
                    } else if (data instanceof Contact) {
                        Contact contactz = (Contact) data;
                        CALLER_NAME = contactz.getName();
                    } else {
                        Log.e(TAG, "getContactDataDetails: Unknown data type");
                    }
                    callerNameLiveData.setValue(CALLER_NAME);
                } else {
                    Log.e(TAG, "getContactDataDetails: null");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "getContactDataDetails: " + errorMessage);
                CALLER_NAME = getContactNameFromLocal(PHONE_NUMBER, CallActivity.this);
                callerNameLiveData.setValue(CALLER_NAME);
            }
        });
    }
}