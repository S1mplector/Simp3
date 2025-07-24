package com.musicplayer.ui.dialogs;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.musicplayer.services.AudioConversionService;
import com.musicplayer.services.AudioConversionService.ConversionSettings;
import com.musicplayer.services.AudioConversionService.TargetFormat;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Dialog for configuring and executing audio file conversion to JavaFX-compatible formats.
 */
public class AudioConversionDialog extends Stage {
    
    private final AudioConversionService conversionService;
    private final List<File> filesToConvert;
    private ConversionSettings settings;
    
    // UI Components
    private RadioButton wavRadio;
    private RadioButton aiffRadio;
    private CheckBox preserveOriginalsCheck;
    private CheckBox autoConvertCheck;
    private TextField conversionDirField;
    private Button browseDirButton;
    private ComboBox<String> sampleRateCombo;
    private ComboBox<String> bitDepthCombo;
    private ComboBox<String> channelsCombo;
    
    private ProgressBar progressBar;
    private Label progressLabel;
    private Button convertButton;
    private Button cancelButton;
    
    public AudioConversionDialog(Window owner, AudioConversionService conversionService, List<File> filesToConvert) {
        this.conversionService = conversionService;
        this.filesToConvert = filesToConvert;
        this.settings = new ConversionSettings();
        
        initOwner(owner);
        setTitle("Audio Conversion Settings");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        initializeUI();
        loadSettings();
    }
    
    private void initializeUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");
        
        // Header
        Label headerLabel = new Label("Audio Conversion Settings");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(
            "Converting to WAV/AIFF enables visualizer and full playback controls.\n" +
            "Files to convert: " + filesToConvert.size()
        );
        descLabel.setStyle("-fx-text-fill: #666666;");
        
