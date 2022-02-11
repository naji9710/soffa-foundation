package io.soffa.foundation.core.data;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.core.exceptions.TechnicalException;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jdbi.v3.core.Jdbi;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Data
@Builder
public class DataSourceProperties {

    private static final Logger LOG = Logger.get(DataSourceProperties.class);
    public static final String H2_DRIVER = "org.h2.Driver";
    public static final String H2 = "h2";
    public static final String PG = "postgresql";
    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;
    private List<String> migrations;

    @SneakyThrows
    public static DataSourceProperties create(final String name, final String datasourceUrl) {

        String databaseUrl = datasourceUrl.trim();
        String provider;

        if (databaseUrl.startsWith("h2://")) {
            provider = H2;
        } else if (databaseUrl.matches("^(pg|postgres(ql)?)://.*")) {
            provider = PG;
        } else {
            throw new TechnicalException("Database protocol not implemented yet: " + databaseUrl);
        }

        URL url = new URL(databaseUrl.replaceAll("^([^:]+)://(.*)$", "https://$2"));

        List<NameValuePair> params = URLEncodedUtils.parse(url.toURI(), StandardCharsets.UTF_8);
        String schema = null;

        for (NameValuePair param : params) {
            if ("schema".equalsIgnoreCase(param.getName())) {
                schema = TextUtil.trimToEmpty(param.getValue());
                break;
            }
        }

        JdbcInfo jdbcInfo = createJdbcUrl(provider, url, schema, databaseUrl);
        if (schema != null && jdbcInfo.getUrl().startsWith("jdbc:h2")) {
            schema = schema.toUpperCase();
        }
        return DataSourceProperties.builder()
            .name(name)
            .username(jdbcInfo.getUsername())
            .password(jdbcInfo.getPassword())
            .schema(schema)
            .url(jdbcInfo.getUrl())
            .driverClassName(jdbcInfo.getDriver())
            .build();
    }

    @SneakyThrows
    private static JdbcInfo createJdbcUrl(String provider, URL url, String schema, String initialUrl) {
        UrlInfo urlInfo = UrlInfo.parse(url);
        if (TextUtil.isEmpty(urlInfo.getUsername())) {
            LOG.warn("No username found in database url: %s", initialUrl);
        } else if (TextUtil.isEmpty(urlInfo.getPassword())) {
            LOG.warn("No password found in database url: %s", initialUrl);
        }
        StringBuilder jdbcUrl = new StringBuilder();
        String jdbcDriver;
        StringBuilder hostname = new StringBuilder(url.getHost());
        String path = url.getPath().replaceAll("^/", "");
        String sc = schema;
        if (H2.equals(provider)) {
            jdbcDriver = H2_DRIVER;
            jdbcUrl.append(String.format("jdbc:h2:%1$s:%2$s;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", hostname, path));
            if (TextUtil.isNotEmpty(schema)) {
                sc = schema.toUpperCase();
                jdbcUrl.append(";INIT=CREATE SCHEMA IF NOT EXISTS ").append(sc);
            }
        } else {
            jdbcDriver = "org.postgresql.Driver";
            if (url.getPort() == -1) {
                hostname.append(":5432");
            } else {
                hostname.append(':').append(url.getPort());
            }
            jdbcUrl.append(String.format("jdbc:postgresql://%1$s/%2$s", hostname, path));
            if (TextUtil.isNotEmpty(schema)) {
                sc = schema.toLowerCase();
                createSchema(jdbcUrl.toString(), urlInfo.getUsername(), urlInfo.getPassword(), schema);
                jdbcUrl.append("?currentSchema=").append(schema);
            }
        }

        return new JdbcInfo(jdbcDriver, jdbcUrl.toString(), urlInfo.getUsername(), urlInfo.getPassword(), sc);
    }

    private static void createSchema(String jdbcUrl, String username, String password, String schema) {
        // Automatic schema creation if possible
        Jdbi jdbi;
        if (username != null) {
            jdbi = Jdbi.create(jdbcUrl, username, password);
        } else {
            jdbi = Jdbi.create(jdbcUrl);
        }
        jdbi.inTransaction(handle -> {
            handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            return null;
        });
    }

    public boolean hasSchema() {
        return TextUtil.isNotEmpty(schema);
    }

    @Value
    private static class JdbcInfo {
        String driver;
        String url;
        String username;
        String password;
        String schema;
    }

}
