package com.musicplayer.ui.controllers;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.musicplayer.data.models.Song;
import com.musicplayer.services.AudioConversionService;
import com.musicplayer.services.MusicLibraryManager;
import com.musicplayer.ui.dialogs.AudioConversionDialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Controller for managing audio conversion functionality.
 * Handles conversion of audio files to JavaFX-compatible formats for full feature support.
 */
public class AudioConversionController {
    
    private final AudioConversionService conversionService;
    private final MusicLibraryManager musicLibraryManager;
    
    public AudioConversionController(MusicLibraryManager musicLibraryManager) {
        this.musicLibraryManager = musicLibraryManager;
        this.conversionService = new AudioConversionService();
    }
    
    /**
     * Show conversion dialog for selected songs.
     */
    public void showConversionDialog(Window owner, List<Song> selectedSongs) {
        if (selectedSongs == null || selectedSongs.isEmpty()) {
            showInfo(owner, "No Selection", "Please select songs to convert.");
            return;
        }
        
        // Convert songs to files
        List<File> filesToConvert = selectedSongs.stream()
            .map(song -> new File(song.getFilePath()))
            .filter(File::exists)
            .collect(Collectors.toList());
        
        if (filesToConvert.isEmpty()) {
            showInfo(owner, "No Valid Files", "No valid audio files found for conversion.");
            return;
        }
        
        // Filter out files that are already JavaFX compatible
        List<File> convertibleFiles = filesToConvert.stream()
            .filter(file -> {
                String ext = getFileExtension(file.getName());
                return conversionService.isConvertible(ext) && !conversionService.isJavaFXCompatible(ext);
            })
            .collect(Collectors.toList());
        
        if (convertibleFiles.isEmpty()) {
            showInfo(owner, "No Conversion Needed", 
                "All selected files are already in JavaFX-compatible formats (WAV/AIFF).");
            return;
        }
        
        // Show conversion dialog
        AudioConversionDialog dialog = new AudioConversionDialog(owner, conversionService, convertibleFiles);
        dialog.showAndWait();
        
        // Refresh library after conversion to pick up new files
        refreshLibraryAfterConversion();
    }
    
