import Foundation

@objc public class SecureStorage: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
