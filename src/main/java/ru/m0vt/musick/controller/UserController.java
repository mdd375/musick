package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.UserCreateDTO;
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
    @PreAuthorize("@securityService.isSameUser(authentication, #id) or hasRole('ADMIN')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id) or hasRole('ADMIN')")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id) or hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{userId}/purchases")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId) or hasRole('ADMIN')")
    public List<Purchase> getUserPurchases(@PathVariable Long userId) {
        return userService.getUserPurchases(userId);
    }

    @GetMapping("/{userId}/reviews")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId) or hasRole('ADMIN')")
    public List<Review> getUserReviews(@PathVariable Long userId) {
        return userService.getUserReviews(userId);
    }

    @GetMapping("/{userId}/subscriptions")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId) or hasRole('ADMIN')")
    public List<Subscription> getUserSubscriptions(@PathVariable Long userId) {
        return userService.getUserSubscriptions(userId);
    }

    @PostMapping("/{userId}/subscriptions")
    @PreAuthorize("@securityService.isSameUser(authentication, #userId) or hasRole('ADMIN')")
    public Object addUserSubscription(
        @PathVariable Long userId,
        @RequestBody Artist artist
    ) {
        return userService.addUserSubscription(userId, artist);
    }
}
