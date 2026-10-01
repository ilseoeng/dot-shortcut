package app.dotshortcut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.dotshortcut.core.Prefs
import app.dotshortcut.ui.DotShortcutTheme
import app.dotshortcut.ui.SettingsScreen
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.dotshortcut.launch.LaunchActivity

class MainActivity : ComponentActivity() {

    private val prefs by lazy { Prefs(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Call shortcuts carry a private capability instead of a spoofable public action.
        ShortcutManagerCompat.addDynamicShortcuts(this, listOf(
            ShortcutInfoCompat.Builder(this, "call")
                .setShortLabel(getString(R.string.shortcut_call))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_call))
                .setIntent(LaunchActivity.callIntent(this))
                .build(),
        ))
        setContent {
            DotShortcutTheme {
                SettingsScreen(prefs = prefs)
            }
        }
    }
}
