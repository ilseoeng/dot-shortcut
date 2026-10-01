package app.dotshortcut.a11y

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import app.dotshortcut.core.DotLauncher
import app.dotshortcut.core.ConnectionTest
import app.dotshortcut.core.ConnectionTestStatus
import java.util.ArrayDeque

/**
 * dot 통화 버튼 자동 선택 (실험).
 *
 * 외부 통화 시작 경로가 없어, dot 채팅을 연 뒤 화면의 '통화' 버튼을
 * 대신 누른다. ChatGPT UI 구조에 의존하므로 앱 업데이트로 깨질 수 있다.
 */
class DotCallAccessibilityService : AccessibilityService() {
    private val testHandler = Handler(Looper.getMainLooper())
    private val testScan = object : Runnable {
        override fun run() {
            if (ConnectionTest.status.value != ConnectionTestStatus.RUNNING) return
            observeConnection()
            if (ConnectionTest.status.value == ConnectionTestStatus.RUNNING) {
                testHandler.postDelayed(this, 400L)
            }
        }
    }

    private fun monitorConnection() {
        testHandler.removeCallbacks(testScan)
        testHandler.post(testScan)
    }

    private fun observeConnection() {
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != DotLauncher.CHATGPT_PACKAGE) return
        ConnectionTest.observe(
            this,
            true,
            hasDotMarker(root),
            findCallButton(root) != null,
        )
    }

    companion object {
        private const val TAG = "DotCallA11y"
        private const val PENDING_TIMEOUT_MS = 15_000L

        /** dot 화면 식별용: 시트 루트의 content-desc */
        private const val DOT_MARKER = "dot"

        /** '통화 시작' 버튼 후보 content-desc (로케일별) */
        private val CALL_DESCS = setOf("통화", "Start call", "Start Call")

        @Volatile
        private var pendingUntil: Long = 0L
        private var instance: DotCallAccessibilityService? = null

        fun monitorConnectionTest() {
            pendingUntil = 0L
            instance?.monitorConnection()
        }

        /** 통화 버튼 자동 클릭을 [PENDING_TIMEOUT_MS] 동안 대기시킨다. */
        fun requestCall() {
            pendingUntil = System.currentTimeMillis() + PENDING_TIMEOUT_MS
        }

        fun isServiceEnabled(context: Context): Boolean {
            val enabledOn = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0,
            ) == 1
            if (!enabledOn) return false
            val enabled = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val expected = ComponentName(context, DotCallAccessibilityService::class.java)
                .flattenToString()
            val short = ComponentName(context, DotCallAccessibilityService::class.java)
                .flattenToShortString()
            return enabled.split(':').any {
                it.equals(expected, ignoreCase = true) ||
                    it.equals(short, ignoreCase = true)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        if (ConnectionTest.status.value == ConnectionTestStatus.RUNNING) monitorConnection()
    }

    override fun onDestroy() {
        testHandler.removeCallbacks(testScan)
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        pendingUntil = 0L
        testHandler.removeCallbacks(testScan)
        if (instance === this) instance = null
        if (ConnectionTest.status.value == ConnectionTestStatus.RUNNING) ConnectionTest.fail()
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        pendingUntil = 0L
        if (ConnectionTest.status.value == ConnectionTestStatus.RUNNING) ConnectionTest.fail()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (ConnectionTest.status.value == ConnectionTestStatus.RUNNING &&
            event.packageName == DotLauncher.CHATGPT_PACKAGE) {
            observeConnection()
        }
        val deadline = pendingUntil
        if (deadline == 0L) return
        if (System.currentTimeMillis() > deadline) {
            pendingUntil = 0L
            return
        }
        if (event.packageName != DotLauncher.CHATGPT_PACKAGE) return
        val root = rootInActiveWindow ?: return
        if (root.packageName != DotLauncher.CHATGPT_PACKAGE) return
        if (!hasDotMarker(root)) return
        val button = findCallButton(root) ?: return
        if (button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            Log.i(TAG, "dot call button clicked")
        }
        pendingUntil = 0L
    }

    private fun hasDotMarker(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var hops = 0
        while (queue.isNotEmpty() && hops < 400) {
            hops++
            val node = queue.removeFirst()
            if (node.contentDescription?.toString() == DOT_MARKER) return true
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let(queue::add)
            }
        }
        return false
    }

    private fun findCallButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var hops = 0
        while (queue.isNotEmpty() && hops < 400) {
            hops++
            val node = queue.removeFirst()
            val desc = node.contentDescription?.toString()
            if (desc in CALL_DESCS && node.isClickable) return node
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let(queue::add)
            }
        }
        return null
    }
}
