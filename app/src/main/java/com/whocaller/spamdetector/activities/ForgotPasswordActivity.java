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
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.databinding.ActivityForgotPasswordBinding;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();


        binding.btnSend.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.edEmail.getText()).toString().trim();

            if (validation()) {
                resetPassword(email);
            }

        });
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean validation() {
        if (Objects.requireNonNull(binding.edEmail.getText()).toString().isEmpty()) {
            binding.edEmail.setError(getString(R.string.evalisemail));
            return false;
        }

        return true;
    }
}
