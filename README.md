# CandleScanner (Ai-bot-maim)

This repository is an Android educational prototype that scans candlestick charts using the phone's live camera and produces BUY / SELL / WAIT / NO TRADE signals using an on-device rule-based engine.

Key features:
- CameraX live preview with ImageAnalysis
- OpenCV-based candle detection (heuristic)
- 40-second decision pipeline and multi-layer confirmation engine
- Accuracy scoring and clear refusal to signal when confidence is low
- GitHub Actions workflow that builds Debug APK and uploads as an artifact

Important: This is an analysis tool only — no auto-trading, no broker APIs, and no cloud inference. See `docs/` for architecture, accuracy, CI, and limitations.

Quick start:
1. Ensure Android SDK + JDK are installed (or run the bootstrap script below).
2. Build locally using convenience script:

   - Bootstrap SDK (downloads minimal commandline tools & build-tools):
     `./scripts/bootstrap-android-sdk.sh`

   - Build APK (runs bootstrap if SDK missing):
     `./scripts/run-build.sh`  or `make build`

   - The resulting APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

3. Or use Actions > Run workflow to build and download the debug APK (recommended if local SDK installation is not possible here).

Note: In some containerized environments, installing SDK components may fail due to network/firewall/permission constraints — in that case use the GitHub Actions workflow which runs on GitHub-hosted runners.

License & Disclaimer: Educational use only. No guaranteed profit claims. See `docs/limitations.md`.
