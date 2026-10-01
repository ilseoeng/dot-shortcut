package app.dotshortcut.ui

import android.app.role.RoleManager
import android.content.pm.ShortcutManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.dotshortcut.R
import app.dotshortcut.core.AppSettings
import app.dotshortcut.launch.LaunchActivity
import app.dotshortcut.core.AssistAction
import app.dotshortcut.core.DotLauncher
import app.dotshortcut.core.Prefs
import app.dotshortcut.core.ShortcutKind
import app.dotshortcut.core.ConnectionTest
import app.dotshortcut.core.ConnectionTestStatus
import app.dotshortcut.core.canAddShortcut
import app.dotshortcut.a11y.DotCallAccessibilityService
import app.dotshortcut.launch.GuideActivity
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(prefs: Prefs) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by prefs.settings.collectAsState(initial = AppSettings())

    var chatGptInstalled by remember {
        mutableStateOf(DotLauncher.isChatGptInstalled(context.packageManager))
    }
    var roleHeld by remember { mutableStateOf(isAssistantRoleHeld(context)) }
    var showFailDialog by remember { mutableStateOf(false) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember {
        mutableStateOf(DotCallAccessibilityService.isServiceEnabled(context))
    }
    val testStatus by ConnectionTest.status.collectAsState()
    val shortcutEnabled = canAddShortcut(
        chatGptInstalled, accessibilityEnabled, settings.autoTestConfirmedAt > 0L, testStatus,
    )
    val startTest: () -> Unit = {
        scope.launch {
            accessibilityEnabled = DotCallAccessibilityService.isServiceEnabled(context)
            prefs.clearAutoTest()
            when {
                !chatGptInstalled -> showInstallDialog = true
                !accessibilityEnabled -> {
                    ConnectionTest.fail()
                    context.startActivity(GuideActivity.intent(context, GuideActivity.REASON_TEST_A11Y))
                }
                else -> {
                    ConnectionTest.start(context)
                    DotCallAccessibilityService.monitorConnectionTest()
                    if (!DotLauncher.openDotChat(context)) {
                        ConnectionTest.fail()
                        showFailDialog = true
                    }
                }
            }
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { roleHeld = isAssistantRoleHeld(context) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                chatGptInstalled = DotLauncher.isChatGptInstalled(context.packageManager)
                roleHeld = isAssistantRoleHeld(context)
                accessibilityEnabled = DotCallAccessibilityService.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.app_subtitle),
            fontSize = 14.sp,
            color = TextSecondary,
        )
        Spacer(Modifier.height(22.dp))

        ConnectionCard(
            installed = chatGptInstalled,
            settings = settings,
            testStatus = testStatus,
            accessibilityEnabled = accessibilityEnabled,
            onTest = startTest,
        )
        Spacer(Modifier.height(14.dp))

        ShortcutCard(
            selected = settings.shortcutKind,
            onSelect = { scope.launch { prefs.setShortcutKind(it) } },
        )
        Spacer(Modifier.height(14.dp))

        AssistCard(
            roleHeld = roleHeld,
            action = settings.assistAction,
            onRequestRole = { requestAssistantRole(context, roleLauncher::launch) },
            onActionSelect = { scope.launch { prefs.setAssistAction(it) } },
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { pinSelectedShortcut(context, settings.shortcutKind) },
            enabled = shortcutEnabled,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            Text(
                stringResource(R.string.btn_add_shortcut),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(if (shortcutEnabled) R.string.footer_note else R.string.shortcut_test_required),
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(30.dp))
    }

    if (showFailDialog) {
        AlertDialog(
            onDismissRequest = { showFailDialog = false },
            title = { Text(stringResource(R.string.dlg_fail_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.dlg_fail_intro), fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    listOf(
                        R.string.fail_reason_install,
                        R.string.fail_reason_login,
                        R.string.fail_reason_dot_setup,
                        R.string.fail_reason_version,
                    ).forEach { Text("· " + stringResource(it), fontSize = 13.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showFailDialog = false
                    startTest()
                }) { Text(stringResource(R.string.btn_retry)) }
            },
            dismissButton = {
                TextButton(onClick = { showFailDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text(stringResource(R.string.guide_install_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.guide_install_body), fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showInstallDialog = false
                    DotLauncher.openChatGptInstall(context)
                }) { Text(stringResource(R.string.btn_open_play_store)) }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}

@Composable
private fun ConnectionCard(
    installed: Boolean,
    settings: AppSettings,
    testStatus: ConnectionTestStatus,
    accessibilityEnabled: Boolean,
    onTest: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_dot_mark),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.card_connection_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        ConnectionChip(installed, settings, testStatus, accessibilityEnabled)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.card_connection_desc),
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onTest,
                enabled = testStatus != ConnectionTestStatus.RUNNING,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, TextPrimary),
            ) {
                Text(
                    stringResource(if (testStatus == ConnectionTestStatus.RUNNING) R.string.test_running else R.string.btn_connection_test),
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (testStatus == ConnectionTestStatus.RUNNING || testStatus == ConnectionTestStatus.FAILED) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(if (testStatus == ConnectionTestStatus.RUNNING) R.string.test_observing else R.string.test_unconfirmed),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ConnectionChip(installed: Boolean, settings: AppSettings,
                           testStatus: ConnectionTestStatus, accessibilityEnabled: Boolean) {
    val (text, warn) = when {
        !installed -> stringResource(R.string.status_not_installed) to true
        !accessibilityEnabled -> stringResource(R.string.status_a11y_needed) to true
        testStatus == ConnectionTestStatus.RUNNING -> stringResource(R.string.test_running) to true
        testStatus == ConnectionTestStatus.FAILED -> stringResource(R.string.status_need_retry) to true
        settings.autoTestConfirmedAt > 0L -> stringResource(R.string.status_auto_confirmed) to false
        else -> stringResource(R.string.status_need_check) to true
    }
    Text(
        text = text,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Medium,
        color = if (warn) ChipWarnFg else ChipOkFg,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (warn) ChipWarnBg else ChipOkBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun ShortcutCard(
    selected: ShortcutKind,
    onSelect: (ShortcutKind) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.card_shortcut_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutOption(
                    kind = ShortcutKind.CHAT,
                    label = stringResource(R.string.shortcut_kind_chat),
                    selected = selected == ShortcutKind.CHAT,
                    onSelect = onSelect,
                ) {
                    Image(painterResource(R.drawable.ic_chat), null, Modifier.size(32.dp))
                }
                ShortcutOption(
                    kind = ShortcutKind.CALL,
                    label = stringResource(R.string.shortcut_kind_call),
                    selected = selected == ShortcutKind.CALL,
                    onSelect = onSelect,
                ) {
                    Image(painterResource(R.drawable.ic_call), null, Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.ShortcutOption(
    kind: ShortcutKind,
    label: String,
    selected: Boolean,
    onSelect: (ShortcutKind) -> Unit,
    preview: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (kind == ShortcutKind.CALL) PeachTile else NeutralTile)
            .border(
                width = 1.5.dp,
                color = if (selected) Accent else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(kind) })
            .padding(vertical = 16.dp, horizontal = 6.dp),
    ) {
        Box(Modifier.height(40.dp), contentAlignment = Alignment.Center) { preview() }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun AssistCard(
    roleHeld: Boolean,
    action: AssistAction,
    onRequestRole: () -> Unit,
    onActionSelect: (AssistAction) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.card_assist_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineSoft, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onRequestRole)
                        .padding(vertical = 12.dp),
                ) {
                    Text(
                        stringResource(R.string.assist_set_row),
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (roleHeld) {
                        Text(
                            "설정됨",
                            fontSize = 12.sp,
                            color = ChipOkFg,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text("›", fontSize = 20.sp, color = TextSecondary)
                }

                Box(
                    Modifier.fillMaxWidth().height(1.dp).background(OutlineSoft),
                )

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { menuOpen = true }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(
                            stringResource(R.string.assist_action_row),
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            stringResource(
                                if (action == AssistAction.CALL) R.string.assist_action_call
                                else R.string.assist_action_chat,
                            ),
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        Text(" ▾", fontSize = 13.sp, color = TextSecondary)
                    }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.assist_action_chat)) },
                        onClick = {
                            onActionSelect(AssistAction.CHAT)
                            menuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.assist_action_call)) },
                        onClick = {
                            onActionSelect(AssistAction.CALL)
                            menuOpen = false
                        },
                    )
                }
            }
        }
        }
    }
}

