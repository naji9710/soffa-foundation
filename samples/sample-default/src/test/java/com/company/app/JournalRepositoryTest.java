package com.company.app;

import io.soffa.foundation.features.intents.Journal;
import io.soffa.foundation.features.intents.JournalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class JournalRepositoryTest {

    @Autowired
    private JournalRepository journal;

    @Test
    public void testJournal() {
        assertNotNull(journal);
        assertEquals(0, journal.count());

        Journal record = Journal.builder()
            .event("accounts.email.activation")
            .subject("account:123456789")
            .status("pending")
            .build();
        journal.save(record);
        assertEquals(1, journal.count());

    }

}
