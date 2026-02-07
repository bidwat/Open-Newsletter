package com.example.core_service.campaign;

import com.example.common.events.RecipientPayload;
import com.example.common.events.SendCampaignEvent;
import com.example.core_service.delivery.DeliveryHistoryRepository;
import com.example.core_service.mailinglist.MailingList;
import com.example.core_service.mailinglist.MailingListContact;
import com.example.core_service.mailinglist.MailingListContactRepository;
import com.example.core_service.mailinglist.MailingListRepository;
import com.example.core_service.service.KafkaProducerService;
import com.example.core_service.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private static final int MONTHLY_QUOTA = 500;

    private final CampaignRepository campaignRepository;
    private final MailingListRepository mailingListRepository;
    private final MailingListContactRepository mailingListContactRepository;
    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final KafkaProducerService kafkaProducerService;

    public CampaignService(CampaignRepository campaignRepository,
                           MailingListRepository mailingListRepository,
                           MailingListContactRepository mailingListContactRepository,
                           DeliveryHistoryRepository deliveryHistoryRepository,
                           KafkaProducerService kafkaProducerService) {
        this.campaignRepository = campaignRepository;
        this.mailingListRepository = mailingListRepository;
        this.mailingListContactRepository = mailingListContactRepository;
        this.deliveryHistoryRepository = deliveryHistoryRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Campaign createDraft(User user, Integer mailingListId, String name, String subject, String htmlContent, String textContent) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name is required");
        }
        MailingList mailingList = mailingListRepository.findByIdAndUser(mailingListId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found"));

        Campaign campaign = new Campaign();
        campaign.setUser(user);
        campaign.setMailingList(mailingList);
        campaign.setName(name.trim());
        campaign.setSubject(subject);
        campaign.setHtmlContent(htmlContent);
        campaign.setTextContent(textContent);
        campaign.setStatus(determineStatus(subject, htmlContent, textContent).name());
        return campaignRepository.save(campaign);
    }

    public Campaign updateDraft(User user, Integer campaignId, String name, String subject, String htmlContent, String textContent) {
        Campaign campaign = campaignRepository.findByIdAndUser(campaignId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        if (CampaignStatus.SENT.name().equals(campaign.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sent campaigns cannot be edited");
        }

        if (name != null) {
            campaign.setName(name.trim());
        }
        if (subject != null) {
            campaign.setSubject(subject);
        }
        if (htmlContent != null) {
            campaign.setHtmlContent(htmlContent);
        }
        if (textContent != null) {
            campaign.setTextContent(textContent);
        }

        campaign.setStatus(determineStatus(campaign.getSubject(), campaign.getHtmlContent(), campaign.getTextContent()).name());
        return campaignRepository.save(campaign);
    }

    public List<Campaign> getCampaigns(User user) {
        return campaignRepository.findAllByUser(user);
    }

    public Campaign getCampaign(Integer campaignId, User user) {
        return campaignRepository.findByIdAndUser(campaignId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    public Campaign sendCampaign(User user, Integer campaignId) {
        Campaign campaign = campaignRepository.findByIdAndUser(campaignId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        if (CampaignStatus.SENT.name().equals(campaign.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign already sent");
        }

        CampaignStatus status = determineStatus(campaign.getSubject(), campaign.getHtmlContent(), campaign.getTextContent());
        if (status == CampaignStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign must have subject and content before sending");
        }

        List<MailingListContact> listContacts = mailingListContactRepository.findAllByMailingList(campaign.getMailingList());
        if (listContacts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mailing list has no contacts");
        }

        long alreadySent = countMonthlySends(user);
        if (alreadySent + listContacts.size() > MONTHLY_QUOTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monthly quota exceeded");
        }

        List<RecipientPayload> recipients = listContacts.stream()
                .map(link -> new RecipientPayload(
                        link.getContact().getId().toString(),
                        link.getContact().getEmail(),
                        link.getContact().getFirstName(),
                        link.getContact().getLastName()
                ))
                .collect(Collectors.toList());

        SendCampaignEvent event = new SendCampaignEvent(
                campaign.getId().toString(),
                user.getId().toString(),
                campaign.getSubject(),
                campaign.getHtmlContent(),
                campaign.getTextContent(),
                recipients
        );
        kafkaProducerService.sendCampaignEvent(event);

        campaign.setStatus(CampaignStatus.SENT.name());
        campaign.setSentAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    private CampaignStatus determineStatus(String subject, String htmlContent, String textContent) {
        boolean hasSubject = subject != null && !subject.isBlank();
        boolean hasBody = (htmlContent != null && !htmlContent.isBlank()) || (textContent != null && !textContent.isBlank());
        return hasSubject && hasBody ? CampaignStatus.READY : CampaignStatus.DRAFT;
    }

    private long countMonthlySends(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
        return deliveryHistoryRepository.countByUserAndCreatedAtBetween(user, start, end);
    }
}
