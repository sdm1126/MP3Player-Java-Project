package kr.or.mrhi.mp3player;

import java.io.Serializable;

public class MusicData implements Serializable {

    private String id; // File path
    private String albumId; // Album image path
    private String title;
    private String artist;
    private String duration;

    public MusicData(String id, String albumId, String title, String artist, String duration) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
