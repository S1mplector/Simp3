package com.musicplayer.ui.dialogs;

import java.util.Optional;

import com.musicplayer.data.models.Playlist;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Simple custom JavaFX modal for selecting a playlist.
 */
public final class PlaylistSelectionPopup {

    private PlaylistSelectionPopup() {}

    /**
     * Shows the modal and returns the selected playlist (or empty if cancelled).
     *
     * @param owner         window owner for modality
     * @param playlists     list of playlists to choose from
     * @return Optional with chosen playlist
     */
    public static Optional<Playlist> show(Window owner, ObservableList<Playlist> playlists) {
        Stage stage = new Stage();
        stage.setTitle("Add to Playlist");
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label header = new Label("Select a playlist:");

        FilteredList<Playlist> filtered = new FilteredList<>(playlists, p -> true);

        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV == null ? "" : newV.toLowerCase().trim();
            filtered.setPredicate(pl -> filter.isEmpty() || pl.getName().toLowerCase().contains(filter));
        });

        ListView<Playlist> listView = new ListView<>(filtered);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Buttons
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        ok.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> ok.setDisable(newSel == null));

        buttonBar.getChildren().addAll(ok, cancel);
        root.getChildren().addAll(header, searchField, listView, buttonBar);

        Scene scene = new Scene(root, 300, 400);
        stage.setScene(scene);

        final Playlist[] selected = new Playlist[1];

        ok.setOnAction(e -> {
            selected[0] = listView.getSelectionModel().getSelectedItem();
            stage.close();
        });
        cancel.setOnAction(e -> stage.close());

        stage.showAndWait();
        return Optional.ofNullable(selected[0]);
    }
} 