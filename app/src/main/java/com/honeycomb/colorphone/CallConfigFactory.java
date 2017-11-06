package com.honeycomb.colorphone;

import com.acb.call.customize.AcbCallFactoryImpl;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.views.CallIdleAlert;
import com.acb.sms.SmsMessageAlertActivity;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.commons.config.HSConfig;

import java.util.List;
import java.util.Random;


public class CallConfigFactory extends AcbCallFactoryImpl {

    @Override
    public boolean isModuleEnable() {
        return true;
    }

    @Override
    public boolean isScreenFlashModuleOpenedDefault() {
        return true;
    }

    @Override
    public boolean isSMSAssistantOpenDefault() {
        return false;
    }

    @Override
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "ScreenFlash", "CallAssistant", "DefaultEnabled");
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }

    @Override
    public ThemeViewConfig getViewConfig() {
        return new CPViewConfig();
    }

    @Override
    public SmsMessageAlertActivity.Config getSMSConfig() {
        return new SmsMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                return AdPlacements.AD_MSG_NEW;
            }

            @Override
            public boolean enable() {
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT);
            }

            @Override
            public CharSequence getApplicationName() {
                return "";
            }
        };
    }

    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return AdPlacements.AD_CALL_OFF;
        }

        @Override
        public CharSequence getApplicationName() {
            return "";
        }
    }

    public static class CPViewConfig extends ThemeViewConfig {
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
    }
    }
