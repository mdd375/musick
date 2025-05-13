package ru.m0vt.musick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.m0vt.musick.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
     List<Review> findByAlbumId(Long albumId);
     List<Review> findByUserId(Long userId);
}
