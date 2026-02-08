package com.example.core_service.mailinglist;

import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailingListRepository extends JpaRepository<MailingList, Integer> {
    List<MailingList> findAllByUserAndDeletedAtIsNull(User user);
    Optional<MailingList> findByIdAndUserAndDeletedAtIsNull(Integer id, User user);
}
