package ru.m0vt.musick.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.m0vt.musick.model.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserId(Long userId);
    Optional<Purchase> findByUserIdAndAlbumId(Long userId, Long albumId);
    Purchase findByUser_IdAndAlbum_Id(Long id, Long id1);
}
