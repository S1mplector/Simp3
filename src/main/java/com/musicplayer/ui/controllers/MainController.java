package com.musicplayer.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.models.Playlist;
import com.musicplayer.data.repositories.InMemoryPlaylistRepository;
import com.musicplayer.data.repositories.PersistentSongRepository;
import com.musicplayer.data.repositories.PersistentPlaylistRepository;
import com.musicplayer.data.repositories.PlaylistRepository;
import com.musicplayer.data.repositories.SongRepository;
import com.musicplayer.data.storage.JsonLibraryStorage;
import com.musicplayer.data.storage.LibraryStorage;
import com.musicplayer.services.AudioPlayerService;
import com.musicplayer.services.LibraryService;
import com.musicplayer.services.MusicLibraryManager;
import com.musicplayer.services.PlaylistService;
import com.musicplayer.services.PlaylistManager;
import com.musicplayer.ui.components.PlaylistCell;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.TextInputDialog;

public class MainController implements Initializable {
    
    @FXML private ListView<String> libraryListView;
    @FXML private ListView<Playlist> playlistsListView;
    @FXML private TableView<Song> songsTableView;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> durationColumn;
    @FXML private Button previousButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;
    @FXML private Button selectMusicFolderButton;
    @FXML private Button addPlaylistButton;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    
    // Icons for play/pause button
    private Image playIcon;
    private Image pauseIcon;
    private ImageView playPauseImageView;
    
    private LibraryService libraryService;
    private MusicLibraryManager musicLibraryManager;
    private PlaylistService playlistService;
    private PlaylistManager playlistManager;
    private AudioPlayerService audioPlayerService;
    private ObservableList<Song> songs;
    private ObservableList<Playlist> playlists;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize storage and repositories
        LibraryStorage storage = new JsonLibraryStorage();
        SongRepository songRepository = new PersistentSongRepository(storage);
        PlaylistRepository playlistRepository = new PersistentPlaylistRepository(storage);
        libraryService = new LibraryService(songRepository);
        playlistService = new PlaylistService(playlistRepository);
        
        // Initialize the music library manager
        musicLibraryManager = new MusicLibraryManager(songRepository);
        
        // Initialize the playlist manager
        playlistManager = new PlaylistManager(playlistRepository);
        
        // Initialize audio player service
        audioPlayerService = new AudioPlayerService();
        
        // Initialize the songs and playlists lists
        songs = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        
        // Set up callback to update UI when library changes
        musicLibraryManager.setLibraryUpdateCallback(updatedSongs -> {
            songs.clear();
            songs.addAll(updatedSongs);
            // Update audio player playlist when library changes
            audioPlayerService.setPlaylist(songs);
        });
        
        // Set up callback to update UI when playlists change
        playlistManager.setPlaylistUpdateCallback(updatedPlaylists -> {
            playlists.clear();
            playlists.addAll(updatedPlaylists);
        });
        
        // Load existing library data if available
        musicLibraryManager.initializeLibrary();
        
        // Load existing playlists if available
        playlistManager.initializePlaylists();
        
        // Set up playlist controls
        setupPlaylistControls();
        
        // Set up audio controls
        setupAudioControls();
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Set up table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(cellData -> {
            long durationInSeconds = cellData.getValue().getDuration();
            return new javafx.beans.property.SimpleStringProperty(formatDuration(durationInSeconds));
        });
        
        // Bind the table to the songs list
        songsTableView.setItems(songs);
        
        // Initialize library list
        ObservableList<String> libraryItems = FXCollections.observableArrayList("All Songs", "Artists", "Albums");
        libraryListView.setItems(libraryItems);
        
