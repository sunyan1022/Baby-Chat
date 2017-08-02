package com.hyphenate.easeui.shortcutbadger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Description: 广播解析助手类
 *
 * @author Li Jianying
 * @version 2.0
 * @since 17/05/16
 */
public class BroadcastHelper {
    public static boolean canResolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
        return receivers != null && receivers.size() > 0;
    }
}
