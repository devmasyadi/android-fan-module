package com.adsmanager.fanmodule

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.adsmanager.fan.*

class MainActivity : AppCompatActivity() {

    private lateinit var fanAds: FanAds
    private val bannerId = "1363711600744576_1363713000744436"
    private val interstitialId = "1363711600744576_1508878896227845"
    private val nativeId = "1363711600744576_1508877312894670"
    private val nativeSmallId = "1363711600744576_1508905206225214"
    private val rewardsId = "1363711600744576_1508879032894498"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fanAds = FanAds()
        fanAds.initialize(
            activity = this,
            testDevices = listOf("a9e0b1db-61ee-4a3e-8440-134595340d73"),
            iInitialize = object : IInitialize {
                override fun onInitializationComplete() {
                    fanAds.loadInterstitial(this@MainActivity, interstitialId)
                    fanAds.loadRewards(this@MainActivity, rewardsId)
                    fanAds.loadGdpr(this@MainActivity, true)
                }
            })

        findViewById<Button>(R.id.btnShowBanner).setOnClickListener {
            val bannerView = findViewById<RelativeLayout>(R.id.bannerView)
            fanAds.showBanner(
                this,
                bannerView,
                SizeBanner.SMALL,
                bannerId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e("HALLO", "banner error: $error")
                    }
                })
        }

        findViewById<Button>(R.id.btnShowInterstitial).setOnClickListener {
            fanAds.showInterstitial(this, interstitialId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {
                    Log.e("HALLO", "interstitial error: $error")
                }
            })
        }

        findViewById<Button>(R.id.btnShowRewards).setOnClickListener {
            fanAds.showRewards(this, rewardsId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {
                    Log.e("HALLO", "rewards error: $error")
                }
            }, object : IRewards {
                override fun onUserEarnedReward(rewardsItem: RewardsItem?) {
                }
            })
        }

        findViewById<Button>(R.id.btnSmallNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            fanAds.showNativeAds(
                this,
                nativeView,
                SizeNative.SMALL,
                nativeSmallId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e("HALLO", "native error: $error")
                    }
                })
        }

        findViewById<Button>(R.id.btnShowMediumNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            fanAds.showNativeAds(
                this,
                nativeView,
                SizeNative.MEDIUM,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e("HALLO", "native error: $error")
                    }
                })
        }

    }


}