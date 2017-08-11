package com.honeycomb.colorphone;

import android.content.res.Resources;

import com.acb.call.AcbCallFactoryImpl;
import com.acb.call.ViewConfig;
import com.acb.call.themes.Type;
import com.acb.call.views.CallIdleAlert;
import com.ihs.commons.config.HSConfig;

import java.util.List;
import java.util.Random;


public class CallConfigFactory extends AcbCallFactoryImpl {

    @Override
    public boolean isModuleEnable() {
        return true;
    }

    @Override
    public boolean isSettingsOpenDefault() {
        return true;
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }

    @Override
    public ViewConfig getViewConfig() {
        return new CPViewConfig();
    }

    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return "ColorPhone_A(NativeAds)CallOff";
        }
    }

    public static class CPViewConfig extends ViewConfig {
        int[] faces = new int[]{
                R.drawable.face_1,
                R.drawable.face_2,
                R.drawable.face_3,
                R.drawable.face_4,
                R.drawable.face_5,
                R.drawable.face_6,
                R.drawable.face_7,
                R.drawable.face_8

        };

        static String[] ID_NAMES = new String[] {
                "Brownian", "Dazzle", "DeepLove",
                "DJ", "GoldMyth", "Maze",
                "Modern", "Palette", "Shining",
                "Raining", "Universe"
        };

        static  String[] TextStrings = new String[] {
                "Brownian", "Dazzle", "Deep Love",
                "DJ", "Gold Myth", "Maze",
                "Modern", "Palette", "Shining",
                "Raining", "Universe"
        };

        // TODO order urls
        static String[] GIF_URLS = new String[] {
                "https://files.slack.com/files-pri/T06UGANUX-F6JSF475W/______.gif",
                "https://files.slack.com/files-pri/T06UGANUX-F6JSF475W/______.gif",
                "https://files.slack.com/files-pri/T06UGANUX-F6JSF475W/______.gif",

                "https://ihandy.slack.com/files/xiaoxue.lu/F6KGM43B4/dj-2.gif",
                "https://ihandy.slack.com/files/xiaoxue.lu/F6KE231LK/_________.gif",
                "https://ihandy.slack.com/files/xiaoxue.lu/F6LJWVCHM/_________.gif",

                "https://ihandy.slack.com/files/xiaoxue.lu/F6KE26G03/______.gif",
                "https://ihandy.slack.com/files/xiaoxue.lu/F6KE1QAGK/____________.gif",
                "https://ihandy.slack.com/files/xiaoxue.lu/F6JSEP3LG/______.gif",

                "https://ihandy.slack.com/files/xiaoxue.lu/F6JTVA8HF/____________.gif",
                "https://ihandy.slack.com/files/xiaoxue.lu/F6JSF475W/______.gif",
        };



        @Override
        public int getCallerDefaultPhoto() {
            return faces[new Random(3982).nextInt(faces.length)];
        }

        @Override
        public List<?> getConfigThemes() {
            return HSConfig.getList(new String[]{"Application", "Theme", "List"});

        }

        @Override
        public void onConfigTypes(List<Type> types) {

            Resources resources = ColorPhoneApplication.getContext().getResources();
            final int startId = Type.NEON + 1;
            final int endId = ID_NAMES.length + startId - 1;
            for(int id = startId; id <= endId; ++id) {
                Type info = new Type();
                final int curPos = id - startId;
                info.setId(id);
                info.setIdName(ID_NAMES[curPos]);
                info.setIndex(types.size());
                info.setName(TextStrings[curPos]);
                int accept;
                int reject;
                switch(info.getIdName()) {
                    case "DeepLove":
                        accept = R.drawable.acb_call_deeplove_yes;
                        reject = R.drawable.acb_call_deeplove_no;
                        break;
                    case "Maze":
                        accept = R.drawable.acb_call_maze_yes;
                        reject = R.drawable.acb_call_maze_no;
                        break;
                    case "Modern":
                        accept = R.drawable.acb_call_modern_yes;
                        reject = R.drawable.acb_call_modern_no;
                        break;
                    default:
                        accept = R.drawable.acb_phone_call_answer;
                        reject = R.drawable.acb_phone_call_refuse;
                }

                info.setGifUrl(GIF_URLS[curPos]);
                info.setAcceptIcon(String.valueOf(accept));
                info.setRejectIcon(String.valueOf(reject));
                info.setGif(true);
                info.setResType(Type.RES_LOCAL_ID);
                types.add(info);
            }
        }
    }
}
