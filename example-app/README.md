# Capacitor Secure Storage Example App

This example app demonstrates how to use the Capacitor Secure Storage plugin in a real application. It provides a simple user interface that allows you to:

- Store values securely
- Retrieve stored values
- List all stored keys
- Remove individual keys
- Clear all stored data

## Running the Example

### Prerequisites

- Node.js 14+
- npm or yarn
- For iOS: Xcode and CocoaPods
- For Android: Android Studio

### Setup and Run

1. Install dependencies:

```bash
npm install
# or
yarn
```

2. Build the web app:

```bash
npm run build
# or
yarn build
```

3. Add the platforms you want to test on:

```bash
# For iOS
npx cap add ios

# For Android
npx cap add android
```

4. Sync the web code to the native projects:

```bash
npx cap sync
```

5. Run on a specific platform:

```bash
# For iOS
npx cap open ios
# Then run from Xcode

# For Android
npx cap open android
# Then run from Android Studio

# For web testing (note: web uses localStorage, not secure storage)
npm start
```

## Features Demonstrated

- Setting values with different keys
- Retrieving values by key
- Viewing a list of all stored keys
- Deleting individual keys
- Clearing all stored data
- Error handling

## Implementation Details

This example demonstrates:

- How to properly call the Secure Storage plugin methods
- How to handle both successful operations and errors
- How to create a simple UI for managing secure storage data
- How to use SecureStorage across different platforms (iOS, Android, web)

## Security Notes

- On iOS, data is stored in the Keychain
- On Android, data is encrypted using the KeyStore and AES encryption
- On web, data is stored in localStorage (not secure)

For production applications, consider adding additional security measures and proper error handling.
