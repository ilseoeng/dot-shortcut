package app.dotshortcut.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.view.View
import androidx.activity.ComponentActivity
import app.dotshortcut.core.AssistAction
import app.dotshortcut.core.Prefs
import app.dotshortcut.launch.LaunchActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 기본 디지털 어시스턴트 역할용 VoiceInteractionService.
 * 상시 호출어 인식은 구현하지 않고, 기기의 어시스턴트 호출(버튼 길게 누르기·제스처)만 받는다.
 */
class DotAssistService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
    }
}

class DotAssistSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession =
        DotAssistSession(this)
}

/** 호출 시 UI 없이 설정된 동작만 실행하고 바로 종료하는 세션. */
class DotAssistSession(context: Context) : VoiceInteractionSession(context) {

    override fun onCreateContentView(): View? = null

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        val action = runBlocking { Prefs(context).settings.first().assistAction }
        val target = when (action) {
            // 통화 직접 진입이 검증되기 전까지 CALL은 안내 경로로 보낸다
            AssistAction.CALL -> LaunchActivity.callIntent(context)
            AssistAction.CHAT -> LaunchActivity.chatIntent(context)
        }
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(target)
        } catch (_: Exception) {
        }
        finish()
    }
}

/**
 * 일부 기기·Android 버전이 VoiceInteractionService 대신 ACTION_ASSIST를
 * 어시스턴트 앱의 Activity로 보내는 경우를 위한 폴백 진입점.
 */
class AssistEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = runBlocking { Prefs(this@AssistEntryActivity).settings.first().assistAction }
        val target = when (action) {
            AssistAction.CALL -> LaunchActivity.callIntent(this)
            AssistAction.CHAT -> LaunchActivity.chatIntent(this)
        }
        startActivity(target)
        finish()
    }
}
