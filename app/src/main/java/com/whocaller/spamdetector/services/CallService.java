package com.whocaller.spamdetector.services;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.telecom.InCallService;

import androidx.core.app.NotificationCompat;

import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.CallActivity;
import com.whocaller.spamdetector.helpers.CallManager;
import com.whocaller.spamdetector.helpers.NotificationHelper;


public class CallService extends InCallService {

    public static final String END = "end";

    private CallManager callManager;

    @Override
    public void onCallAdded(Call call) {
        String str;
        super.onCallAdded(call);

        CallManager.inCallService = this;

        if (call != null) {
            try {
                str = call.getDetails().getHandle().getSchemeSpecificPart();
            } catch (NullPointerException unused) {
                str = getString(R.string.unknown);
            }
            CallManager.num = str;
            CallManager.handler = new Handler();
            CallManager callManager = new CallManager(this);
            this.callManager = callManager;
            callManager.setCall(call);
            TelecomAdapter.getInstance().setInCallService(this);


            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("num", str);
            intent.setFlags(268451840);
            intent.putExtra(NotificationCompat.CATEGORY_STATUS, call.getState());
            startActivity(intent);

            // startForeground(ID_NOTIFICATION, this.notification);


            String speakerBtnName, muteBtnName;

            if (CallActivity.isSpeakerOn) {
                speakerBtnName = getString(R.string.speaker_off);
            } else {
                speakerBtnName = getString(R.string.speaker_on);
            }

            if (CallActivity.isMuted) {
                muteBtnName = getString(R.string.unmute);
            } else {
                muteBtnName = getString(R.string.mute);
            }
            NotificationHelper.createIngoingCallNotification(this, call, "12:00:4", speakerBtnName, muteBtnName);

        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        TelecomAdapter.getInstance().clearInCallService();
        if (this.callManager == null) {
            this.callManager = new CallManager(this);
        }
        this.callManager.setCall(null);
        CallManager.num = null;
        CallManager.handler = null;
        CallManager.time = 0;
        stopForeground(true);
    }

    @Override
    public void onConnectionEvent(Call call, String str, Bundle bundle) {
        super.onConnectionEvent(call, str, bundle);
    }


    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        String action;
        if (!(intent == null || this.callManager == null || (action = intent.getAction()) == null || !action.equals(END))) {
            this.callManager.hangup();
        }
        return super.onStartCommand(intent, i, i2);
    }


}
