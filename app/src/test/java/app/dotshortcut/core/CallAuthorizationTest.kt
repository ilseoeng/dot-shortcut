package app.dotshortcut.core
import org.junit.Assert.*
import org.junit.Test

class CallAuthorizationTest {
    @Test fun missingOrWrongTokenIsRejected() {
        assertFalse(CallAuthorization.matches("private-token", null))
        assertFalse(CallAuthorization.matches("private-token", "other"))
        assertFalse(CallAuthorization.matches("", ""))
        assertTrue(CallAuthorization.matches("private-token", "private-token"))
    }
}
