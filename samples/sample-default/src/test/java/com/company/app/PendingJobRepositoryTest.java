package com.company.app;

import io.soffa.foundation.errors.DatabaseException;
import io.soffa.foundation.features.jobs.PendingJob;
import io.soffa.foundation.features.jobs.PendingJobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PendingJobRepositoryTest {

    @Autowired
    private PendingJobRepository pendingJobs;

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

}
