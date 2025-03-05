/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;



import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;

import com.naliya.callerid.AndroPlaza;
import com.naliya.callerid.AppCompatActivity;
import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.naliya.callerid.database.prefs.SettingsPrefHelper;
import com.whocaller.spamdetector.BuildConfig;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.databinding.ActivitySplashBinding;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Permission;
import com.whocaller.spamdetector.utils.PhoneNumberUtils;
import com.whocaller.spamdetector.utils.Utils;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements AndroPlaza.GetApiDatas {

    private FirebaseAuth mAuth;
    SettingsPrefHelper sharedPref;
    AndroPlaza androPlaza;
    ProfilePrefHelper profilePrefHelper;
    private boolean isDataProcessVisible = true;
    private boolean isDataUseVisible = true;
    private boolean isEmpowerVisible = true;
    private boolean isPermissionVisible = true;
    private static final int DEFAULT_DIALER_REQUEST_ID = 83;

    ActivitySplashBinding binding;

    private static final int PERMISSIONS = 100;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance();
        sharedPref = new SettingsPrefHelper(this);
        profilePrefHelper = new ProfilePrefHelper(this);
        androPlaza = new AndroPlaza(this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


/*
        if (sharedPref.getApp_permission() && Permission.checkPermissions(this)) {
            binding.permissions.setVisibility(View.GONE);
            checklogin();
        } else {
            binding.permissions.setVisibility(View.VISIBLE);
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
            binding.btnAgree.setOnClickListener(v -> {
                binding.btnAgreeTxt.setVisibility(View.INVISIBLE);
                binding.btnAgreeProg.setVisibility(View.VISIBLE);
                permission();
            });
        }
*/

        if (sharedPref.getApp_permission() && Permission.checkPermissions(this)) {
            Log.d("DefaultDialer", "Permission if ");
            binding.permissions.setVisibility(View.GONE);
            requestDefaultDialerRole();
        } else {
            Log.d("DefaultDialer", "Permission else " + sharedPref.getApp_permission());
            binding.permissions.setVisibility(View.VISIBLE);
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
            binding.btnAgree.setOnClickListener(v -> {
                binding.btnAgreeTxt.setVisibility(View.INVISIBLE);
                binding.btnAgreeProg.setVisibility(View.VISIBLE);
                requestDefaultDialerRole();
            });
        }



        binding.togglePermission.setOnClickListener(v -> {
            if (isPermissionVisible) {
                binding.contentPermission.setVisibility(View.GONE);
            } else {
                binding.contentPermission.setVisibility(View.VISIBLE);
            }
            isPermissionVisible = !isPermissionVisible;
        });

        binding.toggleDataProcess.setOnClickListener(v -> {
            if (isDataProcessVisible) {
                binding.contentDataProcess.setVisibility(View.GONE);
            } else {
                binding.contentDataProcess.setVisibility(View.VISIBLE);
            }
            isDataProcessVisible = !isDataProcessVisible;
        });

        binding.toggleDataUse.setOnClickListener(v -> {
            if (isDataUseVisible) {
                binding.contentDataUse.setVisibility(View.GONE);
            } else {
                binding.contentDataUse.setVisibility(View.VISIBLE);
            }
            isDataUseVisible = !isDataUseVisible;
        });

        binding.toggleEmpower.setOnClickListener(v -> {
            if (isEmpowerVisible) {
                binding.contentEmpower.setVisibility(View.GONE);
            } else {
                binding.contentEmpower.setVisibility(View.VISIBLE);
            }
            isEmpowerVisible = !isEmpowerVisible;
        });

        binding.privacyLink.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(getResources().getString(R.string.privacy_policy_link)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        binding.termsLink.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(getResources().getString(R.string.terms_conditions_link)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

    }

    @Override
    protected Activity getactivity() {
        return this;
    }

    private void permission() {
        Permission.requestPermissions(SplashActivity.this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permission.handlePermissionResult(requestCode, permissions, grantResults, new Permission.PermissionResultCallback() {
            @Override
            public void onPermissionsGranted() {
                sharedPref.setApp_permission(true);
                checklogin();
            }

            @Override
            public void onPermissionsDenied() {
                Toast.makeText(SplashActivity.this, "Permissions are required for this app to function properly", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void checklogin() {
        if (mAuth.getCurrentUser() != null && androPlaza.IsFirst()) {
            profilePrefHelper.setIsSignUser(true);
            String email = mAuth.getCurrentUser().getEmail();
            String phone = mAuth.getCurrentUser().getPhoneNumber();

            String nationalFormat = PhoneNumberUtils.toNationalFormat(phone, "TR");
            if (nationalFormat != null) {
                phone = nationalFormat.replace(" ", "");

            }

            if (Utils.isNetworkAvailable(this)) {
                Utils.getSettingsData(this);
                UserProfile userProfile = new UserProfile(null, null, email, phone, null, null);
                Utils.getProfileData(SplashActivity.this, userProfile);
            } else {
                if (sharedPref.getApp_status() && profilePrefHelper.IsSignUser()) {
                    Utils.reDirectMainActivity(this);
                } else {
                    binding.btnAgreeTxt.setVisibility(View.VISIBLE);
                    binding.btnAgreeProg.setVisibility(View.GONE);
                    Utils.showToast(this, getString(R.string.check_internet));
                }

            }


        } else {
            if (Utils.isNetworkAvailable(this) && androPlaza.IsFirst()) {
                Utils.getSettingsData(this);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else if (androPlaza.IsFirst()) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

        }

    }



    @Override
    public void onValidationSuccess(String message) {
        if (Utils.isNetworkAvailable(this) && androPlaza.IsFirst()) {
            Utils.getSettingsData(this);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else if (androPlaza.IsFirst()) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
    }

    @Override
    public void onValidationError(String errorMessage, String title) {
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_DIALER_REQUEST_ID) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Please set this app as default whocaller app.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
                Log.d("DefaultDialer", "requestCode if2");
            } else {
                sharedPref.setApp_permission(true);
                Log.d("DefaultDialer", "requestCode else");
                if (Permission.checkPermissions(this)) {
                    checklogin();
                } else {
                    Permission.requestPermissions(this);
                }
            }
        } else {
            Log.d("DefaultDialer", "requestCode else2");
        }
    }

    public void requestDefaultDialerRole() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
            startActivityForResult(intent, DEFAULT_DIALER_REQUEST_ID);
            Log.d("DefaultDialer", "requestDefaultDialerRole if");
        } else {
            Log.d("DefaultDialer", "requestDefaultDialerRole else");// Below Android 10
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (telecomManager != null && !telecomManager.getDefaultDialerPackage().equals(getPackageName())) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, DEFAULT_DIALER_REQUEST_ID);
                Log.d("DefaultDialer", "requestDefaultDialerRole if2");
            } else {
                sharedPref.setApp_permission(true);
                Log.d("DefaultDialer", "requestDefaultDialerRole else2");
                if (Permission.checkPermissions(this)) {
                    checklogin();
                } else {
                    Permission.requestPermissions(this);
                }

            }
        }

    }



    private void takePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    SplashActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSIONS
            );
        } else {
            askSetAsDefault();
        }
    }



    public boolean isDefaultDialer() {
        try {
            if (!getPackageName().startsWith(BuildConfig.APPLICATION_ID)) {
                return true;
            } else if (getPackageName().startsWith(BuildConfig.APPLICATION_ID) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
                return roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && roleManager.isRoleHeld(RoleManager.ROLE_DIALER);
            } else {
                TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
                return isMarshmallowPlus() && telecomManager.getDefaultDialerPackage().equals(getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isMarshmallowPlus() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void askSetAsDefault() {
        if (!checkForIsDefaultDialer()) {
            showMainActivity();
        }
    }

    private boolean checkForIsDefaultDialer() {
        if (!isDefaultDialer()) {
            launchSetDefaultDialerIntent();
            return true;
        }
        return false;
    }


    private void launchSetDefaultDialerIntent() {
        if (isQPlusDevice()) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
            someActivityResultLauncher.launch(intent);
        } else {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            try {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    someActivityResultLauncher.launch(intent);
                } else {
                    Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showMainActivity() {
        // Intent to launch MainActivity or any action needed
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }

    public boolean isQPlusDevice() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private final ActivityResultLauncher<Intent> someActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> showMainActivity());
}

