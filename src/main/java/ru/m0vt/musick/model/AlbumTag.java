package ru.m0vt.musick.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "album_tags")
@IdClass(AlbumTag.AlbumTagId.class)
public class AlbumTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    // Composite key class
    public static class AlbumTagId implements Serializable {
        private Long album;
        private Long tag;

        public AlbumTagId() {
        }

        public AlbumTagId(Long album, Long tag) {
            this.album = album;
            this.tag = tag;
        }

        public Long getAlbum() {
            return album;
        }

        public void setAlbum(Long album) {
            this.album = album;
        }

        public Long getTag() {
            return tag;
        }

        public void setTag(Long tag) {
            this.tag = tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlbumTagId that = (AlbumTagId) o;
            return Objects.equals(album, that.album) &&
                    Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(album, tag);
        }
    }
}