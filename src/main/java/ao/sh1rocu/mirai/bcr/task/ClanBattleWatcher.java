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

package ao.sh1rocu.mirai.bcr.task;

import ao.sh1rocu.mirai.bcr.util.ClanBattleInfoSearch;
import com.google.gson.JsonObject;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;

/**
 * 监听公会战
 *
 * @author Sh1roCu
 */
public class ClanBattleWatcher extends TimerTask {
    /**
     * 暂存出刀信息的时间戳
     */
    static Map<Long, Long> damage_time_stamp = new HashMap<>();

    static final Map<String, String> API = ClanBattleInfoSearch.CLAN_BATTLE_API;

    @Override
    public void run() {
        if (!Bot.getInstances().isEmpty()) {
            for (Bot bot : Bot.getInstances()) {
                if (!bot.getGroups().isEmpty()) {
                    for (Group group : bot.getGroups()) {
                        if (getJson(API.get("clan_day_timeline_report"), group) != null) {
                            JsonObject check = getJson(API.get("clan_day_timeline_report"), group);
                            if (Objects.requireNonNull(check).get("data")!=null) {
                                if (Objects.requireNonNull(check).get("data").getAsJsonObject()
                                        .get("list").isJsonArray()) {
                                    /*获取最新一位成员的出刀信息*/
                                    JsonObject damageInfo = Objects.requireNonNull(getJson(API.get("clan_day_timeline_report"), group)).get("data").getAsJsonObject()
                                            .get("list").getAsJsonArray().get(0).getAsJsonObject();
                                    long newDamageTimeStamp = damageInfo.get("datetime").getAsLong();
                                    if (!damage_time_stamp.containsKey(group.getId())) {
                                        damage_time_stamp.put(group.getId(), newDamageTimeStamp);
                                    } else if (damage_time_stamp.get(group.getId()) < newDamageTimeStamp) {
                                        /*检测到有成员出刀*/
                                        String damageMessage = damageInfo.get("name").getAsString() +
                                                "对[" +
                                                damageInfo.get("lap_num").getAsInt() +
                                                "周目]" +
                                                damageInfo.get("boss_name").getAsString() +
                                                "造成了" +
                                                damageInfo.get("damage").getAsLong() +
                                                "点伤害，获得了" +
                                                damageInfo.get("score").getAsLong() +
                                                "点分数";
                                        if (damageInfo.get("reimburse").getAsInt() == 1) {
                                            damageMessage += "[补偿刀]";
                                        }
                                        if (damageInfo.get("kill").getAsInt() == 1) {
                                            damageMessage += "并击破";
                                        }
                                        group.sendMessage(damageMessage);
                                        String bossStatus = ClanBattleInfoSearch.getBossStatus(group);
                                        group.sendMessage(bossStatus);
                                        /*更新时间戳*/
                                        damage_time_stamp.put(group.getId(), newDamageTimeStamp);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * 获得解析后的json
     *
     * @param api   API
     * @param group 群组
     * @return 返回JsonObject
     */
    private static JsonObject getJson(String api, Group group) {
        try {
            return ClanBattleInfoSearch.parseJson(ClanBattleInfoSearch.getJsonResource(api, group));
        } catch (Exception e) {
            return null;
        }
    }
}
