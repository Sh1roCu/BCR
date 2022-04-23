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

package ao.sh1rocu.mirai.bcr.util;

import ao.sh1rocu.mirai.bcr.BCRMain;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.contact.Group;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 公会战信息查询
 *
 * @author Sh1roCu
 */
public class ClanBattleInfoSearch {
    /**
     * The Clan battle api.
     */
    public static Map<String, String> CLAN_BATTLE_API = new HashMap<String, String>() {
        {
            put("clan_report", "https://www.bigfun.cn/api/feweb?target=gzlj-clan-collect-report/a");
            put("clan_day_report", "https://www.bigfun.cn/api/feweb?target=gzlj-clan-day-report-collect/a");
            put("clan_day_timeline_report", "https://www.bigfun.cn/api/feweb?target=gzlj-clan-day-timeline-report/a");
        }
    };

    /**
     * The Clan battle cfg.
     */
    static final File CLAN_BATTLE_CFG = new File(BCRMain.INSTANCE.getConfigFolderPath() + "\\ClanBattle.json");

    /**
     * 读取config中的cookie
     *
     * @param group 群组
     * @return 返回cookie
     */
    private static String getCookie(Group group) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(CLAN_BATTLE_CFG), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        while ((temp = bufferedReader.readLine()) != null) {
            stringBuilder.append(temp);
        }
        bufferedReader.close();
        streamReader.close();
        String json = stringBuilder.toString();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonElement jsonElement = jsonObject.get(String.valueOf(group.getId()));
        if (jsonElement == null) {
            return "error";
        } else {
            return jsonElement.getAsString();
        }
    }

    /**
     * 获取json
     *
     * @param api   API链接
     * @param group 群组
     * @return 返回JsonString
     */
    public static String getJsonResource(String api, Group group) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(api);
            /*获取cookie*/
            String cookie = getCookie(group);
            if ("error".equals(cookie)) {
                return "error";
            }
            httpGet.setHeader("Cookie", cookie);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String resource = EntityUtils.toString(entity);
            response.close();
            httpClient.close();
            return resource;
        } catch (IOException e) {
            BCRMain.INSTANCE.getLogger().error("查询失败，请检查网络");
            return "error";
        } catch (ParseException e) {
            BCRMain.INSTANCE.getLogger().error("http解析失败");
            return "error";
        }
    }

    /**
     * 解析json
     *
     * @param resource JsonString
     * @return 返回解析后的Json对象
     */
    public static JsonObject parseJson(String resource) {
        try {
            JsonElement jsonElement = JsonParser.parseString(resource);
            return jsonElement.getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取上次公会战的排名信息
     *
     * @param group 群组
     * @return the last ranking info
     */

    public static String getLastRankingInfo(Group group) {
        String resource = getJsonResource(CLAN_BATTLE_API.get("clan_report"), group);
        if ("error".equals(resource)) {
            return "查询失败，请联系bot管理员填写配置文件的cookie";
        }
        JsonObject data = parseJson(resource);
        if (data != null) {
            if (data.get("code").getAsInt() == 401) {
                return "查询失败，cookie已过期，请联系bot管理员更新配置文件";
            }
            if (data.get("data") == null) {
                return "查询失败，该群设置的bigfun账号没有加入行会，请联系bot管理员更换cookie";
            }
            StringBuilder stringBuilder = new StringBuilder();
            JsonObject clanInfo = data.get("data").getAsJsonObject().get("clan").getAsJsonObject();
            JsonObject lastInfo = clanInfo.get("all_ranking").getAsJsonArray().get(0).getAsJsonObject();
            return stringBuilder.append("公会名：").append(clanInfo.get("name").getAsString()).append("\n")
                    .append("最近一次公会战的信息-->\n")
                    .append(" ").append(lastInfo.get("month").getAsString()).append("月")
                    .append(lastInfo.get("clan_battle_name").getAsString()).append("：\n")
                    .append("  排名：").append(clanInfo.get("last_ranking").getAsInt()).append("\n")
                    .append("  最新公会等级：").append(clanInfo.get("last_total_ranking").getAsString())
                    .toString();
        } else {
            return "未找到数据";
        }
    }

    /**
     * 获取上次公会战的出刀数据
     *
     * @param group 群组
     * @return the last attack info
     */

    public static String getLastAttackInfo(Group group) {
        String resource = getJsonResource(CLAN_BATTLE_API.get("clan_report"), group);
        if ("error".equals(resource)) {
            return "查询失败，请联系bot管理员填写配置文件的cookie";
        }
        JsonObject data = parseJson(resource);
        if (data != null) {
            if (data.get("code").getAsInt() == 401) {
                return "查询失败，cookie已过期，请联系bot管理员更新配置文件";
            }
            if (data.get("data") == null) {
                return "查询失败，该群设置的bigfun账号没有加入行会，请联系bot管理员更换cookie";
            }
            StringBuilder stringBuilder = new StringBuilder();
            JsonArray attack_info = data.get("data").getAsJsonObject().get("data").getAsJsonArray();
            stringBuilder.append("----------------------------\n\n");
            int counter = 1;
            for (JsonElement info : attack_info) {
                JsonObject member_info = info.getAsJsonObject();
                stringBuilder.append(counter++).append(".").append(member_info.get("username").getAsString())
                        .append("-->\n")
                        .append("出刀数：")
                        .append(member_info.get("number").getAsInt())
                        .append(" ").append("总伤害：")
                        .append(member_info.get("damage").getAsInt())
                        .append(" ").append("总分数：")
                        .append(member_info.get("score").getAsInt())
                        .append(" ").append("伤害占比：")
                        .append(member_info.get("rate").getAsString())
                        .append("\n\n");
            }
            stringBuilder.append("----------------------------");
            return stringBuilder.toString();
        } else {
            return "未找到数据";
        }
    }

    /**
     * 获取往期所有排名信息
     *
     * @param group 群组
     * @return the clan all ranking
     */

    public static String getClanAllRanking(Group group) {
        String resource = getJsonResource(CLAN_BATTLE_API.get("clan_report"), group);
        if ("error".equals(resource)) {
            return "查询失败，请联系bot管理员填写配置文件的cookie";
        }
        JsonObject data = parseJson(resource);
        if (data != null) {
            if (data.get("code").getAsInt() == 401) {
                return "查询失败，cookie已过期，请联系bot管理员更新配置文件";
            }
            if (data.get("data") == null) {
                return "查询失败，该群设置的bigfun账号没有加入行会，请联系bot管理员更换cookie";
            }
            JsonArray allRankingInfo = data.get("data").getAsJsonObject()
                    .get("clan").getAsJsonObject()
                    .get("all_ranking").getAsJsonArray();
            int count = allRankingInfo.size();
            StringBuilder stringBuilder = new StringBuilder();
            /*bigfun没有前5期的name数据*/
            stringBuilder.append(data.get("data").getAsJsonObject().get("clan").getAsJsonObject()
                            .get("name").getAsString()).append("：\n")
                    .append("第1期-->2020年5月白羊座：").append(allRankingInfo.get(count - 1).getAsJsonObject().get("ranking")).append("名\n")
                    .append("第2期-->2020年6月金牛座：").append(allRankingInfo.get(count - 2).getAsJsonObject().get("ranking")).append("名\n")
                    .append("第3期-->2020年6月底双子座：").append(allRankingInfo.get(count - 3).getAsJsonObject().get("ranking")).append("名\n")
                    .append("第4期-->2020年7月巨蟹座: ").append(allRankingInfo.get(count - 4).getAsJsonObject().get("ranking")).append("名\n")
                    .append("第5期-->2020年8月狮子座：").append(allRankingInfo.get(count - 5).getAsJsonObject().get("ranking")).append("名\n");
            int counter = 6;
            int year = 2020;
            for (int i = count - 6; i >= 0; i--) {
                JsonObject ranking_info = allRankingInfo.get(i).getAsJsonObject();
                stringBuilder.append("第")
                        .append(counter++).append("期-->")
                        .append(year).append("年")
                        .append(ranking_info.get("month").getAsString()).append("月")
                        .append(ranking_info.get("clan_battle_name").getAsString()).append("：")
                        .append(ranking_info.get("ranking").getAsInt()).append("名");
                if (i != 0) {
                    stringBuilder.append("\n");
                }
                if (ranking_info.get("month").getAsInt() == 12) {
                    year++;
                }
            }
            return stringBuilder.toString();
        } else {
            return "未找到数据";
        }
    }

    /**
     * 获取boss状态
     *
     * @param group 群组
     * @return the boss status
     */

    public static String getBossStatus(Group group) {
        String resource = getJsonResource(CLAN_BATTLE_API.get("clan_day_report"), group);
        if ("error".equals(resource)) {
            return "查询失败，请联系bot管理员填写配置文件的cookie";
        }
        JsonObject data = parseJson(resource);
        if (data != null) {
            if (data.get("code").getAsInt() == 401) {
                return "查询失败，cookie已过期，请联系bot管理员更新配置文件";
            }
            if (data.get("data") == null) {
                return "查询失败，该群设置的bigfun账号没有加入行会，请联系bot管理员更换cookie";
            }
            if (!data.get("data").isJsonObject()) {
                return "查询失败，会战还未开始";
            }
            StringBuilder stringBuilder = new StringBuilder();
            JsonObject info = data.get("data").getAsJsonObject();
            JsonObject bossInfo = info.get("bossInfo").getAsJsonObject();
            return stringBuilder.append("当前BOSS状态-->\n")
                    .append("第").append(bossInfo.get("lap_num").getAsInt()).append("周目-")
                    .append(bossInfo.get("name").getAsString()).append("[").append(bossInfo.get("current_life").getAsString())
                    .append("/").append(bossInfo.get("total_life").getAsString()).append("]")
                    .toString();
        } else {
            return "未找到数据";
        }
    }
}
