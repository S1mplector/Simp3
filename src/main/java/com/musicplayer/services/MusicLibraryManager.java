package com.musicplayer.services;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.repositories.PersistentSongRepository;
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
        
        // If using persistent storage, log existing data
        if (songRepository instanceof PersistentSongRepository) {
            PersistentSongRepository persistentRepo = (PersistentSongRepository) songRepository;
            if (persistentRepo.size() > 0) {
                System.out.println("Found existing library with " + persistentRepo.size() + " songs");
                // Try to restore last music folder from first song's path
                List<Song> songs = persistentRepo.findAll();
                if (!songs.isEmpty() && songs.get(0).getFilePath() != null) {
                    File songFile = new File(songs.get(0).getFilePath());
                    if (songFile.exists()) {
                        this.currentMusicFolder = songFile.getParentFile();
                        while (currentMusicFolder != null && !isMusicFolder(currentMusicFolder)) {
                            currentMusicFolder = currentMusicFolder.getParentFile();
                        }
                    }
                }
            }
        }
    }
    
    private boolean isMusicFolder(File folder) {
        // Simple heuristic: folder contains music files
        if (folder == null || !folder.isDirectory()) return false;
        File[] files = folder.listFiles();
        if (files == null) return false;
        
        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".mp3") || name.endsWith(".m4a") || 
                name.endsWith(".wav") || name.endsWith(".flac")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Initializes the library by loading existing data and notifying the callback.
     * Should be called after the UI is ready to receive updates.
     */
    public void initializeLibrary() {
        List<Song> existingSongs = getAllSongs();
        if (!existingSongs.isEmpty() && libraryUpdateCallback != null) {
            System.out.println("Initializing UI with " + existingSongs.size() + " existing songs");
            libraryUpdateCallback.accept(existingSongs);
        }
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
     * @param clearExisting Whether to clear existing songs before scanning
     */
    public void scanMusicFolder(File folder, boolean clearExisting) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder provided for scanning: " + folder);
            return;
        }

        this.currentMusicFolder = folder;
        
        // Run scanning in a separate thread to avoid blocking the UI
        Thread scanThread = new Thread(() -> {
            try {
                System.out.println("Starting scan of folder: " + folder.getAbsolutePath());
                
                // Clear existing songs from repository if requested
                if (clearExisting) {
                    clearLibrary();
                }
                
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
     * Scans a music folder and adds all found songs to the library.
     * This method clears existing songs before scanning.
     * 
     * @param folder The folder to scan for music files
     */
    public void scanMusicFolder(File folder) {
        scanMusicFolder(folder, true);
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
     * Rescans the music library by checking all existing songs and their directories.
     * This method:
     * - Removes songs whose files no longer exist
     * - Finds new songs in existing directories
     * - Preserves songs from multiple directories
     */
    public void rescanCurrentFolder() {
        Thread rescanThread = new Thread(() -> {
            try {
                System.out.println("Starting intelligent rescan of music library...");
                
                // Get all existing songs
                List<Song> existingSongs = getAllSongs();
                if (existingSongs.isEmpty() && currentMusicFolder == null) {
                    System.out.println("No existing songs or music folder to rescan.");
                    return;
                }
                
                // Track which songs still exist
                java.util.Set<String> existingFilePaths = new java.util.HashSet<>();
                java.util.Set<File> directoriesToScan = new java.util.HashSet<>();
                
                // First pass: check existing songs and collect directories
                for (Song song : existingSongs) {
                    if (song.getFilePath() != null) {
                        File songFile = new File(song.getFilePath());
                        if (songFile.exists()) {
                            existingFilePaths.add(song.getFilePath());
                            // Add parent directory to scan list
                            File parentDir = songFile.getParentFile();
                            if (parentDir != null && parentDir.exists()) {
                                directoriesToScan.add(parentDir);
                            }
                        } else {
                            // File no longer exists, remove the song
                            System.out.println("Removing missing file: " + song.getFilePath());
                            songRepository.delete(song.getId());
                        }
                    }
                }
                
                // Also add the current music folder if set
                if (currentMusicFolder != null && currentMusicFolder.exists()) {
                    directoriesToScan.add(currentMusicFolder);
        }
                
                // Find common root directories to avoid scanning subdirectories multiple times
                java.util.Set<File> rootDirectories = findRootDirectories(directoriesToScan);
                
                // Second pass: scan directories for new songs
                System.out.println("Scanning " + rootDirectories.size() + " root directories for new songs...");
                int newSongCount = 0;
                
                for (File dir : rootDirectories) {
                    System.out.println("Scanning: " + dir.getAbsolutePath());
                    List<Song> scannedSongs = MusicScanner.scanDirectory(dir);
                    
                    // Add only new songs
                    for (Song song : scannedSongs) {
                        if (!existingFilePaths.contains(song.getFilePath())) {
                            songRepository.save(song);
                            newSongCount++;
                        }
                    }
                }
                
                System.out.println("Rescan complete. Added " + newSongCount + " new songs. Total: " + getSongCount());
                
                // Notify callback of library update
                if (libraryUpdateCallback != null) {
                    javafx.application.Platform.runLater(() -> {
                        libraryUpdateCallback.accept(getAllSongs());
                    });
                }
                
            } catch (Exception e) {
                System.err.println("Error during rescan: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        rescanThread.setDaemon(true);
        rescanThread.setName("MusicRescan");
        rescanThread.start();
    }
    
    /**
     * Finds the root directories from a set of directories.
     * Removes subdirectories if their parent is already in the set.
     */
    private java.util.Set<File> findRootDirectories(java.util.Set<File> directories) {
        java.util.Set<File> roots = new java.util.HashSet<>();
        
        for (File dir : directories) {
            boolean isSubdirectory = false;
            for (File other : directories) {
                if (dir != other && isSubdirectoryOf(dir, other)) {
                    isSubdirectory = true;
                    break;
                }
            }
            if (!isSubdirectory) {
                roots.add(dir);
            }
        }
        
        return roots;
    }
    
    /**
     * Checks if child is a subdirectory of parent.
     */
    private boolean isSubdirectoryOf(File child, File parent) {
        File current = child;
        while (current != null) {
            if (current.equals(parent)) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }
    

    
    /**
     * Gets the total number of songs in the library.
     * 
     * @return Number of songs
     */
    public int getSongCount() {
        return getAllSongs().size();
    }
    
    /**
     * Forces a save of all current data to persistent storage (if supported).
     * This is useful for ensuring data is persisted before application shutdown.
     */
    public void forceSave() {
        if (songRepository instanceof PersistentSongRepository) {
            ((PersistentSongRepository) songRepository).forceSave();
        }
    }
    
    /**
     * Checks if there is existing data in the library.
     * 
     * @return true if the library contains songs, false otherwise
     */
    public boolean hasExistingData() {
        return getSongCount() > 0;
    }
}
