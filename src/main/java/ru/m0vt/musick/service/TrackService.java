package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.Track;
import ru.m0vt.musick.repository.TrackRepository;

import java.util.List;

@Service
public class TrackService {
    @Autowired
    private TrackRepository trackRepository;

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackById(Long id) {
        return trackRepository.findById(id).orElse(null);
    }

    public void saveTrack(Track track) {
        trackRepository.save(track);
    }

    public void deleteTrack(Long id) {
        trackRepository.deleteById(id);
    }
    public boolean existsById(Long id) {
        return trackRepository.existsById(id);
    }
}
