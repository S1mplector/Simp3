package com.musicplayer.ui.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Enhanced audio spectrum visualizer with gradient bars, peak caps, and glow effects.
 */
public class AudioVisualizer extends Canvas {

    private final int numBands;
    // Smoothed magnitudes displayed
    private final float[] displayMagnitudes;
    // Peak values for each band
    private final float[] peakValues;
    // Peak fall velocities
    private final float[] peakVelocities;
    
    private static final double SMOOTHING_FACTOR = 0.7; // Reduced for more responsive movement
    private static final double PEAK_FALL_SPEED = 0.15; // Speed at which peaks fall
    private static final double PEAK_HANG_TIME = 0.92; // How long peaks hang before falling
    
    // Animation timer for smooth peak falling
    private AnimationTimer peakAnimator;
    private long lastUpdate = 0;
    
    // Color scheme with hue animation
    private double currentHue = 120; // Start with green (120 degrees)
    private static final double HUE_SHIFT_SPEED = 10; // Degrees per second
    private final Color peakColor = Color.rgb(255, 255, 255, 0.9); // White peak caps

    public AudioVisualizer(int numBands) {
        this.numBands = numBands;
        this.displayMagnitudes = new float[numBands];
        this.peakValues = new float[numBands];
        this.peakVelocities = new float[numBands];

        // Initialize peaks
        for (int i = 0; i < numBands; i++) {
            peakValues[i] = -60.0f;
            peakVelocities[i] = 0.0f;
        }

        // Redraw when resized
        widthProperty().addListener((obs, o, n) -> draw());
        heightProperty().addListener((obs, o, n) -> draw());
        
        // Start peak animation
        startPeakAnimation();
    }

    /**
     * Update the visualizer with the latest magnitude data.
     * This should be called from the JavaFX Application Thread.
     */
    public void update(float[] newMagnitudes) {
        if (newMagnitudes == null) {
            return;
        }
        int len = Math.min(newMagnitudes.length, numBands);
        for (int i = 0; i < len; i++) {
            float newVal = newMagnitudes[i];
            // Exponential smoothing
            displayMagnitudes[i] = (float) (SMOOTHING_FACTOR * displayMagnitudes[i] + 
                                           (1 - SMOOTHING_FACTOR) * newVal);
            
            // Update peaks
            if (displayMagnitudes[i] > peakValues[i]) {
                peakValues[i] = displayMagnitudes[i];
                peakVelocities[i] = 0.0f; // Reset velocity when peak is updated
            }
        }
        draw();
    }

    private void startPeakAnimation() {
        peakAnimator = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                // Calculate delta time in seconds
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                // Update hue for color animation
                currentHue += HUE_SHIFT_SPEED * deltaTime;
                if (currentHue >= 360) {
                    currentHue -= 360;
                }
                
                // Update peak positions
                boolean needsRedraw = false;
                for (int i = 0; i < numBands; i++) {
                    if (peakValues[i] > displayMagnitudes[i]) {
                        // Apply gravity to velocity
                        peakVelocities[i] += PEAK_FALL_SPEED;
                        
                        // Apply hang time (slow initial fall)
                        float effectiveVelocity = peakVelocities[i] * (1.0f - (float)PEAK_HANG_TIME);
                        
                        // Update peak position
                        peakValues[i] -= effectiveVelocity;
                        
                        // Don't let peak go below current bar
                        if (peakValues[i] < displayMagnitudes[i]) {
                            peakValues[i] = displayMagnitudes[i];
                            peakVelocities[i] = 0.0f;
                        }
                        needsRedraw = true;
                    }
                }
                
                if (needsRedraw) {
                    draw();
                }
            }
        };
        peakAnimator.start();
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double bandWidth = width / numBands;
        double barWidth = bandWidth * 0.8; // 80% width for bars, 20% for spacing
        double spacing = bandWidth * 0.2;

        // Create base color from current hue
        Color baseColor = Color.hsb(currentHue, 0.8, 1.0); // High saturation and brightness

        // Apply glow effect to the entire canvas
        DropShadow glow = new DropShadow();
        glow.setColor(baseColor);
        glow.setRadius(10);
        glow.setSpread(0.2);
        gc.setEffect(glow);

        for (int i = 0; i < numBands; i++) {
            float magnitude = displayMagnitudes[i];
            float peak = peakValues[i];
            
            // Normalize values (assuming dB range of -60 to 0)
            double normalizedMag = (60 + magnitude) / 60.0;
            double normalizedPeak = (60 + peak) / 60.0;
            normalizedMag = Math.max(0, Math.min(1, normalizedMag));
            normalizedPeak = Math.max(0, Math.min(1, normalizedPeak));

            double barHeight = normalizedMag * height * 0.9; // Leave 10% margin at top
            double peakY = height - (normalizedPeak * height * 0.9);
            double x = i * bandWidth + spacing / 2;
            double y = height - barHeight;

            // Create gradient for bars
            LinearGradient gradient = new LinearGradient(
                0, y, 0, height,
                false, null,
                new Stop(0, baseColor.brighter()),
                new Stop(0.5, baseColor),
                new Stop(1, baseColor.darker().darker())
            );
            
            // Draw main bar with gradient
            gc.setFill(gradient);
            gc.fillRect(x, y, barWidth, barHeight);
            
            // Draw peak cap
            if (normalizedPeak > normalizedMag && peakY < height - 2) {
                gc.setFill(peakColor);
                gc.fillRect(x, peakY, barWidth, 3); // 3 pixel tall peak cap
            }
        }
        
        // Remove effect after drawing
        gc.setEffect(null);
    }
    
    /**
     * Clean up resources when the visualizer is no longer needed.
     */
    public void dispose() {
        if (peakAnimator != null) {
            peakAnimator.stop();
        }
    }
} 