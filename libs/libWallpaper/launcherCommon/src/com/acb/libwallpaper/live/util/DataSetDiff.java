package com.acb.libwallpaper.live.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Compare two {@link Collection} and find their difference against each other.
 */
public class DataSetDiff<T> {

    /**
     * Default implementation is altering the old collection to make it identical (elements, but not their order) to
     * the new collection.
     */
    public static class ResultHandler<T> {
        public void onItemsAdded(Collection<T> oldCollection, Collection<T> addedItems) {
            oldCollection.addAll(addedItems);
        }

        public void onItemsRemoved(Collection<T> oldCollection, Collection<T> removedItems) {
            oldCollection.removeAll(removedItems);
        }
    }

    /**
     * Note that {@link ResultHandler#onItemsRemoved(Collection, Collection)} is invoked before
     * {@link ResultHandler#onItemsAdded(Collection, Collection)} in case both item removal and addition occur.
     */
    public boolean diff(Collection<T> newCollection, Collection<T> oldCollection,
                        @NonNull ResultHandler<T> resultHandler) {
        boolean changed = false;
        List<T> newItems = new ArrayList<>(newCollection);
        List<T> removedItems = new ArrayList<>(oldCollection);

        // Removed items
        removedItems.removeAll(newCollection); // O(N ^ 2)
        if (!removedItems.isEmpty()) {
            changed = true;
            resultHandler.onItemsRemoved(oldCollection, removedItems);
        }

        // New wallpapers
        newItems.removeAll(oldCollection); // O(N ^ 2)
        if (!newItems.isEmpty()) {
            changed = true;
            resultHandler.onItemsAdded(oldCollection, newItems);
        }
        return changed;
    }
}
