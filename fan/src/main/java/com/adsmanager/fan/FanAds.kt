package com.adsmanager.fan

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.ads.*


class FanAds : IAds {
    override fun initialize(
        activity: Activity,
        iInitialize: IInitialize,
        testDevices: List<String>?
    ) {
        AudienceNetworkAds.initialize(activity)
        AdSettings.addTestDevices(testDevices)
        iInitialize.onInitializationComplete()
    }

    override fun loadGdpr(activity: Activity, childDirected: Boolean) {

    }

    override fun showBanner(
        activity: Activity,
        bannerView: RelativeLayout,
        sizeBanner: SizeBanner,
        adUnitId: String,
        callbackAds: CallbackAds?
    ) {

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.
        val adSize = when (sizeBanner) {
            SizeBanner.SMALL -> AdSize.BANNER_HEIGHT_50
            SizeBanner.MEDIUM -> AdSize.RECTANGLE_HEIGHT_250
        }
        val adView = AdView(activity, adUnitId, adSize)
        bannerView.removeAllViews()
        bannerView.addView(adView)
        val adListener = object : AdListener {
            override fun onError(ad: Ad?, error: AdError?) {
                callbackAds?.onAdFailedToLoad("ad: ${ad?.isAdInvalidated}, error: ${error?.errorMessage}")
            }

            override fun onAdLoaded(p0: Ad?) {
                callbackAds?.onAdLoaded()
            }

            override fun onAdClicked(p0: Ad?) {

            }

            override fun onLoggingImpression(p0: Ad?) {

            }

        }
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())
    }

    private var interstitialAd: InterstitialAd? = null

    private var interstitialCallbackAds: CallbackAds? = null
    override fun loadInterstitial(activity: Activity, adUnitId: String) {
        // Instantiate an InterstitialAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        interstitialAd = InterstitialAd(activity, adUnitId)
        // Create listeners for the Interstitial Ad
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
            }

            override fun onError(ad: Ad, adError: AdError) {
                interstitialCallbackAds?.onAdFailedToLoad("ad: ${ad.isAdInvalidated}, error: ${adError.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                interstitialCallbackAds?.onAdLoaded()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
            }
        }

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAd?.loadAd(
            interstitialAd!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )
    }

    override fun showInterstitial(activity: Activity, adUnitId: String, callbackAds: CallbackAds?) {
        interstitialCallbackAds = callbackAds
        // Check if interstitialAd has been loaded successfully
        if (interstitialAd?.isAdLoaded == false) {
            loadInterstitial(activity, adUnitId)
            callbackAds?.onAdFailedToLoad("Interstitial not ready")
            return
        }
        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
        if (interstitialAd?.isAdInvalidated == true) {
            loadInterstitial(activity, adUnitId)
            callbackAds?.onAdFailedToLoad("Interstitial not ready")
            return
        }
        // Show the ad
        interstitialAd?.show()
        loadInterstitial(activity, adUnitId)
        callbackAds?.onAdLoaded()
    }


    override fun showNativeAds(
        activity: Activity,
        nativeView: RelativeLayout,
        sizeNative: SizeNative,
        adUnitId: String,
        callbackAds: CallbackAds?
    ) {
        when (sizeNative) {
            SizeNative.SMALL -> showSmallNative(activity, adUnitId, callbackAds, nativeView)
            SizeNative.MEDIUM -> showMediumNative(activity, adUnitId, callbackAds, nativeView)
        }
    }

    private fun showSmallNative(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        nativeView: RelativeLayout
    ) {
        val nativeBannerAd = NativeBannerAd(activity, adUnitId)
        val nativeAdListener = object : NativeAdListener {
            override fun onError(ad: Ad?, error: AdError?) {
                callbackAds?.onAdFailedToLoad("ad: ${ad?.isAdInvalidated}, error: ${error?.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad) {
                callbackAds?.onAdLoaded()
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd != ad) {
                    return
                }
                // Inflate Native Banner Ad into Container
                inflateAdSmall(nativeBannerAd, activity, nativeView)
            }

            override fun onAdClicked(p0: Ad?) {

            }

            override fun onLoggingImpression(p0: Ad?) {

            }

            override fun onMediaDownloaded(p0: Ad?) {

            }

        }

        // load the ad
        nativeBannerAd.loadAd(
            nativeBannerAd.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build()
        )

    }

    private fun showMediumNative(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        nativeView: RelativeLayout
    ) {
        val nativeAd = NativeAd(activity, adUnitId)
        val nativeAdListener = object : NativeAdListener {
            override fun onError(ad: Ad?, error: AdError?) {
                callbackAds?.onAdFailedToLoad("ad: $ad, error: ${error?.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad?) {
                callbackAds?.onAdLoaded()
                if (nativeAd != ad) {
                    return
                }
                inflateAdMedium(nativeAd, activity, nativeView)
            }

            override fun onAdClicked(p0: Ad?) {

            }

            override fun onLoggingImpression(p0: Ad?) {

            }

            override fun onMediaDownloaded(p0: Ad?) {

            }
        }
        // Request an ad
        nativeAd.loadAd(
            nativeAd.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build()
        )
    }

    private var rewardedVideoAd: RewardedVideoAd? = null
    override fun loadRewards(activity: Activity, adUnitId: String) {
        // Instantiate a RewardedVideoAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get
        // a no fill error).
        rewardedVideoAd = RewardedVideoAd(activity, adUnitId)
        rewardedVideoAd?.loadAd(
            rewardedVideoAd!!.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener)
                .build()
        )
    }

    private var iRewards: IRewards? = null

    override fun showRewards(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        iRewards: IRewards?
    ) {
        this.iRewards = iRewards
        // Check if rewardedVideoAd has been loaded successfully
        if (rewardedVideoAd == null || rewardedVideoAd?.isAdLoaded == false) {
            loadRewards(activity, adUnitId)
            callbackAds?.onAdFailedToLoad("Rewards not ready")
            return
        }
        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
        if (rewardedVideoAd?.isAdInvalidated == true) {
            loadRewards(activity, adUnitId)
            callbackAds?.onAdFailedToLoad("Rewards not ready")
            return
        }
        rewardedVideoAd?.show()
        loadRewards(activity, adUnitId)
    }

    private val rewardedVideoAdListener = object : RewardedVideoAdListener {
        override fun onError(ad: Ad, error: AdError) {
            // Rewarded video ad failed to load
        }

        override fun onAdLoaded(ad: Ad) {
            // Rewarded video ad is loaded and ready to be displayed
        }

        override fun onAdClicked(ad: Ad) {
            // Rewarded video ad clicked
        }

        override fun onLoggingImpression(ad: Ad) {
            // Rewarded Video ad impression - the event will fire when the
            // video starts playing
        }

        override fun onRewardedVideoCompleted() {
            // Rewarded Video View Complete - the video has been played to the end.
            // You can use this event to initialize your reward

            // Call method to give reward
            // giveReward();
            iRewards?.onUserEarnedReward(null)
        }

        override fun onRewardedVideoClosed() {
            // The Rewarded Video ad was closed - this can occur during the video
            // by closing the app, or closing the end card.
        }
    }

    fun inflateAdSmall(
        nativeBannerAd: NativeBannerAd,
        activity: Activity?,
        layNative: RelativeLayout
    ) {
        try {
            nativeBannerAd.unregisterView()
            val nativeAdLayout = NativeAdLayout(activity, null, 1)
            val inflater = LayoutInflater.from(activity)
            val adView = inflater.inflate(
                R.layout.fan_small_native,
                nativeAdLayout,
                false
            ) as LinearLayout
            layNative.removeAllViews()
            layNative.addView(adView)
            val adChoicesContainer: RelativeLayout = adView.findViewById(R.id.ad_choices_container)
            val adOptionsView = AdOptionsView(
                activity,
                nativeBannerAd,
                nativeAdLayout
            )
            adChoicesContainer.removeAllViews()
            adChoicesContainer.addView(adOptionsView, 0)
            val nativeAdTitle: TextView = adView.findViewById(R.id.native_ad_title)
            val nativeAdSocialContext: TextView = adView.findViewById(R.id.native_ad_social_context)
//            val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
            val nativeAdIconView: MediaView = adView.findViewById(R.id.native_icon_view)
            val nativeAdCallToAction: Button = adView.findViewById(R.id.native_ad_call_to_action)
            nativeAdCallToAction.text = nativeBannerAd.adCallToAction
            nativeAdCallToAction.visibility =
                if (nativeBannerAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
            nativeAdTitle.text = nativeBannerAd.advertiserName
            nativeAdSocialContext.text = nativeBannerAd.adSocialContext
//            sponsoredLabel.text = nativeBannerAd.sponsoredTranslation
            val clickableViews: MutableList<View> = java.util.ArrayList()
            clickableViews.add(nativeAdTitle)
            clickableViews.add(nativeAdCallToAction)
            nativeBannerAd.registerViewForInteraction(
                adView,
                nativeAdIconView,
                clickableViews
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun inflateAdMedium(nativeAdFan: NativeAd, activity: Activity?, layNative: RelativeLayout) {
        try {
            nativeAdFan.unregisterView()
            val nativeAdLayout =
                NativeAdLayout(activity, null, 1)
            val inflater = LayoutInflater.from(activity)
            val adView = inflater.inflate(
                R.layout.fan_medium_native,
                nativeAdLayout,
                false
            ) as LinearLayout
            layNative.removeAllViews()
            layNative.addView(adView)
            val adChoicesContainer: LinearLayout = adView.findViewById(R.id.ad_choices_container)
            val adOptionsView = AdOptionsView(
                activity,
                nativeAdFan,
                nativeAdLayout
            )
            adChoicesContainer.removeAllViews()
            adChoicesContainer.addView(adOptionsView, 0)
            val nativeAdIcon: MediaView = adView.findViewById(R.id.native_ad_icon)
            val nativeAdTitle: TextView = adView.findViewById(R.id.native_ad_title)
            val nativeAdMedia: MediaView = adView.findViewById(R.id.native_ad_media)
            val nativeAdSocialContext: TextView = adView.findViewById(R.id.native_ad_social_context)
            val nativeAdBody: TextView = adView.findViewById(R.id.native_ad_body)
            val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
            val nativeAdCallToAction: Button = adView.findViewById(R.id.native_ad_call_to_action)
            nativeAdTitle.text = nativeAdFan.advertiserName
            nativeAdBody.text = nativeAdFan.adBodyText
            nativeAdSocialContext.text = nativeAdFan.adSocialContext
            nativeAdCallToAction.visibility =
                if (nativeAdFan.hasCallToAction()) View.VISIBLE else View.INVISIBLE
            nativeAdCallToAction.text = nativeAdFan.adCallToAction
            sponsoredLabel.text = nativeAdFan.sponsoredTranslation
            val clickableViews: MutableList<View> = ArrayList()
            clickableViews.add(nativeAdTitle)
            clickableViews.add(nativeAdCallToAction)
            nativeAdFan.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}