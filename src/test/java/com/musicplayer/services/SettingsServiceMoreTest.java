package com.musicplayer.services;

import com.musicplayer.data.models.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsServiceMoreTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setDataDir() {
        System.setProperty("simp3.data.dir", tempDir.resolve("data").toString());
    }

    @Test
    void theme_and_update_flags_persist() {
        SettingsService svc = new SettingsService();
        svc.getSettings().setTheme(Settings.Theme.DARK);
        svc.getSettings().setAutoCheckForUpdates(false);
        svc.getSettings().setDownloadUpdatesInBackground(false);
        svc.getSettings().setShowPreReleaseVersions(true);
        svc.getSettings().setUpdateCheckIntervalHours(12);
        svc.saveSettings();

        SettingsService reload = new SettingsService();
        Settings s = reload.getSettings();
        assertEquals(Settings.Theme.DARK, s.getTheme());
        assertFalse(s.isAutoCheckForUpdates());
        assertFalse(s.isDownloadUpdatesInBackground());
        assertTrue(s.isShowPreReleaseVersions());
        assertEquals(12, s.getUpdateCheckIntervalHours());
    }

    @Test
    void loads_defaults_if_settings_file_is_corrupt() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Path settingsFile = dataDir.resolve("settings.json");
        Files.writeString(settingsFile, "{ this is not valid json }");

        SettingsService svc = new SettingsService();
        Settings s = svc.getSettings();

        // Defaults from model
        assertTrue(s.isVisualizerEnabled());
        assertEquals(Settings.VisualizerColorMode.GRADIENT_CYCLING, s.getVisualizerColorMode());
        assertEquals(Settings.Theme.LIGHT, s.getTheme());
    }
}
