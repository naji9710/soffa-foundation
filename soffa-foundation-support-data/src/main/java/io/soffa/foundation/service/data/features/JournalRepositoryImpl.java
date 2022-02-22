package io.soffa.foundation.service.data.features;

import io.soffa.foundation.application.features.journal.Journal;
import io.soffa.foundation.application.features.journal.JournalRepository;
import io.soffa.foundation.infrastructure.db.DB;
import io.soffa.foundation.service.data.JdbiRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JournalRepositoryImpl extends JdbiRepository<Journal> implements JournalRepository {

    private static final String Q_INSERT = "INSERT INTO <table>(id,event,subject,data,status,created_at) VALUES(:id, :event, :subject, :data, :status, :createdAt)";

    public JournalRepositoryImpl(@Autowired(required = false) Jdbi jdbi,
                                 @Autowired(required = false) DB db) {
        super(jdbi, db, "f_journal", "jrn_");
    }

    @Override
    protected String queryInsertQuery() {
        return Q_INSERT;
    }


}
