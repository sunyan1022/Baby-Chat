package com.hyphenate.easeui.shortcutbadger.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hyphenate.easeui.shortcutbadger.Badger;
import com.hyphenate.easeui.shortcutbadger.BroadcastHelper;
import com.hyphenate.easeui.shortcutbadger.ShortcutBadgeException;

import java.util.Collections;
import java.util.List;

/**
 * Description: Shortcut Badger support for vivo Launcher.
 *
 * @author Li Jianying
 * @version 2.0
 * @since 2016-10-18
 */

public class VivoHomeBadger implements Badger {

    private static final String INTENT_ACTION = "launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM";

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {

        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra("packageName", componentName.getPackageName());
        intent.putExtra("className", componentName.getClassName());
        intent.putExtra("notificationNum", badgeCount);
        if (BroadcastHelper.canResolveBroadcast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            throw new ShortcutBadgeException("unable to resolve intent: " + intent.toString());
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList(
                "com.bbk.launcher2"
        );
    }
}
