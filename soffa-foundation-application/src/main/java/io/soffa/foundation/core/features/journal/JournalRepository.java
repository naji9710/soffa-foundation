package io.soffa.foundation.core.features.journal;

public interface JournalRepository {

    long count();

    void save(Journal record);


}
