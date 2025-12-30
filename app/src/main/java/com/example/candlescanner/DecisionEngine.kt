package com.example.candlescanner

import kotlin.math.max

/**
 * Decision engine implementing the 4 layers and accuracy scoring rules described.
 */
class DecisionEngine {
    enum class Decision { BUY, SELL, WAIT, NO_TRADE }

    data class Result(val decision: Decision, val accuracy: Int, val reason: String, val alert: Boolean)

    fun evaluate(snapshot: CandleSnapshot, threshold: Int = 80): Result {
        snapshot.message?.let {
            return Result(Decision.NO_TRADE, 0, it, false)
        }

        val candles = snapshot.candles
        if (candles.size < 5) return Result(Decision.NO_TRADE, 0, "TRADE NIO NA – insufficient candles", false)

        // LAYER 1 - market & trend (HH HL / LH LL)
        val trendScore = checkTrend(candles)
        if (trendScore < 10) return Result(Decision.NO_TRADE, 0, "TRADE NIO NA – Market unstable", false)

        // LAYER 2 - candle pattern
        val (patternOk, patternScore, patternReason) = checkPattern(candles)
        if (!patternOk) return Result(Decision.NO_TRADE, 0, patternReason, false)

        // LAYER 3 - location (support/resistance) simplified: check if last candle is at recent extremum
        val locationScore = checkLocation(candles)
        if (locationScore < 5) return Result(Decision.WAIT, 0, "WAIT – Setup complete hoy nai", false)

        // LAYER 4 - indicator (visual only) - stub (no indicators detected)
        val indicatorScore = 10 // neutral

        val accuracy = max(0, min(100, trendScore + patternScore + locationScore + indicatorScore))

        if (accuracy < threshold) return Result(Decision.NO_TRADE, accuracy, "NO TRADE – accuracy $accuracy% (< $threshold%)", false)

        // Decide direction based on last strong pattern
        val last = candles.last()
        val decision = if (last.bullish) Decision.BUY else Decision.SELL
        val reason = "${if (last.bullish) "Bullish" else "Bearish"} pattern near zone"

        return Result(decision, accuracy, reason, true)
    }

    private fun checkTrend(candles: List<Candle>): Int {
        // Simplified: compute series of highs/lows
        // Score 0-30
        var score = 0
        val n = candles.size
        var ups = 0
        var downs = 0
        for (i in 1 until n) {
            if (candles[i].area > candles[i-1].area) ups++ else downs++
        }
        val dominance = kotlin.math.abs(ups - downs)
        score = (dominance.coerceAtMost(10) * 3)
        return score.coerceAtMost(30)
    }

    private fun checkPattern(candles: List<Candle>): Triple<Boolean, Int, String> {
        // Look for strong engulfing or hammer
        // Score 0-30
        val last = candles.last()
        val prev = candles.getOrNull(candles.size - 2) ?: return Triple(false, 0, "TRADE NIO NA – Candle weak")

        // engulfing
        val engulfing = (last.area > prev.area * 1.2)
        val hammer = (last.bodySize < (last.wickBottom - last.wickTop) * 0.3)

        val score = when {
            engulfing -> 25
            hammer -> 20
            last.area > 200 -> 18
            else -> 0
        }

        if (score < 18) return Triple(false, score, "TRADE NIO NA – Candle weak")
        return Triple(true, score.coerceAtMost(30), "Pattern OK")
    }

    private fun checkLocation(candles: List<Candle>): Int {
        // If last candle is near local extremum -> stronger
        val last = candles.last()
        val prev = candles.getOrNull(candles.size - 2) ?: return 0
        val highs = candles.map { it.wickTop }
        val lows = candles.map { it.wickBottom }
        val maxH = highs.maxOrNull() ?: 0f
        val minL = lows.minOrNull() ?: 0f
        val proximityTop = (maxH - last.wickTop)
        val proximityBot = (last.wickBottom - minL)
        return when {
            proximityTop < 10 -> 15
            proximityBot < 10 -> 15
            else -> 5
        }
    }
}
