package com.musicplayer.ui.controllers;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.stage.Stage;

import com.musicplayer.services.AudioPlayerService;
import com.musicplayer.services.SettingsService;
import com.musicplayer.ui.components.AudioVisualizer;
import com.musicplayer.ui.windows.MiniPlayerWindow;

/**
 * Controller responsible for managing audio visualizer state and behavior.
 * Handles coordination between main window and mini player visualizers,
 * ensuring consistent behavior and preventing conflicts.
 */
public class VisualizerController {
    
    private final AudioPlayerService audioPlayerService;
    private final SettingsService settingsService;
    
    // Main window visualizer components
    private AudioVisualizer mainVisualizer;
    private javafx.scene.media.AudioSpectrumListener mainSpectrumListener;
    private Stage mainStage;
    
    // Mini player reference
    private MiniPlayerWindow miniPlayerWindow;
    
    // State tracking
    private boolean isMainVisualizerActive = false;
    
    public VisualizerController(AudioPlayerService audioPlayerService, SettingsService settingsService) {
        this.audioPlayerService = audioPlayerService;
        this.settingsService = settingsService;
    }
    
    /**
     * Initialize the main window visualizer.
     * @param stage The main application stage
     */
    public void initializeMainVisualizer(Stage stage) {
        this.mainStage = stage;
        
        // Create main visualizer
        mainVisualizer = new AudioVisualizer(64);
        mainVisualizer.setMouseTransparent(true);
        mainVisualizer.setVisible(false);
        
        // Create spectrum listener for main visualizer
        mainSpectrumListener = (timestamp, duration, magnitudes, phases) -> {
            if (mainVisualizer != null) {
                mainVisualizer.update(magnitudes);
            }
        };
        
        // Set up listeners for playback state changes
        setupPlaybackStateListeners();
        
        // Set up window state monitoring
        setupWindowStateMonitoring();
        
        // Initial state update
        updateMainVisualizerState();
    }
    
    /**
     * Set the mini player window reference for coordination.
     * @param miniPlayerWindow The mini player window instance
     */
    public void setMiniPlayerWindow(MiniPlayerWindow miniPlayerWindow) {
        this.miniPlayerWindow = miniPlayerWindow;
    }
    
    /**
     * Update the main visualizer state based on current conditions.
     */
    public void updateMainVisualizerState() {
        if (mainVisualizer == null) return;
        
        boolean visualizerEnabled = settingsService != null && settingsService.getSettings() != null && 
                                  settingsService.getSettings().isVisualizerEnabled();
        boolean isPlaying = audioPlayerService != null && audioPlayerService.isPlaying();
        boolean isWindowFocused = isMainWindowFocused();
        boolean miniPlayerActive = miniPlayerWindow != null && miniPlayerWindow.isShowing();
        
        if (isPlaying && isWindowFocused && visualizerEnabled && !miniPlayerActive) {
            // Show main visualizer
            activateMainVisualizer();
        } else {
            // Hide main visualizer
            deactivateMainVisualizer(miniPlayerActive);
        }
        
        // Update layout
        updateMainVisualizerLayout();
    }
    
    /**
     * Activate the main window visualizer.
     */
    private void activateMainVisualizer() {
        if (mainVisualizer == null) return;
        
        mainVisualizer.setVisible(true);
        isMainVisualizerActive = true;
        
        // Connect spectrum listener
        if (mainSpectrumListener != null && audioPlayerService != null) {
            audioPlayerService.setAudioSpectrumListener(mainSpectrumListener);
        }
        
        // Resume animation if paused
        if (mainVisualizer.isPaused()) {
            mainVisualizer.resume();
        }
        
        System.out.println("Main visualizer activated");
    }
    
    /**
     * Deactivate the main window visualizer.
     * @param miniPlayerActive Whether the mini player is currently active
     */
    private void deactivateMainVisualizer(boolean miniPlayerActive) {
        if (mainVisualizer == null) return;
        
        mainVisualizer.setVisible(false);
        isMainVisualizerActive = false;
        
        // Only disconnect spectrum listener if mini player is not active
        if (audioPlayerService != null && !miniPlayerActive) {
            boolean isPlaying = audioPlayerService.isPlaying();
            boolean visualizerEnabled = settingsService != null && settingsService.getSettings() != null && 
                                      settingsService.getSettings().isVisualizerEnabled();
            
            // Clear spectrum listener only if we're not playing or visualizer is disabled
            if (!isPlaying || !visualizerEnabled) {
                audioPlayerService.setAudioSpectrumListener(null);
            }
        }
        
        // Pause animation
        if (!mainVisualizer.isPaused()) {
            mainVisualizer.pause();
        }
        
        System.out.println("Main visualizer deactivated (mini player active: " + miniPlayerActive + ")");
    }
    
    /**
     * Check if the main window is currently focused (not minimized).
     */
    private boolean isMainWindowFocused() {
        if (mainStage != null) {
            return !mainStage.isIconified();
        }
        return false;
    }
    
    /**
     * Set up listeners for playback state changes.
     */
    private void setupPlaybackStateListeners() {
        if (audioPlayerService != null) {
            audioPlayerService.playingProperty().addListener((obs, wasPlaying, isPlaying) -> {
                updateMainVisualizerState();
            });
        }
    }
    
