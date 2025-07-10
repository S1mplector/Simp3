package com.musicplayer.ui.components;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Combined pinboard and activity feed panel.
 */
public class PinboardPanel extends VBox {
    
    private final VBox pinboardSection;
    private final VBox activitySection;
    private final List<PinboardItem> pinnedItems = new ArrayList<>();
    private final int MAX_ACTIVITIES = 10;
    
    public PinboardPanel() {
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Pinboard section
        Label pinboardLabel = new Label("Pinboard");
        pinboardLabel.setFont(Font.font(14));
        pinboardLabel.getStyleClass().add("sidebar-header");
        
        pinboardSection = new VBox(5);
        pinboardSection.setPadding(new Insets(5, 0, 10, 0));
        
        // Activity feed section
        Label activityLabel = new Label("Activity");
        activityLabel.setFont(Font.font(14));
        activityLabel.getStyleClass().add("sidebar-header");
        
        activitySection = new VBox(5);
        activitySection.setPadding(new Insets(5, 0, 0, 0));
        
        ScrollPane activityScroll = new ScrollPane(activitySection);
        activityScroll.setFitToWidth(true);
        activityScroll.setPrefHeight(200);
        activityScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        getChildren().addAll(
            pinboardLabel,
            pinboardSection,
            new Separator(),
            activityLabel,
            activityScroll
        );
    }
    
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
    
    public void addActivity(ActivityFeedItem.ActivityType type, String message) {
        ActivityFeedItem item = new ActivityFeedItem(type, message);
        activitySection.getChildren().add(0, item); // Add at top
        
        // Limit activities
        if (activitySection.getChildren().size() > MAX_ACTIVITIES) {
            activitySection.getChildren().remove(MAX_ACTIVITIES);
        }
        
        // Auto-remove after 30 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(30));
        pause.setOnFinished(e -> {
            if (activitySection.getChildren().contains(item)) {
                activitySection.getChildren().remove(item);
            }
        });
        pause.play();
    }
    
    public void clearActivities() {
        activitySection.getChildren().clear();
    }
} 