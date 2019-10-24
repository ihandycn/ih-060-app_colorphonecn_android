package com.honeycomb.colorphone.uploadview;

import android.content.Context;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class UploadVideoPresenter implements UploadVideoContract.Presenter {

    private Context context;
    private UploadVideoModel model;
    private UploadVideoContract.View view;

    public UploadVideoPresenter(Context context, UploadVideoModel model, UploadVideoContract.View view) {
        this.context = context;
        this.model = model;
        this.view = view;
    }

    @Override
    public void requestUploadVideoData() {
        model.requestUploadVideoData(new UploadVideoModel.LoadDataCallBack<AllUserThemeBean>() {
            @Override
            public void loadData(AllUserThemeBean bean) {
                if (bean != null && bean.getShow_list() != null && bean.getShow_list().size() > 0) {
                    view.showContentView(transformData(bean));
                } else {
                    view.showNoContentView();
                }
            }

            @Override
            public void showFail() {
                view.showNoNetView();
            }
        });
    }


    @Override
    public void requestDeleteUploadData(List<Long> themeIdList) {
        model.requestDeleteUploadData(themeIdList, new UploadVideoModel.DeleteDataCallBack() {
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
