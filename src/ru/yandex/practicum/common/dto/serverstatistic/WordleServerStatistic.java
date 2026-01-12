package ru.yandex.practicum.common.dto.serverstatistic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class WordleServerStatistic {
    private int startPosition = 0;

    private final ArrayList<WordleServerStatisticItem> rating;

    public WordleServerStatistic(List<WordleServerStatisticItem> rating) {
        this.rating = new ArrayList<>(
                rating.stream().sorted(Comparator.comparing(WordleServerStatisticItem::getCount)).toList().reversed()
        );
    }

    public List<WordleServerStatisticItem> getRating() {
        return rating;
    }

    public WordleServerStatisticItem incrementUserStatistic(String username) {
        int ind = findUserIndex(username);
        WordleServerStatisticItem item;
        if (ind == -1) {
            item = new WordleServerStatisticItem(username, 0);
        } else {
            item = rating.get(ind);
            rating.remove(ind);
        }
        item.incCount();

        int r = 0;
        int l = rating.size();
        while (l - r > 1) {
            int mid = (r + l) / 2;
            if (rating.get(mid).getCount() < item.getCount()) {
                l = mid;
            } else {
                r = mid;
            }
        }
        if (r == rating.size()) {
            rating.addLast(item);
        } else {
            rating.add(r + 1, item);
        }
        return item.clone();
    }

    public Optional<WordleServerStatistic> getUserStatistic(String username, int beforeCount, int afterCount) {
        if (beforeCount < 0 || afterCount < 0) return Optional.empty();

        int ind = findUserIndex(username);
        for (int i = 0; i < rating.size(); i++) {
            if (rating.get(i).getUsername().equalsIgnoreCase(username)) {
                ind = i;
                break;
            }
        }
        if (ind == -1) {
            return Optional.empty();
        }
        int sliceStartPosition;
        int sliceEndPosition;
        if (ind < beforeCount + afterCount) {
            sliceStartPosition = 0;
            sliceEndPosition = min(rating.size(), beforeCount + afterCount);
        } else {
            sliceStartPosition = max(0, ind - beforeCount);
            sliceEndPosition = min(rating.size(), ind + afterCount + 1);
        }
        WordleServerStatistic userStatistics =
                new WordleServerStatistic(new ArrayList<>(rating.subList(sliceStartPosition, sliceEndPosition)));
        userStatistics.setStartPosition(sliceStartPosition);
        return Optional.of(userStatistics);
    }

    private int findUserIndex(String username) {
        for (int i = 0; i < rating.size(); i++) {
            if (rating.get(i).getUsername().equalsIgnoreCase(username)) {
                return i;
            }
        }
        return -1;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }
}


