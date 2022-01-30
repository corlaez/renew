package com.corlaez.renew

import kotlinx.coroutines.coroutineScope
import java.time.Duration

public class Renewable<T>(public var renewableValue: T, public val supplier: suspend() -> T) {
    private var cancelList = listOf<() -> Unit>()

    public suspend fun renewEvery(duration: Duration, waitCompletionThenDelay: Boolean = false): () -> Unit {
        val renew = Renew(waitCompletionThenDelay, duration) { newValue: T ->
            renewableValue = newValue
        }
        cancelList = cancelList + renew::cancel
        coroutineScope {
            renew.init(supplier)
        }
        return renew::cancel
    }

    public fun cancelAll() {
        val copyCancelList = cancelList.toList()
        cancelList = emptyList()
        copyCancelList.forEach { cancel ->
            cancel()
        }
    }
}
