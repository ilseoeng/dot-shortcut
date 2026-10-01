package app.dotshortcut.core

import org.junit.Assert.*
import org.junit.Test

class ConnectionTestSessionTest {
    @Test fun noEventsStillTimesOutAndIdleCannotPass() {
        val session = ConnectionTestSession()
        session.observe(0, true, true, true)
        assertEquals(ConnectionTestStatus.IDLE, session.status)
        session.start(500)
        session.expire(15_499)
        assertEquals(ConnectionTestStatus.RUNNING, session.status)
        session.expire(15_500)
        assertEquals(ConnectionTestStatus.FAILED, session.status)
    }

    @Test fun onlyAnObservedDotScreenPasses() {
        val session = ConnectionTestSession()
        session.start(100)
        session.observe(101, isChatGpt = false, hasDot = true, hasChatControl = true)
        assertEquals(ConnectionTestStatus.RUNNING, session.status)
        session.observe(102, isChatGpt = true, hasDot = false, hasChatControl = true)
        assertEquals(ConnectionTestStatus.RUNNING, session.status)
        session.observe(103, isChatGpt = true, hasDot = true, hasChatControl = false)
        assertEquals(ConnectionTestStatus.RUNNING, session.status)
        session.observe(104, isChatGpt = true, hasDot = true, hasChatControl = true)
        assertEquals(ConnectionTestStatus.SUCCESS, session.status)
    }

    @Test fun timeoutCannotBecomeSuccessAndRetryResetsSuccess() {
        val session = ConnectionTestSession()
        session.start(100)
        session.observe(15_100, true, true, true)
        assertEquals(ConnectionTestStatus.FAILED, session.status)
        session.observe(15_101, true, true, true)
        assertEquals(ConnectionTestStatus.FAILED, session.status)
        session.start(20_000)
        session.observe(20_001, true, true, true)
        assertEquals(ConnectionTestStatus.SUCCESS, session.status)
        session.start(30_000)
        assertEquals(ConnectionTestStatus.RUNNING, session.status)
    }

    @Test fun shortcutRequiresVerifiedResultInstalledAppAndAccessibility() {
        assertFalse(canAddShortcut(false, true, true, ConnectionTestStatus.IDLE))
        assertFalse(canAddShortcut(true, false, true, ConnectionTestStatus.IDLE))
        assertFalse(canAddShortcut(true, true, false, ConnectionTestStatus.IDLE))
        assertFalse(canAddShortcut(true, true, true, ConnectionTestStatus.RUNNING))
        assertFalse(canAddShortcut(true, true, true, ConnectionTestStatus.FAILED))
        assertTrue(canAddShortcut(true, true, true, ConnectionTestStatus.SUCCESS))
        assertTrue(canAddShortcut(true, true, true, ConnectionTestStatus.IDLE))
    }
}
