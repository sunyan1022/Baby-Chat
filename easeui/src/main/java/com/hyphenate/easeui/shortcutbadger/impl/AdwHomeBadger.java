package com.hyphenate.easeui.shortcutbadger.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hyphenate.easeui.shortcutbadger.Badger;
import com.hyphenate.easeui.shortcutbadger.BroadcastHelper;
import com.hyphenate.easeui.shortcutbadger.ShortcutBadgeException;

import java.util.Arrays;
import java.util.List;

/**
 * Description: Shortcut Badger support for adw Launcher.
 *
 * @author Li Jianying
 * @version 2.0
 * @since 2016-10-18
 */
public class AdwHomeBadger implements Badger {

    private static final String INTENT_UPDATE_COUNTER = "org.adw.launcher.counter.SEND";
    private static final String PACKAGE_NAME = "PNAME";
    private static final String CLASSNAME = "CNAME";
    private static final String COUNT = "COUNT";

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {

        Intent intent = new Intent(INTENT_UPDATE_COUNTER);
        intent.putExtra(PACKAGE_NAME, componentName.getPackageName());
        intent.putExtra(CLASSNAME, componentName.getClassName());
        intent.putExtra(COUNT, badgeCount);
        if(BroadcastHelper.canResolveBroadcast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            throw new ShortcutBadgeException("unable to resolve intent: " + intent.toString());
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "org.adw.launcher",
                "org.adwfreak.launcher"
        );
    }
}
