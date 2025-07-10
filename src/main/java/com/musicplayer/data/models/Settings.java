package com.musicplayer.data.models;

/**
 * Application settings model.
 */
public class Settings {
    
    // Visual settings
    private boolean visualizerEnabled = true;
    private VisualizerColorMode visualizerColorMode = VisualizerColorMode.GRADIENT_CYCLING;
    private String visualizerSolidColor = "#32CD32"; // Default lime green
    
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
} 