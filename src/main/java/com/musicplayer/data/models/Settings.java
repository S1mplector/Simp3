package com.musicplayer.data.models;

import java.time.LocalDateTime;

/**
 * Application settings model.
 */
public class Settings {
    
    // Visual settings
    private boolean visualizerEnabled = true;
    private VisualizerColorMode visualizerColorMode = VisualizerColorMode.GRADIENT_CYCLING;
    private String visualizerSolidColor = "#32CD32"; // Default lime green
    private VisualizerDisplayMode visualizerDisplayMode = VisualizerDisplayMode.SPECTRUM_BARS;
    
    // Mini player settings
    private double miniPlayerX = -1; // -1 means use default position
    private double miniPlayerY = -1; // -1 means use default position

    // Whether mini player window should stay always on top
    private boolean miniPlayerPinned = true;
    
    // Update settings
    private boolean autoCheckForUpdates = true;
    private int updateCheckIntervalHours = 24;
    private LocalDateTime lastUpdateCheck;
    private String skippedUpdateVersion;
    private boolean downloadUpdatesInBackground = true;
    private boolean showPreReleaseVersions = false;
    
    // Distribution type preferences
    private DistributionType preferredDistributionType = DistributionType.UNKNOWN;
    private boolean rememberDistributionChoice = false;
    
    /**
     * Enum for visualizer color modes.
     */
    public enum VisualizerDisplayMode {
        SPECTRUM_BARS("Spectrum Bars"),
        WAVEFORM("Waveform"),
        COMBINED("Combined");
        private final String displayName;
        VisualizerDisplayMode(String displayName){this.displayName = displayName;}
        public String getDisplayName(){return displayName;}
    }

    public enum VisualizerColorMode {
        GRADIENT_CYCLING("Gradient Cycling"),
        SOLID_COLOR("Solid Color");
        
        private final String displayName;
        
        VisualizerColorMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Getters and setters
    public boolean isVisualizerEnabled() {
        return visualizerEnabled;
    }
    
    public void setVisualizerEnabled(boolean visualizerEnabled) {
        this.visualizerEnabled = visualizerEnabled;
    }
    
    public VisualizerColorMode getVisualizerColorMode() {
        return visualizerColorMode;
    }
    
    public void setVisualizerColorMode(VisualizerColorMode visualizerColorMode) {
        this.visualizerColorMode = visualizerColorMode;
    }
    
    public VisualizerDisplayMode getVisualizerDisplayMode(){return visualizerDisplayMode;}
    public void setVisualizerDisplayMode(VisualizerDisplayMode mode){this.visualizerDisplayMode = mode;}

    public String getVisualizerSolidColor() {
        return visualizerSolidColor;
    }
    
    public void setVisualizerSolidColor(String visualizerSolidColor) {
        this.visualizerSolidColor = visualizerSolidColor;
    }
    
    public double getMiniPlayerX() {
        return miniPlayerX;
    }
    
    public void setMiniPlayerX(double miniPlayerX) {
        this.miniPlayerX = miniPlayerX;
    }
    
    public double getMiniPlayerY() {
        return miniPlayerY;
    }
    
    public void setMiniPlayerY(double miniPlayerY) {
        this.miniPlayerY = miniPlayerY;
    }

    public boolean isMiniPlayerPinned() {
        return miniPlayerPinned;
    }

    public void setMiniPlayerPinned(boolean miniPlayerPinned) {
        this.miniPlayerPinned = miniPlayerPinned;
    }
    
    // Update settings getters and setters
    public boolean isAutoCheckForUpdates() {
        return autoCheckForUpdates;
    }
    
    public void setAutoCheckForUpdates(boolean autoCheckForUpdates) {
        this.autoCheckForUpdates = autoCheckForUpdates;
    }
    
    public int getUpdateCheckIntervalHours() {
        return updateCheckIntervalHours;
    }
    
    public void setUpdateCheckIntervalHours(int updateCheckIntervalHours) {
        this.updateCheckIntervalHours = updateCheckIntervalHours;
    }
    
    public LocalDateTime getLastUpdateCheck() {
        return lastUpdateCheck;
    }
    
    public void setLastUpdateCheck(LocalDateTime lastUpdateCheck) {
        this.lastUpdateCheck = lastUpdateCheck;
    }
    
    public String getSkippedUpdateVersion() {
        return skippedUpdateVersion;
    }
    
    public void setSkippedUpdateVersion(String skippedUpdateVersion) {
        this.skippedUpdateVersion = skippedUpdateVersion;
    }
    
    public boolean isDownloadUpdatesInBackground() {
        return downloadUpdatesInBackground;
    }
    
    public void setDownloadUpdatesInBackground(boolean downloadUpdatesInBackground) {
        this.downloadUpdatesInBackground = downloadUpdatesInBackground;
    }
    
    public boolean isShowPreReleaseVersions() {
        return showPreReleaseVersions;
    }
    
    public void setShowPreReleaseVersions(boolean showPreReleaseVersions) {
        this.showPreReleaseVersions = showPreReleaseVersions;
    }
    
    // Distribution type preference getters and setters
    public DistributionType getPreferredDistributionType() {
        return preferredDistributionType;
    }
    
    public void setPreferredDistributionType(DistributionType preferredDistributionType) {
        this.preferredDistributionType = preferredDistributionType;
    }
    
    public boolean isRememberDistributionChoice() {
        return rememberDistributionChoice;
    }
    
    public void setRememberDistributionChoice(boolean rememberDistributionChoice) {
        this.rememberDistributionChoice = rememberDistributionChoice;
    }
}