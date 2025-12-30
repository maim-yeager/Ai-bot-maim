# Accuracy Calculation & Win-Chance System 📊

The accuracy is computed as a sum of four components (0–100):

- Trend strength (0–30)
  - Based on recent candle dominance (ups vs downs)
- Pattern quality (0–30)
  - Strong engulfing or hammer -> higher score
- Location strength (0–20)
  - Proximity to recent highs/lows -> stronger
- Indicator agreement (0–20)
  - Visual indicator agreement if RSI/EMA lines detected (stub in prototype)

Rules:
- Accuracy < 80% → NO TRADE
- 80–84% → Risky (Pro mode may allow)
- ≥ 85% → High probability (allowed)

Notes:
- The scores are heuristic and should be validated with labeled chart images.
- No claims of guaranteed accuracy. Use only for educational analysis.
