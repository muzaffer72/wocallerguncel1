package com.whocaller.spamdetector.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.CustomDialogActivity;

/**
 * Bildirimden gelen aksiyon isteklerini işleyen BroadcastReceiver.
 * - Silencing zil sesi düzeltildi
 * - Beklet ve yanıtla seçeneği eklendi
 * - Arama yapma desteği iyileştirildi
 */
public class ActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ActionReceiver onReceive: " + intent.toString());

        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Log.d(TAG, "Intent extra: " + key + " = " + intent.getExtras().get(key));
            }
        }

        // CallManager'ı başlat
        CallManager callManager = new CallManager(context);
        Call currentCall = callManager.getCall();

        try {
            if (intent.hasExtra("endCall")) {
                Log.d(TAG, "endCall action received");
                NotificationHelper.stopRingtoneAndNotification(context); // Eklenen satır
                if (currentCall != null) {
                    CallManager.hangUpCall(currentCall);
                    Toast.makeText(context, R.string.call_ended, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Cannot end call - call is null");
                    Toast.makeText(context, "Aktif çağrı bulunamadı", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.hasExtra("speakerCall")) {
                Log.d(TAG, "speakerCall action received");
                // Hoparlör durumunu değiştir
                CallManager.speakerCall(!CallManager.isSpeakerOn);

            } else if (intent.hasExtra("muteMic")) {
                Log.d(TAG, "muteMic action received");
                // Mikrofon durumunu değiştir
                CallManager.muteCall(!CallManager.isMuted);

            } else if (intent.hasExtra("silenceRing")) {
                Log.d(TAG, "silenceRing action received");
                // Zil sesini kapat
                NotificationHelper.silenceRingtone(context);
                Toast.makeText(context, R.string.ringtone_silenced, Toast.LENGTH_SHORT).show();

            } else if (intent.hasExtra("dismissMissedCall")) {
                Log.d(TAG, "dismissMissedCall action received");
                // Cevapsız çağrı bildirimini temizle
                NotificationHelper.clearMissedCallNotification(context);

            } else if (intent.hasExtra("makeCall")) {
                // Çağrı yap
                String phoneNumber = intent.getStringExtra("makeCall");
                Log.d(TAG, "makeCall action received for number: " + phoneNumber);

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    try {
                        // Utils sınıfındaki arama fonksiyonunu kullan
                        // Eğer bu çalışmazsa, android.intent.action.CALL kullanarak da yapabilirsiniz
                        com.whocaller.spamdetector.utils.Utils.makeCall(phoneNumber, context);
                    } catch (Exception e) {
                        Log.e(TAG, "Error making call: " + e.getMessage(), e);
                        Toast.makeText(context, "Arama yapılamadı: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Cannot make call - phone number is empty");
                    Toast.makeText(context, "Aranacak numara bulunamadı", Toast.LENGTH_SHORT).show();
                }
            }
            // YENİ: Aktif çağrı sırasında gelen 2. çağrı için işlemler
            else if (intent.hasExtra("rejectCall")) {
                Log.d(TAG, "rejectCall action received");
                if (currentCall != null) {
                    // İkinci gelen aramayı reddet
                    CallManager.hangUpCall(currentCall);
                    Toast.makeText(context, R.string.call_rejected, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Cannot reject call - call is null");
                    Toast.makeText(context, "Reddedilecek çağrı bulunamadı", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.hasExtra("answerAndHold")) {
                Log.d(TAG, "answerAndHold action received");
                if (currentCall != null) {
                    // Mevcut çağrıyı beklet ve gelen çağrıyı yanıtla
                    try {
                        CallManager.answerCall(currentCall);
                        Toast.makeText(context, R.string.call_answered, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error answering call: " + e.getMessage());
                        Toast.makeText(context, "Çağrı yanıtlanamadı: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Cannot answer call - call is null");
                    Toast.makeText(context, "Yanıtlanacak çağrı bulunamadı", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.hasExtra("viewInfo")) {
                Log.d(TAG, "viewInfo action received");
                // CustomDialog'u aç
                String phoneNumber = intent.getStringExtra("phoneNumber");
                String contactName = intent.getStringExtra("contactName");

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    try {
                        Intent dialogIntent = new Intent(context, CustomDialogActivity.class);
                        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        dialogIntent.putExtra("callerNumber", phoneNumber);
                        dialogIntent.putExtra("callerName", contactName != null ? contactName : "");
                        dialogIntent.putExtra("isMissedCall", true);
                        context.startActivity(dialogIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting CustomDialogActivity: " + e.getMessage());
                        // Tekrar dene
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                Intent dialogIntent = new Intent(context, CustomDialogActivity.class);
                                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                dialogIntent.putExtra("callerNumber", phoneNumber);
                                dialogIntent.putExtra("callerName", contactName != null ? contactName : "");
                                dialogIntent.putExtra("isMissedCall", true);
                                context.startActivity(dialogIntent);
                            } catch (Exception ex) {
                                Log.e(TAG, "Second attempt failed: " + ex.getMessage());
                            }
                        }, 1000);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling action: " + e.getMessage(), e);
            Toast.makeText(context, "İşlem gerçekleştirilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}