package com.honeycomb.colorphone.ugc;

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
import java.nio.ByteBuffer;

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

    /**
     * Extract audio from specified video.
     * @param srcPath the path of video file.
     * @param dstPath the path of audio file that will be generated when done.
     * @param startMs the time position the extraction action will start at, if startMs is
     * negative, time position will be the beginning of the video.
     * @param endMs the time position the extraction action will end at, if endMs is
     * negative, time position will be the ending of the video.
     * @throws IOException if failed caused by IOException
     */
    public static void doExtractAudioFromVideo(String srcPath, String dstPath, int startMs, int endMs) throws IOException {
        genVideoUsingMuxer(srcPath, dstPath, -1, -1, true, false);
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

    private VideoUtils(){
    }
}
