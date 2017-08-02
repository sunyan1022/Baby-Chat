package com.hyphenate.easeui.shortcutbadger.impl;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.hyphenate.easeui.shortcutbadger.Badger;
import com.hyphenate.easeui.shortcutbadger.ShortcutBadgeException;

import java.util.Collections;
import java.util.List;

/**
 * Description: Shortcut Badger support for huawei Launcher.
 *
 * @author Li Jianying
 * @version 2.0
 * @since 2016-10-18
 */
public class HuaweiHomeBadger implements Badger {

    private static final String LOG_TAG = HuaweiHomeBadger.class.getSimpleName();

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {
        String launcherClassName = componentName.getClassName();
        if (launcherClassName == null) {
            Log.d(LOG_TAG, "Main activity is null");
            return;
        }
        Bundle localBundle = new Bundle();
        localBundle.putString("package", context.getPackageName());
        localBundle.putString("class", launcherClassName);
        localBundle.putInt("badgenumber", badgeCount);
        context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, localBundle);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList(
                "com.huawei.android.launcher"
        );
    }
}
