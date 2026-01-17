package ru.yandex.practicum.client.network;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiUrlBuilder {
    private final static String HOST_PATTERN = "http://%s:%s";
    private final static char QUERIES_BEGIN = '?';
    private final static char QUERIES_SEPARATOR = '&';
    private final static char QUERY_KEY_VALUE_SEPARATOR = '=';

    private final String host;

    public ApiUrlBuilder(String host) {
        this.host = host;
    }

    public ApiUrlBuilder(String ip, String port) {
        this.host = String.format(HOST_PATTERN, ip, port);
    }

    @SafeVarargs
    public final URI buildUrl(String route, Map.Entry<String, String>... queryParams) {
        StringBuilder queriesString = new StringBuilder();
        int position = 0;
        for (Map.Entry<String, String> queryParam : queryParams) {
            queriesString.append(queryParam.getKey())
                    .append(QUERY_KEY_VALUE_SEPARATOR)
                    .append(queryParam.getValue());

            if (position++ != queryParams.length - 1) {
                queriesString.append(QUERIES_SEPARATOR);
            }
        }
        String encodedQueries = URLEncoder.encode(queriesString.toString(), StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder();
        url.append(host).append(route);
        if (!encodedQueries.isEmpty()) {
            url.append(QUERIES_BEGIN).append(encodedQueries);
        }
        return URI.create(url.toString());
    }
}
