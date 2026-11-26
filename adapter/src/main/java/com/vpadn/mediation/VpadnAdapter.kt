package com.vpadn.mediation

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.VersionInfo
import com.google.android.gms.ads.mediation.Adapter
import com.google.android.gms.ads.mediation.InitializationCompleteCallback
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationBannerAd
import com.google.android.gms.ads.mediation.MediationBannerAdCallback
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration
import com.google.android.gms.ads.mediation.MediationConfiguration
import com.google.android.gms.ads.mediation.MediationInterstitialAd
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration
import com.google.android.gms.ads.mediation.MediationNativeAdCallback
import com.google.android.gms.ads.mediation.MediationNativeAdConfiguration
import com.google.android.gms.ads.mediation.NativeAdMapper
import com.vpon.ads.RequestConfiguration
import com.vpon.ads.VponAdListener
import com.vpon.ads.VponAdLoader
import com.vpon.ads.VponAdRequest
import com.vpon.ads.VponAdSize
import com.vpon.ads.VponBanner
import com.vpon.ads.VponFullScreenContentCallback
import com.vpon.ads.VponInterstitialAd
import com.vpon.ads.VponInterstitialAdLoadCallback
import com.vpon.ads.VponMobileAds
import com.vpon.ads.VponNativeAd
import java.lang.ref.WeakReference

class VpadnAdapter : Adapter(), MediationBannerAd, MediationInterstitialAd {

    companion object {
        private const val ADAPTER_VERSION: String = BuildConfig.VERSION_NAME
        private const val LT:String = "VpadnAdapter(${ADAPTER_VERSION})"
        private const val AD_UNIT_KEY: String = "pubid" // "ad_unit";
        private const val NATIVE_AD_UNIT_KEY: String = "parameter" // "ad_unit";
        private const val VPON_DOMAIN: String = "com.vpon"
        const val AD_CONTENT_URL: String = "contentURL"
        const val AD_CONTENT_DATA: String = "contentData"
        const val LOCATION: String = "location"
        const val KEY_ICON: String = "_vpon_icon"
        val ADMOB_SIZE_BANNER: AdSize = AdSize(320, 50)

        // GMA creates a new adapter instance for every ad load, so the registrations must live
        // at class level. Apps call VpadnAdapter.getVponObstruction().addViews(licenseKey, views).
        private val vponObstruction: VponObstruction = VponObstruction()

        @JvmStatic
        fun getVponObstruction(): VponObstruction = vponObstruction
    }

