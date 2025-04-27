# capacitor-secure-storage

A secure storage plugin for Capacitor that's a thin wrapper around Android KeyStore and iOS Keychain APIs.

## Install

```bash
npm install capacitor-secure-storage
npx cap sync
```

## Usage

```typescript
import { SecureStorage } from 'capacitor-secure-storage';

// Store a value
await SecureStorage.set({
  key: 'my-key',
  value: 'my-secure-value',
});

// Get a value
const result = await SecureStorage.get({ key: 'my-key' });
console.log('Value:', result.value);

// Remove a value
await SecureStorage.remove({ key: 'my-key' });

// Clear all stored values
await SecureStorage.clear();

// Get all keys
const keysResult = await SecureStorage.keys();
console.log('All keys:', keysResult.keys);
```

## API

<docgen-index>

- [`set(...)`](#set)
- [`get(...)`](#get)
- [`remove(...)`](#remove)
- [`clear()`](#clear)
- [`keys()`](#keys)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### set(...)

```typescript
set(options: { key: string; value: string; }) => Promise<void>
```

Store a value in secure storage.

| Param         | Type                                         | Description        |
| ------------- | -------------------------------------------- | ------------------ |
| **`options`** | <code>{ key: string; value: string; }</code> | Key-value to store |

---

### get(...)

```typescript
get(options: { key: string; }) => Promise<{ value: string | null; }>
```

Get a value from secure storage.

| Param         | Type                          | Description                   |
| ------------- | ----------------------------- | ----------------------------- |
| **`options`** | <code>{ key: string; }</code> | Key to retrieve the value for |

**Returns:** <code>Promise&lt;{ value: string | null; }&gt;</code>

---

### remove(...)

```typescript
remove(options: { key: string; }) => Promise<void>
```

Remove a value from secure storage.

| Param         | Type                          | Description                 |
| ------------- | ----------------------------- | --------------------------- |
| **`options`** | <code>{ key: string; }</code> | Key to remove the value for |

---

### clear()

```typescript
clear() => Promise<void>
```

Clear all values from secure storage.

---

### keys()

```typescript
keys() => Promise<{ keys: string[]; }>
```

Get all keys stored in secure storage.

**Returns:** <code>Promise&lt;{ keys: string[]; }&gt;</code>

---

</docgen-api>

## Security Details

### Android

Uses the Android KeyStore system to create an AES-GCM encryption key. The key is protected by the Android KeyStore and used to encrypt/decrypt values that are then stored in SharedPreferences.

### iOS

Uses the iOS Keychain Services API to securely store key-value pairs. Values are stored with the `kSecAttrAccessibleAfterFirstUnlock` accessibility setting, meaning they are accessible after the first unlock and remain accessible while the device is unlocked.

### Web

On the web platform, this plugin falls back to using localStorage with a prefix. Note that localStorage is not secure and values are stored in plaintext. For production web applications requiring secure storage, consider implementing a different solution.
