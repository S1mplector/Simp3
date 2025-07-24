package com.musicplayer.ui.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Spectrum bar visualizer that displays audio frequency data as animated bars.
 */
public class SpectrumVisualizer extends BaseVisualizer {
    
    // Peak values for each band
    private final float[] peakValues;
    // Peak fall velocities
    private final float[] peakVelocities;
    
    private static final double PEAK_FALL_SPEED = 0.15; // Speed at which peaks fall
    private static final double PEAK_HANG_TIME = 0.92; // How long peaks hang before falling
    
    private final Color peakColor = Color.rgb(255, 255, 255, 0.9); // White peak caps

    public SpectrumVisualizer(int numBands) {
        super(numBands);
        this.peakValues = new float[numBands];
        this.peakVelocities = new float[numBands];
        
        // Initialize peaks
        for (int i = 0; i < numBands; i++) {
            peakValues[i] = -60.0f;
            peakVelocities[i] = 0.0f;
        }
    }

    @Override
    protected void updateSpecificData(float[] magnitudes) {
        // Update peaks
        int len = Math.min(magnitudes.length, numBands);
        for (int i = 0; i < len; i++) {
            if (displayMagnitudes[i] > peakValues[i]) {
                peakValues[i] = displayMagnitudes[i];
                peakVelocities[i] = 0.0f; // Reset velocity when peak is updated
            }
        }
    }

    @Override
    protected boolean performAnimationUpdate(double deltaTime) {
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
        return needsRedraw;
    }

    @Override
    protected void drawVisualization(GraphicsContext gc, double width, double height, Color baseColor) {
        double bandWidth = width / numBands;
        double barWidth = bandWidth * 0.8; // 80% width for bars, 20% for spacing
        double spacing = bandWidth * 0.2;

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

    @Override
    protected void clearSpecificData() {
        // Clear peak data
        for (int i = 0; i < numBands; i++) {
            peakValues[i] = -60.0f;
            peakVelocities[i] = 0.0f;
        }
    }
}
