package com.honeycomb.colorphone;

import android.text.TextUtils;

import com.acb.call.AcbCallFactoryImpl;
import com.acb.call.ViewConfig;
import com.acb.call.themes.Type;
import com.acb.call.views.CallIdleAlert;
import com.ihs.commons.config.HSConfig;

import java.util.Collections;
import java.util.Comparator;
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
                "https://superapps-dev.s3.amazonaws.com/light/Brownian.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503453886&Signature=hZj2jGCbOztJsqppT8efNCd6lQs%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Dazzle.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454059&Signature=3GGWLJ0O%2BmG8d0yme2af2WIqlvs%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Deep%20Love.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454071&Signature=9abVU51%2Fop7wX30B%2BgKpoQpy8%2B8%3D",

                "https://superapps-dev.s3.amazonaws.com/light/DJ.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454082&Signature=99cayJPt0aYzmC0V1OnCk%2B6dNNs%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Gold%20Myth.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454112&Signature=jxXiUET013YfW3%2FDZF3U5GsacW4%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Maze.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454126&Signature=p7nkmeK3Zp1NnX7mowJyb0VCg58%3D",

                "https://superapps-dev.s3.amazonaws.com/light/Modern.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454140&Signature=wYx%2BIGgScTgOy%2Bm6QnmuFj2PYdc%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Palette.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454150&Signature=PvK3CK33kCQJ2JsRfGC0EhK0BmM%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Shining.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454199&Signature=c5Fqj7oVwNCvQNdtgcnAoXHpzN4%3D",

                "https://superapps-dev.s3.amazonaws.com/light/Raining.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454170&Signature=mdDbl5EiFiFkp1m8MJT%2FIvCzi%2Fc%3D",
                "https://superapps-dev.s3.amazonaws.com/light/Universe.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503454212&Signature=bgvdvFW9%2FgRcczW8kem3UrdZH%2Bc%3D",
                "https://superapps-dev.s3.amazonaws.com/light/snowfall.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503492646&Signature=f9smxysaosbcD%2FBad80dnYp7Eas%3D",

                "https://superapps-dev.s3.amazonaws.com/light/blizzard.gif?" +
                        "AWSAccessKeyId=AKIAITKGUQINFPMTBIFA&Expires=1503492537&Signature=ToslyMhtpnKrhysG3V6FR5Q9geI%3D",
        };

        static String GIF_URL_PREFIX = "http://cdn.appcloudbox.net/colorphoneapps/gifs/";

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

            List<String> orders = (List<String>) HSConfig.getList("Application", "Theme", "ThemeOrders");

            // Reorder before
            for (Type t : types) {
                t.setIndex(getIndexOfType(orders, t.getIdName()));
            }

            final int startId = Type.NEON + 1;
            final int endId = ID_NAMES.length + startId - 1;
            for(int id = startId; id <= endId; ++id) {
                Type info = new Type();
                final int curPos = id - startId;
                info.setId(id);
                info.setIdName(ID_NAMES[curPos]);
                info.setIndex(getIndexOfType(orders, ID_NAMES[curPos]));
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
            Collections.sort(types, new Comparator<Type>() {
                @Override
                public int compare(Type o1, Type o2) {
                    return o1.getIndex() - o2.getIndex();
                }
            });
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
