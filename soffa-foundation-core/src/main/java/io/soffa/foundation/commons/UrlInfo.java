package io.soffa.foundation.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlInfo {

    private String protocol;
    private int port;
    private String hostname;
    private String username;
    private String password;
    private String path;
    private Map<String,Object> params;
    private static final Map<String,Integer> DEFAULT_PORTS = new HashMap<>();
    static {
        DEFAULT_PORTS.put("http", 80);
        DEFAULT_PORTS.put("https", 443);
        DEFAULT_PORTS.put("amqp", 5672);
        DEFAULT_PORTS.put("amqps", 5672);
    }

    public String getAddress() {
        if (isDefaultPort()) {
            return protocol + "://" + hostname;
        }
        return protocol + "://" + hostname + ":" + port;
    }

    public String getHostnameWithPort() {
        if (port > 0) {
            return hostname + ":" + port;
        }
        return hostname;
    }

    private boolean isDefaultPort() {
        if (port == 0) return true;
        for (String protocol : DEFAULT_PORTS.keySet()) {
            if (protocol.equalsIgnoreCase(this.protocol)) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static UrlInfo parse(String value) {
        String[] parts = value.split("://");
        String protocol = parts[0];
        UrlInfo info = parse(new URL("https://" + parts[1]));
        info.setProtocol(protocol);
        return info;
    }

    @SneakyThrows
    public static UrlInfo parse(URL url) {
        String userInfos = url.getUserInfo();
        String username = null;
        String password = null;
        Map<String,Object> params = new HashMap<>();
        if (userInfos != null) {
            if (userInfos.contains(":")) {
                String[] userAndPassword = userInfos.trim().split(":");
                password = URLDecoder.decode(userAndPassword[1], StandardCharsets.UTF_8.toString());
                username = URLDecoder.decode(userAndPassword[0], StandardCharsets.UTF_8.toString());
            } else {
                username = userInfos.trim();
            }
        }
        String q = url.getQuery();
        if (TextUtil.isNotEmpty(q)) {
            String[] paramsStr = q.split("&");
            for (String param : paramsStr) {
                String[] keyAndValue = param.split("=");
                params.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return new UrlInfo(url.getProtocol(), url.getPort(), url.getHost(), username, password, url.getPath(), params);
    }

    public Object param(String name){
        return params.get(name);
    }


}
