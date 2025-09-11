package com.musicplayer.data.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DistributionTypeTest {

    @Test
    void detects_installer_from_setup_filename() {
        // Given a typical installer filename
        String filename = "simp3-1.2.3-setup.exe";

        // When detecting distribution type
        DistributionType type = DistributionType.fromFilename(filename);

        // Then it should be recognized as INSTALLER
        assertEquals(DistributionType.INSTALLER, type);
    }
}
