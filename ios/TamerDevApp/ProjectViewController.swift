import UIKit
import Lynx
import tamerdevclient
import tamerinsets
import tamernavigation
import tamerrouter

/// Shared with TamerNav stack spokes (`TamerNavHost.applySpokeBuilder`); required for one JS context group.
private enum TamerNavLynxRuntime {
    static let sharedGroup: LynxGroup = {
        let option = LynxGroupOption()
        return LynxGroup(name: "TamerNav", with: option)
    }()
}

private func tamer_project_disableLynxLongPressMenuIfAvailable() {
    guard let cls = NSClassFromString("LynxDevtoolEnv") else { return }
    let sel = NSSelectorFromString("sharedInstance")
    guard let env = (cls as AnyObject).perform(sel)?.takeUnretainedValue() as? NSObject else { return }
    env.setValue(false, forKey: "longPressMenuEnabled")
}

class ProjectViewController: UIViewController {
    private var lynxView: LynxView?
    private var devMenuView: LynxView?
    private var devClientManager: DevClientManager?
    private var previousReloadProjectHandler: (() -> Void)?
    private var previousDismissTamerDebugPanelHandler: (() -> Void)?
    private var hasTriggeredInitialProjectLoad = false
    private var pendingInitialLoadWorkItem: DispatchWorkItem?
    var bundleUrl: String?
    var onDismiss: (() -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
#if DEBUG
        let env = LynxEnv.sharedInstance()
        env.lynxDebugEnabled = true
        env.devtoolEnabled = true
        env.logBoxEnabled = true
#endif
        tamer_project_disableLynxLongPressMenuIfAvailable()
        view.backgroundColor = .black
        edgesForExtendedLayout = .all
        extendedLayoutIncludesOpaqueBars = true
        additionalSafeAreaInsets = .zero
        view.insetsLayoutMarginsFromSafeArea = false
        view.preservesSuperviewLayoutMargins = false
        viewRespectsSystemMinimumLayoutMargins = false
        setupLynxView()
        devClientManager = DevClientManager(bundleUrl: bundleUrl, onReload: { [weak self] in
            self?.reloadLynxView()
        })
        devClientManager?.connect()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        previousReloadProjectHandler = DevClientModule.reloadProjectHandler
        DevClientModule.reloadProjectHandler = { [weak self] in
            self?.dismissProjectDevMenu()
            self?.reloadLynxView()
        }
        previousDismissTamerDebugPanelHandler = DevClientModule.dismissTamerDebugPanelHandler
        DevClientModule.dismissTamerDebugPanelHandler = { [weak self] in
            self?.dismissProjectDevMenu()
        }
    }

