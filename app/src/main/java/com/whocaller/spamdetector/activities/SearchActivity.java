/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.adapter.OnlineContactAdapter;
import com.whocaller.spamdetector.ads.Ads;
import com.whocaller.spamdetector.api.ApiClient;
import com.whocaller.spamdetector.databinding.ActivitySearchBinding;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {


    ActivitySearchBinding binding;
    LottieAnimationView animationView;

    private List<Contact> onlineContactList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        animationView = findViewById(R.id.animationView);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);



        Intent intents = getIntent();

        if (Utils.isNetworkAvailable(this)) {
            Ads ads = new Ads(this);
            ads.loadingNativeAdSmall(this);
        }

        if (intents != null) {
            String txt = intents.getStringExtra("SEARCH");

            if (txt != null) {
                if (txt.length() >= 10){
                    binding.searchEt.setText(txt);
                    searchTextStart();
                    new Handler().postDelayed(() -> searchContacts(txt), 1000);
                }else {
                    binding.searchEt.setError(getString(R.string.you_need_to_enter_the_last_4_characters));
                }

            }
        }


        binding.backBtn.setOnClickListener(v -> backPressed());

        binding.btnSearch.setOnClickListener(v -> {
            if (validation()) {
                if (Utils.isNetworkAvailable(SearchActivity.this)) {
                    searchTextStart();
                    new Handler().postDelayed(() -> searchContacts(binding.searchEt.getText().toString()), 1000);
                } else {
                    Utils.showToast(SearchActivity.this, getResources().getString(R.string.check_internet));
                }

            }
        });
    }


    public void searchTextStart() {
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
        binding.btnSearch.setEnabled(false);
        binding.searchTextBtn.setText(getResources().getString(R.string.searching));
    }

    public void searchTextStop() {
        animationView.setVisibility(View.GONE);
        animationView.cancelAnimation();
        binding.btnSearch.setEnabled(true);
        binding.searchTextBtn.setText(getResources().getString(R.string.search));
    }


    public void searchContacts(String searchTerm) {
        ApiClient.ApiService apiService = ApiClient.getClient().create(ApiClient.ApiService.class);
        Call<List<Contact>> call = apiService.searchContacts(searchTerm);

        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call, @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Contact> contacts = response.body();
                    onlineContactList = contacts;
                    displayContacts(onlineContactList);
                } else {
                    Toast.makeText(SearchActivity.this, "Bulunamadı", Toast.LENGTH_SHORT).show();
                    searchTextStop();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                searchTextStop();
                Log.e("SearchActivity", "Error: " + t.getMessage());
            }
        });

    }


    private void displayContacts(List<Contact> contacts) {
        if (contacts.isEmpty()) {
            binding.onlineRecycler.setVisibility(View.GONE);
            binding.mainLay.setVisibility(View.VISIBLE);
            searchTextStop();
            Toast.makeText(this, "Bulunamadı", Toast.LENGTH_SHORT).show();
        } else {
            binding.mainLay.setVisibility(View.GONE);
            binding.onlineRecycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
            binding.onlineRecycler.setVisibility(View.VISIBLE);
            OnlineContactAdapter onlineContactAdapter = new OnlineContactAdapter(contacts, this);
            binding.onlineRecycler.setAdapter(onlineContactAdapter);
            searchTextStop();
        }
    }

    public boolean validation() {
        if (binding.searchEt.getText().toString().isEmpty()) {
            binding.searchEt.setError(getString(R.string.name_or_number));
            return false;
        } else if (binding.searchEt.getText().toString().length() < 9){
            binding.searchEt.setError(getString(R.string.you_need_to_enter_the_last_4_characters));
            return false;
        }
        return true;
    }


    public void backPressed() {
        if (binding.onlineRecycler.getVisibility() == View.VISIBLE) {
            binding.onlineRecycler.setVisibility(View.GONE);
            binding.mainLay.setVisibility(View.VISIBLE);
        } else {
            Utils.reDirectMainActivity(SearchActivity.this);
        }
    }

}