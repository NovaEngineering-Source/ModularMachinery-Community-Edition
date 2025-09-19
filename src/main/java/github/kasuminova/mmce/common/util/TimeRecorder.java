package github.kasuminova.mmce.common.util;

import github.kasuminova.mmce.common.util.concurrent.ActionExecutor;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

public class TimeRecorder {
    private final IntArrayFIFOQueue usedTimeList        = new IntArrayFIFOQueue();
    private final IntArrayFIFOQueue searchUsedTimeList  = new IntArrayFIFOQueue();
    private       int               usedTimeCache       = 0;
    private       int               searchUsedTimeCache = 0;

    public void updateUsedTime(ActionExecutor executor) {
        addUsedTime(executor == null ? 0 : executor.usedTime);
    }

    public void incrementUsedTime(int add) {
        usedTimeCache += add;

        if (!usedTimeList.isEmpty()) {
            usedTimeList.enqueueFirst(usedTimeList.dequeueInt() + add);
        } else {
            usedTimeList.enqueueFirst(add);
        }
    }

    public void addUsedTime(int time) {
        usedTimeCache += time;
        usedTimeList.enqueueFirst(time);
        if (usedTimeList.size() > 100) {
            usedTimeCache -= usedTimeList.dequeueLastInt();
        }
    }

    public void addRecipeResearchUsedTime(int time) {
        searchUsedTimeCache += time;
        searchUsedTimeList.enqueueFirst(time);
        if (searchUsedTimeList.size() > 20) {
            searchUsedTimeCache -= searchUsedTimeList.dequeueLastInt();
        }
    }

    public void incrementRecipeResearchUsedTime(int add) {
        searchUsedTimeCache += add;
        if (!searchUsedTimeList.isEmpty()) {
            searchUsedTimeList.enqueueFirst(searchUsedTimeList.dequeueInt() + add);
        } else {
            searchUsedTimeList.enqueueFirst(add);
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