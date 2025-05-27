package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.UserService;

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

    @GetMapping("/{userId}/purchases")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId)")
    public List<Purchase> getUserPurchases(@PathVariable Long userId) {
        return userService.getUserPurchases(userId);
    }

    @GetMapping("/{userId}/reviews")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId)")
    public List<Review> getUserReviews(@PathVariable Long userId) {
        return userService.getUserReviews(userId);
    }

    @GetMapping("/{userId}/subscriptions")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId)")
    public List<Subscription> getUserSubscriptions(@PathVariable Long userId) {
        return userService.getUserSubscriptions(userId);
    }

    @PostMapping("/{userId}/subscriptions")
    @PreAuthorize(
        "@securityService.isSameUser(authentication, #userId) and hasRole('USER') or hasRole('ADMIN')"
    )
    public Object addUserSubscription(
        @PathVariable Long userId,
        @RequestBody Artist artist
    ) {
        return userService.addUserSubscription(userId, artist);
    }
}
