# SiMP3 Architecture Documentation

## Overview

SiMP3 is designed using a layered architecture with clear separation of concerns. The application follows the principles of clean architecture, ensuring that business logic is independent of UI and external dependencies.

## Layer Descriptions

### 1. Core Layer (`core/`)

The core layer contains the fundamental business logic of the music player application.

#### Audio Module (`core/audio/`)
- **Purpose**: Handles all audio-related operations
- **Key Components**:
  - `AudioPlayer`: Main interface for audio playback control
  - `AudioEngine`: Core audio processing engine
  - `AudioFormat`: Supported audio format definitions
  - `AudioDecoder`: Audio file decoding utilities
  - `PlaybackState`: Enumeration of player states (playing, paused, stopped)

#### Playlist Module (`core/playlist/`)
- **Purpose**: Manages playlist creation, modification, and playback order
- **Key Components**:
  - `PlaylistManager`: Central playlist management
  - `PlaylistItem`: Individual playlist entries
  - `ShuffleStrategy`: Different shuffle algorithms
  - `RepeatMode`: Repeat mode implementations

#### Library Module (`core/library/`)
- **Purpose**: Manages the music library and metadata
- **Key Components**:
  - `LibraryScanner`: Scans directories for music files
  - `MetadataExtractor`: Extracts ID3 tags and metadata
  - `LibraryOrganizer`: Organizes music by artist, album, genre
  - `SearchEngine`: Provides search functionality

### 2. Data Layer (`data/`)

The data layer handles all data persistence and retrieval operations.

#### Models (`data/models/`)
- **Purpose**: Domain objects representing core entities
- **Key Components**:
  - `Song`: Represents a music track with metadata
  - `Artist`: Represents a music artist
  - `Album`: Represents an album with tracks
  - `Playlist`: Represents a user-created playlist
  - `Genre`: Represents music genres
  - `UserPreferences`: User settings and preferences

#### Repositories (`data/repositories/`)
- **Purpose**: Data access layer following repository pattern
- **Key Components**:
  - `SongRepository`: CRUD operations for songs
  - `PlaylistRepository`: Playlist persistence
  - `LibraryRepository`: Library data management
  - `PreferencesRepository`: User preferences storage

#### Storage (`data/storage/`)
- **Purpose**: File system and database operations
- **Key Components**:
  - `FileSystemStorage`: File system operations
  - `DatabaseStorage`: Local database management
  - `ConfigurationStorage`: Application configuration
  - `CacheStorage`: Temporary data caching

### 3. UI Layer (`ui/`)

The presentation layer built with JavaFX following the MVP pattern.

#### Controllers (`ui/controllers/`)
- **Purpose**: JavaFX controllers managing user interactions
- **Key Components**:
  - `MainController`: Main window controller
  - `PlayerController`: Audio player controls
  - `LibraryController`: Music library view
  - `PlaylistController`: Playlist management
  - `SettingsController`: Application settings

#### Views (`ui/views/`)
- **Purpose**: Custom view components for complex UI elements
- **Key Components**:
  - `WaveformView`: Audio waveform visualization
  - `SpectrumView`: Audio spectrum analyzer
  - `AlbumArtView`: Album artwork display
  - `ProgressView`: Playback progress indicator

#### Components (`ui/components/`)
- **Purpose**: Reusable UI components
- **Key Components**:
  - `CustomButton`: Styled button components
  - `VolumeSlider`: Custom volume control
  - `SeekBar`: Playback position control
  - `EqualizerPanel`: Audio equalizer interface

### 4. Service Layer (`services/`)

Business services that coordinate between different layers.

- **PlayerService**: Coordinates audio playback with UI
- **LibraryService**: Manages library operations
- **PlaylistService**: Handles playlist logic
- **MetadataService**: Manages metadata operations
- **ConfigurationService**: Application configuration management
- **NotificationService**: User notifications and alerts

### 5. Utilities (`utils/`)

Common utility classes used throughout the application.

- **FileUtils**: File system utilities
- **AudioUtils**: Audio format and conversion utilities
- **ImageUtils**: Image processing for album art
- **StringUtils**: String manipulation utilities
- **DateUtils**: Date and time formatting
- **ValidationUtils**: Input validation helpers

### 6. Configuration (`config/`)

Application configuration and settings management.

- **AppConfig**: Main application configuration
- **ThemeConfig**: UI theme settings
- **AudioConfig**: Audio engine configuration
- **KeyboardConfig**: Keyboard shortcuts configuration

## Design Patterns

### 1. Repository Pattern
Used in the data layer to abstract data access operations and provide a consistent interface for data retrieval.

### 2. Observer Pattern
Implemented for event handling between layers, particularly for playback state changes and library updates.

### 3. Factory Pattern
Used for creating different types of audio decoders and UI components based on runtime conditions.

### 4. Strategy Pattern
Implemented for different shuffle algorithms and audio processing strategies.

### 5. Singleton Pattern
Used for global services like configuration management and the main audio engine.

### 6. Command Pattern
Used for implementing undo/redo functionality in playlist management and settings.

## Dependency Flow

```
UI Layer → Service Layer → Core Layer → Data Layer
```

- **UI Layer** depends on **Service Layer** for business operations
- **Service Layer** coordinates between **Core** and **Data** layers
- **Core Layer** contains business logic independent of external concerns
- **Data Layer** handles persistence and external data sources

## Communication Between Layers

### Event-Driven Architecture
- Layers communicate through well-defined interfaces
- Events are used for loose coupling between components
- Observer pattern enables reactive updates across the application

### Dependency Injection
- Dependencies are injected rather than created within classes
- Enables easier testing and modularity
- Configuration-driven dependency resolution

## Testing Strategy

### Unit Tests
- Each layer has comprehensive unit tests
- Mock objects for external dependencies
- Focus on business logic validation

### Integration Tests
- Test interaction between layers
- Validate data flow and transformation
- End-to-end scenario testing

### UI Tests
- JavaFX TestFX for UI testing
- User interaction simulation
- Visual component validation

## Error Handling

### Centralized Error Management
- Custom exception hierarchy
- Error propagation between layers
- User-friendly error messages

### Logging Strategy
- SLF4J with Logback for structured logging
- Different log levels for different components
- Performance monitoring and debugging

## Performance Considerations

### Audio Processing
- Efficient audio streaming and buffering
- Background processing for metadata extraction
- Optimized file I/O operations

### UI Responsiveness
- Background tasks for heavy operations
- Progress indicators for long-running tasks
- Efficient list virtualization for large libraries

### Memory Management
- Proper resource cleanup
- Efficient caching strategies
- Memory-aware image loading for album art
