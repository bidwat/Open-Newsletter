package com.example.core_service.mailinglist;

import com.example.core_service.contact.Contact;
import com.example.core_service.contact.ContactRepository;
import com.example.core_service.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailingListService {

    private final MailingListRepository mailingListRepository;
    private final ContactRepository contactRepository;
    private final MailingListContactRepository mailingListContactRepository;

    public MailingListService(MailingListRepository mailingListRepository,
                              ContactRepository contactRepository,
                              MailingListContactRepository mailingListContactRepository) {
        this.mailingListRepository = mailingListRepository;
        this.contactRepository = contactRepository;
        this.mailingListContactRepository = mailingListContactRepository;
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

    public List<MailingList> getLists(User user) {
        return mailingListRepository.findAllByUser(user);
    }

    public MailingList getList(User user, Integer listId) {
        return mailingListRepository.findByIdAndUser(listId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found"));
    }

    public List<Contact> getContacts(User user, Integer listId) {
        MailingList list = mailingListRepository.findByIdAndUser(listId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found"));
        return mailingListContactRepository.findAllByMailingList(list)
                .stream()
                .map(MailingListContact::getContact)
                .collect(Collectors.toList());
    }

    public Contact addContactToList(User user, Integer listId, String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        MailingList list = mailingListRepository.findByIdAndUser(listId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mailing list not found"));

        Contact contact = contactRepository.findByUserAndEmail(user, email.trim().toLowerCase())
                .orElseGet(() -> {
                    Contact newContact = new Contact();
                    newContact.setUser(user);
                    newContact.setEmail(email.trim().toLowerCase());
                    newContact.setFirstName(firstName);
                    newContact.setLastName(lastName);
                    return contactRepository.save(newContact);
                });

        mailingListContactRepository.findByMailingListAndContact(list, contact)
                .orElseGet(() -> {
                    MailingListContact link = new MailingListContact();
                    link.setMailingList(list);
                    link.setContact(contact);
                    return mailingListContactRepository.save(link);
                });

        return contact;
    }
}
