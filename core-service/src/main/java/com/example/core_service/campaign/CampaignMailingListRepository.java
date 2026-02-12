package com.example.core_service.campaign;

import com.example.core_service.mailinglist.MailingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignMailingListRepository extends JpaRepository<CampaignMailingList, Integer> {
    List<CampaignMailingList> findAllByCampaign(Campaign campaign);
    void deleteAllByCampaign(Campaign campaign);
    List<CampaignMailingList> findAllByMailingList(MailingList mailingList);
}
