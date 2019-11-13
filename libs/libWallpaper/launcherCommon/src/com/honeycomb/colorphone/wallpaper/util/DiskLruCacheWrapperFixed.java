package com.honeycomb.colorphone.wallpaper.util;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default DiskCache implementation. There must be no more than one active instance for a given
 * directory at a time.
 *
 * @see #get(File, int)
 */
public class DiskLruCacheWrapperFixed implements DiskCache {
    private static final String TAG = "DiskLruCacheWrapperFixed";

    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static DiskLruCacheWrapperFixed wrapper = null;

    private final DiskCacheWriteLocker writeLocker = new DiskCacheWriteLocker();
    private final SafeKeyGenerator safeKeyGenerator;
    private final File directory;
    private final int maxSize;
    private DiskLruCache diskLruCache;

    /**
     * Get a DiskCache in the given directory and size. If a disk cache has alread been created with
     * a different directory and/or size, it will be returned instead and the new arguments
     * will be ignored.
     *
     * @param directory The directory for the disk cache
     * @param maxSize   The max size for the disk cache
     * @return The new disk cache with the given arguments, or the current cache if one already exists
     */
    public static synchronized DiskCache get(File directory, int maxSize) {
        if (directory == null || maxSize <= 0) {
            throw new IllegalArgumentException("Error directory: " + directory + " or maxSize: " + maxSize);
        }
        if (wrapper == null || !wrapper.directory.equals(directory) || wrapper.maxSize != maxSize) {
            wrapper = new DiskLruCacheWrapperFixed(directory, maxSize);
        }
        return wrapper;
    }

    protected DiskLruCacheWrapperFixed(File directory, int maxSize) {
        this.directory = directory;
        this.maxSize = maxSize;
        this.safeKeyGenerator = new SafeKeyGenerator();
    }

    private synchronized DiskLruCache getDiskCache() throws IOException {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize);
        }
        return diskLruCache;
    }

    private synchronized void resetDiskCache() {
        diskLruCache = null;
    }

    @Override
    public File get(Key key) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        File result = null;
        try {
            //It is possible that the there will be a put in between these two gets. If so that shouldn't be a problem
            //because we will always put the same value at the same key so our input streams will still represent
            //the same data
            final DiskLruCache.Value value = getDiskCache().get(safeKey);
            if (value != null) {
                result = value.getFile(0);
            }
        } catch (IOException e) {
            HSLog.d(TAG, "Unable to get from disk cache" + e.getMessage());
        }
        return result;
    }

    @Override
    public void put(Key key, Writer writer) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        writeLocker.acquire(key);
        try {
            DiskLruCache.Editor editor = getDiskCache().edit(safeKey);
            // Editor will be null if there are two concurrent puts. In the worst case we will just silently fail.
            if (editor != null) {
                try {
                    File file = editor.getFile(0);
                    if (writer.write(file)) {
                        editor.commit();
                    }
                } finally {
                    editor.abortUnlessCommitted();
                }
            }
        } catch (IOException e) {
            HSLog.d(TAG, "Unable to put to disk cache" + e.getMessage());
        } finally {
            writeLocker.release(key);
        }
    }

    @Override
    public void delete(Key key) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        try {
            getDiskCache().remove(safeKey);
        } catch (IOException e) {
            HSLog.d(TAG, "Unable to delete from disk cache" + e.getMessage());
        }
    }

    @Override
    public synchronized void clear() {
        try {
            getDiskCache().delete();
            resetDiskCache();
        } catch (IOException e) {
            HSLog.d(TAG, "Unable to clear disk cache" + e.getMessage());
        }
    }

    private class DiskCacheWriteLocker {
        private final Map<Key, WriteLock> locks = new HashMap<Key, WriteLock>();
        private final WriteLockPool writeLockPool = new WriteLockPool();

        void acquire(Key key) {
            WriteLock writeLock;
            synchronized (this) {
                writeLock = locks.get(key);
                if (writeLock == null) {
                    writeLock = writeLockPool.obtain();
                    locks.put(key, writeLock);
                }
                writeLock.interestedThreads++;
            }

            writeLock.lock.lock();
        }

        void release(Key key) {
            WriteLock writeLock;
            synchronized (this) {
                writeLock = locks.get(key);
                if (writeLock == null || writeLock.interestedThreads <= 0) {
                    throw new IllegalArgumentException(
                            "Cannot release a lock that is not held" + ", key: " + key + ", interestedThreads: "
                                    + (writeLock == null ? 0 : writeLock.interestedThreads));
                }

                if (--writeLock.interestedThreads == 0) {
                    WriteLock removed = locks.remove(key);
                    if (!removed.equals(writeLock)) {
                        throw new IllegalStateException("Removed the wrong lock"
                                + ", expected to remove: " + writeLock
                                + ", but actually removed: " + removed
                                + ", key: " + key);
                    }
                    writeLockPool.offer(removed);
                }
            }

            writeLock.lock.unlock();
        }

        private class WriteLock {
            final Lock lock = new ReentrantLock();
            int interestedThreads;
        }

        private class WriteLockPool {
            private static final int MAX_POOL_SIZE = 10;
            private final Queue<WriteLock> pool = new ArrayDeque<WriteLock>();

            WriteLock obtain() {
                WriteLock result;
                synchronized (pool) {
                    result = pool.poll();
                }
                if (result == null) {
                    result = new WriteLock();
                }
                return result;
            }

            void offer(WriteLock writeLock) {
                synchronized (pool) {
                    if (pool.size() < MAX_POOL_SIZE) {
                        pool.offer(writeLock);
                    }
                }
            }
        }
    }

    private class SafeKeyGenerator {
        private final LruCache<Key, String> loadIdToSafeHash = new LruCache<Key, String>(1000);

        public String getSafeKey(Key key) {
            String safeKey;
            synchronized (loadIdToSafeHash) {
                safeKey = loadIdToSafeHash.get(key);
            }
            if (safeKey == null) {
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    key.updateDiskCacheKey(messageDigest);
                    safeKey = Util.sha256BytesToHex(messageDigest.digest());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }
}
