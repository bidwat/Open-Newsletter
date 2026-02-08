package com.example.core_service.api;

import com.example.core_service.api.dto.CampaignResponse;
import com.example.core_service.api.dto.CreateCampaignRequest;
import com.example.core_service.api.dto.MailingListSummaryResponse;
import com.example.core_service.api.dto.UpdateCampaignRequest;
import com.example.core_service.campaign.Campaign;
import com.example.core_service.campaign.CampaignService;
import com.example.core_service.user.User;
import com.example.core_service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final UserService userService;

    public CampaignController(CampaignService campaignService,
                              UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    @PostMapping
    public CampaignResponse createDraft(@AuthenticationPrincipal Jwt jwt,
                                        @RequestBody CreateCampaignRequest request) {
        User user = userService.resolveUser(jwt);
        List<Integer> listIds = request.getMailingListIds();
        if ((listIds == null || listIds.isEmpty()) && request.getMailingListId() != null) {
            listIds = List.of(request.getMailingListId());
        }
        Campaign campaign = campaignService.createDraft(
                user,
                listIds,
                request.getName(),
                request.getSubject(),
                request.getHtmlContent(),
                request.getTextContent(),
                request.getExcludedContactIds()
        );
        return toResponse(campaign);
    }

    @PutMapping("/{campaignId}")
    public CampaignResponse updateDraft(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable Integer campaignId,
                                        @RequestBody UpdateCampaignRequest request) {
        User user = userService.resolveUser(jwt);
        Campaign campaign = campaignService.updateDraft(
                user,
                campaignId,
                request.getMailingListIds(),
                request.getName(),
                request.getSubject(),
                request.getHtmlContent(),
                request.getTextContent(),
                request.getExcludedContactIds()
        );
        return toResponse(campaign);
    }

    @GetMapping
    public List<CampaignResponse> getCampaigns(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.resolveUser(jwt);
        return campaignService.getCampaigns(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{campaignId}")
    public CampaignResponse getCampaign(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer campaignId) {
        User user = userService.resolveUser(jwt);
        return toResponse(campaignService.getCampaign(campaignId, user));
    }

    @PostMapping("/{campaignId}/send")
    public CampaignResponse sendCampaign(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer campaignId) {
        User user = userService.resolveUser(jwt);
        Campaign campaign = campaignService.sendCampaign(user, campaignId);
        return toResponse(campaign);
    }

    @PostMapping("/{campaignId}/copy")
    public CampaignResponse copyCampaign(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer campaignId) {
        User user = userService.resolveUser(jwt);
        return toResponse(campaignService.copyCampaign(user, campaignId));
    }

    @DeleteMapping("/{campaignId}")
    public void deleteCampaign(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer campaignId) {
        User user = userService.resolveUser(jwt);
        campaignService.softDeleteCampaign(user, campaignId);
    }

    private CampaignResponse toResponse(Campaign campaign) {
        List<MailingListSummaryResponse> mailingLists = campaignService.getCampaignMailingLists(campaign).stream()
                .map(list -> new MailingListSummaryResponse(list.getId(), list.getName()))
                .collect(Collectors.toList());

        List<Integer> excludedContactIds = campaignService.getExcludedContactIds(campaign);

        return new CampaignResponse(
                campaign.getId(),
                mailingLists,
                campaign.getName(),
                campaign.getSubject(),
                campaign.getHtmlContent(),
                campaign.getTextContent(),
                campaign.getStatus(),
                campaign.getCreatedAt(),
                campaign.getSentAt(),
                excludedContactIds
        );
    }
}
