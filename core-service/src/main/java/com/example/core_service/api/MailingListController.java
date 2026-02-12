package com.example.core_service.api;

import com.example.core_service.api.dto.*;
import com.example.core_service.contact.Contact;
import com.example.core_service.mailinglist.MailingList;
import com.example.core_service.mailinglist.MailingListService;
import com.example.core_service.user.User;
import com.example.core_service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return toResponse(list);
    }

    @PutMapping("/{listId}")
    public MailingListResponse updateList(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable Integer listId,
                                          @RequestBody UpdateMailingListRequest request) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.updateList(user, listId, request.getName(), request.getDescription());
        return toResponse(list);
    }

    @PostMapping("/{listId}/hidden")
    public MailingListResponse toggleHidden(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable Integer listId,
                                            @RequestBody ToggleHiddenRequest request) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.toggleHidden(user, listId, request.isHidden());
        return toResponse(list);
    }

    @DeleteMapping("/{listId}")
    public void deleteList(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        mailingListService.softDeleteList(user, listId);
    }

    @PostMapping("/{listId}/copy")
    public MailingListResponse copyList(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        MailingList copy = mailingListService.copyList(user, listId);
        return toResponse(copy);
    }

    @GetMapping
    public List<MailingListResponse> getLists(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.resolveUser(jwt);
        return mailingListService.getLists(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{listId}")
    public MailingListResponse getList(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.getList(user, listId);
        return toResponse(list);
    }

    @GetMapping("/{listId}/with-contacts")
    public MailingListWithContactsResponse getListWithContacts(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer listId) {
        User user = userService.resolveUser(jwt);
        MailingList list = mailingListService.getList(user, listId);
        List<ContactResponse> contacts = mailingListService.getContacts(user, listId).stream()
                .map(contact -> new ContactResponse(contact.getId(), contact.getEmail(), contact.getFirstName(), contact.getLastName()))
                .collect(Collectors.toList());
        return new MailingListWithContactsResponse(list.getId(), list.getName(), list.getDescription(), list.isHidden(), list.getCreatedAt(), contacts);
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

    @PutMapping("/{listId}/contacts/{contactId}")
    public ContactResponse updateContact(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Integer listId,
                                         @PathVariable Integer contactId,
                                         @RequestBody UpdateContactRequest request) {
        User user = userService.resolveUser(jwt);
        Contact contact = mailingListService.updateContact(user, listId, contactId, request.getEmail(), request.getFirstName(), request.getLastName());
        return new ContactResponse(contact.getId(), contact.getEmail(), contact.getFirstName(), contact.getLastName());
    }

    @DeleteMapping("/{listId}/contacts/{contactId}")
    public void removeContact(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable Integer listId,
                              @PathVariable Integer contactId) {
        User user = userService.resolveUser(jwt);
        mailingListService.softDeleteContact(user, listId, contactId);
    }

    @PostMapping("/import")
    public ImportContactsResponse importToNewList(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam("name") String name,
                                                  @RequestParam(value = "description", required = false) String description,
                                                  @RequestParam("file") MultipartFile file) {
        User user = userService.resolveUser(jwt);
        MailingListService.ImportResult result = mailingListService.createListWithImport(user, name, description, file);
        return new ImportContactsResponse(result.mailingListId(), result.imported(), result.skipped());
    }

    @PostMapping("/{listId}/import")
    public ImportContactsResponse importToExistingList(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable Integer listId,
                                                       @RequestParam("file") MultipartFile file) {
        User user = userService.resolveUser(jwt);
        MailingListService.ImportResult result = mailingListService.importContacts(user, listId, file);
        return new ImportContactsResponse(result.mailingListId(), result.imported(), result.skipped());
    }

    private MailingListResponse toResponse(MailingList list) {
        return new MailingListResponse(list.getId(), list.getName(), list.getDescription(), list.isHidden(), list.getCreatedAt());
    }
}
