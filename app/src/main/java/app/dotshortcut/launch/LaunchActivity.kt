package app.dotshortcut.launch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.app.KeyguardManager
import android.widget.Toast
import java.util.UUID
import app.dotshortcut.core.CallAuthorization
import app.dotshortcut.R
import androidx.activity.ComponentActivity
import app.dotshortcut.a11y.DotCallAccessibilityService
import app.dotshortcut.core.DotLauncher

/**
 * 위젯·어시스턴트의 탭을 실제 목적 동작으로 연결하는 트램펄린.
 * 성공하면 설정 화면을 거치지 않고 즉시 종료하고, 실패할 때만 안내 화면을 연다.
 */
class LaunchActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "app.dotshortcut.extra.MODE"
        const val MODE_CHAT = "chat"
        const val MODE_CALL = "call"
        private const val EXTRA_CALL_TOKEN = "app.dotshortcut.extra.CALL_TOKEN"

        private fun callToken(context: Context): String {
            val storage = context.getSharedPreferences("call_authorization", Context.MODE_PRIVATE)
            return storage.getString("token", null) ?: UUID.randomUUID().toString().also {
                storage.edit().putString("token", it).commit()
            }
        }
        const val ACTION_LAUNCH_CHAT = "app.dotshortcut.LAUNCH_CHAT"
        const val ACTION_LAUNCH_CALL = "app.dotshortcut.LAUNCH_CALL"

        fun chatIntent(context: Context): Intent =
            Intent(context, LaunchActivity::class.java)
                .setAction(ACTION_LAUNCH_CHAT)
                .putExtra(EXTRA_MODE, MODE_CHAT)

        fun callIntent(context: Context): Intent =
            Intent(context, LaunchActivity::class.java)
                .setAction(ACTION_LAUNCH_CALL)
                .putExtra(EXTRA_MODE, MODE_CALL)
                .putExtra(EXTRA_CALL_TOKEN, callToken(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra(EXTRA_MODE) ?: when (intent.action) {
            ACTION_LAUNCH_CALL -> MODE_CALL
            else -> MODE_CHAT
        }
        if (mode == MODE_CALL && (
            getSystemService(KeyguardManager::class.java).isKeyguardLocked ||
            !CallAuthorization.matches(callToken(this), intent.getStringExtra(EXTRA_CALL_TOKEN))
        )) {
            Toast.makeText(this, R.string.call_shortcut_invalid, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        when (mode) {
            MODE_CALL -> when {
                !DotLauncher.isChatGptInstalled(packageManager) ->
                    startActivity(GuideActivity.intent(this, GuideActivity.REASON_NOT_INSTALLED))
                // 접근성 서비스가 켜져 있으면 dot 채팅을 열고 '통화' 버튼 자동 선택을 대기시킨다
                DotCallAccessibilityService.isServiceEnabled(this) -> {
                    DotCallAccessibilityService.requestCall()
                    if (!DotLauncher.openDotChat(this)) {
                        startActivity(
                            GuideActivity.intent(this, GuideActivity.REASON_LAUNCH_FAILED),
                        )
                    }
                }
                else -> startActivity(
                    GuideActivity.intent(this, GuideActivity.REASON_CALL_A11Y),
                )
            }

            else -> {
                if (!DotLauncher.isChatGptInstalled(packageManager)) {
                    startActivity(GuideActivity.intent(this, GuideActivity.REASON_NOT_INSTALLED))
                } else if (!DotLauncher.openDotChat(this)) {
                    startActivity(GuideActivity.intent(this, GuideActivity.REASON_LAUNCH_FAILED))
                }
            }
        }
        finish()
    }
}