        System.out.println("MainController initialized.");
    }
    
    private void setupAudioControls() {
        // Initialize play/pause icons
        playIcon = new Image(getClass().getResourceAsStream("/images/icons/play.png"));
        pauseIcon = new Image(getClass().getResourceAsStream("/images/icons/pause.png"));
        
        // Get the ImageView from the play/pause button (it should already be set in FXML)
        playPauseImageView = (ImageView) playPauseButton.getGraphic();
        
        // Set initial icon
        playPauseImageView.setImage(playIcon);
        
        // Bind play/pause button icon to playing state
        audioPlayerService.playingProperty().addListener((obs, oldPlaying, newPlaying) -> {
            playPauseImageView.setImage(newPlaying ? pauseIcon : playIcon);
        });
        
        // Bind time slider to current time (with proper max value)
        timeSlider.valueProperty().bind(audioPlayerService.currentTimeProperty());
        timeSlider.maxProperty().bind(audioPlayerService.totalTimeProperty());
        
        // Set up time slider for seeking (both click and drag)
        timeSlider.setOnMouseClicked(event -> {
            if (audioPlayerService.getTotalTime() > 0) {
                double seekTime = (event.getX() / timeSlider.getWidth()) * audioPlayerService.getTotalTime();
                audioPlayerService.seek(seekTime);
            }
        });
        
        // Allow dragging to seek
        timeSlider.setOnMouseDragged(event -> {
            if (audioPlayerService.getTotalTime() > 0) {
                double seekTime = (event.getX() / timeSlider.getWidth()) * audioPlayerService.getTotalTime();
                audioPlayerService.seek(seekTime);
            }
        });
        
        // Bind volume slider to volume property
        volumeSlider.valueProperty().bindBidirectional(audioPlayerService.volumeProperty());
        volumeSlider.setMax(1.0); // Volume range 0.0 to 1.0
        volumeSlider.setValue(0.5); // Default volume
        
        // Bind time labels
        currentTimeLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> formatDuration((long) audioPlayerService.getCurrentTime()),
                audioPlayerService.currentTimeProperty()
            )
        );
        
        totalTimeLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> formatDuration((long) audioPlayerService.getTotalTime()),
                audioPlayerService.totalTimeProperty()
            )
        );
        
        // Set up table double-click to play song
        songsTableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<Song>() {
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    
                    // Highlight currently playing song
                    if (song != null && !empty) {
                        if (song.equals(audioPlayerService.getCurrentSong()) && audioPlayerService.isPlaying()) {
                            setStyle("-fx-background-color: lightblue;");
                        } else if (song.equals(audioPlayerService.getCurrentSong())) {
                            setStyle("-fx-background-color: lightgray;");
                        } else {
                            setStyle("");
                        }
                    } else {
                        setStyle("");
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Song selectedSong = row.getItem();
                    playSelectedSong(selectedSong);
                }
            });
            return row;
        });
        
        // Update table row highlighting when song changes
        audioPlayerService.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            songsTableView.refresh(); // Refresh to update row highlighting
        });
        
        audioPlayerService.playingProperty().addListener((obs, oldPlaying, newPlaying) -> {
            songsTableView.refresh(); // Refresh to update row highlighting
        });
    }
    
    private void setupKeyboardShortcuts() {
        // Set up keyboard shortcuts for the main scene
        // This will be called when the scene is available
        javafx.application.Platform.runLater(() -> {
            if (songsTableView.getScene() != null) {
                songsTableView.getScene().setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case SPACE:
                            handlePlayPause();
                            event.consume();
                            break;
                        case LEFT:
                            if (event.isControlDown()) {
                                handlePrevious();
                                event.consume();
                            }
                            break;
                        case RIGHT:
                            if (event.isControlDown()) {
                                handleNext();
                                event.consume();
                            }
                            break;
                        case UP:
                            if (event.isControlDown()) {
                                double currentVolume = audioPlayerService.getVolume();
                                audioPlayerService.setVolume(Math.min(1.0, currentVolume + 0.1));
                                event.consume();
                            }
                            break;
                        case DOWN:
                            if (event.isControlDown()) {
                                double currentVolume = audioPlayerService.getVolume();
                                audioPlayerService.setVolume(Math.max(0.0, currentVolume - 0.1));
                                event.consume();
                            }
                            break;
                        default:
                            break;
                    }
                });
            }
        });
    }
    
    private void playSelectedSong(Song song) {
        // Set current songs as playlist and play selected song
        audioPlayerService.setPlaylist(songs);
        audioPlayerService.playTrack(song);
    }
    
    @FXML
    private void handleSelectMusicFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Folder");
        File selectedDirectory = directoryChooser.showDialog(selectMusicFolderButton.getScene().getWindow());
        
        if (selectedDirectory != null) {
            System.out.println("Selected music folder: " + selectedDirectory.getAbsolutePath());
            
            // Check if there's existing data and ask user what to do
            if (musicLibraryManager.hasExistingData()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Music Library");
                alert.setHeaderText("Existing music library found");
                alert.setContentText("You have " + musicLibraryManager.getSongCount() + 
                                   " songs in your library. What would you like to do?");
                
                ButtonType clearAndScan = new ButtonType("Clear & Scan New Folder");
                ButtonType addToLibrary = new ButtonType("Add to Existing Library");
                ButtonType cancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
                
                alert.getButtonTypes().setAll(clearAndScan, addToLibrary, cancel);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == clearAndScan) {
                        musicLibraryManager.scanMusicFolder(selectedDirectory, true);
                    } else if (response == addToLibrary) {
                        musicLibraryManager.scanMusicFolder(selectedDirectory, false);
                    }
                    // If cancel, do nothing
                });
            } else {
                // No existing data, just scan normally
                musicLibraryManager.scanMusicFolder(selectedDirectory, true);
            }
        }
    }
    
    @FXML
    private void handlePlayPause() {
        if (songs.isEmpty()) {
            System.out.println("No songs in library to play");
            return;
        }
        
        // If no song is currently selected, start with the first song
        if (audioPlayerService.getCurrentSong() == null) {
            audioPlayerService.setPlaylist(songs);
            if (!songs.isEmpty()) {
                audioPlayerService.playTrack(songs.get(0));
            }
        } else {
            audioPlayerService.togglePlayPause();
        }
    }
    
    @FXML
    private void handlePrevious() {
        audioPlayerService.previousTrack();
    }
    
    @FXML
    private void handleNext() {
        audioPlayerService.nextTrack();
    }
    
    /**
     * Stop playback completely.
     */
    public void stopPlayback() {
        audioPlayerService.stop();
    }
    
    /**
     * Get information about the currently playing song.
     * 
     * @return String with current song info, or null if no song is playing
     */
    public String getCurrentSongInfo() {
        Song currentSong = audioPlayerService.getCurrentSong();
        if (currentSong != null) {
            return String.format("%s - %s (%s)", 
                currentSong.getArtist(), 
                currentSong.getTitle(), 
                currentSong.getAlbum());
        }
        return null;
    }
    
    private String formatDuration(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Cleanup method to dispose of resources when the application is closing.
     */
    public void cleanup() {
        System.out.println("Shutting down application...");
        
        // Save library data to persistent storage
        if (musicLibraryManager != null) {
            musicLibraryManager.forceSave();
            System.out.println("Library data saved to storage");
        }
        
        // Save playlist data to persistent storage
        if (playlistManager != null) {
            playlistManager.forceSave();
            System.out.println("Playlist data saved to storage");
        }
        
        // Dispose of audio resources
        if (audioPlayerService != null) {
            audioPlayerService.dispose();
            System.out.println("Audio resources disposed");
        }
        
        System.out.println("Application shutdown complete");
    }
    
    /**
     * Sets up the playlist controls and ListView.
     */
    private void setupPlaylistControls() {
        // Set up the playlist ListView
        playlistsListView.setItems(playlists);
        
        // Create custom cell factory for playlists
        playlistsListView.setCellFactory(listView -> {
            PlaylistCell cell = new PlaylistCell();
            
            // Set up delete callback
            cell.setOnDelete(playlist -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Playlist");
                alert.setHeaderText("Delete playlist '" + playlist.getName() + "'?");
                alert.setContentText("This action cannot be undone.");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        playlistManager.deletePlaylist(playlist.getId());
                    }
                });
            });
            
            // Set up rename callback
            cell.setOnRename(renameRequest -> {
                boolean success = playlistManager.renamePlaylist(
                    renameRequest.getPlaylist().getId(), 
                    renameRequest.getNewName()
                );
                
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Rename Failed");
                    alert.setHeaderText("Could not rename playlist");
                    alert.setContentText("A playlist with that name already exists or the name is invalid.");
                    alert.showAndWait();
                }
            });
            
            return cell;
        });
        
        // Handle playlist selection to show songs
        playlistsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldPlaylist, newPlaylist) -> {
                if (newPlaylist != null) {
                    songs.clear();
                    songs.addAll(newPlaylist.getSongs());
                    audioPlayerService.setPlaylist(songs);
                }
            }
        );
    }
    
    /**
     * Handles the add playlist button action.
     */
    @FXML
    private void handleAddPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Enter playlist name:");
        
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    playlistManager.createPlaylist(name.trim());
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Creation Failed");
                    alert.setHeaderText("Could not create playlist");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
}
