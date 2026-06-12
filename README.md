# Notafo (Notes + Tasks + Focus)

[![Android CI](https://github.com/Magpiny/NotesApp/actions/workflows/ci.yml/badge.svg)](https://github.com/Magpiny/NotesApp/actions/workflows/ci.yml)
![Android SDK](https://img.shields.io/badge/SDK-33%2B-brightgreen)
![Version](https://img.shields.io/badge/Version-2.5.2-blue)
![License](https://img.shields.io/badge/License-Apache_2.0-orange)

Notafo is a premium, local-first personal productivity suite for Android. It combines a highly secure note-taking experience with an advanced task management system and customizable deep-work tools, all within a fluid Material 3 design.

## 🚀 Version 2.5.2 Highlights - "The Media & Core Update"

This update introduces multimedia support to your notes and brings the latest Android 15 performance optimizations to the app core.

### 🖼️ Multimedia Integration (v2.5.2)
- **Image Attachments**: You can now add high-resolution images to any note. View them in a scrollable attachment gallery within the editor.
- **Smart Management**: Easily remove attachments with a single tap, with automatic cleanup of unused media.

### 🛠️ Core Modernization & Stability
- **Android 15 Optimized**: Fully compatible with **Android 15 (API 37)**, leveraging the latest system performance and security improvements.
- **Gradle 9.5 Upgrade**: Migrated to **Gradle 9.5.1** and **AGP 9.2.1** for significantly faster build times and better dependency resolution.
- **UI Crash Protection**: Resolved several critical crashes in the **Color Picker** and **Markdown Editor** related to layout constraints and data overflow.
- **Database Optimization**: Refined auto-save logic to prevent redundant operations and ensure maximum responsiveness.

### ✍️ High-Fidelity Markdown
- **Library Integration**: Replaced custom rendering with Mike Penz's Multiplatform Markdown Renderer for 100% specification compliance.
- **Syntax Highlighting**: Full color support for code blocks (C++, Kotlin, Python, etc.) in the new **Editor Preview** mode.
- **Smart Listing**: Intelligently auto-increments numbers and continues bullet points as you type.
- **Improved Alignment**: Optimized list rendering for clear, professional-looking notes on all screen sizes.

### ✅ Advanced Task Management
- **Reliable Subtasks**: Powered by Room Relations, subtasks now persist instantly and show real-time progress counts (e.g., "3/5 subtasks") in the main list.
- **Multi-Stage Reminders**: Set precise notifications for **10 minutes before**, **5 minutes before**, and **exactly on time**.
- **AlarmClock Precision**: Uses Android's high-priority alarm system to ensure notifications fire even in low-power modes.

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
- **Build System:** Gradle 9.5.1 with KSP 2.3.9
- **Localization:** Multi-language support (English/Español/Français/etc.)

## 📦 Installation

Notafo is designed for Android 13.0 (API 33) and above. Build the project from source using Android Studio Ladybug or later.

## ⚖️ License

This project is licensed under the Apache License 2.0.
