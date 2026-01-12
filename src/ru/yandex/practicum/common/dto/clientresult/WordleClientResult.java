package ru.yandex.practicum.common.dto.clientresult;

public class WordleClientResult {
    private String username;

    public WordleClientResult(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
