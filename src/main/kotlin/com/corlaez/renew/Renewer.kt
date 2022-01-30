import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import java.time.Duration

public data class QueryAndUpdate<T>(val query: suspend () -> T, val update: (T) -> Unit) {
    public fun renewEvery(duration: Duration): Renewer<T> {
        return Renewer(duration, this)
    }
}

public class Renewer<T>(
    public val interval: Duration,
    public val queryAndUpdate: QueryAndUpdate<T>,
    public val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private var job: Job? = null
    private var isCancelled: Boolean = false
    private var isInitExecuted: Boolean = false

    public suspend fun init(): State {
        isInitExecuted = true
        val state = getState()
        if (state != State.IDLE) return state
        coroutineScope {
            job = launch {
                flow {
                    if(job?.isCancelled!!) this@coroutineScope.cancel()
                    while (true) {
                        delay(interval.toMillis())
                        emit(queryAndUpdate.query())
                    }
                }.collect { queryAndUpdate.update(it) }
            }
        }
        return State.RUNNING
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
