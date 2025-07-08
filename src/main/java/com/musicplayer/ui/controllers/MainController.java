package com.musicplayer.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.repositories.InMemoryPlaylistRepository;
import com.musicplayer.data.repositories.InMemorySongRepository;
import com.musicplayer.data.repositories.PlaylistRepository;
import com.musicplayer.data.repositories.SongRepository;
import com.musicplayer.services.LibraryService;
import com.musicplayer.services.PlaylistService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
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
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    
    private LibraryService libraryService;
    private PlaylistService playlistService;
    private ObservableList<Song> songs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize repositories and services
        SongRepository songRepository = new InMemorySongRepository();
        PlaylistRepository playlistRepository = new InMemoryPlaylistRepository();
        libraryService = new LibraryService(songRepository);
        playlistService = new PlaylistService(playlistRepository);
        
        // Initialize the songs list
        songs = FXCollections.observableArrayList();
        
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
    
    @FXML
    private void handleAddFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Folder");
        File selectedDirectory = directoryChooser.showDialog(null);
        
        if (selectedDirectory != null) {
            // TODO: Implement folder scanning logic
            System.out.println("Selected folder: " + selectedDirectory.getAbsolutePath());
            
            // For now, add a sample song
            Song sampleSong = new Song();
            sampleSong.setTitle("Sample Song");
            sampleSong.setArtist("Sample Artist");
            sampleSong.setAlbum("Sample Album");
            sampleSong.setDuration(225); // 3:45 in seconds
            sampleSong.setFilePath(selectedDirectory.getAbsolutePath() + "/sample.mp3");
            
            libraryService.addSong(sampleSong);
            refreshSongsList();
        }
    }
    
    @FXML
    private void handlePlayPause() {
        // TODO: Implement play/pause logic
        if (playPauseButton.getText().equals("Play")) {
            playPauseButton.setText("Pause");
            System.out.println("Playing music...");
        } else {
            playPauseButton.setText("Play");
            System.out.println("Pausing music...");
        }
    }
    
    @FXML
    private void handlePrevious() {
        // TODO: Implement previous track logic
        System.out.println("Previous track");
    }
    
    @FXML
    private void handleNext() {
        // TODO: Implement next track logic
        System.out.println("Next track");
    }
    
    private void refreshSongsList() {
        songs.clear();
        songs.addAll(libraryService.getAllSongs());
    }
    
    private String formatDuration(long durationInSeconds) {
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
