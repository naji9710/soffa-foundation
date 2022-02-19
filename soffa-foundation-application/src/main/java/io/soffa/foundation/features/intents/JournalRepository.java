package io.soffa.foundation.features.intents;

public interface JournalRepository {

    long count();

    void save(Journal record);


}
