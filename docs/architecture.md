# Architecture Overview ✅

## High-level components

- **MainActivity**: Hosts CameraX preview and UI overlay; requests camera permissions; orchestrates the 40-second pipeline.
- **CandleAnalyzer**: ImageAnalysis Analyzer that converts camera frames into a simplified list of `Candle` objects using OpenCV heuristics.
- **DecisionPipeline**: Implements the strict 40-second decision timing (0–10, 10–20, 20–30, 30–40 sec phases).
- **DecisionEngine**: Multi-layer confirmation engine with 4 layers (market/trend, candle pattern, location, indicator stub) and the accuracy scoring rules.
- **UI**: Live preview + overlay showing Decision, Accuracy %, Short reason, Countdown timer, sound/vibration alerts.
  - Tap the reason text to open the adjustable **accuracy threshold** (Pro feature, prototype).

## Data flow (camera -> decision)
1. CameraX preview provides frames to ImageAnalysis.
2. `CandleAnalyzer` detects vertical candle-like contours and maintains last 20–30 candles.
3. `DecisionPipeline` stages the evaluation over 40 seconds.
4. `DecisionEngine` scores each layer and computes final accuracy.
5. UI shows the result, plays sound/vibration if the result is actionable.

## Safety & compliance
- All processing is on-device; no network calls.
- No trading API or auto-execution.
- Risk disclaimer shown on first launch.
- Never claim 100% accuracy.
