package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.Tag;
import ru.m0vt.musick.repository.TagRepository;

import java.util.List;

@Service
public class TagService {
    private TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTagById(Long id) {
        return tagRepository.findById(id).orElse(null);
    }

    public void saveTag(Tag tag) {
        tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return tagRepository.existsById(id);
    }
}
