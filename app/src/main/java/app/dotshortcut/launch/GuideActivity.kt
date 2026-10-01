package app.dotshortcut.launch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ComponentName
import android.provider.Settings
import app.dotshortcut.R
import app.dotshortcut.core.DotLauncher
import app.dotshortcut.ui.DotShortcutTheme

/** 바로가기 실행에 필요한 설정 또는 연결 실패를 안내한다. */
class GuideActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_REASON = "app.dotshortcut.extra.REASON"
        const val REASON_CALL_PENDING = "call_pending"
        const val REASON_CALL_A11Y = "call_a11y"
        const val REASON_TEST_A11Y = "test_a11y"
        const val REASON_NOT_INSTALLED = "not_installed"
        const val REASON_LAUNCH_FAILED = "launch_failed"

        fun intent(context: Context, reason: String): Intent =
            Intent(context, GuideActivity::class.java).putExtra(EXTRA_REASON, reason)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reason = intent.getStringExtra(EXTRA_REASON) ?: REASON_LAUNCH_FAILED
        setContent {
            DotShortcutTheme {
                GuideOverlay(reason = reason, onClose = { finish() })
            }
        }
    }

    @Composable
    private fun GuideOverlay(reason: String, onClose: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(onClick = onClose)
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .clickable(onClick = {})
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                ) {
                    Text(
                        text = stringResource(titleRes(reason)),
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(bodyRes(reason)),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (reason == REASON_CALL_A11Y || reason == REASON_TEST_A11Y) {
                        Spacer(Modifier.height(20.dp))
                        listOf(
                            R.string.a11y_step_apps,
                            R.string.a11y_step_dot,
                            R.string.a11y_step_enable,
                        ).forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. " + stringResource(step),
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(if (reason == REASON_TEST_A11Y) R.string.test_a11y_return_note else R.string.a11y_return_note),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { onPrimary(reason) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                stringResource(primaryRes(reason)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { onSecondary(reason) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text(
                                stringResource(secondaryRes(reason)),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }
            }
        }
    }

    private fun onPrimary(reason: String) {
        when (reason) {
            REASON_CALL_A11Y, REASON_TEST_A11Y -> openA11ySettings()
            REASON_NOT_INSTALLED -> DotLauncher.openChatGptInstall(this)
            else -> {
                if (DotLauncher.isChatGptInstalled(packageManager)) {
                    DotLauncher.openDotChat(this)
                } else {
                    DotLauncher.openChatGptInstall(this)
                }
            }
        }
        finish()
    }

    /** 우리 서비스의 접근성 상세 화면을 바로 열고, 안 되면 목록으로 간다. */
    private fun openA11ySettings() {
        // ACTION_ACCESSIBILITY_DETAILS_SETTINGS + EXTRA_ACCESSIBILITY_COMPONENT_NAME (API 31+)
        val detail = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
            putExtra(
                "android.provider.extra.ACCESSIBILITY_COMPONENT_NAME",
                ComponentName(
                    this@GuideActivity,
                    app.dotshortcut.a11y.DotCallAccessibilityService::class.java,
                ).flattenToString(),
            )
        }
        try {
            startActivity(detail)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun onSecondary(reason: String) {
        when (reason) {
            // 접근성 없이 통화는 불가 — 채팅까지만 열어준다
            REASON_CALL_A11Y -> {
                if (DotLauncher.isChatGptInstalled(packageManager)) {
                    DotLauncher.openDotChat(this)
                }
            }
            else -> Unit
        }
        finish()
    }

    private fun titleRes(reason: String) = when (reason) {
        REASON_TEST_A11Y -> R.string.test_a11y_title
        REASON_CALL_PENDING -> R.string.guide_call_title
        REASON_CALL_A11Y -> R.string.guide_call_a11y_title
        REASON_NOT_INSTALLED -> R.string.guide_install_title
        else -> R.string.guide_failed_title
    }

    private fun bodyRes(reason: String) = when (reason) {
        REASON_TEST_A11Y -> R.string.test_a11y_body
        REASON_CALL_PENDING -> R.string.guide_call_body
        REASON_CALL_A11Y -> R.string.guide_call_a11y_body
        REASON_NOT_INSTALLED -> R.string.guide_install_body
        else -> R.string.guide_failed_body
    }

    private fun primaryRes(reason: String) = when (reason) {
        REASON_CALL_A11Y, REASON_TEST_A11Y -> R.string.btn_open_a11y_settings
        REASON_NOT_INSTALLED -> R.string.btn_open_play_store
        else -> R.string.guide_call_open_chat
    }

    private fun secondaryRes(reason: String) = when (reason) {
        REASON_CALL_A11Y -> R.string.guide_call_chat_only
        else -> R.string.close
    }
}
