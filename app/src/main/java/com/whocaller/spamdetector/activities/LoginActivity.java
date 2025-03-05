/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.naliya.callerid.AppCompatActivity;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.api.ApiClient;
import com.whocaller.spamdetector.databinding.ActivityLoginBinding;
import com.whocaller.spamdetector.modal.ErrorResponse;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Utils;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ActivityLoginBinding binding;

    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        configureGoogleClient();


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        if (mAuth.getCurrentUser() != null) {
            Utils.reDirectMainActivity(LoginActivity.this);
        }

        binding.btnLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.edPassword.getText()).toString().trim();

            if (Utils.isNetworkAvailable(LoginActivity.this)) {
                if (validation()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.loginText.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(false);
                    loginUser(email, password);
                }
            } else {
                Utils.showToast(LoginActivity.this, getResources().getString(R.string.check_internet));

            }

        });

        binding.txtForgotpassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        binding.btnSign.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.btnRegisterPhone.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, OtpActivity.class)));


        binding.btnRegisterGoogle.setOnClickListener(v -> {
            if (Utils.isNetworkAvailable(LoginActivity.this)) {
                signInToGoogle();
                binding.googleLay.setVisibility(View.GONE);
                binding.googleProgressBar.setVisibility(View.VISIBLE);
            } else {
                Utils.showToast(LoginActivity.this, getResources().getString(R.string.check_internet));
            }

        });


    }

    @Override
    protected Activity getactivity() {
        return this;
    }




    public boolean validation() {
        if (binding.edEmail.getText().toString().isEmpty()) {
            binding.edEmail.setError(getString(R.string.evalisemail));
            return false;
        }
        if (binding.edPassword.getText().toString().isEmpty()) {
            binding.edPassword.setError(getString(R.string.epassword));
            return false;
        }
        return true;
    }


    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        UserProfile userProfile = new UserProfile(null, null, email, null, null, null);
                        Utils.getProfileData(LoginActivity.this, userProfile);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.loginText.setVisibility(View.VISIBLE);
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void configureGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void signInToGoogle() {
        startforresultt.launch(new Intent(googleSignInClient.getSignInIntent()));
    }

    ActivityResultLauncher<Intent> startforresultt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                        Log.e("LoginActivity", Objects.requireNonNull(account.getDisplayName()));
                    } catch (ApiException e) {
                        Log.w("LoginActivity", "Google sign in failed", e);
                        binding.googleLay.setVisibility(View.VISIBLE);
                        binding.googleProgressBar.setVisibility(View.GONE);

                    }
                } else {
                    Log.w("LoginActivity", "Google sign in failed");
                    binding.googleLay.setVisibility(View.VISIBLE);
                    binding.googleProgressBar.setVisibility(View.GONE);
                }
            }
    );

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("LOGIN ACTIVITY", "signInWithCredential:success: currentUser: " + user.getEmail());
                        String email = user.getEmail();
                        String name = user.getDisplayName();
                        String profile = String.valueOf(user.getPhotoUrl());

                        Log.e("LOGIN ACTIVITY", "img url: " + profile);

                        UserProfile userProfile = new UserProfile(name, null, email, null, null, profile);
                        registerWithEmail(LoginActivity.this, userProfile);
                    } else {
                        Log.w("LOGIN ACTIVITY", "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        binding.googleLay.setVisibility(View.VISIBLE);
                        binding.googleProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void registerWithEmail(Context context, UserProfile userProfile) {
        ApiClient.ApiService apiService = ApiClient.getClient().create(ApiClient.ApiService.class);
        Call<Void> call = apiService.createUserProfileOnGoogle(userProfile);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Utils.getProfileData(LoginActivity.this, userProfile);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorBody, ErrorResponse.class);
                        Log.e("LoginActivity", "Error Message: " + errorResponse.getMessage());
                        Log.e("LoginActivity", "Error Details: " + errorResponse.getError());
                        if (errorResponse.getError().equals("The email has already been taken.")) {
                            Utils.getProfileData(LoginActivity.this, userProfile);
                        } else {
                            binding.googleLay.setVisibility(View.VISIBLE);
                            binding.googleProgressBar.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

                        Utils.getProfileData(LoginActivity.this, userProfile);
                    }


                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                binding.googleLay.setVisibility(View.VISIBLE);
                binding.googleProgressBar.setVisibility(View.GONE);
                if (mAuth.getCurrentUser() != null) {
                    //mAuth.signOut();
                    Utils.getProfileData(LoginActivity.this, userProfile);
                }
            }
        });
    }
}
