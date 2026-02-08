package com.example.core_service.mailinglist;

import com.example.core_service.contact.Contact;
import com.example.core_service.contact.ContactRepository;
import com.example.core_service.imports.ContactImportService;
import com.example.core_service.imports.ImportedContact;
import com.example.core_service.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailingListService {

    private final MailingListRepository mailingListRepository;
    private final ContactRepository contactRepository;
    private final MailingListContactRepository mailingListContactRepository;
    private final ContactImportService contactImportService;

    public MailingListService(MailingListRepository mailingListRepository,
                              ContactRepository contactRepository,
                              MailingListContactRepository mailingListContactRepository,
                              ContactImportService contactImportService) {
        this.mailingListRepository = mailingListRepository;
        this.contactRepository = contactRepository;
        this.mailingListContactRepository = mailingListContactRepository;
        this.contactImportService = contactImportService;
    }

    public MailingList createList(User user, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "List name is required");
        }
        MailingList list = new MailingList();
        list.setUser(user);
        list.setName(name.trim());
        list.setDescription(description);
        return mailingListRepository.save(list);
    }

    public MailingList updateList(User user, Integer listId, String name, String description) {
        MailingList list = getList(user, listId);
        if (name != null && !name.isBlank()) {
            list.setName(name.trim());
        }
        if (description != null) {
            list.setDescription(description);
        }
        return mailingListRepository.save(list);
    }

    public void softDeleteList(User user, Integer listId) {
        MailingList list = getList(user, listId);
        list.setDeletedAt(LocalDateTime.now());
        mailingListRepository.save(list);
    }

    public MailingList copyList(User user, Integer listId) {
        MailingList original = getList(user, listId);
        MailingList copy = new MailingList();
        copy.setUser(user);
        copy.setName("copy " + original.getName());
        copy.setDescription(original.getDescription());
        MailingList savedCopy = mailingListRepository.save(copy);

        List<MailingListContact> contacts = mailingListContactRepository.findAllByMailingList(original);
        contacts.stream()
                .map(MailingListContact::getContact)
                .filter(contact -> contact.getDeletedAt() == null)
                .forEach(contact -> {
                    MailingListContact newLink = new MailingListContact();
                    newLink.setMailingList(savedCopy);
                    newLink.setContact(contact);
                    mailingListContactRepository.save(newLink);
                });
        return savedCopy;
    }

    public List<MailingList> getLists(User user) {
        return mailingListRepository.findAllByUserAndDeletedAtIsNull(user);
    }

    public MailingList getList(User user, Integer listId) {
        return mailingListRepository.findByIdAndUserAndDeletedAtIsNull(listId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found"));
    }

    public List<Contact> getContacts(User user, Integer listId) {
        MailingList list = getList(user, listId);
        return mailingListContactRepository.findAllByMailingList(list)
                .stream()
                .map(MailingListContact::getContact)
                .filter(contact -> contact.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    public Contact addContactToList(User user, Integer listId, String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        MailingList list = getList(user, listId);

        Contact contact = contactRepository.findByUserAndEmail(user, email.trim().toLowerCase())
                .orElseGet(() -> {
                    Contact newContact = new Contact();
                    newContact.setUser(user);
                    newContact.setEmail(email.trim().toLowerCase());
                    newContact.setFirstName(firstName);
                    newContact.setLastName(lastName);
                    return contactRepository.save(newContact);
                });

        if (contact.getDeletedAt() != null) {
            contact.setDeletedAt(null);
            contactRepository.save(contact);
        }

        mailingListContactRepository.findByMailingListAndContact(list, contact)
                .orElseGet(() -> {
                    MailingListContact link = new MailingListContact();
                    link.setMailingList(list);
                    link.setContact(contact);
                    return mailingListContactRepository.save(link);
                });

        return contact;
    }

    public Contact updateContact(User user, Integer listId, Integer contactId, String email, String firstName, String lastName) {
        MailingList list = getList(user, listId);
        Contact contact = contactRepository.findByIdAndUserAndDeletedAtIsNull(contactId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));

        mailingListContactRepository.findByMailingListAndContact(list, contact)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found in list"));

        if (email != null && !email.isBlank()) {
            contact.setEmail(email.trim().toLowerCase());
        }
        if (firstName != null) {
            contact.setFirstName(firstName);
        }
        if (lastName != null) {
            contact.setLastName(lastName);
        }
        return contactRepository.save(contact);
    }

    public void removeContactFromList(User user, Integer listId, Integer contactId) {
        MailingList list = getList(user, listId);
        Contact contact = contactRepository.findByIdAndUserAndDeletedAtIsNull(contactId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        mailingListContactRepository.deleteByMailingListAndContact(list, contact);
    }

    public void softDeleteContact(User user, Integer listId, Integer contactId) {
        MailingList list = getList(user, listId);
        Contact contact = contactRepository.findByIdAndUserAndDeletedAtIsNull(contactId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        mailingListContactRepository.deleteByMailingListAndContact(list, contact);
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);
    }

    public ImportResult importContacts(User user, Integer listId, MultipartFile file) {
        MailingList list = getList(user, listId);
        List<ImportedContact> contacts = contactImportService.parseContacts(file);
        int imported = 0;
        int skipped = 0;

        for (ImportedContact contact : contacts) {
            if (contact.getEmail() == null || contact.getEmail().isBlank()) {
                skipped++;
                continue;
            }
            addContactToList(user, list.getId(), contact.getEmail(), contact.getFirstName(), contact.getLastName());
            imported++;
        }
        return new ImportResult(list.getId(), imported, skipped);
    }

    public ImportResult createListWithImport(User user, String name, String description, MultipartFile file) {
        MailingList list = createList(user, name, description);
        ImportResult result = importContacts(user, list.getId(), file);
        return new ImportResult(result.mailingListId(), result.imported(), result.skipped());
    }

    public record ImportResult(Integer mailingListId, int imported, int skipped) {
    }
}
