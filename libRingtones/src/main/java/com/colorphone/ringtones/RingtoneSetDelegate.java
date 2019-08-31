package com.colorphone.ringtones;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.ringtones.module.Ringtone;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class RingtoneSetDelegate implements RingtoneManager.RingtoneSetHandler {
    private RingtoneSetViewHolder ringtoneSetViewHolder;

    private Activity mActivity;

    public RingtoneSetDelegate(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onSetRingtone(Ringtone ringtone) {
        if (ringtoneSetViewHolder == null) {
            ViewStub stub = mActivity.findViewById(R.id.stub_ringtone_set_frame);
            if (stub != null) {
                stub.inflate();
            } else {
                throw new IllegalStateException("activity must include stub_ringtone_set_frame");
            }

            View rootRingtoneSetView = mActivity.findViewById(R.id.ringtone_set_root);
            ringtoneSetViewHolder = new RingtoneSetViewHolder(rootRingtoneSetView);
            ringtoneSetViewHolder.backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.onBackPressed();
                }
            });

            RingtoneSetter ringtoneSetter = RingtoneConfig.getInstance().getRingtoneSetter();
            ringtoneSetViewHolder.setSomeoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ringtoneSetter != null) {
                        ringtoneSetter.onSetForSomeOne(ringtone);
                    }

                }
            });

            ringtoneSetViewHolder.setDefaultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ringtoneSetter != null) {
                        ringtoneSetter.onSetAsDefault(ringtone);
                    }
                }
            });
        }

        ringtoneSetViewHolder.rootView.setVisibility(View.VISIBLE);

        ringtoneSetViewHolder.title.setText(ringtone.getTitle());
        ringtoneSetViewHolder.singer.setText(ringtone.getSinger());
        RingtoneImageLoader imageLoader = RingtoneConfig.getInstance().getRingtoneImageLoader();
        imageLoader.loadImage(
                ringtoneSetViewHolder.cover.getContext(),
                ringtone.getImgUrl(),
                ringtoneSetViewHolder.cover,
                R.drawable.ringtone_item_cover_default);
    }

    public void onStart() {
        if (isRingtoneSetPageShow()) {
            ringtoneSetViewHolder.hide();
        }
    }

    private boolean isRingtoneSetPageShow() {
        return ringtoneSetViewHolder != null && ringtoneSetViewHolder.isVisible();
    }

    public boolean handleBackPress() {
        if (isRingtoneSetPageShow()) {
            ringtoneSetViewHolder.hide();
            return true;
        }
        return false;
    }

    private static class RingtoneSetViewHolder {
        private final TextView title;
        private final TextView singer;
        private final ImageView cover;
        private final TextView setDefaultButton;
        private final TextView setSomeoneButton;

        private View rootView;
        private View backButton;


        public RingtoneSetViewHolder(View rootView) {
            this.rootView = rootView;
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    // block event
                    return true;
                }
            });

            backButton = rootView.findViewById(R.id.nav_back);
            title = (TextView) rootView.findViewById(R.id.ringtone_title);
            singer = (TextView) rootView.findViewById(R.id.ringtone_singer);
            cover = (ImageView) rootView.findViewById(R.id.cover_image);
            setDefaultButton = rootView.findViewById(R.id.ringtone_set_default);
            setSomeoneButton = rootView.findViewById(R.id.ringtone_set_someone);


            Drawable drawable1 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#FFE048"),
                    Dimensions.pxFromDp(26), true);

            Drawable drawable2 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#E8E8E9"),
                    Dimensions.pxFromDp(26), true);

            setDefaultButton.setBackground(drawable1);

            setSomeoneButton.setBackground(drawable2);
        }

        public boolean isVisible() {
            return rootView.getVisibility() == View.VISIBLE;
        }

        public void hide() {
            rootView.setVisibility(View.GONE);
        }
    }
}
