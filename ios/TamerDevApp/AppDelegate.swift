import UIKit
import tamerlinking

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        LynxInitProcessor.shared.setupEnvironment()
        if let url = launchOptions?[.url] as? URL {
            let s = url.absoluteString
            LinkingModule.setInitialUrl(s)
            LinkingModule.onUrlReceived(s)
        }
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        let s = url.absoluteString
        LinkingModule.setInitialUrl(s)
        LinkingModule.onUrlReceived(s)
        return true
    }
}
