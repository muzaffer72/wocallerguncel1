package com.whocaller.spamdetector.helpers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.telecom.VideoProfile;
import android.util.Log;
import android.widget.Toast;

import com.whocaller.spamdetector.R;

/**
 * Mevcut çağrıyı tek bir static alanda tutan ve
 * mute / speaker vb. işlevleri barındıran CallManager.
 * "hasOngoingCall()" metodu eklendi; NotificationHelper için önemlidir.
 * - Aktif arama sırasında gelen aramaları yönetme desteği eklendi
 */
public class CallManager {
    private static final String TAG = "CallManager";

    public static final String ACTION_CALL = "action_call";
    public static final String ACTION_TIME = "action_time";

    // Tek bir aktif çağrıyı tutuyoruz
    private static Call call;
    public static Handler handler;

    public static String num;
    public static int status;
    public static int time;

    public AudioManager am;
    private final Context context;

    public static int HP_CALL_STATE = 0;
    public static InCallService inCallService; // Dışarıdan atayabileceğiniz InCallService referansı

    private boolean hold = false;

    // Ses yönetimi için kontrol değişkenleri
    public static boolean isMuted = false;
    public static boolean isSpeakerOn = false;

    // ---- CALLBACK ----
    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call2, int newState) {
            super.onStateChanged(call2, newState);
            HP_CALL_STATE = call.getState();

            // Arama state'i değişince broadcast
            Intent intent = new Intent(CallManager.ACTION_CALL);
            intent.putExtra("data", newState);
            CallManager.this.context.sendBroadcast(intent);

            // Konuşma bittiğinde vs. time sayacını durdur
            if (newState == Call.STATE_DISCONNECTED) {
                if (CallManager.handler != null) {
                    CallManager.handler.removeCallbacks(runnable);
                }
            }

            // NotificationHelper'a durum değişimini bildir
            NotificationHelper.handleCallStateChange(context, call);
        }
    };

    // ---- RUNNABLE (Süre sayacı) ----
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (CallManager.handler != null) {
                CallManager.handler.postDelayed(this, 1000);
                String timeStr = getTime(CallManager.time);
                Intent intent = new Intent(CallManager.ACTION_TIME);
                intent.putExtra("time", timeStr);
                CallManager.this.context.sendBroadcast(intent);
                CallManager.time++;
            }
        }
    };

    public static String getTime(int i) {
        int minutes = i / 60;
        int seconds = i % 60;
        StringBuilder sb = new StringBuilder();
        if (minutes < 10) sb.append("0");
        sb.append(minutes);
        sb.append(":");
        if (seconds < 10) sb.append("0");
        sb.append(seconds);
        return sb.toString();
    }

    // ---- CONSTRUCTOR ----
    public CallManager(Context context) {
        this.context = context.getApplicationContext();
        this.am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    // ---- SET / GET CALL ----
    public final void setCall(Call call2) {
        // Eski çağrı callback'ini kaldır
        if (call != null) {
            call.unregisterCallback(callback);
            this.hold = false;
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
        }

        // Yeni çağrı ayarla
        if (call2 != null) {
            call2.registerCallback(callback);
            status = call2.getState();

            // Numara bilgisini güncelle
            if (call2.getDetails() != null && call2.getDetails().getHandle() != null) {
                num = call2.getDetails().getHandle().getSchemeSpecificPart();
                Log.d(TAG, "Call number set: " + num);
            }
        }
        call = call2;

        // Callback'ler ile durum değişimini bildiriyoruz
        if (call2 != null) {
            // Mevcut durumu raporla
            NotificationHelper.handleCallStateChange(context, call2);
        }
    }

    public Call getCall() {
        return call;
    }

    // ---- hasOngoingCall() EKLENDİ ----
    public boolean hasOngoingCall() {
        Call c = getCall();
        if (c == null) return false;

        int st = c.getState();
        // Bir çağrıyı "devam ediyor" sayacağımız durumlar
        return (st == Call.STATE_ACTIVE
                || st == Call.STATE_DIALING
                || st == Call.STATE_CONNECTING
                || st == Call.STATE_RINGING
                || st == Call.STATE_HOLDING);
    }

    // ---- ARAMAYI YANITLA vs. ---
    public void answer() {
        Call c = call;
        if (c != null) {
            c.answer(VideoProfile.STATE_AUDIO_ONLY);
            Log.d(TAG, "Call answered");
        } else {
            Log.e(TAG, "Cannot answer call - call is null");
        }
    }

    public void hangup() {
        if (call != null) {
            call.disconnect();
            Log.d(TAG, "Call hung up");
        } else {
            Log.e(TAG, "Cannot hang up call - call is null");
        }
    }

    // ---- HOLD / UNHOLD ÖRNEKLERİ ----
    public void holdAndPlay() {
        if (call != null) {
            if (!this.hold) {
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                }
                call.hold();
                this.hold = true;
                Log.d(TAG, "Call put on hold");
            } else {
                // unhold
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                }
                this.hold = false;
                call.unhold();
                Log.d(TAG, "Call resumed from hold");
            }
        } else {
            Log.e(TAG, "Cannot hold/unhold call - call is null");
        }
    }

    public boolean isHold() {
        return this.hold;
    }

    // ---- STATİK METOTLAR (ActionReceiver vb. kullanıyordu) ----
    public static void playDtmfTone(Call mCall, char c) {
        if (mCall != null) {
            mCall.playDtmfTone(c);
            mCall.stopDtmfTone();
            Log.d(TAG, "DTMF tone played: " + c);
        } else {
            Log.e(TAG, "Cannot play DTMF tone - call is null");
        }
    }

    public static void holdCall(Call mCall) {
        if (mCall != null) {
            mCall.hold();
            Log.d(TAG, "Call put on hold");
        } else {
            Log.e(TAG, "Cannot hold call - call is null");
        }
    }

    public static void unholdCall(Call mCall) {
        if (mCall != null) {
            mCall.unhold();
            Log.d(TAG, "Call resumed from hold");
        } else {
            Log.e(TAG, "Cannot unhold call - call is null");
        }
    }

    public static void hangUpCall(Call mCall) {
        if (mCall != null) {
            mCall.disconnect();
            Log.d(TAG, "Call hung up");
        } else {
            Log.e(TAG, "Cannot hang up call - call is null");
        }
    }

    public static void answerCall(Call mCall) {
        if (mCall != null) {
            mCall.answer(VideoProfile.STATE_AUDIO_ONLY);
            Log.d(TAG, "Call answered");
        } else {
            Log.e(TAG, "Cannot answer call - call is null");
        }
    }

    // ---- MUTE & SPEAKER AYARLARI ----
    public static void muteCall(boolean wantMuted) {
        if (inCallService != null) {
            // Mevcut durumu değiştir
            isMuted = !wantMuted;
            inCallService.setMuted(wantMuted);
            if (wantMuted) {
                Toast.makeText(inCallService, R.string.call_muted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(inCallService, R.string.call_unmuted, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Call mute set to: " + wantMuted);
        } else {
            Log.e(TAG, "Cannot mute call - inCallService is null");
        }
    }

    public static void speakerCall(boolean wantSpeakerOn) {
        if (inCallService != null) {
            // Mevcut durumu değiştir
            isSpeakerOn = !wantSpeakerOn;
            if (wantSpeakerOn) {
                inCallService.setAudioRoute(CallAudioState.ROUTE_SPEAKER);
                Toast.makeText(inCallService, R.string.speaker_on, Toast.LENGTH_SHORT).show();
            } else {
                inCallService.setAudioRoute(CallAudioState.ROUTE_EARPIECE);
                Toast.makeText(inCallService, R.string.speaker_off, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Call speaker set to: " + wantSpeakerOn);
        } else {
            Log.e(TAG, "Cannot change speaker mode - inCallService is null");
        }
    }
}