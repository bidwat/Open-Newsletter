package com.example.core_service.campaign;

import com.example.common.events.RecipientPayload;
import com.example.common.events.SendCampaignEvent;
import com.example.core_service.contact.Contact;
import com.example.core_service.contact.ContactRepository;
import com.example.core_service.delivery.DeliveryHistoryRepository;
import com.example.core_service.mailinglist.MailingList;
import com.example.core_service.mailinglist.MailingListContact;
import com.example.core_service.mailinglist.MailingListContactRepository;
import com.example.core_service.mailinglist.MailingListRepository;
import com.example.core_service.service.KafkaProducerService;
import com.example.core_service.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private static final int MONTHLY_QUOTA = 500;

    private final CampaignRepository campaignRepository;
    private final MailingListRepository mailingListRepository;
    private final MailingListContactRepository mailingListContactRepository;
    private final CampaignMailingListRepository campaignMailingListRepository;
    private final CampaignExclusionRepository campaignExclusionRepository;
    private final ContactRepository contactRepository;
    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final KafkaProducerService kafkaProducerService;

    public CampaignService(CampaignRepository campaignRepository,
                           MailingListRepository mailingListRepository,
                           MailingListContactRepository mailingListContactRepository,
                           CampaignMailingListRepository campaignMailingListRepository,
                           CampaignExclusionRepository campaignExclusionRepository,
                           ContactRepository contactRepository,
                           DeliveryHistoryRepository deliveryHistoryRepository,
                           KafkaProducerService kafkaProducerService) {
        this.campaignRepository = campaignRepository;
        this.mailingListRepository = mailingListRepository;
        this.mailingListContactRepository = mailingListContactRepository;
        this.campaignMailingListRepository = campaignMailingListRepository;
        this.campaignExclusionRepository = campaignExclusionRepository;
        this.contactRepository = contactRepository;
        this.deliveryHistoryRepository = deliveryHistoryRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Campaign createDraft(User user, List<Integer> mailingListIds, String name, String subject, String htmlContent, String textContent, List<Integer> excludedContactIds) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name is required");
        }
        List<MailingList> lists = resolveLists(user, mailingListIds);

        Campaign campaign = new Campaign();
        campaign.setUser(user);
        campaign.setName(name.trim());
        campaign.setSubject(subject);
        campaign.setHtmlContent(htmlContent);
        campaign.setTextContent(textContent);
        campaign.setStatus(determineStatus(subject, htmlContent, textContent).name());
        Campaign saved = campaignRepository.save(campaign);

        saveCampaignLists(saved, lists);
        saveExclusions(saved, user, excludedContactIds);
        return campaignRepository.save(saved);
    }

    @Transactional
    public Campaign updateDraft(User user, Integer campaignId, List<Integer> mailingListIds, String name, String subject, String htmlContent, String textContent, List<Integer> excludedContactIds) {
        Campaign campaign = getCampaign(campaignId, user);

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

        if (mailingListIds != null) {
            List<MailingList> lists = resolveLists(user, mailingListIds);
            campaignMailingListRepository.deleteAllByCampaign(campaign);
            saveCampaignLists(campaign, lists);
        }


        campaign.setStatus(determineStatus(campaign.getSubject(), campaign.getHtmlContent(), campaign.getTextContent()).name());
        return campaignRepository.save(campaign);
    }

    public List<Campaign> getCampaigns(User user) {
        return campaignRepository.findAllByUserAndDeletedAtIsNull(user);
    }

    public Campaign getCampaign(Integer campaignId, User user) {
        return campaignRepository.findByIdAndUserAndDeletedAtIsNull(campaignId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    public List<MailingList> getCampaignMailingLists(Campaign campaign) {
        return campaignMailingListRepository.findAllByCampaign(campaign).stream()
                .map(CampaignMailingList::getMailingList)
                .collect(Collectors.toList());
    }

    public List<Integer> getExcludedContactIds(Campaign campaign) {
        return campaignExclusionRepository.findAllByCampaign(campaign).stream()
                .map(CampaignExclusion::getContact)
                .map(Contact::getId)
                .collect(Collectors.toList());
    }

    public void softDeleteCampaign(User user, Integer campaignId) {
        Campaign campaign = getCampaign(campaignId, user);
        campaign.setDeletedAt(LocalDateTime.now());
        campaignRepository.save(campaign);
    }

    public Campaign copyCampaign(User user, Integer campaignId) {
        Campaign original = getCampaign(campaignId, user);

        Campaign copy = new Campaign();
        copy.setUser(user);
        copy.setName("copy " + original.getName());
        copy.setSubject(original.getSubject());
        copy.setHtmlContent(original.getHtmlContent());
        copy.setTextContent(original.getTextContent());
        copy.setStatus(CampaignStatus.DRAFT.name());
        Campaign saved = campaignRepository.save(copy);

        List<CampaignMailingList> lists = campaignMailingListRepository.findAllByCampaign(original);
        lists.forEach(link -> {
            CampaignMailingList newLink = new CampaignMailingList();
            newLink.setCampaign(saved);
            newLink.setMailingList(link.getMailingList());
            campaignMailingListRepository.save(newLink);
        });

        List<CampaignExclusion> exclusions = campaignExclusionRepository.findAllByCampaign(original);
        exclusions.forEach(link -> {
            CampaignExclusion newLink = new CampaignExclusion();
            newLink.setCampaign(saved);
            newLink.setContact(link.getContact());
            campaignExclusionRepository.save(newLink);
        });

        return saved;
    }

    public Campaign sendCampaign(User user, Integer campaignId) {
        Campaign campaign = getCampaign(campaignId, user);

        if (CampaignStatus.SENT.name().equals(campaign.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign already sent");
        }

        CampaignStatus status = determineStatus(campaign.getSubject(), campaign.getHtmlContent(), campaign.getTextContent());
        if (status == CampaignStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign must have subject and content before sending");
        }

        List<CampaignMailingList> listLinks = campaignMailingListRepository.findAllByCampaign(campaign);
        if (listLinks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign has no mailing lists");
        }

        Set<Integer> excludedIds = campaignExclusionRepository.findAllByCampaign(campaign)
                .stream()
                .map(link -> link.getContact().getId())
                .collect(Collectors.toSet());

        Map<Integer, Contact> uniqueContacts = new LinkedHashMap<>();
        for (CampaignMailingList link : listLinks) {
            List<MailingListContact> listContacts = mailingListContactRepository.findAllByMailingList(link.getMailingList());
            for (MailingListContact listContact : listContacts) {
                Contact contact = listContact.getContact();
                if (contact.getDeletedAt() != null) {
                    continue;
                }
                if (excludedIds.contains(contact.getId())) {
                    continue;
                }
                uniqueContacts.putIfAbsent(contact.getId(), contact);
            }
        }

        if (uniqueContacts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign has no eligible contacts");
        }

        long alreadySent = countMonthlySends(user);
        if (alreadySent + uniqueContacts.size() > MONTHLY_QUOTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monthly quota exceeded");
        }

        List<RecipientPayload> recipients = uniqueContacts.values().stream()
                .map(contact -> new RecipientPayload(
                        contact.getId().toString(),
                        contact.getEmail(),
                        contact.getFirstName(),
                        contact.getLastName()
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

    private List<MailingList> resolveLists(User user, List<Integer> mailingListIds) {
        if (mailingListIds == null || mailingListIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mailing list is required");
        }
        List<MailingList> lists = mailingListIds.stream()
                .map(id -> mailingListRepository.findByIdAndUserAndDeletedAtIsNull(id, user)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found")))
                .collect(Collectors.toList());
        if (lists.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mailing list is required");
        }
        return lists;
    }

    private void saveCampaignLists(Campaign campaign, List<MailingList> lists) {
        lists.forEach(list -> {
            CampaignMailingList link = new CampaignMailingList();
            link.setCampaign(campaign);
            link.setMailingList(list);
            campaignMailingListRepository.save(link);
        });
    }

    private void saveExclusions(Campaign campaign, User user, List<Integer> excludedContactIds) {
        if (excludedContactIds == null) {
            return;
        }
        excludedContactIds.forEach(contactId -> {
            Contact contact = contactRepository.findByIdAndUserAndDeletedAtIsNull(contactId, user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excluded contact not found"));
            CampaignExclusion exclusion = new CampaignExclusion();
            exclusion.setCampaign(campaign);
            exclusion.setContact(contact);
            campaignExclusionRepository.save(exclusion);
        });
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
