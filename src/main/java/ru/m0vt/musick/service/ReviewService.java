package ru.m0vt.musick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public List<Review> getReviewsByAlbumId(Long albumId) {
        return reviewRepository.findByAlbumId(albumId);
    }

    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }
}
