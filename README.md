# Career Shield OS — Construction Site Risk & Hybrid AI Operating System

> Built for **#BuildHybridAI 2026** (Android Track) using **Kotlin**, **Jetpack Compose**, **Room Database**, **Firebase App Check**, and the **Firebase AI Logic Hybrid Inference SDK** (`firebase-ai` + `firebase-ai-ondevice` with **Gemini Nano** on-device and **Cloud Gemini** fallback).

---

## Value Proposition

Construction project managers and site engineers work in signal-dead basements, remote job sites, and high-pressure client meetings where a single missed Variation Order (VO), delayed Extension of Time (EOT) notice, safety violation, or undocumented defect can trigger severe **Liquidated Damages (LD)** penalties or stop-work liability.

**Career Shield OS** puts a rugged, high-contrast command center in the manager's pocket:
- **Works Offline on Dead-Signal Sites**: Daily checklists, site risk photos (`FileProvider` + Room DB v2), and SOS notice templates live locally in SQLite, while **`GeminiAiRepository` + Firebase AI Logic Hybrid Inference (`InferenceMode.PREFER_ON_DEVICE`)** runs fast text tasks on-device via **Gemini Nano / AICore** at zero API cost and zero network latency.
- **Seamless Cloud Escalation**: Heavy multimodal site photo inspections, Google Search-grounded Thai construction contract law scans, deep `ThinkingLevel.HIGH` crisis reasoning, and hands-free Live Voice sessions automatically route to **Cloud Gemini** (`gemini-3.8-flash`, `gemini-4-carbon`, `gemini-3.8-live`) when online.
- **Transparent Execution Indicator**: Every AI response inspects `response.inferenceSource` (`InferenceSource.ON_DEVICE` vs `InferenceSource.IN_CLOUD`) to render a live status badge (`[On-Device: AICore]` or the verified `extension.getModelName()` when confirmed by AICore vs `[Cloud Fallback: Gemini]`) in the app header and output cards.

---

## Hybrid AI Architecture (`GeminiAiRepository`)

### 1. Firebase AI Logic Hybrid Inference (`PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW`)
Using `com.google.firebase:firebase-ai` (via Firebase BoM `34.17.0`) together with `com.google.firebase:firebase-ai-ondevice` (`16.0.0-beta04`) and `Firebase.ai(backend = GenerativeBackend.googleAI())`:

