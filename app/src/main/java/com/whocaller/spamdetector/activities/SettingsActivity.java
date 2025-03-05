/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;


import com.whocaller.spamdetector.BuildConfig;
import com.whocaller.spamdetector.R;
import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.databinding.ActivitySettingsBinding;
import com.whocaller.spamdetector.utils.Utils;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;

    SettingsPrefHelper settingsPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        settingsPrefHelper = new SettingsPrefHelper(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);



        if (getNetworkCountryIso(this) != null) {
            binding.tvCountryRegionSetting1.setText(getNetworkCountryIso(this));
        } else {
            binding.countryRegionSetting.setVisibility(View.GONE);
        }

        if (settingsPrefHelper.getCallerIdPosition().equals(getString(R.string.top))) {
            binding.topPositionRadioButton.setChecked(true);
        } else if (settingsPrefHelper.getCallerIdPosition().equals(getString(R.string.center))) {
            binding.centerPositionRadioButton.setChecked(true);
        } else if (settingsPrefHelper.getCallerIdPosition().equals(getString(R.string.bottom))) {
            binding.bottomPositionRadioButton.setChecked(true);
        }

        binding.topPositionRadioButton.setOnClickListener(radioButtonClickListener);
        binding.centerPositionRadioButton.setOnClickListener(radioButtonClickListener);
        binding.bottomPositionRadioButton.setOnClickListener(radioButtonClickListener);

        binding.switchShowCallerIdSetting.setChecked(settingsPrefHelper.getShowCallerId());

        binding.switchShowPhonebookContactsSetting.setChecked(settingsPrefHelper.getCallerIdOnlyContacts());

        binding.switchShowAfterCallDetailSetting.setChecked(settingsPrefHelper.getShowCallerIdAfterCall());


        binding.switchShowCallerIdSetting.setOnClickListener(v -> settingsPrefHelper.setShowCallerId(binding.switchShowCallerIdSetting.isChecked()));

        binding.switchShowPhonebookContactsSetting.setOnClickListener(v -> settingsPrefHelper.setCallerIdOnlyContacts(binding.switchShowPhonebookContactsSetting.isChecked()));

        binding.switchShowAfterCallDetailSetting.setOnClickListener(v -> settingsPrefHelper.setShowCallerIdAfterCall(binding.switchShowAfterCallDetailSetting.isChecked()));


        binding.callerIdSetting.setOnClickListener(v -> {
            binding.settingsLay.setVisibility(View.GONE);
            binding.headerTitle.setText(getString(R.string.caller_id));
            binding.callerIdLay.setVisibility(View.VISIBLE);
        });

        binding.soundsNotificationSetting.setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        binding.shareAppSetting.setOnClickListener(v -> Utils.shareApp(this, getString(R.string.share_text)));

        binding.checkUpdateSetting.setOnClickListener(v -> Utils.rateApp(this));

        binding.backBtn.setOnClickListener(v -> backPressed());

        binding.aboutUSSetting.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));

        binding.profileSetting.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        binding.privacySetting.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
            intent.putExtra("title", getString(R.string.menu_privacy));
            intent.putExtra("link", settingsPrefHelper.getPrivacyPolicy());
            startActivity(intent);
        });

        binding.searchSetting.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        binding.feedbackSetting.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.putExtra("android.intent.extra.EMAIL", new String[]{settingsPrefHelper.getAppEmail()});
            intent.putExtra("android.intent.extra.SUBJECT", getResources().getString(R.string.app_name) + " Feedback: ");
            intent.putExtra("android.intent.extra.TEXT", "");
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, SettingsActivity.this.getString(R.string.choose_email) + " :"));
        });


    }


    public void backPressed() {
        if (binding.settingsLay.getVisibility() == View.GONE) {
            binding.headerTitle.setText(getString(R.string.settings));
            binding.settingsLay.setVisibility(View.VISIBLE);
            binding.callerIdLay.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    private final View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.topPositionRadioButton.setChecked(false);
            binding.centerPositionRadioButton.setChecked(false);
            binding.bottomPositionRadioButton.setChecked(false);


            ((RadioButton) view).setChecked(true);


            String selectedText = ((RadioButton) view).getText().toString();
            settingsPrefHelper.setCallerIdPosition(selectedText);
            Toast.makeText(SettingsActivity.this, "Selected: " + selectedText, Toast.LENGTH_SHORT).show();
        }
    };

    public String getNetworkCountryIso(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }


}