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
import ao.sh1rocu.mirai.bcr.util.ClanBattleInfoSearch;
import ao.sh1rocu.mirai.bcr.util.SearchUPer;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.PlainText;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * 群组消息事件
 * @author Sh1roCu
 */
public class MyGroupMessageEvent implements Consumer<GroupMessageEvent> {
    @Override
    public void accept(GroupMessageEvent event) {
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
                if (!"error".equals(calendar)) {
                    event.getSubject().sendMessage("本月活动如下：\n" + calendar);
                } else {
                    event.getSubject().sendMessage("获取活动失败");
                }
                break;
            case "/排名":
                event.getSubject().sendMessage("正在查询最近一次公会战的排名...");
                String ranking = ClanBattleInfoSearch.getLastRankingInfo(event.getSubject());
                event.getSubject().sendMessage(ranking);
                break;
            case "/出刀信息":
                event.getSubject().sendMessage("正在查询最近一次公会战的出刀信息...");
                String attack = ClanBattleInfoSearch.getLastAttackInfo(event.getSubject());
                event.getSubject().sendMessage(attack);
                break;
            case "/boss":
                event.getSubject().sendMessage("正在查询当前BOSS的状态...");
                String bossStatus = ClanBattleInfoSearch.getBossStatus(event.getSubject());
                event.getSubject().sendMessage(bossStatus);
                break;
            case "/往期排名":
                event.getSubject().sendMessage("正在查询往期公会战排名...");
                String allRanking = ClanBattleInfoSearch.getClanAllRanking(event.getSubject());
                event.getSubject().sendMessage(allRanking);
                break;
            default:
                break;

        }
    }
}
