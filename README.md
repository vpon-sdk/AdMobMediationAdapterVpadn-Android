# AdMobMediationAdapterVpadn-Android

[English](README.en.md) | 繁體中文

## 簡介

Vpon 的 Android AdMob 中介（mediation）adapter。

AdMobMediationAdapterVpadn 串接 Google Mobile Ads SDK 與 Vpon SDK（vpadnSDK），讓 AdMob 中介可以把 Banner、插頁、原生廣告的請求導向 Vpon。

adapter 以 AAR 檔提供，請到本 repository 的 [Releases](https://github.com/vpon-sdk/AdMobMediationAdapterVpadn-Android/releases) 下載。

📄 AdMob 中介設定指南：https://wiki.vpon.com/android/mediation/admob/  
📄 Vpon SDK 文件：https://wiki.vpon.com/android/

---

## 需求

- Android 5.0（API 21）以上。GMA Next-Gen SDK 本身需要 Android 7.0（API 24）以上。
- Vpon SDK 5.8.x
- 下列其中一種 Google Mobile Ads SDK：
  - Google Mobile Ads SDK（`com.google.android.gms:play-services-ads`）
  - GMA Next-Gen SDK（`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk`）

---

## 安裝

### 1. 下載 AAR

從 [Releases](https://github.com/vpon-sdk/AdMobMediationAdapterVpadn-Android/releases) 下載 `admob-adapter-2.3.4.aar`（檔名以 Release 附件為準），放進 App 模組的 `libs/` 資料夾。

### 2. 加入 Vpon Maven repository

在 App 層級的 `build.gradle` 加入：

```groovy
repositories {
    maven { url 'https://m.vpon.com/sdk/android/maven' }
}
```

再依 App 使用的 Google Mobile Ads SDK，照 2.1 或 2.2 加入相依套件。`5.8.+` 會使用 5.8 系列的最新版本。

Vpon SDK 與 Google Mobile Ads SDK 由 App 自行加入；adapter 不會帶入這兩個 SDK。Vpon SDK 的其他設定（AndroidManifest、權限等）請參考 [Vpon SDK 文件](https://wiki.vpon.com/android/)。

### 2.1 使用 Google Mobile Ads SDK（`play-services-ads`）

```groovy
dependencies {
    implementation files('libs/admob-adapter-2.3.4.aar')
    implementation 'com.vpon:vpadnSDK:5.8.+'
    // 25.5.0 需要 minSdk 24 以上。App 的 minSdk 是 23 時請用 24.9.0；是 21 或 22 時請用 23.6.0（支援 API 21 的最後一個版本）。
    implementation 'com.google.android.gms:play-services-ads:25.5.0'
}
```

### 2.2 使用 GMA Next-Gen SDK（`ads-mobile-sdk`）

改用 `ads-mobile-sdk` 時，依 [Google 的說明](https://developers.google.com/admob/android/next-gen/mediation)，在 App 層級的 `build.gradle` 全域排除舊版 SDK：

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

## AdMob 中介設定

- Banner、插頁：在 AdMob 的中介服務群組新增廣告聯播網「Vpon」，填入 Vpon License Key。
- 原生廣告：新增自訂事件（custom event），class name 與參數請見下方指南。

詳細步驟請參考：📄 https://wiki.vpon.com/android/mediation/admob/

---

## Friendly obstruction（選用）

廣告上方若有 App 自己的元件（例如關閉按鈕、倒數計時），可以登記為 friendly obstruction，避免影響可見度量測：

```kotlin
import com.vpadn.mediation.VpadnAdapter
import com.vpon.ads.VponAdRequest
import com.vpon.pojo.VponObstructView

// 請求廣告之前登記；licenseKey 是 AdMob 後台設定的 Vpon License Key
VpadnAdapter.getVponObstruction().addViews(licenseKey, listOf(
    VponObstructView(closeButton, VponAdRequest.FriendlyObstructionPurpose.CLOSE_AD, "close button")
))

// 頁面結束時移除
VpadnAdapter.getVponObstruction().removeViews(licenseKey)
```

`addViews` 會覆蓋同一個 License Key 原本的登記。adapter 只保留這些 View 的弱參照，不會造成 Activity 洩漏。

---

## 版本相容性

| Adapter | 最低 Android | Vpon SDK | Google Mobile Ads | 備註 |
|---------|--------------|----------|-------------------|------|
| 2.3.4   | 5.0（API 21） | 5.8.x | `play-services-ads`；GMA Next-Gen `ads-mobile-sdk`（Next-Gen 本身需要 API 24） | |

各版 Google Mobile Ads SDK 要求的最低 Android 版本，以 Google 公布的為準。

`2.3.4` 已在 Android 17 實機上，以 Vpon SDK 5.8.0 透過 AdMob 中介測試 Banner（含自適應尺寸）、插頁、原生廣告，Google Mobile Ads SDK 為 `play-services-ads` 與 GMA Next-Gen `ads-mobile-sdk`。

---

## 從原始碼建置

```sh
./gradlew :adapter:assembleRelease
```

產出 `adapter/build/outputs/aar/admob-adapter-<版本>.aar`。需要 JDK 17。

---

## 修改與授權

本 repository 的原始碼僅供檢視與除錯。依 LICENSE，修改或再散佈須取得 Vpon 書面授權。問題回報、功能建議或授權相關詢問，請聯絡 <fae@vpon.com>。

---

## License

Copyright © Vpon Inc. All rights reserved.  
Unauthorized copying, distribution, or use of this SDK is strictly prohibited.  
Use of this SDK is subject to the [Vpon Terms of Service](http://www.vpon.com).
