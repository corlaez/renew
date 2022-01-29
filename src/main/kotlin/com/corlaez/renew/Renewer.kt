package com.corlaez.renew

import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.time.Duration

public class BackgroundIntervalRenew<T>(
    public var interval: Duration,
    supplier: () -> T,
) {
    private val renew = Renewable(supplier)
    private var loop = runBlocking {
            renew.renovate()
            launch {
                while (this.isActive) {
                    delay(interval.toMillis())
                    renew.renovate()
                }
            }
        }

    public fun cancel() {
        loop.cancel("Manually cancelled")
    }

    public fun getValue(): Deferred<T>  {
        return renew.getValue()
    }

    private class Renewable<T>(val supplier: suspend () -> T) {
        var deferred: Deferred<T>? = null

        fun getValue(): Deferred<T> {
            return deferred ?: throw IllegalStateException("Call renovate before calling getValue")
        }

        suspend fun fetch(): Deferred<T> = coroutineScope {
            async { supplier() }
        }

        suspend fun renovate(): Unit = coroutineScope {
            deferred = fetch()
        }
    }
}
