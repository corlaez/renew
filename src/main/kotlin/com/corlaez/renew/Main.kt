import kotlinx.coroutines.*
import java.time.Duration

private var i: Long = 0
private var e: Long = 0

public fun main() {
        val renew = Renew(Duration.ofSeconds(1), consume = ::consume)
        runBlocking {
                launch(Dispatchers.IO) {
                        renew.init {
                                i++
                                println("init query $i")
                                delay(3000)
                                i
                        }
                }
                println("Completed")
        }
}

private fun consume(l: Long) {
        e++
        println("end query $e")
}