package app.dotshortcut.core
import java.security.MessageDigest

object CallAuthorization {
    fun matches(expected: String, supplied: String?): Boolean =
        expected.isNotEmpty() && supplied != null &&
            MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), supplied.toByteArray(Charsets.UTF_8))
}
