package com.example.core_service.campaign;

import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
    List<Campaign> findAllByUserAndDeletedAtIsNull(User user);
    Optional<Campaign> findByIdAndUserAndDeletedAtIsNull(Integer id, User user);
}
