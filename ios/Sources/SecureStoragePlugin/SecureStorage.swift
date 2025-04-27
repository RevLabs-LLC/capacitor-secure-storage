import Foundation
import Security

/**
 * iOS implementation of secure storage using the Keychain Services API.
 *
 * Security approach:
 * - Uses iOS Keychain which provides hardware-backed secure storage
 * - Data is encrypted by the OS using device-specific keys
 * - Protected from other apps and potentially from unauthorized users 
 *   (depending on the protection class used)
 */
@objc public class SecureStorage: NSObject {
    // Service identifier used to group our keychain items
    private let serviceIdentifier = "SecureStoragePlugin"
    
    /**
     * Store a value securely in the iOS Keychain
     *
     * @param key The key to store the value under
     * @param value The string value to securely store
     * @return Whether the operation was successful
     */
    @objc public func set(_ key: String, value: String) -> Bool {
        // Convert the string to data for storage
        guard let valueData = value.data(using: .utf8) else {
            return false
        }
        
        // Prepare the query dictionary with our storage parameters
        let query: [String: Any] = [
            // Specify that we're using a generic password item
            kSecClass as String: kSecClassGenericPassword,
            // Group our items under the service identifier
            kSecAttrService as String: serviceIdentifier,
            // The key becomes the account name in Keychain
            kSecAttrAccount as String: key,
            // The value to store
            kSecValueData as String: valueData,
            // This specifies when the keychain item is accessible:
            // - After first unlock: available after device has been unlocked once after restart
            // - Not available when device is locked (like when app is in background)
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]
        
        // First try to delete any existing item with same key
        // This avoids errors when trying to add a duplicate item
        SecItemDelete(query as CFDictionary)
        
        // Add the new item to the keychain
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    /**
     * Retrieve a value from the iOS Keychain
     *
     * @param key The key to retrieve the value for
     * @return The stored string value, or nil if not found
     */
    @objc public func get(_ key: String) -> String? {
        // Prepare the query dictionary to find our item
        let query: [String: Any] = [
            // Look for a generic password item
            kSecClass as String: kSecClassGenericPassword,
            // With our service identifier
            kSecAttrService as String: serviceIdentifier,
            // And the specific key (account)
            kSecAttrAccount as String: key,
            // Request the item's data to be returned
            kSecReturnData as String: kCFBooleanTrue as Any,
            // Limit to just one result
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        // Look up the item in the keychain
        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
        
        // If found, convert the data back to a string
        if status == errSecSuccess, let data = dataTypeRef as? Data {
            return String(data: data, encoding: .utf8)
        }
        
        return nil
    }
    
    /**
     * Remove a value from the iOS Keychain
     *
     * @param key The key to remove
     * @return Whether the operation was successful
     */
    @objc public func remove(_ key: String) -> Bool {
        // Prepare the query to find the specific item
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceIdentifier,
            kSecAttrAccount as String: key
        ]
        
        // Delete the item from the keychain
        let status = SecItemDelete(query as CFDictionary)
        // Success if item was deleted or if it wasn't found (already deleted)
        return status == errSecSuccess || status == errSecItemNotFound
    }
    
    /**
     * Clear all values stored by this plugin in the Keychain
     *
     * @return Whether the operation was successful
     */
    @objc public func clear() -> Bool {
        // Prepare a query that matches all items for our service
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceIdentifier
        ]
        
        // Delete all matching items
        let status = SecItemDelete(query as CFDictionary)
        // Success if items were deleted or if none were found
        return status == errSecSuccess || status == errSecItemNotFound
    }
    
    /**
     * Get all keys stored by this plugin in the Keychain
     *
     * @return Array of stored keys
     */
    @objc public func keys() -> [String] {
        // Prepare a query to find all items for our service and return their attributes
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceIdentifier,
            // Request the attributes dictionary rather than the data
            kSecReturnAttributes as String: kCFBooleanTrue as Any,
            // Return all matches, not just the first one
            kSecMatchLimit as String: kSecMatchLimitAll
        ]
        
        // Search the keychain
        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
        
        var keys: [String] = []
        
        // If items were found, extract the account names (our keys)
        if status == errSecSuccess {
            if let items = dataTypeRef as? [[String: Any]] {
                for item in items {
                    if let account = item[kSecAttrAccount as String] as? String {
                        keys.append(account)
                    }
                }
            }
        }
        
        return keys
    }
}
