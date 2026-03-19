# Papel-IT Sticky Notes

A full-featured, user-friendly, and visually appealing sticky notes desktop application built with JavaFX, Java 17, and Gradle. Offers seamless managing of sticky notes, complete with rich-text editor capabilities, "Always on Top" functionality, and persistence via an internal SQLite database.

## System Requirements
- OS: Linux, Windows, or macOS
- Java 17+ (For development only, final released installers do not require Java)

## Technical Stack & Features
- **Java 17 & JavaFX 17** via OpenJFX.
- **SQLite Database** persisting in standard user directories (`~/.jfxnotes` or `%APPDATA%\JFXNotes`).
- **DAO Pattern:** Clean separation of data access logic.
- **Theming:** Integrated Light Mode and Dark Mode switching via JavaFX CSS.
- **jlink & jpackage Integration:** Generating cross-platform customized native installers using Gradle `badass-jlink-plugin`.

## Development Setup
1. Open the project in your IDE (IntelliJ IDEA, Visual Studio Code).
2. The `pom.xml` will resolve dependencies securely and accurately.

## Building and Running
To compile the project and download all dependencies:
```bash
mvn clean package
```

To run the application locally during development:
```bash
mvn javafx:run
```

## Generating Native Installers
To create a native standalone executable (`.exe` for Windows, `.rpm` for Redhat-based Linux) and a custom Java runtime image:

First, generate a tightly packed custom JRE module via `jlink`:
```bash
mvn javafx:jlink
```

Then, trigger the installation packaging:
```bash
mvn install -P linux-jpackage    # On Linux
mvn install -P windows-jpackage  # On Windows
```

Alternatively, `mvn install` will automatically detect your OS and generate the respective installer natively.
The resulting package will be stored inside the `target/jpackage/` directory.
