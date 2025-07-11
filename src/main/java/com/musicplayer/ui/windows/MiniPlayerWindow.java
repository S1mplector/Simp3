package com.musicplayer.ui.windows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import com.musicplayer.core.playlist.PlaylistEngine;
import com.musicplayer.data.models.Song;
import com.musicplayer.services.AudioPlayerService;
import com.musicplayer.services.FavoritesService;
import com.musicplayer.services.PlaylistManager;
import com.musicplayer.services.SettingsService;
import com.musicplayer.ui.dialogs.PlaylistSelectionPopup;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Compact mini player window that can be always-on-top.
 * Provides essential playback controls in a minimal interface.
 */
public class MiniPlayerWindow {
    
    private final Stage miniStage;
    private final AudioPlayerService audioPlayerService;
    private final Stage mainStage;
    private final SettingsService settingsService;
    private final FavoritesService favoritesService;
    private final PlaylistManager playlistManager;
    
    // UI Components
    private Label titleLabel;
    private Label artistLabel;
    private Button playPauseButton;
    private Button previousButton;
    private Button nextButton;
    private Button shuffleButton;
    private Button repeatButton;
    private Button moreOptionsButton;
    private Button pinButton;
    private Button closeButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private StackPane albumArtContainer;
    private ImageView albumArt;
    private ImageView albumArtTransition;
    private Label timeLabel;
    
    // Icons
    private final Image playIcon;
    private final Image pauseIcon;
    private final Image previousIcon;
    private final Image nextIcon;
    private final Image shuffleIcon;
    private final Image repeatIcon;
    private final Image pinIcon;
    private final Image unpinIcon;
    private final Image closeIcon;
    private final Image defaultAlbumArt;
    
    // Window state
    private boolean isPinned = true;
    private double xOffset = 0;
    private double yOffset = 0;
    
    // Sleep timer
    private javafx.animation.Timeline sleepTimer;
    
    // Logger for album art loading
    private static final Logger LOGGER = Logger.getLogger(MiniPlayerWindow.class.getName());
    
    public MiniPlayerWindow(AudioPlayerService audioPlayerService, Stage mainStage,
                           SettingsService settingsService, FavoritesService favoritesService,
                           PlaylistManager playlistManager) {
        this.audioPlayerService = audioPlayerService;
        this.mainStage = mainStage;
        this.settingsService = settingsService;
        this.favoritesService = favoritesService;
        this.playlistManager = playlistManager;
        this.miniStage = new Stage();
        
        // Load icons
        playIcon = new Image(getClass().getResourceAsStream("/images/icons/play.png"));
        pauseIcon = new Image(getClass().getResourceAsStream("/images/icons/pause.png"));
        previousIcon = new Image(getClass().getResourceAsStream("/images/icons/previous.png"));
        nextIcon = new Image(getClass().getResourceAsStream("/images/icons/next.png"));
        shuffleIcon = new Image(getClass().getResourceAsStream("/images/icons/shuffle.png"));
        repeatIcon = new Image(getClass().getResourceAsStream("/images/icons/repeat.png"));
        // For now, use existing icons as placeholders for pin functionality
        pinIcon = new Image(getClass().getResourceAsStream("/images/icons/ok.png"));
        unpinIcon = new Image(getClass().getResourceAsStream("/images/icons/cancel.png"));
        closeIcon = new Image(getClass().getResourceAsStream("/images/icons/remove.png"));
        defaultAlbumArt = new Image(getClass().getResourceAsStream("/images/icons/album_placeholder.png"));
        
        initializeUI();
        setupBindings();
        setupDragHandling();
    }
    