        // Create scrollable content area
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(10));
        
        // Format selection
        VBox formatBox = createFormatSelectionBox();
        
        // Options
        VBox optionsBox = createOptionsBox();
        
        // Audio quality settings
        VBox qualityBox = createQualityBox();
        
        contentBox.getChildren().addAll(formatBox, optionsBox, qualityBox);
        
        // Make content scrollable
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Progress section (initially hidden) - positioned after scrollable content
        VBox progressBox = createProgressBox();
        progressBox.setVisible(false);
        progressBox.setManaged(false);
        
        // Buttons - always at the bottom
        HBox buttonBox = createButtonBox(progressBox);
        
        root.getChildren().addAll(
            headerLabel, descLabel, scrollPane, progressBox, buttonBox
        );
        
        Scene scene = new Scene(root, 520, 650);
        setScene(scene);
    }
    
    private VBox createFormatSelectionBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label titleLabel = new Label("Target Format");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        ToggleGroup formatGroup = new ToggleGroup();
        
        wavRadio = new RadioButton("WAV (Recommended)");
        wavRadio.setToggleGroup(formatGroup);
        wavRadio.setSelected(true);
        
        aiffRadio = new RadioButton("AIFF");
        aiffRadio.setToggleGroup(formatGroup);
        
        Label formatInfoLabel = new Label("Both formats provide full JavaFX compatibility with visualizer support.");
        formatInfoLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        
        box.getChildren().addAll(titleLabel, wavRadio, aiffRadio, formatInfoLabel);
        return box;
    }
    
    private VBox createOptionsBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label titleLabel = new Label("Conversion Options");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        preserveOriginalsCheck = new CheckBox("Keep original files");
        preserveOriginalsCheck.setSelected(true);
        
        autoConvertCheck = new CheckBox("Auto-convert new files on import");
        autoConvertCheck.setSelected(false);
        
        // Output directory selection
        Label dirLabel = new Label("Output Directory (leave empty for same as original):");
        
        HBox dirBox = new HBox(10);
        dirBox.setAlignment(Pos.CENTER_LEFT);
        
        conversionDirField = new TextField();
        conversionDirField.setPromptText("Same as original files");
        conversionDirField.setPrefWidth(300);
        
        browseDirButton = new Button("Browse...");
        browseDirButton.setOnAction(e -> browseOutputDirectory());
        
        dirBox.getChildren().addAll(conversionDirField, browseDirButton);
        
        box.getChildren().addAll(titleLabel, preserveOriginalsCheck, autoConvertCheck, dirLabel, dirBox);
        return box;
    }
    
    private VBox createQualityBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label titleLabel = new Label("Audio Quality");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Sample Rate
        grid.add(new Label("Sample Rate:"), 0, 0);
        sampleRateCombo = new ComboBox<>();
        sampleRateCombo.getItems().addAll("44100 Hz (CD Quality)", "48000 Hz (Studio)", "96000 Hz (High-Res)");
        sampleRateCombo.setValue("44100 Hz (CD Quality)");
        grid.add(sampleRateCombo, 1, 0);
        
        // Bit Depth
        grid.add(new Label("Bit Depth:"), 0, 1);
        bitDepthCombo = new ComboBox<>();
        bitDepthCombo.getItems().addAll("16-bit (CD Quality)", "24-bit (Studio)", "32-bit (High-Res)");
        bitDepthCombo.setValue("16-bit (CD Quality)");
        grid.add(bitDepthCombo, 1, 1);
        
        // Channels
        grid.add(new Label("Channels:"), 0, 2);
        channelsCombo = new ComboBox<>();
        channelsCombo.getItems().addAll("Stereo (2 channels)", "Mono (1 channel)");
        channelsCombo.setValue("Stereo (2 channels)");
        grid.add(channelsCombo, 1, 2);
        
        box.getChildren().addAll(titleLabel, grid);
        return box;
    }
    
    private VBox createProgressBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 15;");
        box.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Conversion Progress");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        
        progressLabel = new Label("Ready to convert...");
        progressLabel.setStyle("-fx-text-fill: #666666;");
        
        box.getChildren().addAll(titleLabel, progressBar, progressLabel);
        return box;
    }
    
    private HBox createButtonBox(VBox progressBox) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        
        convertButton = new Button("Start Conversion");
        convertButton.setDefaultButton(true);
        convertButton.setOnAction(e -> startConversion(progressBox));
        
        box.getChildren().addAll(cancelButton, convertButton);
        return box;
    }
    
    private void browseOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Directory");
        
        File selectedDir = chooser.showDialog(this);
        if (selectedDir != null) {
            conversionDirField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    private void loadSettings() {
        ConversionSettings currentSettings = conversionService.getSettings();
        
        // Load format selection
        if (currentSettings.getTargetFormat() == TargetFormat.AIFF) {
            aiffRadio.setSelected(true);
        } else {
            wavRadio.setSelected(true);
        }
        
        // Load options
        preserveOriginalsCheck.setSelected(currentSettings.isPreserveOriginals());
        autoConvertCheck.setSelected(currentSettings.isAutoConvertOnImport());
        
        if (currentSettings.getConversionDirectory() != null) {
            conversionDirField.setText(currentSettings.getConversionDirectory());
        }
        
        // Load quality settings
        sampleRateCombo.setValue(getSampleRateDisplayText(currentSettings.getSampleRate()));
        bitDepthCombo.setValue(getBitDepthDisplayText(currentSettings.getSampleSizeInBits()));
        channelsCombo.setValue(getChannelsDisplayText(currentSettings.getChannels()));
    }
    
    private void saveSettings() {
        // Save format
        settings.setTargetFormat(wavRadio.isSelected() ? TargetFormat.WAV : TargetFormat.AIFF);
        
        // Save options
        settings.setPreserveOriginals(preserveOriginalsCheck.isSelected());
        settings.setAutoConvertOnImport(autoConvertCheck.isSelected());
        
        String conversionDir = conversionDirField.getText().trim();
        settings.setConversionDirectory(conversionDir.isEmpty() ? null : conversionDir);
        
        // Save quality settings
        settings.setSampleRate(parseSampleRate(sampleRateCombo.getValue()));
        settings.setSampleSizeInBits(parseBitDepth(bitDepthCombo.getValue()));
        settings.setChannels(parseChannels(channelsCombo.getValue()));
        
        // Apply settings to service
        conversionService.setSettings(settings);
    }
    
    private void startConversion(VBox progressBox) {
        saveSettings();
        
        // Show progress section
        progressBox.setVisible(true);
        progressBox.setManaged(true);
        
        // Disable convert button
        convertButton.setDisable(true);
        
        // Filter files that can be converted
        List<File> convertibleFiles = filesToConvert.stream()
            .filter(file -> {
                String ext = getFileExtension(file.getName());
                return conversionService.isConvertible(ext) && !conversionService.isJavaFXCompatible(ext);
            })
            .collect(Collectors.toList());
        
        if (convertibleFiles.isEmpty()) {
            showInfo("No Conversion Needed", "All selected files are already in JavaFX-compatible formats.");
            close();
            return;
        }
        
        // Create conversion task
        Task<List<File>> conversionTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                return conversionService.convertFiles(convertibleFiles, new AudioConversionService.ConversionProgressCallback() {
                    @Override
                    public void onProgress(String fileName, int current, int total, double percentage) {
                        Platform.runLater(() -> {
                            progressBar.setProgress(percentage / 100.0);
                            progressLabel.setText(String.format("Converting %s (%d/%d) - %.1f%%", 
                                fileName, current, total, percentage));
                            
                            // Update window title to show progress
                            setTitle(String.format("Audio Conversion - %d/%d (%.0f%%)", current, total, percentage));
                        });
                    }
                    
                    @Override
                    public void onComplete(List<File> convertedFiles, List<String> errors) {
                        Platform.runLater(() -> {
                            progressBar.setProgress(1.0);
                            progressLabel.setText("Conversion completed successfully!");
                            setTitle("Audio Conversion - Complete");
                            
                            // Show detailed completion notification
                            String message;
                            if (!errors.isEmpty()) {
                                message = String.format(
                                    "Conversion completed with some issues:\n\n" +
                                    "✓ Successfully converted: %d files\n" +
                                    "⚠ Failed to convert: %d files\n\n" +
                                    "Target format: %s\n" +
                                    "Check the error details for more information.",
                                    convertedFiles.size(), errors.size(),
                                    settings.getTargetFormat().getExtension().toUpperCase()
                                );
                                showConversionResults(convertedFiles.size(), errors);
                            } else {
                                message = String.format(
                                    "🎉 Conversion completed successfully!\n\n" +
                                    "✓ Converted files: %d\n" +
                                    "📁 Target format: %s\n" +
                                    "🎵 Enhanced features now available:\n" +
                                    "  • Audio visualizer\n" +
                                    "  • Better playback performance\n" +
                                    "  • Enhanced audio controls",
                                    convertedFiles.size(),
                                    settings.getTargetFormat().getExtension().toUpperCase()
                                );
                                showInfo("Conversion Complete", message);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String fileName, Exception error) {
                        // Individual file errors are handled in onComplete
                    }
                }).get(); // Wait for completion
            }
        };
        
        conversionTask.setOnSucceeded(e -> {
            convertButton.setDisable(false);
            cancelButton.setText("Close");
        });
        
        conversionTask.setOnFailed(e -> {
            convertButton.setDisable(false);
            progressLabel.setText("Conversion failed!");
            showError("Conversion Error", "An error occurred during conversion: " + conversionTask.getException().getMessage());
        });
        
        // Run conversion in background
        Thread conversionThread = new Thread(conversionTask);
        conversionThread.setDaemon(true);
        conversionThread.start();
    }
    
    private void showConversionResults(int successCount, List<String> errors) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Successfully converted %d files.\n\n", successCount));
        
        if (!errors.isEmpty()) {
            message.append("Errors encountered:\n");
            for (String error : errors) {
                message.append("• ").append(error).append("\n");
            }
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conversion Results");
        alert.setHeaderText("Conversion Completed with Some Issues");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper methods for parsing UI values
    private float parseSampleRate(String displayText) {
        if (displayText.contains("44100")) return 44100.0f;
        if (displayText.contains("48000")) return 48000.0f;
        if (displayText.contains("96000")) return 96000.0f;
        return 44100.0f; // default
    }
    
    private int parseBitDepth(String displayText) {
        if (displayText.contains("16")) return 16;
        if (displayText.contains("24")) return 24;
        if (displayText.contains("32")) return 32;
        return 16; // default
    }
    
    private int parseChannels(String displayText) {
        if (displayText.contains("Mono")) return 1;
        return 2; // default stereo
    }
    
    private String getSampleRateDisplayText(float sampleRate) {
        if (sampleRate == 48000.0f) return "48000 Hz (Studio)";
        if (sampleRate == 96000.0f) return "96000 Hz (High-Res)";
        return "44100 Hz (CD Quality)";
    }
    
    private String getBitDepthDisplayText(int bitDepth) {
        if (bitDepth == 24) return "24-bit (Studio)";
        if (bitDepth == 32) return "32-bit (High-Res)";
        return "16-bit (CD Quality)";
    }
    
    private String getChannelsDisplayText(int channels) {
        if (channels == 1) return "Mono (1 channel)";
        return "Stereo (2 channels)";
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
}
