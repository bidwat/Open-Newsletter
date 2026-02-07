package com.example.core_service.api;

import com.example.core_service.api.dto.AddContactRequest;
import com.example.core_service.api.dto.ContactResponse;
import com.example.core_service.api.dto.CreateMailingListRequest;
import com.example.core_service.api.dto.MailingListResponse;
import com.example.core_service.contact.Contact;
import com.example.core_service.mailinglist.MailingList;
import com.example.core_service.mailinglist.MailingListService;
import com.example.core_service.user.User;
import com.example.core_service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mailing-lists")
public class MailingListController {

    private final MailingListService mailingListService;
    private final UserService userService;

    public MailingListController(MailingListService mailingListService, UserService userService) {
        this.mailingListService = mailingListService;
        this.userService = userService;
    }

    @PostMapping
    public MailingListResponse createList(@AuthenticationPrincipal Jwt jwt,
                                          @RequestBody CreateMailingListRequest request) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.createList(user, request.getName(), request.getDescription());
        return new MailingListResponse(list.getId(), list.getName(), list.getDescription(), list.getCreatedAt());
    }

    @GetMapping
    public List<MailingListResponse> getLists(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.resolveUser(jwt);
        return mailingListService.getLists(user).stream()
                .map(list -> new MailingListResponse(list.getId(), list.getName(), list.getDescription(), list.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{listId}")
    public MailingListResponse getList(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.getList(user, listId);
        return new MailingListResponse(list.getId(), list.getName(), list.getDescription(), list.getCreatedAt());
    }

    @GetMapping("/{listId}/contacts")
    public List<ContactResponse> getContacts(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        return mailingListService.getContacts(user, listId).stream()
                .map(contact -> new ContactResponse(contact.getId(), contact.getEmail(), contact.getFirstName(), contact.getLastName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/{listId}/contacts")
    public ContactResponse addContact(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable Integer listId,
                                      @RequestBody AddContactRequest request) {
        User user = userService.resolveUser(jwt);
        Contact contact = mailingListService.addContactToList(user, listId, request.getEmail(), request.getFirstName(), request.getLastName());
        return new ContactResponse(contact.getId(), contact.getEmail(), contact.getFirstName(), contact.getLastName());
    }
}
