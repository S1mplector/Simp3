package com.musicplayer.services;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.repositories.SongRepository;
import com.musicplayer.utils.MusicScanner;

/**
 * Service class for managing the music library, including folder scanning and song management.
 * This class encapsulates all music library operations and provides a clean interface
 * for the UI controllers.
 */
public class MusicLibraryManager {
    
    private final SongRepository songRepository;
    private File currentMusicFolder;
    
    // Callback for notifying when library changes occur
    private Consumer<List<Song>> libraryUpdateCallback;
    
    public MusicLibraryManager(SongRepository songRepository) {
        this.songRepository = songRepository;
    }
    
    /**
     * Sets a callback to be notified when the library is updated.
     * 
     * @param callback Consumer that receives the updated list of songs
     */
    public void setLibraryUpdateCallback(Consumer<List<Song>> callback) {
        this.libraryUpdateCallback = callback;
    }
    
    /**
     * Scans a music folder and adds all found songs to the library.
     * This method runs asynchronously to avoid blocking the UI.
     * 
     * @param folder The folder to scan for music files
     */
    public void scanMusicFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder provided for scanning: " + folder);
            return;
        }
        
        this.currentMusicFolder = folder;
        
        // Run scanning in a separate thread to avoid blocking the UI
        Thread scanThread = new Thread(() -> {
            try {
                System.out.println("Starting scan of folder: " + folder.getAbsolutePath());
                
                // Clear existing songs from repository
                clearLibrary();
                
                // Scan the folder for music files
                List<Song> scannedSongs = MusicScanner.scanDirectory(folder);
                
                // Add all scanned songs to the repository
                for (Song song : scannedSongs) {
                    songRepository.save(song);
                }
                
                System.out.println("Scan complete. Found " + scannedSongs.size() + " songs.");
                
                // Notify callback of library update (if set)
                if (libraryUpdateCallback != null) {
                    // Run callback on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        libraryUpdateCallback.accept(getAllSongs());
                    });
                }
                
            } catch (Exception e) {
                System.err.println("Error during music folder scan: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        scanThread.setDaemon(true);
        scanThread.setName("MusicScanner-" + folder.getName());
        scanThread.start();
    }
    
    /**
     * Adds a single song to the library.
     * 
     * @param song The song to add
     */
    public void addSong(Song song) {
        songRepository.save(song);
        
        // Notify callback if set
        if (libraryUpdateCallback != null) {
            libraryUpdateCallback.accept(getAllSongs());
        }
    }
    
    /**
     * Removes a song from the library.
     * 
     * @param songId The ID of the song to remove
     */
    public void removeSong(long songId) {
        songRepository.delete(songId);
        
        // Notify callback if set
        if (libraryUpdateCallback != null) {
            libraryUpdateCallback.accept(getAllSongs());
        }
    }
    
    /**
     * Gets all songs in the library.
     * 
     * @return List of all songs
     */
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }
    
    /**
     * Gets a song by its ID.
     * 
     * @param id The song ID
     * @return The song, or null if not found
     */
    public Song getSongById(long id) {
        return songRepository.findById(id);
    }
    
    /**
     * Clears all songs from the library.
     */
    public void clearLibrary() {
        List<Song> allSongs = songRepository.findAll();
        for (Song song : allSongs) {
            songRepository.delete(song.getId());
        }
        
        // Notify callback if set
        if (libraryUpdateCallback != null) {
            libraryUpdateCallback.accept(getAllSongs());
        }
    }
    
    /**
     * Gets the currently set music folder.
     * 
     * @return The current music folder, or null if none is set
     */
    public File getCurrentMusicFolder() {
        return currentMusicFolder;
    }
    
    /**
     * Rescans the current music folder (if one is set).
     */
    public void rescanCurrentFolder() {
        if (currentMusicFolder != null) {
            scanMusicFolder(currentMusicFolder);
        }
    }
    
    /**
     * Gets the total number of songs in the library.
     * 
     * @return Number of songs
     */
    public int getSongCount() {
        return getAllSongs().size();
    }
}
