package com.corlaez.renew

import RenewState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import java.time.Duration

internal interface RenewApi<T> {
    val interval: Duration
    val consume: (T) -> Unit

    suspend fun init(query: suspend () -> T): Boolean
    fun getState(): RenewState
    fun cancel(message: String = "Cancelled manually")
}

internal class RenewImpl<T>(
    private val runQueryAsyncFromDelay: Boolean,
    override val interval: Duration,
    override val consume: (T) -> Unit,
): RenewApi<T> {
    private var job: Job? = null
    private var isCancelled: Boolean = false
    private var isInitExecuted: Boolean = false

    override suspend fun init(query: suspend () -> T): Boolean {
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

    override fun getState(): RenewState {
        return if (isCancelled)
            RenewState.CANCELLED
        else if (isInitExecuted)
            RenewState.RUNNING
        else
            RenewState.IDLE
    }

    override fun cancel(message: String) {
        isCancelled = true
        job?.cancel(message)
    }

    protected fun finalize() {
        cancel("Cancelled by finalize")
    }
}
