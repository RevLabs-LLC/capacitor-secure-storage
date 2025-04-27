import Foundation
import Capacitor

/**
 * Capacitor plugin for secure storage on iOS.
 * 
 * This plugin bridges JavaScript calls from the Capacitor webview
 * to the native iOS Keychain implementation.
 * 
 * The iOS Keychain is a secure storage mechanism that protects
 * sensitive data using hardware-backed encryption.
 */
@objc(SecureStoragePlugin)
public class SecureStoragePlugin: CAPPlugin, CAPBridgedPlugin {
    // Identifier for the plugin
    public let identifier = "SecureStoragePlugin"
    // Name used in JavaScript
    public let jsName = "SecureStorage"
    // List of methods exposed to JavaScript
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "set", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "get", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "remove", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clear", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "keys", returnType: CAPPluginReturnPromise)
    ]
    // Instance of our secure storage implementation
    private let implementation = SecureStorage()

    /**
     * Store a value securely in the Keychain
     * 
     * JavaScript call: SecureStorage.set({ key: 'key', value: 'value' })
     */
    @objc func set(_ call: CAPPluginCall) {
        // Validate required parameters
        guard let key = call.getString("key") else {
            call.reject("Key string must be provided")
            return
        }
        
        guard let value = call.getString("value") else {
            call.reject("Value string must be provided")
            return
        }
        
        // Store the value and check result
        if implementation.set(key, value: value) {
            call.resolve() // Return success with no data
        } else {
            call.reject("Failed to store value")
        }
    }

    /**
     * Retrieve a value from the Keychain
     * 
     * JavaScript call: SecureStorage.get({ key: 'key' })
     * Returns: { value: 'value' } or { value: null } if not found
     */
    @objc func get(_ call: CAPPluginCall) {
        // Validate required parameters
        guard let key = call.getString("key") else {
            call.reject("Key string must be provided")
            return
        }
        
        // Get the value from Keychain (may be nil if not found)
        let value = implementation.get(key)
        call.resolve([
            "value": value as Any // as Any handles nil correctly
        ])
    }

    /**
     * Remove a value from the Keychain
     * 
     * JavaScript call: SecureStorage.remove({ key: 'key' })
     */
    @objc func remove(_ call: CAPPluginCall) {
        // Validate required parameters
        guard let key = call.getString("key") else {
            call.reject("Key string must be provided")
            return
        }
        
        // Remove the value and check result
        if implementation.remove(key) {
            call.resolve() // Return success with no data
        } else {
            call.reject("Failed to remove value")
        }
    }

    /**
     * Clear all values from the Keychain for this plugin
     * 
     * JavaScript call: SecureStorage.clear()
     */
    @objc func clear(_ call: CAPPluginCall) {
        // Clear all values and check result
        if implementation.clear() {
            call.resolve() // Return success with no data
        } else {
            call.reject("Failed to clear storage")
        }
    }

    /**
     * Get all keys stored in the Keychain by this plugin
     * 
     * JavaScript call: SecureStorage.keys()
     * Returns: { keys: ['key1', 'key2', ...] }
     */
    @objc func keys(_ call: CAPPluginCall) {
        // Get all keys from Keychain
        let keys = implementation.keys()
        call.resolve([
            "keys": keys
        ])
    }
}
