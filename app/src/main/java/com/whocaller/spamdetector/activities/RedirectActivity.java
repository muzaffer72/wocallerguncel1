/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.databinding.ActivityRedirectBinding;

public class RedirectActivity extends AppCompatActivity {
    ActivityRedirectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRedirectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        SettingsPrefHelper settingsPrefHelper = new SettingsPrefHelper(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        Intent intent = getIntent();

        if (intent.getBooleanExtra("isUpdate", false)) {

            binding.title.setText(getString(R.string.redirect_title));
            binding.description.setText(settingsPrefHelper.getAppUpdateDesc());
            binding.btnActionTxt.setText(getString(R.string.update_app));
            binding.btnClose.setOnClickListener(v -> finishAffinity());


            binding.btnSkip.setOnClickListener(v -> startActivity(new Intent(RedirectActivity.this, MainActivity.class)));

            binding.btnAction.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(settingsPrefHelper.getAppRedirectUrl())));
                finishAffinity();
            });

        } else {
            binding.btnSkip.setVisibility(View.GONE);
            binding.title.setText(getString(R.string.maintenance_title));
            binding.description.setText(getString(R.string.maintenance_description));
            binding.btnActionTxt.setText(getString(R.string.try_again_later));
            binding.btnClose.setOnClickListener(v -> finishAffinity());
            binding.btnAction.setOnClickListener(v -> finishAffinity());
        }


    }


}