package com.corlaez.renew

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import java.time.Duration

/**
 * Allows time-consuming queries to be executed and re-executed in the background with a delay between executions.
 *
 * @property runQueryAsyncFromDelay when true the delay will start without waiting for the query to complete
 * @property interval is the Duration that will be used in the delay between executions
 * @property consume a function that determines what to do each time the query returns a value
 * */
public class Renew<T>(
    public val runQueryAsyncFromDelay: Boolean,
    public val interval: Duration,
    public val consume: (T) -> Unit,
) {
    private var job: Job? = null
    private var isCancelled: Boolean = false
    private var isInitExecuted: Boolean = false

    /** Starts a flow that will emit or send the updated values as the queries separated by delays complete
     * @param query A function that provides a value. It is assumed this function takes a long time to complete */
    public suspend fun init(query: suspend () -> T): Boolean {
        val state = getState()
        isInitExecuted = true
        if (state != RenewState.IDLE) return false
        coroutineScope {
            job = launch {
                val flow = if (runQueryAsyncFromDelay) {
                    channelFlow {
                        if(job!!.isCancelled) this@coroutineScope.cancel()
                        runFlowLoop(this@coroutineScope, this::send, query)
                    }
                } else {
                    flow {
                        if(job!!.isCancelled) this@coroutineScope.cancel()
                        runFlowLoop(this@coroutineScope, this::emit, query)
                    }
                }
                flow.collect { consume(it) }
            }
        }
        return true
    }

    private suspend fun runFlowLoop(coroutineScope: CoroutineScope, sendOrEmit: suspend (T) -> Unit, query: suspend () -> T) {
        while (true) {
            delay(interval.toMillis())
            if (runQueryAsyncFromDelay) {
                coroutineScope.launch {
                    // channelFlow send
                    sendOrEmit(query())
                }
            } else {
                // flow emit
                sendOrEmit(query())
            }
        }
    }

    public fun getState(): RenewState {
        return if (isCancelled)
            RenewState.CANCELLED
        else if (isInitExecuted)
            RenewState.RUNNING
        else
            RenewState.IDLE
    }

    /** Stops the renewal concurrent process if there is one and prevents it to start or resume*/
    public fun cancel() {
        isCancelled = true
        job?.cancel("Cancelled manually")
    }

    protected fun finalize() {
        job?.cancel("Cancelled by finalize")
    }
}
