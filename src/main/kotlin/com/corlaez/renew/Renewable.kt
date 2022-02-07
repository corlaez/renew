package com.corlaez.renew

import kotlinx.coroutines.coroutineScope
import java.time.Duration

/**
 * Wraps a value and aids managing its renewal.
 * Uses Renew behind the scenes to perform the renewal but exposes a simplified API.
 *
 * @property renewableValue the value that will be renewed
 * @property onUpdate (Optional) a callback that can execute an action. It is called after updating renewableValue
 * */
public class Renewable<T>(public var renewableValue: T, public val onUpdate: (T) -> Unit = {}) {
    private var runningRenewList = listOf<Renew<T>>()

    /** Starts a Renew task that updates the renewableValue.
     * @param duration
     * @param waitCompletionThenDelay
     * @param supplier
     * */
    public suspend fun renew(duration: Duration, waitCompletionThenDelay: Boolean = false, supplier: suspend() -> T): () -> Unit {
        val renew = Renew(waitCompletionThenDelay, duration) { newValue: T ->
            renewableValue = newValue
            onUpdate(newValue)
        }
        coroutineScope {
            renew.init(supplier)
        }
        runningRenewList = runningRenewList + renew
        return renew::cancel
    }

    /** Returns the list of all the running Renew tasks */
    public fun runningRenewList(): List<Renew<T>> {
        return runningRenewList.toList()
    }

    /** Cancels all Renew tasks */
    public fun cancelAll() {
        val copyCancelList = runningRenewList.toList()
        runningRenewList = emptyList()
        copyCancelList.forEach { renew ->
            renew.cancel()
        }
    }
}
