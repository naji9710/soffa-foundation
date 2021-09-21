package io.soffa.commons.data;

import io.soffa.commons.exceptions.TechnicalException;
import io.soffa.commons.lang.TextUtil;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.jdbi.v3.core.Jdbi;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Data
@Builder
public class DataSourceProperties {

    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;

    public static final String H2 = "h2";
    public static final String PG = "postgresql";

    public boolean hasSchema() {
        return TextUtil.isNotEmpty(schema);
    }

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

        URL url = new URL(databaseUrl.replaceAll("^([^:]+)://(.*)$", "http://$2"));
        String userInfos = url.getUserInfo();
        String username = "";
        String password = null;
        String jdbcUrl;
        String jdbcDriver;
        String hostname = url.getHost();
        String path = url.getPath().replaceAll("^/", "");

        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(url.toString()).build().getQueryParams();
        String schema = TextUtil.trimToEmpty(parameters.getFirst("schema"));

        if (userInfos != null) {
            if (userInfos.contains(":")) {
                String[] userAndPassword = userInfos.trim().split(":");
                password = URLDecoder.decode(userAndPassword[1], StandardCharsets.UTF_8.toString());
                username = URLDecoder.decode(userAndPassword[0], StandardCharsets.UTF_8.toString());
            } else {
                username = userInfos.trim();
            }
        }

        if (H2.equals(provider)) {
            jdbcDriver = H2_DRIVER;
            jdbcUrl = String.format("jdbc:h2:%1$s:%2$s;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", hostname, path);
            if (TextUtil.isNotEmpty(schema)) {
                schema = schema.toUpperCase();
                createSchema(jdbcUrl, username, password, schema);
                jdbcUrl += String.format(";INIT=CREATE SCHEMA IF NOT EXISTS %1$s", schema);
            }
        } else {
            jdbcDriver = "org.postgresql.Driver";
            if (url.getPort() == -1) {
                hostname += ":5432";
            } else {
                hostname += ":" + url.getPort();
            }
            jdbcUrl = String.format("jdbc:postgresql://%1$s/%2$s", hostname, path);
            if (TextUtil.isNotEmpty(schema)) {
                schema = schema.toLowerCase();
                createSchema(jdbcUrl, username, password, schema);
                jdbcUrl += "?currentSchema=" + schema;
            }
        }
        return DataSourceProperties.builder()
            .name(name)
            .username(username)
            .password(password)
            .schema(schema)
            .url(jdbcUrl)
            .driverClassName(jdbcDriver)
            .build();
    }

    private static void createSchema(String jdbcUrl, String username, String password, String schema) {
        // Automatic schema creation if possible
        Jdbi jdbi = Jdbi.create(jdbcUrl, username, password);
        jdbi.inTransaction(handle -> {
            handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            return null;
        });
    }

    public static final String H2_DRIVER = "org.h2.Driver";
}
