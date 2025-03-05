package com.whocaller.spamdetector.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.activities.CallActivity;
import com.whocaller.spamdetector.activities.CustomDialogActivity;

/**
 * NotificationHelper -- güncel
 * - İkinci çağrı reddedildiğinde, eğer başka aktif çağrı varsa bildirimi kapatmıyor.
 * - Zil sesini kapatma butonu (silenceRing).
 * - CustomDialog açma vb.
 * - Aktif arama bildirimleri kalıcı yapıldı
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    public static final int NOTIFICATION_ID = 834831;
    public static final int MISSED_CALL_NOTIFICATION_ID = 834832;

    // Kanal ID'leri
    private static final String CHANNEL_ID = "Hidden_Pirates_Phone_App";
    private static final String CHANNEL_NAME = "Incoming Call Notification";
    private static final String MISSED_CALL_CHANNEL_ID = "Hidden_Pirates_Missed_Call";
    private static final String MISSED_CALL_CHANNEL_NAME = "Missed Call Notification";

    // Zil sesi
    private static Ringtone ringtone;
    private static boolean isRingtonePlaying = false;
    private static AudioManager audioManager;
    private static int originalRingerMode = -1;
    private static int originalVolume = -1;

    // Çağrı durumu
    private static boolean isInCall = false;
    private static int lastCallState = -1;
    private static String lastCallerNumber = "";
    private static String lastCallerName = "";
    private static long callStartTime = 0;

    // Zil sesi kim yönetiyor?
    private static boolean isRingtoneHandledByActivity = false;

    // Zil sesinin kapatılıp kapatılmadığını kontrol eder
    private static boolean isRingtoneSilenced = false;

    public static void setRingtoneHandledByActivity(boolean isHandled) {
        isRingtoneHandledByActivity = isHandled;
        Log.d(TAG, "Ringtone is now handled by activity: " + isHandled);
    }

    // ---- createIngoingCallNotification ----
    public static void createIngoingCallNotification(Context context, Call call, String callDuration,
                                                     String speakerBtnTxt, String muteBtnTxt) {
        if (call == null) {
            Log.e(TAG, "createIngoingCallNotification: Call is null");
            return;
        }

        int currentCallState = call.getState();
        Log.d(TAG, "Incoming call state: " + currentCallState);

        if (currentCallState == lastCallState) {
            return;
        }
        lastCallState = currentCallState;

        if (call.getDetails().getHandle() != null) {
            lastCallerNumber = call.getDetails().getHandle().getSchemeSpecificPart();
            lastCallerName = ContactsHelper.getContactNameFromLocal(lastCallerNumber, context);
            if (currentCallState == Call.STATE_RINGING) {
                callStartTime = System.currentTimeMillis();
            }
        }

        // RINGING harici durumda zil çalmayı durdur
        if (currentCallState != Call.STATE_RINGING) {
            if (!isRingtoneHandledByActivity) {
                stopRingtone(context);
            }
            isInCall = (currentCallState == Call.STATE_ACTIVE);

            if (currentCallState == Call.STATE_ACTIVE
                    || currentCallState == Call.STATE_DIALING
                    || currentCallState == Call.STATE_CONNECTING) {
                showCallNotification(context, call, callDuration, speakerBtnTxt, muteBtnTxt, false);
            }
            return;
        }

        // RINGING ise bildirim
        showCallNotification(context, call, callDuration, speakerBtnTxt, muteBtnTxt, true);

        if (isRingtoneHandledByActivity) {
            Log.d(TAG, "Ringtone handled by activity, skipping playback");
            return;
        }

        // İkinci çağrı durumunu kontrol et
        if (isInCall) {
            if (audioManager == null) {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }
            // Aktif çağrı varsa sadece titreşim (zil çalmadan)
            if (audioManager != null) {
                // Ringer modunu sakla ve titreşim moduna geç
                if (originalRingerMode == -1) {
                    originalRingerMode = audioManager.getRingerMode();
                }
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Log.d(TAG, "Active call detected, setting to VIBRATE mode only");
            }
        } else if (!isRingtonePlaying && !isRingtoneSilenced) {
            // Normal arama geldiğinde ve zil kapatılmadıysa çal
            playRingtone(context);
        }
    }

    // ---- createOutgoingNotification ----
    public static void createOutgoingNotification(Context context, Call call) {
        if (call == null) {
            Log.e(TAG, "createOutgoingNotification: Call is null");
            return;
        }
        if (!isRingtoneHandledByActivity) {
            stopRingtone(context);
        }
        showCallNotification(context, call,
                context.getString(R.string.calling),
                context.getString(R.string.speaker),
                context.getString(R.string.mute_call),
                false);
    }

    // ---- showCallNotification ----
    @SuppressLint("NotificationTrampoline")
    private static void showCallNotification(Context context, Call call, String callDuration,
                                             String speakerBtnTxt, String muteBtnTxt, boolean isIncoming) {
        if (call == null) return;
        String callerPhoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
        String callerName = ContactsHelper.getContactNameFromLocal(callerPhoneNumber, context);

        lastCallerNumber = callerPhoneNumber;
        lastCallerName = callerName;

        createNotificationChannelIfNeeded(context);

        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        callIntent.putExtra("num", callerPhoneNumber); // Telefon numarasını açıkça belirt
        callIntent.setPackage(context.getPackageName());
        PendingIntent callPendingIntent = PendingIntent.getActivity(
                context, 0, callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setOngoing(true) // Bildirimi kalıcı yap
                .setPriority(NotificationCompat.PRIORITY_MAX) // En yüksek öncelik
                .setContentIntent(callPendingIntent)
                .setFullScreenIntent(callPendingIntent, true)
                .setSmallIcon(R.drawable.ic_call_green)
                .setContentInfo(callDuration)
                .setContentTitle(callerName)
                .setContentText(callerPhoneNumber)
                .setCategory(Notification.CATEGORY_CALL)
                .setChannelId(CHANNEL_ID)
                .setOnlyAlertOnce(false)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Kilit ekranında göster
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true) // Arama süresi sayacı ekle
                .setAutoCancel(false); // Otomatik kapanmayı engelle

        // Kapatılamaz bildirimi ayarla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_CALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ için bildirim davranışı
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        }

        if (isIncoming || call.getState() == Call.STATE_ACTIVE) {
            // Çağrı bitir
            Intent endCallIntent = new Intent(context, ActionReceiver.class);
            endCallIntent.putExtra("endCall", "YES");
            PendingIntent endCallPendingIntent = PendingIntent.getBroadcast(
                    context, 1, endCallIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(R.drawable.ic_call_end_red,
                    context.getString(R.string.end_call),
                    endCallPendingIntent);

            // Aktif görüşme sırasında gelen aramalar için seçenekler
            if (isIncoming && isInCall) {
                // Aramaları yönetme seçenekleri
                // Reddet
                Intent rejectCallIntent = new Intent(context, ActionReceiver.class);
                rejectCallIntent.putExtra("rejectCall", "YES");
                PendingIntent rejectCallPendingIntent = PendingIntent.getBroadcast(
                        context, 5, rejectCallIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                builder.addAction(R.drawable.ic_call_end_red,
                        context.getString(R.string.reject),
                        rejectCallPendingIntent);

                // Yanıtla ve beklet
                Intent answerAndHoldIntent = new Intent(context, ActionReceiver.class);
                answerAndHoldIntent.putExtra("answerAndHold", "YES");
                PendingIntent answerAndHoldPendingIntent = PendingIntent.getBroadcast(
                        context, 6, answerAndHoldIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                builder.addAction(R.drawable.ic_call_green,
                        context.getString(R.string.answer_and_hold),
                        answerAndHoldPendingIntent);
            } else {
                // Normal arama için butonlar
                // Hoparlör
                Intent speakerCallIntent = new Intent(context, ActionReceiver.class);
                speakerCallIntent.putExtra("speakerCall", "YES");
                PendingIntent speakerCallPendingIntent = PendingIntent.getBroadcast(
                        context, 2, speakerCallIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                builder.addAction(R.drawable.ic_volume_up,
                        context.getString(R.string.speaker),
                        speakerCallPendingIntent);

                // Mikrofon kapatma
                Intent muteCallIntent = new Intent(context, ActionReceiver.class);
                muteCallIntent.putExtra("muteMic", "YES");
                PendingIntent muteCallPendingIntent = PendingIntent.getBroadcast(
                        context, 3, muteCallIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                builder.addAction(R.drawable.ic_volume_up,
                        context.getString(R.string.mute_call),
                        muteCallPendingIntent);
            }

            // Zili kapat
            Intent silenceRingIntent = new Intent(context, ActionReceiver.class);
            silenceRingIntent.putExtra("silenceRing", "YES");
            PendingIntent silenceRingPendingIntent = PendingIntent.getBroadcast(
                    context, 4, silenceRingIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(R.drawable.volume,
                    context.getString(R.string.silence_ringtone),
                    silenceRingPendingIntent);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "POST_NOTIFICATIONS permission not granted");
            return;
        }

        // Bildirimi göster
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notification);

        Log.d(TAG, "Call notification shown - caller: " + callerName + ", number: " + callerPhoneNumber);
    }

    // ---- createNotificationChannelIfNeeded ----
    private static void createNotificationChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID);
        if (existing == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);
            channel.enableVibration(true);
            channel.setBypassDnd(true); // Rahatsız etmeyin modunu bypass et
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        NotificationChannel missedCallChannel = manager.getNotificationChannel(MISSED_CALL_CHANNEL_ID);
        if (missedCallChannel == null) {
            NotificationChannel channel = new NotificationChannel(
                    MISSED_CALL_CHANNEL_ID, MISSED_CALL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(defaultSound, attributes);
            manager.createNotificationChannel(channel);
        }
    }

    // ---- playRingtone ----
    private static void playRingtone(Context context) {
        if (isRingtoneHandledByActivity) {
            return;
        }
        if (isRingtonePlaying && ringtone != null && ringtone.isPlaying()) {
            return;
        }

        try {
            // AudioManager'ı başlat
            if (audioManager == null) {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }

            if (audioManager != null && originalRingerMode == -1) {
                originalRingerMode = audioManager.getRingerMode();
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            }

            if (audioManager != null) {
                // Sessiz modda ise normal moda geçir
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                        audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }

                // Ring sesini maksimum ses seviyesinin %80'ine ayarla
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                int targetVolume = (int) (maxVolume * 0.8);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, targetVolume, 0);
            }

            // Zil sesi URI'sini al
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            // Ringtone nesnesini oluştur
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
            }

            // Ses özelliklerini ayarla
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ringtone != null) {
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            } else if (ringtone != null) {
                ringtone.setStreamType(AudioManager.STREAM_RING);
            }

            // Zil sesini çal
            if (ringtone != null && !ringtone.isPlaying()) {
                ringtone.play();
                isRingtonePlaying = true;
                Log.d(TAG, "Ringtone started playing by NotificationHelper");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone: " + e.getMessage(), e);
        }
    }

    // ---- stopRingtone ----
    private static void stopRingtone(Context context) {
        if (isRingtoneHandledByActivity) {
            isRingtonePlaying = false;
            return;
        }
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                Log.d(TAG, "Ringtone stopped");
            }

            // AudioManager durumunu eski haline getir
            if (audioManager != null && originalRingerMode != -1) {
                audioManager.setRingerMode(originalRingerMode);
                if (originalVolume != -1) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, originalVolume, 0);
                }
                originalRingerMode = -1;
                originalVolume = -1;
            }

            ringtone = null;
            isRingtonePlaying = false;
            isRingtoneSilenced = false; // Zil sesi durdurulduğunda silenced durumunu sıfırla
        } catch (Exception e) {
            Log.e(TAG, "Error stopping ringtone: " + e.getMessage(), e);
        }
    }

    // ---- silenceRingtone ----
    public static void silenceRingtone(Context context) {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                Log.d(TAG, "Ringtone silenced manually");
            }

            // AudioManager'ı kullanarak ses seviyesini sıfıra düşür
            if (audioManager == null) {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }

            if (audioManager != null) {
                // Orijinal değerleri sakla
                if (originalRingerMode == -1) {
                    originalRingerMode = audioManager.getRingerMode();
                    originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                }

                // Titreşim moduna geç
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }

            isRingtonePlaying = false;
            isRingtoneSilenced = true; // Zil sesinin kullanıcı tarafından kapatıldığını belirt
            Log.d(TAG, "Ringtone silenced with AudioManager");
        } catch (Exception e) {
            Log.e(TAG, "Error silencing ringtone: " + e.getMessage(), e);
        }
    }

    // ---- stopRingtoneAndNotification ----
    public static void stopRingtoneAndNotification(Context context) {
        stopRingtone(context);
        clearCallNotification(context);
    }

    // ---- handleCallStateChange ----
    public static void handleCallStateChange(Context context, Call call) {
        // Çağrı yöneticisi oluştur
        CallManager cm = new CallManager(context);

        if (call == null) {
            stopRingtoneAndNotification(context);
            isInCall = false;
            lastCallState = -1;
            return;
        }

        // Çağrı durumu değişikliklerini logla
        int callState = call.getState();
        Log.d(TAG, "Call state changed: " + callState + ", previous state: " + lastCallState);

        if (callState == lastCallState) {
            return;
        }
        lastCallState = callState;

        switch (callState) {
            case Call.STATE_ACTIVE:
                // Aktif çağrıya geçildiğinde zil sesini durdur
                stopRingtone(context);
                isInCall = true;
                break;

            case Call.STATE_DISCONNECTED:
            case Call.STATE_DISCONNECTING:
                // Çağrı sonlandığında zil sesini durdur
                stopRingtone(context);

                // Başka çağrı var mı kontrol et
                if (!cm.hasOngoingCall()) {
                    // Hiçbir çağrı kalmadı => Tamamen kapat
                    stopRingtoneAndNotification(context);
                    isInCall = false;

                    // Cevapsız çağrı kontrolü
                    boolean wasMissedCall = checkForMissedCall(context, call);

                    // Uygun çağrı verilerini sakla
                    final String finalNumber = lastCallerNumber;
                    final String finalName = lastCallerName;

                    // CustomDialog'u başlat - gecikme ekleyerek
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (finalNumber != null && !finalNumber.isEmpty()) {
                            startCustomDialog(context, finalNumber, finalName, wasMissedCall);
                        }
                    }, 1000); // 1 saniye beklet
                } else {
                    // Hala başka çağrı varsa, bu reddedilen / biten çağrı
                    // Bildirimi kapatmıyoruz
                    Log.d(TAG, "Another call is still active => keep notification");
                }
                break;

            case Call.STATE_RINGING:
                // RINGING createIngoingCallNotification ile yönetiliyor
                Log.d(TAG, "Call ringing state detected, playing ringtone");
                if (!isRingtonePlaying && !isRingtoneSilenced && !isInCall) {
                    playRingtone(context);
                }
                break;

            default:
                break;
        }
    }

    // ---- startCustomDialog ----
    private static void startCustomDialog(Context context, String phoneNumber, String contactName, boolean wasMissedCall) {
        try {
            Intent dialogIntent = new Intent(context, CustomDialogActivity.class);
            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            dialogIntent.putExtra("callerNumber", phoneNumber);
            dialogIntent.putExtra("callerName", contactName != null ? contactName : "");

            // Cevapsız çağrı bilgisini ekle
            if (wasMissedCall) {
                dialogIntent.putExtra("isMissedCall", true);
            }

            Log.d(TAG, "Starting CustomDialogActivity for number: " + phoneNumber
                    + ", name: " + (contactName != null ? contactName : "Unknown"));

            context.startActivity(dialogIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting CustomDialogActivity: " + e.getMessage(), e);

            // Başarısız olursa tekrar dene (1 saniye sonra)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Intent dialogIntent = new Intent(context, CustomDialogActivity.class);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    dialogIntent.putExtra("callerNumber", phoneNumber);
                    dialogIntent.putExtra("callerName", contactName != null ? contactName : "");
                    if (wasMissedCall) {
                        dialogIntent.putExtra("isMissedCall", true);
                    }
                    context.startActivity(dialogIntent);
                    Log.d(TAG, "Second attempt to start CustomDialogActivity successful");
                } catch (Exception ex) {
                    Log.e(TAG, "Second attempt failed: " + ex.getMessage(), ex);
                }
            }, 1000);
        }
    }

    // ---- checkForMissedCall ----
    private static boolean checkForMissedCall(Context context, Call call) {
        if (call == null || lastCallerNumber.isEmpty()) {
            return false;
        }

        Log.d(TAG, "Checking for missed call. Start time: " + callStartTime + ", current time: " + System.currentTimeMillis());

        long duration = System.currentTimeMillis() - callStartTime;
        boolean isShortDuration = duration < 10000; // 10 saniyeden kısa süren aramalar

        Log.d(TAG, "Call duration: " + duration + "ms, isInCall: " + isInCall + ", isShortDuration: " + isShortDuration);

        if (!isInCall && isShortDuration) {
            // Cevapsız çağrı bildirimini göster
            showMissedCallNotification(context, lastCallerNumber, lastCallerName);
            Log.d(TAG, "Missed call detected: " + lastCallerName + " (" + lastCallerNumber + ")");

            return true;
        }

        return false;
    }

    // ---- showMissedCallNotification ----
    @SuppressLint("NotificationTrampoline")
    private static void showMissedCallNotification(Context context, String phoneNumber, String contactName) {
        createNotificationChannelIfNeeded(context);

        // CustomDialog açma niyeti
        Intent openDialogIntent = new Intent(context, CustomDialogActivity.class);
        openDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openDialogIntent.putExtra("callerNumber", phoneNumber);
        openDialogIntent.putExtra("callerName", contactName);
        openDialogIntent.putExtra("isMissedCall", true);
        PendingIntent openDialogPendingIntent = PendingIntent.getActivity(
                context, 103, openDialogIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Geri arama niyeti
        Intent callIntent = new Intent(context, ActionReceiver.class);
        callIntent.putExtra("makeCall", phoneNumber);
        callIntent.setPackage(context.getPackageName());
        PendingIntent callPendingIntent = PendingIntent.getBroadcast(
                context, 101, callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Bildirimi kapatma niyeti
        Intent dismissIntent = new Intent(context, ActionReceiver.class);
        dismissIntent.putExtra("dismissMissedCall", "YES");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context, 102, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Bildirimi oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MISSED_CALL_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openDialogPendingIntent) // Dialog açan intent'i ana içerik olarak ayarla
                .setSmallIcon(R.drawable.missing)
                .setContentTitle(context.getString(R.string.missed_call))
                .setContentText(contactName.isEmpty() ? phoneNumber : contactName)
                .setSubText(contactName.isEmpty() ? "" : phoneNumber)
                .setCategory(Notification.CATEGORY_MISSED_CALL)
                .setChannelId(MISSED_CALL_CHANNEL_ID)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Kilit ekranında göster

        // Bildirim eylemlerini ekle
        builder.addAction(R.drawable.ic_call_green,
                context.getString(R.string.call_again),
                callPendingIntent);

        // Bilgi görüntüleme butonu
        builder.addAction(R.drawable.ic_avatar,
                context.getString(R.string.view_info),
                openDialogPendingIntent);

        builder.addAction(R.drawable.ic_close,
                context.getString(R.string.dismiss),
                dismissPendingIntent);

        // İzin kontrolü
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "POST_NOTIFICATIONS permission not granted");
            return;
        }

        // Bildirimi göster
        NotificationManagerCompat.from(context).notify(MISSED_CALL_NOTIFICATION_ID, builder.build());
        Log.d(TAG, "Missed call notification shown for: " + phoneNumber);
    }

    // ---- clearMissedCallNotification ----
    public static void clearMissedCallNotification(Context context) {
        try {
            NotificationManagerCompat.from(context).cancel(MISSED_CALL_NOTIFICATION_ID);
            Log.d(TAG, "Missed call notification cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing missed call notification: " + e.getMessage(), e);
        }
    }

    // ---- clearCallNotification ----
    private static void clearCallNotification(Context context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
            Log.d(TAG, "Call notification cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing call notification: " + e.getMessage(), e);
        }
    }
}