    override var canBecomeFirstResponder: Bool { true }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        _ = becomeFirstResponder()
        triggerInitialProjectLoadIfNeeded(reason: "viewDidAppear")
    }

    override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        ShakeDetector.handleMotionEnded(motion) { [weak self] in
            self?.showProjectDevMenu()
        }
        super.motionEnded(motion, with: event)
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if let lynxView = lynxView {
            applyFullscreenLayout(to: lynxView)
        }
        if let devMenuView = devMenuView {
            applyFullscreenLayout(to: devMenuView)
        }
        triggerInitialProjectLoadIfNeeded(reason: "viewDidLayoutSubviews")
    }

    override func viewSafeAreaInsetsDidChange() {
        super.viewSafeAreaInsetsDidChange()
        TamerInsetsModule.reRequestInsets()
        triggerInitialProjectLoadIfNeeded(reason: "viewSafeAreaInsetsDidChange")
    }

    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }

    private func buildLynxView() -> LynxView {
        let size = fullscreenBounds().size
        let lv = LynxView { builder in
            let provider = DevTemplateProvider()
            builder.group = TamerNavLynxRuntime.sharedGroup
            builder.config = LynxConfig(provider: provider)
            builder.templateResourceFetcher = provider
            builder.genericResourceFetcher = provider
            builder.screenSize = size
            builder.fontScale = 1.0
        }
        lv.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        lv.insetsLayoutMarginsFromSafeArea = false
        lv.preservesSuperviewLayoutMargins = false
        applyFullscreenLayout(to: lv)
        return lv
    }

    private func setupLynxView() {
        let lv = buildLynxView()
        lv.backgroundColor = .black
        lv.isHidden = false
        lv.alpha = 1
        lv.isUserInteractionEnabled = true
        view.addSubview(lv)
        TamerInsetsModule.attachHostView(lv)
        TamerNavHost.attachRoot(lv, presenter: self)
        TamerRouterNativeModule.attachHostView(lv)
        pendingInitialLoadWorkItem?.cancel()
        pendingInitialLoadWorkItem = nil
        hasTriggeredInitialProjectLoad = false
        self.lynxView = lv
    }

    private func reloadLynxView() {
        dismissProjectDevMenu()
        pendingInitialLoadWorkItem?.cancel()
        pendingInitialLoadWorkItem = nil
        lynxView?.removeFromSuperview()
        lynxView = nil
        setupLynxView()
        triggerInitialProjectLoadIfNeeded(reason: "reloadLynxView")
    }

    private func triggerInitialProjectLoadIfNeeded(reason: String) {
        guard !hasTriggeredInitialProjectLoad else { return }
        guard isViewLoaded, view.window != nil else {
            NSLog("[ProjectVC] initial load waiting reason=%@ window=%@", reason, view.window != nil ? "attached" : "nil")
            return
        }
        let bounds = fullscreenBounds()
        guard bounds.width > 0, bounds.height > 0 else {
            NSLog("[ProjectVC] initial load deferred reason=%@ bounds=%@", reason, NSCoder.string(for: bounds))
            pendingInitialLoadWorkItem?.cancel()
            let workItem = DispatchWorkItem { [weak self] in
                self?.triggerInitialProjectLoadIfNeeded(reason: "\(reason)-retry")
            }
            pendingInitialLoadWorkItem = workItem
            DispatchQueue.main.async(execute: workItem)
            return
        }
        guard let lynxView else { return }
        hasTriggeredInitialProjectLoad = true
        pendingInitialLoadWorkItem?.cancel()
        pendingInitialLoadWorkItem = nil
        applyFullscreenLayout(to: lynxView)
        NSLog("[ProjectVC] initial project load reason=%@ bounds=%@ safe=%@", reason, NSCoder.string(for: bounds), NSCoder.string(for: view.safeAreaInsets))
        lynxView.loadTemplate(fromURL: "main.lynx.bundle", initData: nil)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { [weak self, weak lynxView] in
            guard let self, let lynxView else { return }
            self.logViewport("project post-load", lynxView: lynxView)
            self.applyFullscreenLayout(to: lynxView)
        }
    }

    private func buildDevMenuLynxView() -> LynxView {
        let size = fullscreenBounds().size
        let lv = LynxView { builder in
            let provider = DevTemplateProvider()
            builder.config = LynxConfig(provider: provider)
            builder.templateResourceFetcher = provider
            builder.genericResourceFetcher = provider
            builder.screenSize = size
            builder.fontScale = 1.0
        }
        lv.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        lv.insetsLayoutMarginsFromSafeArea = false
        lv.preservesSuperviewLayoutMargins = false
        lv.backgroundColor = .clear
        applyFullscreenLayout(to: lv)
        return lv
    }

    private func showProjectDevMenu() {
        guard devMenuView == nil else { return }
        let lv = buildDevMenuLynxView()
        view.addSubview(lv)
        lv.loadTemplate(fromURL: "tamer-debug.lynx.bundle", initData: nil)
        devMenuView = lv
    }

    private func dismissProjectDevMenu() {
        devMenuView?.removeFromSuperview()
        devMenuView = nil
    }

    private func applyFullscreenLayout(to lynxView: LynxView) {
        let bounds = fullscreenBounds()
        let size = bounds.size
        lynxView.frame = bounds
        lynxView.updateScreenMetrics(withWidth: size.width, height: size.height)
        lynxView.updateViewport(withPreferredLayoutWidth: size.width, preferredLayoutHeight: size.height, needLayout: true)
        lynxView.preferredLayoutWidth = size.width
        lynxView.preferredLayoutHeight = size.height
        lynxView.layoutWidthMode = .exact
        lynxView.layoutHeightMode = .exact
        logViewport("project apply", lynxView: lynxView)
    }

    private func fullscreenBounds() -> CGRect {
        let bounds = view.bounds
        if bounds.width > 0, bounds.height > 0 {
            return bounds
        }
        return UIScreen.main.bounds
    }

    private func logViewport(_ label: String, lynxView: LynxView) {
        let rootWidth = lynxView.rootWidth()
        let rootHeight = lynxView.rootHeight()
        let intrinsic = lynxView.intrinsicContentSize
        NSLog("[ProjectVC] %@ view=%@ safe=%@ lynxFrame=%@ lynxBounds=%@ root=%0.2fx%0.2f intrinsic=%@", label, NSCoder.string(for: view.bounds), NSCoder.string(for: view.safeAreaInsets), NSCoder.string(for: lynxView.frame), NSCoder.string(for: lynxView.bounds), rootWidth, rootHeight, NSCoder.string(for: intrinsic))
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isBeingDismissed || isMovingFromParent {
            DevClientModule.reloadProjectHandler = previousReloadProjectHandler
            DevClientModule.dismissTamerDebugPanelHandler = previousDismissTamerDebugPanelHandler
            devClientManager?.disconnect()
        }
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        guard isBeingDismissed || isMovingFromParent else { return }
        pendingInitialLoadWorkItem?.cancel()
        pendingInitialLoadWorkItem = nil
        onDismiss?()
    }
}
