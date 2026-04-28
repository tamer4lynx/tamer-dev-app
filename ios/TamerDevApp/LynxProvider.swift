import Foundation
import Lynx

class LynxProvider: NSObject, LynxTemplateProvider, LynxTemplateResourceFetcher, LynxGenericResourceFetcher {
    func loadTemplate(withUrl url: String!, onComplete callback: LynxTemplateLoadBlock!) {
        DispatchQueue.global(qos: .background).async {
            let result = self.loadData(url: url)
            callback?(result.data, result.error)
        }
    }

    func fetchTemplate(_ request: LynxResourceRequest, onComplete callback: @escaping LynxTemplateResourceCompletionBlock) {
        DispatchQueue.global(qos: .background).async {
            let result = self.loadData(url: request.url)
            callback(result.data.map { LynxTemplateResource(nsData: $0) }, result.error)
        }
    }

    func fetchSSRData(_ request: LynxResourceRequest, onComplete callback: @escaping LynxSSRResourceCompletionBlock) {
        DispatchQueue.global(qos: .background).async {
            let result = self.loadData(url: request.url)
            callback(result.data, result.error)
        }
    }

    func fetchResource(_ request: LynxResourceRequest, onComplete callback: @escaping LynxGenericResourceCompletionBlock) -> (() -> Void) {
        DispatchQueue.global(qos: .background).async {
            let result = self.loadData(url: request.url)
            callback(result.data, result.error)
        }
        return {}
    }

    func fetchResourcePath(_ request: LynxResourceRequest, onComplete callback: @escaping LynxGenericResourcePathCompletionBlock) -> (() -> Void) {
        let error = NSError(domain: "LynxProvider", code: 501,
                            userInfo: [NSLocalizedDescriptionKey: "Resource path lookup is not supported"])
        callback(nil, error)
        return {}
    }

    private func loadData(url: String?) -> (data: Data?, error: NSError?) {
        guard let normalized = normalizeBundlePath(url),
              let bundleUrl = Bundle.main.url(forResource: normalized, withExtension: nil),
              let data = try? Data(contentsOf: bundleUrl) else {
            return (nil, NSError(domain: "LynxProvider", code: 404,
                                 userInfo: [NSLocalizedDescriptionKey: "Bundle not found: \(url ?? "nil")"]))
        }
        return (data, nil)
    }

    private func normalizeBundlePath(_ url: String?) -> String? {
        guard var s = url?.trimmingCharacters(in: .whitespacesAndNewlines), !s.isEmpty else { return nil }
        if let query = s.firstIndex(of: "?") {
            s = String(s[..<query])
        }
        while s.hasPrefix("/") {
            s.removeFirst()
        }
        return (s as NSString).standardizingPath
    }
}
