# NotesApp

NotesApp is a modern, local-first personal productivity suite for Android. It combines a highly secure note-taking experience with an advanced task management system, all within a sleek, "Swagger" design.

## 🚀 Version 2.0 Highlights

This major update transforms the app into a full-featured productivity suite with a focus on privacy and efficiency.

### 🛡️ Security & Privacy
- **Secret Vault:** A dedicated secure section for sensitive notes, hidden from main lists and search results.
- **Biometric Authentication:** Protection for the vault using the Android Biometric API (Fingerprint and Face ID).
- **Per-note Locking:** Lock individual notes with a single tap for extra privacy.
- **Strictly Local:** 100% offline performance. Your data never leaves your device.

### ⏱️ Advanced Productivity
- **Pomodoro Focus Timer:** Built-in timer with 25/5 minute cycles and session tracking to improve deep work.
- **Smart Todo System:** Classic list and modern Kanban board views with status-based workflow progression.
- **NLP Quick Add:** Add tasks via natural language parsing (e.g., "buy groceries tomorrow !high #home").
- **Recurring Tasks:** Support for daily, weekly, and monthly task automation.

### 🎨 Visual Identity
- **"Swagger" Design:** A unique brand identity using a deep teal & midnight navy color palette.
- **Dynamic Typography:** User-selectable font families (Serif, Sans-serif, Monospace) and optimized legibility.
- **Clean Markdown:** Formatting tokens (like `**` and `_`) are hidden for a cleaner visual experience.

## 🛠️ Technical Stack

- **Language:** Kotlin 2.3.21 (K2 Compiler)
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room with FTS4 support
- **Dependency Injection:** Hilt
- **Build System:** Gradle 9.2.1 (AGP) with KSP 2.3.8
- **Concurrency:** Kotlin Coroutines & Flow
- **Localization:** Multi-language support (English/Español)

## 📦 Installation

NotesApp is designed for Android 8.0 (API 26) and above. You can build the project from source or download the latest APK from the [Releases](https://github.com/your-username/NotesApp/releases) page.

## ⚖️ License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
