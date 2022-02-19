package io.soffa.foundation.service.features;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.data.DB;
import io.soffa.foundation.features.jobs.PendingJob;
import io.soffa.foundation.features.jobs.PendingJobRepository;
import io.soffa.foundation.service.data.JdbiRepository;
import org.jdbi.v3.core.statement.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PendingJobRepositoryImpl extends JdbiRepository<PendingJob> implements PendingJobRepository {

    private static final String Q_INSERT = "INSERT INTO <table>(id,operation,subject,data,created_at) VALUES(:id, :operation, :subject, :data, :createdAt)";

    public PendingJobRepositoryImpl(@Autowired(required = false) DB db) {
        super(db, "f_pending_jobs");
    }

    @Override
    protected String queryInsertQuery() {
        return Q_INSERT;
    }

    @Override
    public boolean isPending(String operation, String subject) {
        return link().withHandle(h -> {
            String q = "WHERE operation=:operation AND subject=:subject";
            return count(h, q, ImmutableMap.of("operation", operation, "subject", subject))> 0;
        });
    }

    @Override
    public void delete(String operation, String subject) {
        link().useTransaction(h -> {
            String q = buildQuery("WHERE operation=:operation AND subject=:subject", getTableName());
            delete(h, q, ImmutableMap.of("operation", operation, "subject", subject));
        });
    }

    @Override
    public boolean consume(String operation, String subject) {
        String id = link().withHandle(h -> {
            String q = "SELECT id FROM <table> WHERE operation=:operation AND subject=:subject";
            Query query = newQuery(h, q, ImmutableMap.of("operation", operation, "subject", subject));
            return query.mapTo(String.class).findFirst().orElse(null);
        });
        if (id == null) {
            return false;
        }
        long deleted = link().inTransaction(h -> delete(h, "WHERE id=:id", ImmutableMap.of("id", id)));
        return deleted > 0;
    }
}
