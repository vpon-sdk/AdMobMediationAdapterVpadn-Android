package com.vpadn.mediation

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.google.android.gms.ads.mediation.NativeAdMapper
import com.google.android.gms.ads.nativead.NativeAd
import com.vpon.ads.VponMediaView
import com.vpon.ads.VponNativeAd

class VponNativeAdMapper(
    context: Context,
    private val vponNativeAd: VponNativeAd,
    nativeAdImage: VponNativeMappingImage?
) : NativeAdMapper() {

    init {
        with(vponNativeAd) {
            title?.let { headline = it }
            advertiseLabel?.let { advertiser = it }
            body?.let { setBody(it) }
            callToAction?.let { setCallToAction(it) }
            setHasVideoContent(getMediaContent()?.hasVideoContent() ?: false)
        }

        overrideClickHandling = true
        overrideImpressionRecording = true

        val vponMediaView = VponMediaView(context).apply {
            setNativeAd(vponNativeAd)
        }
        setMediaView(vponMediaView)

        nativeAdImage?.let { icon = it }
    }

    override fun trackViews(
        containerView: View,
        clickableAssetViews: MutableMap<String, View>,
        nonclickableAssetViews: MutableMap<String, View>
    ) {
        super.trackViews(containerView, clickableAssetViews, nonclickableAssetViews)
        vponNativeAd.registerViewForInteraction(containerView)
    }

    class VponNativeMappingImage(private val drawable: Drawable, private val uri: Uri) :
        NativeAd.Image() {
        override fun getScale(): Double = 1.0

        override fun getDrawable(): Drawable = drawable

        override fun getUri(): Uri = uri
    }

    companion object {
        const val KEY_ICON: String = "_vpon_icon"
    }
}
