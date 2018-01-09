/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honeycomb.colorphone.boost;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility methods for boost management.
 */
public final class BoostUtils {

    private static final Random mRand = new Random();

    private static final String[] SYSTEM_APPS = {
            "com.google.android.ext.services",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox",
            "com.android.stk",
            "com.android.systemui",
            "com.android.phone",
            "com.android.mms.service",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.android.providers.media",
            "com.android.bluetooth",
            "com.google.android.inputmethod.pinyin",
            "com.android.server.telecom",
            "com.google.android.music",
            "com.google.android.gm",
            "com.google.android.apps.inputmethod.hindi",
            "com.android.vending",
            "com.google.android.apps.docs",
            "com.dsi.ant.server",
            "com.qualcomm.qti.services.secureui",
            "com.nxp.nfceeapi.service",
            "com.qualcomm.wfd.service",
            "com.qualcomm.location",
            "com.google.android.inputmethod.latin",
            "com.google.android.apps.plus",
            "eu.chainfire.supersu",
            "com.sonyericsson.android.camera",
            "com.sonyericsson.crashmonitor",
            "com.realvnc.android.remote",
            "com.facebook.katana",
            "com.sonymobile.phoneusage",
            "com.sonymobile.photoanalyzer",
            "com.sonymobile.cameracommon",
            "com.google.android.talk",
            "com.sonyericsson.psm.sysmonservice",
            "com.sonymobile.runtimeskinning.core",
            "com.sonyericsson.advancedwidget.clock",
            "com.sonyericsson.extras.liveware",
            "com.sonyericsson.textinput.uxp",
            "com.sonymobile.xperialink",
            "com.sonymobile.mirrorlink.system",
            "com.sonyericsson.android.camera3d",
            "com.svox.pico",
            "com.sonymobile.mx.android",
            "com.android.keychain",
            "com.sonymobile.sonyselectdata",
            "com.sonyericsson.home",
            "com.sonymobile.enterprise.service",
            "com.sonyericsson.setupwizard",
            "com.android.voicedialer",
            "com.sonyericsson.smartcard",
            "system",
            "com.android.defcontainer",
            "com.sonyericsson.album",
            "com.sonyericsson.android.addoncamera.artfilter",
            "com.android.nfc",
            "com.sonyericsson.devicemonitor",
            "com.sonyericsson.android.bootinfo",
            "com.android.musicfx",
            "com.sonyericsson.updatecenter",
            "com.sonyericsson.usbux",
            "com.google.android.partnersetup",
            "com.google.android.gsf.login",
            "com.sony.smallapp.managerservice",
            "com.sec.android.app.taskmanager",
            "com.sec.android.gallery3d",
            "com.sec.android.service.health",
            "com.sec.android.app.soundalive",
            "com.sec.android.app.samsungapps",
            "com.samsung.android.app.pinboard",
            "com.sec.android.app.shealth",
            "com.samsung.android.app.assistantmenu",
            "com.sec.android.service.sm",
            "com.osp.app.signin",
            "com.samsung.android.app.vrsetupwizardstub",
            "com.google.android.apps.maps",
            "com.samsung.android.MtpApplication",
            "com.android.contacts",
            "com.samsung.android.app.galaxyfinder",
            "com.samsung.dcm",
            "com.samsung.android.providers.context",
            "com.sec.android.app.myfiles",
            "com.sec.android.app.videoplayer",
            "com.sec.android.provider.logsprovider",
            "com.ws.dm",
            "com.smlds",
            "com.sec.android.app.music",
            "com.android.mms",
            "com.sec.android.service.cm",
            "com.samsung.android.fingerprint.service",
            "com.samsung.android.sdk.samsunglink",
            "com.samsung.android.app.filterinstaller",
            "com.samsung.android.writingbuddyservice",
            "com.samsung.android.snote",
            "com.vlingo.midas",
            "com.sec.android.widgetapp.activeapplicationwidget",
            "com.sec.android.app.popupuireceiver",
            "com.android.settings",
            "com.sec.android.app.tmserver",
            "com.sec.pcw.device",
            "com.android.incallui",
            "com.samsung.android.provider.shootingmodeprovider",
            "com.sec.android.daemonapp",
            "com.samsung.android.app.powersharing",
            "com.sec.spp.push",
            "com.sec.android.inputmethod",
            "com.sec.android.app.launcher",
            "com.sec.android.service.bezel",
            "com.sec.android.widgetapp.ap.hero.accuweather",
            "com.mediatek.batterywarning",
            "com.android.launcher",
            "com.android.quicksearchbox",
            "com.android.inputmethod.latin",
            "com.nqmobile.antivirus20",
            "com.mediatek.thermalmanager",
            "com.android.gallery3d",
            "com.evernote.skitch",
            "com.mediatek.atci.service",
            "com.mediatek.voicecommand",
            "com.android.location.fused",
            "com.redbend.dmClient",
            "com.tinno.gesture.phone",
            "com.mediatek.bluetooth",
            "com.huawei.android.ds",
            "com.taobao.taobao",
            "com.huawei.android.pushagent",
            "com.huawei.appmarket",
            "com.huawei.geofence",
            "com.huawei.bone",
            "com.android.externalstorage",
            "com.vlife.huawei.wallpaper",
            "com.huawei.powergenie",
            "com.huawei.ca",
            "com.huawei.wallet",
            "com.netease.newsreader.activity",
            "com.android.keyguard",
            "com.android.documentsui",
            "com.tencent.mtt",
            "com.huawei.gallery.photoshare",
            "org.simalliance.openmobileapi.service",
            "com.android.huawei.projectmenu",
            "com.huawei.motionservice",
            "com.snowballtech.walletservice",
            "com.nuance.swype.emui",
            "com.huawei.floatMms",
            "com.huawei.camera",
            "com.huawei.systemmanager",
            "com.huawei.android.totemweather",
            "com.huawei.android.karaokeeffect",
            "com.baidu.input_huawei",
            "com.cootek.smartdialer_oem_module",
            "com.sohu.sohuvideo",
            "com.sina.weibo",
            "com.huawei.android.powermonitor",
            "com.huawei.bd",
            "com.android.email",
            "com.huawei.android.hwouc",
            "com.amap.android.location",
            "com.huawei.remoteassistant",
            "com.huawei.hwid",
            "com.hw.sohu.newsclient.newswall",
            "com.huawei.android.airsharingcast",
            "com.huawei.android.mewidget",
            "com.android.supl",
            "com.huawei.android.launcher",
            "com.android.dolbymobileaudioeffect",
            "com.huawei.android.multiscreen",
            "com.baidu.map.location",
            "com.huawei.smartpower",
            "com.rxnetworks.pgpsdownloader",
            "com.motorola.MotGallery2",
            "com.motorola.ccc.checkin",
            "com.motorola.motocare.internal",
            "com.motorola.bach.modemstats",
            "com.motorola.motocare",
            "com.motorola.ccc.ota",
            "com.motorola.ccc.devicemanagement",
            "com.motorola.android.nativedropboxagent",
            "com.motorola.ccc.notification",
            "com.motorola.ccc.mainplm",
            "com.android.dialer",
            "com.lge.ime",
            "com.android.packageinstaller",
            "com.mount.dev",
            "com.lge.lockscreensettings",
            "com.lge.keepscreenon",
            "com.lge.ia",
            "com.google.android.setupwizard",
            "com.google.android.apps.magazines",
            "com.lge.splitwindow",
            "com.lge.mlt",
            "com.google.android.syncadapters.calendar",
            "com.google.android.configupdater",
            "com.lge.appbox.client",
            "com.lge.sizechangable.musicwidget.widget",
            "com.qualcomm.services.location",
            "com.lge.mrg.service",
            "com.google.android.apps.books",
            "com.lge.music",
            "com.lge.sizechangable.weather",
            "com.android.providers.calendar",
            "com.lge.springcleaning",
    };
    private static final List<String> SYSTEM_APPS_LIST = new ArrayList<>(202);

    static {
        for (String systemApp : SYSTEM_APPS) {
            SYSTEM_APPS_LIST.add(systemApp);
        }
    }

    public static List<String> getSystemApps() {
        return SYSTEM_APPS_LIST;
    }

    public static int getBoostedMemSizeBytes(Context context, int boostedPercentage) {
        int boostedSizeBytes = Math.round(DeviceManager.getInstance().getTotalRam() *
                // Dither the percentage to make it looks real
                (boostedPercentage + mRand.nextFloat() - 0.5f) / 100f);
        if (boostedSizeBytes <= 0) {
            boostedSizeBytes = mRand.nextInt(3) + 1;
        }
        return boostedSizeBytes;
    }
}
