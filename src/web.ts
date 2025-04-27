import { WebPlugin } from '@capacitor/core';

import type { SecureStoragePlugin } from './definitions';

/**
 * Web implementation of the SecureStorage plugin.
 *
 * NOTE: This implementation uses localStorage which is NOT secure.
 * Values are stored in plain text and can be accessed by any script
 * running on the same domain. This implementation is provided only
 * as a convenience for development and testing purposes.
 *
 * For production web applications requiring actual secure storage,
 * consider implementing a server-side solution or using a dedicated
 * security library.
 */
export class SecureStorageWeb extends WebPlugin implements SecureStoragePlugin {
  // Prefix added to all keys to avoid collisions with other localStorage items
  private readonly storagePrefix = 'cap_secureStorage_';

  /**
   * Store a value in localStorage with the storage prefix
   */
  async set(options: { key: string; value: string }): Promise<void> {
    localStorage.setItem(this.getKeyName(options.key), options.value);
  }

  /**
   * Retrieve a value from localStorage
   * @returns Object containing the value or null if not found
   */
  async get(options: { key: string }): Promise<{ value: string | null }> {
    const value = localStorage.getItem(this.getKeyName(options.key));
    return { value };
  }

  /**
   * Remove a specific value from localStorage
   */
  async remove(options: { key: string }): Promise<void> {
    localStorage.removeItem(this.getKeyName(options.key));
  }

  /**
   * Clear all values added by this plugin from localStorage.
   * This only removes items with our plugin's prefix, not all localStorage items.
   */
  async clear(): Promise<void> {
    const keysToRemove = Object.keys(localStorage).filter((key) => key.startsWith(this.storagePrefix));

    for (const key of keysToRemove) {
      localStorage.removeItem(key);
    }
  }

  /**
   * Get all keys stored by this plugin in localStorage
   * @returns Object containing array of keys (without the prefix)
   */
  async keys(): Promise<{ keys: string[] }> {
    const allKeys = Object.keys(localStorage)
      .filter((key) => key.startsWith(this.storagePrefix))
      .map((key) => key.substring(this.storagePrefix.length));

    return { keys: allKeys };
  }

  /**
   * Utility method to convert a user-provided key to the actual storage key
   * by adding the prefix
   */
  private getKeyName(key: string): string {
    return `${this.storagePrefix}${key}`;
  }
}
