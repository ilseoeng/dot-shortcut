package app.dotshortcut.core

enum class ConnectionTestStatus { IDLE, RUNNING, SUCCESS, FAILED }

class ConnectionTestSession {
    var status = ConnectionTestStatus.IDLE
        private set
    private var deadline = 0L

    fun start(now: Long) {
        deadline = now + 15_000L
        status = ConnectionTestStatus.RUNNING
    }

    fun observe(now: Long, isChatGpt: Boolean, hasDot: Boolean, hasChatControl: Boolean) {
        expire(now)
        if (status == ConnectionTestStatus.RUNNING && isChatGpt && hasDot && hasChatControl) {
            status = ConnectionTestStatus.SUCCESS
        }
    }

    fun expire(now: Long) {
        if (status == ConnectionTestStatus.RUNNING && now >= deadline) {
            status = ConnectionTestStatus.FAILED
        }
    }

    fun fail() { status = ConnectionTestStatus.FAILED }
}

fun canAddShortcut(installed: Boolean, accessibility: Boolean, verified: Boolean,
                   status: ConnectionTestStatus): Boolean =
    installed && accessibility && verified &&
        (status == ConnectionTestStatus.IDLE || status == ConnectionTestStatus.SUCCESS)
