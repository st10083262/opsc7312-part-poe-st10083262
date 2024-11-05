# opsc7312-part-poe-st10083262
# NoteMaster

NoteMaster is an Android application developed in Kotlin for efficient note-taking and personal productivity. The app allows users to create, edit, and save notes locally and online, with Firebase Firestore as the backend database for cloud storage and RoomDB for offline storage. Key features include real-time sync between offline and online data, push notifications for added notes, and biometric login.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Technologies](#technologies)
- [Firebase Setup](#firebase-setup)
- [RoomDB Setup](#roomdb-setup)
- [Offline Sync with RoomDB and Firestore](#offline-sync-with-roomdb-and-firestore)
- [FCM Push Notifications](#fcm-push-notifications)
- [Biometric Authentication](#biometric-authentication)
- [Localization](#localization)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Create and Manage Notes**: Users can add, edit, and delete notes within the app.
- **Offline Mode with Sync**: Notes created offline sync with Firestore when the device reconnects to the internet.
- **Firebase Firestore**: For secure and scalable cloud storage of user notes.
- **RoomDB**: For offline storage of notes when the device is disconnected.
- **Push Notifications**: Users receive notifications when a new note is added.
- **Biometric Authentication**: Users can enable fingerprint or facial recognition for enhanced security.
- **Localization**: Supports English, Zulu, and Xhosa languages.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/NoteMaster.git
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle to download necessary dependencies.

4. Configure Firebase for the project (see [Firebase Setup](#firebase-setup)).

## Usage

### Running the App

- Open the project in **Android Studio** and connect an Android device or emulator.
- Run the app by selecting **Run > Run 'app'**.
  
Upon launching, users will see the home screen with options to add and view notes. The settings screen offers dark mode, biometric authentication, and language preferences.

## Technologies

- **Kotlin**: Programming language for Android development.
- **Firebase Firestore**: Cloud database for real-time data syncing.
- **RoomDB**: Local database for offline access.
- **Firebase Cloud Messaging (FCM)**: For sending push notifications.
- **BiometricPrompt**: For secure authentication with fingerprint or facial recognition.
- **Glide**: Image loading and caching for profile images.

## Firebase Setup

1. Set up a Firebase project at [Firebase Console](https://console.firebase.google.com/).
2. Add your Android app to the Firebase project by registering the app’s package name.
3. Download the `google-services.json` file and place it in your app directory.
4. Enable Firestore Database and Firebase Cloud Messaging (FCM) in the Firebase Console.

## RoomDB Setup

RoomDB is used to store notes offline. It supports note-saving and retrieval even without internet connectivity. RoomDB syncs with Firestore when the device goes online, ensuring seamless data integrity across sessions.

### Offline Sync with RoomDB and Firestore

The app leverages RoomDB for offline storage. When the user reconnects to the internet, any locally saved notes are synchronized with Firebase Firestore, making them accessible across devices.

### Code Example

```kotlin
// Saving note to RoomDB
lifecycleScope.launch(Dispatchers.IO) {
    noteDao.insert(note)
}
```

## FCM Push Notifications

Firebase Cloud Messaging is used to send notifications when a new note is added. To enable notifications:

1. In your Firebase Console, navigate to Cloud Messaging and configure a new notification channel.
2. Add the FCM server key to your project.

### Code Example for Sending Notifications

```kotlin
private fun sendNoteAddedNotification() {
    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.notemaster_icon)
        .setContentTitle("New Note Added")
        .setContentText("A new note has been created!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    with(NotificationManagerCompat.from(this)) {
        notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
```

## Biometric Authentication

NoteMaster supports fingerprint and face recognition for added security. Biometric authentication can be enabled from the settings screen.

### Code Example

```kotlin
val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        sharedPreferences.edit().putBoolean("biometric_enabled", true).apply()
        Toast.makeText(this@SettingsActivity, "Biometric login enabled", Toast.LENGTH_SHORT).show()
    }
})
```

## Localization

The app supports English, Zulu, and Xhosa languages. To add more languages, update the `strings.xml` files in `res/values` and create separate files for each language in `res/values-<language_code>`.

### Language Strings Example

```xml
<string name="app_name">NoteMaster</string>
<string name="new_note_notification_title">New Note</string>
<string name="new_note_notification_body">A new note has been created!</string>
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch for each feature or bug fix.
3. Commit your changes and push them to your fork.
4. Submit a pull request.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

