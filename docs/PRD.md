# Product Requirements Document — StartScope

**Version:** 0.9 draft  
**Date:** 15 May 2026  
**Platform:** Android 15 (API 35) and above  
**Owner:** Nicolai Tufar  
**Status:** Pre-build

---

## 1. Vision

Give every Android user a system-level "black box" for app launches. Not averages from Firebase, but the exact truth on *your* phone: why each app started, whether it was cold/warm/hot, how long each phase took, and what you can do about it.

StartScope turns Android 15's new `ApplicationStartInfo` API into a readable timeline.

## 2. Problem statement

- Apps feel slow. Banking apps take 4 seconds, social apps pop up uninvited. Android settings only show battery, not launch cost.
- Developers see aggregated startup times in consoles. Users see nothing.
- Android 15 added `ActivityManager.getHistoricalProcessStartReasons()` and `addApplicationStartInfoCompletionListener()`, which expose start reason, start type, timestamps for fork, bindApplication, first frame, fully drawn. No consumer app surfaces this data.

## 3. Goals

1. Record every process start on device for the last 7 days, on-device only.
2. Classify each start as cold, warm, or hot, with reason (launcher, broadcast, alarm, job, push, content provider, service).
3. Show per-app startup time breakdown and identify wasteful autostarts.
4. Suggest actionable fixes the user can apply without root.
5. Work 100% offline, with <1% daily battery impact.

## 4. Non-goals

- Not a task killer. We do not force-stop apps automatically.
- Not a developer profiler (no method tracing, no Perfetto).
- Not a cloud analytics product. No telemetry upload.
- Not supporting Android <15 in v1.

## 5. Target users

**Primary:** Power users in Greece and EU with Android 15 phones who notice lag, especially on mid-range devices.

**Secondary:** Android developers who want real-world startup data from their own daily driver.

**Persona:** Maria, 34, Athens. Uses Eurobank, Viva Wallet, Instagram. Phone feels hot after commuting. Wants to know which apps wake up in background.

## 6. User stories

- As Maria, I can open the app and see a timeline: "14:32 — Eurobank — cold start — 1,842 ms — reason: broadcast from com.google.android.gms".
- As Maria, I can tap an app and see its average cold start this week vs last week.
- As Maria, I can see "Top autostart offenders" ranked by number of non-launcher starts.
- As a developer, I can export a JSON of my app's starts with all timestamps.
- As a privacy-conscious user, I can verify the app requests no INTERNET permission.

## 7. Functional requirements

### MVP (v0.1)


| ID  | Feature               | Details                                                                                                                                                                                                         |
| --- | --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| F1  | Historical collection | Poll `getHistoricalProcessStartReasons(200)` every 15 min via WorkManager. Also register `addApplicationStartInfoCompletionListener` for live events.                                                           |
| F2  | Data parsing          | Map `getStartType()` → COLD/WARM/HOT, `getReason()` → human string, `getStartupTimestamps()` → fork, bindApplication, firstFrame, fullyDrawn. Calculate TTID = firstFrame - launch, TTFD = fullyDrawn - launch. |
| F3  | Storage               | Room DB table `starts` with fields: packageName, processName, pid, startType, reason, launchMode, wasForceStopped, ttidMs, ttfdMs, timestamp, intentAction. Retain 7 days, auto-prune.                          |
| F4  | Timeline UI           | Jetpack Compose screen: reverse chronological list, grouped by day. Filter by app.                                                                                                                              |
| F5  | App detail            | Tap app → show avg cold/warm/hot times, count by reason, last 20 starts.                                                                                                                                        |
| F6  | Permissions           | Request `PACKAGE_USAGE_STATS` via settings intent. No root.                                                                                                                                                     |
| F7  | Privacy               | Manifest declares no INTERNET, no ACCESS_NETWORK_STATE. All processing on-device.                                                                                                                               |


### v0.2


