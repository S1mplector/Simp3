package com.musicplayer.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Simple bar-based audio spectrum visualizer.
 */
public class AudioVisualizer extends Canvas {

    private final int numBands;
    // Smoothed magnitudes displayed
    private final float[] displayMagnitudes;
    private static final double SMOOTHING_FACTOR = 0.9; // 0 = no smoothing, 1 = infinite smoothing

    public AudioVisualizer(int numBands) {
        this.numBands = numBands;
        this.displayMagnitudes = new float[numBands];

        // Redraw when resized
        widthProperty().addListener((obs, o, n) -> draw());
        heightProperty().addListener((obs, o, n) -> draw());
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
            displayMagnitudes[i] = (float) (SMOOTHING_FACTOR * displayMagnitudes[i] + (1 - SMOOTHING_FACTOR) * newVal);
        }
        draw();
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

        for (int i = 0; i < numBands; i++) {
            float magnitude = displayMagnitudes[i];
            double normalized = (60 + magnitude) / 60.0;
            normalized = Math.max(0, Math.min(1, normalized));

            double barHeight = normalized * height;
            double x = i * bandWidth;
            double y = height - barHeight;

            gc.setFill(Color.rgb(144, 238, 144, 0.6)); // light green with transparency
            gc.fillRect(x, y, bandWidth - 2, barHeight);
        }
    }
} 