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

import ao.sh1rocu.mirai.bcr.BCRMain;
import com.google.gson.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
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
import java.util.TimerTask;

/**
 * 监听动态
 *
 * @author Sh1roCu
 */
public class DynamicWatcher extends TimerTask {
    /**
     * 暂存各个UP主最新动态的时间戳
     */
    static Map<String, Long> dynamic_timestamp = new HashMap<>();
    /**
     * Config File
     */
    static final File CONFIG = new File(BCRMain.INSTANCE.getConfigFolderPath() + "\\DynamicWatcher.json");

    @Override
    public void run() {
        JsonObject dataJson;
        try {
            dataJson = readConfig();
        } catch (JsonSyntaxException | IllegalStateException e) {
            BCRMain.INSTANCE.getLogger().error("DynamicWatcher.json配置文件格式错误，请修改");
            return;
        }
        if (!Bot.getInstances().isEmpty()) {
            for (Bot bot : Bot.getInstances()) {
                /*遍历uid*/
                for (String uid : dataJson.keySet()) {
                    try {
                        CloseableHttpClient client = HttpClients.createDefault();
                        /* need_top参数表示是否获取置顶动态 0不获取 1获取 使用0来保证获取最新动态 */
                        HttpGet httpGet = new HttpGet("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=" + uid + "&need_top=0");
                        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36");
                        CloseableHttpResponse response = client.execute(httpGet);
                        if (response.getCode() == 200) {
                            HttpEntity entity = response.getEntity();
                            JsonObject jsonObject = JsonParser.parseString(EntityUtils.toString(entity)).getAsJsonObject();
                            /*获取最新动态*/
                            JsonObject dynamic = jsonObject.get("data").getAsJsonObject().get("cards").getAsJsonArray().get(0).getAsJsonObject();
                            long newDynamicTimestamp = dynamic.get("desc").getAsJsonObject().get("timestamp").getAsLong();
                            if (!dynamic_timestamp.containsKey(uid)) {
                                dynamic_timestamp.put(uid, newDynamicTimestamp);
                            } else if (dynamic_timestamp.get(uid) < newDynamicTimestamp) {
                                /*更新时间戳*/
                                dynamic_timestamp.put(uid, newDynamicTimestamp);
                                if (!bot.getGroups().isEmpty()) {
                                    /*遍历群组*/
                                    for (Group group : bot.getGroups()) {
                                        if (dataJson.get(uid).getAsJsonObject().has(String.valueOf(group.getId()))) {
                                            String permission = dataJson.get(uid).getAsJsonObject().get(String.valueOf(group.getId())).getAsString();
                                            if ("on".equals(permission)) {
                                                /*发送动态*/
                                                try {
                                                    sendDynamic(group, dynamic);
                                                } catch (IOException e) {
                                                    BCRMain.INSTANCE.getLogger().warning("动态发送失败，请检查网络");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            entity.close();
                        } else {
                            BCRMain.INSTANCE.getLogger().warning("无效的UID");
                        }
                        response.close();
                        client.close();
                    } catch (IOException | ParseException e) {
                        BCRMain.INSTANCE.getLogger().warning("动态获取出错");
                    }
                }
            }
        }
    }

    /**
     * 发送动态
     */
    private static void sendDynamic(Group group, JsonObject dynamic) throws IOException {
        JsonObject info = JsonParser
                .parseString(dynamic.get("card").getAsString())
                .getAsJsonObject();
        JsonObject userInfo = dynamic.get("desc").getAsJsonObject().get("user_profile").getAsJsonObject().get("info").getAsJsonObject();
        /*获得up主头像*/
        Image face = getUPerFace(userInfo.get("face").getAsString(), group);
        MessageChainBuilder message = new MessageChainBuilder();
        JsonObject originJson, origin;
        switch (getDynamicType(dynamic)) {
            /*转发类型*/
            case 1:
                message.append(face.plus(userInfo.get("uname").getAsString()).plus("（转发）-->\n"))
                        .append(info.get("item").getAsJsonObject().get("content").getAsString())
                        .append("\n");
                /*获取原动态*/
                originJson = JsonParser.parseString(info.get("origin").getAsString()).getAsJsonObject();
                /*获取原动态类型*/
                switch (getOriginDynamicType(dynamic)) {
                                                         /* case 1: 嵌套转发仍提供最初动态的json数据
                                                                    无论被转发多少次，初始原动态都不可能为转发类型，即orig_type不可能为1
                                                            break;*/
                    /*带图片类型*/
                    case 2:
                        origin = originJson.get("item").getAsJsonObject();
                        String text = origin.get("description").getAsString();
                        MessageChain imageChain = getImagesMessage(origin.get("pictures").getAsJsonArray(), group);
                        group.sendMessage(message
                                .append("原动态：\n")
                                .append(info.get("origin_user").getAsJsonObject().get("info").getAsJsonObject().get("uname").getAsString())
                                .append("-->\n")
                                .append(text)
                                .append("\n")
                                .append(imageChain)
                                .build());
                        break;
                    /*纯文本类型*/
                    case 4:
                        origin = originJson.get("item").getAsJsonObject();
                        group.sendMessage(message
                                .append("原动态：\n")
                                .append(info.get("origin_user").getAsJsonObject().get("info").getAsJsonObject().get("uname").getAsString())
                                .append("-->\n")
                                .append(origin.getAsJsonObject().get("content").getAsString())
                                .build());
                        break;
                    /*带视频类型*/
                    case 8:
                        group.sendMessage(message
                                .append("原动态：\n")
                                .append(info.get("origin_user").getAsJsonObject().get("info").getAsJsonObject().get("uname").getAsString())
                                .append("-->\n")
                                .append(JsonParser.parseString(info.get("origin").getAsString()).getAsJsonObject()
                                        .get("desc").getAsString())
                                .append("\n")
                                .append(getVideoCoverImage(JsonParser.parseString(info.get("origin").getAsString()).getAsJsonObject()
                                        .get("pic").getAsString(), group))
                                .append("\nhttps://www.bilibili.com/video/")
                                .append(dynamic.get("desc").getAsJsonObject().get("origin").getAsJsonObject().get("bvid").getAsString())
                                .build());
                        break;
                    /*专栏类型*/
                    case 64:
                        group.sendMessage(message
                                .append("\n原动态（专栏）：\n")
                                .append(info.get("origin_user").getAsJsonObject().get("info").getAsJsonObject().get("uname").getAsString())
                                .append("-->\n")
                                .append(getArticleBannerImage(originJson.get("origin_image_urls").getAsString(), group))
                                .append("\n")
                                .append("标题：")
                                .append(originJson.get("title").getAsString())
                                .append("\n")
                                .append("概述：")
                                .append(originJson.get("summary").getAsString())
                                .append(".........\n").append("https://www.bilibili.com/read/cv").append(JsonParser.parseString(info.get("origin").getAsString())
                                        .getAsJsonObject().get("id").getAsString())
                                .build());
                        break;
                    default:
                        break;
                }   //转发类型end
                break;
            /*带图片类型*/
            case 2:
                MessageChain imageChain = getImagesMessage(info.get("item").getAsJsonObject().get("pictures").getAsJsonArray(), group);
                group.sendMessage(message
                        .append(face.plus(userInfo.get("uname").getAsString()).plus("-->\n"))
                        .append(info.get("item").getAsJsonObject().get("description").getAsString())
                        .append("\n")
                        .append(imageChain)
                        .build());
                break;
            /*纯文本类型*/
            case 4:
                group.sendMessage(message
                        .append(face.plus(userInfo.get("uname").getAsString()).plus("-->\n"))
                        .append(info.get("item").getAsJsonObject().get("content").getAsString())
                        .build());
                break;
            /*带视频类型*/
            case 8:
                group.sendMessage(message
                        .append(face.plus(userInfo.get("uname").getAsString()).plus("-->\n"))
                        .append(info.get("desc").getAsString())
                        .append("\n")
                        .append(getVideoCoverImage(info.get("pic").getAsString(), group))
                        .append("\n")
                        .append("https://www.bilibili.com/video/")
                        .append(dynamic.get("desc").getAsJsonObject().get("bvid").getAsString())
                        .build());
                break;
            /*专栏类型*/
            case 64:
                group.sendMessage(message
                        .append(face.plus(userInfo.get("uname").getAsString()).plus("-->\n"))
                        .append(getArticleBannerImage(info.get("origin_image_urls").getAsString(), group))
                        .append("\n")
                        .append("标题：")
                        .append(info.get("title").getAsString())
                        .append("\n")
                        .append("概述：")
                        .append(info.get("summary").getAsString())
                        .append(".........\n").append("https://www.bilibili.com/read/cv").append(JsonParser.parseString(info.get("origin").getAsString())
                                .getAsJsonObject().get("id").getAsString())
                        .build());
                break;
            default:
                break;
        }
    }

    /**
     * 读取Config内容
     *
     * @return 返回json对象
     */

    private static JsonObject readConfig() throws JsonSyntaxException, IllegalStateException {
        String temp;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(CONFIG), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            while ((temp = bufferedReader.readLine()) != null) {
                stringBuilder.append(temp);
            }
            bufferedReader.close();
            streamReader.close();
        } catch (IOException e) {
            BCRMain.INSTANCE.getLogger().warning("配置文件读取出错");
        }
        return JsonParser.parseString(stringBuilder.toString()).getAsJsonObject();
    }
    /*
     * JSON中type的值代表动态类型
     * 1：转发类型 （可有文本） 另外该类型中会额外有orig_type来表示原动态的类型，取值同理
     * 2：带图片的类型（可有文本）
     * 4：纯文本类型
     * 8：带视频的类型（可有文本）
     * 64：专栏类型（可有文本）
     * （迷惑）
     */

    /**
     * 获取监测到的最新动态的类型
     *
     * @param dynamic 最新动态的json对象
     * @return 返回动态的类型
     */
    private static int getDynamicType(JsonObject dynamic) {
        return dynamic.get("desc").getAsJsonObject().get("type").getAsInt();
    }

    /**
     * 当获取的最新动态为转发类型时，获取原动态的类型
     *
     * @param dynamic 最新动态的json对象
     * @return 返回动态的类型
     */

    private static int getOriginDynamicType(JsonObject dynamic) {
        return dynamic.get("desc").getAsJsonObject().get("orig_type").getAsInt();
    }

    /**
     * 获取ExternalResource
     *
     * @param url 准备获取的图片的url
     * @return 返回ExternalResource
     */
    private static ExternalResource getExternalResource(String url) throws IOException {
        ExternalResource externalResource;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        byte[] imageData = EntityUtils.toByteArray(entity);
        externalResource = ExternalResource.create(imageData);
        entity.close();
        response.close();
        client.close();
        return externalResource;
    }

    /**
     * 获取UP主头像
     */
    private static Image getUPerFace(String url, Group group) throws IOException {
        return getImage(url, group);
    }

    /**
     * 获取视频封面
     *
     * @param url   图片url
     * @param group Group
     * @return 返回视频封面
     */
    private static Image getVideoCoverImage(String url, Group group) throws IOException {
        return getImage(url, group);
    }

    /**
     * 获取文章专栏在动态中显示的图片
     *
     * @param url   图片url
     * @param group Group
     * @return 返回专栏图片
     */
    private static Image getArticleBannerImage(String url, Group group) throws IOException {
        return getImage(url, group);
    }

    /**
     * 实现获取图片
     *
     * @param url   图片url
     * @param group Group
     * @return 返回图片
     */
    private static Image getImage(String url, Group group) throws IOException {
        Image image;
        ExternalResource externalResource = getExternalResource(url);
        image = Contact.uploadImage(group, externalResource);
        try {
            externalResource.close();
        } catch (IOException e) {
            BCRMain.INSTANCE.getLogger().warning("ExternalResource failed to close");
        }
        return image;
    }

    /**
     * 构造图片消息链
     *
     * @param imageUrls 图片url
     * @param group     Group
     * @return 返回图片消息链
     */
    private static MessageChain getImagesMessage(JsonArray imageUrls, Group group) {
        MessageChainBuilder imageChain = new MessageChainBuilder();
        /*遍历获取图片*/
        for (JsonElement element : imageUrls) {
            String url = element.getAsJsonObject().get("img_src").getAsString();
            try {
                ExternalResource externalResource = getExternalResource(url);
                imageChain.append(Contact.uploadImage(group, externalResource));
                externalResource.close();
            } catch (IOException e) {
                BCRMain.INSTANCE.getLogger().warning("图片获取出错");
            }
        }
        return imageChain.build();
    }
}
