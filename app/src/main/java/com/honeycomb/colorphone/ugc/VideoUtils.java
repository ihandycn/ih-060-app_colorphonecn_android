package com.honeycomb.colorphone.ugc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class VideoUtils {
    /**
     * Constant used to indicate we should recycle the input in
     * {@link ThumbnailUtils#extractThumbnail(Bitmap, int, int, int)} unless the output is the input.
     */
    private static final int OPTIONS_RECYCLE_INPUT = 0x2;

    /**
     * Constant used to indicate the dimension of mini thumbnail.
     */
    private static final int TARGET_SIZE_MINI_THUMBNAIL = 320;

    /**
     * Constant used to indicate the dimension of micro thumbnail.
     */
    private static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;

    /**
     * Create a thumbnail for a specified video by its <b><i>first<i/></b> keyframe.
     * May return null if the video is corrupt or the format is not supported.
     *
     * @param filePath the path of video file.
     * @param kind     could be {@link MediaStore.Video.Thumbnails#MINI_KIND} or {@link MediaStore.Video.Thumbnails#MICRO_KIND}
     * @return the thumbnail or null if the video is corrupt or the format is not supported.
     */
    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(0);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    /**
     * Extract audio from specified video.
     *
     * @param srcPath the path of video file.
     * @param dstPath the path of audio file that will be generated when done.
     * @param startMs the time position the extraction action will start at, if startMs is
     *                negative, time position will be the beginning of the video.
     * @param endMs   the time position the extraction action will end at, if endMs is
     *                negative, time position will be the ending of the video.
     * @throws IOException if failed caused by IOException
     */
    public static void doExtractAudioFromVideo(String srcPath, String dstPath, int startMs, int endMs) throws IOException {
        genVideoUsingMuxer(srcPath, dstPath, startMs, endMs, true, false);
    }

    // https://gist.github.com/ArsalRaza/132a6e99d59aa80b9861ae368bc786d0#file-videoutils-java
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private static final String TAG = "Audio Extractor Decoder";

    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     *                 negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     *                 no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException if failed caused by IOException
     */
    @SuppressWarnings("SameParameterValue")
    private static void genVideoUsingMuxer(String srcPath, String dstPath, int startMs, int endMs, boolean useAudio, boolean useVideo) throws IOException {
        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        SparseIntArray indexMap = new SparseIntArray(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            boolean selectCurrentTrack = false;
            if (mime != null && mime.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true;
            } else if (mime != null && mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true;
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferSize = newSize > bufferSize ? newSize : bufferSize;
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        // Set up the orientation and starting time for extractor.
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcPath);
        String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        int offset = 0;
        int trackIndex;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        muxer.start();
        while (true) {
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
            if (bufferInfo.size < 0) {
                Log.d(TAG, "Saw input EOS.");
                bufferInfo.size = 0;
                break;
            } else {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                    Log.d(TAG, "The current sample is over the trim end time.");
                    break;
                } else {
                    bufferInfo.flags = extractor.getSampleFlags();
                    trackIndex = extractor.getSampleTrackIndex();
                    muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                    extractor.advance();
                }
            }
        }
        muxer.stop();
        muxer.release();
    }

    /**
     *  Get duration of the specified video.
     * @param path the path of the specified video.
     * @return duration.
     */
    public static long getVideoDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(path);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);   // 毫秒
        long timeInMillisec = Long.parseLong(time);

        retriever.release();
        return timeInMillisec;
    }


    @SuppressLint("InlinedApi")
    private static final String[] sLocalVideoColumns = {
            MediaStore.Video.Media._ID, // 视频id
            MediaStore.Video.Media.DATA, // 视频路径
            MediaStore.Video.Media.SIZE, // 视频字节大小
            MediaStore.Video.Media.DISPLAY_NAME, // 视频名称 xxx.mp4
            MediaStore.Video.Media.TITLE, // 视频标题
            MediaStore.Video.Media.DATE_ADDED, // 视频添加到MediaProvider的时间
            MediaStore.Video.Media.DATE_MODIFIED, // 上次修改时间，该列用于内部MediaScanner扫描，外部不要修改
            MediaStore.Video.Media.MIME_TYPE, // 视频类型 video/mp4
            MediaStore.Video.Media.DURATION, // 视频时长
            MediaStore.Video.Media.ARTIST, // 艺人名称
            MediaStore.Video.Media.ALBUM, // 艺人专辑名称
            MediaStore.Video.Media.RESOLUTION, // 视频分辨率 X x Y格式
            MediaStore.Video.Media.DESCRIPTION, // 视频描述
            MediaStore.Video.Media.IS_PRIVATE,
            MediaStore.Video.Media.TAGS,
            MediaStore.Video.Media.CATEGORY, // YouTube类别
            MediaStore.Video.Media.LANGUAGE, // 视频使用语言
            MediaStore.Video.Media.LATITUDE, // 拍下该视频时的纬度
            MediaStore.Video.Media.LONGITUDE, // 拍下该视频时的经度
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
    };
    private static final String[] sLocalVideoThumbnailColumns = {
            MediaStore.Video.Thumbnails.DATA, // 视频缩略图路径
            MediaStore.Video.Thumbnails.VIDEO_ID, // 视频id
            MediaStore.Video.Thumbnails.KIND,
            MediaStore.Video.Thumbnails.WIDTH, // 视频缩略图宽度
            MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
    };


    /**
     * Get video list in device.
     * @param context Context.
     * @return video list.
     */
    @SuppressLint("InlinedApi")
    public static List<VideoInfo> getVideoList(Context context) {
        List<VideoInfo> mVideoInfos = new ArrayList<>();
        Cursor cursor = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    VideoInfo videoInfo = new VideoInfo();

                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                    long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                    String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));
                    String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                    String description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION));
                    int isPrivate = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE));
                    String tags = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS));
                    String category = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                    int dateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
                    int miniThumbMagic = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC));
                    String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                    String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    int bookmark = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK));

                    Cursor thumbnailCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                            MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                    if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                        do {
                            String thumbnailData = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                            int kind = thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND));
                            long width = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH));
                            long height = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT));

                            videoInfo.thumbnailData = thumbnailData;
                            videoInfo.kind = kind;
                            videoInfo.width = width;
                            videoInfo.height = height;
                        } while (thumbnailCursor.moveToNext());

                        thumbnailCursor.close();
                    }

                    videoInfo.id = id;
                    videoInfo.data = data;
                    videoInfo.size = size;
                    videoInfo.displayName = displayName;
                    videoInfo.title = title;
                    videoInfo.dateAdded = dateAdded;
                    videoInfo.dateModified = dateModified;
                    videoInfo.mimeType = mimeType;
                    videoInfo.duration = duration;
                    videoInfo.artist = artist;
                    videoInfo.album = album;
                    videoInfo.resolution = resolution;
                    videoInfo.description = description;
                    videoInfo.isPrivate = isPrivate;
                    videoInfo.tags = tags;
                    videoInfo.category = category;
                    videoInfo.latitude = latitude;
                    videoInfo.longitude = longitude;
                    videoInfo.dateTaken = dateTaken;
                    videoInfo.miniThumbMagic = miniThumbMagic;
                    videoInfo.bucketId = bucketId;
                    videoInfo.bucketDisplayName = bucketDisplayName;
                    videoInfo.bookmark = bookmark;

                    Log.v(TAG, "videoInfo = " + videoInfo.toString());

                    mVideoInfos.add(videoInfo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            //
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return mVideoInfos;
    }

    @SuppressWarnings("WeakerAccess")
    public static final class VideoInfo implements Serializable {
        public int id;
        public String data;
        public long size;
        public String displayName;
        public String title;
        public long dateAdded;
        public long dateModified;
        public String mimeType;
        public long duration;
        public String artist;
        public String album;
        public String resolution;
        public String description;
        public int isPrivate;
        public String tags;
        public String category;
        public double latitude;
        public double longitude;
        public int dateTaken;
        public int miniThumbMagic;
        public String bucketId;
        public String bucketDisplayName;
        public int bookmark;

        private String thumbnailData;
        private int kind;
        private long width;
        private long height;

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    ", size=" + size +
                    ", displayName='" + displayName + '\'' +
                    ", title='" + title + '\'' +
                    ", dateAdded=" + dateAdded +
                    ", dateModified=" + dateModified +
                    ", mimeType='" + mimeType + '\'' +
                    ", duration=" + duration +
                    ", artist='" + artist + '\'' +
                    ", album='" + album + '\'' +
                    ", resolution='" + resolution + '\'' +
                    ", description='" + description + '\'' +
                    ", isPrivate=" + isPrivate +
                    ", tags='" + tags + '\'' +
                    ", category='" + category + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", dateTaken=" + dateTaken +
                    ", miniThumbMagic=" + miniThumbMagic +
                    ", bucketId='" + bucketId + '\'' +
                    ", bucketDisplayName='" + bucketDisplayName + '\'' +
                    ", bookmark=" + bookmark +
                    ", thumbnailData='" + thumbnailData + '\'' +
                    ", kind=" + kind +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }


    private VideoUtils() {
    }
}
