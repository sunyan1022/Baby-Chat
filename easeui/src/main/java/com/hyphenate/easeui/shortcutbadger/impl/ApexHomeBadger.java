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
 * Description: Shortcut Badger support for apex Launcher.
 *
 * @author Li Jianying
 * @version 2.0
 * @since 2016-10-18
 */
public class ApexHomeBadger implements Badger {

    private static final String INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED";
    private static final String PACKAGE_NAME = "package";
    private static final String COUNT = "count";
    private static final String CLASS = "class";

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {

        Intent intent = new Intent(INTENT_UPDATE_COUNTER);
        intent.putExtra(PACKAGE_NAME, componentName.getPackageName());
        intent.putExtra(COUNT, badgeCount);
        intent.putExtra(CLASS, componentName.getClassName());
        if (BroadcastHelper.canResolveBroadcast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            throw new ShortcutBadgeException("unable to resolve intent: " + intent.toString());
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList("com.anddoes.launcher");
    }
}
