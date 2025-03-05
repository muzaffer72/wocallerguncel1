/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.whocaller.spamdetector.R;


public class BannerAdView extends LinearLayout {

    private Context mContext;

    public BannerAdView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public BannerAdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public BannerAdView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }


    private void initView() {
        // View view = this;
        inflate(mContext, R.layout.view_banner_ad, this);
    }

}
