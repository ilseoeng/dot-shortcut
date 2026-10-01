package app.dotshortcut.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dot_shortcut")

enum class AssistAction(val wire: String) {
    CHAT("chat"),
    CALL("call"),
    ;

    companion object {
        fun of(v: String?): AssistAction = entries.firstOrNull { it.wire == v } ?: CHAT
    }
}

enum class ShortcutKind(val wire: String) {
    CHAT("chat"),
    CALL("call"),
    ;

    companion object {
        fun of(v: String?): ShortcutKind = entries.firstOrNull { it.wire == v } ?: CHAT
    }
}

data class AppSettings(
    val assistAction: AssistAction = AssistAction.CHAT,
    val shortcutKind: ShortcutKind = ShortcutKind.CHAT,
    /** 사용자가 "내 dot 채팅이 열렸어요"를 확인한 시각. 실시간 계정 상태가 아닌 테스트 기록. */
    val testConfirmedAt: Long = 0L,
    val testFailedAt: Long = 0L,
    /** Only a screen observation by the accessibility service unlocks shortcut pinning. */
    val autoTestConfirmedAt: Long = 0L,
)

class Prefs(private val context: Context) {

    private object K {
        val ASSIST_ACTION = stringPreferencesKey("assist_action")
        val WIDGET_KIND = stringPreferencesKey("widget_kind")
        val TEST_CONFIRMED_AT = longPreferencesKey("test_confirmed_at")
        val TEST_FAILED_AT = longPreferencesKey("test_failed_at")
        val AUTO_TEST_CONFIRMED_AT = longPreferencesKey("auto_test_confirmed_at")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            assistAction = AssistAction.of(p[K.ASSIST_ACTION]),
            shortcutKind = ShortcutKind.of(p[K.WIDGET_KIND]),
            testConfirmedAt = p[K.TEST_CONFIRMED_AT] ?: 0L,
            testFailedAt = p[K.TEST_FAILED_AT] ?: 0L,
            autoTestConfirmedAt = p[K.AUTO_TEST_CONFIRMED_AT] ?: 0L,
        )
    }

    suspend fun setAssistAction(action: AssistAction) {
        context.dataStore.edit { it[K.ASSIST_ACTION] = action.wire }
    }

    suspend fun setShortcutKind(kind: ShortcutKind) {
        context.dataStore.edit { it[K.WIDGET_KIND] = kind.wire }
    }

    suspend fun recordTestConfirmed() {
        context.dataStore.edit { it[K.TEST_CONFIRMED_AT] = System.currentTimeMillis() }
    }

    suspend fun recordTestFailed() {
        context.dataStore.edit { it[K.TEST_FAILED_AT] = System.currentTimeMillis() }
    }

    suspend fun clearAutoTest() {
        context.dataStore.edit { it.remove(K.AUTO_TEST_CONFIRMED_AT) }
    }

    suspend fun recordAutoTestConfirmed() {
        context.dataStore.edit { it[K.AUTO_TEST_CONFIRMED_AT] = System.currentTimeMillis() }
    }
}
