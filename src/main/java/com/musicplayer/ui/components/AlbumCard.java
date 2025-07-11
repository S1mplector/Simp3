package com.musicplayer.ui.components;

import com.musicplayer.data.models.Album;
import com.musicplayer.ui.util.AlbumArtLoader;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * UI card representing a single album.
 */
public class AlbumCard extends StackPane {

    private final Album album;
    private boolean selected = false;

    public AlbumCard(Album album) {
        this.album = album;
        setPrefSize(110, 130);
        setPadding(new Insets(5));
        setCursor(Cursor.HAND);
        setFocusTraversable(false);

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        // Create ImageView for album art
        ImageView coverView = new ImageView();
        coverView.setFitWidth(90);
        coverView.setFitHeight(90);
        coverView.setPreserveRatio(true);
        coverView.setSmooth(true);
        
        // Load album art from the first song in the album
        if (album.getSongs() != null && !album.getSongs().isEmpty()) {
            // Load album art asynchronously from the first song
            AlbumArtLoader.loadAlbumArt(album.getSongs().get(0))
                .thenAcceptAsync(image -> {
                    coverView.setImage(image);
                }, Platform::runLater);
        } else {
            // Use placeholder if no songs in album
            coverView.setImage(getPlaceholderImage());
        }

        Label titleLbl = new Label(album.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(100);
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        content.getChildren().addAll(coverView, titleLbl);
        getChildren().add(content);
        getStyleClass().add("album-card");
        setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        setOnMouseClicked(e -> toggleSelected());

        // Hover visual cue
        setOnMouseEntered(e -> {
            if (!isSelected()) setStyle("-fx-background-color: #444444; -fx-background-radius: 5;");
        });
        setOnMouseExited(e -> {
            if (!isSelected()) setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");
        });
    }

    private Image getPlaceholderImage() {
        return new Image(AlbumCard.class.getResourceAsStream("/images/icons/album_placeholder.png"), 90, 90, true, true);
    }

    public Album getAlbum() {
        return album;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean sel) {
        this.selected = sel;
        if (sel) {
            // Green glow effect
            setEffect(new DropShadow(20, Color.LIMEGREEN));
        } else {
            setEffect(null);
        }
    }

    private void toggleSelected() {
        setSelected(!isSelected());
    }
} 