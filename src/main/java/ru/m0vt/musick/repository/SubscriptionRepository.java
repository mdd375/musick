package ru.m0vt.musick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.m0vt.musick.model.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    List<Subscription> findByArtistId(Long artistId);
    boolean existsByUserIdAndArtistId(Long userId, Long artistId);
    Optional<Subscription> findByUserIdAndArtistId(Long userId, Long artistId);
}
