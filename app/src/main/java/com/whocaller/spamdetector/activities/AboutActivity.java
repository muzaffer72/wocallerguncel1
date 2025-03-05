/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.whocaller.spamdetector.BuildConfig;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.ads.Ads;
import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.databinding.ActivityAboutBinding;
import com.whocaller.spamdetector.utils.Utils;


public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        SettingsPrefHelper settingsPrefHelper = new SettingsPrefHelper(this);

        binding.version.setText(getString(R.string.version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");
        binding.company.setText(settingsPrefHelper.getAppDevelopedBy());
        binding.email.setText(settingsPrefHelper.getAppEmail());
        binding.website.setText(settingsPrefHelper.getAppWebSite());
        binding.contact.setText(settingsPrefHelper.getAppContact());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        binding.backBtn.setOnClickListener(v -> callback.handleOnBackPressed());

        if (Utils.isNetworkAvailable(this)) {
            Ads ads = new Ads(this);
            ads.loadingNativeAdSmall(this);
        }

    }
}

