# CI / APK generation 🔧

Workflow: `.github/workflows/android-build.yml`

Usage:
1. Open the GitHub repository.
2. Go to the **Actions** tab and select `Android CI - Build Debug APK`.
3. Click **Run workflow** to trigger a build.
4. After the job completes, download the artifact named `candle-scanner-apk`.

The workflow provisions JDK and Android SDK and runs `./gradlew assembleDebug` before uploading the debug APK.

Tip: If you need release builds in future, add signing configs and a release job.