    private void initializeUI() {
        miniStage.initStyle(StageStyle.UNDECORATED);
        miniStage.setTitle("SiMP3 Mini Player");
        miniStage.setAlwaysOnTop(isPinned);
        miniStage.setResizable(false);
        
        // Album art with container for transitions
        albumArtContainer = new StackPane();
        albumArtContainer.setPrefSize(80, 80);
        albumArtContainer.setMaxSize(80, 80);
        
        albumArt = new ImageView(defaultAlbumArt);
        albumArt.setFitWidth(80);
        albumArt.setFitHeight(80);
        albumArt.setPreserveRatio(true);
        albumArt.setSmooth(true);
        
        albumArtTransition = new ImageView();
        albumArtTransition.setFitWidth(80);
        albumArtTransition.setFitHeight(80);
        albumArtTransition.setPreserveRatio(true);
        albumArtTransition.setSmooth(true);
        albumArtTransition.setOpacity(0);
        
        // Add shadow effect to album art
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetX(2.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.color(0, 0, 0, 0.3));
        albumArtContainer.setEffect(shadow);
        
        albumArtContainer.getChildren().addAll(albumArt, albumArtTransition);
        
        // Song info
        titleLabel = new Label("No song playing");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        titleLabel.setMaxWidth(200);
        titleLabel.setEllipsisString("...");
        
        artistLabel = new Label("");
        artistLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        artistLabel.setMaxWidth(200);
        artistLabel.setEllipsisString("...");
        
        VBox songInfo = new VBox(2);
        songInfo.getChildren().addAll(titleLabel, artistLabel);
        songInfo.setAlignment(Pos.CENTER_LEFT);
        
        // Time label
        timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        
        // Progress slider
        progressSlider = new Slider();
        progressSlider.setPrefWidth(200);
        progressSlider.setMaxHeight(5);
        progressSlider.setStyle("-fx-control-inner-background: #cccccc;");
        
        // Control buttons
        previousButton = createControlButton(previousIcon, "Previous");
        playPauseButton = createControlButton(playIcon, "Play/Pause");
        nextButton = createControlButton(nextIcon, "Next");
        
        // Playback mode buttons
        shuffleButton = createModeButton(shuffleIcon, "Shuffle");
        repeatButton = createModeButton(repeatIcon, "Repeat");
        updateModeButtons();
        
        // Volume slider (compact)
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(60);
        volumeSlider.setMaxHeight(5);
        volumeSlider.getStyleClass().add("volume-slider");
        
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-font-size: 12px;");
        
        HBox volumeControl = new HBox(3);
        volumeControl.setAlignment(Pos.CENTER);
        volumeControl.getChildren().addAll(volumeLabel, volumeSlider);
        
        HBox controls = new HBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(previousButton, playPauseButton, nextButton, shuffleButton, repeatButton);
        
        // Window controls
        moreOptionsButton = createWindowButton("⋮", "More Options");
        moreOptionsButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                  "-fx-background-color: transparent; -fx-border-color: transparent; " +
                                  "-fx-padding: 2; -fx-cursor: hand;");
        pinButton = createWindowButton(isPinned ? pinIcon : unpinIcon, "Toggle Always on Top");
        closeButton = createWindowButton(closeIcon, "Close Mini Player");
        
        HBox windowControls = new HBox(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.getChildren().addAll(moreOptionsButton, pinButton, closeButton);
        
        // Layout
        VBox centerContent = new VBox(3);
        centerContent.setAlignment(Pos.CENTER_LEFT);
        centerContent.getChildren().addAll(songInfo, timeLabel, progressSlider, controls);
        centerContent.setPadding(new Insets(5));
        
        VBox rightContent = new VBox(5);
        rightContent.setAlignment(Pos.CENTER);
        rightContent.getChildren().addAll(volumeControl, windowControls);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox mainLayout = new HBox(10);
        mainLayout.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(albumArtContainer, centerContent, spacer, rightContent);
        mainLayout.getStyleClass().add("mini-player-root");
        
        Scene scene = new Scene(mainLayout, 480, 120);
        scene.getStylesheets().add(getClass().getResource("/css/mini-player.css").toExternalForm());
        miniStage.setScene(scene);
        
        // Add context menu
        setupContextMenu(mainLayout);
        
        // Setup more options menu
        setupMoreOptionsMenu();
        
        // Load saved position or use default
        loadWindowPosition();
    }
    
