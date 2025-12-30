# Limitations & Honest Notes ⚠️

- This is a prototype: detection is heuristic and tuned for clear candlestick images on common chart styles.
- Lighting, camera angle, resolution, zoom, chart overlays, or non-standard renderings may degrade detection.
- The `CandleAnalyzer` contains simplified converters and may need YUV->RGB fixes (stubbed for brevity).
- OpenCV Android dependency artifact may need to be updated if a snapshot isn't available in your build environment.
- Always prefer `NO TRADE` when confidence is low — the engine is intentionally conservative.

Use this repo as a starting point for research, validation, and tuning with labeled chart images.