| ID  | Feature            | Details                                                                                                                                                                                                                     |
| --- | ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| F8  | Autostart doctor   | Detect patterns: >5 starts/day with reason = BROADCAST, ALARM, JOB, PUSH and not LAUNCHER. Show card: "Instagram started 23 times by push yesterday, avg 340ms".                                                            |
| F9  | Suggestions engine | Rule-based: if app has >10 broadcast starts and user hasn't opened it in 3 days → suggest "Restrict background" deep link to system settings. If cold start >2000ms and reason = BOOT_COMPLETE → suggest disable autostart. |
| F10 | Export             | Share JSON or CSV of selected app's starts.                                                                                                                                                                                 |
| F11 | Widgets            | Home screen widget: "Slowest cold start today".                                                                                                                                                                             |


### v0.3

| F12 | Baseline comparison | Store 14-day rolling baseline per app. Alert when today's avg TTID > baseline + 30%. |
| F13 | Intent inspector | Show `getIntent()` action and extras (redacted) to explain why a service started. |

## 8. Non-functional requirements

- **Performance:** Collection <20ms per poll. DB write batched. Battery <1% per day on Pixel 7 test.
- **Reliability:** Works after reboot, handles incomplete starts (`STARTUP_STATE_STARTED` but no first frame).
- **Accessibility:** Full TalkBack support, dynamic font scaling.
- **Localization:** Greek and English for v1.
- **Size:** APK <8MB, no bundled ML models.

## 9. Technical architecture

**Language:** Kotlin 2.0, Compose BOM 2026.05

**Components:**

- `StartCollectorService` (foreground, type `dataSync`) registers completion listener
- `StartWorker` (WorkManager, constraints: device idle) polls historical data
- `StartRepository` → Room
- `DoctorEngine` → pure Kotlin rules for suggestions
- UI: Compose Navigation, Material 3

**Key APIs:**

- `ActivityManager.getHistoricalProcessStartReasons(int)`
- `ActivityManager.addApplicationStartInfoCompletionListener(Executor, Consumer<List<ApplicationStartInfo>>)`
- `UsageStatsManager.queryUsageStats` for last-opened time

**Data model:**

```
ApplicationStartRecord {
  id: Long
  packageName: String
  startType: Int // 1 cold, 2 warm, 3 hot
  reason: Int // map to START_REASON_*
  ttidMs: Long?
  ttfdMs: Long?
  timestampMs: Long
  launchMode: Int
  wasForceStopped: Boolean
  intentAction: String?
}
```

## 10. UX flow

1. First launch → onboarding explains Android 15 requirement, requests Usage Access.
2. Home → timeline. Empty state shows "collecting first starts".
3. Tap entry → detail sheet with phase breakdown bar chart.
4. Doctor tab → list of offenders with "Fix" buttons linking to system settings (Battery optimization, Background restrictions).
5. Settings → retention period, export, about.

Visual style: monochrome timeline, color-coded start type (blue cold, amber warm, green hot).

## 11. Privacy and security

- No network permission in base APK. Optional "share feedback" module is separate dynamic feature.
- All timestamps stored in nanoseconds converted to local time on display only.
- Data never leaves device unless user explicitly exports.
- Open source the core collector under Apache 2 to build trust.

## 12. Success metrics

- Day-7 retention >35% for power users
- Average user identifies and restricts 2+ autostart apps in first week
- Median TTID reduction of 15% for top 3 apps after applying suggestions (measured internally)
- Crash-free sessions >99.8%

## 13. Roadmap

**Phase 1 (4 weeks):** MVP F1-F7, internal dogfood on Pixel 8 Android 15
**Phase 2 (3 weeks):** Doctor engine F8-F9, Greek localization
**Phase 3 (3 weeks):** Baseline alerts, widget, Play Store closed testing
**Phase 4:** Explore Android 16 APIs for I/O bytes per start

## 14. Risks and mitigations

- **Only Android 15+ (~28% install base in May 2026):** Acceptable for v1, market as "for modern phones".
- **OEMs restrict Usage Access:** Provide step-by-step guides for Samsung, Xiaomi.
- **Data volume:** Limit to 200 records per poll, prune aggressively.
- **Misinterpretation of reasons:** Show raw constant plus plain Greek explanation, link to docs.

## 15. Open questions

1. Should we correlate with `ApplicationExitInfo` to show crash loops?
2. Do we need a root companion for pre-15 devices using logcat parsing?
3. Should suggestions be automated via Accessibility actions, or keep manual for trust?

---

