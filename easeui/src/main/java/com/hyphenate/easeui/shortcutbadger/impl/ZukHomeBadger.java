package com.hyphenate.easeui.shortcutbadger.impl;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.hyphenate.easeui.shortcutbadger.Badger;
import com.hyphenate.easeui.shortcutbadger.ShortcutBadgeException;

import java.util.Collections;
import java.util.List;

/**
 * Description: Shortcut Badger support for Zuk Launcher.
 * 需再设置 -- 通知和状态栏 -- 应用角标管理 中开启应用
 * 同样可以设置oppo的部分机型
 *
 * @author Li Jianying
 * @version 2.0
 * @since 2016-10-18
 */

public class ZukHomeBadger implements Badger {

    private final Uri CONTENT_URI = Uri.parse("content://com.android.badge/badge");

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount)
            throws ShortcutBadgeException {
        Bundle extra = new Bundle();
        extra.putInt("app_badge_count", badgeCount);
        context.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
    }

    @Override
    public List<String> getSupportLaunchers() {
        //TODO  添加oppo 手机的 报名
        return Collections.singletonList("com.zui.launcher");
    }
}
