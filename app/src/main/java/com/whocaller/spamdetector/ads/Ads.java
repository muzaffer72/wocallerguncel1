/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.naliya.callerid.database.prefs.AdsPrefHelper;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.utils.Utils;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Objects;

public class Ads {
    AdsPrefHelper adsPrefHelper;
    NativeAd nativeAdd;
    public AdView adView;
    public static InterstitialAd mInterstitialAd;
    public static boolean InterAddLoad = false;
    public static final String ADMOB = "admob";
    public static final String UNITY = "unity";
    public static final int UNITY_ADS_BANNER_WIDTH_MEDIUM = 320;
    public static final int UNITY_ADS_BANNER_HEIGHT_MEDIUM = 50;

    public Ads(Context context) {
        adsPrefHelper = new AdsPrefHelper(context);
    }

    public void init(Context context) {
        if (adsPrefHelper.getAdStatus()) {
            switch (adsPrefHelper.getMainAds()) {
                case ADMOB:
                    MobileAds.initialize(context, initializationStatus -> {
                    });
                    setInterstitialAd(context);
                    break;
                case UNITY:
                    UnityAds.initialize(context, adsPrefHelper.getUnityGameId(), true, new IUnityAdsInitializationListener() {
                        @Override
                        public void onInitializationComplete() {
                            setInterstitialAd(context);
                        }

                        @Override
                        public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        }
                    });

                    break;

            }
        }
    }

    public void showBannerAd(Activity activity) {
        if (adsPrefHelper.getAdStatus()) {
            switch (adsPrefHelper.getMainAds()) {
                case ADMOB:
                    FrameLayout adContainerView = activity.findViewById(R.id.admob_banner_view_container);
                    adContainerView.post(() -> {
                        adView = new AdView(activity);
                        adView.setAdUnitId(adsPrefHelper.getAdMobBannerId());
                        adContainerView.removeAllViews();
                        adContainerView.addView(adView);
                        adView.setAdSize(Utils.getAdSize(activity));

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adView.loadAd(adRequest);

                        adView.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                adContainerView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                adContainerView.setVisibility(View.GONE);

                            }

                        });
                    });
                    break;
                case UNITY:
                    RelativeLayout unityAdView = activity.findViewById(R.id.unity_banner_view_container);
                    BannerView bottomBanner = new BannerView(activity, adsPrefHelper.getUnityBannerPlacementId(), new UnityBannerSize(UNITY_ADS_BANNER_WIDTH_MEDIUM, UNITY_ADS_BANNER_HEIGHT_MEDIUM));
                    bottomBanner.setListener(new BannerView.IListener() {
                        @Override
                        public void onBannerLoaded(BannerView bannerView) {
                            unityAdView.setVisibility(View.VISIBLE);
                            Log.d("Unity_banner", "ready");
                        }

                        @Override
                        public void onBannerClick(BannerView bannerView) {

                        }

                        @Override
                        public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
                            Log.d("SupportTest", "Banner Error" + bannerErrorInfo);
                            unityAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onBannerLeftApplication(BannerView bannerView) {

                        }
                    });
                    unityAdView.addView(bottomBanner);
                    bottomBanner.load();

                    break;

            }

        }
    }

    public void setInterstitialAd(Context context) {
        if (adsPrefHelper.getAdStatus()) {
            switch (adsPrefHelper.getMainAds()) {
                case ADMOB:
                    AdRequest adRequest = new AdRequest.Builder().build();
                    InterstitialAd.load(context, adsPrefHelper.getAdMobInterstitialId(), adRequest,
                            new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    mInterstitialAd = interstitialAd;
                                    InterAddLoad = true;
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    mInterstitialAd = null;
                                }
                            });
                    break;
                case UNITY:
                    UnityAds.load(adsPrefHelper.getUnityInterstitialPlacementId(), new IUnityAdsLoadListener() {
                        @Override
                        public void onUnityAdsAdLoaded(String placementId) {
                            InterAddLoad = true;
                        }

                        @Override
                        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                        }
                    });
                    break;

            }

        }
    }

    public void showInterstitialAd(Context ctx) {
        if (adsPrefHelper.getAdStatus()) {
            switch (adsPrefHelper.getMainAds()) {
                case ADMOB:
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show((Activity) ctx);
                        InterAddLoad = false;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            setInterstitialAd(ctx);
                        }, 10000);

                    }
                    break;
                case UNITY:
                    UnityAds.show((Activity) ctx, adsPrefHelper.getUnityInterstitialPlacementId(), new IUnityAdsShowListener() {
                        @Override
                        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                        }

                        @Override
                        public void onUnityAdsShowStart(String placementId) {
                        }

                        @Override
                        public void onUnityAdsShowClick(String placementId) {
                        }

                        @Override
                        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                            InterAddLoad = false;
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                setInterstitialAd(ctx);
                            }, 10000);


                        }
                    });
                    break;

            }

        }
    }

    public void loadingNativeAdSmall(Activity activity) {
        if (adsPrefHelper.getAdStatus() && !adsPrefHelper.getAdMobNativeId().isEmpty() && adsPrefHelper.getMainAds().equals("admob")) {
            AdLoader.Builder builder = new AdLoader.Builder(activity, adsPrefHelper.getAdMobNativeId());
            final FrameLayout frameLayout = activity.findViewById(R.id.fl_adplaceholder);

            builder.forNativeAd(unifiedNativeAd -> {
                if (nativeAdd != null) {
                    nativeAdd.destroy();
                }
                nativeAdd = unifiedNativeAd;

                @SuppressLint("InflateParams")
                com.google.android.gms.ads.nativead.NativeAdView adView = (com.google.android.gms.ads.nativead.NativeAdView) activity.getLayoutInflater().inflate(R.layout.admob_native_small, null);

                populateUnifiedNativeAdViewSmall(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);

            });

            NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
            builder.withNativeAdOptions(adOptions);

            AdLoader adLoader = builder.withAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    frameLayout.setVisibility(View.VISIBLE);
                }

            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }

    }

    public void populateUnifiedNativeAdViewSmall(NativeAd nativeAd, com.google.android.gms.ads.nativead.NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));


        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());


        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);

        } else {
            Objects.requireNonNull(Objects.requireNonNull(adView.getBodyView())).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((RoundedImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }


        adView.setNativeAd(nativeAd);


    }


    public void populateUnifiedNativeAdView(NativeAd nativeAd, com.google.android.gms.ads.nativead.NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
        Objects.requireNonNull(adView.getMediaView()).setMediaContent(Objects.requireNonNull(nativeAd.getMediaContent()));


        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);

        } else {
            Objects.requireNonNull(Objects.requireNonNull(adView.getBodyView())).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);


    }

    public void loadingNativeAd(Activity activity) {
        if (adsPrefHelper.getAdStatus() && !adsPrefHelper.getAdMobNativeId().isEmpty() && adsPrefHelper.getMainAds().equals("admob")) {
            AdLoader.Builder builder = new AdLoader.Builder(activity, adsPrefHelper.getAdMobNativeId());
            final FrameLayout frameLayout = activity.findViewById(R.id.fl_adplaceholder);

            builder.forNativeAd(unifiedNativeAd -> {
                if (nativeAdd != null) {
                    nativeAdd.destroy();
                }
                nativeAdd = unifiedNativeAd;

                @SuppressLint("InflateParams")
                com.google.android.gms.ads.nativead.NativeAdView adView = (com.google.android.gms.ads.nativead.NativeAdView) activity.getLayoutInflater().inflate(R.layout.admob_native, null);

                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);

            });

            NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
            builder.withNativeAdOptions(adOptions);

            AdLoader adLoader = builder.withAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    frameLayout.setVisibility(View.VISIBLE);
                }

            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

}
