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
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 查询UP主信息
 *
 * @author Sh1roCu
 */
public class SearchUPer {
    /**
     * The Headers.
     */
    static List<Header> headers = new ArrayList<Header>() {
        {
            add(new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36"));
        }
    };

    /**
     * 查询UP主信息
     *
     * @param uid    the uid
     * @param sender the sender
     */
    public static void searchUPer(String uid, Contact sender) {
        try {
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultHeaders(headers)
                    .build();
            HttpGet httpGet = new HttpGet("https://api.bilibili.com/x/relation/stat?vmid=" + uid);  //返回关注和粉丝数
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String resource = EntityUtils.toString(entity);
            JsonElement json = JsonParser.parseString(resource);
            JsonObject jsonObject = json.getAsJsonObject();
            StringBuilder stringBuilder = new StringBuilder();
            if (jsonObject.get("code").getAsInt() != 0) {
                sender.sendMessage("不存在该用户");
            } else {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                HttpGet httpGet2 = new HttpGet("https://api.bilibili.com/x/space/acc/info?mid=" + uid + "&" + "jsonp=jsonp");
                CloseableHttpResponse response2 = httpClient.execute(httpGet2);
                JsonElement json2 = JsonParser.parseString(EntityUtils.toString(response2.getEntity()));
                JsonObject data2 = json2.getAsJsonObject().get("data").getAsJsonObject();
                String live_room = "未开通直播间";
                String live_status = "未开播";
                String title = "";
                String tagString = "";
                StringBuilder str = new StringBuilder();
                if (data2.get("live_room").isJsonObject()) {
                    JsonObject live_info = data2.get("live_room").getAsJsonObject();
                    title = live_info.get("title").getAsString();
                    if (live_info.get("roomStatus").getAsInt() == 1) {
                        live_room =
                                "https://live.bilibili.com/" + live_info.get("roomid").getAsInt();
                    }
                    if (live_info.get("liveStatus").getAsInt() == 1) {
                        live_status = "已开播";
                    }
                }
                if (data2.get("tags").isJsonArray()) {
                    JsonArray tags = data2.get("tags").getAsJsonArray();
                    for (JsonElement tag : tags) {
                        str.append(tag.getAsString()).append(" ");
                    }
                }
                tagString = str.toString();
                live_status = live_status.equals("未开播") ? "未开播" : live_status + "-->直播标题：" + title;
                String faceUrl = data2.get("face").getAsString();
                /*获取头像*/
                HttpGet faceGet = new HttpGet(faceUrl);
                CloseableHttpResponse faceResponse = httpClient.execute(faceGet);
                Image face = getFace(faceUrl, faceResponse, uid, sender);
                faceResponse.close();
                String info = stringBuilder
                        .append("UID：" + uid + "\n")
                        .append("昵称：" + data2.get("name").getAsString() + "\n")
                        .append("等级：" + data2.get("level").getAsInt() + "\n")
                        .append("(资料)性别：" + data2.get("sex").getAsString() + "\n")
                        .append("(资料)生日：" + data2.get("birthday").getAsString() + "\n")
                        .append("(资料)个人简介：" + data2.get("sign").getAsString() + "\n")
                        .append("已关注人数：" + data.get("following").getAsInt() + "\n")
                        .append("粉丝数：" + data.get("follower").getAsInt() + "\n")
                        .append("直播间：" + live_room + "\n")
                        .append("开播状态：" + live_status + "\n")
                        .append("个人tag：" + tagString)
                        .toString();
                sender.sendMessage(face.plus("\n").plus(info));
                response2.close();
            }
            response.close();
            httpClient.close();
        } catch (IOException | ParseException e) {
            BCRMain.INSTANCE.getLogger().warning("解析失败");
        }
    }

    /**
     * 获取头像
     * @param faceUrl 头像的url
     * @param faceResponse api返回的响应
     * @param uid UP主的UID
     * @param sender 群组或用户（好友，群成员
     *               @return 返回头像
     */
    private static Image getFace(String faceUrl, CloseableHttpResponse faceResponse, String uid, Contact sender) throws IOException {
        /*分割获取图片类型*/
        String[] splited = faceUrl.split("\\.");
        String faceType = splited[splited.length - 1];
        byte[] faceData = EntityUtils.toByteArray(faceResponse.getEntity());
        File file = new File(BCRMain.INSTANCE.getDataFolderPath() + "\\UPerFaceCache\\" + uid + "." + faceType);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(faceData);
        ExternalResource externalResource = ExternalResource.create(file);
        Image face = Contact.uploadImage(sender, externalResource);
        externalResource.close();
        stream.close();
        return face;
    }
}
