package ru.yandex.practicum.common.dto.clientresult;

public record WordleClientResult(String username, int usedAttempts, boolean hasUsedHints) { }
