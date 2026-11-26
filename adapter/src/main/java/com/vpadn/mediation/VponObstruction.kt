package com.vpadn.mediation

import android.view.View
import com.vpon.ads.VponAdRequest
import com.vpon.pojo.VponObstructView
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Friendly obstruction views registered by the app, keyed by Vpon license key.
 *
 * Obtain the shared instance with [VpadnAdapter.getVponObstruction]. The adapter adds the
 * registered views to every Vpon ad request made with the same license key.
 *
 * Views are held weakly so that a registration the app forgets to remove does not keep an
 * Activity alive. Call [removeViews] when the views are no longer on screen.
 */
class VponObstruction {

    internal class Entry(
        view: View,
        val purpose: VponAdRequest.FriendlyObstructionPurpose,
        val description: String?
    ) {
        val viewRef: WeakReference<View> = WeakReference(view)
    }

    internal val map: MutableMap<String, List<Entry>> = ConcurrentHashMap()

    /**
     * Returns the views registered for [licenseKey] that are still alive, or `null` if nothing is
     * registered for it.
     */
    internal fun getViewsByLicenseKey(licenseKey: String?): List<VponObstructView>? {
        val entries = licenseKey?.let { map[it] } ?: return null
        return entries.mapNotNull { entry ->
            entry.viewRef.get()?.let { VponObstructView(it, entry.purpose, entry.description) }
        }
    }

    /**
     * Registers the friendly obstruction views for [licenseKey], replacing any views previously
     * registered for the same key. Passing `null` views removes the registration.
     */
    fun addViews(licenseKey: String?, views: List<VponObstructView?>?) {
        if (licenseKey == null) return
        if (views == null) {
            map.remove(licenseKey)
            return
        }
        map[licenseKey] = views.filterNotNull()
            .map { Entry(it.obstructView, it.purpose, it.description) }
    }

    /** Removes the views registered for [licenseKey]. */
    fun removeViews(licenseKey: String?) {
        if (licenseKey == null) return
        map.remove(licenseKey)
    }
}
