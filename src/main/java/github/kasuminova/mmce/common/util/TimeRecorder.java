package github.kasuminova.mmce.common.util;

import github.kasuminova.mmce.common.util.concurrent.ActionExecutor;

import java.util.ArrayDeque;
import java.util.Deque;

public class TimeRecorder {
    private final Deque<Integer> usedTimeList = new ArrayDeque<>(100 + 2);
    private final Deque<Integer> searchUsedTimeList = new ArrayDeque<>(20 + 2);
    private int usedTimeCache = 0;
    private int searchUsedTimeCache = 0;

    public void updateUsedTime(ActionExecutor executor) {
        addUsedTime(executor == null ? 0 : executor.usedTime);
    }

    public void incrementUsedTime(int add) {
        usedTimeCache += add;
        Integer first = usedTimeList.getFirst();
        if (first != null) {
            usedTimeList.removeFirst();
            usedTimeList.addFirst(first + add);
        } else {
            usedTimeList.addFirst(add);
        }
    }

    public void addUsedTime(int time) {
        usedTimeCache += time;
        usedTimeList.addFirst(time);
        if (usedTimeList.size() > 100) {
            usedTimeCache -= usedTimeList.pollLast();
        }
    }

    public void addRecipeResearchUsedTime(int time) {
        searchUsedTimeCache += time;
        searchUsedTimeList.addFirst(time);
        if (searchUsedTimeList.size() > 20) {
            searchUsedTimeCache -= searchUsedTimeList.pollLast();
        }
    }

    public void incrementRecipeResearchUsedTime(int add) {
        searchUsedTimeCache += add;
        Integer first = searchUsedTimeList.getFirst();
        if (first != null) {
            searchUsedTimeList.removeFirst();
            searchUsedTimeList.addFirst(first + add);
        } else {
            searchUsedTimeList.addFirst(add);
        }
    }

    public int usedTimeAvg() {
        if (usedTimeList.isEmpty()) {
            return 0;
        }
        return usedTimeCache / usedTimeList.size();
    }

    public int recipeSearchUsedTimeAvg() {
        if (searchUsedTimeList.isEmpty()) {
            return 0;
        }
        return searchUsedTimeCache / searchUsedTimeList.size();
    }
}