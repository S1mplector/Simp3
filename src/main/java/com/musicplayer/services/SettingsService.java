package com.musicplayer.services;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.musicplayer.data.models.Settings;

/**
 * Service for managing application settings.
 */
public class SettingsService {
    
    private static final String SETTINGS_FILE = "data/settings.json";
    private final ObjectMapper objectMapper;
    private Settings settings;
    
    public SettingsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadSettings();
    }
    
    /**
     * Load settings from file or create default settings.
     */
    private void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);
        
        if (settingsFile.exists()) {
            try {
                settings = objectMapper.readValue(settingsFile, Settings.class);
                System.out.println("Loaded settings from file");
            } catch (IOException e) {
                System.err.println("Failed to load settings: " + e.getMessage());
                settings = new Settings(); // Use defaults
            }
        } else {
            settings = new Settings(); // Use defaults
            saveSettings(); // Save defaults to file
        }
    }
    
    /**
     * Save current settings to file.
     */
    public void saveSettings() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        try {
            objectMapper.writeValue(new File(SETTINGS_FILE), settings);
            System.out.println("Settings saved to file");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    /**
     * Get the current settings.
     * @return Current settings
     */
    public Settings getSettings() {
        return settings;
    }
    
    /**
     * Update visualizer enabled state.
     * @param enabled Whether visualizer should be enabled
     */
    public void setVisualizerEnabled(boolean enabled) {
        settings.setVisualizerEnabled(enabled);
        saveSettings();
    }
    
    /**
     * Update visualizer color mode.
     * @param colorMode The color mode to use
     */
    public void setVisualizerColorMode(Settings.VisualizerColorMode colorMode) {
        settings.setVisualizerColorMode(colorMode);
        saveSettings();
    }
    
    /**
     * Update visualizer solid color.
     * @param color The color in hex format (e.g., "#FF0000")
     */
    public void setVisualizerSolidColor(String color) {
        settings.setVisualizerSolidColor(color);
        saveSettings();
    }
    
    /**
     * Update mini player position.
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public void setMiniPlayerPosition(double x, double y) {
        settings.setMiniPlayerX(x);
        settings.setMiniPlayerY(y);
        saveSettings();
    }
    
    /**
     * Get mini player X position.
     * @return X coordinate or -1 if not set
     */
    public double getMiniPlayerX() {
        return settings.getMiniPlayerX();
    }
    
    /**
     * Get mini player Y position.
     * @return Y coordinate or -1 if not set
     */
    public double getMiniPlayerY() {
        return settings.getMiniPlayerY();
    }
    
    /**
     * Update mini player pinned state.
     * @param pinned Whether the mini player should be always on top
     */
    public void setMiniPlayerPinned(boolean pinned) {
        settings.setMiniPlayerPinned(pinned);
        saveSettings();
    }
    
    /**
     * Get mini player pinned state.
     * @return true if mini player is pinned (always on top)
     */
    public boolean isMiniPlayerPinned() {
        return settings.isMiniPlayerPinned();
    }
}