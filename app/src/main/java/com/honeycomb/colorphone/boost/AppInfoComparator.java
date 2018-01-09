package com.honeycomb.colorphone.boost;

import java.util.Comparator;

public class AppInfoComparator implements Comparator<AppInfo> {
    @Override
    public int compare(AppInfo lhs, AppInfo rhs) {
        char indexer1 = lhs.getFirst();
        char indexer2 = rhs.getFirst();

        if (indexer1 == indexer2) {
            return 0;
        } else if (indexer1 == '#') {
            return 1;
        } else if (indexer2 == '#') {
            return -1;
        } else if (indexer1 < indexer2) {
            return -1;
        } else {
            return 1;
        }
    }
}
