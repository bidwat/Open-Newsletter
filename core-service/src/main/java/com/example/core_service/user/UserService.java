package com.example.core_service.user;

import com.example.common.util.NormalizationUtils;
import com.example.common.events.UserRegisteredEvent;
import com.example.core_service.auth0.Auth0UserInfoService;
import com.example.core_service.service.KafkaProducerService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    private final Auth0UserInfoService auth0UserInfoService;

    public UserService(UserRepository userRepository, KafkaProducerService kafkaProducerService, Auth0UserInfoService auth0UserInfoService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.auth0UserInfoService = auth0UserInfoService;
    }

    @Transactional
    public User syncUser(String auth0Id, String email) {
        return resolveOrCreateUser(auth0Id, email);
    }

    public User getOrCreateUser(String auth0Id, String email) {
        return syncUser(auth0Id, email);
    }

    public User getOrCreateUserByAccessToken(String auth0Id, String accessToken) {
        return userRepository.findUserByAuth0Id(auth0Id)
                .orElseGet(() -> {
                    String email = auth0UserInfoService.fetchUserInfo(accessToken).email();
                    if (email == null || email.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No email available for this Auth0 user");
                    }
                    try {
                        return resolveOrCreateUser(auth0Id, email);
                    } catch (DataIntegrityViolationException ex) {
                        return userRepository.findUserByAuth0Id(auth0Id)
                                .or(() -> userRepository.findUserByEmail(email))
                                .orElseThrow(() -> ex);
                    }
                });
    }

    public User resolveUser(Jwt jwt) {
        return getOrCreateUserByAccessToken(jwt.getSubject(), jwt.getTokenValue());
    }


    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User registerNewUser(String auth0Id, String email) {
        User newUser = new User();
        newUser.setAuth0Id(auth0Id);
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsername(email));

        User savedUser = userRepository.save(newUser);


        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId().toString(),
                savedUser.getEmail()
        );
        kafkaProducerService.sendUserRegisteredEvent(event);

        return savedUser;
    }

    @Transactional
    protected User resolveOrCreateUser(String auth0Id, String email) {
        return userRepository.findUserByAuth0Id(auth0Id)
                .orElseGet(() -> userRepository.findUserByEmail(email)
                        .map(existingUser -> {
                            if (!auth0Id.equals(existingUser.getAuth0Id())) {
                                existingUser.setAuth0Id(auth0Id);
                            }
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> registerNewUser(auth0Id, email)));
    }

    @Transactional
    public User updateProfile(Integer userId, String name, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String trimmedName = name == null ? null : name.trim();
        if (trimmedName == null || trimmedName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        String normalizedUsername = normalizeUsername(username);

        userRepository.findUserByUsername(normalizedUsername)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
                });

        user.setName(trimmedName);
        user.setUsername(normalizedUsername);
        return userRepository.save(user);
    }

    private String generateUniqueUsername(String email) {
        String candidateBase = normalizeUsername(email == null ? "" : email.split("@")[0]);
        String candidate = candidateBase;
        int suffix = 1;

        while (userRepository.existsByUsername(candidate)) {
            candidate = candidateBase + "_" + suffix;
            suffix++;
        }

        return candidate;
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        String normalized = NormalizationUtils.normalizeString(username);
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        return normalized;
    }
}
