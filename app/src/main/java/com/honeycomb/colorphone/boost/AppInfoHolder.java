package com.honeycomb.colorphone.boost;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class AppInfoHolder {

    private static final String PREF_CALCULATOR_PACKAGE_NAME = "pref_calculator_package_name";
    private static final String PREF_CALCULATOR_ACTIVITY_NAME = "pref_calculator_activity_name";

    private static final String CHROME_PACKAGE_NAME = "com.android.chrome";

    private static final String CALCULATOR_PACKAGE_NAME = "com.android.calculator2";
    private static final String CALCULATOR_CLASS_NAME = "com.android.calculator2.Calculator";

    public static final List<String> PHONE_PACKAGE = new ArrayList<String>();
    public static final String CONTACTS_PACKAGE = "com.android.contacts";
    public static final String DIALTACTS_ACTIVITY_CLASS = "com.android.contacts.activities.DialtactsActivity";

    static {
        PHONE_PACKAGE.add("com.android.phone");
        PHONE_PACKAGE.add("com.android.server.telecom");
    }

    private static final String[] CALCULATOR_PACKAGE_NAMES = {
            "com.sec.android.app.popupcalculator",
            "com.sec.android.app.calculator",
            "com.android.calculator2",
            "com.google.android.calculator",
            "com.htc.calculator",
            "com.htc.android.fusion.calculator",
            "com.digitalchemy.calculator.freedecimal",
            "uk.co.nickfines.RealCalc",
            "my.android.calc",
            "alhazmy.Balot_Calculator",
            "de.underflow.calc",
            "com.apalon.calculator.gp",
            "de.sfr.calctape",
            "com.pantech.app.calculator",
            "com.visionobjects.calculator",
            "com.digitalchemy.calculator.decimal",
            "com.scaleasw.powercalc",
            "com.kingcalculator",
            "com.asus.calculator",
            "uk.co.nickfines.RealCalcPlus",
            "org.solovyev.android.calculator",
            "com.balotCalculator",
            "com.roamingsquirrel.android.calculator",
            "net.taobits.officecalculator.android.pro",
            "net.taobits.officecalculator.android",
            "com.android2.calculator3",
            "com.scientificCalculator",
            "de.xipa.calc",
            "com.mathally.calculator.free",
            "us.mathlab.android.calc.edu",
            "com.lu.calculator",
            "robi.calculator",
            "com.skf.calculator",
            "com.lenovo.calculator",
            "cn.nubia.calculator2.preset",
            "com.meizu.flyme.calculator",
            "com.htc.calculator.widget",
            "com.htc.android.calculator.widget",
            "com.pantech.app.skyengcalculator",
            "com.android.bbkcalculator",
            "com.kttech.calculator",
            "com.meng.calculator",
            "com.android.g3_calculator2",
            "jp.co.sharp.android.calc",
            "com.huaqin.mycalculator",
            "com.phicomm.calculator2",
            "com.hamzahrmalik.calculator2",
            "com.pantech.app.aotcalculator",
            "com.ape.calculator2",
            "com.android.calculator",
            "com.tyd.calculator",
            "org.fiui.calculator",
            "com.wingtech.calc",
            "android.calculator",
            "com.hyunkyo.calcul.electron01",
            "com.mobiroo.n.sourcenextcorporation.supercalc",
            "com.hskj.iphonecalculator",
            "com.easyandroid.calculator2",
            "com.numix.calculator",
            "org.mmin.handycalc",
            "com.quinny898.app.cmcalc",
            "my.android.procalc",
            "com.inturi.net.android.TimberAndLumberCalc",
            "com.dencreak.dlcalculator",
            "com.spectensys.calculatorfree",
            "info.woodsmall.calculator",
            "com.americos.calcoid",
            "com.ids.smartcalculator",
            "mobi.appplus.calculator.plus",
            "com.prettypet.google.prettycalc",
            "com.moneta.android.monetacalculator",
            "com.realmax.calc",
            "com.andanapps.app.calc",
            "com.mathsolver.calc",
            "com.msj.easycalcpro",
            "com.digitalchemy.calculator.freefraction",
            "ru.arybin.calculator",
            "app.kwc.math.totalcalc",
            "calculator.widget.various.themes",
            "com.ibox.calculators",
            "com.candl.athena",
            "jp.Appsys.PanecalST",
            "com.developstudios.casio",
            "us.mathlab.android",
            "com.best_calc",
            "com.arget.calc",
            "com.vpn.basiccalculator",
            "com.blogspot.turbocolor.magma_calc",
            "net.tecnotopia.SimpleCalculator",
            "com.n225zero.ColorfulCalc",
            "jp.ne.kutu.Panecal",
            "com.mdroidapps.mycalc",
            "net.camijun.CamiCalc",
            "com.merowoo.genericcalculator",
            "com.speedsoftware.allin1calcfree",
            "com.team.greenfire.calculator_materialdesign",
            "com.rahul.material.calculator",
            "com.tricolorcat.calculator",
            "com.numix.calculator_pro",
            "home.solo.plugin.calculator",
            "com.americos.calcoidplus",
            "com.jee.calc",
            "com.nadinestudio.scientificcalculator",
            "com.moblynx.calculatorjb",
            "com.smartisanos.calculator",
            "com.apalon.calculator",
            "com.niks.calc",
            "View.CalCollection.SangGeon.Cauly",
            "com.aeustech.wearrotarycalculator",
    };

    private static AppInfoHolder instance;

    private List<String> homes = new ArrayList<String>();
    private List<String> cameras = new ArrayList<String>();
    private List<String> dialpads = new ArrayList<String>();

    private String calculatorPackageName;
    private String calculatorActivityName;

    public static void init(Context context) {
        if (instance == null) {
            instance = new AppInfoHolder(context);
        }
    }

    public static AppInfoHolder getInstance() {
        return instance;
    }

    private AppInfoHolder(final Context context) {
        PackageManager packageManager = context.getPackageManager();

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            homes.add(info.activityInfo.packageName);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        resolveInfo = packageManager.queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            cameras.add(info.activityInfo.packageName);
        }

        Intent dialpadIntent = new Intent(Intent.ACTION_DIAL);
        dialpadIntent.setData(Uri.parse("tel:"));
        resolveInfo = packageManager.queryIntentActivities(dialpadIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            dialpads.add(info.activityInfo.packageName);
        }

        calculatorPackageName = HSPreferenceHelper.getDefault().getString(PREF_CALCULATOR_PACKAGE_NAME, "");
        calculatorActivityName = HSPreferenceHelper.getDefault().getString(PREF_CALCULATOR_ACTIVITY_NAME, "");
        if (TextUtils.isEmpty(calculatorPackageName) || TextUtils.isEmpty(calculatorActivityName)) {
            Threads.postOnThreadPoolExecutor(new Runnable() {

                @Override
                public void run() {
                    for (String packageName : CALCULATOR_PACKAGE_NAMES) {
                        if (Utils.isIntentExist(context, getCalculatorIntent(packageName, ""))) {
                            ResolveInfo info = context.getPackageManager().resolveActivity(getCalculatorIntent(packageName, ""), 0);
                            calculatorPackageName = info.activityInfo.packageName;
                            calculatorActivityName = info.activityInfo.name;
                            HSPreferenceHelper.getDefault().putString(PREF_CALCULATOR_PACKAGE_NAME, calculatorPackageName);
                            HSPreferenceHelper.getDefault().putString(PREF_CALCULATOR_ACTIVITY_NAME, calculatorActivityName);
                            break;
                        }
                    }
                }
            });
        }
    }

    public boolean isLauncherApp(String packageName) {
        return homes.contains(packageName);
    }

    public boolean isCameraApp(String packageName) {
        return cameras.contains(packageName);
    }

    public boolean isDialpadApp(String packageName) {
        return dialpads.contains(packageName);
    }

    private Intent getCalculatorIntent(String packageName, String activityName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activityName)) {
            intent.setClassName(packageName, activityName);
        } else if (TextUtils.isEmpty(packageName) || CALCULATOR_PACKAGE_NAME.equals(packageName)) {
            intent.setComponent(new ComponentName(CALCULATOR_PACKAGE_NAME, CALCULATOR_CLASS_NAME));
        } else {
            intent.setPackage(packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public Intent getCalculatorIntent() {
        return getCalculatorIntent(calculatorPackageName, calculatorActivityName);
    }

    public boolean isCalculatorApp(String packageName) {
        if (TextUtils.isEmpty(calculatorPackageName)) {
            return CALCULATOR_PACKAGE_NAME.equals(packageName);
        } else {
            return calculatorPackageName.equals(packageName);
        }
    }

    public static Intent getIntent(ComponentName component) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (CHROME_PACKAGE_NAME.equals(component.getPackageName())) {
            intent.setPackage(CHROME_PACKAGE_NAME);
        } else {
            intent.setComponent(component);
        }
        return intent;
    }
}
