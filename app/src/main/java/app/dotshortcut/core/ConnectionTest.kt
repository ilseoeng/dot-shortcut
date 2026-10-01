package app.dotshortcut.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A test observes screen controls only; it never presses the call button. */
object ConnectionTest {
    private val session = ConnectionTestSession()
    private val mutableStatus = MutableStateFlow(ConnectionTestStatus.IDLE)
    val status = mutableStatus.asStateFlow()
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val timeout = Runnable {
        session.expire(SystemClock.elapsedRealtime())
        mutableStatus.value = session.status
    }

    suspend fun start(context: Context) {
        handler.removeCallbacks(timeout)
        Prefs(context.applicationContext).clearAutoTest()
        session.start(SystemClock.elapsedRealtime())
        mutableStatus.value = session.status
        handler.postDelayed(timeout, 15_000L)
    }

    fun observe(context: Context, isChatGpt: Boolean, hasDot: Boolean, hasChatControl: Boolean) {
        if (session.status != ConnectionTestStatus.RUNNING) return
        session.observe(SystemClock.elapsedRealtime(), isChatGpt, hasDot, hasChatControl)
        if (session.status == ConnectionTestStatus.SUCCESS) {
            handler.removeCallbacks(timeout)
            scope.launch {
                Prefs(context.applicationContext).recordAutoTestConfirmed()
                mutableStatus.value = ConnectionTestStatus.SUCCESS
            }
        } else {
            mutableStatus.value = session.status
        }
    }

    fun fail() {
        handler.removeCallbacks(timeout)
        session.fail()
        mutableStatus.value = session.status
    }
}
