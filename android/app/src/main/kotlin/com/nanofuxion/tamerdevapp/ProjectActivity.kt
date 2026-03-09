package com.nanofuxion.tamerdevapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.core.view.ViewCompat
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import com.nanofuxion.tamerdevapp.DevClientManager

class ProjectActivity : AppCompatActivity() {
    private var lynxView: LynxView? = null
    private var devClientManager: DevClientManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        lynxView = buildLynxView()
        setContentView(lynxView)
        ViewCompat.setOnApplyWindowInsetsListener(lynxView!!) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
        lynxView?.renderTemplateUrl("main.lynx.bundle", "")
        devClientManager = DevClientManager(this) { lynxView?.renderTemplateUrl("main.lynx.bundle", "") }
        devClientManager?.connect()

    }

    override fun onDestroy() {
        lynxView?.destroy()
        lynxView = null
        devClientManager?.disconnect()

        super.onDestroy()
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        viewBuilder.setTemplateProvider(TemplateProvider(this))
        return viewBuilder.build(this)
    }
}
