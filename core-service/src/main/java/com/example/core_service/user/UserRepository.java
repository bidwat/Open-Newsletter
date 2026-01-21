package com.example.core_service.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query method to find by Auth0 ID (we'll need this later)
    boolean existsByAuth0Id(String auth0Id);
}