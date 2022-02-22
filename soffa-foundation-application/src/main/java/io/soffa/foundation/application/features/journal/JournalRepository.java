package io.soffa.foundation.application.features.journal;

public interface JournalRepository {

    long count();

    void save(Journal record);


}
