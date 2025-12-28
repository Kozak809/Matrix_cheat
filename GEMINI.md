# GEMINI.md - Project Context

## Project Overview
**Matrix** is a client-side Minecraft cheat mod built for the Fabric loader (targeting Minecraft 1.21.4). It provides various utility modules (hacks) such as KillAura, Flight, ESP, and more. The project utilizes Mixins to hook into the game engine and a custom event system for handling game events.

## Technical Stack
*   **Language:** Java 21
*   **Framework:** Fabric Loader (Fabric API)
*   **Build System:** Gradle (using `fabric-loom`)
*   **Libraries:**
    *   **Lombok:** Used to reduce boilerplate code.
    *   **Mixin:** For modifying Minecraft source code at runtime.

## Architecture

### Directory Structure
The project follows a standard Fabric mod structure with source sets split (mainly using `client`).

*   `src/client/java/dev/mlml/matrix/`: Root package.
    *   `MatrixMod.java`: Main client entry point (`ClientModInitializer`).
    *   `module/`: Core module system.
        *   `modules/`: Individual cheat implementations (e.g., `KillAura.java`, `Flight.java`).
        *   `ModuleManager.java`: Manages registration and toggling of modules.
    *   `mixin/`: Mixin classes injecting logic into vanilla Minecraft classes.
    *   `event/`: Custom event bus system (Listeners, Handlers, Events).
    *   `config/`: Configuration system for saving/loading module settings.
    *   `gui/`: Custom UI rendering (HUD, Config Screen).

### Key Concepts
*   **Modules:** Features are encapsulated as `Module` classes. They listen for events and interact with the game.
*   **Mixins:** Used extensively to intercept packets, render calls, and entity logic to enable cheat functionality.
*   **Configuration:** Custom configuration handling to save module states and settings.

## Build & Run Instructions

### Prerequisites
*   JDK 21
*   Gradle (wrapper provided)

### Commands
*   **Build Project:**
    ```powershell
    ./gradlew build
    ```
*   **Build & Release:**
    Builds the jar and moves it to the `releases/` directory.
    ```powershell
    ./gradlew buildAndMoveJar
    ```
*   **Run Client:**
    Launches Minecraft with the mod loaded in a development environment.
    ```powershell
    ./gradlew runClient
    ```

## Development Conventions
*   **Code Style:** Java 21 features are encouraged. Lombok is used for data classes.
*   **Mixins:** Placed in the `mixin` package and registered in `matrix.mixins.json`.
*   **Assets:** Resources (lang files, icons) are located in `src/client/resources`.