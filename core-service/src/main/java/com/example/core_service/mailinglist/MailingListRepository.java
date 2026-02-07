package com.example.core_service.mailinglist;

import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailingListRepository extends JpaRepository<MailingList, Integer> {
    List<MailingList> findAllByUser(User user);
    Optional<MailingList> findByIdAndUser(Integer id, User user);
}
