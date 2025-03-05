/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.modal;

import android.annotation.SuppressLint;
import android.content.Context;

import com.whocaller.spamdetector.R;

import java.util.concurrent.TimeUnit;

public class CallLogItem {
    private String name;
    private String phoneNumber;
    private int callType;
    private String callDate;
    private int callTypeIconResId;
    private String callDuration;


    public CallLogItem(String name, String phoneNumber, int callType, String callDate, int callTypeIconResId, String callDuration) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDate = callDate;
        this.callTypeIconResId = callTypeIconResId;
        this.callDuration = callDuration;

    }


    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getCallType() {
        return callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public int getCallTypeIconResId() {
        return callTypeIconResId;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedCallDuration(Context context) {
        long durationInSeconds = Long.parseLong(callDuration);
        long hours = TimeUnit.SECONDS.toHours(durationInSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = durationInSeconds - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        if (hours > 0) {
            return String.format(context.getString(R.string.call_duration_hr_min_sec), hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format(context.getString(R.string.call_duration_min_sec), minutes, seconds);
        } else {
            return String.format(context.getString(R.string.call_duration_sec), seconds);
        }
    }

}
