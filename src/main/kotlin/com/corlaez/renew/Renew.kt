import com.corlaez.renew.RenewApi
import com.corlaez.renew.RenewImpl
import java.time.Duration

public enum class RenewState {
    IDLE,
    RUNNING,
    CANCELLED
}

public class Renew<T>(
    public override val interval: Duration,
    public override val consume: (T) -> Unit,
): RenewApi<T> {
    private val renewImpl: RenewImpl<T> = RenewImpl(true, interval, consume)

    override suspend fun init(query: suspend () -> T): Boolean {
        return renewImpl.init(query)
    }

    override fun getState(): RenewState {
        return renewImpl.getState()
    }

    override fun cancel(message: String) {
        renewImpl.cancel(message)
    }
}

public class RenewBlocking<T>(
    public override val interval: Duration,
    public override val consume: (T) -> Unit,
): RenewApi<T> {
    private val renewImpl: RenewImpl<T> = RenewImpl(false, interval, consume)

    override suspend fun init(query: suspend () -> T): Boolean {
        return renewImpl.init(query)
    }

    override fun getState(): RenewState {
        return renewImpl.getState()
    }

    override fun cancel(message: String) {
        renewImpl.cancel(message)
    }
}
