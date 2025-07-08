package com.musicplayer.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.repositories.InMemoryPlaylistRepository;
import com.musicplayer.data.repositories.InMemorySongRepository;
import com.musicplayer.data.repositories.PlaylistRepository;
import com.musicplayer.data.repositories.SongRepository;
import com.musicplayer.services.AudioPlayerService;
import com.musicplayer.services.LibraryService;
import com.musicplayer.services.MusicLibraryManager;
import com.musicplayer.services.PlaylistService;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

public class MainController implements Initializable {
    
    @FXML private ListView<String> libraryListView;
    @FXML private ListView<String> playlistsListView;
    @FXML private TableView<Song> songsTableView;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> durationColumn;
    @FXML private Button previousButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;
    @FXML private Button selectMusicFolderButton;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    
    private LibraryService libraryService;
    private MusicLibraryManager musicLibraryManager;
    private PlaylistService playlistService;
    private AudioPlayerService audioPlayerService;
    private ObservableList<Song> songs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize repositories and services
        SongRepository songRepository = new InMemorySongRepository();
        PlaylistRepository playlistRepository = new InMemoryPlaylistRepository();
        libraryService = new LibraryService(songRepository);
        playlistService = new PlaylistService(playlistRepository);
        
        // Initialize the music library manager
        musicLibraryManager = new MusicLibraryManager(songRepository);
        
        // Initialize audio player service
        audioPlayerService = new AudioPlayerService();
        
        // Initialize the songs list
        songs = FXCollections.observableArrayList();
        
        // Set up callback to update UI when library changes
        musicLibraryManager.setLibraryUpdateCallback(updatedSongs -> {
            songs.clear();
            songs.addAll(updatedSongs);
            // Update audio player playlist when library changes
            audioPlayerService.setPlaylist(songs);
        });
        
        // Set up audio controls
        setupAudioControls();
        
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
        
        // Initialize playlists list
        ObservableList<String> playlistItems = FXCollections.observableArrayList("My Playlist 1", "Favorites");
        playlistsListView.setItems(playlistItems);
        
        System.out.println("MainController initialized.");
    }
    
    private void setupAudioControls() {
        // Bind play/pause button text to playing state
        playPauseButton.textProperty().bind(
            Bindings.when(audioPlayerService.playingProperty())
                .then("Pause")
                .otherwise("Play")
        );
        
        // Bind time slider to current time (with proper max value)
        timeSlider.valueProperty().bind(audioPlayerService.currentTimeProperty());
        timeSlider.maxProperty().bind(audioPlayerService.totalTimeProperty());
        
        // Set up time slider for seeking
        timeSlider.setOnMouseClicked(event -> {
            double seekTime = (event.getX() / timeSlider.getWidth()) * audioPlayerService.getTotalTime();
            audioPlayerService.seek(seekTime);
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
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Song selectedSong = row.getItem();
                    playSelectedSong(selectedSong);
                }
            });
            return row;
        });
    }
    
    private void playSelectedSong(Song song) {
        // Set current songs as playlist and play selected song
        audioPlayerService.setPlaylist(songs);
        audioPlayerService.playSong(song);
    }
    
    @FXML
    private void handleSelectMusicFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Folder");
        File selectedDirectory = directoryChooser.showDialog(selectMusicFolderButton.getScene().getWindow());
        
        if (selectedDirectory != null) {
            System.out.println("Selected music folder: " + selectedDirectory.getAbsolutePath());
            // Use the MusicLibraryManager to handle scanning
            musicLibraryManager.scanMusicFolder(selectedDirectory);
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
                audioPlayerService.playSong(songs.get(0));
            }
        } else {
            audioPlayerService.playPause();
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
    
    private String formatDuration(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Cleanup method to dispose of resources when the application is closing.
     */
    public void cleanup() {
        if (audioPlayerService != null) {
            audioPlayerService.dispose();
        }
    }
}