    override fun loadNativeAdMapper(
        configuration: MediationNativeAdConfiguration,
        adLoadCallback: MediationAdLoadCallback<NativeAdMapper, MediationNativeAdCallback>
    ) {
        val adUnitId = configuration.serverParameters.getString(NATIVE_AD_UNIT_KEY)
        Log.d(LT, "VponNativeAd.licenseKey :$adUnitId")
        adUnitId?.let {
            val vponAdLoaderBuilder = VponAdLoader.Builder(configuration.context, it)
            val vponAdRequest = buildVponAdRequestByCriteria(
                it, configuration.mediationExtras, configuration.taggedForChildDirectedTreatment()
            )
            var mediationNativeAdCallback: MediationNativeAdCallback? = null
            vponAdLoaderBuilder.forNativeAd(object : VponNativeAd.OnNativeAdLoadedListener() {
                override fun onNativeAdLoaded(nativeAd: VponNativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    nativeAd?.let { vponNativeAd ->
                        var hashMap: HashMap<String?, String?>?
                        var vponUnifiedNativeAdMapper: VponNativeAdMapper?
                        vponNativeAd.icon?.let { icon ->
                            hashMap = HashMap()
                            hashMap?.let { map ->
                                map[VponNativeAdMapper.KEY_ICON] = icon.url
                                VponAdLoader.DownloadImagesTask(object :
                                    VponAdLoader.ImageDownloadListener {
                                    override fun onDownloadSuccess(drawableMap: HashMap<String?, Drawable?>) {
                                        drawableMap[KEY_ICON]?.let { drawable ->
                                            vponUnifiedNativeAdMapper =
                                                VponNativeAdMapper(
                                                    configuration.context,
                                                    vponNativeAd,
                                                    VponNativeAdMapper
                                                        .VponNativeMappingImage(
                                                            drawable, Uri.parse(icon.url)
                                                        )
                                                )
                                            val handler = Handler(configuration.context.mainLooper)
                                            handler.post {
                                                mediationNativeAdCallback =
                                                    adLoadCallback.onSuccess(vponUnifiedNativeAdMapper!!)
                                            }
                                        }
                                    }

                                    override fun onDownloadFailed() {
                                        vponUnifiedNativeAdMapper =
                                            VponNativeAdMapper(
                                                configuration.context, vponNativeAd, null
                                            )
                                        val handler = Handler(configuration.context.mainLooper)
                                        handler.post {
                                            mediationNativeAdCallback =
                                                adLoadCallback.onSuccess(vponUnifiedNativeAdMapper!!)
                                        }
                                    }
                                }).execute(map)
                            }
                        } ?: run {
                            // no icon scenario
                            vponUnifiedNativeAdMapper =
                                VponNativeAdMapper(
                                    configuration.context, vponNativeAd, null
                                )
                            val handler = Handler(configuration.context.mainLooper)
                            handler.post {
                                mediationNativeAdCallback =
                                    adLoadCallback.onSuccess(vponUnifiedNativeAdMapper!!)
                            }
                        }
                    } ?: run {

                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            adLoadCallback.onFailure(
                                AdError(
                                    VponAdRequest.VponErrorCode.INTERNAL_ERROR.errorCode
                                    , VponAdRequest.VponErrorCode.INTERNAL_ERROR.errorDescription!!, VPON_DOMAIN
                                )
                            )
                        }
                    }
                }
            })
                .withAdListener(object : VponAdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationNativeAdCallback?.onAdClosed()
                        }
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        super.onAdFailedToLoad(errorCode)
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            adLoadCallback.onFailure(
                                AdError(
                                    mapVponErrorCodeToAdMopErrorCode(
                                        errorCode
                                    ), "onAdFailedToLoad", VPON_DOMAIN
                                )
                            )
                        }
                    }

                    override fun onAdLeftApplication() {
                        super.onAdLeftApplication()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationNativeAdCallback?.onAdLeftApplication()
                        }
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationNativeAdCallback?.onAdOpened()
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationNativeAdCallback?.reportAdClicked()
                        }
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationNativeAdCallback?.reportAdImpression()
                        }
                    }
                }).build().loadAd(vponAdRequest)
        } ?: run {
            val handler = Handler(configuration.context.mainLooper)
            handler.post {
                adLoadCallback.onFailure(
                    AdError(
                        AdRequest.ERROR_CODE_INVALID_REQUEST,
                        "vpon license key null", VPON_DOMAIN
                    )
                )
            }
        }
    }

    override fun loadInterstitialAd(
        configuration: MediationInterstitialAdConfiguration,
        adLoadCallback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
    ) {
        val adUnitId = configuration.serverParameters.getString(AD_UNIT_KEY)
        adUnitId?.let {
            Log.d(LT, "VponInterstitialAd.licenseKey :$adUnitId")
            val vponAdRequest = buildVponAdRequestByCriteria(
                it, configuration.mediationExtras, configuration.taggedForChildDirectedTreatment()
            )
            VponInterstitialAd.load(configuration.context, it, vponAdRequest, object :
                VponInterstitialAdLoadCallback() {

                lateinit var mediationInterstitialAdCallback: MediationInterstitialAdCallback

                override fun onAdFailedToLoad(adError: VponAdRequest.VponErrorCode) {
                    val handler = Handler(configuration.context.mainLooper)
                    handler.post {
                        adLoadCallback.onFailure(
                            AdError(
                                mapVponErrorCodeToAdMopErrorCode(
                                    adError.errorCode
                                ), adError.errorDescription!!, VPON_DOMAIN
                            )
                        )
                    }
                }

                override fun onAdLoaded(ad: VponInterstitialAd) {
                    Log.d(LT, "onAdLoaded invoked!!")
                    interstitialAd = ad
                    interstitialAd.apply {
                        fullScreenContentCallback = object : VponFullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                                val handler = Handler(configuration.context.mainLooper)
                                handler.post {
                                    mediationInterstitialAdCallback.reportAdClicked()
                                }
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                val handler = Handler(configuration.context.mainLooper)
                                handler.post {
                                    mediationInterstitialAdCallback.onAdClosed()
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(errorCode: Int) {
                                super.onAdFailedToShowFullScreenContent(errorCode)
                                val handler = Handler(configuration.context.mainLooper)
                                handler.post {
                                    // The ad already reported onSuccess at load time, so a show
                                    // failure must go to the show callback, not adLoadCallback.
                                    mediationInterstitialAdCallback.onAdFailedToShow(
                                        AdError(
                                            errorCode,
                                            mapFullScreenErrorCodeReason(errorCode),
                                            VPON_DOMAIN
                                        )
                                    )
                                }
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                val handler = Handler(configuration.context.mainLooper)
                                handler.post {
                                    mediationInterstitialAdCallback.reportAdImpression()
                                }
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                val handler = Handler(configuration.context.mainLooper)
                                handler.post {
                                    mediationInterstitialAdCallback.onAdOpened()
                                }
                            }
                        }
                    }
                    val handler = Handler(configuration.context.mainLooper)
                    handler.post {
                        mediationInterstitialAdCallback =
                            adLoadCallback.onSuccess(this@VpadnAdapter)
                    }
                }
            })
        } ?: run {
            val handler = Handler(configuration.context.mainLooper)
            handler.post {
                adLoadCallback.onFailure(
                    AdError(
                        AdRequest.ERROR_CODE_INVALID_REQUEST,
                        "vpon license key null", VPON_DOMAIN
                    )
                )
            }
        }
    }

    override fun loadBannerAd(
        configuration: MediationBannerAdConfiguration,
        adLoadCallback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
    ) {
        val adUnitId = configuration.serverParameters.getString(AD_UNIT_KEY)
        adUnitId?.let {
            Log.d(LT, "VponBanner.licenseKey :$adUnitId")
            val vponAdSize = getVponAdSizeByAdSize(configuration.adSize, configuration.context)
            val vponAdRequest = buildVponAdRequestByCriteria(
                it, configuration.mediationExtras, configuration.taggedForChildDirectedTreatment()
            )
            vponBanner = VponBanner(WeakReference(configuration.context).get()!!)
            vponBanner.apply {
                licenseKey = adUnitId
                adSize = vponAdSize
                setAdListener(object : VponAdListener() {

                    private lateinit var mediationBannerAdCallback: MediationBannerAdCallback

                    override fun onAdClosed() {
                        super.onAdClosed()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback.onAdClosed()
                        }
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        super.onAdFailedToLoad(errorCode)
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            adLoadCallback.onFailure(
                                AdError(
                                    mapVponErrorCodeToAdMopErrorCode(
                                        errorCode
                                    ), "onAdFailedToLoad", VPON_DOMAIN
                                )
                            )
                        }
                    }

                    override fun onAdLeftApplication() {
                        super.onAdLeftApplication()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback.onAdLeftApplication()
                        }
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback.onAdOpened()
                        }
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback = adLoadCallback.onSuccess(this@VpadnAdapter)
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback.reportAdClicked()
                        }
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        val handler = Handler(configuration.context.mainLooper)
                        handler.post {
                            mediationBannerAdCallback.reportAdImpression()
                        }
                    }
                })
            }.loadAd(vponAdRequest)

        } ?: run {
            // no ad unit id
            val handler = Handler(configuration.context.mainLooper)
            handler.post {
                adLoadCallback.onFailure(
                    AdError(
                        AdRequest.ERROR_CODE_INVALID_REQUEST,
                        "vpon license key null", VPON_DOMAIN
                    )
                )
            }
        }
    }

    override fun getSDKVersionInfo(): VersionInfo {
        Log.d(LT, "Adapter.getSDKVersionInfo invoked!!")
        return parseVersion(VponAdRequest.VERSION)
    }

    override fun getVersionInfo(): VersionInfo {
        Log.d(LT, "Adapter.getVersionInfo invoked!!")
        return parseVersion(ADAPTER_VERSION)
    }

    private fun parseVersion(versionString: String): VersionInfo {
        // The Vpon SDK version name has a leading "v" (e.g. "v5.8.0"); without stripping it the
        // major version parsed as 0 and GMA saw the Vpon SDK as 0.8.0.
        val versions = versionString.trim().trimStart('v', 'V')
            .split("\\.".toRegex()).filter { it.isNotEmpty() }
        return if (versions.size >= 3) {
            val primaryVersion = versions[0].toIntOrNull() ?: 0
            val minorVersion = versions[1].toIntOrNull() ?: 0
            val serialVersion = versions[2].toIntOrNull() ?: 0
            VersionInfo(primaryVersion, minorVersion, serialVersion)
        } else {
            VersionInfo(0, 0, 0)
        }
    }

    private fun getVponAdSizeByAdSize(adSize: AdSize, context: Context): VponAdSize {
        // Legacy GMA hands smart banners to adapters as FULL_WIDTH(-1) x AUTO_HEIGHT(-2), which Vpon
        // cannot fill. Resolve them to the actual size in dp first.
        // AdSize.SMART_BANNER is deliberately not referenced: GMA Next-Gen has no such field
        // (NoSuchFieldError), and the AdSize given to adapters never equals it anyway.
        if (adSize.width < 0 || adSize.height < 0) {
            val density = context.resources.displayMetrics.density
            val width = (adSize.getWidthInPixels(context) / density).toInt()
            val height = (adSize.getHeightInPixels(context) / density).toInt()
            if (width > 0 && height > 0) {
                return getVponAdSizeByAdSize(AdSize(width, height), context)
            }
        }
        return when (adSize) {
            AdSize.BANNER, ADMOB_SIZE_BANNER -> VponAdSize.BANNER
            AdSize.FULL_BANNER -> VponAdSize.IAB_BANNER
            AdSize.MEDIUM_RECTANGLE -> VponAdSize.IAB_MRECT
            AdSize.LEADERBOARD -> VponAdSize.IAB_LEADERBOARD
            AdSize.LARGE_BANNER -> VponAdSize.LARGE_BANNER
            else -> {
                val networkId = VponMobileAds.getRequestConfiguration().networkId
                if (networkId == RequestConfiguration.NETWORK_ID_SKM) {
                    VponAdSize(adSize.width, adSize.height)
                } else {
                    getMappedSize(adSize)
                }
            }
        }
    }

    private val sizeMappings = listOf(
        Triple(728, 90, VponAdSize.IAB_LEADERBOARD),
        Triple(468, 60, VponAdSize.IAB_BANNER),
        Triple(320, 480, VponAdSize.LARGE_RECTANGLE),
        Triple(300, 250, VponAdSize.IAB_MRECT),
        Triple(320, 100, VponAdSize.LARGE_BANNER),
        Triple(320, 50, VponAdSize.BANNER)
    )

    private fun getMappedSize(adSize: AdSize): VponAdSize {
        val width = adSize.width
        val height = adSize.height
        Log.d(LT, "getMappedSize invoked with size: ${width}x$height")

        return sizeMappings.firstOrNull { (w, h, _) -> width >= w && height >= h }
            ?.third
            ?: VponAdSize(width, height)
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private fun buildVponAdRequestByCriteria(
        licenseKey: String,
        mediationExtras: Bundle,
        taggedForChildDirectedTreatment: Int
    ): VponAdRequest {
        return VponAdRequest.Builder().apply {
            setAutoRefresh(false)
            mediationExtras.getString(AD_CONTENT_URL)?.let { setContentUrl(it) }
            (mediationExtras.get(AD_CONTENT_DATA) as? HashMap<String, Any>)?.let { setContentData(it) }
            (mediationExtras.get(LOCATION) as? Location)?.let { setLocation(it) }
            vponObstruction.getViewsByLicenseKey(licenseKey)
                ?.forEach { (obstructView, purpose, description) ->
                    addFriendlyObstruction(obstructView, purpose, description)
                }
            tagForChildDirectedTreatment(taggedForChildDirectedTreatment)
        }.build()
    }

    private fun mapVponErrorCodeToAdMopErrorCode(vponErrorCode: Int): Int {
        return when (vponErrorCode) {
            VponAdRequest.VponErrorCode.NO_FILL.errorCode -> {
                AdRequest.ERROR_CODE_MEDIATION_NO_FILL
            }

            VponAdRequest.VponErrorCode.NETWORK_ERROR.errorCode -> {
                AdRequest.ERROR_CODE_NETWORK_ERROR
            }

            VponAdRequest.VponErrorCode.INTERNAL_ERROR.errorCode -> {
                AdRequest.ERROR_CODE_INTERNAL_ERROR
            }

            VponAdRequest.VponErrorCode.INVALID_REQUEST.errorCode -> {
                AdRequest.ERROR_CODE_INVALID_REQUEST
            }

            else -> AdRequest.ERROR_CODE_INTERNAL_ERROR
        }
    }

    private fun mapFullScreenErrorCodeReason(vponErrorCode: Int): String {
        when (vponErrorCode) {
            VponFullScreenContentCallback.ERROR_CODE_INTERNAL_ERROR -> return "INTERNAL_ERROR"
            VponFullScreenContentCallback.ERROR_CODE_AD_REUSED -> return "AD_REUSED"
            VponFullScreenContentCallback.ERROR_CODE_NOT_READY -> return "NOT_READY"
            VponFullScreenContentCallback.ERROR_CODE_APP_NOT_FOREGROUND -> return "APP_NOT_FOREGROUND"
            VponFullScreenContentCallback.ERROR_CODE_MEDIATION_SHOW_ERROR -> return "MEDIATION_SHOW_ERROR"
        }
        return "INTERNAL_ERROR"
    }

    override fun initialize(
        context: Context,
        initializationCompleteCallback: InitializationCompleteCallback,
        mediationConfiguration: MutableList<MediationConfiguration>
    ) {
        Log.d(LT, "Adapter.initialize invoked!!")
        // GMA waits for this callback (up to its timeout) before reporting
        // initialization complete, so it must always be called.
        // VponMobileAds.initialize() is synchronous.
        try {
            VponMobileAds.initialize(context)
            initializationCompleteCallback.onInitializationSucceeded()
        } catch (t: Throwable) {
            initializationCompleteCallback.onInitializationFailed("Vpon SDK initialization failed: ${t.message}")
        }
    }

    private lateinit var vponBanner: VponBanner

    override fun getView(): View {
        Log.d(LT, "MediationBannerAd.getView invoked!!")
        return vponBanner
    }

    private lateinit var interstitialAd: VponInterstitialAd

    override fun showAd(p0: Context) {
        Log.d(LT, "MediationInterstitialAd.showAd invoked!!")
        interstitialAd.show()
    }
}