package com.musicplayer.ui.components;

import com.musicplayer.data.models.Album;
import com.musicplayer.data.repositories.AlbumRepository;
import com.musicplayer.data.repositories.SongRepository;
import com.musicplayer.ui.controllers.AlbumEditController;
import com.musicplayer.ui.util.AlbumArtLoader;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
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
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private boolean selected = false;

    public AlbumCard(Album album, AlbumRepository albumRepository, SongRepository songRepository) {
        this.album = album;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
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
        
        // Load album art from the album (checks custom art first, then metadata)
        AlbumArtLoader.loadAlbumArt(album)
            .thenAcceptAsync(image -> {
                coverView.setImage(image);
            }, Platform::runLater);

        Label titleLbl = new Label(album.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(100);
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        content.getChildren().addAll(coverView, titleLbl);
        
        // Create settings button in top-right corner
        Button settingsButton = createSettingsButton();
        
        // Create delete button in top-left corner
        Button deleteButton = createDeleteButton();
        
        // Add content and buttons to main container
        getChildren().addAll(content, settingsButton, deleteButton);
        
        getStyleClass().add("album-card");
        setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        setOnMouseClicked(e -> {
            // Don't trigger selection if settings or delete button was clicked
            Button deleteBtn = (Button) getChildren().get(2); // Get the delete button
            if (e.getTarget() != settingsButton && !isDescendantOf(e.getTarget(), settingsButton) &&
                e.getTarget() != deleteBtn && !isDescendantOf(e.getTarget(), deleteBtn)) {
                toggleSelected();
            }
        });

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
    
    /**
     * Create the settings button positioned in the top-right corner.
     */
    private Button createSettingsButton() {
        Button settingsButton = new Button();
        settingsButton.setFocusTraversable(false);
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-background-radius: 50%; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px;");
        
        // Add settings icon
        try {
            ImageView settingsIcon = new ImageView(new Image(
                getClass().getResourceAsStream("/images/icons/settings.png"), 12, 12, true, true));
            settingsButton.setGraphic(settingsIcon);
        } catch (Exception e) {
            // If icon not found, use text
            settingsButton.setText("⚙");
            settingsButton.setStyle(settingsButton.getStyle() + " -fx-font-size: 10px;");
        }
        
        // Position in top-right corner
        StackPane.setAlignment(settingsButton, Pos.TOP_RIGHT);
        StackPane.setMargin(settingsButton, new Insets(2, 2, 0, 0));
        
        // Add hover effect
        settingsButton.setOnMouseEntered(e -> {
            settingsButton.setStyle(settingsButton.getStyle() + " -fx-background-color: rgba(255, 255, 255, 0.2);");
        });
        
        settingsButton.setOnMouseExited(e -> {
            settingsButton.setStyle(settingsButton.getStyle().replace(" -fx-background-color: rgba(255, 255, 255, 0.2);", ""));
        });
        
        // Handle settings button click
        settingsButton.setOnAction(e -> openAlbumEditDialog());
        
        return settingsButton;
    }
    
    /**
     * Create the delete button positioned in the top-left corner.
     */
    private Button createDeleteButton() {
        Button deleteButton = new Button();
        deleteButton.setFocusTraversable(false);
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-background-radius: 50%; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px;");
        
        // Add trash icon
        try {
            ImageView deleteIcon = new ImageView(new Image(
                getClass().getResourceAsStream("/images/icons/trash.png"), 12, 12, true, true));
            deleteButton.setGraphic(deleteIcon);
        } catch (Exception e) {
            // If icon not found, use text
            deleteButton.setText("🗑");
            deleteButton.setStyle(deleteButton.getStyle() + " -fx-font-size: 10px;");
        }
        
        // Position in top-left corner
        StackPane.setAlignment(deleteButton, Pos.TOP_LEFT);
        StackPane.setMargin(deleteButton, new Insets(2, 0, 0, 2));
        
        // Add hover effect
        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setStyle(deleteButton.getStyle() + " -fx-background-color: rgba(255, 0, 0, 0.3);");
        });
        
        deleteButton.setOnMouseExited(e -> {
            deleteButton.setStyle(deleteButton.getStyle().replace(" -fx-background-color: rgba(255, 0, 0, 0.3);", ""));
        });
        
        // Handle delete button click
        deleteButton.setOnAction(e -> openDeleteConfirmationDialog());
        
        return deleteButton;
    }
    
    /**
     * Open the delete confirmation dialog.
     */
    private void openDeleteConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Album");
        alert.setHeaderText("Delete Album: " + album.getTitle());
        alert.setContentText("Are you sure you want to delete this album and all its songs?\n" +
                            "This action will permanently remove all music files from your computer.\n" +
                            "This cannot be undone.");
        
        // Apply custom CSS if available
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        } catch (Exception e) {
            // CSS not found, continue without styling
        }
        
        alert.initOwner(getScene().getWindow());
        
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        
        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                deleteAlbum();
            }
        });
    }
    
    /**
     * Delete the album and all its associated files.
     */
    private void deleteAlbum() {
        try {
            // Delete physical files first
            deletePhysicalFiles();
            
            // Delete songs from the song repository
            for (com.musicplayer.data.models.Song song : album.getSongs()) {
                songRepository.delete(song.getId());
            }
            
            // Delete from album repository
            albumRepository.delete(album.getId());
            
            // Notify parent container to refresh the UI
            notifyParentOfDeletion();
            
            System.out.println("Album deleted successfully: " + album.getTitle());
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete Error");
            alert.setHeaderText("Error deleting album");
            alert.setContentText("An error occurred while deleting the album: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Delete all physical files associated with this album.
     */
    private void deletePhysicalFiles() throws Exception {
        java.util.List<String> filesToDelete = new java.util.ArrayList<>();
        
        // Collect all song file paths
        for (com.musicplayer.data.models.Song song : album.getSongs()) {
            if (song.getFilePath() != null) {
                filesToDelete.add(song.getFilePath());
            }
        }
        
        // Delete custom album art if it exists
        if (album.getCoverArtPath() != null) {
            java.io.File coverArtFile = new java.io.File(album.getCoverArtPath());
            if (coverArtFile.exists() && coverArtFile.getParentFile().getName().equals("album-art")) {
                // Only delete if it's in our custom album-art directory
                filesToDelete.add(album.getCoverArtPath());
            }
        }
        
        // Delete all collected files
        for (String filePath : filesToDelete) {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("Failed to delete file: " + filePath);
                } else {
                    System.out.println("Deleted file: " + filePath);
                }
            }
        }
        
        // Try to delete empty directories
        deleteEmptyDirectories(filesToDelete);
    }
    
    /**
     * Delete empty directories after file deletion.
     */
    private void deleteEmptyDirectories(java.util.List<String> deletedFiles) {
        java.util.Set<String> directoriesToCheck = new java.util.HashSet<>();
        
        // Collect unique directories
        for (String filePath : deletedFiles) {
            java.io.File file = new java.io.File(filePath);
            String parentDir = file.getParent();
            if (parentDir != null) {
                directoriesToCheck.add(parentDir);
            }
        }
        
        // Try to delete empty directories
        for (String dirPath : directoriesToCheck) {
            java.io.File dir = new java.io.File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                String[] contents = dir.list();
                if (contents != null && contents.length == 0) {
                    if (dir.delete()) {
                        System.out.println("Deleted empty directory: " + dirPath);
                    }
                }
            }
        }
    }
    
    /**
     * Notify the parent container that this album has been deleted.
     */
    private void notifyParentOfDeletion() {
        // Find the parent AlbumGridView and request a refresh
        javafx.scene.Node parent = getParent();
        while (parent != null) {
            if (parent instanceof AlbumGridView) {
                ((AlbumGridView) parent).refreshAlbums();
                break;
            }
            parent = parent.getParent();
        }
    }
    
    /**
     * Check if a node is a descendant of another node.
     */
    private boolean isDescendantOf(Object target, javafx.scene.Node parent) {
        if (!(target instanceof javafx.scene.Node)) {
            return false;
        }
        
        javafx.scene.Node node = (javafx.scene.Node) target;
        while (node != null) {
            if (node == parent) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }
    
    /**
     * Open the album edit dialog.
     */
    private void openAlbumEditDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/album-edit.fxml"));
            DialogPane dialogPane = loader.load();
            AlbumEditController controller = loader.getController();
            controller.setAlbum(album);
            
            // Create a custom dialog without default buttons
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Album - " + album.getTitle());
            dialog.initOwner(getScene().getWindow());
            
            // Apply CSS for styling
            try {
                dialogPane.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            } catch (Exception e) {
                // CSS not found, continue without styling
            }
            
            // Add a hidden button type to allow dialog to be closeable
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);
            
            // Handle custom buttons
            controller.setOnSave(result -> {
                // Handle the save operation
                handleAlbumEdit(result);
                dialog.setResult(ButtonType.OK);
                dialog.close();
            });
            
            controller.getSaveButton().setOnAction(e -> {
                controller.saveChanges();
            });
            
            controller.getCancelButton().setOnAction(e -> {
                dialog.setResult(ButtonType.CANCEL);
                dialog.close();
            });
            
            // Also handle ESC key to close dialog
            dialogPane.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    dialog.setResult(ButtonType.CANCEL);
                    dialog.close();
                }
            });
            
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open album edit dialog");
            alert.setContentText("An error occurred while opening the album edit dialog.");
            alert.showAndWait();
        }
    }
    
    /**
     * Handle the result of album editing.
     */
    private void handleAlbumEdit(AlbumEditController.AlbumEditResult result) {
        try {
            // Update album name if it changed
            if (!result.getNewName().equals(album.getTitle())) {
                album.setTitle(result.getNewName());
                
                // Update the title label in the UI
                VBox content = (VBox) getChildren().get(0);
                Label titleLabel = (Label) content.getChildren().get(1);
                titleLabel.setText(album.getTitle());
            }
            
            // Handle album art changes
            if (result.getNewImageFile() != null) {
                // Save the custom album art
                saveCustomAlbumArt(result.getNewImageFile());
            } else if (result.isResetToDefault()) {
                // Reset to default album art (clear custom path)
                album.setCoverArtPath(null);
                
                // Refresh the album art display
                VBox content = (VBox) getChildren().get(0);
                ImageView coverView = (ImageView) content.getChildren().get(0);
                
                // Load album art using the updated loader that checks custom art first
                AlbumArtLoader.loadAlbumArt(album)
                    .thenAcceptAsync(image -> {
                        coverView.setImage(image);
                    }, Platform::runLater);
            }
            
            // Save the album changes to the repository
            albumRepository.save(album);
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error processing album edit");
            alert.setContentText("An error occurred while processing the album changes.");
            alert.showAndWait();
        }
    }
    
    /**
     * Save custom album art to the data directory and update the album.
     */
    private void saveCustomAlbumArt(java.io.File imageFile) {
        try {
            // Create custom album art directory if it doesn't exist
            java.nio.file.Path dataDir = java.nio.file.Paths.get(System.getProperty("user.home"), ".simp3", "album-art");
            java.nio.file.Files.createDirectories(dataDir);
            
            // Generate a unique filename based on album ID and title
            String sanitizedTitle = album.getTitle().replaceAll("[^a-zA-Z0-9]", "_");
            String extension = getFileExtension(imageFile.getName());
            String filename = "album_" + album.getId() + "_" + sanitizedTitle + "." + extension;
            java.nio.file.Path targetPath = dataDir.resolve(filename);
            
            // Copy the image file to the custom album art directory
            java.nio.file.Files.copy(imageFile.toPath(), targetPath, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Update the album with the new cover art path
            album.setCoverArtPath(targetPath.toString());
            
            // Update the UI to show the new image immediately
            VBox content = (VBox) getChildren().get(0);
            ImageView coverView = (ImageView) content.getChildren().get(0);
            
            // Load the new image
            Image newImage = new Image(targetPath.toUri().toString(), 90, 90, true, true);
            coverView.setImage(newImage);
            
            System.out.println("Custom album art saved: " + targetPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error saving album art");
            alert.setContentText("Could not save the selected image: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Get the file extension from a filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "png"; // Default extension
    }
} 