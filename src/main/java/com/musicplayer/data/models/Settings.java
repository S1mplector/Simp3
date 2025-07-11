package com.musicplayer.data.models;

/**
 * Application settings model.
 */
public class Settings {
    
    // Visual settings
    private boolean visualizerEnabled = true;
    private VisualizerColorMode visualizerColorMode = VisualizerColorMode.GRADIENT_CYCLING;
    private String visualizerSolidColor = "#32CD32"; // Default lime green
    
    // Mini player settings
    private double miniPlayerX = -1; // -1 means use default position
    private double miniPlayerY = -1; // -1 means use default position

    // Whether mini player window should stay always on top
    private boolean miniPlayerPinned = true;
    
    /**
     * Enum for visualizer color modes.
     */
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
}