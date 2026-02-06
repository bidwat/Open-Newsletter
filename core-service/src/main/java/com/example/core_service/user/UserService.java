package com.example.core_service.user;

import com.example.common.events.UserRegisteredEvent;
import com.example.core_service.service.KafkaProducerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public UserService(UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public User syncUser(String auth0Id, String email) {
        return userRepository.findUserByAuth0Id(auth0Id)
                .orElseGet(() -> registerNewUser(auth0Id, email));
    }

    List<User> findAll() {
        return userRepository.findAll();
    }

    User registerNewUser(String auth0Id, String email) {
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
