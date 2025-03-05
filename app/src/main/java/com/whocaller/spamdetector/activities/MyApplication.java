/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;


import com.naliya.callerid.Application;
import com.whocaller.spamdetector.BuildConfig;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.ads.Ads;
import com.onesignal.OneSignal;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initNotification();
        Ads ads = new Ads(this);
        ads.init(this);
    }

    @Override
    public String setProducCode() {
        return Config.API_KEY;
    }

    @Override
    public String setApplicationID() {
        return BuildConfig.APPLICATION_ID;
    }

    public void initNotification() {
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.onesignal_app_id));
        OneSignal.promptForPushNotifications();
    }


}