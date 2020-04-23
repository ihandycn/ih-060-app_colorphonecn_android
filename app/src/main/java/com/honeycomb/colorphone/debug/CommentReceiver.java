package com.honeycomb.colorphone.debug;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.feedback.RateGuideDialogWithAcc1;
import com.honeycomb.colorphone.feedback.RateGuideDialogWithAccOppo2;
import com.honeycomb.colorphone.feedback.RateGuideDialogWithAccXiaomi2;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.acc.AccCommentReceiver;

public class CommentReceiver extends AccCommentReceiver {

    private static final String TAG = "CommentReceiver";

    @Override
    public void onCommentFirstStep(@NonNull Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentFirstStep: " + rect.toString());
        RateGuideDialogWithAcc1.show(HSApplication.getContext(), rect);
    }

    @Override
    public void onCommentXiaoMiSecondStep(Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentXiaoMiSecondStep: click, rect = " + rect.toString());
        RateGuideDialogWithAccXiaomi2.show(HSApplication.getContext(), rect);
    }

    @Override
    public void onCommentOppoSecondStep(@NonNull Rect rect) {
        HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentOppoSecondStep: " + rect.toString());
        RateGuideDialogWithAccOppo2.show(HSApplication.getContext(), rect);
    }
}
