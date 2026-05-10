package com.nanofuxion.tamerdevapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import com.lynx.tasm.LynxBooleanOption
import com.nanofuxion.tamerdevclient.DevClientModule
import com.nanofuxion.tamerrouter.TamerRouterNativeModule
import org.json.JSONObject
import com.nanofuxion.tamerdevapp.generated.GeneratedLynxExtensions
import com.nanofuxion.tamerdevapp.generated.GeneratedActivityLifecycle

class LynxPushActivity : AppCompatActivity() {
    private var lynxView: LynxView? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedLynxExtensions.register(this)
        GeneratedActivityLifecycle.onCreate(intent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        val initDataJson = intent.getStringExtra(EXTRA_INIT_DATA) ?: ""
        val launchUrl = intent.getStringExtra(EXTRA_LAUNCH_URL)
        if (!launchUrl.isNullOrBlank()) {
            com.nanofuxion.tamerlinking.LinkingModule.setInitialUrl(launchUrl)
        }
        val bundleUrl = resolveBundleUrl(initDataJson)
        lynxView = buildLynxView()
        setContentView(lynxView)
        GeneratedActivityLifecycle.onViewAttached(lynxView)
        GeneratedLynxExtensions.onHostViewChanged(lynxView)
        lynxView?.renderTemplateUrl(bundleUrl, initDataJson)
        TamerRouterNativeModule.attachHostView(lynxView)
        DevClientModule.attachHostActivity(this)
        DevClientModule.attachLynxView(lynxView)
        GeneratedActivityLifecycle.onCreateDelayed(handler)
    }

    override fun onResume() {
        super.onResume()
        GeneratedActivityLifecycle.onResume()
        DevClientModule.attachHostActivity(this)
        DevClientModule.attachLynxView(lynxView)
        GeneratedLynxExtensions.onHostViewChanged(lynxView)
    }

    override fun onPause() {
        super.onPause()
        GeneratedActivityLifecycle.onPause()
    }

    override fun onDestroy() {
        GeneratedActivityLifecycle.onViewDetached()
        GeneratedLynxExtensions.onHostViewChanged(null)
        TamerRouterNativeModule.attachHostView(null)
        lynxView?.destroy()
        lynxView = null
        DevClientModule.attachHostActivity(null)
        DevClientModule.attachLynxView(null)
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.tamer_stack_pop_enter, R.anim.tamer_stack_pop_exit)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        GeneratedActivityLifecycle.onBackPressed { consumed ->
            if (!consumed) {
                runOnUiThread { super.onBackPressed() }
            }
        }
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        TamerNavLynxRuntime.configureBuilder(this, viewBuilder, "main.lynx.bundle")
        GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
        return viewBuilder.build(this)
    }

    private fun resolveBundleUrl(initDataJson: String): String {
        val fromExtra = intent.getStringExtra(EXTRA_BUNDLE)
        if (!fromExtra.isNullOrBlank()) return fromExtra
        if (initDataJson.isNotBlank()) {
            try {
                val u = JSONObject(initDataJson).optString("bundleUrl")
                if (u.isNotBlank()) return u
            } catch (_: Exception) { }
        }
        return BUNDLE_DEV_CLIENT
    }

    companion object {
        const val EXTRA_INIT_DATA = "tamer_init_data_json"
        const val EXTRA_LAUNCH_URL = "tamer_launch_url"
        const val EXTRA_BUNDLE = "tamer_bundle_url"
        private const val BUNDLE_DEV_CLIENT = "dev-client.lynx.bundle"
    }
}
