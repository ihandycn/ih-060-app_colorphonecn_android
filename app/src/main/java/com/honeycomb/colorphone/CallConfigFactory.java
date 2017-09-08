package com.honeycomb.colorphone;

import android.text.TextUtils;

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
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "CallAssistant", "DefaultEnabled");
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
            return AdPlacements.AD_CALL_OFF;
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
                "Raining", "Universe", "Snow",
                "Floating"
        };

        static  String[] TextStrings = new String[] {
                "Brownian", "Dazzle", "Deep Love",
                "DJ", "Gold Myth", "Maze",
                "Modern", "Palette", "Shining",
                "Raining", "Universe", "Snow",
                "Floating"
        };

        // TODO order urls
        static String[] GIF_URLS_DEBUG = new String[] {
                "http://superapps-dev.s3.amazonaws.com/light/brownian.gif",
               "http://superapps-dev.s3.amazonaws.com/light/dazzle.gif",
                "http://superapps-dev.s3.amazonaws.com/light/deep%20Love.gif",

                "http://superapps-dev.s3.amazonaws.com/light/DJ.gif",
                "http://superapps-dev.s3.amazonaws.com/light/Gold%20Myth.gif",
                "http://superapps-dev.s3.amazonaws.com/light/Maze.gif",

                "http://superapps-dev.s3.amazonaws.com/light/Modern.gif",
                "http://superapps-dev.s3.amazonaws.com/light/Palette.gif",
                "http://superapps-dev.s3.amazonaws.com/light/Shining.gif",

                "http://superapps-dev.s3.amazonaws.com/light/Raining.gif",
                "http://superapps-dev.s3.amazonaws.com/light/Universe.gif",
                "http://superapps-dev.s3.amazonaws.com/light/snowfall.gif",

                "http://superapps-dev.s3.amazonaws.com/light/blizzard.gif",
        };

        static String GIF_URL_PREFIX = "http://cdn.appcloudbox.net/colorphoneapps/gifs/";

        @Override
        public int getCallerDefaultPhoto() {
            final int index = new Random().nextInt(900);
            return faces[index % faces.length];
        }

        @Override
        public List<?> getConfigThemes() {
            return HSConfig.getList(new String[]{"Application", "Theme", "List"});

        }

        @Override
        public void onConfigTypes(List<Type> types) {

            final int startId = Type.NEON + 1;
            final int endId = ID_NAMES.length + startId - 1;
            for(int id = startId; id <= endId; ++id) {
                Type info = new Type();
                final int curPos = id - startId;
                info.setId(id);
                info.setIdName(ID_NAMES[curPos]);
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

                if (BuildConfig.DEBUG) {
                    info.setGifUrl(GIF_URLS_DEBUG[curPos]);
                } else {
                    info.setGifUrl(GIF_URL_PREFIX + info.getIdName() + ".gif");
                }
                info.setAcceptIcon(String.valueOf(accept));
                info.setRejectIcon(String.valueOf(reject));
                info.setGif(true);
                info.setResType(Type.RES_LOCAL_ID);
                types.add(info);
            }

        }
    }

        private static int getIndexOfType(List<String> orders, String idName) {
            for (int i = 0; i < orders.size(); i++) {
                if (!TextUtils.isEmpty(idName) && idName.equalsIgnoreCase(orders.get(i))) {
                    return i;
                }
            }
            return 0;
        }
    }
