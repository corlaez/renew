import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration

public fun main() {
        runBlocking {
                val renewer = Renewer(
                        Duration.ofSeconds(1),
                        QueryAndUpdate(
                                query = { System.currentTimeMillis() },
                                update = { println("outside $it") }
                        )
                )
                launch(Dispatchers.IO) { renewer.run() }
                Thread.sleep(500)
                println("Completed")
                Thread.sleep(6000)
        }
}

public data class QueryAndUpdate<T>(val query: () -> T, val update: (T) -> Unit) {
        public fun renewEvery(duration: Duration): Renewer<T> {
                return Renewer(duration, this)
        }
}

public class Renewer<T>(
        public val interval: Duration,
        public val queryAndUpdate: QueryAndUpdate<T>,
        public val dispatcher: CoroutineDispatcher = Dispatchers.IO,
        public val waitCompletion: Boolean = false,
): AutoCloseable {
        private lateinit var loop: Job
        private lateinit var firstQuery: Job

        public suspend fun run() {
                flow {
                        while (true) {
                                delay(1000) // pretend we are doing something useful here
                                emit(System.currentTimeMillis()) // emit next value
                        }
                }.collect { println("outside $it") }
        }

        public suspend fun awaitFirstUpdate() {
                firstQuery.join()
        }

        override fun close() {
                loop.cancel("Manually cancelled")
                firstQuery.cancel("Manually cancelled")
        }
}












