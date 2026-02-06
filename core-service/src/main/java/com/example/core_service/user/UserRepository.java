package com.example.core_service.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAuth0Id(String auth0Id);
    Optional<User> findUserByAuth0Id(String auth0Id);
    Optional<User> findUserByEmail(String email);
}