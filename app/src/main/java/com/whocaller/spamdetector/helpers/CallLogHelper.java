/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.helpers;

import static com.whocaller.spamdetector.utils.Utils.getCallTypeIconResId;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.whocaller.spamdetector.modal.CallLogItem;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CallLogHelper {

    public static List<CallLogItem> getCallLogsForNumber(Context context, String phoneNumber) {
        List<CallLogItem> callLogItems = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        String[] projection = {
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        String selection = CallLog.Calls.NUMBER + " = ?";
        String[] selectionArgs = {phoneNumber};

        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, CallLog.Calls.DATE + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                int callType = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                String callDate = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                String callDuration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                CallLogItem callLogItem = new CallLogItem(
                        name,
                        number,
                        callType,
                        callDate,
                        getCallTypeIconResId(callType),
                        callDuration
                );

                callLogItems.add(callLogItem);
            }
            cursor.close();
        }

        return callLogItems;
    }

    // LiveData entegrasyonu: Arka planda çağrı loglarını çekip LiveData ile döndüren metot.
    public static LiveData<List<CallLogItem>> getCallLogsForNumberLiveData(Context context, String phoneNumber) {
        MutableLiveData<List<CallLogItem>> liveData = new MutableLiveData<>();
        new Thread(() -> {
            List<CallLogItem> callLogItems = getCallLogsForNumber(context, phoneNumber);
            liveData.postValue(callLogItems);
        }).start();
        return liveData;
    }
}
