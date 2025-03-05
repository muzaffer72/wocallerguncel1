/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.helpers;

import static com.whocaller.spamdetector.utils.Utils.getCallTypeIconResId;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.modal.CallLogItem;
import com.whocaller.spamdetector.utils.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsHelper {

    public static String getContactNameFromLocal(String phoneNumber, Context context) {
        String contactName = "";

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {

            if (contactName.equals("")) {
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        contactName = cursor.getString(0);
                    }
                    cursor.close();
                }

                if (contactName.equals("")) {
                    String number = "";

                    if (phoneNumber != null) {
                        String nationalFormat = PhoneNumberUtils.toNationalFormat(phoneNumber, "TR");
                        number = Objects.requireNonNullElse(nationalFormat, phoneNumber);
                    }
                    Uri uri2 = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                    String[] projection2 = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                    Cursor cursor2 = context.getContentResolver().query(uri2, projection2, null, null, null);
                    if (cursor2 != null) {
                        if (cursor2.moveToFirst()) {
                            contactName = cursor2.getString(0);
                        }
                        cursor2.close();
                    }

                    if (contactName.equals("")) {
                        contactName = Config.UNKNOWN_CALLER_NAME;
                    }
                }
            }


        } else {
            Toast.makeText(context, "Please grant contacts permission", Toast.LENGTH_SHORT).show();
        }

        return contactName;
    }

    public static class LoadCallLogsTask {

        private Context context;
        private RelativeLayout progressBar;
        private CallLogCallback callback;
        private int incomingCallsCount = 0;
        private int outgoingCallsCount = 0;

        public LoadCallLogsTask(Context context, RelativeLayout progressBar, CallLogCallback callback) {
            this.context = context;
            this.progressBar = progressBar;
            this.callback = callback;
        }

        public void execute() {
            progressBar.setVisibility(View.VISIBLE);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                List<CallLogItem> callLogItems = doInBackground();
                new Handler(Looper.getMainLooper()).post(() -> onPostExecute(callLogItems));
            });
        }

        private List<CallLogItem> doInBackground() {
            List<CallLogItem> callLogItems = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null, null, null,
                    CallLog.Calls.DATE + " DESC");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int callType = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    String callDate = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    String callDuration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                    if (callType == CallLog.Calls.INCOMING_TYPE) {
                        incomingCallsCount++;
                    } else if (callType == CallLog.Calls.OUTGOING_TYPE) {
                        outgoingCallsCount++;
                    }

                    int callTypeIconResId = getCallTypeIconResId(callType);
                    callLogItems.add(new CallLogItem(name, phoneNumber, callType, callDate, callTypeIconResId, callDuration));
                }
                cursor.close();
            }
            return callLogItems;
        }

        private void onPostExecute(List<CallLogItem> callLogItems) {
            progressBar.setVisibility(View.GONE);
            if (callback != null) {
                callback.onCallLogsLoaded(callLogItems, incomingCallsCount, outgoingCallsCount);
            }
        }

    }

    public interface CallLogCallback {
        void onCallLogsLoaded(List<CallLogItem> callLogItems, int incomingCallsCount, int outgoingCallsCount);
    }

}
