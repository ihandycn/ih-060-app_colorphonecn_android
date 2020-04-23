package com.honeycomb.colorphone.debug;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.acc.AccCommentReceiver;

public class CommentReceiver extends AccCommentReceiver {

    private static final String TAG = "CommentReceiver";

    @Override
    public void onCommentFirstStep(@NonNull Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentFirstStep: " + rect.toString());
    }

    @Override
    public void onCommentXiaoMiSecondStep(Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentXiaoMiSecondStep: click, rect = " + rect.toString());
    }

    @Override
    public void onCommentOppoSecondStep(@NonNull Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentOppoSecondStep: " + rect.toString());
    }
}
