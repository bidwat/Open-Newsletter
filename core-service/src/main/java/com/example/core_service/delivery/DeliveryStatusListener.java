package com.example.core_service.delivery;

import com.example.common.events.DeliveryStatusEvent;
import com.example.common.kafka.KafkaTopics;
import com.example.core_service.campaign.CampaignRepository;
import com.example.core_service.contact.ContactRepository;
import com.example.core_service.user.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeliveryStatusListener {

    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final CampaignRepository campaignRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public DeliveryStatusListener(DeliveryHistoryRepository deliveryHistoryRepository,
                                  CampaignRepository campaignRepository,
                                  ContactRepository contactRepository,
                                  UserRepository userRepository) {
        this.deliveryHistoryRepository = deliveryHistoryRepository;
        this.campaignRepository = campaignRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = KafkaTopics.DELIVERY_STATUS, groupId = "core-service-group")
    public void handleDeliveryStatus(DeliveryStatusEvent event) {
        if (event == null || event.getCampaignId() == null || event.getUserId() == null || event.getContactId() == null) {
            return;
        }

        Integer campaignId = Integer.parseInt(event.getCampaignId());
        Integer userId = Integer.parseInt(event.getUserId());
        Integer contactId = Integer.parseInt(event.getContactId());

        campaignRepository.findById(campaignId).ifPresent(campaign ->
                userRepository.findById(userId).ifPresent(user ->
                        contactRepository.findById(contactId).ifPresent(contact -> {
                            DeliveryHistory history = new DeliveryHistory();
                            history.setCampaign(campaign);
                            history.setUser(user);
                            history.setContact(contact);
                            history.setStatus(event.getStatus().name());
                            history.setErrorMessage(event.getErrorMessage());
                            deliveryHistoryRepository.save(history);
                        })
                )
        );
    }
}
