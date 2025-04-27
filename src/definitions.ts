/**
 * Interface for the SecureStorage Capacitor plugin
 * This plugin provides secure key-value storage on mobile platforms:
 * - Android: Uses Android KeyStore + AES encryption
 * - iOS: Uses iOS Keychain
 * - Web: Falls back to localStorage (not secure)
 */
export interface SecureStoragePlugin {
  /**
   * Set a value in secure storage
   * @param options.key The key to store under
   * @param options.value The value to store
   */
  set(options: { key: string; value: string }): Promise<void>;

  /**
   * Get a value from secure storage
   * @param options.key The key to fetch
   * @returns The stored value or null if not found
   */
  get(options: { key: string }): Promise<{ value: string | null }>;

  /**
   * Remove a value from secure storage
   * @param options.key The key to remove
   */
  remove(options: { key: string }): Promise<void>;

  /**
   * Clear all values from secure storage
   */
  clear(): Promise<void>;

  /**
   * Get all keys stored in secure storage
   * @returns Array of keys
   */
  keys(): Promise<{ keys: string[] }>;
}
