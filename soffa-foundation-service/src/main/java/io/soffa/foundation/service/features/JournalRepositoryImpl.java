package io.soffa.foundation.service.features;

import io.soffa.foundation.data.DB;
import io.soffa.foundation.features.intents.Journal;
import io.soffa.foundation.features.intents.JournalRepository;
import io.soffa.foundation.service.data.JdbiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JournalRepositoryImpl extends JdbiRepository<Journal> implements JournalRepository {

    private static final String Q_INSERT = "INSERT INTO <table>(id,event,subject,data,status,created_at) VALUES(:id, :event, :subject, :data, :status, :createdAt)";

    public JournalRepositoryImpl(@Autowired(required = false) DB db) {
        super(db, "f_journal", "jrn_");
    }

    @Override
    protected String queryInsertQuery() {
        return Q_INSERT;
    }


}
