package com.musicplayer.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicplayer.config.UpdateConfig;
import com.musicplayer.data.models.Settings;
import com.musicplayer.data.models.UpdateInfo;
import com.musicplayer.utils.VersionComparator;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Service for checking and applying application updates.
 */
public class UpdateService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);
    
    // GitHub API configuration
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_RELEASES_URL = GITHUB_API_BASE + "/repos/" +
        UpdateConfig.GITHUB_OWNER + "/" + UpdateConfig.GITHUB_REPO + "/releases";
    
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    
    // Properties for download progress
    private final DoubleProperty downloadProgress = new SimpleDoubleProperty(0.0);
    private final StringProperty downloadStatus = new SimpleStringProperty("");
    
    private String currentVersion;
    
    public UpdateService(SettingsService settingsService) {
        this.settingsService = settingsService;
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("UpdateChecker");
            return t;
        });
        
        loadCurrentVersion();
    }
    
    /**
     * Load current version from application properties.
     */
    private void loadCurrentVersion() {
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(is);
            currentVersion = props.getProperty("app.version", "1.0.0");
            logger.info("Current application version: {}", currentVersion);
        } catch (Exception e) {
            logger.error("Failed to load application version", e);
            currentVersion = "1.0.0";
        }
    }
    
    /**
     * Start automatic update checking based on settings.
     */
    public void startAutoUpdateCheck() {
        Settings settings = settingsService.getSettings();
        if (!settings.isAutoCheckForUpdates()) {
            logger.info("Auto-update checking is disabled in settings");
            return;
        }
        
        logger.info("Starting auto-update check service");
        logger.info("Current version: {}", currentVersion);
        logger.info("GitHub repository: {}/{}", UpdateConfig.GITHUB_OWNER, UpdateConfig.GITHUB_REPO);
        
        // Schedule periodic update checks
        int intervalHours = settings.getUpdateCheckIntervalHours();
        int initialDelay = UpdateConfig.STARTUP_CHECK_DELAY;
        
        if (initialDelay == 0) {
            logger.info("Checking for updates immediately on startup");
        } else {
            logger.info("First update check will run in {} seconds", initialDelay);
        }
        
        scheduler.scheduleWithFixedDelay(
            this::checkForUpdatesInBackground,
            initialDelay,
            intervalHours * 60 * 60, // Convert hours to seconds
            TimeUnit.SECONDS
        );
        
        logger.info("Auto-update checking scheduled every {} hours", intervalHours);
    }
    
    /**
     * Check for updates in the background.
     */
    private void checkForUpdatesInBackground() {
        logger.info("Starting background update check");
        checkForUpdates().thenAccept(updateInfo -> {
            if (updateInfo != null) {
                logger.info("Update available: version {} (current: {})",
                    updateInfo.getVersion(), currentVersion);
                logger.info("Download URL: {}", updateInfo.getDownloadUrl());
                logger.info("File size: {}", updateInfo.getFileSizeFormatted());
                logger.info("Mandatory: {}", updateInfo.isMandatory());
                
                Platform.runLater(() -> {
                    // Notify UI about available update
                    logger.info("Notifying UI about available update");
                    // The UI will handle showing the update dialog
                });
            } else {
                logger.info("No updates available - current version {} is up to date", currentVersion);
            }
        }).exceptionally(e -> {
            logger.error("Background update check failed: {}", e.getMessage(), e);
            return null;
        });
    }
    
    /**
     * Check for updates from GitHub releases.
     * 
     * @return CompletableFuture with UpdateInfo if update is available, null otherwise
     */
    public CompletableFuture<UpdateInfo> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Checking for updates...");
                Settings settings = settingsService.getSettings();
                settings.setLastUpdateCheck(LocalDateTime.now());
                settingsService.saveSettings();
                
                // Query GitHub API
                String apiUrl = settings.isShowPreReleaseVersions() ?
                    GITHUB_RELEASES_URL : GITHUB_RELEASES_URL + "/latest";
                logger.info("Querying GitHub API: {}", apiUrl);
                
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "SiMP3-UpdateChecker");
                conn.setConnectTimeout(UpdateConfig.CONNECTION_TIMEOUT);
                conn.setReadTimeout(UpdateConfig.READ_TIMEOUT);
                
                logger.debug("Sending request to GitHub API...");
                int responseCode = conn.getResponseCode();
                logger.info("GitHub API response code: {}", responseCode);
                
                if (responseCode != 200) {
                    logger.warn("GitHub API returned non-200 status: {}", responseCode);
                    return null;
                }
                
                // Parse response
                JsonNode response = objectMapper.readTree(conn.getInputStream());
                UpdateInfo latestUpdate = null;
                
                if (response.isArray()) {
                    // Multiple releases (including pre-releases)
                    for (JsonNode release : response) {
                        UpdateInfo info = parseReleaseInfo(release);
                        if (info != null && shouldConsiderUpdate(info, settings)) {
                            latestUpdate = info;
                            break;
                        }
                    }
                } else {
                    // Single release (latest stable)
                    latestUpdate = parseReleaseInfo(response);
                }
                
                if (latestUpdate != null && shouldConsiderUpdate(latestUpdate, settings)) {
                    logger.info("Found newer version: {} (current: {})",
                        latestUpdate.getVersion(), currentVersion);
                    return latestUpdate;
                }
                
                logger.info("No updates available - already on latest version");
                return null;
                
            } catch (Exception e) {
                logger.error("Failed to check for updates", e);
                throw new RuntimeException("Update check failed", e);
            }
        });
    }
    
    /**
     * Parse release information from GitHub API response.
     */
    private UpdateInfo parseReleaseInfo(JsonNode release) {
        try {
            String tagName = release.get("tag_name").asText();
            String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            
            logger.debug("Parsing release: {} (version: {})", tagName, version);
            
            // Skip if not a valid version
            if (!VersionComparator.isValidVersion(version)) {
                logger.debug("Skipping invalid version format: {}", version);
                return null;
            }
            
            UpdateInfo info = new UpdateInfo();
            info.setVersion(version);
            info.setReleaseNotes(release.get("body").asText(""));
            info.setHtmlUrl(release.get("html_url").asText());
            info.setMandatory(release.get("name").asText("").toLowerCase().contains("mandatory"));
            
            // Parse release date
            String publishedAt = release.get("published_at").asText();
            LocalDateTime releaseDate = LocalDateTime.parse(publishedAt, 
                DateTimeFormatter.ISO_DATE_TIME);
            info.setReleaseDate(releaseDate);
            
            // Find Windows executable asset
            JsonNode assets = release.get("assets");
            if (assets != null && assets.isArray()) {
                for (JsonNode asset : assets) {
                    String name = asset.get("name").asText();
                    if (isWindowsExecutable(name)) {
                        info.setDownloadUrl(asset.get("browser_download_url").asText());
                        info.setFileSize(asset.get("size").asLong());
                        
                        // Look for checksum file
                        String checksumName = name + ".sha256";
                        for (JsonNode checksumAsset : assets) {
                            if (checksumAsset.get("name").asText().equals(checksumName)) {
                                // We'll download and read the checksum later
                                info.setChecksum(checksumAsset.get("browser_download_url").asText());
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (info.getDownloadUrl() != null) {
                logger.debug("Successfully parsed release info for version {}", version);
                return info;
            } else {
                logger.debug("No Windows executable found for version {}", version);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Failed to parse release info", e);
            return null;
        }
    }
    
    /**
     * Check if a file name matches Windows executable patterns.
     */
    private boolean isWindowsExecutable(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".exe") || 
               (fileName.contains("win") && fileName.endsWith(".zip")) ||
               fileName.contains("portable");
    }
    
    /**
     * Check if an update should be considered based on version and settings.
     */
    private boolean shouldConsiderUpdate(UpdateInfo info, Settings settings) {
        // Check if version is newer
        boolean isNewer = VersionComparator.isNewer(info.getVersion(), currentVersion);
        if (!isNewer) {
            logger.debug("Version {} is not newer than current version {}",
                info.getVersion(), currentVersion);
            return false;
        }
        
        // Check if version is skipped
        String skippedVersion = settings.getSkippedUpdateVersion();
        if (skippedVersion != null && skippedVersion.equals(info.getVersion()) && !info.isMandatory()) {
            logger.info("Version {} is skipped by user preference", info.getVersion());
            return false;
        }
        
        logger.debug("Version {} should be considered for update", info.getVersion());
        return true;
    }
    
    /**
     * Download an update file.
     * 
     * @param updateInfo Update information
     * @return CompletableFuture with the downloaded file
     */
    public CompletableFuture<File> downloadUpdate(UpdateInfo updateInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting download of version {}", updateInfo.getVersion());
                
                // Create temp directory for downloads
                Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "simp3-updates");
                Files.createDirectories(tempDir);
                logger.info("Download directory: {}", tempDir);
                
                // Determine file name from URL
                String fileName = updateInfo.getDownloadUrl().substring(
                    updateInfo.getDownloadUrl().lastIndexOf('/') + 1);
                Path targetFile = tempDir.resolve(fileName);
                logger.info("Target file: {}", targetFile);
                
                // Download file
                downloadFile(updateInfo.getDownloadUrl(), targetFile, updateInfo.getFileSize());
                
                // Verify checksum if available
                if (updateInfo.getChecksum() != null && updateInfo.getChecksum().startsWith("http")) {
                    logger.info("Verifying checksum...");
                    verifyChecksum(targetFile, updateInfo.getChecksum());
                } else {
                    logger.info("No checksum available for verification");
                }
                
                logger.info("Download completed successfully: {}", targetFile);
                return targetFile.toFile();
                
            } catch (Exception e) {
                logger.error("Failed to download update", e);
                throw new RuntimeException("Download failed", e);
            }
        });
    }
    
    /**
     * Download a file with progress tracking.
     */
    private void downloadFile(String urlStr, Path targetFile, long expectedSize) throws IOException {
        logger.info("Downloading from: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "SiMP3-Updater");
        
        long startTime = System.currentTimeMillis();
        
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetFile))) {
            
            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int bytesRead;
            long lastLogTime = System.currentTimeMillis();
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                
                // Update progress
                if (expectedSize > 0) {
                    final long currentDownloaded = downloaded;
                    final double progress = (double) currentDownloaded / expectedSize;
                    Platform.runLater(() -> {
                        downloadProgress.set(progress);
                        downloadStatus.set(String.format("Downloaded %.1f MB of %.1f MB",
                            currentDownloaded / (1024.0 * 1024.0),
                            expectedSize / (1024.0 * 1024.0)));
                    });
                    
                    // Log progress every 5 seconds
                    if (System.currentTimeMillis() - lastLogTime > 5000) {
                        logger.info("Download progress: {:.1f}% ({:.1f} MB / {:.1f} MB)",
                            progress * 100,
                            currentDownloaded / (1024.0 * 1024.0),
                            expectedSize / (1024.0 * 1024.0));
                        lastLogTime = System.currentTimeMillis();
                    }
                }
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Download completed in {} seconds", duration / 1000.0);
        
        Platform.runLater(() -> {
            downloadProgress.set(1.0);
            downloadStatus.set("Download complete");
        });
    }
    
    /**
     * Verify file checksum.
     */
    private void verifyChecksum(Path file, String checksumUrl) throws Exception {
        // Download checksum file
        Path checksumFile = file.getParent().resolve(file.getFileName() + ".sha256");
        downloadFile(checksumUrl, checksumFile, -1);
        
        // Read expected checksum
        String expectedChecksum = Files.readString(checksumFile).trim().split("\\s+")[0];
        
        // Calculate actual checksum
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        String actualChecksum = bytesToHex(digest.digest());
        
        if (!expectedChecksum.equalsIgnoreCase(actualChecksum)) {
            throw new SecurityException("Checksum verification failed");
        }
        
        logger.info("Checksum verified successfully");
    }
    
    /**
     * Convert byte array to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Apply the downloaded update.
     * 
     * @param updateFile Downloaded update file
     * @return true if update was staged successfully
     */
    public boolean applyUpdate(File updateFile) {
        try {
            logger.info("Staging update for installation: {}", updateFile.getName());
            
            // Create update directory
            Path updateDir = Paths.get("update");
            Files.createDirectories(updateDir);
            logger.info("Created update directory: {}", updateDir.toAbsolutePath());
            
            // Copy update file
            Path stagedUpdate = updateDir.resolve(updateFile.getName());
            Files.copy(updateFile.toPath(), stagedUpdate, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Staged update file: {}", stagedUpdate.toAbsolutePath());
            
            // Create update script
            createUpdateScript(stagedUpdate);
            
            logger.info("Update staged successfully - will be applied on next restart");
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to stage update: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create update script for Windows.
     */
    private void createUpdateScript(Path updateFile) throws IOException {
        Path scriptPath = Paths.get("update", "apply-update.bat");
        
        String script = String.format("""
            @echo off
            echo Applying SiMP3 update...
            timeout /t 3 /nobreak > nul
            
            :: Backup current executable
            if exist "SiMP3.exe" (
                move /Y "SiMP3.exe" "SiMP3.exe.backup"
            )
            
            :: Extract update if it's a zip file
            if "%%~x1"==".zip" (
                powershell -Command "Expand-Archive -Path '%s' -DestinationPath '.' -Force"
            ) else (
                :: Copy new executable
                copy /Y "%s" "SiMP3.exe"
            )
            
            :: Clean up
            del /Q "%s"
            
            :: Start updated application
            start "" "SiMP3.exe"
            
            :: Delete this script
            del "%%~f0"
            """, 
            updateFile.toString(),
            updateFile.toString(),
            updateFile.toString()
        );
        
        Files.writeString(scriptPath, script);
        logger.info("Update script created: {}", scriptPath);
    }
    
    /**
     * Check if an update is staged and ready to apply.
     */
    public boolean isUpdateStaged() {
        return Files.exists(Paths.get("update", "apply-update.bat"));
    }
    
    /**
     * Get the current application version.
     */
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    /**
     * Get download progress property.
     */
    public DoubleProperty downloadProgressProperty() {
        return downloadProgress;
    }
    
    /**
     * Get download status property.
     */
    public StringProperty downloadStatusProperty() {
        return downloadStatus;
    }
    
    /**
     * Get the settings service instance.
     */
    public SettingsService getSettingsService() {
        return settingsService;
    }
    
    /**
     * Shutdown the update service.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}