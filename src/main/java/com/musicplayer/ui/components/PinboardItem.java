package com.musicplayer.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Represents a pinned item (album/playlist) in the pinboard.
 */
public class PinboardItem extends HBox {
    
    public enum ItemType {
        ALBUM, PLAYLIST, ARTIST
    }
    
    private final String id;
    private final String name;
    private final ItemType type;
    private final Runnable onClickAction;
    
    public PinboardItem(String id, String name, ItemType type, Runnable onClickAction) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.onClickAction = onClickAction;
        
        setupUI();
    }
    
    private void setupUI() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(5));
        setCursor(Cursor.HAND);
        setMaxWidth(Double.MAX_VALUE);
        
        // Icon based on type and special cases
        String iconPath;
        if ("favorites".equals(id)) {
            iconPath = "/images/icons/fav.png";
        } else {
            iconPath = switch (type) {
            case ALBUM -> "/images/icons/album_placeholder.png";
            case PLAYLIST -> "/images/icons/song.png";
            case ARTIST -> "/images/icons/app.png";
        };
        }
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");
        nameLabel.setMaxWidth(150);
        nameLabel.setEllipsisString("...");
        
        getChildren().addAll(icon, nameLabel);
        
        // Hover effect
        setOnMouseEntered(e -> {
            // Semi-transparent lime overlay
            setStyle("-fx-background-color: #32cd3246; -fx-background-radius: 3;");
            setEffect(new DropShadow(1, Color.BLACK));
        });
        
        setOnMouseExited(e -> {
            setStyle("-fx-background-color: transparent;");
            setEffect(null);
        });
        
        setOnMouseClicked(e -> {
            if (onClickAction != null) {
                onClickAction.run();
            }
        });
    }
    
    public String getItemId() {
        return id;
    }
    
    public ItemType getType() {
        return type;
    }
} 