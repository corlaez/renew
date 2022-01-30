import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import java.time.Duration

public fun main() {
        runBlocking {
                var i: Long = 0
                var e: Long = 0
                val renewer = Renewer2<Long>(
                        Duration.ofSeconds(1)
                ) {
                        e++
                        println("end query $e")
                }
                launch(Dispatchers.IO) {
                        renewer.init {
                                i++
                                println("init query $i")
                                delay(3000)
                                i
                        }
                }
                println("Completed")
        }
}

public class Renewer2<T>(
        public val interval: Duration,
        public val runQueryAsyncFromDelay: Boolean = true,
        public val consume: (T) -> Unit,
) {
        private var job: Job? = null
        private var isCancelled: Boolean = false
        private var isInitExecuted: Boolean = false

        public companion object {
                public operator fun <T> invoke(interval: Duration, runQueryAsyncFromDelay: Boolean,consume: (T) -> Unit): Renewer2<T> {
                        return Renewer2(interval, runQueryAsyncFromDelay, consume)
                }
        }

        public suspend fun init(query: suspend () -> T): Boolean {
                val state = getState()
                isInitExecuted = true
                if (state != State.IDLE) return false
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

        public enum class State {
                IDLE,
                RUNNING,
                CANCELLED
        }

        public fun getState(): State {
                return if (isCancelled)
                        State.CANCELLED
                else if (isInitExecuted)
                        State.RUNNING
                else
                        State.IDLE
        }

        public fun cancel(message: String = "Cancelled manually") {
                isCancelled = true
                job?.cancel(message)
        }

        protected fun finalize() {
                cancel("Cancelled by finalize")
        }
}
