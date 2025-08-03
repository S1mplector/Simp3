package com.musicplayer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class that wraps basic operations around the external "yt-dlp" binary.
 * <p>
 * Responsibilities:
 * 1. Detect if the binary is present on the host system
 * 2. Download a single YouTube video as an audio-only file (default: MP3)
 * 3. Expose progress via simple listener callbacks (percentage updates only)
 *
 * This class purposefully keeps a very small surface-area. Anything related to
 * UI/album selection/library-integration is handled elsewhere so we keep the
 * download logic independent and easily testable.
 */
public class YouTubeDownloadService {

    /** Convenience overload that uses default format (mp3) and quality */
    public void downloadAudio(String url, File outputDirectory, DownloadListener listener) {
        downloadAudio(url, outputDirectory, "mp3", null, listener);
    }

    /** Callback interface for long-running download operations. */
    public interface DownloadListener {
        /**
         * @param percentage progress in the range 0-100. May be -1 if unknown.
         * @param message    arbitrary human-readable progress message
         */
        void onProgress(int percentage, String message);

        /** Called when the download finishes successfully. */
        void onSuccess(File downloadedFile);

        /** Called if the download fails for any reason. */
        void onError(String errorMessage, Exception exception);
    }

    /** Quick check by executing `yt-dlp --version`. */
    public boolean isYtDlpInstalled() {
        try {
            Process process = new ProcessBuilder("yt-dlp", "--version")
                    .redirectErrorStream(true)
                    .start();
            int exit = process.waitFor();
            return exit == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Downloads the best audio stream as MP3 to the given directory.
     * <p>
     * The command executed resembles:
     * yt-dlp -f bestaudio --extract-audio --audio-format mp3 -o "%(title)s.%(ext)s" <url>
     *
     * @param url              the YouTube (or supported site) url
     * @param outputDirectory  directory to place the resulting file
     * @param listener         optional progress callback, may be null
     */
    public void downloadAudio(String url, File outputDirectory, String audioFormat, String audioQuality, DownloadListener listener) {
        if (url == null || url.isBlank()) {
            if (listener != null) listener.onError("URL is empty", null);
            return;
        }
        if (outputDirectory == null) {
            if (listener != null) listener.onError("Output directory is null", null);
            return;
        }

        /* Build argument list */
        List<String> cmd = new ArrayList<>();
        cmd.add("yt-dlp");
        cmd.add("-f");
        cmd.add("bestaudio");
        cmd.add("--extract-audio");
        cmd.add("--audio-format");
        cmd.add(audioFormat != null && !audioFormat.isBlank() ? audioFormat : "mp3");
        if (audioQuality != null && !audioQuality.isBlank()) {
            cmd.add("--audio-quality");
            cmd.add(audioQuality);
        }
        cmd.add("-o");
        cmd.add("%(title)s.%(ext)s");
        cmd.add(url);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(outputDirectory);
        pb.redirectErrorStream(true);

        Thread worker = new Thread(() -> {
            try {
                Process proc = pb.start();
                /* Parse stdout line-by-line looking for progress information of the form:
                   [download]   5.3% of ... at  ... ETA ...
                */
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (listener != null) listener.onProgress(parsePercentage(line), line);
                    }
                }
                int exit = proc.waitFor();
                if (exit == 0) {
                    // Attempt to locate the downloaded file – simplest way is to list most recently modified mp3 in output dir
                    String ext = (audioFormat != null && !audioFormat.isBlank()) ? audioFormat : "mp3";
                    File downloaded = getMostRecentFile(outputDirectory, ext);
                    if (listener != null) listener.onSuccess(downloaded);
                } else {
                    if (listener != null) listener.onError("yt-dlp exited with code " + exit, null);
                }
            } catch (Exception ex) {
                if (listener != null) listener.onError("Error during download", ex);
            }
        }, "yt-dlp-download-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private int parsePercentage(String line) {
        try {
            int idxStart = line.indexOf('%');
            if (idxStart > 0) {
                // backtrack to first space then parse substring between space and %
                int space = line.lastIndexOf(' ', idxStart);
                if (space >= 0) {
                    String percentStr = line.substring(space, idxStart).trim();
                    if (percentStr.endsWith("%")) percentStr = percentStr.substring(0, percentStr.length() - 1);
                    return (int) Float.parseFloat(percentStr);
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private File getMostRecentFile(File dir, String extension) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith("." + extension));
        if (files == null || files.length == 0) return null;
        File newest = files[0];
        for (File f : files) {
            if (f.lastModified() > newest.lastModified()) {
                newest = f;
            }
        }
        return newest;
    }
}
