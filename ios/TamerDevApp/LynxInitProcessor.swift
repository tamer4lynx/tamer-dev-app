// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.

import Foundation

// GENERATED IMPORTS START
import tamerdevclient
import tamericons
import tamerinsets
import tamerrouter
import tamersystemui
import tamerwebview
// GENERATED IMPORTS END

final class LynxInitProcessor {
    static let shared = LynxInitProcessor()
    private init() {}

    func setupEnvironment() {
        TamerIconElement.registerFonts()
        setupLynxEnv()
        setupLynxService()
    }

    private func setupLynxEnv() {
        let env = LynxEnv.sharedInstance()
#if DEBUG
        env.lynxDebugEnabled = true
        env.devtoolEnabled = true
        env.logBoxEnabled = true
#endif
        let globalConfig = LynxConfig(provider: env.config.templateProvider)

        // GENERATED AUTOLINK START
        // Register module from package: @tamer4lynx/tamer-dev-client
        globalConfig.register(DevClientModule.self)

        // Register element from package: @tamer4lynx/tamer-icons
        globalConfig.registerUI(TamerIconElement.self, withName: "icon")

        globalConfig.registerUI(TamerWebViewElement.self, withName: "webview")

        // Register module from package: @tamer4lynx/tamer-insets
        globalConfig.register(TamerInsetsModule.self)

        // Register module from package: @tamer4lynx/tamer-router
        globalConfig.register(TamerRouterNativeModule.self)

        // Register module from package: @tamer4lynx/tamer-system-ui
        globalConfig.register(SystemUIModule.self)
// GENERATED AUTOLINK END

        // GENERATED DEV_CLIENT_SUPPORTED START
        DevClientModule.attachSupportedModuleClassNames([
            "com.nanofuxion.tamerdevclient.DevClientModule",
            "com.nanofuxion.tamerinsets.TamerInsetsModule",
            "com.nanofuxion.tamerrouter.TamerRouterNativeModule",
            "com.nanofuxion.tamersystemui.SystemUIModule"
        ])
// GENERATED DEV_CLIENT_SUPPORTED END

        env.prepareConfig(globalConfig)
    }

    private func setupLynxService() {
        let webPCoder = SDImageWebPCoder.shared
        SDImageCodersManager.shared.addCoder(webPCoder)
    }
}
