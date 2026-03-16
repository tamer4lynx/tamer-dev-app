// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.

import Foundation

// GENERATED IMPORTS START
import tamerrouter
import tamerdisplaybrowser
import tamertextinput
import tamericons
import tamerinsets
import tamerdevclient
import tamersystemui
import tamerlinking
import tamerbiometric
import tamertransports
import tamersecurestore
import jiggle
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
        let globalConfig = LynxConfig(provider: env.config.templateProvider)

        // GENERATED AUTOLINK START
        // Register module from package: tamer-router
        globalConfig.register(TamerRouterNativeModule.self)

        // Register module from package: tamer-display-browser
        globalConfig.register(DisplayBrowserModule.self)

        // Register element from package: tamer-text-input
        globalConfig.registerUI(TamerTextInput.self, withName: "tamer-input")

        // Register element from package: tamer-icons
        globalConfig.registerUI(TamerIconElement.self, withName: "icon")

        // Register module from package: tamer-insets
        globalConfig.register(TamerInsetsModule.self)

        // Register module from package: tamer-dev-client
        globalConfig.register(DevClientModule.self)

        // Register module from package: tamer-system-ui
        globalConfig.register(SystemUIModule.self)

        // Register module from package: tamer-linking
        globalConfig.register(LinkingModule.self)

        // Register module from package: tamer-biometric
        globalConfig.register(BiometricModule.self)

        // Register module from package: tamer-transports
        globalConfig.register(LynxFetchModule.self)

        // Register module from package: tamer-transports
        globalConfig.register(LynxWebSocketModule.self)

        // Register module from package: tamer-secure-store
        globalConfig.register(SecureStoreModule.self)

        // Register module from package: jiggle
        globalConfig.register(JiggleModule.self)
// GENERATED AUTOLINK END

        env.prepareConfig(globalConfig)
    }

    private func setupLynxService() {
        let webPCoder = SDImageWebPCoder.shared
        SDImageCodersManager.shared.addCoder(webPCoder)
    }
}
