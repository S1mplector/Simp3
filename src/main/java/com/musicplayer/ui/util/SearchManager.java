package com.musicplayer.ui.util;

import com.musicplayer.data.models.Playlist;
import com.musicplayer.data.models.Song;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Utility class to encapsulate search filtering logic for songs and playlists.
 */
public final class SearchManager {

    private SearchManager() {}

    public static void bindSongSearch(TextField searchField,
                                      FilteredList<Song> filteredSongs,
                                      ListView<String> libraryView) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
            filteredSongs.setPredicate(song -> {
                if (song == null) return false;

                // Only filter when viewing "All Songs"
                boolean inAllSongs = "All Songs".equals(libraryView.getSelectionModel().getSelectedItem());
                if (!inAllSongs) return true;

                if (filter.isEmpty()) return true;
                return song.getTitle().toLowerCase().contains(filter) ||
                       song.getArtist().toLowerCase().contains(filter) ||
                       song.getAlbum().toLowerCase().contains(filter);
            });
        });
    }

    public static void bindPlaylistSearch(TextField searchField,
                                          FilteredList<Playlist> filteredPlaylists) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
            filteredPlaylists.setPredicate(pl -> filter.isEmpty() || pl.getName().toLowerCase().contains(filter));
        });
    }
} 