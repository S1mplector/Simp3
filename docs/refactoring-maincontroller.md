# MainController Refactoring Documentation

## Overview
This document describes the refactoring of MainController.java to address the "god object" anti-pattern by extracting functionality into focused, single-responsibility controllers.

## Problem Statement
MainController.java had grown to approximately 1,500 lines of code, handling too many responsibilities:
- Audio playback controls
- Playlist management
- Library management
- UI updates
- Settings management
- Visualizer controls
- And more...

This violated the Single Responsibility Principle and made the code difficult to maintain and test.

## Solution Approach
Extract functionality into separate, focused controllers with clear responsibilities:

### 1. AudioController (✓ Completed)
**Responsibility**: All audio playback functionality
- Play/pause/stop controls
- Volume and seeking
- Album art display
- Keyboard shortcuts for audio
- Playback mode controls (shuffle, repeat)
- Track navigation (previous/next)

**Key Changes**:
- Created `AudioController.java` (470 lines)
- Extracted all audio-related methods from MainController
- Created `IControllerCommunication` interface for inter-controller communication
- Updated MainController to delegate audio operations to AudioController

### 2. PlaylistController (Next Priority)
**Responsibility**: Playlist CRUD operations and management
- Create/rename/delete playlists
- Add/remove songs from playlists
- Playlist selection and display
- Drag-and-drop reordering

### 3. LibraryController
**Responsibility**: Music library operations
- Folder selection and scanning
- Library refresh/rescan
- Album and artist views
- Library statistics

### 4. VisualizerController
**Responsibility**: Audio visualizer management
- Visualizer initialization and cleanup
- Settings application
- Window state monitoring
- Spectrum data handling

### 5. Additional Controllers
- **SearchController**: Search functionality for songs and playlists
- **FavoritesController**: Favorite songs management
- **MiniPlayerController**: Mini player window coordination
- **SettingsController**: Already exists, may need enhancement
- **UICoordinator**: Cross-cutting UI concerns and controller coordination

## Implementation Details

### AudioController Implementation
1. **Dependency Injection**: Since we're not splitting FXML files yet, we use manual dependency injection:
   ```java
   audioController.setUIComponents(
       playPauseButton, previousButton, nextButton,
       timeSlider, volumeSlider, volumeIcon,
       volumePercentageLabel, currentTimeLabel, totalTimeLabel,
       albumArtContainer, albumArtImageView, albumArtImageView2,
       songTitleLabel, songArtistLabel
   );
   ```

2. **Initialization Order**: Fixed initialization to ensure collections exist before use:
   ```java
   // Initialize collections BEFORE creating AudioController
   songs = FXCollections.observableArrayList();
   playlists = FXCollections.observableArrayList();
   
   // Then create and initialize AudioController
   audioController = new AudioController();
   ```

3. **Playlist Synchronization**: Added `updatePlaylist()` calls to keep AudioController in sync:
   ```java
   audioController.updatePlaylist(songs);
   ```

4. **Method Visibility**: Changed handler methods from private to public for cross-controller access.

### IControllerCommunication Interface
Created to enable loose coupling between controllers:
```java
public interface IControllerCommunication {
    // Audio state callbacks
    void onSongChanged(Song newSong);
    void onPlaybackStateChanged(boolean isPlaying);
    void onVolumeChanged(double volume);
    
    // Library callbacks
    void onLibraryUpdated(List<Song> songs);
    void onAlbumSelected(Album album);
    
    // Playlist callbacks
    void onPlaylistCreated(Playlist playlist);
    void onPlaylistUpdated(Playlist playlist);
    void onPlaylistDeleted(String playlistId);
    void onPlaylistSelected(Playlist playlist);
}
```

## Benefits Achieved
1. **Reduced Complexity**: MainController reduced from ~1,500 to ~1,350 lines (will decrease further with additional extractions)
2. **Single Responsibility**: AudioController now has one clear purpose
3. **Improved Testability**: Can unit test audio functionality in isolation
4. **Better Maintainability**: Audio-related changes now localized to AudioController
5. **Reusability**: AudioController can potentially be reused in other views

## Next Steps
1. Extract PlaylistController (highest priority after AudioController)
2. Extract LibraryController
3. Extract VisualizerController
4. Continue with remaining controllers
5. Eventually split FXML files to support multiple controllers properly
6. Add comprehensive unit tests for each controller

## Testing Confirmation
The application has been tested after refactoring and confirmed to work normally with all audio functionality intact.