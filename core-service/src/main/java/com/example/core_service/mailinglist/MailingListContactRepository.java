package com.example.core_service.mailinglist;

import com.example.core_service.contact.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailingListContactRepository extends JpaRepository<MailingListContact, Integer> {
    List<MailingListContact> findAllByMailingList(MailingList mailingList);
    Optional<MailingListContact> findByMailingListAndContact(MailingList mailingList, Contact contact);
    void deleteByMailingListAndContact(MailingList mailingList, Contact contact);
}
