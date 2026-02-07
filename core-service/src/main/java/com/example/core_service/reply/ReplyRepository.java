package com.example.core_service.reply;

import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findAllByUser(User user);
}
