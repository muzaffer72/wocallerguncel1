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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.api.ApiClient;
import com.whocaller.spamdetector.databinding.ActivityOtpBinding;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.PhoneNumberUtils;
import com.whocaller.spamdetector.utils.Utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String phoneNumber;
    private String mVerificationId;


    String countryCode;

    ActivityOtpBinding binding;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();

        countryCode = binding.countryPicker.getDefaultCountryCode();

        binding.countryPicker.setOnCountryChangeListener(() -> countryCode = binding.countryPicker.getSelectedCountryCode());


        binding.btnSend.setOnClickListener(v -> {
            if (Utils.isNetworkAvailable(OtpActivity.this)) {
                if (validation1()) {
                    phoneNumber = "+" + countryCode + binding.edMobile.getText().toString();
                    sendVerificationCode(phoneNumber);
                    binding.genarateProgressBar.setVisibility(View.VISIBLE);
                    binding.genarateText.setVisibility(View.GONE);
                    binding.btnSend.setEnabled(false);
                    binding.txtMob.setText("We have sent you an SMS on " + " " + phoneNumber + "\n with 6 digit verification code");
                }
            } else {
                Utils.showToast(OtpActivity.this, getResources().getString(R.string.check_internet));
            }


        });


        binding.btnVerify.setOnClickListener(v -> {
            if (Utils.isNetworkAvailable(OtpActivity.this)) {
                if (validation()) {
                    verifyOtp(Objects.requireNonNull(binding.edOtp1.getText()) + "" + Objects.requireNonNull(binding.edOtp2.getText()) + "" + Objects.requireNonNull(binding.edOtp3.getText()) + "" + Objects.requireNonNull(binding.edOtp4.getText()) + "" + Objects.requireNonNull(binding.edOtp5.getText()) + "" + Objects.requireNonNull(binding.edOtp6.getText()));
                    binding.verifyProgressBar.setVisibility(View.VISIBLE);
                    binding.verifyText.setVisibility(View.GONE);
                    binding.btnVerify.setEnabled(false);
                }
            } else {
                Utils.showToast(OtpActivity.this, getResources().getString(R.string.check_internet));
            }

        });

        binding.backBtn.setOnClickListener(v -> {
            if (binding.verifyLay.getVisibility() == View.VISIBLE) {
                binding.verifyLay.setVisibility(View.GONE);
                binding.sendLay.setVisibility(View.VISIBLE);
            } else {
                startActivity(new Intent(OtpActivity.this, LoginActivity.class));
            }

        });


        setOtpTextWatcher(binding.edOtp1, null, binding.edOtp2);
        setOtpTextWatcher(binding.edOtp2, binding.edOtp1, binding.edOtp3);
        setOtpTextWatcher(binding.edOtp3, binding.edOtp2, binding.edOtp4);
        setOtpTextWatcher(binding.edOtp4, binding.edOtp3, binding.edOtp5);
        setOtpTextWatcher(binding.edOtp5, binding.edOtp4, binding.edOtp6);
        setOtpTextWatcher(binding.edOtp6, binding.edOtp5, null);
    }

    private void setOtpTextWatcher(final EditText currentEditText, final EditText previousEditText, final EditText nextEditText) {
        currentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextEditText != null) {
                    nextEditText.requestFocus();
                } else if (s.length() == 0 && previousEditText != null) {
                    previousEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public boolean validation() {

        if (Objects.requireNonNull(binding.edOtp1.getText()).toString().isEmpty()) {
            binding.edOtp1.setError("");
            return false;
        }
        if (Objects.requireNonNull(binding.edOtp2.getText()).toString().isEmpty()) {
            binding.edOtp2.setError("");
            return false;
        }
        if (Objects.requireNonNull(binding.edOtp3.getText()).toString().isEmpty()) {
            binding.edOtp3.setError("");
            return false;
        }
        if (Objects.requireNonNull(binding.edOtp4.getText()).toString().isEmpty()) {
            binding.edOtp4.setError("");
            return false;
        }
        if (Objects.requireNonNull(binding.edOtp5.getText()).toString().isEmpty()) {
            binding.edOtp5.setError("");
            return false;
        }
        if (Objects.requireNonNull(binding.edOtp6.getText()).toString().isEmpty()) {
            binding.edOtp6.setError("");
            return false;
        }
        return true;
    }


    public boolean validation1() {
        if (binding.edMobile.getText().toString().isEmpty()) {
            binding.edMobile.setError(getString(R.string.emobileno));
            return false;
        }

        String nationalFormat = PhoneNumberUtils.toNationalFormat("+" + countryCode + binding.edMobile.getText().toString(), "TR");
        if (nationalFormat == null) {
            binding.edMobile.setError(getString(R.string.envalid_number));
            return false;
        }

        return true;
    }


    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            binding.sendLay.setVisibility(View.GONE);
            binding.verifyLay.setVisibility(View.VISIBLE);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                binding.edOtp1.setText("" + code.charAt(0));
                binding.edOtp2.setText("" + code.charAt(1));
                binding.edOtp3.setText("" + code.charAt(2));
                binding.edOtp4.setText("" + code.charAt(3));
                binding.edOtp5.setText("" + code.charAt(4));
                binding.edOtp6.setText("" + code.charAt(5));
                verifyOtp(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
        }
    };

    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                saveUserToDatabase(null, null, null, phoneNumber, null);
            } else {
                Toast.makeText(OtpActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(String firstName, String lastName, String email, String phone, String password) {
        String nationalFormat = PhoneNumberUtils.toNationalFormat(phone, "TR");
        if (nationalFormat != null) {
            phone = nationalFormat.replace(" ", "");
        }
        UserProfile userProfile = new UserProfile(firstName, lastName, email, phone, password, null);
        ApiClient.ApiService apiService = ApiClient.getClient().create(ApiClient.ApiService.class);
        Call<Void> call = apiService.createUserProfileOnPhone(userProfile);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Utils.getProfileData(OtpActivity.this, userProfile);
                    Toast.makeText(OtpActivity.this, "User profile created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OtpActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Utils.reDirectMainActivity(OtpActivity.this);
            }
        });
    }



}
