package io.revlabs.capacitor.securestorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Android implementation of secure storage using the Android KeyStore system.
 * 
 * Security approach:
 * 1. Generate a master encryption key in the Android KeyStore
 *    - The key never leaves the secure hardware and can't be extracted
 * 2. Use that key to encrypt/decrypt values with AES-GCM algorithm
 * 3. Store the encrypted values in SharedPreferences
 * 
 * This provides strong security because:
 * - The encryption key is protected by the secure hardware
 * - Modern encryption (AES-GCM) provides both confidentiality and integrity
 * - Each value has its own initialization vector (IV) for security
 */
public class SecureStorage {
    private static final String TAG = "SecureStorage";
    
    // Android KeyStore is the system secure key repository
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    
    // Encryption parameters - using AES with GCM mode for authenticated encryption
    private static final String ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    private static final String ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    private static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    private static final String ENCRYPTION_TRANSFORMATION = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/" + ENCRYPTION_PADDING;
    
    // Name of our master key in the KeyStore
    private static final String MASTER_KEY_ALIAS = "SECURE_STORAGE_MASTER_KEY";
    
    // Name of the SharedPreferences file that will store our encrypted values
    private static final String SHARED_PREFS_NAME = "SECURE_STORAGE_PREFS";
    
    // GCM encryption parameters
    private static final int GCM_IV_LENGTH = 12; // Initialization Vector length (bytes)
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length (bits)

    private Context context;
    private KeyStore keyStore;
    private SharedPreferences sharedPreferences;

    /**
     * Initialize the secure storage system
     * @param context Android context used to access KeyStore and SharedPreferences
     */
    public SecureStorage(Context context) {
        this.context = context;
        try {
            // Access the Android KeyStore
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
            
            // Get the SharedPreferences instance where we'll store encrypted values
            sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

            // Check if our master encryption key exists, if not create it
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                generateMasterKey();
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error initializing SecureStorage", e);
        }
    }

    /**
     * Store a value securely
     * @param key The key to store the value under
     * @param value The value to encrypt and store (or null to remove)
     */
    public void set(String key, String value) {
        try {
            // If value is null, just remove the entry
            if (value == null) {
                remove(key);
                return;
            }

            // Get our master key from the KeyStore
            SecretKey secretKey = (SecretKey) keyStore.getKey(MASTER_KEY_ALIAS, null);
            
            // Initialize the cipher for encryption
            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            // Get the random IV that was generated
            byte[] iv = cipher.getIV();
            
            // Encrypt the value
            byte[] encryptedBytes = cipher.doFinal(value.getBytes());

            // Combine IV and encrypted data so we can store them together
            // We need the same IV for decryption later
            byte[] combined = new byte[GCM_IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, GCM_IV_LENGTH, encryptedBytes.length);

            // Base64 encode for storing in SharedPreferences
            String encryptedValue = Base64.encodeToString(combined, Base64.DEFAULT);
            
            // Store the encrypted value
            sharedPreferences.edit().putString(key, encryptedValue).apply();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | 
                NoSuchPaddingException | InvalidKeyException | BadPaddingException | 
                IllegalBlockSizeException e) {
            Log.e(TAG, "Error storing value", e);
        }
    }

    /**
     * Retrieve and decrypt a stored value
     * @param key The key to retrieve
     * @return The decrypted value, or null if not found or decryption fails
     */
    public String get(String key) {
        try {
            // Get the encrypted value from SharedPreferences
            String encryptedValue = sharedPreferences.getString(key, null);
            if (encryptedValue == null) {
                return null;
            }

            // Decode from Base64
            byte[] combined = Base64.decode(encryptedValue, Base64.DEFAULT);
            if (combined.length < GCM_IV_LENGTH) {
                return null;
            }

            // Extract the IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // Get our master key from the KeyStore
            SecretKey secretKey = (SecretKey) keyStore.getKey(MASTER_KEY_ALIAS, null);
            
            // Initialize the cipher for decryption with the same IV used for encryption
            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Decrypt the data
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | 
                NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | 
                BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "Error retrieving value", e);
            return null;
        }
    }

    /**
     * Remove a value from storage
     * @param key The key to remove
     */
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    /**
     * Remove all values from storage
     */
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Get all stored keys
     * @return Array of all keys stored in the secure storage
     */
    public String[] keys() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        return allEntries.keySet().toArray(new String[0]);
    }

    /**
     * Generate a new master encryption key in the Android KeyStore
     * This is called only once when the plugin is first used
     */
    private void generateMasterKey() {
        try {
            // Get a KeyGenerator instance for creating AES keys
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    ENCRYPTION_ALGORITHM,
                    KEYSTORE_PROVIDER
            );

            // Configure our key parameters
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(ENCRYPTION_BLOCK_MODE) // GCM mode for authenticated encryption
                    .setEncryptionPaddings(ENCRYPTION_PADDING)
                    .setRandomizedEncryptionRequired(true) // Require randomized IV for security
                    .build();

            // Initialize the generator with our parameters
            keyGenerator.init(keyGenParameterSpec);
            
            // Generate the key - it's automatically stored in the KeyStore
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Error generating master key", e);
        }
    }
}
