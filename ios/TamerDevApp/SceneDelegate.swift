import UIKit
import tamerdevclient

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = ViewController()
        window?.makeKeyAndVisible()

        // Handle URL from app launch (e.g., from QR scan in Apple Wallet)
        if let urlContext = connectionOptions.urlContexts.first {
            handleDeepLink(urlContext.url)
        }
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        // Handle URL when app is already running (e.g., from native scanner)
        if let urlContext = URLContexts.first {
            handleDeepLink(urlContext.url)
        }
    }

    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "tamerdevapp", let host = url.host, !host.isEmpty else { return }

        // Parse deep link: tamerdevapp://host:port/path → http://host:port/path
        var bundleUrl = "http://" + host
        if let port = url.port { bundleUrl += ":\(port)" }
        bundleUrl += url.path

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            DevClientModule.openProjectDirectHandler?(bundleUrl)
        }
    }
}
