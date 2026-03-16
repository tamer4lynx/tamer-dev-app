import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        LynxInitProcessor.shared.setupEnvironment()

        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = DevLauncherViewController()
        window?.makeKeyAndVisible()
        return true
    }

    func application(_ app: UIApplication, open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        if url.scheme == "tamerdevapp", let host = url.host, host == "project" {
            presentProjectViewController()
        }
        return true
    }

    @objc func presentProjectViewController() {
        guard let root = window?.rootViewController else { return }
        let projectVC = ProjectViewController()
        projectVC.modalPresentationStyle = .fullScreen
        root.present(projectVC, animated: true)
    }
}
