package ru.yandex.practicum.server.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QueryParser {
    private static final String PARAMS_SEPARATOR = "&";
    private static final char KEY_VALUE_SEPARATOR = '=';
    private final Map<String, List<String>> params;

    public QueryParser(String query) {
        this.params = splitQuery(query);
    }

    public List<String> getParam(String key) {
        return params.getOrDefault(key, new LinkedList<>());
    }

    static Map<String, List<String>> splitQuery(String query) {
        final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
        final String[] pairs = query.split(PARAMS_SEPARATOR);
        for (String pair : pairs) {
            final int idx = pair.indexOf(KEY_VALUE_SEPARATOR);
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            if (!queryPairs.containsKey(key)) {
                queryPairs.put(key, new LinkedList<>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ?
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
            queryPairs.get(key).add(value);
        }
        return queryPairs;
    }
}
