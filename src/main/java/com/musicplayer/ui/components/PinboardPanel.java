package com.musicplayer.ui.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Combined pinboard, library stats, and listening stats panel.
 */
public class PinboardPanel extends VBox {
    
    private final VBox libraryStatsSection;
    private final VBox pinboardSection;
    private final VBox listeningStatsSection;
    private final List<PinboardItem> pinnedItems = new ArrayList<>();
    
    // Labels for library stats
    private Label totalSongsLabel;
    private Label totalAlbumsLabel;
    private Label musicFolderLabel;
    
    // Labels for listening stats
    private Label listenedTodayLabel;
    private Label listenedWeeklyLabel;
    private Label listenedMonthlyLabel;
    private Label mostPlayedLabel;
    
    public PinboardPanel() {
        setSpacing(20); // Increased spacing for better distribution
        setPadding(new Insets(10));
        
        // Library stats section - placed under Library header
        libraryStatsSection = new VBox(3);
        libraryStatsSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Initialize library stats labels
        totalSongsLabel = createStatsLabel("Songs: 0");
        totalAlbumsLabel = createStatsLabel("Albums: 0");
        musicFolderLabel = createStatsLabel("Folder: Not set");
        musicFolderLabel.setWrapText(true);
        
        libraryStatsSection.getChildren().addAll(
            totalSongsLabel,
            totalAlbumsLabel,
            musicFolderLabel
        );
        
        // Pinboard section
        Label pinboardLabel = new Label("Pinboard");
        pinboardLabel.setFont(Font.font(14));
        pinboardLabel.getStyleClass().add("sidebar-header");
        
        pinboardSection = new VBox(5);
        pinboardSection.setPadding(new Insets(5, 0, 10, 0));
        
        // Listening stats section (previously Activity)
        Label listeningStatsLabel = new Label("Listening Stats");
        listeningStatsLabel.setFont(Font.font(14));
        listeningStatsLabel.getStyleClass().add("sidebar-header");
        
        listeningStatsSection = new VBox(5);
        listeningStatsSection.setPadding(new Insets(5, 0, 0, 0));
        
        // Initialize listening stats labels
        listenedTodayLabel = createStatsLabel("Today: 0 songs");
        listenedWeeklyLabel = createStatsLabel("This week: 0 songs");
        listenedMonthlyLabel = createStatsLabel("This month: 0 songs");
        mostPlayedLabel = createStatsLabel("Most played: -");
        mostPlayedLabel.setWrapText(true);
        
        listeningStatsSection.getChildren().addAll(
            listenedTodayLabel,
            listenedWeeklyLabel,
            listenedMonthlyLabel,
            mostPlayedLabel
        );
        
        // Add all sections
        getChildren().addAll(
            libraryStatsSection,
            new Separator(),
            pinboardLabel,
            pinboardSection,
            new Separator(),
            listeningStatsLabel,
            listeningStatsSection
        );
    }
    
    private Label createStatsLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        return label;
    }
    
    // Library stats update methods
    public void updateLibraryStats(int totalSongs, int totalAlbums, String musicFolder) {
        totalSongsLabel.setText("Songs: " + totalSongs);
        totalAlbumsLabel.setText("Albums: " + totalAlbums);
        if (musicFolder != null && !musicFolder.isEmpty()) {
            // Show only folder name or last two directories for brevity
            File folder = new File(musicFolder);
            String displayPath = folder.getName();
            if (folder.getParentFile() != null) {
                displayPath = folder.getParentFile().getName() + "/" + displayPath;
            }
            musicFolderLabel.setText("Folder: " + displayPath);
        } else {
            musicFolderLabel.setText("Folder: Not set");
        }
    }
    
    // Listening stats update methods
    public void updateListeningStats(int todayCount, int weeklyCount, int monthlyCount, String mostPlayed) {
        listenedTodayLabel.setText("Today: " + todayCount + " songs");
        listenedWeeklyLabel.setText("This week: " + weeklyCount + " songs");
        listenedMonthlyLabel.setText("This month: " + monthlyCount + " songs");
        mostPlayedLabel.setText("Most played: " + (mostPlayed != null ? mostPlayed : "-"));
    }
    
    // Existing pinboard methods remain the same
    public void addPinnedItem(String id, String name, PinboardItem.ItemType type, Runnable onClickAction) {
        // Check if already pinned
        if (pinnedItems.stream().anyMatch(item -> item.getItemId().equals(id))) {
            return;
        }
        
        PinboardItem item = new PinboardItem(id, name, type, onClickAction);
        pinnedItems.add(item);
        pinboardSection.getChildren().add(item);
    }
    
    public void removePinnedItem(String id) {
        pinnedItems.removeIf(item -> {
            if (item.getItemId().equals(id)) {
                pinboardSection.getChildren().remove(item);
                return true;
            }
            return false;
        });
    }
    
    public boolean isPinned(String id) {
        return pinnedItems.stream().anyMatch(item -> item.getItemId().equals(id));
    }
    
    // Keep the activity notification method for future toast implementation
    // This will be repurposed later
    public void addActivity(ActivityFeedItem.ActivityType type, String message) {
        // For now, this is a no-op. We'll implement toast notifications later.
        // The notification logic is preserved here for future use.
    }
    
    public void clearActivities() {
        // No-op for now, preserved for compatibility
    }
} 