/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.modal;


import android.os.Parcel;
import android.os.Parcelable;

public class CallHistory implements Parcelable {
    private String callerName;
    private String phoneNumber;
    private String callType;
    private String callDate;
    private String calllookupKey;

    protected CallHistory(Parcel in) {
        callerName = in.readString();
        phoneNumber = in.readString();
        callType = in.readString();
        callDate = in.readString();
        calllookupKey = in.readString();

    }

    public static final Creator<CallHistory> CREATOR = new Creator<CallHistory>() {
        @Override
        public CallHistory createFromParcel(Parcel in) {
            return new CallHistory(in);
        }

        @Override
        public CallHistory[] newArray(int size) {
            return new CallHistory[size];
        }
    };


    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(callerName);
        dest.writeString(phoneNumber);
        dest.writeString(callType);
        dest.writeString(callDate);
        dest.writeString(calllookupKey);
    }
}




