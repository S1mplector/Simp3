package com.musicplayer.ui.components;

import java.util.List;
import java.util.function.Consumer;

import com.musicplayer.data.models.Album;
import com.musicplayer.data.repositories.AlbumRepository;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

/**
 * Scrollable grid of AlbumCard components.
 */
public class AlbumGridView extends ScrollPane {

    private final FlowPane flow;
    private AlbumCard selectedCard;
    private final Consumer<Album> selectionCallback;
    private final AlbumRepository albumRepository;

    public AlbumGridView(List<Album> albums, Consumer<Album> onSelect, AlbumRepository albumRepository) {
        this.selectionCallback = onSelect;
        this.albumRepository = albumRepository;

        flow = new FlowPane();
        flow.setPadding(new Insets(10));
        flow.setHgap(10);
        flow.setVgap(10);

        setFitToWidth(true);
        setContent(flow);
        
        // Remove focus border/outline
        setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-color: transparent;");
        
        refresh(albums);
    }

    public void refresh(List<Album> albums) {
        flow.getChildren().clear();
        for (Album album : albums) {
            AlbumCard card = new AlbumCard(album, albumRepository);
            card.setOnMouseClicked(e -> selectCard(card));
            flow.getChildren().add(card);
        }
    }

    private void selectCard(AlbumCard card) {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }
        selectedCard = card;
        card.setSelected(true);

        if (selectionCallback != null) {
            selectionCallback.accept(card.getAlbum());
        }
    }
} 