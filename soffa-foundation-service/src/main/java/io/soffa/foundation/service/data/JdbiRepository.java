package io.soffa.foundation.service.data;

import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.data.DB;
import io.soffa.foundation.data.EntityModel;
import io.soffa.foundation.errors.ConfigurationException;
import io.soffa.foundation.errors.DatabaseException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JdbiRepository<T extends EntityModel> {

    private final DB db;
    private final String tableName;

    public JdbiRepository(DB db, String tableName) {
        this.db = db;
        if (db!=null) {
            this.tableName = TextUtil.trimToEmpty(db.getTablesPrefix()) + tableName;
        }else {
            this.tableName = tableName;
        }
    }

    public void checkDb() {
        if (this.db==null) {
            throw new ConfigurationException("No database configuration found in the project");
        }
    }

    public long count() {
        return link().withHandle(handle -> {
            String query = TextUtil.format("select count(id) from %s", tableName);
            return handle.select(query).mapTo(Long.class).one();
        });
    }

    public final void save(T record) {
        if (record.getId() == null) {
            record.setId(IdGenerator.shortUUID("jrn"));
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(new Date());
        }
        try {
            link().useTransaction(handle -> {
                handle.createUpdate(queryInsertQuery())
                    .define("table", tableName)
                    .bindBean(record)
                    .execute();
            });
        } catch (Exception e) {
            throw new DatabaseException(e, e.getMessage());
        }
    }

    protected Jdbi link() {
        checkDb();
        return Jdbi.create(new TransactionAwareDataSourceProxy(db.determineTargetDataSource()));
    }

    public String getTableName() {
        return tableName;
    }

    protected abstract String queryInsertQuery();

    protected String buildQuery(String... parts) {
        return Arrays.stream(parts).map(s -> {
            if (s.contains("__TABLE__")) {
                return s.replace("__TABLE__", getTableName());
            } else {
                return s;
            }
        }).collect(Collectors.joining(" "));
    }

    protected Query newQuery(Handle h, String q) {
        return newQuery(h, q, null);
    }

    protected Query newQuery(Handle h, String q, Map<String, Object> binding) {
        Query query = h.createQuery(q).define("table", getTableName());
        if (binding != null && !binding.isEmpty()) {
            query.bindMap(binding);
        }
        return query;
    }

    protected Update newUpdate(Handle h, String q, Map<String, Object> binding) {
        Update query = h.createUpdate(q).define("table", getTableName());
        if (binding != null && !binding.isEmpty()) {
            query.bindMap(binding);
        }
        return query;
    }

    protected long count(Handle h, String q, Map<String, Object> binding) {
        if (q.toLowerCase().startsWith("where")) {
            return count(h, "FROM <table> " + q, binding);
        } else if (q.toLowerCase().startsWith("from")) {
            return count(h, "SELECT COUNT(*) " + q, binding);
        }
        return newQuery(h, q, binding).mapTo(Long.class).one();
    }

    @SuppressWarnings("UnusedReturnValue")
    protected long delete(Handle h, String q, Map<String, Object> binding) {
        if (q.toLowerCase().startsWith("where")) {
            return delete(h, "DELETE FROM <table> " + q, binding);
        }
        return newUpdate(h, q, binding).execute();
    }

}