    private Button createControlButton(Image icon, String tooltip) {
        Button button = new Button();
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("control-button");
        
        // Add click animation
        button.setOnMousePressed(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);
            scaleDown.play();
        });
        
        button.setOnMouseReleased(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.play();
        });
        
        return button;
    }
    
    private Button createWindowButton(Image icon, String tooltip) {
        Button button = new Button();
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("window-button");
        
        return button;
    }
    
    private Button createWindowButton(String text, String tooltip) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("window-button");
        
        return button;
    }
    
    private Button createModeButton(Image icon, String tooltip) {
        Button button = new Button();
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("mode-button");
        
        // Add click animation
        button.setOnMousePressed(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);
            scaleDown.play();
        });
        
        button.setOnMouseReleased(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.play();
        });
        
        return button;
    }
    
    private void setupBindings() {
        // Play/pause button
        playPauseButton.setOnAction(e -> audioPlayerService.togglePlayPause());
        previousButton.setOnAction(e -> audioPlayerService.previousTrack());
        nextButton.setOnAction(e -> audioPlayerService.nextTrack());
        
        // Shuffle and repeat buttons
        shuffleButton.setOnAction(e -> {
            audioPlayerService.setShuffle(!audioPlayerService.isShuffle());
            updateModeButtons();
        });
        
        repeatButton.setOnAction(e -> {
            PlaylistEngine.RepeatMode currentMode = audioPlayerService.getRepeatMode();
            PlaylistEngine.RepeatMode nextMode;
            switch (currentMode) {
                case NONE:
                    nextMode = PlaylistEngine.RepeatMode.ALL;
                    break;
                case ALL:
                    nextMode = PlaylistEngine.RepeatMode.ONE;
                    break;
                case ONE:
                default:
                    nextMode = PlaylistEngine.RepeatMode.NONE;
                    break;
            }
            audioPlayerService.setRepeatMode(nextMode);
            updateModeButtons();
        });
        
        // Update play/pause icon
        audioPlayerService.playingProperty().addListener((obs, wasPlaying, isPlaying) -> {
            ImageView imageView = (ImageView) playPauseButton.getGraphic();
            imageView.setImage(isPlaying ? pauseIcon : playIcon);
        });
        
        // Update song info
        audioPlayerService.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            updateSongInfo(newSong);
        });
        
        // Progress slider - remove bidirectional binding to prevent conflicts
        progressSlider.maxProperty().bind(audioPlayerService.totalTimeProperty());
        
        // Update slider position from audio service
        audioPlayerService.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.doubleValue());
            }
        });
        
        // Seek on click and drag
        progressSlider.setOnMousePressed(event -> {
            if (audioPlayerService.getTotalTime() > 0) {
                double seekTime = (event.getX() / progressSlider.getWidth()) * audioPlayerService.getTotalTime();
                audioPlayerService.seek(seekTime);
            }
        });
        
        progressSlider.setOnMouseDragged(event -> {
            if (audioPlayerService.getTotalTime() > 0) {
                double seekTime = (event.getX() / progressSlider.getWidth()) * audioPlayerService.getTotalTime();
                audioPlayerService.seek(seekTime);
            }
        });
        
        // Volume control
        volumeSlider.valueProperty().bindBidirectional(audioPlayerService.volumeProperty());
        
        // Update time label
        audioPlayerService.currentTimeProperty().addListener((obs, oldTime, newTime) -> updateTimeLabel());
        audioPlayerService.totalTimeProperty().addListener((obs, oldTime, newTime) -> updateTimeLabel());
        
        // Pin button
        pinButton.setOnAction(e -> togglePin());
        
        // Close button
        closeButton.setOnAction(e -> hide());
        
        // Double-click to restore main window
        miniStage.getScene().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                restoreMainWindow();
            }
        });
    }
    
    private void setupDragHandling() {
        HBox mainLayout = (HBox) miniStage.getScene().getRoot();
        
        mainLayout.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        mainLayout.setOnMouseDragged(event -> {
            miniStage.setX(event.getScreenX() - xOffset);
            miniStage.setY(event.getScreenY() - yOffset);
        });
        
        // Save position when drag ends
        mainLayout.setOnMouseReleased(event -> {
            saveWindowPosition();
        });
    }
    
    private void setupContextMenu(HBox mainLayout) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem showMainWindow = new MenuItem("Show Main Window");
        showMainWindow.setOnAction(e -> restoreMainWindow());
        
        MenuItem alwaysOnTop = new MenuItem("Always on Top");
        alwaysOnTop.setOnAction(e -> togglePin());
        
        MenuItem separator = new MenuItem();
        separator.setDisable(true);
        
        MenuItem close = new MenuItem("Close Mini Player");
        close.setOnAction(e -> hide());
        
        contextMenu.getItems().addAll(showMainWindow, alwaysOnTop, separator, close);
        
        mainLayout.setOnContextMenuRequested(event -> {
            alwaysOnTop.setText(isPinned ? "✓ Always on Top" : "Always on Top");
            contextMenu.show(mainLayout, event.getScreenX(), event.getScreenY());
        });
    }
    
    private void setupMoreOptionsMenu() {
        ContextMenu moreOptionsMenu = new ContextMenu();
        moreOptionsMenu.getStyleClass().add("more-options-menu");
        
        MenuItem addToFavorites = new MenuItem("Add to Favorites");
        addToFavorites.setOnAction(e -> {
            Song currentSong = audioPlayerService.getCurrentSong();
            if (currentSong != null) {
                boolean isFavorite = favoritesService.toggleFavorite(currentSong);
                addToFavorites.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
            }
        });
        
        MenuItem addToPlaylist = new MenuItem("Add to Playlist");
        addToPlaylist.setOnAction(e -> {
            Song currentSong = audioPlayerService.getCurrentSong();
            if (currentSong != null && playlistManager != null) {
                // Convert playlists to ObservableList
                javafx.collections.ObservableList<com.musicplayer.data.models.Playlist> playlists =
                    javafx.collections.FXCollections.observableArrayList(playlistManager.getAllPlaylists());
                
                PlaylistSelectionPopup.show(miniStage, playlists).ifPresent(playlist -> {
                    playlistManager.addSongToPlaylist(playlist.getId(), currentSong);
                });
            }
        });
        
        MenuItem showInLibrary = new MenuItem("Show in Library");
        showInLibrary.setOnAction(e -> restoreMainWindow());
        
        Menu sleepTimerMenu = new Menu("Sleep Timer");
        MenuItem timer15 = new MenuItem("15 minutes");
        MenuItem timer30 = new MenuItem("30 minutes");
        MenuItem timer45 = new MenuItem("45 minutes");
        MenuItem timer60 = new MenuItem("60 minutes");
        MenuItem timerOff = new MenuItem("Turn Off");
        
        timer15.setOnAction(e -> setSleepTimer(15));
        timer30.setOnAction(e -> setSleepTimer(30));
        timer45.setOnAction(e -> setSleepTimer(45));
        timer60.setOnAction(e -> setSleepTimer(60));
        timerOff.setOnAction(e -> cancelSleepTimer());
        
        sleepTimerMenu.getItems().addAll(timer15, timer30, timer45, timer60,
                                         new SeparatorMenuItem(), timerOff);
        
        moreOptionsMenu.getItems().addAll(addToFavorites, addToPlaylist,
                                         new SeparatorMenuItem(), showInLibrary,
                                         new SeparatorMenuItem(), sleepTimerMenu);
        
        // Update favorites text when menu is about to show
        moreOptionsMenu.setOnShowing(e -> {
            Song currentSong = audioPlayerService.getCurrentSong();
            if (currentSong != null) {
                boolean isFavorite = favoritesService.isFavorite(currentSong.getId());
                addToFavorites.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
            }
        });
        
        moreOptionsButton.setOnAction(e -> {
            moreOptionsMenu.show(moreOptionsButton,
                               javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }
    
    private void setSleepTimer(int minutes) {
        cancelSleepTimer();
        
        sleepTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.minutes(minutes),
                e -> {
                    audioPlayerService.pause();
                    Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION,
                            "Sleep timer has stopped playback."
                        );
                        alert.setTitle("Sleep Timer");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                    });
                }
            )
        );
        sleepTimer.play();
        
        // Show confirmation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION,
            "Sleep timer set for " + minutes + " minutes."
        );
        alert.setTitle("Sleep Timer");
        alert.setHeaderText(null);
        alert.show();
    }
    
    private void cancelSleepTimer() {
        if (sleepTimer != null) {
            sleepTimer.stop();
            sleepTimer = null;
        }
    }
    
    private void updateModeButtons() {
        // Update shuffle button
        boolean isShuffled = audioPlayerService.isShuffle();
        shuffleButton.setOpacity(isShuffled ? 1.0 : 0.5);
        shuffleButton.getStyleClass().removeAll("mode-button-active", "mode-button-inactive");
        shuffleButton.getStyleClass().add(isShuffled ? "mode-button-active" : "mode-button-inactive");
        
        // Update repeat button
        PlaylistEngine.RepeatMode repeatMode = audioPlayerService.getRepeatMode();
        double repeatOpacity = repeatMode == PlaylistEngine.RepeatMode.NONE ? 0.5 : 1.0;
        repeatButton.setOpacity(repeatOpacity);
        repeatButton.getStyleClass().removeAll("mode-button-active", "mode-button-inactive", "mode-button-repeat-one");
        
        switch (repeatMode) {
            case ALL:
                repeatButton.getStyleClass().add("mode-button-active");
                repeatButton.setTooltip(new Tooltip("Repeat All"));
                break;
            case ONE:
                repeatButton.getStyleClass().addAll("mode-button-active", "mode-button-repeat-one");
                repeatButton.setTooltip(new Tooltip("Repeat One"));
                break;
            case NONE:
            default:
                repeatButton.getStyleClass().add("mode-button-inactive");
                repeatButton.setTooltip(new Tooltip("Repeat Off"));
                break;
        }
    }
    
    private void updateTimeLabel() {
        double current = audioPlayerService.getCurrentTime();
        double total = audioPlayerService.getTotalTime();
        timeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }
    
    private String formatTime(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
    
    private void updateSongInfo(Song song) {
        if (song != null) {
            titleLabel.setText(song.getTitle());
            artistLabel.setText(song.getArtist() + " • " + song.getAlbum());
            
            // Load album art asynchronously
            loadAlbumArt(song);
        } else {
            titleLabel.setText("No song playing");
            artistLabel.setText("");
            transitionToImage(defaultAlbumArt);
        }
        updateTimeLabel();
    }
    
    /**
     * Load album art from the song's file metadata.
     * @param song The song to load album art for
     */
    private void loadAlbumArt(Song song) {
        CompletableFuture.supplyAsync(() -> {
            try {
                File audioFile = new File(song.getFilePath());
                if (!audioFile.exists()) {
                    return null;
                }
                
                AudioFile f = AudioFileIO.read(audioFile);
                Tag tag = f.getTag();
                
                if (tag != null) {
                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null) {
                        byte[] imageData = artwork.getBinaryData();
                        return new Image(new ByteArrayInputStream(imageData));
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load album art for: " + song.getFilePath(), e);
            }
            return null;
        }).thenAcceptAsync(image -> {
            if (image != null) {
                transitionToImage(image);
            } else {
                transitionToImage(defaultAlbumArt);
            }
        }, Platform::runLater);
    }
    
    /**
     * Transition to a new album art image with fade effect.
     * @param newImage The new image to display
     */
    private void transitionToImage(Image newImage) {
        // Set the new image to the transition ImageView
        albumArtTransition.setImage(newImage);
        
        // Fade out current image and fade in new image
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), albumArt);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), albumArtTransition);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> {
            // Swap the images
            albumArt.setImage(newImage);
            albumArt.setOpacity(1.0);
            albumArtTransition.setOpacity(0.0);
        });
        
        fadeOut.play();
        fadeIn.play();
    }
    
    private void togglePin() {
        isPinned = !isPinned;
        miniStage.setAlwaysOnTop(isPinned);
        
        ImageView imageView = (ImageView) pinButton.getGraphic();
        imageView.setImage(isPinned ? pinIcon : unpinIcon);
        pinButton.setTooltip(new Tooltip(isPinned ? "Disable Always on Top" : "Enable Always on Top"));
    }
    
    private void restoreMainWindow() {
        if (mainStage.isIconified()) {
            mainStage.setIconified(false);
        }
        mainStage.show();
        mainStage.toFront();
        hide();
    }
    
    public void show() {
        miniStage.show();
        // Update current song info
        updateSongInfo(audioPlayerService.getCurrentSong());
    }
    
    public void hide() {
        saveWindowPosition();
        miniStage.hide();
    }
    
    public boolean isShowing() {
        return miniStage.isShowing();
    }
    
    public Stage getStage() {
        return miniStage;
    }
    
    /**
     * Save the current window position to settings.
     */
    private void saveWindowPosition() {
        if (settingsService != null) {
            settingsService.setMiniPlayerPosition(miniStage.getX(), miniStage.getY());
        }
    }
    
    /**
     * Load window position from settings or use default.
     */
    private void loadWindowPosition() {
        double savedX = settingsService != null ? settingsService.getMiniPlayerX() : -1;
        double savedY = settingsService != null ? settingsService.getMiniPlayerY() : -1;
        
        if (savedX >= 0 && savedY >= 0) {
            // Validate that the saved position is still valid for current screen configuration
            if (isPositionValid(savedX, savedY)) {
                miniStage.setX(savedX);
                miniStage.setY(savedY);
            } else {
                // Use default position if saved position is invalid
                setDefaultPosition();
            }
        } else {
            // Use default position if no saved position
            setDefaultPosition();
        }
    }
    
    /**
     * Set the window to its default position (top-right of primary screen).
     */
    private void setDefaultPosition() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        miniStage.setX(primaryScreenBounds.getMaxX() - 500);
        miniStage.setY(20);
    }
    
    /**
     * Check if a position is valid for the current screen configuration.
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return true if the position is valid, false otherwise
     */
    private boolean isPositionValid(double x, double y) {
        // Check if the position is within any screen bounds
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            // Check if at least part of the window would be visible
            if (x + 50 >= bounds.getMinX() && x <= bounds.getMaxX() - 50 &&
                y + 20 >= bounds.getMinY() && y <= bounds.getMaxY() - 20) {
                return true;
            }
        }
        return false;
    }
}