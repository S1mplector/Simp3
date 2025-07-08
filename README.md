# SiMP3 - Simple Music Player

A modular Java-based music player application built with JavaFX and following clean architecture principles.

## Project Structure

```
simp3/
├── src/
│   ├── main/
│   │   ├── java/com/musicplayer/
│   │   │   ├── core/                    # Core business logic
│   │   │   │   ├── audio/              # Audio processing and playback
│   │   │   │   ├── playlist/           # Playlist management
│   │   │   │   └── library/            # Music library management
│   │   │   ├── data/                   # Data layer
│   │   │   │   ├── models/             # Data models (Song, Artist, Album, etc.)
│   │   │   │   ├── repositories/       # Data access layer
│   │   │   │   └── storage/            # File I/O and persistence
│   │   │   ├── ui/                     # User interface layer
│   │   │   │   ├── controllers/        # JavaFX controllers
│   │   │   │   ├── views/              # Custom view components
│   │   │   │   └── components/         # Reusable UI components
│   │   │   ├── services/               # Business services
│   │   │   ├── utils/                  # Utility classes
│   │   │   └── config/                 # Configuration management
│   │   └── resources/
│   │       ├── css/                    # Stylesheets
│   │       ├── images/                 # Icons and images
│   │       └── fxml/                   # FXML layout files
│   └── test/
│       └── java/com/musicplayer/       # Unit and integration tests
├── lib/                                # External libraries
├── docs/                               # Documentation
└── README.md
```

## Architecture Overview

### Core Layer (`core/`)
- **Audio**: Handles audio file processing, playback control, and format support
- **Playlist**: Manages playlist creation, modification, and persistence
- **Library**: Manages the music library, scanning, and metadata extraction

### Data Layer (`data/`)
- **Models**: Domain objects (Song, Artist, Album, Playlist)
- **Repositories**: Data access interfaces and implementations
- **Storage**: File system operations and database persistence

### UI Layer (`ui/`)
- **Controllers**: JavaFX controllers implementing MVP pattern
- **Views**: Custom view components for complex UI elements
- **Components**: Reusable UI components (buttons, sliders, etc.)

### Service Layer (`services/`)
- Business logic services that coordinate between core and data layers
- External service integrations (metadata fetching, lyrics, etc.)

### Utilities (`utils/`)
- File format utilities
- Audio format converters
- Common helper functions

### Configuration (`config/`)
- Application settings
- User preferences
- Theme configuration

## Features (Planned)

- **Audio Playback**: Support for MP3, WAV, FLAC, and other common formats
- **Library Management**: Automatic music library scanning and organization
- **Playlist Support**: Create, edit, and manage playlists
- **Metadata Support**: ID3 tag reading and editing
- **Search & Filter**: Advanced search and filtering capabilities
- **Equalizer**: Built-in audio equalizer
- **Themes**: Customizable UI themes
- **Shortcuts**: Keyboard shortcuts and hotkeys

## Technologies

- **Java 17+**: Core language
- **JavaFX**: UI framework
- **Maven**: Build tool and dependency management
- **JUnit 5**: Testing framework
- **Jackson**: JSON processing for configuration
- **Apache Commons**: Utility libraries

## Getting Started

1. Ensure Java 17+ is installed
2. Clone the repository
3. Run `mvn clean compile` to build the project
4. Run `mvn javafx:run` to start the application

## License

MIT License
