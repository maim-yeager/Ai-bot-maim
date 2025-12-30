package com.example.candlescanner

import kotlinx.coroutines.*

/**
 * Orchestrates the 40-second decision flow (4 phases). Calls DecisionEngine at the end.
 */
class DecisionPipeline(private val engine: DecisionEngine, private val snapshot: CandleSnapshot, private val threshold: Int = 80) {

    var onUpdate: ((phase: Int, secondsLeft: Int) -> Unit)? = null
    var onResult: ((DecisionEngine.Result) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    fun start() {
        scope.launch {
            // 0-10 sec: capture + detection (we already have snapshot)
            for (s in 10 downTo 1) {
                onUpdate?.invoke(1, s)
                delay(1000)
            }

            // 10-20 sec: market trend check
            for (s in 10 downTo 1) {
                onUpdate?.invoke(2, s)
                delay(1000)
            }

            // 20-30 sec: pattern + location validation
            for (s in 10 downTo 1) {
                onUpdate?.invoke(3, s)
                delay(1000)
            }

            // 30-40 sec: accuracy calculation & decision
            for (s in 10 downTo 1) {
                onUpdate?.invoke(4, s)
                delay(1000)
            }

            val result = engine.evaluate(snapshot, threshold)
            onResult?.invoke(result)
        }
    }
}
