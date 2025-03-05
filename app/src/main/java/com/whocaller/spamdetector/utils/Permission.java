/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.utils;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Permission {
    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE
    };

    public static boolean checkPermissions(Context context) {
        if (context == null) {
            Log.e("PermissionHelper", "Context is null");
            return false;
        }
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Activity activity) {
        if (activity == null) {
            Log.e("PermissionHelper", "Activity is null, cannot request permissions");
            return;
        }

        if (checkPermissions(activity)) {
            Log.d("PermissionHelper", "All permissions already granted");
            return;
        }

        Log.d("PermissionHelper", "Requesting permissions");

        executorService.execute(() -> mainHandler.post(() -> ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)));

    }

    public static void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionResultCallback callback) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }

        if (callback == null) {
            Log.e("PermissionHelper", "Callback is null");
            return;
        }

        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            callback.onPermissionsGranted();
        } else {
            callback.onPermissionsDenied();
        }
    }

    public interface PermissionResultCallback {
        void onPermissionsGranted();

        void onPermissionsDenied();
    }
}
