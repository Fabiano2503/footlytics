package com.fabianoanticona.footlytics.data;

import com.fabianoanticona.footlytics.model.CompetitionItem;

import java.util.List;

public class DataCache {

    private static volatile DataCache instance;
    private List<CompetitionItem> competitions;

    private DataCache() {
    }

    public static DataCache getInstance() {
        if (instance == null) {
            synchronized (DataCache.class) {
                if (instance == null) {
                    instance = new DataCache();
                }
            }
        }
        return instance;
    }

    public synchronized List<CompetitionItem> getCompetitions() {
        return competitions;
    }

    public synchronized void setCompetitions(List<CompetitionItem> competitions) {
        this.competitions = competitions;
    }

    public synchronized void clear() {
        this.competitions = null;
    }
}
