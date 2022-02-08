import com.corlaez.renew.Renewable
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

private var i: Long = 0
private var e: Long = 0

class MyTest {

        @Test
        fun mytest() {
                assertEquals(false, true)
/*
        val renewable = Renewable(1L) {
                i++
                println("init query $i")
                delay(3000)
                i
        }
        runBlocking {
                launch(Dispatchers.IO) {
                        renewable.renewEvery(Duration.ofSeconds(1))
                }
                println("Completed")
        }*/
/*
        val renew = Renew(Duration.ofSeconds(1), consume = consume)
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
        }*/
        }

        private fun consume(l: Long) {
                e++
                println("end query $e")
        }
}