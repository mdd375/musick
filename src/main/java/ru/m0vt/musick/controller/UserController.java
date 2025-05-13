package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{userId}/purchases")
    public List<Purchase> getUserPurchases(@PathVariable Long userId) {
        return userService.getUserPurchases(userId);
    }

    @GetMapping("/{userId}/reviews")
    public List<Review> getUserReviews(@PathVariable Long userId) {
        return userService.getUserReviews(userId);
    }

    @GetMapping("/{userId}/subscriptions")
    public List<Subscription> getUserSubscriptions(@PathVariable Long userId) {
        return userService.getUserSubscriptions(userId);
    }

    @PostMapping("/{userId}/subscriptions")
    public Object addUserSubscription(
        @PathVariable Long userId,
        @RequestBody Artist artist
    ) {
        return userService.addUserSubscription(userId, artist);
    }
}
