# AdMobMediationAdapterVpadn-Android

English | [繁體中文](README.md)

## Overview

Vpon's AdMob mediation adapter for Android.

AdMobMediationAdapterVpadn connects the Google Mobile Ads SDK with the Vpon SDK (vpadnSDK), so that AdMob mediation can route banner, interstitial, and native ad requests to Vpon.

The adapter is distributed as an AAR file. Download it from this repository's [Releases](https://github.com/vpon-sdk/AdMobMediationAdapterVpadn-Android/releases).

📄 AdMob mediation guide: https://wiki.vpon.com/android/mediation/admob/  
📄 Vpon SDK documentation: https://wiki.vpon.com/android/

---

## Requirements

- Android 5.0 (API 21)+. The GMA Next-Gen SDK itself requires Android 7.0 (API 24)+.
- Vpon SDK 5.8.x
- One of the following Google Mobile Ads SDKs:
  - Google Mobile Ads SDK (`com.google.android.gms:play-services-ads`)
  - GMA Next-Gen SDK (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk`)

---

## Installation

### 1. Download the AAR

Download `admob-adapter-2.3.4.aar` from [Releases](https://github.com/vpon-sdk/AdMobMediationAdapterVpadn-Android/releases) (use the file name of the release asset) and put it in your app module's `libs/` folder.

### 2. Add the Vpon Maven repository

Add the following to your app-level `build.gradle`:

```groovy
repositories {
    maven { url 'https://m.vpon.com/sdk/android/maven' }
}
```

Then add the dependencies from 2.1 or 2.2, depending on which Google Mobile Ads SDK your app uses. `5.8.+` uses the latest 5.8 release.

Your app adds the Vpon SDK and the Google Mobile Ads SDK itself; the adapter does not bring in either SDK. For the rest of the Vpon SDK setup (AndroidManifest, permissions, etc.), see the [Vpon SDK documentation](https://wiki.vpon.com/android/).

### 2.1 Using the Google Mobile Ads SDK (`play-services-ads`)

```groovy
dependencies {
    implementation files('libs/admob-adapter-2.3.4.aar')
    implementation 'com.vpon:vpadnSDK:5.8.+'
    // 25.5.0 requires minSdk 24+. If your app's minSdk is 23, use 24.9.0; if it is 21 or 22, use 23.6.0 (the last version that supports API 21).
    implementation 'com.google.android.gms:play-services-ads:25.5.0'
}
```

### 2.2 Using the GMA Next-Gen SDK (`ads-mobile-sdk`)

When you use `ads-mobile-sdk`, follow [Google's instructions](https://developers.google.com/admob/android/next-gen/mediation) and exclude the legacy SDK globally in your app-level `build.gradle`:

```groovy
configurations.configureEach {
    exclude group: 'com.google.android.gms', module: 'play-services-ads'
    exclude group: 'com.google.android.gms', module: 'play-services-ads-lite'
}

dependencies {
    implementation files('libs/admob-adapter-2.3.4.aar')
    implementation 'com.vpon:vpadnSDK:5.8.+'
    implementation 'com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.7.0'
}
```

---

## AdMob mediation setup

- Banner and interstitial: in your AdMob mediation group, add the "Vpon" ad network and enter your Vpon License Key.
- Native: add a custom event; see the guide below for the class name and parameters.

For details, see: 📄 https://wiki.vpon.com/android/mediation/admob/

---

## Friendly obstructions (optional)

If your app draws its own views over the ad (for example a close button or a countdown), register them as friendly obstructions so they do not affect viewability measurement:

```kotlin
import com.vpadn.mediation.VpadnAdapter
import com.vpon.ads.VponAdRequest
import com.vpon.pojo.VponObstructView

// Register before requesting ads; licenseKey is the Vpon License Key set in the AdMob console
VpadnAdapter.getVponObstruction().addViews(licenseKey, listOf(
    VponObstructView(closeButton, VponAdRequest.FriendlyObstructionPurpose.CLOSE_AD, "close button")
))

// Remove when the screen is finished
VpadnAdapter.getVponObstruction().removeViews(licenseKey)
```

`addViews` replaces any earlier registration for the same License Key. The adapter keeps only weak references to these views, so it does not leak your Activity.

---

## Compatibility

| Adapter | Minimum Android | Vpon SDK | Google Mobile Ads | Notes |
|---------|-----------------|----------|-------------------|-------|
| 2.3.4   | 5.0 (API 21)    | 5.8.x | `play-services-ads`; GMA Next-Gen `ads-mobile-sdk` (Next-Gen itself requires API 24) | |

Each Google Mobile Ads SDK version has its own minimum Android version; see Google's documentation.

`2.3.4` has been tested through AdMob mediation on an Android 17 device with Vpon SDK 5.8.0, covering banner (including adaptive sizes), interstitial, and native ads, with Google Mobile Ads SDK `play-services-ads` and GMA Next-Gen `ads-mobile-sdk`.

---

## Building from source

```sh
./gradlew :adapter:assembleRelease
```

The AAR is written to `adapter/build/outputs/aar/admob-adapter-<version>.aar`. JDK 17 is required.

---

## Modification and license

The source code in this repository is provided for review and debugging only. Under the LICENSE, modification or redistribution requires written permission from Vpon. For bug reports, feature requests, or licensing questions, contact <fae@vpon.com>.

---

## License

Copyright © Vpon Inc. All rights reserved.  
Unauthorized copying, distribution, or use of this SDK is strictly prohibited.  
Use of this SDK is subject to the [Vpon Terms of Service](http://www.vpon.com).
