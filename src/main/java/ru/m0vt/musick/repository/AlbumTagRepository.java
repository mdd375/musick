package ru.m0vt.musick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.m0vt.musick.model.AlbumTag;
import ru.m0vt.musick.model.AlbumTag.AlbumTagId;

import java.util.List;

@Repository
public interface AlbumTagRepository extends JpaRepository<AlbumTag, AlbumTagId> {
     List<AlbumTag> findByAlbumId(Long albumId);
     List<AlbumTag> findByTagId(Long tagId);
     AlbumTag findByAlbumIdAndTagId(Long albumId, Long tagId);
}