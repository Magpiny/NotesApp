# Notafo (Notes + Tasks + Focus)

Notafo is a modern, local-first personal productivity suite for Android. It combines a highly secure note-taking experience with an advanced task management system and deep-work tools, all within a sleek, "Swagger" design.

## 🚀 Version 2.3.4 Highlights

This update rebranding the app to **Notafo** and introduces significant UI refinements, security hardening, and logic improvements.

### 🛡️ Security & Privacy
- **Hardened Secret Vault:** Strictly enforced biometric/device security (Fingerprint, Face, or PIN). Access is denied if no device security is configured.
- **Biometric Authentication:** Protection for the vault using the Android Biometric API.
- **Strictly Local:** 100% offline performance. Your data never leaves your device.

### ⏱️ Advanced Productivity
- **Smart Todo System:** Resolved task list clutter by filtering completed/cancelled tasks from the active list.
- **Pomodoro Focus Timer:** Built-in timer with 25/5 minute cycles and session tracking to improve deep work.
- **NLP Quick Add:** Add tasks via natural language parsing (e.g., "buy milk tomorrow !high #home").
- **Recurring Tasks:** Support for daily, weekly, and monthly task automation.

### 🎨 Visual Identity & UI Polish
- **"Swagger" Design:** A unique brand identity using a deep teal & midnight navy color palette.
- **Beautifully Bright Icons:** Action icons in the editor (Undo, Share, Delete, Pin) are now consistently bright white for a polished look.
- **Optimized Layout:** Relocated Settings to the top-left and added an Archive icon to the Home screen for a cleaner bottom navigation bar.
- **Dynamic Typography:** User-selectable font families (Serif, Sans-serif, Monospace) with high-contrast text for perfect readability.

## 🛠️ Technical Stack

- **Language:** Kotlin 2.3.21 (K2 Compiler)
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room with FTS4 support
- **Dependency Injection:** Hilt
- **Build System:** Gradle 9.2.1 (AGP) with KSP 2.3.8
- **Concurrency:** Kotlin Coroutines & Flow
- **Localization:** Multi-language support (English/Español/Français/etc.)

## 📦 Installation

Notafo is designed for Android 13.0 (API 33) and above. You can build the project from source using Android Studio Ladybug or later.

## ⚖️ License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
