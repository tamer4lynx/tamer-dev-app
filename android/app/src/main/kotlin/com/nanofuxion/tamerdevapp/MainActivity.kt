package com.nanofuxion.tamerdevapp

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lynx.tasm.LynxBooleanOption
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator
import com.nanofuxion.tamerdevclient.DevClientModule
import com.nanofuxion.tamernavigation.stack.TamerNavHost

import com.nanofuxion.tamerdevapp.generated.GeneratedLynxExtensions
import com.nanofuxion.tamerdevapp.generated.GeneratedActivityLifecycle

class MainActivity : AppCompatActivity() {
    private var reloadReceiver: BroadcastReceiver? = null
    private val currentUri = "dev-client.lynx.bundle"
    private var pendingScanOnPermissionGranted: Runnable? = null
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingScanOnPermissionGranted?.run()
        pendingScanOnPermissionGranted = null
    }
    private val scanResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        scanResult?.contents?.let { DevClientModule.instance?.deliverScanResult(it) }
    }
    private var lynxView: LynxView? = null
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            GeneratedActivityLifecycle.onBackPressed { consumed ->
                if (!consumed) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedLynxExtensions.register(this)
        TamerNavHost.spokeBuilder = { ctx ->
            val viewBuilder = LynxViewBuilder()
            viewBuilder.setLynxGroup(TamerNavLynxRuntime.group)
            val provider = TemplateProvider(ctx)
            viewBuilder.setTemplateProvider(provider)
            viewBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
            viewBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
            viewBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
            GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
            viewBuilder.build(ctx)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Handle deep link from intent (e.g., from native scanner)
        handleDeepLink(intent)

        lynxView = buildLynxView()
        setContentView(lynxView)
        GeneratedActivityLifecycle.onViewAttached(lynxView)
        GeneratedLynxExtensions.onHostViewChanged(lynxView)
        lynxView?.renderTemplateUrl(currentUri, "")
        DevClientModule.attachHostActivity(this)
        DevClientModule.attachLynxView(lynxView)
        DevClientModule.attachCameraPermissionRequester { onGranted ->
            pendingScanOnPermissionGranted = onGranted
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        DevClientModule.attachScanLauncher {
            scanResultLauncher.launch(IntentIntegrator(this).setCaptureActivity(PortraitCaptureActivity::class.java).setPrompt("Scan dev server QR").createScanIntent())
        }
        DevClientModule.attachReloadProjectLauncher {
            startActivity(Intent(this@MainActivity, ProjectActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            ))
        }
        DevClientModule.attachOpenProjectDirectLauncher { bundleUrl ->
            startActivity(Intent(this@MainActivity, ProjectActivity::class.java).apply {
                putExtra("bundleUrl", bundleUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
        reloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == DevClientModule.ACTION_RELOAD_PROJECT) {
                    runOnUiThread {
                        startActivity(Intent(this@MainActivity, ProjectActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        ))
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(reloadReceiver, IntentFilter(DevClientModule.ACTION_RELOAD_PROJECT), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(reloadReceiver, IntentFilter(DevClientModule.ACTION_RELOAD_PROJECT))
        }
        onBackPressedDispatcher.addCallback(this, backCallback)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle deep link when app is already running
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "tamerdevapp") {
            // Parse deep link: tamerdevapp://host:port/path
            val host = data.host ?: return
            val port = data.port
            val path = data.path ?: ""
            val bundleUrl = if (port > 0) "http://$host:$port$path" else "http://$host$path"
            if (bundleUrl.isNotEmpty()) {
                startActivity(Intent(this@MainActivity, ProjectActivity::class.java).apply {
                    putExtra("bundleUrl", bundleUrl)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        GeneratedActivityLifecycle.onPause()
    }

    override fun onResume() {
        super.onResume()
        GeneratedActivityLifecycle.onResume()
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        viewBuilder.setLynxGroup(TamerNavLynxRuntime.group)
        val provider = TemplateProvider(this)
        viewBuilder.setTemplateProvider(provider)
        viewBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
        viewBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
        viewBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
        GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
        return viewBuilder.build(this)
    }
    override fun onDestroy() {
        reloadReceiver?.let { unregisterReceiver(it) }
        GeneratedActivityLifecycle.onViewDetached()
        GeneratedLynxExtensions.onHostViewChanged(null)
        lynxView?.destroy()
        lynxView = null
        DevClientModule.attachReloadProjectLauncher(null)
        DevClientModule.attachOpenProjectDirectLauncher(null)
        DevClientModule.attachHostActivity(null)
        DevClientModule.attachLynxView(null)
        super.onDestroy()
    }

}