    /**
     * Set up window state monitoring for the main stage.
     */
    private void setupWindowStateMonitoring() {
        if (mainStage != null) {
            mainStage.iconifiedProperty().addListener((obs, wasMinimized, isMinimized) -> {
                if (isMinimized) {
                    // Window minimized - deactivate main visualizer
                    if (mainVisualizer != null) {
                        mainVisualizer.pause();
                    }
                    System.out.println("Window minimized - main visualizer paused");
                } else {
                    // Window restored - update visualizer state
                    Platform.runLater(() -> {
                        // Always reattach spectrum listener when returning to main window
                        if (audioPlayerService != null && mainSpectrumListener != null) {
                            audioPlayerService.setAudioSpectrumListener(mainSpectrumListener);
                        }
                        
                        // Update visualizer state
                        updateMainVisualizerState();
                    });
                }
            });
        }
    }
    
    /**
     * Update the main visualizer layout in the UI.
     */
    private void updateMainVisualizerLayout() {
        if (mainVisualizer == null || mainStage == null) return;
        
        Platform.runLater(() -> {
            try {
                BorderPane root = (BorderPane) mainStage.getScene().getRoot();
                if (root == null) return;
                
                Node bottomNode = root.getBottom();
                if (bottomNode == null) return;
                
                // Check if visualizer is already in a stack pane
                if (bottomNode instanceof StackPane) {
                    StackPane existingStack = (StackPane) bottomNode;
                    if (!existingStack.getChildren().contains(mainVisualizer)) {
                        existingStack.getChildren().add(0, mainVisualizer); // Add at bottom
                        mainVisualizer.widthProperty().bind(existingStack.widthProperty());
                        mainVisualizer.heightProperty().bind(existingStack.heightProperty());
                    }
                } else {
                    // Create new stack pane with visualizer
                    root.setBottom(null);
                    StackPane stack = new StackPane();
                    stack.getChildren().addAll(mainVisualizer, bottomNode);
                    
                    // Bind visualizer size to stack pane
                    mainVisualizer.widthProperty().bind(stack.widthProperty());
                    mainVisualizer.heightProperty().bind(stack.heightProperty());
                    
                    root.setBottom(stack);
                }
            } catch (Exception e) {
                System.err.println("Error updating visualizer layout: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle mini player show event.
     */
    public void onMiniPlayerShow() {
        updateMainVisualizerState();
    }
    
    /**
     * Handle mini player hide event.
     */
    public void onMiniPlayerHide() {
        updateMainVisualizerState();
    }
    
    /**
     * Apply visualizer settings changes.
     */
    public void applyVisualizerSettings() {
        updateMainVisualizerState();
    }
    
    /**
     * Check if the main visualizer is currently active.
     */
    public boolean isMainVisualizerActive() {
        return isMainVisualizerActive;
    }
    
    /**
     * Get the main visualizer instance.
     */
    public AudioVisualizer getMainVisualizer() {
        return mainVisualizer;
    }
    
    /**
     * Apply the current visualizer settings
     */
    public void applySettings() {
        if (mainVisualizer != null && settingsService != null) {
            var settings = settingsService.getSettings();
            
            // Apply enabled state
            mainVisualizer.setEnabled(settings.isVisualizerEnabled());
            
            // Apply color mode
            boolean gradientCycling = settings.getVisualizerColorMode() == 
                com.musicplayer.data.models.Settings.VisualizerColorMode.GRADIENT_CYCLING;
            mainVisualizer.setGradientCyclingEnabled(gradientCycling);
            
            // Apply solid color
            try {
                mainVisualizer.setSolidColor(javafx.scene.paint.Color.web(settings.getVisualizerSolidColor()));
            } catch (Exception e) {
                // Default to green if color parsing fails
                mainVisualizer.setSolidColor(javafx.scene.paint.Color.LIMEGREEN);
            }
            
            // Apply visualization display mode
            switch (settings.getVisualizerDisplayMode()) {
                case WAVEFORM -> mainVisualizer.setVisualizationType(com.musicplayer.ui.components.AudioVisualizer.VisualizationType.WAVEFORM);
                case COMBINED -> mainVisualizer.setVisualizationType(com.musicplayer.ui.components.AudioVisualizer.VisualizationType.COMBINED);
                default -> mainVisualizer.setVisualizationType(com.musicplayer.ui.components.AudioVisualizer.VisualizationType.SPECTRUM_BARS);
            }
            
            // Update state based on current conditions
            updateMainVisualizerState();
        }
    }
    
    /**
     * Set up window state monitoring to pause/resume visualizer when minimized.
     * @param stage The primary stage of the application
     */
    public void setupWindowStateMonitoring(javafx.stage.Stage stage) {
        if (stage != null) {
            stage.iconifiedProperty().addListener((obs, wasMinimized, isMinimized) -> {
                if (isMinimized) {
                    // Window is minimized - pause main visualizer
                    if (mainVisualizer != null) {
                        mainVisualizer.pause();
                    }
                    System.out.println("Window minimized - main visualizer paused");
                } else {
                    // Window is restored - update visualizer state
                    javafx.application.Platform.runLater(() -> {
                        updateMainVisualizerState();
                        System.out.println("Window restored - visualizer state updated");
                    });
                }
            });
        }
    }
    
    /**
     * Cleanup resources when shutting down
     */
    public void cleanup() {
        if (mainVisualizer != null) {
            mainVisualizer.dispose();
        }
        if (audioPlayerService != null) {
            audioPlayerService.setAudioSpectrumListener(null);
        }
    }
}
