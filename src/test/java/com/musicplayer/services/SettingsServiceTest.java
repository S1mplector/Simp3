package com.musicplayer.services;

import com.musicplayer.data.models.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsServiceTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setDataDir() {
        // Point SettingsService to an isolated data directory inside the temp dir
        System.setProperty("simp3.data.dir", tempDir.resolve("data").toString());
    }

    @Test
    void loads_defaults_and_persists_on_first_run() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Path settingsFile = dataDir.resolve("settings.json");
        assertFalse(Files.exists(settingsFile));

        SettingsService svc = new SettingsService();
        // defaults per model
        Settings s = svc.getSettings();
        assertTrue(s.isVisualizerEnabled());
        assertEquals(Settings.VisualizerColorMode.GRADIENT_CYCLING, s.getVisualizerColorMode());

        // constructor should have saved defaults
        assertTrue(Files.exists(settingsFile), "settings.json should be created on first load");

        // Modify and persist
        svc.setVisualizerEnabled(false);
        svc.setVisualizerColorMode(Settings.VisualizerColorMode.SOLID_COLOR);
        svc.setVisualizerSolidColor("#FF0000");
        assertTrue(Files.size(settingsFile) > 0);

        // New instance should load what was saved
        SettingsService svc2 = new SettingsService();
        Settings loaded = svc2.getSettings();
        assertFalse(loaded.isVisualizerEnabled());
        assertEquals(Settings.VisualizerColorMode.SOLID_COLOR, loaded.getVisualizerColorMode());
        assertEquals("#FF0000", loaded.getVisualizerSolidColor());
    }

    @Test
    void mini_player_position_and_pin_are_persisted() {
        SettingsService svc = new SettingsService();
        svc.setMiniPlayerPosition(123.45, 67.89);
        svc.setMiniPlayerPinned(false);

        SettingsService reload = new SettingsService();
        assertEquals(123.45, reload.getMiniPlayerX());
        assertEquals(67.89, reload.getMiniPlayerY());
        assertFalse(reload.isMiniPlayerPinned());
    }
}
