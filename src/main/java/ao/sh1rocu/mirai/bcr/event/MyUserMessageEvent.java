/*
 * Copyright (C) 2022 Sh1roCu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ao.sh1rocu.mirai.bcr.event;

import ao.sh1rocu.mirai.bcr.util.Calendar;
import ao.sh1rocu.mirai.bcr.util.SearchUPer;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 用户（好友，群员）发送消息事件
 * @author ShiroCu
 */
public class MyUserMessageEvent implements Consumer<UserMessageEvent> {
    @Override
    public void accept(UserMessageEvent event) {
        String cmd = Objects.requireNonNull(event.getMessage().get(PlainText.Key)).toString().toLowerCase();
        if (cmd.contains("/uid ")) {
            String uid = cmd.split(" ")[1];
            event.getSubject().sendMessage("开始查询UID为" + uid + "的UP主的信息...");
            SearchUPer.searchUPer(uid, event.getSubject());
        }
        switch (cmd) {
            case "/rank":
                event.getSubject().sendMessage("rank表链接：\nhttps://docs.qq.com/sheet/DYWxDbGdRYWV1bHFv?tab=6lwsa");
                break;
            case "/活动":
                event.getSubject().sendMessage("正在查询本月活动...");
                String calendar = Calendar.getCalendar();
                if (!calendar.equals("error")) {
                    event.getSubject().sendMessage("本月活动如下：\n" + calendar);
                } else {
                    event.getSubject().sendMessage("获取活动失败");
                }
                break;
            default:
                break;
        }
    }
}
