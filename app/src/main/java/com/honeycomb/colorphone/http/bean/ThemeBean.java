package com.honeycomb.colorphone.http.bean;

public class ThemeBean {

    public String Description;
    public int DownloadNum;
    public String Gif;
    public boolean Hot;
    public String Icon;
    public String IconAccept;
    public String IconReject;
    public long Id;
    public String IdName;

    public LocalPushClass LocalPush;

    public String Mp4;
    public String Name;
    public String PreviewImage;
    public String ResType;
    public String UserName;
    public String Ringtone;
    public String ThemeGuidePreviewImage;

    public StatusClass Status;

    private static class LocalPushClass {
        public boolean Enable;
        public String LocalPushIcon;
        public String LocalPushPreviewImage;
    }

    private static class StatusClass {
        public boolean Lock;
        public boolean StaticPreview;
    }

}
