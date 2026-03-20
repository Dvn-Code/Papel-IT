# 📝 Papel IT — Smart, Beautiful Sticky Notes

[![Package Windows EXE](https://github.com/USER_NAME/Papel-IT/actions/workflows/package-windows.yml/badge.svg)](https://github.com/USER_NAME/Papel-IT/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

**Papel IT** is a premium, minimalist desktop application designed for thinkers, creators, and organizers. Built with **JavaFX 17**, it combines the simplicity of physical sticky notes with the power of modern desktop synchronization.

---

## ✨ Key Features

*   🎨 **Vibrant Color Palette**: Choose from 8 curated, high-contrast colors (Pink, Peach, Green, Blue, Purple, Yellow, Gold, Cyan).
*   🎭 **Adaptive Themes**: Seamlessly switch between **Light** and **Dark** modes with persistent memory.
*   🖋️ **Rich Text Editing**: Full support for Bold, Italic, and structured notes directly in the editor.
*   📌 **Smart Pinning**: "Always on Top" functionality with instant dashboard re-sorting.
*   🏷️ **Automatic Tagging**: Organize your thoughts into categories: **Home**, **Work**, **Personal**, and **Ideas**.
*   🔍 **Instant Search**: Reactive, real-time filtering across all your note titles and contents.
*   💾 **Invisible Persistence**: Robust auto-save via local SQLite database; never lose a thought again.
*   🚀 **Smart Shutdown**: Efficiently frees up system resources by closing the background process only when the last window is dismissed.

---

## 📦 Installation & Release

### 🐧 For Linux (Fedora / RedHat)
Generate and install the native RPM package:
```bash
# 1. Build the branded RPM
chmod +x build-rpm.sh
./build-rpm.sh

# 2. Install
sudo dnf install target/jpackage/papelit-1.0.0-1.x86_64.rpm
```

### 🪟 For Windows (10/11)
We provide two ways to run Papel IT on Windows:

1.  **Professional Installer**: Download the `.exe` from our [GitHub Releases].
2.  **Portable Edition (Recommended)**: Download the `.zip` file, extract it, and run `Papel-IT-Portable.exe` directly.

> [!IMPORTANT]
> **Smart App Control Fix**: Because this is an independent app, Windows may block the installer. 
> To fix this: **Right-click the file** -> **Properties** -> Check **"Unblock"** -> **Apply**. Then run!

---

## 🛠️ Technical Stack
*   **Core**: Java 17 (LTS)
*   **UI Framework**: JavaFX 17+ (OpenJFX)
*   **Database**: SQLite (JDBC)
*   **Build Tool**: Maven
*   **Deployment**: jpackage (OS Native Installers)

---

## 🏗️ Development Setup

To run locally during development:
```bash
mvn clean package
mvn javafx:run
```

To build a fresh Windows version on Linux, simply push your code to GitHub; our **GitHub Actions** will handle the cross-compilation and provide your installer automatically.

---

## 🤝 Contributing
Feel free to open an issue or submit a pull request! We're always looking to make **Papel IT** even more beautiful.

*Designed with ❤️ by PapelIT Team*