| Feature | Inference Mode | Primary Engine | Fallback / Notes |
|---|---|---|---|
| **Nano 4 Full Preview Diagnostic** (`runNano4PreviewDiagnostic`) | `InferenceMode.ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore Preview (`nano-v4-full` / Gemma 4 E4B)** | Strict on-device only (`maxOutputTokens = 256`); never falls back to Cloud; tests EN and TH prompts separately |
| **Material Budget Sanity Check** (`auditMaterialRequisition`) | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Grounded strictly in read-only `CertifiedBaselineData` (`CareerShield-RoleCore-v2026.1`); ephemeral inputs never mutate baseline |
| **Site Safety Risk Analyzer** (`analyzeSiteSafetyRisks`) | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Cloud `gemini-3.8-flash`; assesses severity, Thai Ministerial Safety Regs (2564), PPE & Stop-Work controls |
| **SOS Script Tailoring** (VO / EOT / Safety notices) | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Cloud `gemini-3.8-flash` |
| **Daily Remind Log Summarization** | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Summarizes Room DB task states & photo counts |
| **Anti-BS Duplicate Progress Check** | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Audits Room tasks for stagnant/duplicate reports or missing photo proof |
| **Single-Turn Site Checklist Q&A** | `InferenceMode.PREFER_ON_DEVICE` / `ONLY_ON_DEVICE` + `OnDeviceModelOption.PREVIEW` | **AICore On-Device Preview** | Cloud `gemini-3.8-flash` |
| **Site Photo Risk Inspection** (Camera / Room DB photo) | `InferenceMode.ONLY_IN_CLOUD` (Multimodal) | **Cloud `gemini-3.8-flash`** | Analyzes downscaled bitmaps for safety, QA/QC, LD & VO evidence |
| **Contract Clause & LD Risk Scanner** | Cloud Grounded / Deep Thinking | **Cloud `gemini-3.8-flash`** + `Tool.googleSearch()` / **`gemini-4-carbon`** (`ThinkingLevel.HIGH`) | Legal risk detection & Counter-Clause drafting |
| **Multi-turn Advisor & Live Voice** | Cloud Session / Live API | **`gemini-3.8-flash`** (`startChat`) & **`gemini-3.8-live`** | Real-time hands-free site voice guidance |

### 2. Offline Site Test Mode (`ONLY_ON_DEVICE`) & Preview Diagnostic
A global **Offline** toggle in the top status strip switches hybrid text pipelines from `InferenceMode.PREFER_ON_DEVICE` to `InferenceMode.ONLY_ON_DEVICE`, allowing judges and engineers to test true on-device AICore execution (and observe authentic `FirebaseAIOnDeviceNotAvailableException` diagnostics on unsupported emulators with a 1-tap Cloud Fallback recovery). In addition, the **Nano 4 Full Preview Diagnostic** card in the **Hybrid AI** tab executes an isolated `ONLY_ON_DEVICE` call with `OnDeviceModelOption.PREVIEW` (`maxOutputTokens = 256`) for both English and Thai prompts without falling back to Cloud.

### 3. Firebase App Check Security (`FirebaseManager`)
All Firebase AI Logic calls are protected by **Firebase App Check**, initialized via `FirebaseManager.initializeAppCheck(context, intent)` (called through `configureAppCheck(this, intent)` as the first step in `MainActivity.onCreate()` before any UI, ViewModel, or `GeminiAiRepository` calls):
- **Local Development (`BuildConfig.DEBUG`)**: Installs `DebugAppCheckProviderFactory` and synchronously persists `FIREBASE_APPCHECK_DEBUG_TOKEN` from the launch `Intent` via `SharedPreferences.Editor.commit()` so attestation succeeds immediately.
- **Production Builds (`!BuildConfig.DEBUG`)**: Installs `PlayIntegrityAppCheckProviderFactory` for hardware-backed Play Integrity attestation.
- No hardcoded Gemini API keys or private secrets are stored in source code or `.env`.

### 4. Local Persistence (Room Database v3 + FileProvider) & Certified Baseline
- `CertifiedBaselineData` (Read-Only): Curated role-specific reference rules (`CareerShield-RoleCore-v2026.1`) for floor/tile/mortar norms, concrete strength, facade glass, safety regs, and LD/VO guardrails. Ephemeral requisition checks never overwrite this baseline.
- `ExceptionAuditEntity` (`exception_audit_logs`): Records manager-approved exceptions or investigation holds (`APPROVED_WITH_EXCEPTION` / `HOLD_FOR_INVESTIGATION`), transparency justification notes, and inference origin without mutating core norms.
- `TaskEntity` (`career_tasks`): Tracks daily checkpoints, risk categories (`SAFETY`, `SLA`, `FINANCE`, `CONTRACT`), completion state, timestamps, and local `photoUri` captured via Android Camera (`TakePicture`) or Android Photo Picker (`PickVisualMedia`).
- `ScriptEntity` (`sos_scripts`): Stores reusable Thai construction SOS notices (EOT, VO, Safety, Retention) for 1-click clipboard copy, Android Share Intent, and Hybrid AI customization.
- Includes explicit Room `MIGRATION_1_2` (`ALTER TABLE career_tasks ADD COLUMN photoUri TEXT`) and `MIGRATION_2_3` (`CREATE TABLE exception_audit_logs`).

---

## Setup & Build Instructions

### Prerequisites
- Android Studio (Koala / Ladybug or newer) with JDK 11+
- Android SDK 36 (`minSdk = 26` required by `firebase-ai-ondevice`)
- A Firebase project with **Firebase AI Logic** and **Firebase App Check** enabled.

### 1. Configure `google-services.json`
`app/google-services.json` is excluded from Git in `.gitignore` so project credentials are never committed to a public repository.
1. Open the [Firebase Console](https://console.firebase.google.com/), select or create your project, and enable **Firebase AI Logic** (Gemini Developer API).
2. Register an Android app with package name `com.aistudio.careershield.rkmvqz` (add your debug/release SHA-1 and SHA-256 fingerprints).
3. Download **`google-services.json`** and place it at:
   ```text
   app/google-services.json
   ```
4. In **Firebase Console > App Check**, register your app with **Play Integrity** (for production builds) and manage **Debug Tokens** (for local emulator/development testing).

### 2. Pass `FIREBASE_APPCHECK_DEBUG_TOKEN` for Local Development
In debug builds (`BuildConfig.DEBUG`), `FirebaseManager` reads the `FIREBASE_APPCHECK_DEBUG_TOKEN` string extra from `MainActivity`'s launch `Intent` and writes it synchronously to `SharedPreferences` before installing `DebugAppCheckProviderFactory`:

- **Via `adb` on an emulator or test device**:
  1. Register a UUID debug token in **Firebase Console > App Check > Apps > Manage debug tokens** (or copy the generated debug secret printed in Logcat by `DebugAppCheckProvider` on first run).
  2. Launch the app passing the token in the Intent extra:
     ```bash
     adb shell am start -n com.aistudio.careershield.rkmvqz/com.example.MainActivity \
       --es FIREBASE_APPCHECK_DEBUG_TOKEN "<YOUR_REGISTERED_DEBUG_TOKEN>"
     ```
- **Via Android Studio**:
  1. Open **Run > Edit Configurations...** for the `app` configuration.
  2. Under **Launch Flags**, add:
     ```text
     --es FIREBASE_APPCHECK_DEBUG_TOKEN "<YOUR_REGISTERED_DEBUG_TOKEN>"
     ```

### 3. On-Device Gemini Nano 4 Full Preview Testing
- **AICore Developer Preview device** (enrolled supported device per [AICore Dev Preview](https://developers.google.com/ml-kit/genai/aicore-dev-preview)): Hybrid features request `OnDeviceModelOption.PREVIEW` (`nano-v4-full`, Gemma 4 E4B foundation) via `InferenceMode.PREFER_ON_DEVICE`. When `response.inferenceSource == InferenceSource.ON_DEVICE`, the badge displays `[On-Device: AICore]` or the verified `extension.getModelName()`. Note: Official documentation validates English and Korean on-device; Thai output quality can be evaluated directly via the diagnostic card.
- **Emulators / non-AICore devices**: `PREFER_ON_DEVICE` automatically falls back to Cloud Gemini (`[Cloud Fallback: Gemini]`). Use the **Nano 4 Full Preview Diagnostic** card (`ONLY_ON_DEVICE`) to inspect raw `checkStatus()`, `getModelName()`, and authentic SDK errors without cloud fallback.

### 4. Build & Test Commands
```bash
# Run Robolectric, Room DB, and FirebaseManager unit tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```

---

## License

Licensed under the **Apache License, Version 2.0**. See [LICENSE](LICENSE) for details.
