package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.AlbumTag;
import ru.m0vt.musick.repository.AlbumTagRepository;

import java.util.List;

@Service
public class AlbumTagService {
     @Autowired
     private AlbumTagRepository albumTagRepository;

     public List<AlbumTag> getTagsByAlbumId(Long albumId) {
         return albumTagRepository.findByAlbumId(albumId);
     }

     public List<AlbumTag> getTagsByTagId(Long tagId) {
         return albumTagRepository.findByTagId(tagId);
     }
}
