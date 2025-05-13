package ru.m0vt.musick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.m0vt.musick.model.Purchase;
import ru.m0vt.musick.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    boolean existsById(Long id);
}
