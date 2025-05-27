package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.AddBalanceDTO;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/me/purchases")
    public List<Purchase> getUserPurchases(Authentication authentication) {
        return userService.getUserPurchases(authentication);
    }

    @GetMapping("/me/reviews")
    public List<Review> getUserReviews(Authentication authentication) {
        return userService.getUserReviews(authentication);
    }

    @GetMapping("/me/subscriptions")
    public List<Subscription> getUserSubscriptions(Authentication authentication) {
        return userService.getUserSubscriptions(authentication);
    }

    @PostMapping("/me/subscriptions/{artistId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Object addUserSubscription(
        @PathVariable Long artistId,
        Authentication authentication
    ) {
        return userService.addUserSubscription(artistId, authentication);
    }
    
    /**
     * Эндпоинт для пополнения баланса пользователя
     * 
     * @param addBalanceDTO Сумма для пополнения
     * @param authentication Данные аутентификации
     * @return Обновлённый объект пользователя
     */
    @PostMapping("/me/balance")
    public User addBalance(
        @Valid @RequestBody AddBalanceDTO addBalanceDTO,
        Authentication authentication
    ) {
        return userService.addBalance(addBalanceDTO, authentication);
    }
}
