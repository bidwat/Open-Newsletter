package com.example.core_service.user;

import com.example.common.events.UserRegisteredEvent;
import com.example.core_service.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public UserController(UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId().toString(),
                savedUser.getEmail()
        );
        kafkaProducerService.sendUserRegisteredEvent(event);

        return savedUser;
    }


}