/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.api.ApiClient;
import com.whocaller.spamdetector.databinding.ActivityMyAccountBinding;
import com.whocaller.spamdetector.modal.UserProfile;
import com.whocaller.spamdetector.utils.Utils;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri filePath;
    private ActivityMyAccountBinding binding;
    ProfilePrefHelper profilePrefHelper;
    private String TAG = "MyAccountActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAccountBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ApiClient.ApiService apiService = ApiClient.getClient().create(ApiClient.ApiService.class);
        profilePrefHelper = new ProfilePrefHelper(MyAccountActivity.this);
        String imgurl = profilePrefHelper.getImage();
        setDetails();


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(MyAccountActivity.this, ProfileActivity.class));
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);



        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(MyAccountActivity.this, ProfileActivity.class));
            finish();
        });

        if (Utils.isValidURL(imgurl)) {
            Glide.with(getApplicationContext())
                    .load(imgurl)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .into(binding.profilePic);

            Glide.with(getApplicationContext())  // or use getContext()
                    .load(imgurl)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .into(binding.profilePic);
        } else {
            Glide.with(getApplicationContext())
                    .load(Config.BASE_URL + "/storage/app/public/" + imgurl)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .into(binding.profilePic);
        }


        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri dataUri = result.getData().getData();
                        if (dataUri != null) {
                            filePath = dataUri;
                            startCrop(filePath);
                        }
                    }
                });
        binding.addPhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MyAccountActivity.this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MyAccountActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                showFileChooser();
            }
        });

        binding.saveChanges.setOnClickListener(v -> {
            if (Utils.isNetworkAvailable(MyAccountActivity.this)) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.saveText.setVisibility(View.GONE);
                binding.saveChanges.setEnabled(false);

                String firstName = Objects.requireNonNull(binding.firstName.getText()).toString();
                String lastName = Objects.requireNonNull(binding.lastName.getText()).toString();
                String phone = Objects.requireNonNull(binding.phoneNumber.getText()).toString();
                String email = Objects.requireNonNull(binding.email.getText()).toString();

                if (filePath != null) {
                    uploadImage(filePath, new ImageUploadCallback() {
                        @Override
                        public void onImageUploaded(String imageUrl) {
                            UserProfile updatedUserProfile = new UserProfile(firstName, lastName, email, phone, null, imageUrl);
                            saveProfile(apiService, updatedUserProfile);
                        }

                        @Override
                        public void onUploadFailed(String error) {
                            Toast.makeText(MyAccountActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                            afterSave();
                        }
                    });
                } else {
                    UserProfile updatedUserProfile = new UserProfile(firstName, lastName, email, phone, null, imgurl);
                    saveProfile(apiService, updatedUserProfile);
                }
            } else {
                Utils.showToast(MyAccountActivity.this, getResources().getString(R.string.check_internet));
            }

        });
    }

    private void setDetails() {
        binding.firstName.setText(profilePrefHelper.getFirstName());
        binding.lastName.setText(profilePrefHelper.getLastName());
        binding.email.setText(profilePrefHelper.getEmail());

        if (Utils.isValidPhoneNumber(profilePrefHelper.getPhone())) {
            binding.phoneNumber.setEnabled(false);
            binding.phoneNumber.setText(profilePrefHelper.getPhone());
            binding.phoneNumber.setTextColor(getResources().getColor(R.color.edittext_hint_color, null));
        }

    }


    private void afterSave() {
        binding.progressBar.setVisibility(View.GONE);
        binding.saveText.setVisibility(View.VISIBLE);
        binding.saveChanges.setEnabled(true);
    }


    private void saveProfile(ApiClient.ApiService apiService, UserProfile updatedUserProfile) {
        if (Utils.isNetworkAvailable(this)) {
            Call<Void> updateCall = apiService.updateProfile(updatedUserProfile);
            updateCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {

                        String img = updatedUserProfile.getImgUrl().replace("public/", "");

                        profilePrefHelper.saveUserProfile(updatedUserProfile.getFirstName(), updatedUserProfile.getLastName(),
                                updatedUserProfile.getPhone(), updatedUserProfile.getEmail(), img);
                        Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setDetails();
                        Log.d(TAG, "Profile updated successfully");
                        afterSave();
                    } else {
                        Toast.makeText(getApplicationContext(), "Profile update failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Profile update failed");
                        afterSave();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), "Update request failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update request failed", t);
                    afterSave();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
            afterSave();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            startCrop(filePath);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                filePath = resultUri;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    binding.profilePic.setImageBitmap(bitmap); // Set the cropped image to ImageView
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "CroppedImage.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.start(this);
    }

    private void uploadImage(Uri fileUri, ImageUploadCallback callback) {
        if (fileUri != null) {
            String mimeType = getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            File file = new File(getPath(fileUri));
            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));

            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            ApiClient.ApiService apiService = ApiClient.getClient().create(ApiClient.ApiService.class);
            Call<ResponseBody> call = apiService.uploadImage(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String imagePath = jsonObject.optString("path");

                            if (!TextUtils.isEmpty(imagePath)) {
                                callback.onImageUploaded(imagePath);
                            } else {
                                callback.onUploadFailed("Empty image path received from server");
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            callback.onUploadFailed("Error parsing response: " + e.getMessage());
                        }
                    } else {
                        callback.onUploadFailed("Upload Failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onUploadFailed("Upload Request Failed: " + t.getMessage());
                }
            });
        } else {
            callback.onUploadFailed("File Uri is null");
        }
    }


    private String getPath(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (path == null) {
            path = uri.getPath();
        }

        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFileChooser();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface ImageUploadCallback {
        void onImageUploaded(String imageUrl);

        void onUploadFailed(String error);
    }

}