private fun isAssistantRoleHeld(context: Context): Boolean =
    Build.VERSION.SDK_INT >= 29 &&
        context.getSystemService(RoleManager::class.java)
            ?.isRoleHeld(RoleManager.ROLE_ASSISTANT) == true

private fun requestAssistantRole(context: Context, launch: (Intent) -> Unit) {
    // 삼성(검증 기기 SM-S931N, API 36)에서는 ASSISTANT 역할 요청 Intent가
    // "Role is not requestable"로 즉시 종료되므로 기본 앱 설정으로 보낸다.
    val isSamsung = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    if (Build.VERSION.SDK_INT >= 29 && !isSamsung) {
        val rm = context.getSystemService(RoleManager::class.java)
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            launch(rm.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT))
            return
        }
    }
    openDefaultAppsSettings(context)
}

private fun openDefaultAppsSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(context, R.string.assist_settings_unavailable, Toast.LENGTH_LONG).show()
        }
    }
}

/** 선택된 종류를 런처 바로가기(일반 아이콘)로 홈 화면에 고정한다. */
private fun pinSelectedShortcut(context: Context, kind: ShortcutKind) {
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
    if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported) {
        showPinUnsupported(context)
        return
    }
    val (intent, icon, label) = when (kind) {
        ShortcutKind.CHAT -> Triple(
            LaunchActivity.chatIntent(context),
            R.drawable.ic_shortcut_chat,
            R.string.shortcut_chat,
        )
        ShortcutKind.CALL -> Triple(
            LaunchActivity.callIntent(context),
            R.drawable.ic_shortcut_call,
            R.string.shortcut_call,
        )
    }
    val shortcut = ShortcutInfoCompat.Builder(context, "shortcut_${kind.wire}")
        .setShortLabel(context.getString(label))
        .setIcon(IconCompat.createWithResource(context, icon))
        .setIntent(intent)
        .build()
    ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
}

private fun showPinUnsupported(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.shortcut_pin_unsupported),
        Toast.LENGTH_LONG,
    ).show()
}
