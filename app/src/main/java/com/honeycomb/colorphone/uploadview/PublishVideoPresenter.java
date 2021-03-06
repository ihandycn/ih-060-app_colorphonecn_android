package com.honeycomb.colorphone.uploadview;

import android.content.Context;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.theme.ThemeUpdateListener;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class PublishVideoPresenter implements PublishVideoContract.Presenter {

    private Context context;
    private PublishVideoModel model;
    private PublishVideoContract.View view;


    public PublishVideoPresenter(Context context, PublishVideoModel model, PublishVideoContract.View view) {
        this.context = context;
        this.model = model;
        this.view = view;
    }

    @Override
    public void requestPublishVideoData(boolean isRefresh) {
        model.requestPublishVideoData(isRefresh, new ThemeUpdateListener() {
            @Override
            public void onFailure(String errorMsg) {
                view.showNoNetView(isRefresh);
            }

            @Override
            public void onSuccess(boolean isHasData) {
                if (isHasData) {
                    view.showContentView(isRefresh);
                } else {
                    view.showNoContentView(isRefresh);
                }
            }
        });
    }

    @Override
    public void requestDeletePublishData(List<Long> themeIdList) {
        model.requestDeletePublishData(themeIdList, new PublishVideoModel.DeleteDataCallBack() {
            @Override
            public void success() {
                view.updateEditStatusAfterDelete();
            }

            @Override
            public void fail() {
                view.deleteFail();
            }
        });
    }

    private ArrayList<Theme> transformData(AllUserThemeBean bean) {
        ArrayList<Theme> dataList = new ArrayList<>();
        if (bean.getShow_list() != null && bean.getShow_list().size() > 0) {
            for (AllUserThemeBean.ShowListBean item : bean.getShow_list()) {
                Theme theme = new Theme();
                theme.setIndex(dataList.size());
                theme.setId(item.getCustomize_show_id());
                theme.setIdName(item.getFile_name());
                theme.setResType("url");
                theme.setItemIcon("");
                theme.setName(item.getFile_name());
                theme.setAcceptIcon("http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_answer.png");
                theme.setRejectIcon("http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_refuse.png");
                theme.setPreviewImage(item.getImage_url());
                theme.setThemeGuideImage("");
                theme.setMp4Url(item.getVideo_url());
                theme.setGifUrl("");
                theme.setHot(false);
                theme.setSuggestMediaType(Type.MEDIA_MP4);
                theme.setNotificationBigPictureUrl("");
                theme.setNotificationLargeIconUrl("");
                theme.setNotificationEnabled(false);
                theme.setDownload(0);
                theme.setRingtoneUrl(item.getAudio_url());
                theme.setUploaderName("");
                theme.setLocked(false);
                theme.setCanDownload(true);
                theme.setSpecialTopic(false);
                theme.setAvatar(R.drawable.theme_preview_avatar_default);
                theme.setAvatarName(HSApplication.getContext().getString(R.string.app_name));

                dataList.add(theme);
            }
        }
        return dataList;
    }
}
