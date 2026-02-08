package com.example.core_service.contact;

import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    Optional<Contact> findByUserAndEmail(User user, String email);
    Optional<Contact> findByUserAndEmailAndDeletedAtIsNull(User user, String email);
    Optional<Contact> findByIdAndUserAndDeletedAtIsNull(Integer id, User user);
}
