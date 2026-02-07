package com.example.core_service.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.registerNewUser(user.getAuth0Id(), user.getEmail());
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@AuthenticationPrincipal Jwt jwt) {
        String auth0Id = jwt.getSubject();
        User user = userService.getOrCreateUserByAccessToken(auth0Id, jwt.getTokenValue());
        return ResponseEntity.ok(user);
    }


}