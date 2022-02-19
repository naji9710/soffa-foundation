package io.soffa.foundation.features.jobs;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface PendingJobRepository {

    long count();

    void save(PendingJob record);

    default void create(@NonNull String operation, @NonNull String subject) {
        save(PendingJob.builder().operation(operation).subject(subject).build());
    }

    boolean isPending(String operation, String subject);

    void delete(String operation, String subbjet);

    boolean consume(String operation, String subbjet);



}
