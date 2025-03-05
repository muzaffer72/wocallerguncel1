/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.ads.Ads;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.databinding.ActivityProfileBinding;
import com.whocaller.spamdetector.utils.Utils;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    private FirebaseAuth mAuth;

    // Orijinal değişkenler
    String FirstName, LastName, Email, Phone, ImageUrl;
    int PROFILE_SCORE = 0;

    // LiveData değişkenleri
    private MutableLiveData<String> firstNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> lastNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> phoneLiveData = new MutableLiveData<>();
    private MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private MutableLiveData<String> imageUrlLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> profileScoreLiveData = new MutableLiveData<>();

    // Sayaçlar için LiveData
    private MutableLiveData<Integer> incomingCallsCountLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> outgoingCallsCountLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> unknownNumberCountLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> spamCallsCountLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> blockSavedCountLiveData = new MutableLiveData<>();

    String TAG = "ProfileActivity";

    @SuppressLint({"Range", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        ProfilePrefHelper profilePrefHelper = new ProfilePrefHelper(this);
        ContactsDataDb contactsDataDb = new ContactsDataDb(this);
        BlockCallerDbHelper blockCallerDbHelper = new BlockCallerDbHelper(this);

        // Reklam
        if (Utils.isNetworkAvailable(this)) {
            Ads ads = new Ads(this);
            ads.showBannerAd(this);
        }

        // LIVEDATA Observerlar
        // 1) İsim-soyisim
        firstNameLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newFirstName) {
                updateProfileName(newFirstName, lastNameLiveData.getValue());
            }
        });
        lastNameLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newLastName) {
                updateProfileName(firstNameLiveData.getValue(), newLastName);
            }
        });

        // 2) Telefon
        phoneLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String phone) {
                if (phone == null || phone.isEmpty()) {
                    binding.profileNumber.setVisibility(View.GONE);
                } else {
                    binding.profileNumber.setVisibility(View.VISIBLE);
                    binding.profileNumber.setText(phone);
                }
            }
        });

        // 3) Email (Eğer ekranda göstermek isterseniz, benzer şekilde binding.* set edebilirsiniz)
        emailLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newEmail) {
                // Örnek: Ekranda email göstermek isterseniz. Mevcut XML yoksa gerek yok.
                // binding.profileEmail.setText(newEmail);  // => XML layout'ta profileEmail varsa
            }
        });

        // 4) Profil resmi
        imageUrlLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String url) {
                if (url == null || url.isEmpty()) {
                    // Boş ise placeholder
                    Glide.with(getApplicationContext())
                            .load(R.drawable.profile_img)
                            .into(binding.profilePic);
                } else {
                    // URL geçerli ise
                    if (Utils.isValidURL(url)) {
                        Glide.with(getApplicationContext())
                                .load(url)
                                .placeholder(R.drawable.profile_img)
                                .error(R.drawable.profile_img)
                                .into(binding.profilePic);
                    } else {
                        // Normalde Config.BASE_URL + "/storage/app/public/" + ...
                        Glide.with(getApplicationContext())
                                .load(Config.BASE_URL + "/storage/app/public/" + url)
                                .placeholder(R.drawable.profile_img)
                                .error(R.drawable.profile_img)
                                .into(binding.profilePic);
                    }
                }
            }
        });

        // 5) Profil skor
        profileScoreLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newScore) {
                if (newScore == null) return;
                if (newScore < 90) {
                    binding.profileProgress.setVisibility(View.VISIBLE);
                    binding.profileScore.setVisibility(View.VISIBLE);
                    binding.editProfileName.setText(R.string.complete_your_profile);
                    binding.nextProfile.setText(ProfilePrefHelper.nextProfileStatus(ProfileActivity.this));
                    binding.profileScore.setText(newScore + "%");
                    binding.profileProgress.setProgress(newScore);
                } else {
                    binding.scoreLay.setVisibility(View.GONE);
                    binding.editProfileName.setText(R.string.edit_profile);
                }
            }
        });

        // 6) Çağrı / Spam / Blok sayaçları
        incomingCallsCountLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                binding.tvIncomingCallsCount.setText(String.valueOf(count));
            }
        });
        outgoingCallsCountLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                binding.tvOutgoingCallsCount.setText(String.valueOf(count));
            }
        });
        unknownNumberCountLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                binding.tvUnknownNumber.setText(String.valueOf(count));
            }
        });
        spamCallsCountLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                binding.tvSpamCallsCount.setText(String.valueOf(count));
            }
        });
        blockSavedCountLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                binding.tvBlockSavedCount.setText(String.valueOf(count));
            }
        });

        // Pref’ten okunan verileri LiveData’ya aktaralım
        FirstName = profilePrefHelper.getFirstName();
        LastName = profilePrefHelper.getLastName();
        Phone = profilePrefHelper.getPhone();
        Email = profilePrefHelper.getEmail();
        ImageUrl = profilePrefHelper.getImage();
        PROFILE_SCORE = ProfilePrefHelper.getProfileScore(this);

        // setValue => Observer tetiklenir, UI güncellenir
        firstNameLiveData.setValue(FirstName);
        lastNameLiveData.setValue(LastName);
        phoneLiveData.setValue(Phone);
        emailLiveData.setValue(Email);
        imageUrlLiveData.setValue(ImageUrl);
        profileScoreLiveData.setValue(PROFILE_SCORE);

        // Sayaçlar
        incomingCallsCountLiveData.setValue(profilePrefHelper.getIncomingCallsCount());
        outgoingCallsCountLiveData.setValue(profilePrefHelper.getOutgoingCallsCount());
        unknownNumberCountLiveData.setValue(new ContactsDataDb(this).getAllContacts().size());
        spamCallsCountLiveData.setValue(new ContactsDataDb(this).getSpamContacts().size());
        blockSavedCountLiveData.setValue(new BlockCallerDbHelper(this).getAllBlockCallers().size());

        // Logout
        binding.logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            profilePrefHelper.saveUserProfile("", "", "", "", "");
            profilePrefHelper.setIsSignUser(false);
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Profili düzenle
        binding.editProfileName.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MyAccountActivity.class)));

        // Geri basıldığında MainActivity'e dön
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Yukarıda buton
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });

        binding.setting.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));

    }

    /**
     * firstName veya lastName değişince profil adını güncellemek için
     */
    private void updateProfileName(String fName, String lName) {
        if (fName == null) fName = "";
        if (lName == null) lName = "";
        String fullName;
        if (lName.isEmpty()) {
            fullName = fName;
        } else {
            fullName = fName + " " + lName;
        }
        binding.profileName.setText(Utils.toTextCase(fullName));
    }
}
