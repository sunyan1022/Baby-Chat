package com.hyphenate.easeui.shortcutbadger;

import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public interface Badger {

    /**
     * 当更新通知数时将会调用
     *
     * @param context       调用者的 context
     * @param componentName 调用应用的ComponentName 包含包名和类名相关信息
     * @param badgeCount    想要显示的图标消息提醒数量
     * @throws ShortcutBadgeException
     */
    void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException;

    /**
     * 调用的目的是让ShortcutBadger知道是哪种手机l的launchers支持哪种badger实现
     *
     * @return 支持能够创建应用消息提醒的launchers的包名集合
     */
    List<String> getSupportLaunchers();
}
