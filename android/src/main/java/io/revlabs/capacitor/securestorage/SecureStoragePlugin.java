package io.revlabs.capacitor.securestorage;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Capacitor plugin for secure storage on Android.
 * 
 * This plugin bridges JavaScript calls from the Capacitor webview
 * to the native Android secure storage implementation.
 */
@CapacitorPlugin(name = "SecureStorage")
public class SecureStoragePlugin extends Plugin {

    private SecureStorage implementation;

    /**
     * Called when the plugin is loaded.
     * Initialize our secure storage implementation with the Android context.
     */
    @Override
    public void load() {
        implementation = new SecureStorage(getContext());
    }

    /**
     * Store a value securely.
     * 
     * JavaScript call: SecureStorage.set({ key: 'key', value: 'value' })
     */
    @PluginMethod
    public void set(PluginCall call) {
        String key = call.getString("key");
        String value = call.getString("value");

        // Validate required parameters
        if (key == null) {
            call.reject("Key string must be provided");
            return;
        }

        if (value == null) {
            call.reject("Value string must be provided");
            return;
        }

        try {
            implementation.set(key, value);
            call.resolve(); // Return success with no data
        } catch (Exception e) {
            call.reject("Error storing value", e);
        }
    }

    /**
     * Retrieve a value from secure storage.
     * 
     * JavaScript call: SecureStorage.get({ key: 'key' })
     * Returns: { value: 'value' } or { value: null } if not found
     */
    @PluginMethod
    public void get(PluginCall call) {
        String key = call.getString("key");

        // Validate required parameters
        if (key == null) {
            call.reject("Key string must be provided");
            return;
        }

        try {
            String value = implementation.get(key);
            JSObject ret = new JSObject();
            ret.put("value", value); // value will be null if key not found
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Error retrieving value", e);
        }
    }

    /**
     * Remove a value from secure storage.
     * 
     * JavaScript call: SecureStorage.remove({ key: 'key' })
     */
    @PluginMethod
    public void remove(PluginCall call) {
        String key = call.getString("key");

        // Validate required parameters
        if (key == null) {
            call.reject("Key string must be provided");
            return;
        }

        try {
            implementation.remove(key);
            call.resolve(); // Return success with no data
        } catch (Exception e) {
            call.reject("Error removing value", e);
        }
    }

    /**
     * Clear all values from secure storage.
     * 
     * JavaScript call: SecureStorage.clear()
     */
    @PluginMethod
    public void clear(PluginCall call) {
        try {
            implementation.clear();
            call.resolve(); // Return success with no data
        } catch (Exception e) {
            call.reject("Error clearing storage", e);
        }
    }

    /**
     * Get all keys stored in secure storage.
     * 
     * JavaScript call: SecureStorage.keys()
     * Returns: { keys: ['key1', 'key2', ...] }
     */
    @PluginMethod
    public void keys(PluginCall call) {
        try {
            String[] keys = implementation.keys();
            JSObject ret = new JSObject();
            JSArray jsArray = new JSArray();
            
            // Convert the Java string array to a JavaScript array
            for (String key : keys) {
                jsArray.put(key);
            }
            
            ret.put("keys", jsArray);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Error retrieving keys", e);
        }
    }
}
