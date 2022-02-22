package com.company.app;

import io.soffa.foundation.core.features.jobs.PendingJob;
import io.soffa.foundation.core.features.jobs.PendingJobRepository;
import io.soffa.foundation.core.features.journal.Journal;
import io.soffa.foundation.core.features.journal.JournalRepository;
import io.soffa.foundation.errors.DatabaseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FeatureRepositoriesTest {

    @Autowired
    private PendingJobRepository pendingJobs;
    @Autowired
    private JournalRepository journal;

    public static final String EVENT = "accounts.send_activation_email";
    public static final String ACCOUNT_ID = "123456789";

    @Test
    public void testPendingJobs() {
        assertNotNull(pendingJobs);
        assertEquals(0, pendingJobs.count());

        PendingJob record = PendingJob.builder()
            .operation(EVENT)
            .subject(ACCOUNT_ID)
            .build();

        pendingJobs.save(record);
        assertThrows(DatabaseException.class, () -> {
            record.setId(null);
            pendingJobs.save(record); // operation + subject is unique
        });

        assertEquals(1, pendingJobs.count());

        assertTrue(pendingJobs.isPending(EVENT, ACCOUNT_ID));
        assertFalse(pendingJobs.isPending(EVENT, "000000"));

        assertTrue(pendingJobs.consume(EVENT, ACCOUNT_ID));
        assertEquals(0, pendingJobs.count());

        assertFalse(pendingJobs.consume(EVENT, ACCOUNT_ID));

    }


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
