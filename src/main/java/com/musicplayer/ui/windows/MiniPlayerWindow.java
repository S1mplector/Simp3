package com.musicplayer.ui.windows;

import com.musicplayer.data.models.Song;
import com.musicplayer.services.AudioPlayerService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    
    // UI Components
    private Label titleLabel;
    private Label artistLabel;
    private Button playPauseButton;
    private Button previousButton;
    private Button nextButton;
    private Button pinButton;
    private Button closeButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private ImageView albumArt;
    private Label timeLabel;
    
    // Icons
    private final Image playIcon;
    private final Image pauseIcon;
    private final Image previousIcon;
    private final Image nextIcon;
    private final Image pinIcon;
    private final Image unpinIcon;
    private final Image closeIcon;
    private final Image defaultAlbumArt;
    
    // Window state
    private boolean isPinned = true;
    private double xOffset = 0;
    private double yOffset = 0;
    
    public MiniPlayerWindow(AudioPlayerService audioPlayerService, Stage mainStage) {
        this.audioPlayerService = audioPlayerService;
        this.mainStage = mainStage;
        this.miniStage = new Stage();
        
        // Load icons
        playIcon = new Image(getClass().getResourceAsStream("/images/icons/play.png"));
        pauseIcon = new Image(getClass().getResourceAsStream("/images/icons/pause.png"));
        previousIcon = new Image(getClass().getResourceAsStream("/images/icons/previous.png"));
        nextIcon = new Image(getClass().getResourceAsStream("/images/icons/next.png"));
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
        
        // Album art
        albumArt = new ImageView(defaultAlbumArt);
        albumArt.setFitWidth(60);
        albumArt.setFitHeight(60);
        albumArt.setPreserveRatio(true);
        albumArt.setSmooth(true);
        
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
        
        // Volume slider (compact)
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(60);
        volumeSlider.setMaxHeight(5);
        volumeSlider.setStyle("-fx-control-inner-background: #cccccc;");
        
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-font-size: 12px;");
        
        HBox volumeControl = new HBox(3);
        volumeControl.setAlignment(Pos.CENTER);
        volumeControl.getChildren().addAll(volumeLabel, volumeSlider);
        
        HBox controls = new HBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(previousButton, playPauseButton, nextButton);
        
        // Window controls
        pinButton = createWindowButton(isPinned ? pinIcon : unpinIcon, "Toggle Always on Top");
        closeButton = createWindowButton(closeIcon, "Close Mini Player");
        
        HBox windowControls = new HBox(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.getChildren().addAll(pinButton, closeButton);
        
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
        mainLayout.getChildren().addAll(albumArt, centerContent, spacer, rightContent);
        mainLayout.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        Scene scene = new Scene(mainLayout, 450, 90);
        scene.getStylesheets().add(getClass().getResource("/css/mini-player.css").toExternalForm());
        miniStage.setScene(scene);
        
        // Add context menu
        setupContextMenu(mainLayout);
        
        // Position window at top-right of screen
        miniStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxX() - 470);
        miniStage.setY(20);
    }
    
    private Button createControlButton(Image icon, String tooltip) {
        Button button = new Button();
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                       "-fx-padding: 5; -fx-cursor: hand;");
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15; " +
                                                      "-fx-border-color: transparent; -fx-padding: 5; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                                                     "-fx-padding: 5; -fx-cursor: hand;"));
        
        return button;
    }
    
    private Button createWindowButton(Image icon, String tooltip) {
        Button button = new Button();
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                       "-fx-padding: 2; -fx-cursor: hand;");
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setOpacity(0.7));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
        
        return button;
    }
    
    private void setupBindings() {
        // Play/pause button
        playPauseButton.setOnAction(e -> audioPlayerService.togglePlayPause());
        previousButton.setOnAction(e -> audioPlayerService.previousTrack());
        nextButton.setOnAction(e -> audioPlayerService.nextTrack());
        
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
            
            // TODO: Load actual album art when available
            // For now, use default album art
            albumArt.setImage(defaultAlbumArt);
        } else {
            titleLabel.setText("No song playing");
            artistLabel.setText("");
            albumArt.setImage(defaultAlbumArt);
        }
        updateTimeLabel();
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
        miniStage.hide();
    }
    
    public boolean isShowing() {
        return miniStage.isShowing();
    }
    
    public Stage getStage() {
        return miniStage;
    }
}