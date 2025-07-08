package com.musicplayer.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.musicplayer.data.models.Song;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Service for handling audio playback functionality.
 * Manages MediaPlayer instances and provides a clean API for audio operations.
 */
public class AudioPlayerService {
    private MediaPlayer mediaPlayer;
    private List<Song> currentPlaylist = new ArrayList<>();
    private int currentTrackIndex = -1;
    
    // Observable properties for UI binding
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final DoubleProperty currentTime = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalTime = new SimpleDoubleProperty(0.0);
    private final DoubleProperty volume = new SimpleDoubleProperty(0.5);
    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();
    
    public AudioPlayerService() {
        // Initialize volume listener
        volume.addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
    }
    
    /**
     * Sets the current playlist for playback.
     * 
     * @param songs List of songs to set as playlist
     */
    public void setPlaylist(List<Song> songs) {
        this.currentPlaylist = new ArrayList<>(songs);
        if (!songs.isEmpty() && currentTrackIndex == -1) {
            currentTrackIndex = 0;
        }
    }
    
    /**
     * Plays a specific song from the current playlist.
     * 
     * @param song The song to play
     */
    public void playSong(Song song) {
        int index = currentPlaylist.indexOf(song);
        if (index != -1) {
            currentTrackIndex = index;
            loadAndPlayCurrentSong();
        }
    }
    
    /**
     * Toggles play/pause state.
     */
    public void playPause() {
        if (mediaPlayer == null) {
            if (!currentPlaylist.isEmpty() && currentTrackIndex >= 0) {
                loadAndPlayCurrentSong();
            }
            return;
        }
        
        if (playing.get()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }
    
    /**
     * Stops playback.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    /**
     * Advances to the next track in the playlist.
     */
    public void nextTrack() {
        if (currentPlaylist.isEmpty()) return;
        
        currentTrackIndex = (currentTrackIndex + 1) % currentPlaylist.size();
        loadAndPlayCurrentSong();
    }
    
    /**
     * Goes back to the previous track in the playlist.
     */
    public void previousTrack() {
        if (currentPlaylist.isEmpty()) return;
        
        currentTrackIndex = currentTrackIndex > 0 ? currentTrackIndex - 1 : currentPlaylist.size() - 1;
        loadAndPlayCurrentSong();
    }
    
    /**
     * Seeks to a specific time position in the current track.
     * 
     * @param seconds Time position in seconds
     */
    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }
    
    /**
     * Loads and plays the current song based on currentTrackIndex.
     */
    private void loadAndPlayCurrentSong() {
        if (currentPlaylist.isEmpty() || currentTrackIndex < 0 || currentTrackIndex >= currentPlaylist.size()) {
            return;
        }
        
        Song song = currentPlaylist.get(currentTrackIndex);
        File audioFile = new File(song.getFilePath());
        
        if (!audioFile.exists()) {
            System.err.println("Audio file not found: " + song.getFilePath());
            // Try next track if current file doesn't exist
            nextTrack();
            return;
        }
        
        try {
            // Dispose of previous media player
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }
            
            Media media = new Media(audioFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            // Set up event handlers
            mediaPlayer.setOnReady(() -> {
                totalTime.set(mediaPlayer.getTotalDuration().toSeconds());
                mediaPlayer.setVolume(volume.get());
                System.out.println("Media ready: " + song.getTitle());
            });
            
            mediaPlayer.setOnPlaying(() -> {
                playing.set(true);
                System.out.println("Playing: " + song.getTitle());
            });
            
            mediaPlayer.setOnPaused(() -> {
                playing.set(false);
                System.out.println("Paused: " + song.getTitle());
            });
            
            mediaPlayer.setOnStopped(() -> {
                playing.set(false);
                System.out.println("Stopped: " + song.getTitle());
            });
            
            mediaPlayer.setOnEndOfMedia(() -> {
                playing.set(false);
                System.out.println("End of media: " + song.getTitle());
                nextTrack(); // Auto-play next track
            });
            
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (newTime != null) {
                    currentTime.set(newTime.toSeconds());
                }
            });
            
            mediaPlayer.setOnError(() -> {
                System.err.println("Media player error: " + mediaPlayer.getError().getMessage());
                playing.set(false);
                // Try next track on error
                nextTrack();
            });
            
            currentSong.set(song);
            mediaPlayer.play();
            
        } catch (Exception e) {
            System.err.println("Error loading audio file: " + e.getMessage());
            e.printStackTrace();
            playing.set(false);
            nextTrack();
        }
    }
    
    /**
     * Disposes of the media player resources.
     */
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        playing.set(false);
        currentSong.set(null);
    }
    
    // Property getters for UI binding
    public BooleanProperty playingProperty() { return playing; }
    public DoubleProperty currentTimeProperty() { return currentTime; }
    public DoubleProperty totalTimeProperty() { return totalTime; }
    public DoubleProperty volumeProperty() { return volume; }
    public ObjectProperty<Song> currentSongProperty() { return currentSong; }
    
    // Convenience getters
    public boolean isPlaying() { return playing.get(); }
    public double getCurrentTime() { return currentTime.get(); }
    public double getTotalTime() { return totalTime.get(); }
    public double getVolume() { return volume.get(); }
    public Song getCurrentSong() { return currentSong.get(); }
    
    // Convenience setters
    public void setVolume(double volume) { this.volume.set(volume); }
    
    /**
     * Gets the current playlist.
     * 
     * @return Copy of the current playlist
     */
    public List<Song> getCurrentPlaylist() {
        return new ArrayList<>(currentPlaylist);
    }
    
    /**
     * Gets the current track index.
     * 
     * @return Current track index, or -1 if no track is selected
     */
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
}
