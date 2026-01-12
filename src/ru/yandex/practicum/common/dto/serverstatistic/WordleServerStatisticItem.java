package ru.yandex.practicum.common.dto.serverstatistic;

public class WordleServerStatisticItem implements Cloneable {
    private final String username;

    private Integer count;

    public WordleServerStatisticItem(String username, int count) {
        this.username = username;
        this.count = count;
    }

    public String getUsername() {
        return username;
    }

    public Integer getCount() {
        return count;
    }

    public void incCount() {
        count++;
    }

    @Override
    public WordleServerStatisticItem clone() {
        return new WordleServerStatisticItem(username, count);
    }
}
