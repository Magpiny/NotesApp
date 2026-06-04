# Notafo (Notes + Tasks + Focus)

[![Android CI](https://github.com/Magpiny/NotesApp/actions/workflows/ci.yml/badge.svg)](https://github.com/Magpiny/NotesApp/actions/workflows/ci.yml)
![Android SDK](https://img.shields.io/badge/SDK-33%2B-brightgreen)
![Version](https://img.shields.io/badge/Version-2.4.9-blue)
![License](https://img.shields.io/badge/License-Apache_2.0-orange)

Notafo is a premium, local-first personal productivity suite for Android. It combines a highly secure note-taking experience with an advanced task management system and customizable deep-work tools, all within a fluid Material 3 design.

## 🚀 Version 2.4.9 Highlights - "The Quality Update"

This update modernizes the entire technical core of Notafo, bringing professional-grade stability and a best-in-class Markdown experience.

### ✍️ High-Fidelity Markdown (v2.4.9)
- **Library Integration**: Replaced custom rendering with Mike Penz's Multiplatform Markdown Renderer for 100% specification compliance.
- **Syntax Highlighting**: Full color support for code blocks (C++, Kotlin, Python, etc.) in the new **Editor Preview** mode.
- **Smart Listing**: Intelligently auto-increments numbers and continues bullet points as you type.
- **Improved Alignment**: Optimized list rendering for clear, professional-looking notes on all screen sizes.

### ✅ Advanced Task Management
- **Reliable Subtasks**: Powered by Room Relations, subtasks now persist instantly and show real-time progress counts (e.g., "3/5 subtasks") in the main list.
- **Multi-Stage Reminders**: Set precise notifications for **10 minutes before**, **5 minutes before**, and **exactly on time**.
- **AlarmClock Precision**: Uses Android's high-priority alarm system to ensure notifications fire even in low-power modes.

### 🛠️ Technical Modernization
- **Jetpack Compose Upgrade**: Now powered by **Compose BOM 2026.05.01**, utilizing the latest performance optimizations and Material 3 components.
- **Comprehensive Testing**: A robust test suite using **JUnit 5**, **MockK**, and **Robolectric** covering data layers, reminders, and UI logic.
- **Zero-Warning Codebase**: Resolved over 60+ lint and deprecation warnings to ensure long-term maintainability.
- **Small Screen Optimization**: Added a dynamic overflow menu to the editor, preventing button overlap on devices like the Galaxy A56.

## 📸 Visual Tour

````carousel
![Home Screen - Fluid Material 3 Layout](home_screen.png)
<!-- slide -->
![Kanban Board - Visual Task Management](kanban_board.png)
<!-- slide -->
![Advanced Editor - Real-time Markdown & Preview Mode](markdown_editor.png)
<!-- slide -->
![Productivity Search - Unified Results](unified_search.png)
<!-- slide -->
![Focus Timer - Personalized Sessions](focus_timer.png)
````

## 🛠️ Technical Stack

- **Language:** Kotlin 2.3.21 (K2 Compiler)
- **UI Framework:** Jetpack Compose (BOM 2026.05.01) with Material 3 (1.4.0)
- **Database:** Room with @Relation support
- **Testing:** JUnit 5, MockK, Kotest, Robolectric
- **Dependency Injection:** Hilt
- **Build System:** Gradle 9.2.1 with KSP 2.3.8
- **Localization:** Multi-language support (English/Español/Français/etc.)

## 📦 Installation

Notafo is designed for Android 13.0 (API 33) and above. Build the project from source using Android Studio Ladybug or later.

## ⚖️ License

This project is licensed under the Apache License 2.0.
