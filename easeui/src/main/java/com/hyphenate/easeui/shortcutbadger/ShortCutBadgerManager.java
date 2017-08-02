package com.hyphenate.easeui.shortcutbadger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.hyphenate.easeui.shortcutbadger.impl.AdwHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.ApexHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.AsusHomeLauncher;
import com.hyphenate.easeui.shortcutbadger.impl.DefaultBadger;
import com.hyphenate.easeui.shortcutbadger.impl.HuaweiHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.NewHtcHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.NovaHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.SonyHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.VivoHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.XiaomiHomeBadger;
import com.hyphenate.easeui.shortcutbadger.impl.ZukHomeBadger;

import java.util.ArrayList;
import java.util.List;

/**
 * 图标消息实现类
 */
public final class ShortCutBadgerManager {

    private static final String LOG_TAG = "ShortCutBadgerManager";

    private static volatile ShortCutBadgerManager instance;

    private static Badger currentAppShortcutBadger;

    private static ComponentName currentAppComponentName;

    private Context currentContext;

    private boolean launcherReady;

    /**
     * 可以实现添加应用消息数的launcher集合
     */
    private static final List<Class<? extends Badger>> supportHomeBadgers = new ArrayList<>();

    private ShortCutBadgerManager(Context context) {

        currentContext = context.getApplicationContext();

        supportHomeBadgers.add(AdwHomeBadger.class);
        supportHomeBadgers.add(ApexHomeBadger.class);
        supportHomeBadgers.add(NewHtcHomeBadger.class);
        supportHomeBadgers.add(NovaHomeBadger.class);
        supportHomeBadgers.add(SonyHomeBadger.class);
        supportHomeBadgers.add(XiaomiHomeBadger.class);
        supportHomeBadgers.add(AsusHomeLauncher.class);
        supportHomeBadgers.add(HuaweiHomeBadger.class);
        supportHomeBadgers.add(ZukHomeBadger.class);
        supportHomeBadgers.add(VivoHomeBadger.class);

        launcherReady = initBadger();
    }

    public static ShortCutBadgerManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ShortCutBadgerManager.class) {
                if (instance == null) {
                    instance = new ShortCutBadgerManager(context);
                }
            }
        }
        return instance;
    }


    /**
     * 初始化对当前应用launcher对应的Badger
     *
     * @return true  获取到了对应图标的消息提醒处理类，可以实现消息数提醒, false 没有找到，不能实现应用图标消息数
     */
    private boolean initBadger() {

        currentAppComponentName = currentContext.getPackageManager().getLaunchIntentForPackage(currentContext.getPackageName()).getComponent();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = currentContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo == null || resolveInfo.activityInfo.name.toLowerCase().contains("resolver"))
            return false;

        String currentHomePackage = resolveInfo.activityInfo.packageName;

        for (Class<? extends Badger> badger : supportHomeBadgers) {
            Badger shortcutBadger;
            try {
                shortcutBadger = badger.newInstance();
            } catch (Exception e) {
                return false;
            }
            if (shortcutBadger != null && shortcutBadger.getSupportLaunchers().contains(currentHomePackage)) {
                currentAppShortcutBadger = shortcutBadger;
                break;
            }
        }

        if (currentAppShortcutBadger == null) {
            if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                currentAppShortcutBadger = new XiaomiHomeBadger();
            } else if (Build.MANUFACTURER.equalsIgnoreCase("ZUK") || Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
                currentAppShortcutBadger = new ZukHomeBadger();
            } else if (Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                currentAppShortcutBadger = new VivoHomeBadger();
            } else {
                currentAppShortcutBadger = new DefaultBadger();
            }
        }

        return true;
    }

    /**
     * 移除应用图标消息提醒数
     *
     * @return rue 应用成功, false 失败
     */
    public boolean removeCount() {
        return applyCount(0);
    }

    /**
     * 更新通知数
     *
     * @param badgeCount 想要的消息提示数
     * @return true 应用成功, false 失败
     */
    public boolean applyCount(int badgeCount) {
        try {
            applyCountOrThrow(badgeCount);
            return true;
        } catch (ShortcutBadgeException e) {
            Log.e(LOG_TAG, "Unable to execute badge", e);
            return false;
        }
    }

    /**
     * 更新通知数, 失败时抛出  {@link ShortcutBadgeException}
     *
     * @param badgeCount 想要的消息提示数
     */
    private void applyCountOrThrow(int badgeCount) throws ShortcutBadgeException {
        if (currentAppShortcutBadger == null && !launcherReady) {
            throw new ShortcutBadgeException("No default launcher available");
        }

        try {
            if (currentAppShortcutBadger != null) {
                currentAppShortcutBadger.executeBadge(currentContext, currentAppComponentName, badgeCount);
            }
        } catch (Exception e) {
            throw new ShortcutBadgeException("Unable to execute badge", e);
        }
    }

}
