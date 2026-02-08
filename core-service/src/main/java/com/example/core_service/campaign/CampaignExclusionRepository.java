package com.example.core_service.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignExclusionRepository extends JpaRepository<CampaignExclusion, Integer> {
    List<CampaignExclusion> findAllByCampaign(Campaign campaign);
    void deleteAllByCampaign(Campaign campaign);
}
