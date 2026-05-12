# Scanner App

An Android application for capturing data using the device camera and sending it via SMTP email. The app includes configurable settings, CSV processing, and local data persistence.

---

## Features

- Camera-based data scanning
- Sending scanned data via SMTP email
- Configurable email settings
- CSV data processing
- Local storage using DataStore
- Multi-screen navigation
- MVVM architecture

---

## Tech Stack

- Kotlin
- Android SDK
- MVVM architecture
- Jetpack DataStore
- Coroutines
- Camera API
- Navigation Component

---

## Architecture

The project follows the MVVM (Model-View-ViewModel) architecture:

- data layer: repositories, models, and storage logic
- ui layer: screens and UI components
- viewmodel layer: business logic and state management
- navigation layer: app navigation structure

---

## How to Run

1. Clone repository
2. Open in Android Studio
3. Sync Gradle
4. Run the app on emulator or device
