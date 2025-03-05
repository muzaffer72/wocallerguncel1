package com.whocaller.spamdetector.services;

import android.content.Context;
import android.media.AudioManager;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telecom.InCallService;

public class TelecomAdapter {
    private static TelecomAdapter mInstance;
    private InCallService inCallService;

    public static TelecomAdapter getInstance() {
        if (Looper.getMainLooper().isCurrentThread()) {
            if (mInstance == null) {
                mInstance = new TelecomAdapter();
            }
            return mInstance;
        }
        throw new IllegalStateException();
    }

    public void setInCallService(InCallService inCallService) {
        this.inCallService = inCallService;
    }

    // Yeni Getter metodunu ekliyoruz
    public InCallService getInCallService() {
        return this.inCallService;
    }

    public void switchSpeaker(AudioManager audioManager) {
        try {
            if (!audioManager.isSpeakerphoneOn()) {
                setAudioRoute(8);
            } else {
                setAudioRoute(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudioRoute(int i) {
        InCallService inCallService = this.inCallService;
        if (inCallService != null) {
            inCallService.setAudioRoute(i);
        }
    }

    public void muteSpeaker(AudioManager audioManager) {
        audioManager.setMode(2);
        boolean isMicrophoneMute = audioManager.isMicrophoneMute();
        InCallService inCallService = this.inCallService;
        if (inCallService != null) {
            inCallService.setMuted(!isMicrophoneMute);
        }
    }

    // Çağrıyı sonlandırma işlemi TelecomManager ile yapılır
    public void endCall(Context context) {
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            telecomManager.endCall();
        }
    }

    public void clearInCallService() {
        this.inCallService = null;
    }
}
