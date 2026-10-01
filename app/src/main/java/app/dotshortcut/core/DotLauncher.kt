package app.dotshortcut.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * dot 연결 경로를 관리하는 단일 라우팅 모듈.
 * 실기기 검증된 경로만 상수로 유지하고, 주소 변경 대응은 이 파일에서만 한다.
 */
object DotLauncher {

    const val CHATGPT_PACKAGE = "com.openai.chatgpt"

    /**
     * 실기기(SM-S931N, ChatGPT 1.2026.265)에서 기존 dot 채팅 도착을 확인한 경로.
     * 사용자별 dot ID를 포함하지 않아 선택된 계정의 dot으로 연결된다.
     */
    const val DOT_CHAT_URL = "https://chatgpt.com/app/o"

    private const val PLAY_STORE_MARKET = "market://details?id=$CHATGPT_PACKAGE"
    private const val PLAY_STORE_WEB =
        "https://play.google.com/store/apps/details?id=$CHATGPT_PACKAGE"

    fun isChatGptInstalled(pm: PackageManager): Boolean =
        try {
            pm.getPackageInfo(CHATGPT_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    /**
     * dot 채팅 진입을 시도한다.
     * @return Intent 전달 성공 여부. dot 채팅 도착을 보장하지 않으므로
     *         이 값만으로 "연결 완료"를 판정해서는 안 된다.
     */
    fun openDotChat(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DOT_CHAT_URL)).apply {
            setPackage(CHATGPT_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun openChatGptInstall(context: Context) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_MARKET)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(market)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