    /**
     * Show conversion dialog for all library songs that can benefit from conversion.
     */
    public void showBulkConversionDialog(Window owner) {
        List<Song> allSongs = musicLibraryManager.getAllSongs();
        
        if (allSongs.isEmpty()) {
            showInfo(owner, "Empty Library", "No songs found in your library.");
            return;
        }
        
        // Filter songs that would benefit from conversion
        List<File> convertibleFiles = allSongs.stream()
            .map(song -> new File(song.getFilePath()))
            .filter(File::exists)
            .filter(file -> {
                String ext = getFileExtension(file.getName());
                return conversionService.isConvertible(ext) && !conversionService.isJavaFXCompatible(ext);
            })
            .collect(Collectors.toList());
        
        if (convertibleFiles.isEmpty()) {
            showInfo(owner, "No Conversion Needed", 
                "All songs in your library are already in JavaFX-compatible formats or cannot be converted.");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Bulk Audio Conversion");
        confirmAlert.setHeaderText("Convert Library for Full JavaFX Support");
        confirmAlert.setContentText(
            String.format("Found %d songs that can be converted to enable visualizer and full playback features.\n\n" +
                         "This will convert MP3, FLAC, and OGG files to WAV/AIFF format.\n" +
                         "Original files can be preserved based on your settings.\n\n" +
                         "Do you want to proceed?", convertibleFiles.size())
        );
        
        ButtonType proceedButton = new ButtonType("Proceed with Conversion");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        confirmAlert.getButtonTypes().setAll(proceedButton, cancelButton);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == proceedButton) {
                AudioConversionDialog dialog = new AudioConversionDialog(owner, conversionService, convertibleFiles);
                dialog.showAndWait();
                refreshLibraryAfterConversion();
            }
        });
    }
    
    /**
     * Check if auto-conversion is enabled and convert new files automatically.
     */
    public void checkAutoConversion(List<Song> newSongs) {
        if (!conversionService.getSettings().isAutoConvertOnImport()) {
            return; // Auto-conversion disabled
        }
        
        List<File> autoConvertFiles = newSongs.stream()
            .map(song -> new File(song.getFilePath()))
            .filter(File::exists)
            .filter(file -> {
                String ext = getFileExtension(file.getName());
                return conversionService.isConvertible(ext) && !conversionService.isJavaFXCompatible(ext);
            })
            .collect(Collectors.toList());
        
        if (!autoConvertFiles.isEmpty()) {
            // Perform auto-conversion in background
            conversionService.convertFiles(autoConvertFiles, new AudioConversionService.ConversionProgressCallback() {
                @Override
                public void onProgress(String fileName, int current, int total, double percentage) {
                    // Silent background conversion
                }
                
                @Override
                public void onComplete(List<File> convertedFiles, List<String> errors) {
                    if (!convertedFiles.isEmpty()) {
                        // Refresh library to pick up converted files
                        refreshLibraryAfterConversion();
                    }
                }
                
                @Override
                public void onError(String fileName, Exception error) {
                    // Log error but don't interrupt user experience
                    System.err.println("Auto-conversion failed for " + fileName + ": " + error.getMessage());
                }
            });
        }
    }
    
    /**
     * Get conversion statistics for the current library.
     */
    public ConversionStats getLibraryConversionStats() {
        List<Song> allSongs = musicLibraryManager.getAllSongs();
        
        int totalSongs = allSongs.size();
        int javaFXCompatible = 0;
        int convertible = 0;
        int unsupported = 0;
        
        for (Song song : allSongs) {
            String ext = getFileExtension(song.getFilePath());
            
            if (conversionService.isJavaFXCompatible(ext)) {
                javaFXCompatible++;
            } else if (conversionService.isConvertible(ext)) {
                convertible++;
            } else {
                unsupported++;
            }
        }
        
        return new ConversionStats(totalSongs, javaFXCompatible, convertible, unsupported);
    }
    
    /**
     * Check if a song would benefit from conversion.
     */
    public boolean wouldBenefitFromConversion(Song song) {
        if (song == null || song.getFilePath() == null) {
            return false;
        }
        
        String ext = getFileExtension(song.getFilePath());
        return conversionService.isConvertible(ext) && !conversionService.isJavaFXCompatible(ext);
    }
    
    /**
     * Get the conversion service for direct access to settings.
     */
    public AudioConversionService getConversionService() {
        return conversionService;
    }
    
    /**
     * Cleanup resources when shutting down.
     */
    public void shutdown() {
        conversionService.shutdown();
    }
    
    // Helper methods
    
    private void refreshLibraryAfterConversion() {
        // Trigger a library refresh to pick up converted files
        File currentMusicFolder = musicLibraryManager.getCurrentMusicFolder();
        if (currentMusicFolder != null) {
            // Rescan the library to include converted files
            musicLibraryManager.scanMusicFolder(currentMusicFolder, false); // Don't clear existing
        }
    }
    
    private void showInfo(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String getFileExtension(String filePath) {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Statistics about conversion status of the music library.
     */
    public static class ConversionStats {
        private final int totalSongs;
        private final int javaFXCompatible;
        private final int convertible;
        private final int unsupported;
        
        public ConversionStats(int totalSongs, int javaFXCompatible, int convertible, int unsupported) {
            this.totalSongs = totalSongs;
            this.javaFXCompatible = javaFXCompatible;
            this.convertible = convertible;
            this.unsupported = unsupported;
        }
        
        public int getTotalSongs() { return totalSongs; }
        public int getJavaFXCompatible() { return javaFXCompatible; }
        public int getConvertible() { return convertible; }
        public int getUnsupported() { return unsupported; }
        
        public double getJavaFXCompatiblePercentage() {
            return totalSongs > 0 ? (double) javaFXCompatible / totalSongs * 100 : 0;
        }
        
        public double getConvertiblePercentage() {
            return totalSongs > 0 ? (double) convertible / totalSongs * 100 : 0;
        }
        
        public boolean hasConvertibleFiles() {
            return convertible > 0;
        }
        
        @Override
        public String toString() {
            return String.format("Total: %d, JavaFX Compatible: %d (%.1f%%), Convertible: %d (%.1f%%), Unsupported: %d",
                totalSongs, javaFXCompatible, getJavaFXCompatiblePercentage(), 
                convertible, getConvertiblePercentage(), unsupported);
        }
    }
}
