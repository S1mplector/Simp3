package com.musicplayer.ui.controllers;

import com.musicplayer.data.models.Settings;
import com.musicplayer.services.SettingsService;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controller for the settings dialog.
 */
public class SettingsController {
    
    @FXML private CheckBox visualizerEnabledCheckBox;
    @FXML private VBox colorModeSection;
    @FXML private RadioButton gradientCyclingRadio;
    @FXML private RadioButton solidColorRadio;
    @FXML private ToggleGroup colorModeGroup;
    @FXML private HBox colorPickerSection;
    @FXML private ColorPicker solidColorPicker;
    
    private SettingsService settingsService;
    private Settings settings;
    
    /**
     * Set the settings service and load current settings.
     * @param settingsService The settings service to use
     */
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
        this.settings = settingsService.getSettings();
        loadSettings();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Enable/disable color options based on visualizer state
        visualizerEnabledCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            colorModeSection.setDisable(!newVal);
            colorPickerSection.setDisable(!newVal || !solidColorRadio.isSelected());
        });
        
        // Show/hide color picker based on color mode
        colorModeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            colorPickerSection.setDisable(newVal != solidColorRadio || !visualizerEnabledCheckBox.isSelected());
        });
    }
    
    /**
     * Load current settings into the UI.
     */
    private void loadSettings() {
        // Set visualizer enabled state
        visualizerEnabledCheckBox.setSelected(settings.isVisualizerEnabled());
        
        // Set color mode
        if (settings.getVisualizerColorMode() == Settings.VisualizerColorMode.GRADIENT_CYCLING) {
            gradientCyclingRadio.setSelected(true);
        } else {
            solidColorRadio.setSelected(true);
        }
        
        // Set solid color
        try {
            solidColorPicker.setValue(Color.web(settings.getVisualizerSolidColor()));
        } catch (Exception e) {
            // Default to green if color parsing fails
            solidColorPicker.setValue(Color.LIMEGREEN);
        }
        
        // Update enabled states
        colorModeSection.setDisable(!settings.isVisualizerEnabled());
        colorPickerSection.setDisable(!settings.isVisualizerEnabled() || 
                                     settings.getVisualizerColorMode() != Settings.VisualizerColorMode.SOLID_COLOR);
    }
    
    /**
     * Save the current settings when OK is clicked.
     * This method is called before the dialog is closed.
     */
    public void saveSettings() {
        // Update settings from UI
        settings.setVisualizerEnabled(visualizerEnabledCheckBox.isSelected());
        
        if (gradientCyclingRadio.isSelected()) {
            settings.setVisualizerColorMode(Settings.VisualizerColorMode.GRADIENT_CYCLING);
        } else {
            settings.setVisualizerColorMode(Settings.VisualizerColorMode.SOLID_COLOR);
        }
        
        // Convert Color to hex string
        Color color = solidColorPicker.getValue();
        String hexColor = String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
        settings.setVisualizerSolidColor(hexColor);
        
        // Save to file
        settingsService.saveSettings();
    }
} 