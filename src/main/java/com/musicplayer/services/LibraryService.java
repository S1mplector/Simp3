package com.musicplayer.services;

import java.util.List;

import com.musicplayer.data.models.Song;
import com.musicplayer.data.repositories.SongRepository;

public class LibraryService {
    private final SongRepository songRepository;

    public LibraryService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public void addSong(Song song) {
        songRepository.save(song);
    }

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    public Song getSongById(long id) {
        return songRepository.findById(id);
    }

    public void removeSong(long id) {
        songRepository.delete(id);
    }
}
