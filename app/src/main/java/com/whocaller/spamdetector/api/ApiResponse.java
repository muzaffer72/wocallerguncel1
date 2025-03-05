/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.api;


import com.google.gson.annotations.SerializedName;
import com.whocaller.spamdetector.modal.Ads;
import com.whocaller.spamdetector.modal.Settings;

import java.util.List;

public class ApiResponse {
    @SerializedName("settings")
    private List<Settings> settings;

    @SerializedName("ads")
    private List<Ads> ads;

    public List<Settings> getSettings() {
        return settings;
    }

    public List<Ads> getAds() {
        return ads;
    }
}
