package com.nanofuxion.tamerdevapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import com.nanofuxion.tamerdevapp.DevClientManager
import com.nanofuxion.tamerdevclient.TamerRelogLogService
import com.nanofuxion.tamerrouter.TamerRouterNativeModule
import com.nanofuxion.tamerinsets.TamerInsetsModule

class ProjectActivity : AppCompatActivity() {
    private var lynxView: LynxView? = null
    private var devClientManager: DevClientManager? = null

    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        lynxView = buildLynxView()
        setContentView(lynxView)
        TamerRouterNativeModule.attachHostView(lynxView)
        TamerInsetsModule.attachHostView(lynxView)
        lynxView?.renderTemplateUrl("main.lynx.bundle", "")
        devClientManager = DevClientManager(this) { reloadProjectView() }
        devClientManager?.connect()
        TamerRelogLogService.connect()

        listOf(150L, 400L, 800L).forEach { delay ->
            handler.postDelayed({ TamerInsetsModule.reRequestInsets() }, delay)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) TamerInsetsModule.reRequestInsets()
    }

    private fun reloadProjectView() {
        val oldView = lynxView
        TamerRouterNativeModule.attachHostView(null)
        TamerInsetsModule.attachHostView(null)
        oldView?.destroy()

        val nextView = buildLynxView()
        lynxView = nextView
        setContentView(nextView)
        TamerRouterNativeModule.attachHostView(nextView)
        TamerInsetsModule.attachHostView(nextView)
        nextView.renderTemplateUrl("main.lynx.bundle", "")
        listOf(150L, 400L, 800L).forEach { delay ->
            handler.postDelayed({ TamerInsetsModule.reRequestInsets() }, delay)
        }
    }

    override fun onResume() {
        super.onResume()
        lynxView?.let { TamerInsetsModule.reRequestInsets() }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) maybeClearFocusedInput(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun maybeClearFocusedInput(ev: MotionEvent) {
        val focused = currentFocus
        if (focused is EditText) {
            val loc = IntArray(2)
            focused.getLocationOnScreen(loc)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            if (x < loc[0] || x > loc[0] + focused.width || y < loc[1] || y > loc[1] + focused.height) {
                focused.clearFocus()
                (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.hideSoftInputFromWindow(focused.windowToken, 0)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        TamerRouterNativeModule.requestBack { consumed ->
            if (!consumed) {
                runOnUiThread { super.onBackPressed() }
            }
        }
    }

    override fun onDestroy() {
        TamerRouterNativeModule.attachHostView(null)
        TamerInsetsModule.attachHostView(null)
        lynxView?.destroy()
        lynxView = null
        devClientManager?.disconnect()
        TamerRelogLogService.disconnect()

        super.onDestroy()
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        viewBuilder.setTemplateProvider(TemplateProvider(this))
        return viewBuilder.build(this)
    }
}
