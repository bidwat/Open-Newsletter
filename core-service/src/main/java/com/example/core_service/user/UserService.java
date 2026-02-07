package com.example.core_service.user;

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
        return userRepository.findUserByAuth0Id(auth0Id)
                .orElseGet(() -> registerNewUser(auth0Id, email));
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
                        return registerNewUser(auth0Id, email);
                    } catch (DataIntegrityViolationException ex) {
                        return userRepository.findUserByAuth0Id(auth0Id).orElseThrow(() -> ex);
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

        User savedUser = userRepository.save(newUser);


        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId().toString(),
                savedUser.getEmail()
        );
        kafkaProducerService.sendUserRegisteredEvent(event);

        return savedUser;
    }
}
