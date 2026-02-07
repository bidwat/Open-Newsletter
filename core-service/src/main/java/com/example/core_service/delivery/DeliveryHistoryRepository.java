package com.example.core_service.delivery;

import com.example.core_service.campaign.Campaign;
import com.example.core_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Integer> {
    long countByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    Optional<DeliveryHistory> findByCampaignAndContactId(Campaign campaign, Integer contactId);
}
