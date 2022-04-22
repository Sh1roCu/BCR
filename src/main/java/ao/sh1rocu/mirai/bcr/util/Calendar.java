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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 活动月历
 *
 * @author Sh1roCu
 */
public class Calendar {

    /**
     * 获取活动表
     *
     * @return 活动表的字符串形式
     */
    public static String getCalendar() {
        String calendar = "暂无活动";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet("https://pcrbot.github.io/calendar-updater-action/cn.json");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String resource = EntityUtils.toString(entity);
            if (response.getCode() == 200) {
                BCRMain.INSTANCE.getLogger().info("read json successfully");
                calendar = parseJson(resource);
            } else {
                BCRMain.INSTANCE.getLogger().info("error:" + response.getCode());
            }
            response.close();
            httpClient.close();
        } catch (IOException e) {
            BCRMain.INSTANCE.getLogger().error("连接失败，请检查网络是否正常");
            return "error";
        } catch (ParseException e) {
            BCRMain.INSTANCE.getLogger().error("解析出错");
            return "error";
        }
        return calendar;
    }

    /**
     * 解析JSON
     *
     * @param jsonString JSON String
     * @return 解析完成后的指定格式的活动表字符串
     */
    public static String parseJson(String jsonString) {
        JsonElement json = JsonParser.parseString(jsonString);
        int i = 0;
        StringBuilder info = new StringBuilder();
        if (json.isJsonArray()) {
            while (i < json.getAsJsonArray().size()) {
                String nowDate = new SimpleDateFormat("yyyy/MM").format(new Date());
                JsonObject jsonObject = json.getAsJsonArray().get(i).getAsJsonObject();
                String startTime = jsonObject.get("start_time").getAsString();
                if (startTime.contains(nowDate)) {
                    info.append(jsonObject.get("name").getAsString())
                            .append("-->开始时间：")
                            .append(startTime)
                            .append(" 结束时间：")
                            .append(jsonObject.get("end_time").getAsString())
                            .append("\n");
                }
                i++;
            }
        }
        return info.toString().isEmpty() ? "暂无活动" : info.toString();
    }
}

