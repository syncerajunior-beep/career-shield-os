# NANO 4oK: Home Inspector & Site Intelligence

> Built for **#BuildHybridAI 2026** (Android Track) using **Kotlin**, **Jetpack Compose**, **Room Database (v5)**, Android **`PdfRenderer` / `PdfDocument`**, **Firebase App Check**, and **Firebase AI Logic Hybrid Inference** (`firebase-ai` + `firebase-ai-ondevice` with **Gemini Nano 4 Preview** on-device and **Cloud Gemini** fallback).

---

## NANO 4oK: Home Inspector & Site Intelligence

Elevate your construction and property inspection workflow with **NANO 4oK**—the next-generation AI assistant powered by **Gemini Nano 4**. Designed for freelance architects, home inspectors, and site foremen, **NANO 4oK** brings **4 Core Capabilities** to ensure every project is **"All-OK"**:

1. **Precision Inspection**: Instant AI-assisted defect detection for residential homes and condos.
2. **Site Management**: Scalable support for foremen handling complex, large-scale building projects.
3. **Evidence Vault**: Effortless photo logging and automated, professional PDF report generation.
4. **Smart Consultant**: On-the-spot root-cause analysis and practical repair solutions.

*From single-family homes to high-rise developments, inspect smarter, document faster, and build with confidence.*

---

## Streamlined 2-Screen Field Workflow (English Primary • Bilingual EN / TH Toggle)

- **Screen 1 — Site Walk (`Precision Inspection` + `Site Management`)**:
  - Select Floor/Unit & Trade Scope (`OPENINGS`, `MASONRY`, `STRUCTURAL`, `MEP_SAFETY`) with an **immutable real-time clock lock**.
  - Open any uploaded **Architectural PDF Floor Plan / Door-Window Schedule** (or generate the built-in 2-page Sample Architectural PDF), flip pages, and **tap directly on the drawing to drop numbered defect pins (`#1`, `#2`, ...)**.
  - Capture defect / tape-measure photos (`TakePicture` / `PickVisualMedia`) and run:
    - **Compare Photo vs PDF (`Cloud Multimodal`)**: Compares the active PDF plan page bitmap and actual site photo side-by-side to quantify tolerance variance (e.g., masonry rough opening vs. aluminum window spec `W1`) and allocate fault clearly.
    - **Quick Triage (`Hybrid On-Device`)**: Runs `InferenceMode.PREFER_ON_DEVICE` (`OnDeviceModelOption.PREVIEW`) with zero-latency fallback to **Cloud `gemini-3.8-flash`** when AICore is unavailable.
- **Screen 2 — Report & Email (`Evidence Vault` + `Smart Consultant`)**:
  - Filter saved defect pins (`All`, `Critical`, `Open`, `Fixed`), preview the pinned PDF floorplan overview, inspect full-screen site photos, and toggle `OPEN` / `FIXED` status.
  - **1-Tap Email Dispatch** (`Intent.ACTION_SEND_MULTIPLE`) to Contractor (`To:`) and Project Manager / Homeowner (`Cc:`), automatically attaching an **Annotated Blueprint Snapshot JPEG** (numbered defect pins + locked timestamp footer) and all evidence photos.

---

## Hybrid AI Architecture (`GeminiAiRepository`)

Using `com.google.firebase:firebase-ai` (Firebase BoM `34.17.0`) and `com.google.firebase:firebase-ai-ondevice` (`16.0.0-beta04`) via `Firebase.ai(backend = GenerativeBackend.googleAI())`:

| Feature | Inference Mode | Primary Engine | Fallback / Purpose |
|---|---|---|---|
| **Quick Defect Triage** (`triageSiteWalkDefectHybrid`) | `InferenceMode.PREFER_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **Gemini Nano 4 Preview (AICore)** | Zero-latency on-device triage & contractor notice; automatically escalates to **Cloud `gemini-3.8-flash`** on non-AICore hardware |
| **Photo vs. PDF Spec Comparison** (`comparePhotoWithPdfBlueprint`) | Cloud Multimodal (`ONLY_IN_CLOUD`) | **Cloud `gemini-3.8-flash`** | Dual-image multimodal analysis comparing PDF blueprint bitmap + jobsite tape-measure photo to settle Masonry vs. Aluminum disputes |
| **Nano 4 Preview Diagnostic** (`runNano4PreviewDiagnostic`) | `InferenceMode.ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **Strict On-Device Only** (`maxOutputTokens = 256`) | Accessible via the TopBar **Tune / Diagnostics** icon to verify `checkStatus()` / `getModelName()` cleanly without cluttering inspection screens |

### Offline Site Test Mode & Transparent Inference Pill
- The top status strip tracks `response.inferenceSource` (`ON_DEVICE`, `IN_CLOUD`, or `STANDBY`) and lets judges toggle **Offline Mode** (`InferenceMode.ONLY_ON_DEVICE`) to verify strict local execution and 1-tap Cloud fallback recovery on any device.

---

## Security, Persistence & Compliance

- **Firebase App Check (`FirebaseManager`)**:
  - `configureAppCheck(this, intent)` runs before any UI or Firebase calls in `MainActivity.onCreate()`.
  - `BuildConfig.DEBUG` installs `DebugAppCheckProviderFactory` and exposes an in-app **Copy Debug Token** banner inside the Diagnostics dialog (and on App Check errors) so testers on physical devices can copy and register their UUID in **Firebase Console > App Check > Manage debug tokens** without needing `adb`.
  - Release builds (`!BuildConfig.DEBUG`) install `PlayIntegrityAppCheckProviderFactory`.
  - No Gemini API keys or secrets are stored in code or `.env`.
- **Room Database v5 (`site_walk_defects`)**:
  - Persists pin numbers, immutable `capturedAt` timestamps, normalized PDF `(pinXRatio, pinYRatio)` coordinates, measured field readings, defect photo URIs (`FileProvider`), AI findings, responsible trade, and `OPEN`/`FIXED` status with non-destructive `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, and `MIGRATION_4_5`.
- **Least-Privilege Permissions**:
  - Uses zero-permission Android Photo Picker (`PickVisualMedia`) and Storage Access Framework (`OpenDocument` for PDFs), requesting runtime `CAMERA` permission only when capturing defect photos.

---

## Setup & Build Instructions

### 1. Firebase Configuration
`app/google-services.json` and `app/src/main/res/values/firebase_applet_config.xml` are intentionally excluded in `.gitignore`.
1. Open the [Firebase Console](https://console.firebase.google.com/), select your project, and enable **Firebase AI Logic** (Gemini Developer API) and **Firebase App Check**.
2. Register the Android package `com.aistudio.careershield.rkmvqz` with your keystore SHA-1/SHA-256 fingerprints and place `google-services.json` under `app/`.
3. For GitHub Actions APK builds, store the contents of `google-services.json` in repository secret **`GOOGLE_SERVICES_JSON`**.

### 2. Local App Check Debug Token Registration
1. Install the debug APK on your device and tap the **Tune / Diagnostics** icon in the top-right bar.
2. Copy the **App Check Debug Token** UUID shown in the yellow banner.
3. Add it in **Firebase Console > App Check > Apps > Manage debug tokens**.

### 3. Build & Test Commands
```bash
# Run Robolectric, Room migration, and Hybrid AI ViewModel tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```

---

## License

Licensed under the **Apache License, Version 2.0**. See [LICENSE](LICENSE) for details.
