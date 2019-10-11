package com.honeycomb.colorphone.ugc;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

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
     *  Create a thumbnail for a specified video by its <b><i>first<i/></b> keyframe.
     *  May return null if the video is corrupt or the format is not supported.
     * @param filePath the path of video file.
     * @param kind could be {@link MediaStore.Video.Thumbnails#MINI_KIND} or {@link MediaStore.Video.Thumbnails#MICRO_KIND}
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
    private VideoUtils(){
    }
}
