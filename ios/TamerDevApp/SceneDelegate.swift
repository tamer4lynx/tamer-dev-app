import UIKit
import tamerlinking

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        if let url = connectionOptions.urlContexts.first?.url {
            let s = url.absoluteString
            LinkingModule.setInitialUrl(s)
            LinkingModule.onUrlReceived(s)
        }
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = ViewController()
        window?.makeKeyAndVisible()
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        guard let url = URLContexts.first?.url else { return }
        let s = url.absoluteString
        LinkingModule.setInitialUrl(s)
        LinkingModule.onUrlReceived(s)
    }
}
